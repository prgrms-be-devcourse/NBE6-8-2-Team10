package com.back.domain.chat.chat.controller;

import com.back.domain.chat.chat.dto.MessageDto;
import com.back.domain.chat.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;


    @MessageMapping("/sendMessage")
    @SendTo("/topic/messages")
    public MessageDto sendMessage(MessageDto chatMessage) {
        chatService.saveMessage(chatMessage);
        return chatMessage;
    }
}
