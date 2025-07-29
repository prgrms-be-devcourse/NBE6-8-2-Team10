package com.back.domain.chat.chat.controller;

import com.back.domain.chat.chat.dto.MessageDto;
import com.back.domain.chat.chat.entity.Message;
import com.back.domain.chat.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/sendMessage")
    public void sendMessage(MessageDto chatMessage) {  // void로 변경, @SendTo 제거
        System.out.println("=== WebSocket 메시지 수신 ===");
        System.out.println("sender: " + chatMessage.getSender());
        System.out.println("senderEmail: " + chatMessage.getSenderEmail());
        System.out.println("content: " + chatMessage.getContent());
        System.out.println("senderId: " + chatMessage.getSenderId());
        System.out.println("chatRoomId: " + chatMessage.getChatRoomId());
        System.out.println("=================");

        try {
            // 메시지 저장
            Message savedMessage = chatService.saveMessage(chatMessage);
            System.out.println("메시지 저장 완료: " + savedMessage.getId());

            // 채팅방 참여자들 조회
            List<String> participants = chatService.getParticipants(chatMessage.getChatRoomId());

            System.out.println("=== 참여자 조회 결과 ===");
            System.out.println("채팅방 ID: " + chatMessage.getChatRoomId());
            System.out.println("참여자 수: " + participants.size());
            for (String participant : participants) {
                System.out.println("참여자 이메일: " + participant);
            }
            System.out.println("======================");

            // 각 참여자에게 개별 전송
            for (String userEmail : participants) {
                String destination = "/queue/chat/" + chatMessage.getChatRoomId();
                System.out.println("메시지 전송 시도:");
                System.out.println("  대상 사용자: " + userEmail);
                System.out.println("  목적지: " + destination);
                System.out.println("  메시지 내용: " + chatMessage.getContent());

                try {
                    messagingTemplate.convertAndSendToUser(
                            userEmail,
                            destination,
                            chatMessage
                    );
                    System.out.println("  ✅ 전송 성공: " + userEmail);
                } catch (Exception sendError) {
                    System.out.println("  ❌ 전송 실패: " + userEmail + ", 오류: " + sendError.getMessage());
                    sendError.printStackTrace();
                }
            }

            if (participants.isEmpty()) {
                System.out.println("⚠️ 경고: 참여자가 없어서 메시지가 전송되지 않았습니다!");
            } else {
                System.out.println("메시지 전송 완료. 총 " + participants.size() + "명에게 전송됨");
            }

        } catch (Exception e) {
            System.out.println("❌ 메시지 처리 중 에러 발생: " + e.getMessage());
            e.printStackTrace();

            // 에러 메시지는 발신자에게만 전송
            MessageDto errorMessage = new MessageDto();
            errorMessage.setSender("System");
            errorMessage.setContent("메시지 전송에 실패했습니다: " + e.getMessage());

            try {
                messagingTemplate.convertAndSendToUser(
                        chatMessage.getSenderEmail(),
                        "/queue/error",
                        errorMessage
                );
            } catch (Exception errorSendFail) {
                System.out.println("에러 메시지 전송도 실패: " + errorSendFail.getMessage());
            }
        }
    }
}
