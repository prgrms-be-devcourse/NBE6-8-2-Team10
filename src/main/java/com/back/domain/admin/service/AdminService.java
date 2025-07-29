package com.back.domain.admin.service;

import com.back.domain.admin.dto.response.AdminMemberResponse;
import com.back.domain.member.dto.response.MemberInfoResponse;
import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.Role;
import com.back.domain.member.repository.MemberRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final MemberRepository memberRepository;

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
                .orElseThrow(() -> new ServiceException("404-1", "해당 회원이 존재하지 않습니다."));

        return AdminMemberResponse.fromEntity(member);
    }

}
