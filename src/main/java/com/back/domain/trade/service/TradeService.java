package com.back.domain.trade.service;

import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.post.entity.Post;
import com.back.domain.post.repository.PostRepository;
import com.back.domain.trade.entity.Trade;
import com.back.domain.trade.entity.TradeStatus;
import com.back.domain.trade.repository.TradeRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TradeService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final TradeRepository tradeRepository;

    @Transactional
    public Trade createTrade(Long postId, Long buyerId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ServiceException("404-1", "게시글을 찾을 수 없습니다."));

        if (post.getStatus() == Post.Status.SOLD_OUT) {
            throw new ServiceException("400-1", "이미 판매된 게시글입니다.");
        }

        if (post.getMember().getId().equals(buyerId)) {
            throw new ServiceException("403-1", "자신의 게시글은 구매할 수 없습니다.");
        }

        Member seller = post.getMember();
        Member buyer = memberRepository.findById(buyerId)
                .orElseThrow(() -> new ServiceException("404-2", "구매자를 찾을 수 없습니다."));

        Trade trade = new Trade(post, seller, buyer, post.getPrice(), TradeStatus.COMPLETED);
        tradeRepository.save(trade);

        post.markAsSoldOut(); // 판매 완료로 상태 변경

        return trade;
    }
}