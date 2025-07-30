package com.back.domain.chat.chat.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class MessageDto {
    private Long senderId;
    private Long chatRoomId;

    private String senderName;
    private String senderEmail;
    private String content;

    // Jackson JSON 역직렬화를 위한 sender 필드 (senderName과 동일)
    @JsonProperty("sender")
    private String sender;

    @JsonCreator
    public MessageDto(
            @JsonProperty("senderId") Long senderId,
            @JsonProperty("chatRoomId") Long chatRoomId,
            @JsonProperty("senderName") String senderName,
            @JsonProperty("senderEmail") String senderEmail,
            @JsonProperty("content") String content,
            @JsonProperty("sender") String sender) {
        this.senderId = senderId;
        this.chatRoomId = chatRoomId;
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.content = content;
        this.sender = sender != null ? sender : senderName; // sender가 null이면 senderName 사용
    }

    public MessageDto(String senderName, String content, Long senderId, Long chatRoomId) {
        this.senderName = senderName;
        this.sender = senderName; // 동기화
        this.content = content;
        this.senderId = senderId;
        this.chatRoomId = chatRoomId;
    }

    // senderName과 sender 동기화
    public void setSenderName(String senderName) {
        this.senderName = senderName;
        this.sender = senderName;
    }

    public void setSender(String sender) {
        this.sender = sender;
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
