package com.dietrecord.backend.modules.goal.service.impl;

import com.dietrecord.backend.common.api.ApiCode;
import com.dietrecord.backend.common.exception.BizException;
import com.dietrecord.backend.config.AppProperties;
import com.dietrecord.backend.modules.goal.model.dto.GoalSaveDTO;
import com.dietrecord.backend.modules.goal.model.vo.GoalGetVO;
import com.dietrecord.backend.modules.goal.service.GoalService;
import com.dietrecord.backend.modules.user.mapper.UserProfileMapper;
import com.dietrecord.backend.modules.user.model.po.UserProfilePO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 目标设置服务实现。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final UserProfileMapper userProfileMapper;
    private final AppProperties appProperties;

    @Override
    public GoalGetVO getGoal() {
        log.info("开始读取目标设置");

        UserProfilePO userProfile = loadUserProfile();

        log.info("目标设置读取完成，用户ID={}", userProfile.getId());
        return new GoalGetVO(
                userProfile.getWeightKg(),
                userProfile.getTargetWeight(),
                userProfile.getDailyCalLimit()
        );
    }

    @Override
    public void saveGoal(GoalSaveDTO request) {
        log.info("开始保存目标设置，当前体重={}，目标体重={}，热量目标={}",
                request.getCurrentWeight(), request.getTargetWeight(), request.getDailyCalLimit());

        // 先加载固定用户档案，再在当前档案上覆盖可编辑字段。
        UserProfilePO userProfile = loadUserProfile();
        userProfile.setWeightKg(request.getCurrentWeight());
        userProfile.setTargetWeight(request.getTargetWeight());
        userProfile.setDailyCalLimit(request.getDailyCalLimit());

        userProfileMapper.updateById(userProfile);
        log.info("目标设置保存完成，用户ID={}", userProfile.getId());
    }

    private UserProfilePO loadUserProfile() {
        Long userId = resolveUserId();
        UserProfilePO userProfile = userProfileMapper.selectById(userId);
        if (userProfile == null) {
            log.error("用户档案不存在，用户ID={}", userId);
            throw new BizException(ApiCode.INTERNAL_ERROR.getCode(), "user profile not found");
        }
        return userProfile;
    }

    private Long resolveUserId() {
        Long userId = appProperties.getFixedUserId();
        if (userId == null || userId <= 0) {
            log.error("固定用户ID配置非法，fixedUserId={}", userId);
            throw new BizException(ApiCode.INTERNAL_ERROR.getCode(), "fixed user id is invalid");
        }
        return userId;
    }
}
