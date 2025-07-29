package com.back.domain.admin.controller;

import com.back.domain.files.files.service.FileStorageService;
import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.Role;
import com.back.domain.member.repository.MemberRepository;
import com.back.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@DisplayName("AdminController 통합 테스트")
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private FileStorageService fileStorageService;

    @Test
    @DisplayName("전체 회원 목록 조회 성공")
    void getAllMembers_success() throws Exception {
        // given - 관리자 계정 생성
        String email = "testAdmin@admin.com";
        String password = "admin1234!";
        Member admin = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .name("관리자")
                .role(Role.ADMIN)
                .build();
        memberRepository.save(admin);

        // 토큰 생성
        String token = jwtTokenProvider.generateAccessToken(admin);

        // when & then
        mockMvc.perform(get("/api/admin/members")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,DESC")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("회원 목록 조회 성공"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.data.pageable.pageSize").value(10));
    }

    @Test
    @DisplayName("권한 없는 사용자의 회원 목록 조회 실패")
    void getAllMembers_unauthorized() throws Exception {
        // given - 일반 사용자 계정 생성
        String email = "testUser@user.com";
        String password = "user1234!";
        Member user = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .name("일반사용자")
                .role(Role.USER)
                .build();
        memberRepository.save(user);

        String token = jwtTokenProvider.generateAccessToken(user);

        // when & then
        mockMvc.perform(get("/api/admin/members")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

}
