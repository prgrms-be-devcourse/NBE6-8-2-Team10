package com.back.domain.auth.controller;

import com.back.domain.auth.dto.request.MemberLoginRequest;
import com.back.domain.auth.dto.request.MemberSignupRequest;
import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.Role;
import com.back.domain.member.entity.Status;
import com.back.domain.member.repository.MemberRepository;
import com.back.global.rsData.ResultCode;
import com.back.global.rsData.RsData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@DisplayName("AuthController 통합 테스트")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 테스트 데이터 삽입용 의존성 (예시)
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        // JWT 로그인 테스트용 계정 추가
        Member member = Member.builder()
                .email("user1@user.com")
                .password(passwordEncoder.encode("user1234!")) // 암호화 필수
                .name("Test User")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();

        memberRepository.save(member);
    }

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() throws Exception {
        // given
        MemberSignupRequest request = new MemberSignupRequest("test@example.com", "securePass123!", "홍길동");

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-2"))  // ResultCode에 맞게 수정
                .andExpect(jsonPath("$.msg").value("회원가입 성공"));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signup_email_duplicate() throws Exception {
        // given
        Member existing = Member.builder()
                .email("test@example.com")
                .password(passwordEncoder.encode("somepass!"))
                .name("기존유저")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();
        memberRepository.save(existing);

        MemberSignupRequest request = new MemberSignupRequest("test@example.com", "anotherPass123!", "홍길동");

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1")) // 예: 이메일 중복 오류 코드
                .andExpect(jsonPath("$.msg").value("이미 사용 중인 이메일입니다."));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() throws Exception {
        // given
        MemberLoginRequest request = new MemberLoginRequest("user1@user.com", "user1234!");

        // when
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1")) // 로그인 성공 코드
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andReturn();
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void login_invalid_credentials() throws Exception {
        // given
        MemberLoginRequest request = new MemberLoginRequest("user1@user.com", "wrongPassword!");

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-3"))
                .andExpect(jsonPath("$.msg").value("이메일 또는 비밀번호가 잘못되었습니다."));
    }

    @Test
    @DisplayName("JWT 로그인 성공 후 보호된 엔드포인트 접근")
    void login_and_access_protected_endpoint() throws Exception {
        // given
        MemberLoginRequest request = new MemberLoginRequest("user1@user.com", "user1234!");

        // 로그인 요청
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();

        RsData<Map<String, Object>> rsData = objectMapper.readValue(responseJson,
                new TypeReference<RsData<Map<String, Object>>>() {});

        String accessToken = rsData.data().get("accessToken").toString();

        // 보호된 API 접근
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("user1@user.com"));
    }

    @Test
    @DisplayName("JWT 로그인 후 로그아웃 성공")
    void login_and_logout_success() throws Exception {
        // given - 로그인 요청
        MemberLoginRequest loginRequest = new MemberLoginRequest("user1@user.com", "user1234!");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponseJson = loginResult.getResponse().getContentAsString();

        RsData<Map<String, Object>> loginRsData = objectMapper.readValue(
                loginResponseJson, new TypeReference<RsData<Map<String, Object>>>() {});

        String accessToken = loginRsData.data().get("accessToken").toString();

        // when - 로그아웃 요청
        MvcResult logoutResult = mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        String logoutResponseJson = logoutResult.getResponse().getContentAsString();
        RsData<Void> logoutRsData = objectMapper.readValue(
                logoutResponseJson, new TypeReference<RsData<Void>>() {});

        // then - 로그아웃 응답 검증
        assertThat(logoutRsData.resultCode()).isEqualTo(ResultCode.LOGOUT_SUCCESS.code());
        assertThat(logoutRsData.msg()).isEqualTo("로그아웃 성공");

        // 로그아웃 후 refreshToken 제거되었는지 확인
        Member member = memberRepository.findByEmail("user1@user.com").orElseThrow();
        assertThat(member.getRefreshToken()).isNull();
    }
}
