package com.dietrecord.backend.modules.photo.service;

import com.dietrecord.backend.config.AppProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Service
public class PhotoStorageService {

    private static final String DEFAULT_FOLDER = "photo";
    private static final String DEFAULT_EXTENSION = "png";

    private final AppProperties appProperties;

    public PhotoStorageService(AppProperties appProperties) {
        this.appProperties = appProperties;
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

        String storedFileName = buildStoredFileName(file.getOriginalFilename());
        Path targetDirectory = Path.of(uploadRoot, DEFAULT_FOLDER, year, month, day);
        Path targetFile = targetDirectory.resolve(storedFileName);

        try {
            Files.createDirectories(targetDirectory);
            file.transferTo(targetFile);
        } catch (IOException ex) {
            throw new IllegalStateException("failed to store uploaded file", ex);
        }

        return normalizeAccessPrefix(appProperties.getStorage().getAccessPrefix())
                + "/"
                + String.join("/", DEFAULT_FOLDER, year, month, day, storedFileName);
    }

    private String buildStoredFileName(String originalFilename) {
        String cleanedName = StringUtils.getFilename(originalFilename);
        String extension = DEFAULT_EXTENSION;
        if (StringUtils.hasText(cleanedName)) {
            String extractedExtension = StringUtils.getFilenameExtension(cleanedName);
            if (StringUtils.hasText(extractedExtension)) {
                extension = extractedExtension.toLowerCase(Locale.ROOT);
            }
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
