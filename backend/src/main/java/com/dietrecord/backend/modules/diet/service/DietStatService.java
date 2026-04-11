package com.dietrecord.backend.modules.diet.service;

import com.dietrecord.backend.modules.diet.model.dto.TodayDietStatDTO;
import com.dietrecord.backend.modules.diet.model.vo.TodayDietStatVO;

/**
 * 当日饮食统计服务接口。
 */
public interface DietStatService {

    /**
     * 查询指定日期的当日饮食统计结果。
     *
     * @param request 当日饮食统计查询请求
     * @return 当日饮食统计结果
     */
    TodayDietStatVO today(TodayDietStatDTO request);
}
