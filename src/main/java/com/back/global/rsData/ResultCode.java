package com.back.global.rsData;

public enum ResultCode {

    // ----------------------- [200: 성공] -----------------------
    LOGIN_SUCCESS("200-1", 200, "로그인 성공"),
    SIGNUP_SUCCESS("200-2", 200, "회원가입 성공"),
    GET_ME_SUCCESS("200-3", 200, "사용자 정보 조회 성공"),
    LOGOUT_SUCCESS("200-4", 200, "로그아웃 성공"),
    REISSUE_SUCCESS("200-5", 200, "AccessToken 재발급 성공"),
    MEMBER_DELETE_SUCCESS("200-6", 200, "회원 탈퇴 성공"),
    MEMBER_UPDATE_SUCCESS("200-7", 200, "회원 정보 수정 성공"),
    GET_OTHER_SUCCESS("200-8", 200, "사용자 프로필 정보 조회 성공"),
    FILE_UPLOAD_SUCCESS("200-9", 200, "파일 업로드/수정 성공"),
    FILE_DELETE_SUCCESS("200-10", 200, "파일 삭제 성공"),
    GET_PROFILE_IMAGE_SUCCESS("200-11", 200, "프로필 이미지 URL 조회 성공"),
    PROFILE_IMAGE_NOT_FOUND("200-12", 200, "프로필 이미지가 존재하지 않습니다."),

    // ----------------------- [400: 잘못된 요청] -----------------------
    INVALID_REQUEST("400-1", 400, "잘못된 요청입니다."),
    MISSING_FIELDS("400-2", 400, "필수 필드가 누락되었습니다."),
    INVALID_EMAIL_FORMAT("400-3", 400, "이메일 형식이 잘못되었습니다."),
    MEMBER_UPDATE_FAIL("400-4", 400, "회원 정보 수정에 실패했습니다."),
    FILE_UPLOAD_FAIL("400-5", 400, "파일 업로드/수정 실패"),
    FILE_DELETE_FAIL("400-6", 400, "파일 삭제 실패"),
    // ----------------------- [401: 인증 오류] -----------------------
    UNAUTHORIZED("401-1", 401, "인증 정보가 없습니다."),
    TOKEN_EXPIRED("401-2", 401, "토큰이 만료되었습니다."),
    INVALID_CREDENTIALS("401-3", 401, "이메일 또는 비밀번호가 잘못되었습니다."),

    // ----------------------- [403: 권한 오류] -----------------------
    FORBIDDEN("403-1", 403, "접근 권한이 없습니다."),
    PERMISSION_DENIED("403-2", 403, "권한이 없습니다."),

    // ----------------------- [404: 리소스 없음] -----------------------
    RESOURCE_NOT_FOUND("404-1", 404, "요청한 리소스를 찾을 수 없습니다."),
    MEMBER_NOT_FOUND("404-2", 404, "존재하지 않는 회원입니다."),
    POST_NOT_FOUND("404-3", 404, "해당 게시글을 찾을 수 없습니다."),

    // ----------------------- [500: 서버 오류] -----------------------
    SERVER_ERROR("500-1", 500, "서버 내부 오류가 발생했습니다."),
    DATABASE_ERROR("500-2", 500, "데이터베이스 오류가 발생했습니다.");
    // 필요한 코드들 계속 추가

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