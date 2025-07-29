package com.back.domain.admin.controller;

import com.back.domain.admin.dto.response.AdminMemberResponse;
import com.back.domain.admin.service.AdminService;
import com.back.domain.member.dto.response.MemberInfoResponse;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // 전체 회원 목록 조회 API(탈퇴 포함)
    @Operation(summary = "전체 회원 목록 조회", description = "모든 회원 목록을 페이징하여 조회합니다 (탈퇴 포함)")
    @GetMapping("/members")
    public ResponseEntity<RsData<Page<MemberInfoResponse>>> getAllMembers(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<MemberInfoResponse> members = adminService.getAllMembers(pageable);

        RsData<Page<MemberInfoResponse>> response =
                new RsData<>("200-1", "회원 목록 조회 성공", members);

        return ResponseEntity.ok(response);
    }

    // 회원 상세 조회 API
    @Operation(summary = "회원 상세 조회", description = "회원 ID를 통해 회원 정보를 상세 조회합니다")
    @GetMapping("/members/{memberId}")
    public ResponseEntity<RsData<AdminMemberResponse>> getMemberDetail(@PathVariable Long memberId) {
        AdminMemberResponse response = adminService.getMemberDetail(memberId);
        return ResponseEntity.ok(new RsData<>("200-2", "회원 상세 조회 성공", response));
    }
}
