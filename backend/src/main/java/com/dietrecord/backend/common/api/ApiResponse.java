package com.dietrecord.backend.common.api;

import lombok.Data;

@Data
public class ApiResponse<T> {

    /** 响应码 */
    private final int code;

    /** 响应消息 */
    private final String msg;

    /** 响应数据 */
    private final T data;

    private ApiResponse(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ApiCode.SUCCESS.getCode(), ApiCode.SUCCESS.getDefaultMessage(), data);
    }

    public static <T> ApiResponse<T> fail(ApiCode apiCode, String msg) {
        return new ApiResponse<>(apiCode.getCode(), msg, null);
    }

    public static <T> ApiResponse<T> fail(int code, String msg) {
        return new ApiResponse<>(code, msg, null);
    }

}
