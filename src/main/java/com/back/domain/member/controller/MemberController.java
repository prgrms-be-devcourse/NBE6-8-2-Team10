package com.back.domain.member.controller;

import com.back.domain.member.service.MemberService;
import com.back.global.rsData.ResultCode;
import com.back.global.rsData.RsData;
import com.back.global.security.auth.MemberDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 회원 탈퇴(상태변경) API
    @DeleteMapping("/me")
    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자의 상태를 DELETED로 변경합니다.")
    public ResponseEntity<RsData<String>> deleteCurrentMember(@AuthenticationPrincipal MemberDetails memberDetails) {
        try {
            memberService.deleteAccount(memberDetails.getMember());
            return ResponseEntity.ok(
                    new RsData<>(ResultCode.MEMBER_DELETE_SUCCESS, "회원 탈퇴 성공했습니다.")
            );
        } catch (NoSuchElementException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new RsData<>(ResultCode.MEMBER_NOT_FOUND, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RsData<>(ResultCode.SERVER_ERROR, "서버 오류가 발생했습니다."));
        }
    }
}
