package com.dietrecord.backend.modules.diet.service;

import com.dietrecord.backend.modules.diet.model.dto.DietRecordListDTO;
import com.dietrecord.backend.modules.diet.model.dto.DietRecordSaveDTO;
import com.dietrecord.backend.modules.diet.model.vo.DietRecordCardVO;

import java.util.List;

public interface DietRecordService {

    void save(DietRecordSaveDTO request);

    List<DietRecordCardVO> list(DietRecordListDTO request);
}
