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

/**
 * 食物搜索与详情接口。
 */
@RestController
@RequestMapping("/food")
public class FoodController {

    /**
     * 搜索食物。
     *
     * @param request 食物搜索请求
     * @return 当前轮次固定返回未实现
     */
    @PostMapping("/search")
    public ApiResponse<Void> search(@Valid @RequestBody FoodSearchDTO request) {
        return ApiResponse.fail(ApiCode.NOT_IMPLEMENTED,
                "Food search scaffold created for keyword " + request.keyword());
    }

    /**
     * 查询食物详情。
     *
     * @param request 食物详情查询请求
     * @return 当前轮次固定返回未实现
     */
    @PostMapping("/detail")
    public ApiResponse<Void> detail(@Valid @RequestBody FoodDetailDTO request) {
        return ApiResponse.fail(ApiCode.NOT_IMPLEMENTED,
                "Food detail scaffold created for id " + request.id());
    }
}
