package com.back.global.init;

import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.Role;
import com.back.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Profile("test")
@Component
@RequiredArgsConstructor
@Slf4j
public class TestDataInitializer implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("===== TEST 데이터 초기화 시작=====");
        initUser();
        log.info("===== TEST 데이터 초기화 완료=====");
    }

    // 일반 사용자 계정 생성
    public void initUser() {
        createUserIfNotExists("testuser1@user.com", "사용자1");
        createUserIfNotExists("testuser2@user.com", "사용자2");
    }

    private void createUserIfNotExists(String email, String name) {
        if (!memberRepository.existsByEmail(email)) {
            Member user = Member.builder()
                    .email(email)
                    .password(passwordEncoder.encode("user1234!"))
                    .name(name)
                    .role(Role.USER)
                    .build();
            memberRepository.save(user);
            log.info("{} 계정이 생성되었습니다.", name);
        }
    }
}
