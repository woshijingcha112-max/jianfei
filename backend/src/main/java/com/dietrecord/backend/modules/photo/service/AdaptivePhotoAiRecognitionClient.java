package com.dietrecord.backend.modules.photo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dietrecord.backend.config.AppProperties;
import com.dietrecord.backend.modules.food.mapper.FoodLibraryMapper;
import com.dietrecord.backend.modules.food.model.po.FoodLibraryPO;
import com.dietrecord.backend.modules.photo.model.internal.PhotoAiProviderResult;
import com.dietrecord.backend.modules.photo.model.internal.PhotoAiRecognitionCandidate;
import com.dietrecord.backend.modules.photo.model.internal.PhotoAiRecognitionContext;
import com.dietrecord.backend.modules.photo.model.internal.PhotoAiRecognitionResult;
import com.dietrecord.backend.modules.photo.model.internal.PhotoAiStructuredIngredient;
import com.dietrecord.backend.modules.photo.model.internal.ProcessedPhoto;
import com.dietrecord.backend.modules.photo.model.vo.PhotoStructuredResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 图片识别客户端自适配实现。
 */
@Service
@Slf4j
public class AdaptivePhotoAiRecognitionClient implements PhotoAiRecognitionClient {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final AppProperties appProperties;
    private final FoodLibraryMapper foodLibraryMapper;
    private final Map<String, PhotoAiProvider> providerMap;

    public AdaptivePhotoAiRecognitionClient(AppProperties appProperties,
                                            FoodLibraryMapper foodLibraryMapper,
                                            List<PhotoAiProvider> providers) {
        this.appProperties = appProperties;
        this.foodLibraryMapper = foodLibraryMapper;
        this.providerMap = providers.stream().collect(Collectors.toMap(
                provider -> provider.providerCode().toLowerCase(Locale.ROOT),
                Function.identity()
        ));
    }

    @Override
    public PhotoAiRecognitionResult recognize(ProcessedPhoto processedPhoto, String photoUrl) {
        PhotoAiRecognitionContext context = new PhotoAiRecognitionContext(
                UUID.randomUUID().toString(),
                processedPhoto,
                photoUrl,
                defaultMealType(),
                LocalDate.now(),
                LocalDateTime.now()
        );
        long totalStartNanos = System.nanoTime();

        log.info("图片识别编排开始，requestId={}，provider={}，photoUrl={}，originalFilename={}，imageBytes={}，imageWidth={}，imageHeight={}",
                context.requestId(),
                appProperties.getPhoto().getAi().getProvider(),
                photoUrl,
                processedPhoto.originalFilename(),
                processedPhoto.sizeBytes(),
                processedPhoto.width(),
                processedPhoto.height());

        AppProperties.Ai aiProperties = appProperties.getPhoto().getAi();
        if (!aiProperties.isEnabled()) {
            log.info("AI 真实识别未启用，回退本地候选，文件名={}", processedPhoto.originalFilename());
            PhotoAiProviderResult fallbackResult = fallbackRecognize(processedPhoto);
            return new PhotoAiRecognitionResult(
                    fallbackResult.candidates(),
                    buildStructuredResult(context, fallbackResult, null)
            );
        }

        PhotoAiProviderResult mainResult = invokeProvider(aiProperties.getProvider(), context, true);
        PhotoAiProviderResult baiduValidationResult = null;

        // 当前 MVP 先只保留多模态主识别链路，避免为了更快首屏反馈提前把双调用流程做重。
        // 后续若要提升体验，可改为“百度主结果前置，多模态补充食材明细”：
        // 先把百度的菜品级结果立即返回前端，再异步或二段式补齐多模态食材明细。
        if (aiProperties.getBaidu().isEnabled() && !"baidu".equalsIgnoreCase(aiProperties.getProvider())) {
            baiduValidationResult = invokeProvider("baidu", context, false);
        } else if (!aiProperties.getBaidu().isEnabled()) {
            log.info("当前配置已关闭百度菜品识别调用，requestId={}", context.requestId());
        }

        PhotoAiProviderResult effectiveResult = chooseEffectiveResult(mainResult, baiduValidationResult, processedPhoto);
        if (baiduValidationResult == null && "baidu".equalsIgnoreCase(effectiveResult.providerCode())) {
            baiduValidationResult = effectiveResult;
        }

        PhotoAiRecognitionResult recognitionResult = new PhotoAiRecognitionResult(
                effectiveResult.candidates(),
                buildStructuredResult(context, effectiveResult, baiduValidationResult)
        );
        long totalElapsedMillis = Duration.ofNanos(System.nanoTime() - totalStartNanos).toMillis();

        log.info("图片识别编排完成，requestId={}，mainProvider={}，effectiveProvider={}，candidateCount={}，structuredDishName={}，totalElapsedMs={}",
                context.requestId(),
                aiProperties.getProvider(),
                effectiveResult.providerCode(),
                recognitionResult.candidates().size(),
                recognitionResult.structuredResult().recognitionPayload().wholeDishInfo().dishName(),
                totalElapsedMillis);
        return recognitionResult;
    }

