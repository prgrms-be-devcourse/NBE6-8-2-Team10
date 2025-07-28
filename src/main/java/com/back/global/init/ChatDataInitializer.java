package com.back.global.init;

import com.back.domain.chat.chat.entity.ChatRoom;
import com.back.domain.chat.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Profile("dev")
@Component
@RequiredArgsConstructor
@Slf4j
@Order(2) // DevDataInitializer 이후에 실행
public class ChatDataInitializer implements ApplicationRunner {

    private final ChatRoomRepository chatRoomRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("===== 채팅 데이터 초기화 시작 =====");
        initChatRooms();
        log.info("===== 채팅 데이터 초기화 완료 =====");
    }

    @Transactional
    public void initChatRooms() {
        // 기본 채팅방들 생성
        createChatRoomIfNotExists("일반 채팅방");
        createChatRoomIfNotExists("자유 대화방");
        createChatRoomIfNotExists("질문&답변방");
    }

    private void createChatRoomIfNotExists(String roomName) {
        // 같은 이름의 채팅방이 없으면 생성
        if (chatRoomRepository.findByName(roomName) == null) {
            ChatRoom chatRoom = new ChatRoom();
            chatRoom.setName(roomName);
            // post는 일단 null로 설정 (나중에 게시글과 연결)
            chatRoom.setPost(null);
            
            chatRoomRepository.save(chatRoom);
            log.info("채팅방 '{}' 이 생성되었습니다.", roomName);
        }
    }
}
