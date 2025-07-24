package com.back.domain.chat.chat.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class ChatRoom extends BaseEntity {
    //게시글 메니투원 설정

    private String name;

}
