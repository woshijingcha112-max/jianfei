package com.dietrecord.backend.modules.diet.service;

import com.dietrecord.backend.modules.diet.model.dto.TodayDietStatDTO;
import com.dietrecord.backend.modules.diet.model.vo.TodayDietStatVO;

public interface DietStatService {

    TodayDietStatVO today(TodayDietStatDTO request);
}
