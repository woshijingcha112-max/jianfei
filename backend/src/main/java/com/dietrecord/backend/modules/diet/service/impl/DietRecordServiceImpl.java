package com.dietrecord.backend.modules.diet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dietrecord.backend.modules.diet.mapper.DietItemMapper;
import com.dietrecord.backend.modules.diet.mapper.DietRecordMapper;
import com.dietrecord.backend.modules.diet.model.dto.DietRecordItemDTO;
import com.dietrecord.backend.modules.diet.model.dto.DietRecordListDTO;
import com.dietrecord.backend.modules.diet.model.dto.DietRecordSaveDTO;
import com.dietrecord.backend.modules.diet.model.po.DietItemPO;
import com.dietrecord.backend.modules.diet.model.po.DietRecordPO;
import com.dietrecord.backend.modules.diet.model.vo.DietRecordCardVO;
import com.dietrecord.backend.modules.diet.service.DietRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DietRecordServiceImpl implements DietRecordService {

    private static final Long USER_ID = 1L;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final DietRecordMapper dietRecordMapper;
    private final DietItemMapper dietItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(DietRecordSaveDTO request) {
        LocalDateTime now = LocalDateTime.now();

        DietRecordPO recordPO = new DietRecordPO();
        recordPO.setUserId(USER_ID);
        recordPO.setRecordDate(request.recordDate());
        recordPO.setMealType(request.mealType());
        recordPO.setPhotoUrl(request.photoUrl());
        recordPO.setTotalCalories(sumCalories(request.items()));
        recordPO.setRemark(request.remark());
        recordPO.setCreatedAt(now);
        dietRecordMapper.insert(recordPO);

        for (DietRecordItemDTO item : request.items()) {
            DietItemPO itemPO = new DietItemPO();
            itemPO.setRecordId(recordPO.getId());
            itemPO.setFoodId(item.foodId());
            itemPO.setFoodName(item.foodName());
            itemPO.setWeightG(item.weightG());
            itemPO.setCalories(item.calories());
            itemPO.setTagColor(item.tagColor());
            itemPO.setIsConfirmed(item.isConfirmed() == null ? 0 : item.isConfirmed());
            dietItemMapper.insert(itemPO);
        }
    }

    @Override
    public List<DietRecordCardVO> list(DietRecordListDTO request) {
        List<DietRecordPO> recordPOList = dietRecordMapper.selectList(
                new LambdaQueryWrapper<DietRecordPO>()
                        .eq(DietRecordPO::getUserId, USER_ID)
                        .eq(DietRecordPO::getRecordDate, request.date())
                        .orderByDesc(DietRecordPO::getCreatedAt)
                        .orderByDesc(DietRecordPO::getId)
        );
        if (recordPOList.isEmpty()) {
            return List.of();
        }

        List<Long> recordIds = recordPOList.stream()
                .map(DietRecordPO::getId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, List<DietItemPO>> itemMap = dietItemMapper.selectList(
                        new LambdaQueryWrapper<DietItemPO>()
                                .in(DietItemPO::getRecordId, recordIds)
                ).stream()
                .collect(Collectors.groupingBy(DietItemPO::getRecordId));

        return recordPOList.stream()
                .map(recordPO -> toCardVO(recordPO, itemMap.getOrDefault(recordPO.getId(), List.of())))
                .toList();
    }

    private DietRecordCardVO toCardVO(DietRecordPO recordPO, List<DietItemPO> items) {
        String summary = items.stream()
                .map(DietItemPO::getFoodName)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining("、"));
        if (!StringUtils.hasText(summary)) {
            summary = "餐食";
        }

        Integer dominantTag = items.stream()
                .max(Comparator.comparing(item -> safeCalories(item.getCalories())))
                .map(DietItemPO::getTagColor)
                .orElse(2);

        LocalDateTime createdAt = recordPO.getCreatedAt() == null ? LocalDateTime.now() : recordPO.getCreatedAt();
        return new DietRecordCardVO(
                recordPO.getId(),
                createdAt.format(TIME_FORMATTER),
                summary,
                toCaloriesInt(recordPO.getTotalCalories()),
                dominantTag
        );
    }

    private BigDecimal sumCalories(List<DietRecordItemDTO> items) {
        return items.stream()
                .map(DietRecordItemDTO::calories)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal safeCalories(BigDecimal calories) {
        return calories == null ? BigDecimal.ZERO : calories;
    }

    private Integer toCaloriesInt(BigDecimal calories) {
        return safeCalories(calories).setScale(0, RoundingMode.HALF_UP).intValue();
    }
}
