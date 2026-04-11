package com.dietrecord.backend.modules.diet.service;

import com.dietrecord.backend.modules.diet.model.dto.DietRecordListDTO;
import com.dietrecord.backend.modules.diet.model.dto.DietRecordSaveDTO;
import com.dietrecord.backend.modules.diet.model.vo.DietRecordCardVO;

import java.util.List;

/**
 * 饮食记录服务接口。
 */
public interface DietRecordService {

    /**
     * 保存一条饮食记录及其明细项。
     *
     * @param request 饮食记录保存请求
     */
    void save(DietRecordSaveDTO request);

    /**
     * 按日期查询饮食记录卡片列表。
     *
     * @param request 饮食记录列表查询请求
     * @return 指定日期下的饮食记录列表
     */
    List<DietRecordCardVO> list(DietRecordListDTO request);
}
