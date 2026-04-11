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
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final UserProfileMapper userProfileMapper;
    private final AppProperties appProperties;

    @Override
    public GoalGetVO getGoal() {
        UserProfilePO userProfile = loadUserProfile();
        return new GoalGetVO(
                userProfile.getWeightKg(),
                userProfile.getTargetWeight(),
                userProfile.getDailyCalLimit()
        );
    }

    @Override
    public void saveGoal(GoalSaveDTO request) {
        UserProfilePO userProfile = loadUserProfile();
        userProfile.setTargetWeight(request.getTargetWeight());
        userProfile.setDailyCalLimit(request.getDailyCalLimit());

        userProfileMapper.updateById(userProfile);
    }

    private UserProfilePO loadUserProfile() {
        Long userId = resolveUserId();
        UserProfilePO userProfile = userProfileMapper.selectById(userId);
        if (userProfile == null) {
            throw new BizException(ApiCode.INTERNAL_ERROR.getCode(), "user profile not found");
        }
        return userProfile;
    }

    private Long resolveUserId() {
        Long userId = appProperties.getFixedUserId();
        if (userId == null || userId <= 0) {
            throw new BizException(ApiCode.INTERNAL_ERROR.getCode(), "fixed user id is invalid");
        }
        return userId;
    }
}
