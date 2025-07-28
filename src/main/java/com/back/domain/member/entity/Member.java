package com.back.domain.member.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String profileUrl;

    @Column(unique = true)
    private String refreshToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private LocalDateTime deletedAt;

    // 생성자 (빌더)
    @Builder
    public Member(String email, String password, String name, String profileUrl, Role role, Status status) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.profileUrl = profileUrl;
        this.role = (role != null) ? role : Role.USER;  // 기본 역할은 USER
        this.status = (status != null) ? status : Status.ACTIVE;   // 기본 상태는 ACTIVE
    }

    // 리프레시 토큰을 삭제(무효화)함
    public void removeRefreshToken() {
        this.refreshToken = null;
    }

    // 리프레시 토큰을 설정함
    public void updateRefreshToken(String refreshToken) {
        // 유효성 검사: 리프레시 토큰이 null 또는 빈 문자열이 아닌지 확인
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new IllegalArgumentException("리프레시 토큰은 null 또는 빈 문자열일 수 없습니다.");
        }
        this.refreshToken = refreshToken;
    }

}
