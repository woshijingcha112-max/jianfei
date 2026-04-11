package com.dietrecord.backend.modules.photo.service;

import com.dietrecord.backend.config.AppProperties;
import com.dietrecord.backend.modules.photo.model.internal.ProcessedPhoto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

/**
 * 图片落盘服务。
 */
@Service
@Slf4j
public class PhotoStorageService {

    private static final String DEFAULT_FOLDER = "photo";
    private static final String DEFAULT_EXTENSION = "png";

    private final AppProperties appProperties;

    public PhotoStorageService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public String store(ProcessedPhoto photo) {
        String uploadRoot = appProperties.getStorage().getUploadRoot();
        if (!StringUtils.hasText(uploadRoot)) {
            throw new IllegalArgumentException("uploadRoot is required");
        }

        LocalDate today = LocalDate.now();
        String year = String.format("%04d", today.getYear());
        String month = String.format("%02d", today.getMonthValue());
        String day = String.format("%02d", today.getDayOfMonth());

        String storedFileName = buildStoredFileName(photo.originalFilename(), photo.fileExtension());
        Path targetDirectory = Path.of(uploadRoot, DEFAULT_FOLDER, year, month, day);
        Path targetFile = targetDirectory.resolve(storedFileName);

        log.info("开始保存处理后图片，目标路径={}", targetFile);

        try {
            Files.createDirectories(targetDirectory);
            Files.write(targetFile, photo.content());
        } catch (IOException ex) {
            log.error("处理后图片保存失败，目标路径={}", targetFile, ex);
            throw new IllegalStateException("failed to store uploaded file", ex);
        }

        String accessUrl = normalizeAccessPrefix(appProperties.getStorage().getAccessPrefix())
                + "/"
                + String.join("/", DEFAULT_FOLDER, year, month, day, storedFileName);

        log.info("处理后图片保存完成，访问地址={}", accessUrl);
        return accessUrl;
    }

    public String store(MultipartFile file) {
        String uploadRoot = appProperties.getStorage().getUploadRoot();
        if (!StringUtils.hasText(uploadRoot)) {
            throw new IllegalArgumentException("uploadRoot is required");
        }

        LocalDate today = LocalDate.now();
        String year = String.format("%04d", today.getYear());
        String month = String.format("%02d", today.getMonthValue());
        String day = String.format("%02d", today.getDayOfMonth());

        String storedFileName = buildStoredFileName(file.getOriginalFilename(), null);
        Path targetDirectory = Path.of(uploadRoot, DEFAULT_FOLDER, year, month, day);
        Path targetFile = targetDirectory.resolve(storedFileName);

        log.info("开始保存原始上传文件，目标路径={}", targetFile);

        try {
            Files.createDirectories(targetDirectory);
            file.transferTo(targetFile);
        } catch (IOException ex) {
            log.error("原始上传文件保存失败，目标路径={}", targetFile, ex);
            throw new IllegalStateException("failed to store uploaded file", ex);
        }

        String accessUrl = normalizeAccessPrefix(appProperties.getStorage().getAccessPrefix())
                + "/"
                + String.join("/", DEFAULT_FOLDER, year, month, day, storedFileName);

        log.info("原始上传文件保存完成，访问地址={}", accessUrl);
        return accessUrl;
    }

    private String buildStoredFileName(String originalFilename, String fileExtension) {
        String cleanedName = StringUtils.getFilename(originalFilename);
        String extension = StringUtils.hasText(fileExtension) ? fileExtension : null;
        if (!StringUtils.hasText(extension) && StringUtils.hasText(cleanedName)) {
            String extractedExtension = StringUtils.getFilenameExtension(cleanedName);
            if (StringUtils.hasText(extractedExtension)) {
                extension = extractedExtension.toLowerCase(Locale.ROOT);
            }
        }
        if (!StringUtils.hasText(extension)) {
            extension = DEFAULT_EXTENSION;
        }
        return UUID.randomUUID().toString().replace("-", "") + "." + extension;
    }

    private String normalizeAccessPrefix(String accessPrefix) {
        String normalized = StringUtils.hasText(accessPrefix) ? accessPrefix.trim() : "/uploads";
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        while (normalized.endsWith("/") && normalized.length() > 1) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
