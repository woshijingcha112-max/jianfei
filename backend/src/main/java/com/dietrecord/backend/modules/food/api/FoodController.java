package com.dietrecord.backend.modules.food.api;

import com.dietrecord.backend.common.api.ApiCode;
import com.dietrecord.backend.common.api.ApiResponse;
import com.dietrecord.backend.modules.food.model.dto.FoodDetailDTO;
import com.dietrecord.backend.modules.food.model.dto.FoodSearchDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/food")
public class FoodController {

    @PostMapping("/search")
    public ApiResponse<Void> search(@Valid @RequestBody FoodSearchDTO request) {
        return ApiResponse.fail(ApiCode.NOT_IMPLEMENTED,
                "Food search scaffold created for keyword " + request.keyword());
    }

    @PostMapping("/detail")
    public ApiResponse<Void> detail(@Valid @RequestBody FoodDetailDTO request) {
        return ApiResponse.fail(ApiCode.NOT_IMPLEMENTED,
                "Food detail scaffold created for id " + request.id());
    }
}
