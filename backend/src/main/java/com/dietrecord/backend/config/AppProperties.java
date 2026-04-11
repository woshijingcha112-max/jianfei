package com.dietrecord.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Long fixedUserId = 1L;
    private final Storage storage = new Storage();

    public Long getFixedUserId() {
        return fixedUserId;
    }

    public void setFixedUserId(Long fixedUserId) {
        this.fixedUserId = fixedUserId;
    }

    public Storage getStorage() {
        return storage;
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
}
