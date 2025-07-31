package com.back.domain.member.controller;

import com.back.domain.member.dto.request.MemberUpdateRequest;
import com.back.domain.member.dto.response.MemberMyPageResponse;
import com.back.domain.member.dto.response.OtherMemberInfoResponse;
import com.back.domain.member.service.MemberService;
import com.back.global.rsData.ResultCode;
import com.back.global.rsData.RsData;
import com.back.global.security.auth.MemberDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    // 사용자 프로필 조회 API
    @GetMapping("/{id}")
    @Operation(summary = "사용자 프로필 조회", description = "간단한 사용자 프로필 정보를 제공합니다.")
    public ResponseEntity<RsData<OtherMemberInfoResponse>> getOtherMemberProfile(@PathVariable Long id) {
        try {
            OtherMemberInfoResponse response = memberService.getMemberProfileById(id);
            return ResponseEntity.ok(
                    new RsData<>(ResultCode.GET_OTHER_SUCCESS, "프로필 조회 성공", response)
            );
        } catch (NoSuchElementException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new RsData<>(ResultCode.MEMBER_NOT_FOUND, e.getMessage()));
        }
    }


    // 프로필 이미지 업로드 및 업데이트
    @PostMapping(value = "/{memberId}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "프로필 이미지 업로드/수정", description = "사용자의 프로필 이미지를 업로드하거나 기존 이미지를 새 이미지로 변경합니다.")
    public ResponseEntity<RsData<String>> uploadProfileImage(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long memberId,
            @RequestParam("file") MultipartFile file) {
        // 본인의 프로필만 수정 가능하도록 검증 (보안 강화)
        if (!memberDetails.getMember().getId().equals(memberId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new RsData<>(ResultCode.PERMISSION_DENIED, "본인의 프로필 이미지만 수정할 수 있습니다."));
        }
        // 파일 존재
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new RsData<>(ResultCode.FILE_UPLOAD_FAIL, "업로드할 파일이 없습니다."));
        }
        // 파일 크기 제한 (5MB)
        if (file.getSize() > 5 * 1024 * 1024) { // 5MB
            return ResponseEntity.badRequest()
                    .body(new RsData<>(ResultCode.FILE_UPLOAD_FAIL, "파일 크기는 최대 5MB까지 허용됩니다."));
        }
        // 파일 타입 제한(이미지만 가능)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image")) {
            return ResponseEntity.badRequest()
                    .body(new RsData<>(ResultCode.FILE_UPLOAD_FAIL, "이미지 파일만 업로드할 수 있습니다."));
        }
        try {
            String profileUrl = memberService.uploadProfileImage(memberId, file);
            return ResponseEntity.ok(
                    new RsData<>(ResultCode.FILE_UPLOAD_SUCCESS, "프로필 이미지 업로드/수정 성공", profileUrl)
            );
        } catch (NoSuchElementException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new RsData<>(ResultCode.MEMBER_NOT_FOUND, e.getMessage()));
        } catch (RuntimeException e) { // 파일 저장 실패 등
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RsData<>(ResultCode.FILE_UPLOAD_FAIL, "프로필 이미지 업로드 실패: " + e.getMessage()));
        }
    }

    // 프로필 이미지 삭제
    @DeleteMapping("/{memberId}/profile-image")
    @Operation(summary = "프로필 이미지 삭제", description = "사용자의 프로필 이미지를 삭제합니다.")
    public ResponseEntity<RsData<Void>> deleteProfileImage(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long memberId) {
        // 본인의 프로필만 삭제 가능하도록 검증 (보안 강화)
        if (!memberDetails.getMember().getId().equals(memberId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new RsData<>(ResultCode.PERMISSION_DENIED, "본인의 프로필 이미지만 삭제할 수 있습니다."));
        }
        try {
            memberService.deleteProfileImage(memberId);
            return ResponseEntity.ok(
                    new RsData<>(ResultCode.FILE_DELETE_SUCCESS, "프로필 이미지 삭제 성공", null)
            );
        } catch (NoSuchElementException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new RsData<>(ResultCode.MEMBER_NOT_FOUND, e.getMessage()));
        } catch (RuntimeException e) { // 파일 삭제 실패 등
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RsData<>(ResultCode.FILE_DELETE_FAIL, "프로필 이미지 삭제 실패: " + e.getMessage()));
        }
    }

    // 특정 회원의 프로필 이미지 URL 조회 (별도 엔드포인트 제공)
    @GetMapping("/{memberId}/profile-image")
    @Operation(summary = "특정 회원 프로필 이미지 URL 조회", description = "특정 회원의 프로필 이미지 URL을 조회합니다.")
    public ResponseEntity<RsData<String>> getMemberProfileImageUrl(@PathVariable Long memberId) {
        try {
            String profileUrl = memberService.getProfileImageUrl(memberId);
            if (profileUrl == null || profileUrl.isEmpty()) {
                return ResponseEntity.ok(new RsData<>(ResultCode.PROFILE_IMAGE_NOT_FOUND, "프로필 이미지가 존재하지 않습니다.", null));
            }
            return ResponseEntity.ok(new RsData<>(ResultCode.GET_PROFILE_IMAGE_SUCCESS, "프로필 이미지 URL 조회 성공", profileUrl));
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
