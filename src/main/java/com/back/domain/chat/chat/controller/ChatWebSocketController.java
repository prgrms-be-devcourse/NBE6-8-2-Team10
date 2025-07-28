package com.back.domain.chat.chat.controller;

import com.back.domain.chat.chat.dto.MessageDto;
import com.back.domain.chat.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;

    @MessageMapping("/sendMessage")
    @SendTo("/topic/messages")
    public MessageDto sendMessage(MessageDto chatMessage) {
        // 디버깅 로그
        System.out.println("=== 받은 메시지 ===");
        System.out.println("sender: " + chatMessage.getSender());
        System.out.println("content: " + chatMessage.getContent());
        System.out.println("senderId: " + chatMessage.getSenderId());
        System.out.println("chatRoomId: " + chatMessage.getChatRoomId());
        System.out.println("=================");

        try {

            chatService.saveMessage(chatMessage);

            return chatMessage;
        } catch (Exception e) {
            System.out.println("에러 발생: " + e.getMessage());
            e.printStackTrace(); // 상세 에러 로그
            MessageDto errorMessage = new MessageDto();
            errorMessage.setSender("System");
            errorMessage.setContent("메시지 전송에 실패했습니다: " + e.getMessage());
            return errorMessage;
        }
    }
}
