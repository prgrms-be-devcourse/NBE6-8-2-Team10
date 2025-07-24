package com.back.domain.auth;

import com.back.domain.auth.controller.AuthController;
import com.back.domain.auth.dto.request.MemberSignupRequest;
import com.back.domain.member.service.MemberService;
import com.back.global.rsData.ResultCode;
import com.back.global.rsData.RsData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController 테스트")
public class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private MemberService memberService;

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


}
