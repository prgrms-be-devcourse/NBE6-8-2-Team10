package com.back.domain.auth.controller;

import com.back.domain.auth.dto.request.MemberLoginRequest;
import com.back.domain.auth.dto.request.MemberSignupRequest;
import com.back.domain.auth.dto.response.MemberLoginResponse;
import com.back.domain.member.dto.response.MemberInfoResponse;
import com.back.domain.member.service.MemberService;
import com.back.global.rsData.ResultCode;
import com.back.global.rsData.RsData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@DisplayName("AuthController 테스트")
public class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private MemberService memberService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {
        // given
        MemberSignupRequest request = new MemberSignupRequest("test@example.com", "securePass123!", "홍길동");

        // doNothing 설정: memberService.signup()이 예외를 던지지 않음
        doNothing().when(memberService).signup(request);

        // when
        ResponseEntity<RsData<String>> response = authController.signup(request);
        RsData<String> rs = response.getBody();

        // then
        assertThat(rs.resultCode()).isEqualTo(ResultCode.SIGNUP_SUCCESS.code());
        assertThat(rs.msg()).isEqualTo(ResultCode.SIGNUP_SUCCESS.message());

        // memberService.signup()이 정확히 한 번 호출되었는지 확인
        verify(memberService, times(1)).signup(request);
    }

    @Test
    @DisplayName("회원가입 실패 - 서버 오류")
    void signup_exception() {
        // given
        MemberSignupRequest request = new MemberSignupRequest("test@example.com", "pass123!", "홍길동");

        // 예외 던지기
        doThrow(new RuntimeException("회원가입 중 오류")).when(memberService).signup(request);

        // when
        ResponseEntity<RsData<String>> response = authController.signup(request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().resultCode()).isEqualTo(ResultCode.SERVER_ERROR.code());
        assertThat(response.getBody().msg()).isEqualTo("서버 오류가 발생했습니다.");
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        // given
        MemberLoginRequest request = new MemberLoginRequest("test@example.com", "password123!");
        MemberLoginResponse response = new MemberLoginResponse("jwtToken",
                "jwtRefreshToken", new MemberInfoResponse(
                1L,
                "test@example.com",
                "홍길동",
                "USER",
                "https://example.com/profile.jpg"
        ));

        when(memberService.login(request)).thenReturn(response);

        // when
        ResponseEntity<RsData<MemberLoginResponse>> result = authController.login(request);
        RsData<MemberLoginResponse> rs = result.getBody();

        // then
        assertThat(rs.resultCode()).isEqualTo(ResultCode.LOGIN_SUCCESS.code());
        assertThat(rs.msg()).isEqualTo("로그인 성공");
        assertThat(rs.data().accessToken()).isEqualTo("jwtToken");

        verify(memberService, times(1)).login(request);
    }

    @Test
    @DisplayName("JWT 로그인 성공 및 보호된 엔드포인트 접근")
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

        // 제네릭 타입 정보 주면서 파싱
        RsData<Map<String, Object>> rsData = objectMapper.readValue(responseJson,
                new TypeReference<RsData<Map<String, Object>>>() {});

        String accessToken = rsData.data().get("accessToken").toString();

        // 보호된 API 접근 (예: /auth/me)
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그인 실패 - ID 혹은 비밀번호가 잘못되었거나 등록되지 않은 계정")
    void login_invalidCredentials() {
        // given
        MemberLoginRequest request = new MemberLoginRequest("wrong@example.com", "wrongPassword");

        when(memberService.login(request)).thenThrow(new BadCredentialsException("잘못된 로그인"));

        // when
        ResponseEntity<RsData<MemberLoginResponse>> result = authController.login(request);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(result.getBody().resultCode()).isEqualTo(ResultCode.INVALID_CREDENTIALS.code());
        assertThat(result.getBody().msg()).isEqualTo("이메일 또는 비밀번호가 잘못되었습니다.");
    }

    @Test
    @DisplayName("로그인 실패 - 서버 오류")
    void login_serverError() {
        // given
        MemberLoginRequest request = new MemberLoginRequest("test@example.com", "password123!");

        when(memberService.login(request)).thenThrow(new RuntimeException("서버 내부 오류"));

        // when
        ResponseEntity<RsData<MemberLoginResponse>> result = authController.login(request);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.getBody().resultCode()).isEqualTo(ResultCode.SERVER_ERROR.code());
        assertThat(result.getBody().msg()).isEqualTo("서버 오류가 발생했습니다.");
    }

}
