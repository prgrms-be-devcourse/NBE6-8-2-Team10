package com.back.domain.chat.redis.publisher;

import com.back.domain.chat.chat.dto.MessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisMessagePublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic chatTopic;

    /*
     * Redis 채널에 메시지 발행
     * @param message 발행할 메시지
     */
    public void publish(MessageDto message) {
        try {
            log.info("=== Redis Publisher 메시지 발행 ===");
            log.info("발행할 메시지: 채팅방={}, 발신자={}, 내용={}",
                    message.getChatRoomId(),
                    message.getSenderName(),
                    message.getContent());

            // Redis 채널에 메시지 발행
            redisTemplate.convertAndSend(chatTopic.getTopic(), message);

            log.info("메시지 발행 완료: 토픽={}", chatTopic.getTopic());

        } catch (Exception e) {
            log.error("Redis 메시지 발행 실패: {}", e.getMessage(), e);
            throw new RuntimeException("메시지 발행 중 오류가 발생했습니다.", e);
        }
    }

    /*
     * 특정 토픽에 메시지 발행
     * @param topic 발행할 토픽
     * @param message 발행할 메시지
     */
    public void publishToTopic(String topic, MessageDto message) {
        try {
            log.info("=== 특정 토픽으로 메시지 발행 ===");
            log.info("토픽: {}, 메시지: {}", topic, message.getContent());

            redisTemplate.convertAndSend(topic, message);

            log.info("특정 토픽 메시지 발행 완료: {}", topic);

        } catch (Exception e) {
            log.error("특정 토픽 메시지 발행 실패: {}", e.getMessage(), e);
            throw new RuntimeException("특정 토픽 메시지 발행 중 오류가 발생했습니다.", e);
        }
    }
}
