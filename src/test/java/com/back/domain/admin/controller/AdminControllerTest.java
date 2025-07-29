package com.back.domain.admin.controller;

import com.back.domain.files.files.service.FileStorageService;
import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.Role;
import com.back.domain.member.repository.MemberRepository;
import com.back.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
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

    private String adminToken;
    private Member admin;

    // 테스트용 관리자 계정 생성
    @BeforeEach
    void setUpAdminAccount() {
        admin = Member.builder()
                .email("testAdmin@admin.com")
                .password(passwordEncoder.encode("admin1234!"))
                .name("테스트관리자")
                .role(Role.ADMIN)  // 반드시 ADMIN
                .build();
        memberRepository.save(admin);

        adminToken = jwtTokenProvider.generateAccessToken(admin);
    }

    @Test
    @DisplayName("전체 회원 목록 조회 성공")
    void getAllMembers_success() throws Exception {// when & then
        mockMvc.perform(get("/api/admin/members")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,DESC")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("회원 목록 조회 성공"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.data.pageable.pageSize").value(10));
    }

    @Test
    @DisplayName("전체 회원 목록 조회 실패 - 관리자 권한 없음")
    void getAllMembers_unauthorized() throws Exception {
        // given - user2는 TestInitData에서 생성됨
        Member member = memberRepository.findByEmail("user2@user.com").orElseThrow();

        String token = jwtTokenProvider.generateAccessToken(member);

        // when & then
        mockMvc.perform(get("/api/admin/members")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("회원 상세 조회 성공")
    void getMemberDetail() throws Exception {
        // given - user2는 TestInitData에서 생성됨
         Member member = memberRepository.findByEmail("user2@user.com").orElseThrow();

        // when & then
        mockMvc.perform(get("/api/admin/members/{memberId}", member.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("user2@user.com"));
    }


    @Test
    @DisplayName("회원 상세 조회 실패 - 존재하지 않는 회원")
    void getMemberDetail_notFound() throws Exception {
        // given - 존재하지 않는 회원 ID
        Long nonExistentMemberId = 999L;

        // when & then
        mockMvc.perform(get("/api/admin/members/{memberId}", nonExistentMemberId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("해당 회원이 존재하지 않습니다."));
    }

}
