package com.back.domain.trade.controller;

import com.back.domain.files.files.service.FilesService;
import com.back.domain.trade.entity.Trade;
import com.back.domain.trade.service.TradeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TradeControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TradeService tradeService;
    @MockBean
    private FilesService filesService;

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("1. 거래 생성")
    void t1() throws Exception {
        Long postId = 1L;

        String content = objectMapper.writeValueAsString(Map.of("postId", postId));

        ResultActions resultActions = mvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andDo(print());

        Trade trade = tradeService.findLatest();

        resultActions
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("1번 거래가 생성되었습니다."))
                .andExpect(jsonPath("$.data.postId").value(postId))
                .andExpect(jsonPath("$.data.sellerId").value(trade.getSeller().getId()))
                .andExpect(jsonPath("$.data.buyerId").value(trade.getBuyer().getId()))
                .andExpect(jsonPath("$.data.price").value(trade.getPrice()))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("2. 거래 실패 - 게시글이 존재하지 않을 때")
    void t2() throws Exception {
        Long postId = 99L;

        String content = objectMapper.writeValueAsString(Map.of("postId", postId));

        ResultActions resultActions = mvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andDo(print());

        resultActions
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("게시글을 찾을 수 없습니다."));
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("3. 거래 실패 - 자신의 게시글을 구매하려 할 때")
    void t3() throws Exception {
        Long postId = 1L;

        String content = objectMapper.writeValueAsString(Map.of("postId", postId));

        ResultActions resultActions = mvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andDo(print());

        resultActions
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.msg").value("자신의 게시글은 구매할 수 없습니다."));
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("4. 거래 실패 - 이미 판매된 게시글을 구매하려 할 때")
    void t4() throws Exception {
        Long postId = 3L;

        String content = objectMapper.writeValueAsString(Map.of("postId", postId));

        ResultActions resultActions = mvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andDo(print());

        resultActions
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("이미 판매된 게시글입니다."));
    }

}
