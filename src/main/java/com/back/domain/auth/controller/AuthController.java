package com.back.domain.auth.controller;

import com.back.domain.auth.dto.request.MemberLoginRequest;
import com.back.domain.auth.dto.request.MemberSignupRequest;
import com.back.domain.auth.dto.request.TokenReissueRequest;
import com.back.domain.auth.dto.response.MemberLoginResponse;
import com.back.domain.auth.dto.response.TokenReissueResponse;
import com.back.domain.auth.service.AuthService;
import com.back.domain.member.dto.response.MemberInfoResponse;
import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.global.rsData.ResultCode;
import com.back.global.rsData.RsData;
import com.back.global.security.auth.MemberDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;
    private final AuthService authService;

    // 회원가입 API
    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
    public ResponseEntity<RsData<String>> signup(@Valid @RequestBody MemberSignupRequest request) {
        try {
            memberService.signup(request);
            return ResponseEntity.ok(new RsData<>(ResultCode.SIGNUP_SUCCESS, "회원가입 성공"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new RsData<>(ResultCode.INVALID_REQUEST, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RsData<>(ResultCode.SERVER_ERROR, "서버 오류가 발생했습니다."));
        }
    }

    // 로그인 API
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    public ResponseEntity<RsData<MemberLoginResponse>> login(@Valid @RequestBody MemberLoginRequest request) {
        try {
            MemberLoginResponse response = authService.login(request);
            return ResponseEntity.ok(new RsData<>(ResultCode.LOGIN_SUCCESS, "로그인 성공", response));
        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new RsData<>(ResultCode.INVALID_CREDENTIALS, "이메일 또는 비밀번호가 잘못되었습니다."));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RsData<>(ResultCode.SERVER_ERROR, "서버 오류가 발생했습니다."));
        }
    }

    // 로그인 사용자 정보 조회 API
    @GetMapping("/me")
    @Operation(summary = "로그인 사용자 정보 조회", description = "유효한 JWT 토큰을 통해 현재 인증된 사용자 정보를 조회합니다.")
    public ResponseEntity<RsData<MemberInfoResponse>> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof MemberDetails memberDetails)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new RsData<>(ResultCode.UNAUTHORIZED, "로그인된 사용자가 없습니다."));
        }

        Member member = memberDetails.getMember();
        MemberInfoResponse response = MemberInfoResponse.fromEntity(member);

        return ResponseEntity.ok(new RsData<>(ResultCode.GET_ME_SUCCESS, "로그인 사용자 정보 조회 성공", response));
    }

    // 로그아웃 API
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "현재 로그인된 사용자의 refresh 토큰을 삭제합니다.")
    public ResponseEntity<RsData<Void>> logout(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof MemberDetails memberDetails)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new RsData<>(ResultCode.UNAUTHORIZED, "로그인된 사용자가 없습니다."));
        }

        Member member = memberDetails.getMember();
        authService.logout(member);

        return ResponseEntity.ok(new RsData<>(ResultCode.LOGOUT_SUCCESS, "로그아웃 성공", null));
    }

    // Access Token 재발급 API
    @PostMapping("/reissue")
    @Operation(summary = "AccessToken 재발급", description = "RefreshToken으로 AccessToken을 재발급 받습니다.")
    public ResponseEntity<RsData<TokenReissueResponse>> reissue(@Valid @RequestBody TokenReissueRequest request) {
        try {
            TokenReissueResponse response = authService.reissueAccessToken(request);
            return ResponseEntity.ok(new RsData<>(ResultCode.REISSUE_SUCCESS, "토큰 재발급 성공", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new RsData<>(ResultCode.INVALID_REQUEST, e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new RsData<>(ResultCode.TOKEN_EXPIRED, "유효하지 않은 RefreshToken입니다."));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new RsData<>(ResultCode.UNAUTHORIZED, "토큰 재발급에 실패했습니다."));
        }
    }
}