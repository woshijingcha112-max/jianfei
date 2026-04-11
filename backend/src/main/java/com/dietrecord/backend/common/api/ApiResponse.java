package com.dietrecord.backend.common.api;

public class ApiResponse<T> {

    private final int code;
    private final String msg;
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

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }
}
