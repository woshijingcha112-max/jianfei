package com.dietrecord.backend.modules.photo.model.internal;

public record ProcessedPhoto(
        byte[] content,
        String originalFilename,
        String contentType,
        String fileExtension,
        boolean compressed,
        int width,
        int height,
        long sizeBytes,
        String sha256
) {
}
