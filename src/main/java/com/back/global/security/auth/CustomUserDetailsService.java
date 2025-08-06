package com.back.global.security.auth;

import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.Status;
import com.back.domain.member.repository.MemberRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public CustomUserDetailsService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 이메일로 회원 정보 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일의 사용자를 찾을 수 없습니다."));

        // 회원이 탈퇴 상태인 경우 예외 처리
        if (member.getStatus() == Status.DELETED) {
            throw new DisabledException("탈퇴한 회원입니다.");
        }

        // MemberDetails 객체 생성 후 반환
        return new MemberDetails(member);
    }
}