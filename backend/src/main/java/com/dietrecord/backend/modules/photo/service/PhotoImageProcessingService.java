package com.dietrecord.backend.modules.photo.service;

import com.dietrecord.backend.config.AppProperties;
import com.dietrecord.backend.modules.photo.model.internal.ProcessedPhoto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Iterator;
import java.util.Locale;

/**
 * 图片压缩与格式规整服务。
 */
@Service
@Slf4j
public class PhotoImageProcessingService {

    private static final String DEFAULT_IMAGE_EXTENSION = "jpg";

    private final AppProperties appProperties;

    public PhotoImageProcessingService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public ProcessedPhoto process(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is required");
        }

        byte[] originalBytes;
        try {
            originalBytes = file.getBytes();
        } catch (IOException ex) {
            log.error("读取上传图片失败，文件名={}", file.getOriginalFilename(), ex);
            throw new IllegalStateException("failed to read uploaded file", ex);
        }
        if (originalBytes.length == 0) {
            throw new IllegalArgumentException("file is empty");
        }

        BufferedImage sourceImage = readImage(originalBytes);
        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();
        String originalFilename = file.getOriginalFilename();
        String originalFormat = resolveFormat(originalFilename, file.getContentType());

        log.info("开始处理上传图片，文件名={}，原始大小={}字节，宽={}，高={}",
                originalFilename, originalBytes.length, width, height);

        int minDimension = appProperties.getPhoto().getImage().getMinDimension();
        boolean withinSize = originalBytes.length <= appProperties.getPhoto().getImage().getPreserveMaxBytes();
        boolean withinMaxDimension = Math.max(width, height) <= appProperties.getPhoto().getImage().getMaxDimension();
        boolean withinMinDimension = Math.min(width, height) >= minDimension;
        if (withinSize && withinMaxDimension && withinMinDimension) {
            log.info("图片满足直传条件，无需压缩，文件名={}", originalFilename);
            return new ProcessedPhoto(
                    originalBytes,
                    originalFilename,
                    normalizeContentType(originalFormat, file.getContentType()),
                    normalizeExtension(originalFormat),
                    false,
                    width,
                    height,
                    originalBytes.length,
                    sha256(originalBytes)
            );
        }

        // 先把图片尺寸收口到百度可接受范围，再根据目标体积继续压缩。
        boolean hasAlpha = sourceImage.getColorModel().hasAlpha();
        BufferedImage resizedImage = normalizeDimension(
                sourceImage,
                appProperties.getPhoto().getImage().getMinDimension(),
                appProperties.getPhoto().getImage().getMaxDimension()
        );
        String outputFormat = chooseOutputFormat(originalFormat, hasAlpha);
        byte[] encodedBytes = encodeImage(resizedImage, outputFormat, appProperties.getPhoto().getImage().getJpegQuality());
        long targetMaxBytes = appProperties.getPhoto().getImage().getCompressedTargetMaxBytes();

        // 当编码后体积仍超阈值时，继续逐步缩小尺寸，直到达到目标或触达下限。
        while (encodedBytes.length > targetMaxBytes && Math.max(resizedImage.getWidth(), resizedImage.getHeight()) > 640) {
            int nextWidth = Math.max(1, (int) Math.round(resizedImage.getWidth() * 0.85d));
            int nextHeight = Math.max(1, (int) Math.round(resizedImage.getHeight() * 0.85d));
            if (nextWidth == resizedImage.getWidth() && nextHeight == resizedImage.getHeight()) {
                break;
            }
            resizedImage = resizeToExact(resizedImage, nextWidth, nextHeight);
            encodedBytes = encodeImage(resizedImage, outputFormat, appProperties.getPhoto().getImage().getJpegQuality());
        }

        log.info("图片处理完成，文件名={}，输出格式={}，压缩后大小={}字节，宽={}，高={}",
                originalFilename, outputFormat, encodedBytes.length, resizedImage.getWidth(), resizedImage.getHeight());

