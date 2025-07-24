package com.back.global.rsData;

public enum ResultCode {
    SIGNUP_SUCCESS("200-1", 200, "회원가입 성공"),
    INVALID_REQUEST("400-1", 400, "요청 오류"),
    SERVER_ERROR("500-1", 500, "서버 오류"),
    // 필요한 코드들 계속 추가
    ;

    private final String code;
    private final int status;
    private final String message;

    ResultCode(String code, int status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    public String code() {
        return code;
    }

    public int status() {
        return status;
    }

    public String message() {
        return message;
    }
}