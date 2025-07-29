package com.back.domain.chat.chat.controller;

import com.back.domain.chat.chat.entity.ChatRoom;
import com.back.domain.chat.chat.repository.ChatRoomRepository;
import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.Role;
import com.back.domain.member.entity.Status;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.post.entity.Post;
import com.back.domain.post.repository.PostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@DisplayName("ChatRestController deleteChatRoom 테스트")
public class ChatRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Member testMember;
    private Post testPost;
    private ChatRoom testChatRoom;

    @BeforeEach
    void setUp() {
        // 테스트용 Member 생성
        testMember = Member.builder()
                .email("test@example.com")
                .password(passwordEncoder.encode("password123!"))
                .name("테스트유저")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();
        memberRepository.save(testMember);

        // 테스트용 Post 생성
        testPost = Post.builder()
                .member(testMember)
                .title("테스트 게시글")
                .description("테스트 게시글 내용")
                .category(Post.Category.PRODUCT)
                .price(100000)
                .status(Post.Status.SALE)
                .build();
        postRepository.save(testPost);

        // 테스트용 ChatRoom 생성
        testChatRoom = new ChatRoom(testPost, testMember);
        chatRoomRepository.save(testChatRoom);
    }

    @Test
    @DisplayName("채팅방 삭제 성공")
    void deleteChatRoom_success() throws Exception {
        // given
        Long chatRoomId = testChatRoom.getId();
        
        // 채팅방이 존재하는지 확인
        assertThat(chatRoomRepository.existsById(chatRoomId)).isTrue();

        // when & then
        mockMvc.perform(delete("/api/chat/rooms/{chatRoomId}", chatRoomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("채팅방 삭제 성공"));

        // 채팅방이 실제로 삭제되었는지 확인
        assertThat(chatRoomRepository.existsById(chatRoomId)).isFalse();
    }

    @Test
    @DisplayName("채팅방 삭제 실패 - 존재하지 않는 채팅방")
    void deleteChatRoom_fail_notFound() throws Exception {
        // given
        Long nonExistentChatRoomId = 99999L;

        // when & then
        mockMvc.perform(delete("/api/chat/rooms/{chatRoomId}", nonExistentChatRoomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-4"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 채팅방입니다."));
    }

    @Test
    @DisplayName("채팅방 삭제 실패 - 잘못된 경로 파라미터")
    void deleteChatRoom_fail_invalidPath() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/chat/rooms/invalid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("채팅방 삭제 후 다시 삭제 시도 시 실패")
    void deleteChatRoom_fail_alreadyDeleted() throws Exception {
        // given
        Long chatRoomId = testChatRoom.getId();

        // 첫 번째 삭제 (성공)
        mockMvc.perform(delete("/api/chat/rooms/{chatRoomId}", chatRoomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // when & then - 두 번째 삭제 시도 (실패)
        mockMvc.perform(delete("/api/chat/rooms/{chatRoomId}", chatRoomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-4"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 채팅방입니다."));
    }

    @Test
    @DisplayName("여러 채팅방 중 특정 채팅방만 삭제")
    void deleteChatRoom_specific_among_multiple() throws Exception {
        // given - 추가 채팅방 생성
        Post additionalPost = Post.builder()
                .member(testMember)
                .title("추가 테스트 게시글")
                .description("추가 테스트 게시글 내용")
                .category(Post.Category.DESIGN)
                .price(300000)
                .status(Post.Status.SALE)
                .build();
        postRepository.save(additionalPost);

        ChatRoom additionalChatRoom = new ChatRoom(additionalPost, testMember);
        chatRoomRepository.save(additionalChatRoom);

        // 총 2개의 채팅방이 있는지 확인
        assertThat(chatRoomRepository.count()).isEqualTo(2L);

        // when - 첫 번째 채팅방만 삭제
        Long firstChatRoomId = testChatRoom.getId();
        mockMvc.perform(delete("/api/chat/rooms/{chatRoomId}", firstChatRoomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // then - 첫 번째는 삭제되고 두 번째는 남아있어야 함
        assertThat(chatRoomRepository.existsById(firstChatRoomId)).isFalse();
        assertThat(chatRoomRepository.existsById(additionalChatRoom.getId())).isTrue();
        assertThat(chatRoomRepository.count()).isEqualTo(1L);
    }
}
