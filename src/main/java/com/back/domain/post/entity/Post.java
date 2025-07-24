package com.back.domain.post.entity;


import com.back.domain.chat.chat.entity.ChatRoom;
import com.back.domain.files.files.entity.Files;
import com.back.domain.member.entity.Member;
import com.back.domain.trade.entity.Trade;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends BaseEntity {

    // Member ID(외래키)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String title; //제목

    @Column(columnDefinition = "TEXT",nullable = false)
    private String description; //게시글 내용

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category; //특허 종류

    @Column(name = "file_url", length = 2048)
    private String fileUrl; //첨부파일 URL

    @Column(nullable = false)
    private Integer price; //특허 가격

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status; //판매 상태

    //찜 개수
    @Column(name = "favorite_cnt", nullable = false)
    private int favoriteCnt;

    // 게시글 1 ↔ 채팅방 N
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoom> chatRooms = new ArrayList<>();

    // 게시글 1 ↔ 거래 1
    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Trade trade;

    // 게시글 1 ↔ 첨부파일 N
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Files> postFiles = new ArrayList<>();

    // 게시글 1 ↔ 찜 N
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FavoritePost> favoritePosts = new ArrayList<>();

    //초기 시간 생성 및 수정, 찜 개수 초기화
    @PrePersist
    protected void onCreate() {
        this.favoriteCnt = 0;
    }

    // 게시글 카테고리 enum / .getlabel()로 한글 추출
    public enum Category {
        PRODUCT("물건발명"),
        METHOD("방법발명"),
        USE("용도발명"),
        DESIGN("디자인권"),
        TRADEMARK("상표권"),
        COPYRIGHT("저작권"),
        ETC("기타");

        private final String label;
        Category(String label) {
            this.label = label;
        }
        public String getLabel() {
            return label;
        }
    }

    //판매 상태 enum / .getlabel()로 한글 추출
    public enum Status {
        SALE("판매중"),
        SOLD_OUT("판매완료");

        private final String label;
        Status(String label) {
            this.label = label;
        }
        public String getLabel() {
            return label;
        }
    }

    // 찜 개수 증가 및 감소
    public void increaseFavoriteCnt() {
        this.favoriteCnt += 1;
    }
    public void decreaseFavoriteCnt() {
        this.favoriteCnt = Math.max(0, this.favoriteCnt - 1); // 음수 방지
    }

}