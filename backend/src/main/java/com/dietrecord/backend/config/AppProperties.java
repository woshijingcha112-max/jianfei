package com.dietrecord.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Long fixedUserId = 1L;
    private final Storage storage = new Storage();
    private final Photo photo = new Photo();

    public Long getFixedUserId() {
        return fixedUserId;
    }

    public void setFixedUserId(Long fixedUserId) {
        this.fixedUserId = fixedUserId;
    }

    public Storage getStorage() {
        return storage;
    }

    public Photo getPhoto() {
        return photo;
    }

    public static class Storage {

        private String uploadRoot = "D:/data/diet-app/uploads";
        private String accessPrefix = "/uploads";

        public String getUploadRoot() {
            return uploadRoot;
        }

        public void setUploadRoot(String uploadRoot) {
            this.uploadRoot = uploadRoot;
        }

        public String getAccessPrefix() {
            return accessPrefix;
        }

        public void setAccessPrefix(String accessPrefix) {
            this.accessPrefix = accessPrefix;
        }
    }

    public static class Photo {

        private final Image image = new Image();
        private final Ai ai = new Ai();

        public Image getImage() {
            return image;
        }

        public Ai getAi() {
            return ai;
        }
    }

    public static class Image {

        private long preserveMaxBytes = 1_200_000L;
        private long compressedTargetMaxBytes = 900_000L;
        private int maxDimension = 1920;
        private double jpegQuality = 0.88d;

        public long getPreserveMaxBytes() {
            return preserveMaxBytes;
        }

        public void setPreserveMaxBytes(long preserveMaxBytes) {
            this.preserveMaxBytes = preserveMaxBytes;
        }

        public long getCompressedTargetMaxBytes() {
            return compressedTargetMaxBytes;
        }

        public void setCompressedTargetMaxBytes(long compressedTargetMaxBytes) {
            this.compressedTargetMaxBytes = compressedTargetMaxBytes;
        }

        public int getMaxDimension() {
            return maxDimension;
        }

        public void setMaxDimension(int maxDimension) {
            this.maxDimension = maxDimension;
        }

        public double getJpegQuality() {
            return jpegQuality;
        }

        public void setJpegQuality(double jpegQuality) {
            this.jpegQuality = jpegQuality;
        }
    }

    public static class Ai {

        private boolean enabled = false;
        private String provider = "duijieai";
        private String endpoint = "https://replace-with-duijieai-endpoint";
        private String apiKey = "";
        private String model = "replace-me";
        private int timeoutMillis = 8_000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public int getTimeoutMillis() {
            return timeoutMillis;
        }

        public void setTimeoutMillis(int timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
        }
    }
}
