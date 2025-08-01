package com.back.domain.admin.service;

import com.back.domain.admin.dto.request.AdminUpdateMemberRequest;
import com.back.domain.admin.dto.response.AdminMemberResponse;
import com.back.domain.member.dto.response.MemberInfoResponse;
import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.Role;
import com.back.domain.member.repository.MemberRepository;
import com.back.global.exception.ServiceException;
import com.back.global.rsData.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 전체 회원 목록 조회(관리자 제외)
    public Page<MemberInfoResponse> getAllMembers(Pageable pageable) {
        log.info("전체 회원 목록 조회 요청 (관리자 제외)");

        // 페이징 적용 및 DTO 변환하여 반환
        return memberRepository.findAllByRoleNot(Role.ADMIN, pageable)
                .map(MemberInfoResponse::fromEntity);
    }

    // 회원 상세 조회
    public AdminMemberResponse getMemberDetail(Long memberId) {
        log.info("회원 상세 조회 요청 - memberId: {}", memberId);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(ResultCode.MEMBER_NOT_FOUND.code(), "존재하지 않는 회원입니다."));

        return AdminMemberResponse.fromEntity(member);
    }

    // 회원 정보 수정
    public void updateMemberInfo(Long memberId, AdminUpdateMemberRequest request){
        log.info("회원 정보 수정 요청 - memberId: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(ResultCode.MEMBER_NOT_FOUND.code(), "해당 회원이 존재하지 않습니다."));

        // 1. 이름 변경
        if (request.name() != null && !request.name().isBlank()) {
            member.updateName(request.name());
        }

        // 2. 상태 변경
        if (request.status() != null) {
            member.changeStatus(request.status());
        }

        // 3. 프로필 이미지 변경
        if (request.profileUrl() != null && !request.profileUrl().isBlank()) {
            member.updateProfileUrl(request.profileUrl());
        }

        // 4. 비밀번호 초기화
        if (request.resetPassword()) {
            member.updatePassword(passwordEncoder.encode("test1234!"));
        }

        memberRepository.save(member);
    }
}