    private PhotoAiProviderResult chooseEffectiveResult(PhotoAiProviderResult mainResult,
                                                        PhotoAiProviderResult baiduValidationResult,
                                                        ProcessedPhoto processedPhoto) {
        if (mainResult != null && mainResult.hasCandidates()) {
            return mainResult;
        }

        if (baiduValidationResult != null && baiduValidationResult.hasCandidates()) {
            log.warn("主识别未返回有效候选，降级使用百度菜品识别结果，文件名={}", processedPhoto.originalFilename());
            return baiduValidationResult;
        }

        log.warn("主识别与百度校验都未返回有效候选，回退本地候选，文件名={}", processedPhoto.originalFilename());
        return fallbackRecognize(processedPhoto);
    }

    private PhotoAiProviderResult invokeProvider(String providerCode,
                                                 PhotoAiRecognitionContext context,
                                                 boolean mainProvider) {
        if (!StringUtils.hasText(providerCode)) {
            return null;
        }

        PhotoAiProvider provider = providerMap.get(providerCode.trim().toLowerCase(Locale.ROOT));
        if (provider == null) {
            log.warn("未找到图片识别 provider，provider={}", providerCode);
            return null;
        }

        if (!provider.isAvailable()) {
            log.warn("图片识别 provider 当前不可用，provider={}", providerCode);
            return null;
        }

        try {
            long startNanos = System.nanoTime();
            PhotoAiProviderResult result = provider.recognize(context);
            long elapsedMillis = Duration.ofNanos(System.nanoTime() - startNanos).toMillis();
            log.info("{} provider 调用完成，requestId={}，provider={}，候选数={}，dishName={}，elapsedMs={}",
                    mainProvider ? "主识别" : "辅助识别",
                    context.requestId(),
                    provider.providerCode(),
                    result.candidates().size(),
                    result.dishName(),
                    elapsedMillis);
            return result;
        } catch (Exception ex) {
            log.warn("{} provider 调用失败，requestId={}，provider={}，原因={}",
                    mainProvider ? "主识别" : "辅助识别",
                    context.requestId(),
                    provider.providerCode(),
                    ex.getMessage());
            return null;
        }
    }

    private PhotoAiProviderResult fallbackRecognize(ProcessedPhoto processedPhoto) {
        List<FoodLibraryPO> foods = foodLibraryMapper.selectList(
                new LambdaQueryWrapper<FoodLibraryPO>().orderByAsc(FoodLibraryPO::getId)
        );
        if (foods.isEmpty()) {
            log.warn("食物库为空，无法生成本地回退候选，文件名={}", processedPhoto.originalFilename());
            return new PhotoAiProviderResult(
                    "local-fallback",
                    "待确认",
                    null,
                    "待确认",
                    "未接入有效模型结果，使用本地回退候选",
                    List.of(),
                    BigDecimal.ZERO,
                    Boolean.TRUE,
                    "AI 结果为空，已回退到本地候选",
                    List.of()
            );
        }

        long seed = decodeSeed(processedPhoto.sha256());
        int candidateCount = Math.min(3, foods.size());
        int startIndex = (int) Math.floorMod(seed, foods.size());

        List<PhotoAiRecognitionCandidate> candidates = new ArrayList<>();
        List<PhotoAiStructuredIngredient> ingredients = new ArrayList<>();
        BigDecimal totalCalories = BigDecimal.ZERO;

        for (int i = 0; i < candidateCount; i++) {
            FoodLibraryPO food = foods.get((startIndex + i) % foods.size());
            BigDecimal confidence = BigDecimal.valueOf(Math.max(0.55d, 0.78d - (i * 0.07d)));
            BigDecimal weightG = BigDecimal.valueOf(Math.max(60L, 120L - (i * 20L)));
            BigDecimal calories = food.getCaloriesKcal() == null
                    ? BigDecimal.ZERO
                    : food.getCaloriesKcal().multiply(weightG).divide(BigDecimal.valueOf(100), 1, RoundingMode.HALF_UP);

            candidates.add(new PhotoAiRecognitionCandidate(
                    food.getFoodName(),
                    confidence,
                    weightG,
                    calories,
                    food.getTagColor())
            );
            ingredients.add(new PhotoAiStructuredIngredient(
                    food.getFoodName(),
                    confidence,
                    weightG,
                    calories,
                    defaultText(food.getCategory(), "待确认"),
                    "待确认",
                    "本地回退"
            ));
            totalCalories = totalCalories.add(calories);
        }

        return new PhotoAiProviderResult(
                "local-fallback",
                ingredients.get(0).foodName(),
                ingredients.get(0).confidence(),
                "待确认",
                "未获取到稳定 AI 结果，已回退到本地候选食物",
                ingredients,
                totalCalories,
                Boolean.TRUE,
                "AI 结果为空，已回退到本地候选",
                candidates
        );
    }