        return new ProcessedPhoto(
                encodedBytes,
                originalFilename,
                normalizeContentType(outputFormat, file.getContentType()),
                normalizeExtension(outputFormat),
                true,
                resizedImage.getWidth(),
                resizedImage.getHeight(),
                encodedBytes.length,
                sha256(encodedBytes)
        );
    }

    private BufferedImage readImage(byte[] content) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content)) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new IllegalArgumentException("uploaded file is not a supported image");
            }
            return image;
        } catch (IOException ex) {
            throw new IllegalStateException("failed to decode uploaded image", ex);
        }
    }

    private BufferedImage normalizeDimension(BufferedImage image, int minDimension, int maxDimension) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage normalizedImage = image;

        int currentMin = Math.min(width, height);
        if (currentMin < minDimension) {
            double upscale = (double) minDimension / currentMin;
            int targetWidth = Math.max(1, (int) Math.round(width * upscale));
            int targetHeight = Math.max(1, (int) Math.round(height * upscale));
            normalizedImage = resizeToExact(normalizedImage, targetWidth, targetHeight);
            width = normalizedImage.getWidth();
            height = normalizedImage.getHeight();
        }

        int currentMax = Math.max(width, height);
        if (currentMax <= maxDimension) {
            return normalizedImage;
        }

        double downscale = (double) maxDimension / currentMax;
        int targetWidth = Math.max(1, (int) Math.round(width * downscale));
        int targetHeight = Math.max(1, (int) Math.round(height * downscale));
        return resizeToExact(normalizedImage, targetWidth, targetHeight);
    }

    private BufferedImage resizeToExact(BufferedImage source, int targetWidth, int targetHeight) {
        BufferedImage target = new BufferedImage(
                targetWidth,
                targetHeight,
                source.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB
        );
        Graphics2D graphics = target.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (source.getColorModel().hasAlpha()) {
                graphics.setComposite(AlphaComposite.Src);
            } else {
                graphics.setColor(Color.WHITE);
                graphics.fillRect(0, 0, targetWidth, targetHeight);
            }
            graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        } finally {
            graphics.dispose();
        }
        return target;
    }

    private String chooseOutputFormat(String originalFormat, boolean hasAlpha) {
        if (hasAlpha) {
            return "png";
        }
        if ("png".equals(originalFormat)) {
            return "jpg";
        }
        if ("jpeg".equals(originalFormat) || "jpg".equals(originalFormat)) {
            return "jpg";
        }
        return "jpg";
    }

    private byte[] encodeImage(BufferedImage image, String format, double jpegQuality) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if ("jpg".equals(format) || "jpeg".equals(format)) {
                BufferedImage rgbImage = image.getColorModel().hasAlpha() ? convertToRgb(image) : image;
                Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
                if (!writers.hasNext()) {
                    throw new IllegalStateException("jpeg writer is unavailable");
                }
                ImageWriter writer = writers.next();
                try (ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream)) {
                    writer.setOutput(imageOutputStream);
                    ImageWriteParam writeParam = writer.getDefaultWriteParam();
                    if (writeParam.canWriteCompressed()) {
                        writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                        writeParam.setCompressionQuality((float) jpegQuality);
                    }
                    writer.write(null, new IIOImage(rgbImage, null, null), writeParam);
                } finally {
                    writer.dispose();
                }
            } else if (!ImageIO.write(image, format, outputStream)) {
                throw new IllegalStateException("image format is not supported: " + format);
            }
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("failed to encode image", ex);
        }
    }

    private BufferedImage convertToRgb(BufferedImage source) {
        BufferedImage target = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = target.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, source.getWidth(), source.getHeight());
            graphics.drawImage(source, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return target;
    }

    private String resolveFormat(String originalFilename, String contentType) {
        if (StringUtils.hasText(contentType) && contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            String type = contentType.substring("image/".length()).toLowerCase(Locale.ROOT);
            if ("jpeg".equals(type) || "jpg".equals(type) || "png".equals(type)) {
                return type;
            }
        }
        if (StringUtils.hasText(originalFilename)) {
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > -1 && dotIndex < originalFilename.length() - 1) {
                String extension = originalFilename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
                if ("jpeg".equals(extension) || "jpg".equals(extension) || "png".equals(extension)) {
                    return extension;
                }
            }
        }
        return DEFAULT_IMAGE_EXTENSION;
    }

    private String normalizeExtension(String format) {
        if (!StringUtils.hasText(format)) {
            return DEFAULT_IMAGE_EXTENSION;
        }
        String normalized = format.toLowerCase(Locale.ROOT);
        if ("jpeg".equals(normalized)) {
            return "jpg";
        }
        return normalized;
    }

    private String normalizeContentType(String format, String fallbackContentType) {
        if ("png".equals(format)) {
            return "image/png";
        }
        if ("jpg".equals(format) || "jpeg".equals(format)) {
            return "image/jpeg";
        }
        if (StringUtils.hasText(fallbackContentType) && fallbackContentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            return fallbackContentType;
        }
        return "image/jpeg";
    }

    private String sha256(byte[] content) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(content);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("sha-256 digest is unavailable", ex);
        }
    }
}
