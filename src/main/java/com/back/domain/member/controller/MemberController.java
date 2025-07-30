package com.back.domain.member.controller;

import com.back.domain.member.dto.request.MemberUpdateRequest;
import com.back.domain.member.dto.response.MemberMyPageResponse;
import com.back.domain.member.service.MemberService;
import com.back.global.rsData.ResultCode;
import com.back.global.rsData.RsData;
import com.back.global.security.auth.MemberDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    // 회원 정보 조회 API (마이페이지)
    @GetMapping("/me")
    @Operation(summary = "마이페이지 조회", description = "현재 로그인한 사용자의 상세 정보를 반환합니다.")
    public ResponseEntity<RsData<MemberMyPageResponse>> getMyPageInfo(@AuthenticationPrincipal MemberDetails memberDetails) {
        try {
            MemberMyPageResponse response = memberService.findMyPage(memberDetails.getMember());
            return ResponseEntity.ok(
                    new RsData<>(ResultCode.GET_ME_SUCCESS, "마이페이지 조회 성공", response)
            );
        } catch (NoSuchElementException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new RsData<>(ResultCode.MEMBER_NOT_FOUND, "사용자를 찾을 수 없습니다."));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RsData<>(ResultCode.SERVER_ERROR, "서버 오류가 발생했습니다."));
        }
    }

    // 회원 정보 수정 API
    @PatchMapping("/me")
    @Operation(summary = "회원 정보 수정", description = "현재 로그인한 사용자의 이름 또는 비밀번호를 수정합니다.")
    public ResponseEntity<RsData<String>> updateMemberInfo(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @RequestBody MemberUpdateRequest request
    ) {
        try {
            memberService.updateMemberInfo(memberDetails.getMember(), request);
            return ResponseEntity.ok(
                    new RsData<>(ResultCode.MEMBER_UPDATE_SUCCESS, "회원 정보 수정에 성공했습니다.")
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new RsData<>(ResultCode.MEMBER_UPDATE_FAIL, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RsData<>(ResultCode.SERVER_ERROR, "서버 오류가 발생했습니다."));
        }
    }

}
