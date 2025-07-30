package com.back.domain.admin.controller;

import com.back.domain.admin.dto.request.AdminUpdateMemberRequest;
import com.back.domain.files.files.service.FileStorageService;
import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.Status;
import com.back.domain.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    private MemberRepository memberRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private FileStorageService fileStorageService;


    @Test
    @DisplayName("전체 회원 목록 조회 성공")
    @WithUserDetails(value = "admin@admin.com", userDetailsServiceBeanName = "customUserDetailsService")
    void getAllMembers_success() throws Exception {
        mockMvc.perform(get("/api/admin/members")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,DESC")
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
    @WithUserDetails(value = "user2@user.com", userDetailsServiceBeanName = "customUserDetailsService")
    void getAllMembers_unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/members")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("회원 상세 조회 성공")
    @WithUserDetails(value = "admin@admin.com", userDetailsServiceBeanName = "customUserDetailsService")
    void getMemberDetail() throws Exception {
        Member member = memberRepository.findByEmail("user2@user.com").orElseThrow();

        mockMvc.perform(get("/api/admin/members/{memberId}", member.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("user2@user.com"));
    }

    @Test
    @DisplayName("회원 상세 조회 실패 - 존재하지 않는 회원")
    @WithUserDetails(value = "admin@admin.com", userDetailsServiceBeanName = "customUserDetailsService")
    void getMemberDetail_notFound() throws Exception {
        mockMvc.perform(get("/api/admin/members/{memberId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("해당 회원이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("회원 정보 수정 성공")
    @WithUserDetails(value = "admin@admin.com")
    void updateMember_success() throws Exception {
        // given
        Member member = memberRepository.findByEmail("user2@user.com").orElseThrow();

        AdminUpdateMemberRequest request = new AdminUpdateMemberRequest(
                "변경된이름",
                Status.BLOCKED,
                "https://new.image.url/profile.png",
                true // 비밀번호 초기화 요청
        );

        // when
        mockMvc.perform(patch("/api/admin/members/" + member.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-7"))
                .andExpect(jsonPath("$.msg").value("회원 정보 수정 성공"));

        // then
        Member updated = memberRepository.findById(member.getId()).orElseThrow();
        assertEquals("변경된이름", updated.getName());
        assertEquals(Status.BLOCKED, updated.getStatus());
        assertEquals("https://new.image.url/profile.png", updated.getProfileUrl());
        assertTrue(passwordEncoder.matches("test1234!", updated.getPassword()));
    }

    @Test
    @DisplayName("회원 정보 수정 실패 - 존재하지 않는 회원")
    @WithUserDetails(value = "admin@admin.com")
    void updateMember_fail_notFound() throws Exception {
        // given
        Long invalidId = 9999L;
        AdminUpdateMemberRequest request = new AdminUpdateMemberRequest(
                "아무거나",
                Status.ACTIVE,
                null,
                false
        );

        // when & then
        mockMvc.perform(patch("/api/admin/members/" + invalidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("해당 데이터가 존재하지 않습니다."));
    }

}
