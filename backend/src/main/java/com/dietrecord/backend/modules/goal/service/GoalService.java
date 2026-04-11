package com.dietrecord.backend.modules.goal.service;

import com.dietrecord.backend.modules.goal.model.dto.GoalSaveDTO;
import com.dietrecord.backend.modules.goal.model.vo.GoalGetVO;

/**
 * 目标设置服务接口。
 */
public interface GoalService {

    /**
     * 读取当前用户的目标设置。
     *
     * @return 当前目标设置
     */
    GoalGetVO getGoal();

    /**
     * 保存当前用户的目标设置。
     *
     * @param request 目标保存请求
     */
    void saveGoal(GoalSaveDTO request);
}
