package com.dietrecord.backend.modules.diet.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record DietRecordSaveDTO(
        /** 记录日期 */
        @NotNull(message = "recordDate is required")
        LocalDate recordDate,
        /** 餐次类型 */
        @NotNull(message = "mealType is required")
        @Min(value = 1, message = "mealType must be between 1 and 4")
        @Max(value = 4, message = "mealType must be between 1 and 4")
        Integer mealType,
        /** 图片访问地址 */
        @NotBlank(message = "photoUrl is required")
        String photoUrl,
        /** 备注 */
        @Size(max = 200, message = "remark must be at most 200 characters")
        String remark,
        /** 饮食明细列表 */
        @NotEmpty(message = "items is required")
        @Valid
        List<DietRecordItemDTO> items
) {
}
