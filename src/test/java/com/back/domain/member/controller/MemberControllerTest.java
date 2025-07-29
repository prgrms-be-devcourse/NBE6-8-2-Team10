package com.back.domain.member.controller;

import com.back.domain.auth.dto.request.MemberLoginRequest;
import com.back.domain.files.files.service.FileStorageService;
import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.Status;
import com.back.domain.member.repository.MemberRepository;
import com.back.global.rsData.RsData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@DisplayName("MemberController 통합 테스트")
public class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private FileStorageService fileStorageService;

    @Test
    @DisplayName("회원 탈퇴 성공 - 로그인 후 DELETE /api/members/me 호출")
    void delete_account_success() throws Exception {
        // given - 테스트용 회원 저장
        String email = "testUser1@user.com";
        String password = "user1234!";
        Member member = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .name("홍길동")
                .build();
        memberRepository.save(member);

        // 로그인 요청
        MemberLoginRequest request = new MemberLoginRequest(email, password);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        RsData<Map<String, Object>> rsData = objectMapper.readValue(responseJson,
                new TypeReference<RsData<Map<String, Object>>>() {});
        String accessToken = rsData.data().get("accessToken").toString();

        // when - 탈퇴 요청
        mockMvc.perform(delete("/api/members/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-6"))
                .andExpect(jsonPath("$.msg").value("회원 탈퇴 성공했습니다."));

        // then - 실제 DB 상태 확인
        Member deletedMember = memberRepository.findByEmail(email).orElseThrow();
        assertEquals(Status.DELETED, deletedMember.getStatus());
    }

}
