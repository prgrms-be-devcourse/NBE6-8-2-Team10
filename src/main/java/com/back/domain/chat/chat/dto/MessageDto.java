package com.back.domain.chat.chat.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class MessageDto {
    private Long senderId;
    private Long chatRoomId;

    private String senderName;
    private String senderEmail;
    private String content;

    public MessageDto() {
        // 프론트 연결 시 동적으로 할당
        this.senderId = 1L;
        this.chatRoomId = 1L;
    }



    public MessageDto(String senderName, String content, Long senderId, Long chatRoomId) {
        this.senderName = senderName;
        this.content = content;

        this.senderId = senderId;
        this.chatRoomId = chatRoomId;
    }

    // 기존 프론트엔드 호환성을 위한 getter
    public String getSender() {
        return this.senderName;
    }

    // 기존 프론트엔드 호환성을 위한 setter
    public void setSender(String sender) {
        this.senderName = sender;
    }

    // 테스트 비교용 equals(), hashCode() 오버라이딩도 추천
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageDto)) return false;
        MessageDto messageDto = (MessageDto) o;
        return Objects.equals(senderId, messageDto.senderId) &&
                Objects.equals(content, messageDto.content) &&
                Objects.equals(chatRoomId, messageDto.chatRoomId) &&
                Objects.equals(senderEmail, messageDto.senderEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(senderId, content, chatRoomId, senderEmail);
    }
}
