package com.back.domain.trade.repository;

import com.back.domain.trade.entity.Trade;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    // 거래 내역 조회: 구매자 또는 판매자로 필터링
    Page<Trade> findByBuyerOrSeller(Long buyerId, Long sellerId, Pageable pageable);
}
