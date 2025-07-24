package com.back.domain.chat.chat.entity;

import com.back.global.jpa.entity.BaseEntity;
import com.back.domain.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Message extends BaseEntity {
    //member, chatroom 관계 설정
    @ManyToOne
    private ChatRoom chatRoom;

    @ManyToOne
    private Member sender;

    private String content;

}
