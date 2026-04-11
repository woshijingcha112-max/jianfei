package com.dietrecord.backend.common.api;

public enum ApiCode {
    SUCCESS(0, "success"),
    VALIDATE_ERROR(400, "request invalid"),
    NOT_IMPLEMENTED(501, "not implemented"),
    INTERNAL_ERROR(500, "internal error");

    private final int code;
    private final String defaultMessage;

    ApiCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public int getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
