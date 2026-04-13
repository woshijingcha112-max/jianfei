package com.dietrecord.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /** 固定用户主键 */
    private Long fixedUserId = 1L;

    /** 存储配置 */
    private final Storage storage = new Storage();

    /** 图片配置 */
    private final Photo photo = new Photo();

    @Data
    public static class Storage {

        /** 上传根目录 */
        private String uploadRoot = "D:/data/diet-app/uploads";

        /** 上传访问前缀 */
        private String accessPrefix = "/uploads";
    }

    @Data
    public static class Photo {

        /** 图片处理配置 */
        private final Image image = new Image();

        /** AI 识别配置 */
        private final Ai ai = new Ai();
    }

    @Data
    public static class Image {

        /** 图片最短边下限 */
        private int minDimension = 15;

        /** 直传保留的最大字节数 */
        private long preserveMaxBytes = 1_200_000L;

        /** 压缩目标最大字节数 */
        private long compressedTargetMaxBytes = 900_000L;

        /** 图片最大边长 */
        private int maxDimension = 1920;

        /** JPEG 压缩质量 */
        private double jpegQuality = 0.88d;
    }

    @Data
    public static class Ai {

        /** 是否启用真实 AI 识别 */
        private boolean enabled = false;

        /** 当前主识别平台编码 */
        private String provider = "zhipu";

        /** 单次识别超时时间毫秒 */
        private int timeoutMillis = 45_000;

        /** 默认用餐类型 */
        private String defaultMealType = "待确认";

        /** 百度识别配置 */
        private final Baidu baidu = new Baidu();

        /** 智谱识别配置 */
        private final Zhipu zhipu = new Zhipu();
    }

    @Data
    public static class Baidu {

        /** 是否启用百度识别 */
        private boolean enabled = true;

        /** 百度应用 ID */
        private String appId = "";

        /** 百度 token 接口地址 */
        private String tokenEndpoint = "https://aip.baidubce.com/oauth/2.0/token";

        /** 百度菜品识别接口地址 */
        private String endpoint = "https://aip.baidubce.com/rest/2.0/image-classify/v2/dish";

        /** 百度 API Key */
        private String apiKey = "";

        /** 百度 Secret Key */
        private String secretKey = "";

        /** 返回候选数上限 */
        private int topNum = 5;

        /** 百科信息条数 */
        private int baikeNum = 1;

        /** token 提前刷新秒数 */
        private int tokenRefreshBeforeSeconds = 300;
    }

    @Data
    public static class Zhipu {

        /** 是否启用智谱识别 */
        private boolean enabled = true;

        /** 智谱接口地址 */
        private String endpoint = "https://open.bigmodel.cn/api/paas/v4/chat/completions";

        /** 智谱 API Key */
        private String apiKey = "";

        /** 智谱模型名 */
        private String model = "glm-4.6v";

        /** 最大输出 token 数 */
        private int maxTokens = 2048;

        /** 采样温度 */
        private double temperature = 0.1d;

        /** 自定义提示词 */
        private String prompt = "";
    }
}
