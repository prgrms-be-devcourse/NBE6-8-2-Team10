package com.back.global.init;

import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.post.entity.Post;
import com.back.domain.post.repository.PostRepository;
import com.back.domain.member.entity.Role;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Profile("dev")
@Component
@RequiredArgsConstructor
@Slf4j
public class PostDataInitializer {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void init() {
        log.info("===== 게시글 테스트 데이터 생성 시작 =====");

        // 1. 유저 생성
        Member user = memberRepository.findByEmail("test1@user.com")
                .orElseGet(() -> memberRepository.save(Member.builder()
                        .email("test1@user.com")
                        .password(passwordEncoder.encode("1234"))
                        .name("사용자1")
                        .role(Role.USER)
                        .build()));

        // 2. 게시글이 이미 있으면 중복 생성 방지
        if (postRepository.count() > 0) {
            log.info("게시글 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        // 2. 게시글 샘플 데이터 생성
        Post post1 = Post.builder()
                .member(user)
                .title("특허1 판매합니다")
                .description("특허1은 이러한 기능입니다")
                .category(Post.Category.METHOD)
                .price(99999)
                .status(Post.Status.SALE)
                .build();

        Post post2 = Post.builder()
                .member(user)
                .title("특허2 팝니다")
                .description("특허2 기능 설명")
                .category(Post.Category.TRADEMARK)
                .price(10000)
                .status(Post.Status.SALE)
                .build();

        postRepository.saveAll(List.of(post1, post2));

        log.info("===== 게시글 테스트 데이터 생성 완료 =====");
    }
}
