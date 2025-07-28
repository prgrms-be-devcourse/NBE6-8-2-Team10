package com.back.domain.member.service;


import com.back.domain.auth.dto.request.MemberLoginRequest;
import com.back.domain.auth.dto.request.MemberSignupRequest;
import com.back.domain.auth.dto.response.MemberLoginResponse;
import com.back.domain.member.dto.response.MemberInfoResponse;
import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.global.exception.ServiceException;
import com.back.global.rsData.ResultCode;
import com.back.global.security.auth.MemberDetails;
import com.back.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;


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

    // 로그인
    public MemberLoginResponse login(MemberLoginRequest request) {
        // 1. 인증 시도
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(request.email(), request.password());

        Authentication authentication = authenticationManager.authenticate(authToken);

        // 2. 인증 성공시 사용자 정보 로드 (authentication에서 직접 꺼내기)
        MemberDetails memberDetails = (MemberDetails) authentication.getPrincipal();
        Member member = memberDetails.getMember();

        // 3. JWT 생성
        String accessToken = jwtTokenProvider.generateAccessToken(member);
        String refreshToken = jwtTokenProvider.generateRefreshToken(member);

        // 4. 리프레시 토큰 저장
        member.updateRefreshToken(refreshToken);
        try {
            memberRepository.save(member);
        } catch (DataIntegrityViolationException e) {
            // 리프레시 토큰이 중복되는 경우, 기존 토큰을 업데이트
            log.warn("중복된 refreshToken 저장 시도: {}", refreshToken);
            throw new ServiceException(ResultCode.SERVER_ERROR.code(), "중복된 리프레시 토큰입니다.");
        }

        // 5. DTO 응답 반환
        return new MemberLoginResponse(accessToken, refreshToken, MemberInfoResponse.fromEntity(member));
    }

    // 로그아웃
    @Transactional
    public void logout(Member member) {
        member.removeRefreshToken();
        memberRepository.save(member);
    }
}