    private PhotoStructuredResultVO buildStructuredResult(PhotoAiRecognitionContext context,
                                                          PhotoAiProviderResult mainResult,
                                                          PhotoAiProviderResult baiduValidationResult) {
        List<PhotoAiStructuredIngredient> ingredients = resolveIngredients(mainResult);
        PhotoAiRecognitionCandidate primaryCandidate = firstCandidate(mainResult);
        PhotoAiRecognitionCandidate validationCandidate = firstCandidate(baiduValidationResult);

        String dishName = firstNonBlank(
                mainResult.dishName(),
                primaryCandidate == null ? null : primaryCandidate.foodName(),
                "待确认"
        );
        BigDecimal dishConfidence = mainResult.dishConfidence() != null
                ? mainResult.dishConfidence()
                : primaryCandidate == null ? null : primaryCandidate.confidence();

        BigDecimal totalCalories = resolveTotalCalories(mainResult, ingredients);
        String validationConclusion = buildValidationConclusion(dishName, validationCandidate);
        boolean needManualConfirm = resolveNeedManualConfirm(mainResult, validationConclusion, ingredients);
        String manualConfirmReason = buildManualConfirmReason(mainResult, validationConclusion, ingredients, needManualConfirm);

        List<PhotoStructuredResultVO.IngredientDetail> ingredientDetails = ingredients.stream()
                .map(item -> new PhotoStructuredResultVO.IngredientDetail(
                        item.foodName(),
                        formatDecimal(item.confidence()),
                        formatDecimal(item.weightG()),
                        formatDecimal(item.calories()),
                        defaultText(item.category(), "待确认"),
                        defaultText(item.cookingState(), "待确认"),
                        defaultText(item.source(), providerDisplayName(mainResult.providerCode()))
                ))
                .toList();

        return new PhotoStructuredResultVO(
                new PhotoStructuredResultVO.RecognitionPayload(
                        context.requestId(),
                        context.photoUrl(),
                        context.recognizedAt().format(DATE_TIME_FORMATTER),
                        context.mealType(),
                        context.recordDate().format(DATE_FORMATTER),
                        new PhotoStructuredResultVO.WholeDishInfo(
                                dishName,
                                formatDecimal(dishConfidence),
                                defaultText(mainResult.dishCategory(), "待确认"),
                                defaultText(mainResult.sceneDescription(), buildSceneDescription(dishName, ingredients))
                        ),
                        ingredientDetails,
                        new PhotoStructuredResultVO.SummaryInfo(
                                formatDecimal(totalCalories),
                                needManualConfirm ? "是" : "否",
                                needManualConfirm ? manualConfirmReason : ""
                        ),
                        new PhotoStructuredResultVO.ValidationInfo(
                                validationCandidate == null ? "" : defaultText(validationCandidate.foodName(), ""),
                                validationCandidate == null ? "" : formatDecimal(validationCandidate.confidence()),
                                validationCandidate == null ? "" : formatDecimal(validationCandidate.calories()),
                                validationConclusion
                        )
                )
        );
    }

    private List<PhotoAiStructuredIngredient> resolveIngredients(PhotoAiProviderResult mainResult) {
        if (mainResult.hasIngredients()) {
            return mainResult.ingredients();
        }

        List<PhotoAiStructuredIngredient> ingredients = new ArrayList<>();
        for (PhotoAiRecognitionCandidate candidate : mainResult.candidates()) {
            ingredients.add(new PhotoAiStructuredIngredient(
                    candidate.foodName(),
                    candidate.confidence(),
                    candidate.weightG(),
                    candidate.calories(),
                    "待确认",
                    "待确认",
                    providerDisplayName(mainResult.providerCode())
            ));
        }
        return ingredients;
    }

    private BigDecimal resolveTotalCalories(PhotoAiProviderResult mainResult,
                                            List<PhotoAiStructuredIngredient> ingredients) {
        if (mainResult.totalCalories() != null) {
            return mainResult.totalCalories().setScale(1, RoundingMode.HALF_UP);
        }

        BigDecimal totalCalories = BigDecimal.ZERO;
        for (PhotoAiStructuredIngredient ingredient : ingredients) {
            if (ingredient.calories() != null) {
                totalCalories = totalCalories.add(ingredient.calories());
            }
        }
        return totalCalories.setScale(1, RoundingMode.HALF_UP);
    }

