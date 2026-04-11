package com.dietrecord.backend.modules.diet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dietrecord.backend.common.api.ApiCode;
import com.dietrecord.backend.common.exception.BizException;
import com.dietrecord.backend.modules.diet.mapper.DietRecordMapper;
import com.dietrecord.backend.modules.diet.model.dto.TodayDietStatDTO;
import com.dietrecord.backend.modules.diet.model.po.DietRecordPO;
import com.dietrecord.backend.modules.diet.model.vo.TodayDietStatVO;
import com.dietrecord.backend.modules.diet.service.DietStatService;
import com.dietrecord.backend.modules.user.mapper.UserProfileMapper;
import com.dietrecord.backend.modules.user.model.po.UserProfilePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DietStatServiceImpl implements DietStatService {

    private static final Long USER_ID = 1L;
    private static final DateTimeFormatter DATE_LABEL_FORMATTER = DateTimeFormatter.ofPattern("yyyy年M月d日");

    private final DietRecordMapper dietRecordMapper;
    private final UserProfileMapper userProfileMapper;

    @Override
    public TodayDietStatVO today(TodayDietStatDTO request) {
        UserProfilePO userProfilePO = userProfileMapper.selectById(USER_ID);
        if (userProfilePO == null) {
            throw new BizException(ApiCode.INTERNAL_ERROR.getCode(), "user profile not found");
        }

        List<DietRecordPO> recordPOList = dietRecordMapper.selectList(
                new LambdaQueryWrapper<DietRecordPO>()
                        .eq(DietRecordPO::getUserId, USER_ID)
                        .eq(DietRecordPO::getRecordDate, request.date())
        );

        int consumedCalories = recordPOList.stream()
                .map(DietRecordPO::getTotalCalories)
                .map(this::toCaloriesInt)
                .mapToInt(Integer::intValue)
                .sum();

        return new TodayDietStatVO(
                request.date().format(DATE_LABEL_FORMATTER),
                consumedCalories,
                userProfilePO.getDailyCalLimit() == null ? 0 : userProfilePO.getDailyCalLimit()
        );
    }

    private Integer toCaloriesInt(BigDecimal calories) {
        return (calories == null ? BigDecimal.ZERO : calories)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }
}
