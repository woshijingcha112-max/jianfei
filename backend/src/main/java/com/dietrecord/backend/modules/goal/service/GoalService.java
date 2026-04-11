package com.dietrecord.backend.modules.goal.service;

import com.dietrecord.backend.modules.goal.model.dto.GoalSaveDTO;
import com.dietrecord.backend.modules.goal.model.vo.GoalGetVO;

public interface GoalService {

    GoalGetVO getGoal();

    void saveGoal(GoalSaveDTO request);
}
