package com.back.domain.member.service;


import com.back.domain.auth.dto.request.MemberSignupRequest;
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

        // 1. 회원 정보가 존재하는지 확인
        if (member == null) {
            throw new NoSuchElementException("회원 정보가 존재하지 않습니다.");
        }

        // 2. 회원 탈퇴 처리
        member.delete();
    }
}