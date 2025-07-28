package com.back.domain.chat.chat.controller;

import com.back.domain.chat.chat.dto.MessageDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatController 테스트")
class ChatWebSocketControllerTest {

    @InjectMocks
    private ChatWebSocketController chatWebSocketController;

    private SimpMessagingTemplate mockMessagingTemplate;

    @BeforeEach
    void setUp() {
        mockMessagingTemplate = mock(SimpMessagingTemplate.class);
        // 만약 ChatController에 SimpMessagingTemplate이 필요하다면
        // ReflectionTestUtils.setField(chatController, "messagingTemplate", mockMessagingTemplate);
    }

    @Test
    @DisplayName("t1 - 정상적인 메시지 전송 시 동일한 메시지 객체를 반환한다")
    void t1() {
        // given
        MessageDto inputMessage = new MessageDto("홍길동", "안녕하세요!");

        // when
        MessageDto result = chatWebSocketController.sendMessage(inputMessage);

        // then
        assertThat(result)
                .as("반환값이 null이 아니어야 한다")
                .isNotNull()
                .as("입력 객체와 반환 객체가 동일해야 한다")
                .isSameAs(inputMessage);

        assertThat(result.getSender())
                .as("발신자가 입력값과 일치해야 한다")
                .isEqualTo("홍길동");

        assertThat(result.getContent())
                .as("메시지 내용이 입력값과 일치해야 한다")
                .isEqualTo("안녕하세요!");
    }

    @Test
    @DisplayName("t2 - 빈 문자열 메시지 전송 시 빈 문자열이 그대로 반환된다")
    void t2() {
        // given
        MessageDto emptyMessage = new MessageDto("", "");

        // when
        MessageDto result = chatWebSocketController.sendMessage(emptyMessage);

        // then
        assertThat(result)
                .as("반환값이 null이 아니어야 한다")
                .isNotNull();

        assertThat(result.getSender())
                .as("빈 발신자명이 그대로 반환되어야 한다")
                .isEmpty();

        assertThat(result.getContent())
                .as("빈 메시지 내용이 그대로 반환되어야 한다")
                .isEmpty();
    }

    @Test
    @DisplayName("t3 - null 메시지 전송 시 null이 반환된다")
    void t3() {
        // given
        MessageDto nullMessage = null;

        // when
        MessageDto result = chatWebSocketController.sendMessage(nullMessage);

        // then
        assertThat(result)
                .as("null 입력 시 null이 반환되어야 한다")
                .isNull();
    }

    @Test
    @DisplayName("t4 - 긴 메시지 전송 시 정상적으로 처리된다")
    void t4() {
        // given
        String longContent = "a".repeat(1000); // 1000자 메시지
        MessageDto longMessage = new MessageDto("사용자1", longContent);

        // when
        MessageDto result = chatWebSocketController.sendMessage(longMessage);

        // then
        assertThat(result)
                .as("긴 메시지도 정상적으로 처리되어야 한다")
                .isNotNull();

        assertThat(result.getSender())
                .as("발신자명이 정확해야 한다")
                .isEqualTo("사용자1");

        assertThat(result.getContent())
                .as("메시지 내용이 정확해야 한다")
                .isEqualTo(longContent)
                .as("메시지 길이가 보존되어야 한다")
                .hasSize(1000);
    }

    @Test
    @DisplayName("t5 - 특수문자가 포함된 메시지가 정상적으로 처리된다")
    void t5() {
        // given
        String specialContent = "!@#$%^&*()_+{}|:<>?[];',./~`";
        MessageDto specialMessage = new MessageDto("테스트유저", specialContent);

        // when
        MessageDto result = chatWebSocketController.sendMessage(specialMessage);

        // then
        assertThat(result)
                .as("특수문자 메시지가 정상적으로 처리되어야 한다")
                .isNotNull();

        assertThat(result.getSender())
                .as("발신자명이 정확해야 한다")
                .isEqualTo("테스트유저");

        assertThat(result.getContent())
                .as("특수문자가 그대로 보존되어야 한다")
                .isEqualTo(specialContent)
                .as("특수문자들이 모두 포함되어야 한다")
                .contains("!@#$", "%^&*", "()_+", "{}|", ":<>?", "[];", "',./" ,"~`");
    }

