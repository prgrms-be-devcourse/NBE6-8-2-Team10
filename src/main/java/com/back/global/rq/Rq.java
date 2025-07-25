package com.back.global.rq;

import com.back.domain.member.entity.Member;
import com.back.global.security.auth.MemberDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Scope("request")  // 요청마다 Rq 인스턴스를 새로 생성해 상태를 분리
@RequiredArgsConstructor
public class Rq {

    private final HttpServletRequest request;

    public Member getMember() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof MemberDetails)) return null;

        return ((MemberDetails) auth.getPrincipal()).getMember();
    }

    public Long getMemberId() {
        Member member = getMember();
        return (member != null) ? member.getId() : null;
    }

    public boolean isLogin() {
        return getMember() != null;
    }

    public String getMemberRole() {
        Member member = getMember();
        return (member != null) ? member.getRole().name() : null;
    }
}