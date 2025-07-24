package com.back.domain.auth.controller;

import com.back.domain.auth.dto.request.MemberSignupRequest;
import com.back.domain.member.service.MemberService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;

    // 회원가입 API
    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
    public ResponseEntity<RsData<String>> signup(@Valid @RequestBody MemberSignupRequest request) {
        try {
            memberService.signup(request);
            return ResponseEntity.ok(new RsData<>("200-1", "회원가입 성공"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new RsData<>("400-1", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RsData<>("500-1", "서버 오류가 발생했습니다."));
        }
    }
}