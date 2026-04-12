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

        private int minDimension = 15;
        private long preserveMaxBytes = 1_200_000L;
        private long compressedTargetMaxBytes = 900_000L;
        private int maxDimension = 1920;
        private double jpegQuality = 0.88d;

        public int getMinDimension() {
            return minDimension;
        }

        public void setMinDimension(int minDimension) {
            this.minDimension = minDimension;
        }

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
        private String provider = "zhipu";
        private int timeoutMillis = 45_000;
        private String defaultMealType = "待确认";
        private final Baidu baidu = new Baidu();
        private final Zhipu zhipu = new Zhipu();

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

        public int getTimeoutMillis() {
            return timeoutMillis;
        }

        public void setTimeoutMillis(int timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
        }

        public String getDefaultMealType() {
            return defaultMealType;
        }

        public void setDefaultMealType(String defaultMealType) {
            this.defaultMealType = defaultMealType;
        }

        public Baidu getBaidu() {
            return baidu;
        }

        public Zhipu getZhipu() {
            return zhipu;
        }
    }

    public static class Baidu {

        private boolean enabled = true;
        private String appId = "";
        private String tokenEndpoint = "https://aip.baidubce.com/oauth/2.0/token";
        private String endpoint = "https://aip.baidubce.com/rest/2.0/image-classify/v2/dish";
        private String apiKey = "";
        private String secretKey = "";
        private int topNum = 5;
        private int baikeNum = 1;
        private int tokenRefreshBeforeSeconds = 300;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getTokenEndpoint() {
            return tokenEndpoint;
        }

        public void setTokenEndpoint(String tokenEndpoint) {
            this.tokenEndpoint = tokenEndpoint;
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

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public int getTopNum() {
            return topNum;
        }

        public void setTopNum(int topNum) {
            this.topNum = topNum;
        }

        public int getBaikeNum() {
            return baikeNum;
        }

        public void setBaikeNum(int baikeNum) {
            this.baikeNum = baikeNum;
        }

        public int getTokenRefreshBeforeSeconds() {
            return tokenRefreshBeforeSeconds;
        }

        public void setTokenRefreshBeforeSeconds(int tokenRefreshBeforeSeconds) {
            this.tokenRefreshBeforeSeconds = tokenRefreshBeforeSeconds;
        }
    }

    public static class Zhipu {

        private boolean enabled = true;
        private String endpoint = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
        private String apiKey = "";
        private String model = "glm-4.6v";
        private int maxTokens = 2048;
        private double temperature = 0.1d;
        private String prompt = "";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
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

        public int getMaxTokens() {
            return maxTokens;
        }

        public void setMaxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
        }

        public double getTemperature() {
            return temperature;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }
    }
}
