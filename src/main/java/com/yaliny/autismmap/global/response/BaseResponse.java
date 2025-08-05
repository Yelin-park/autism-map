package com.yaliny.autismmap.global.response;

public record BaseResponse<T>(int code, String message, T data) {
    public static <T> BaseResponse<T> success() {
        return new BaseResponse<>(200, "OK", null);
    }

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(200, "OK", data);
    }

    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(200, message, data);
    }

    public static <T> BaseResponse<T> error(int code, String message) {
        return new BaseResponse<>(code, message, null);
    }

    public static <T> BaseResponse<T> error(int code, String message, T data) {
        return new BaseResponse<>(code, message, data);
    }
}