    private boolean resolveNeedManualConfirm(PhotoAiProviderResult mainResult,
                                             String validationConclusion,
                                             List<PhotoAiStructuredIngredient> ingredients) {
        if (Boolean.TRUE.equals(mainResult.needManualConfirm())) {
            return true;
        }

        if (ingredients.isEmpty()) {
            return true;
        }

        return validationConclusion.contains("不一致") || validationConclusion.contains("未执行");
    }

    private String buildManualConfirmReason(PhotoAiProviderResult mainResult,
                                            String validationConclusion,
                                            List<PhotoAiStructuredIngredient> ingredients,
                                            boolean needManualConfirm) {
        if (!needManualConfirm) {
            return "";
        }

        List<String> reasons = new ArrayList<>();
        if (StringUtils.hasText(mainResult.manualConfirmReason())) {
            reasons.add(mainResult.manualConfirmReason());
        }
        if (ingredients.isEmpty()) {
            reasons.add("未识别出稳定食材明细");
        }
        if (validationConclusion.contains("不一致")) {
            reasons.add("主识别结果与百度菜品级判断不一致");
        } else if (validationConclusion.contains("未执行")) {
            reasons.add("本次未完成百度校验");
        }
        return String.join("；", reasons.stream().filter(StringUtils::hasText).distinct().toList());
    }

    private String buildValidationConclusion(String mainDishName, PhotoAiRecognitionCandidate validationCandidate) {
        if (validationCandidate == null || !StringUtils.hasText(validationCandidate.foodName())) {
            return "未执行百度校验";
        }

        if (!StringUtils.hasText(mainDishName)) {
            return "主识别结果缺少菜名，建议人工确认";
        }

        String normalizedMainDish = normalizeText(mainDishName);
        String normalizedValidationDish = normalizeText(validationCandidate.foodName());
        if (!StringUtils.hasText(normalizedMainDish) || !StringUtils.hasText(normalizedValidationDish)) {
            return "菜名信息不完整，建议人工确认";
        }

        if (normalizedMainDish.contains(normalizedValidationDish)
                || normalizedValidationDish.contains(normalizedMainDish)) {
            return "主识别结果与百度菜品级判断一致";
        }
        return "主识别结果与百度菜品级判断不一致，建议人工确认";
    }

    private String buildSceneDescription(String dishName, List<PhotoAiStructuredIngredient> ingredients) {
        if (ingredients.isEmpty()) {
            return "图片中是一道餐食，当前缺少稳定的食材级细节。";
        }

        String ingredientSummary = ingredients.stream()
                .map(PhotoAiStructuredIngredient::foodName)
                .filter(StringUtils::hasText)
                .limit(5)
                .collect(Collectors.joining("、"));
        return "图片中是一份" + dishName + "，可见食材包括" + ingredientSummary + "。";
    }

    private PhotoAiRecognitionCandidate firstCandidate(PhotoAiProviderResult result) {
        if (result == null || result.candidates().isEmpty()) {
            return null;
        }

        return result.candidates().stream()
                .filter(candidate -> candidate != null && StringUtils.hasText(candidate.foodName()))
                .max(Comparator.comparing(PhotoAiRecognitionCandidate::confidence,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(result.candidates().get(0));
    }

    private String providerDisplayName(String providerCode) {
        if ("zhipu".equalsIgnoreCase(providerCode)) {
            return "智谱清言";
        }
        if ("baidu".equalsIgnoreCase(providerCode)) {
            return "百度菜品识别";
        }
        if ("local-fallback".equalsIgnoreCase(providerCode)) {
            return "本地回退";
        }
        return "待确认";
    }

    private String defaultMealType() {
        String defaultMealType = appProperties.getPhoto().getAi().getDefaultMealType();
        return StringUtils.hasText(defaultMealType) ? defaultMealType.trim() : "待确认";
    }

    private long decodeSeed(String value) {
        if (!StringUtils.hasText(value)) {
            return UUID.randomUUID().getMostSignificantBits();
        }

        long hash = 1125899906842597L;
        for (int i = 0; i < value.length(); i++) {
            hash = 31 * hash + value.charAt(i);
        }
        return hash;
    }

    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[\\s_\\-./()\\[\\]{}]+", "");
    }

    private String formatDecimal(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return value.stripTrailingZeros().toPlainString();
    }

    private String defaultText(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return "";
    }
}
