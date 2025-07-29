package com.back.domain.chat.chat.controller;

import com.back.domain.chat.chat.dto.ChatRoomDto;
import com.back.domain.chat.chat.dto.MessageDto;
import com.back.domain.chat.chat.service.ChatService;
import com.back.domain.member.entity.Member;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {
    private final ChatService chatService;


    @GetMapping("/rooms/{chatRoomId}/messages")
    public RsData<List<MessageDto>> getChatRoomMessages(@PathVariable Long chatRoomId, Principal principal) {
        Member member = chatService.findByName(principal.getName());

        List<MessageDto> messageDtos = chatService.getChatRoomMessages(chatRoomId, principal);

        return new RsData<>("200-1", "채팅방 메시지 조회 성공", messageDtos);
    }

    @PostMapping("/rooms/{postId}")
    public RsData<Void> createChatRoom(@PathVariable Long postId, Principal principal){
        chatService.createChatRoom(postId, principal.getName());

        return new RsData<>("200-1", "채팅방 생성 성공");
    }

    @GetMapping("/rooms/my")
    public RsData<List<ChatRoomDto>> getMyChatRooms(Principal principal) {
        List<ChatRoomDto> chatRooms = chatService.getMyChatRooms(principal);

        return new RsData<>("200-1", "내 채팅방 목록 조회 성공", chatRooms);
    }

    @DeleteMapping("/rooms/{chatRoomId}")
    public RsData<ChatRoomDto> deleteChatRoom(@PathVariable Long chatRoomId) {
        chatService.deleteChatRoom(chatRoomId);

        return new RsData<>("200-1", "채팅방 삭제 성공");
    }


}
