package com.back.domain.chat.chat.controller;

import com.back.domain.chat.chat.dto.MessageDto;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @MessageMapping("/sendMessage")
    @SendTo("topic/messages")
    public MessageDto sendMessage(MessageDto chatMessage) {

        return chatMessage;
    }
}