    @Test
    @DisplayName("t6 - 한글, 영어, 숫자가 혼합된 메시지가 정상적으로 처리된다")
    void t6() {
        // given
        MessageDto mixedMessage = new MessageDto("User123", "안녕하세요 Hello 12345");

        // when
        MessageDto result = chatWebSocketController.sendMessage(mixedMessage);

        // then
        assertThat(result)
                .as("다국어 메시지가 정상적으로 처리되어야 한다")
                .isNotNull();

        assertThat(result.getSender())
                .as("영문과 숫자가 포함된 발신자명이 정확해야 한다")
                .isEqualTo("User123")
                .contains("User", "123");

        assertThat(result.getContent())
                .as("한글, 영어, 숫자가 모두 포함되어야 한다")
                .isEqualTo("안녕하세요 Hello 12345")
                .contains("안녕하세요", "Hello", "12345");
    }

    @Test
    @DisplayName("t7 - 공백만 있는 메시지도 정상적으로 처리된다")
    void t7() {
        // given
        MessageDto spaceMessage = new MessageDto("   ", "   ");

        // when
        MessageDto result = chatWebSocketController.sendMessage(spaceMessage);

        // then
        assertThat(result)
                .as("공백 메시지도 정상적으로 처리되어야 한다")
                .isNotNull();

        assertThat(result.getSender())
                .as("공백이 그대로 보존되어야 한다")
                .isEqualTo("   ")
                .hasSize(3)
                .isBlank();

        assertThat(result.getContent())
                .as("공백이 그대로 보존되어야 한다")
                .isEqualTo("   ")
                .hasSize(3)
                .isBlank();
    }

    @Test
    @DisplayName("t8 - 메시지 객체의 equals 메서드가 정상 동작한다")
    void t8() {
        // given
        MessageDto message1 = new MessageDto("사용자", "메시지");
        MessageDto message2 = new MessageDto("사용자", "메시지");
        MessageDto message3 = new MessageDto("다른사용자", "메시지");

        // when & then
        assertThat(message1)
                .as("같은 내용의 메시지는 equals에서 true를 반환해야 한다")
                .isEqualTo(message2)
                .as("다른 내용의 메시지는 equals에서 false를 반환해야 한다")
                .isNotEqualTo(message3);
    }

    @Test
    @DisplayName("t9 - 메시지 객체의 hashCode가 일관성있게 동작한다")
    void t9() {
        // given
        MessageDto message1 = new MessageDto("사용자", "메시지");
        MessageDto message2 = new MessageDto("사용자", "메시지");

        // when & then
        assertThat(message1.hashCode())
                .as("같은 내용의 메시지는 동일한 hashCode를 가져야 한다")
                .isEqualTo(message2.hashCode());
    }

    @Test
    @DisplayName("t10 - 기본 생성자로 생성된 객체도 정상 처리된다")
    void t10() {
        // given
        MessageDto defaultMessage = new MessageDto();
        defaultMessage.setSender("후처리사용자");
        defaultMessage.setContent("후처리메시지");

        // when
        MessageDto result = chatWebSocketController.sendMessage(defaultMessage);

        // then
        assertThat(result)
                .as("기본 생성자로 생성된 객체도 정상 처리되어야 한다")
                .isNotNull();

        assertThat(result.getSender())
                .as("setter로 설정한 발신자명이 정확해야 한다")
                .isEqualTo("후처리사용자");

        assertThat(result.getContent())
                .as("setter로 설정한 메시지 내용이 정확해야 한다")
                .isEqualTo("후처리메시지");
    }
}
