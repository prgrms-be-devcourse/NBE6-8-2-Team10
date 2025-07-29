package com.back.global.rsData;

public enum ResultCode {

    // 200: 성공
    LOGIN_SUCCESS("200-1", 200, "로그인 성공"),
    SIGNUP_SUCCESS("200-2", 200, "회원가입 성공"),
    GET_ME_SUCCESS("200-3", 200, "사용자 정보 조회 성공"),
    LOGOUT_SUCCESS("200-4", 200, "로그아웃 성공"),
    REISSUE_SUCCESS("200-5", 200, "AccessToken 재발급 성공"),
    MEMBER_DELETE_SUCCESS("200-6", 200, "회원 탈퇴 성공"),

    // 400: 클라이언트 오류
    INVALID_REQUEST("400-1", 400, "요청 오류"),
    MISSING_FIELDS("400-2", 400, "필수 필드 누락"),

    // 401: 인증 오류
    UNAUTHORIZED("401-1", 401, "인증 정보 없음"),
    TOKEN_EXPIRED("401-2", 401, "토큰 만료"),
    INVALID_CREDENTIALS("401-3", 401, "이메일 또는 비밀번호 불일치"),
    MEMBER_NOT_FOUND("401-4", 401, "존재하지 않는 회원"),

    // 403: 인가 오류
    FORBIDDEN("403-1", 403, "접근 권한 오류"),

    // 500: 서버 오류
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