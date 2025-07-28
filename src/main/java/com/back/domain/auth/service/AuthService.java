package com.back.domain.auth.service;

import com.back.domain.auth.dto.request.MemberLoginRequest;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final MemberRepository memberRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    // 로그인
    @Transactional
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
            // 리프레시 토큰 저장 실패 시 재시도 또는 예외 처리
            log.error("리프레시 토큰 저장 실패", e);
            throw new ServiceException(ResultCode.SERVER_ERROR.code(), "토큰 저장에 실패했습니다.");
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