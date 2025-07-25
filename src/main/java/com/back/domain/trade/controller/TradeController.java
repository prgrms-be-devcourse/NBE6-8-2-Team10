package com.back.domain.trade.controller;

import com.back.domain.trade.dto.TradeDto;
import com.back.domain.trade.entity.Trade;
import com.back.domain.trade.service.TradeService;
import com.back.global.rsData.RsData;
import com.back.global.security.auth.MemberDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {
    private final TradeService tradeService;

    public record TradeCreateReqBody(
            @NotNull @Positive Long postId
    ) {}
    @PostMapping
    @Operation(summary = "거래 생성")
    public RsData<TradeDto> createTrade(
        @AuthenticationPrincipal MemberDetails memberDetails,
        @RequestBody @Valid TradeCreateReqBody reqBody) {
        Long buyerId = memberDetails.getMember().getId();
        Trade trade = tradeService.createTrade(reqBody.postId(), buyerId);

        return new RsData<>(
                "201-1",
                "%s번 거래가 생성되었습니다.".formatted(trade.getId()),
                new TradeDto(trade)
        );
    }
}
