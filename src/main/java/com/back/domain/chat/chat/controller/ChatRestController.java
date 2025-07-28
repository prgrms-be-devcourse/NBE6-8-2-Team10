package com.back.domain.chat.chat.controller;

import com.back.domain.chat.chat.dto.MessageDto;
import com.back.domain.chat.chat.service.ChatService;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {
    private final ChatService chatService;

    @GetMapping("/rooms/{chatRoomId}/messages")
    public RsData<List<MessageDto>> getChatRoomMessages(@PathVariable Long chatRoomId) {
        List<MessageDto> messageDtos = chatService.getChatRoomMessages(chatRoomId);

        return new RsData<>("200-1", "채팅방 메시지 조회 성공", messageDtos);
    }
}
