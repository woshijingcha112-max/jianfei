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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 当日饮食统计服务实现。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DietStatServiceImpl implements DietStatService {

    private static final Long USER_ID = 1L;
    private static final DateTimeFormatter DATE_LABEL_FORMATTER = DateTimeFormatter.ofPattern("yyyy年M月d日");

    private final DietRecordMapper dietRecordMapper;
    private final UserProfileMapper userProfileMapper;

    @Override
    public TodayDietStatVO today(TodayDietStatDTO request) {
        log.info("开始查询当日饮食统计，日期={}", request.date());

        // 先读取固定用户档案，确保首页统计能拿到目标热量。
        UserProfilePO userProfilePO = userProfileMapper.selectById(USER_ID);
        if (userProfilePO == null) {
            log.error("查询当日饮食统计失败，原因=用户档案不存在，用户ID={}", USER_ID);
            throw new BizException(ApiCode.INTERNAL_ERROR.getCode(), "user profile not found");
        }

        // 再汇总指定日期的饮食记录热量，形成首页总览真值。
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

        log.info("当日饮食统计查询完成，日期={}，记录数={}，已摄入热量={}",
                request.date(), recordPOList.size(), consumedCalories);

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
