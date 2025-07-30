package com.back.domain.member.service;


import com.back.domain.auth.dto.request.MemberSignupRequest;
import com.back.domain.member.dto.request.MemberUpdateRequest;
import com.back.domain.member.dto.response.MemberMyPageResponse;
import com.back.domain.member.dto.response.OtherMemberInfoResponse;
import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;


    // 회원 가입
    @Transactional
    public void signup(MemberSignupRequest request) {

        // 1. 이메일 중복 검사
        if (memberRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        Member member = Member.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .build();

        memberRepository.save(member);
    }

    // 회원 탈퇴 (상태 변경)
    @Transactional
    public void deleteAccount(Member member) {
        // 1. 반드시 영속 상태로 다시 가져오기
        Member foundMember = memberRepository.findById(member.getId())
                .orElseThrow(() -> new NoSuchElementException("회원 정보가 존재하지 않습니다."));

        // 2. 회원 탈퇴 처리
        foundMember.delete();
    }

    // 회원 마이페이지 조회
    public MemberMyPageResponse findMyPage(Member member) {
        // 1. 반드시 영속 상태로 다시 가져오기
        Member foundMember = memberRepository.findById(member.getId())
                .orElseThrow(() -> new NoSuchElementException("회원 정보가 존재하지 않습니다."));

        // 2. 마이페이지 정보 반환
        return MemberMyPageResponse.fromEntity(foundMember);
    }

    // 회원 정보 수정
    @Transactional
    public void updateMemberInfo(Member member, MemberUpdateRequest request) {
        // 1. 반드시 영속 상태로 다시 가져오기
        Member foundMember = memberRepository.findById(member.getId())
                .orElseThrow(() -> new NoSuchElementException("회원 정보가 존재하지 않습니다."));

        // 2. 이름 변경
        if (request.name() != null && !request.name().isBlank()) {
            foundMember.updateName(request.name());
        }

        // 3. 프로필 URL 변경
        if (request.profileUrl() != null && !request.profileUrl().isBlank()) {
            foundMember.updateProfileUrl(request.profileUrl());
        }

        // 4. 비밀번호 변경 요청이 있을 경우만 현재 비밀번호 확인
        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            if (request.currentPassword() == null || request.currentPassword().isBlank()) {
                throw new IllegalArgumentException("현재 비밀번호를 입력해주세요.");
            }

            if (!passwordEncoder.matches(request.currentPassword(), foundMember.getPassword())) {
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
            }

            foundMember.updatePassword(passwordEncoder.encode(request.newPassword()));
        }
    }

    // 사용자 프로필 조회
    public OtherMemberInfoResponse getMemberProfileById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 사용자가 존재하지 않습니다."));
        return OtherMemberInfoResponse.fromEntity(member);
    }

}