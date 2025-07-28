package com.back.domain.chat.chat.entity;

import com.back.domain.post.entity.Post;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class ChatRoom extends BaseEntity {
    //게시글 메니투원 설정
    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    private String name;


    public ChatRoom(Post post, String name) {
        this.post = post;
        this.name = name;
    }
}
