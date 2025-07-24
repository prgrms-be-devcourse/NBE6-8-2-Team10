package com.back.domain.chat.chat.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class MessageDto {
    private String sender;
    private String content;

    public MessageDto() {
    }

    public MessageDto(String sender, String content) {
        this.sender = sender;
        this.content = content;
    }

    // 테스트 비교용 equals(), hashCode() 오버라이딩도 추천
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageDto)) return false;
        MessageDto messageDto = (MessageDto) o;
        return sender.equals(messageDto.sender) && content.equals(messageDto.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, content);
    }
}
