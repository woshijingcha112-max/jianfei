package com.dietrecord.backend.modules.photo.model.internal;

public record ProcessedPhoto(
        /** 图片二进制内容 */
        byte[] content,
        /** 原始文件名 */
        String originalFilename,
        /** 内容类型 */
        String contentType,
        /** 文件扩展名 */
        String fileExtension,
        /** 是否已压缩 */
        boolean compressed,
        /** 图片宽度 */
        int width,
        /** 图片高度 */
        int height,
        /** 图片字节数 */
        long sizeBytes,
        /** 图片 SHA-256 指纹 */
        String sha256
) {
}
