package com.accsaber.backend.scheduler;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.accsaber.backend.service.item.ItemTradeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ItemTradeExpiryScheduler {

    private static final int EXPIRY_DAYS = 14;

    private final ItemTradeService itemTradeService;

    @Scheduled(cron = "${accsaber.scheduler.trade-expiry-cron:0 30 4 * * *}")
    public void expireStalePendingTrades() {
        Instant cutoff = Instant.now().minus(EXPIRY_DAYS, ChronoUnit.DAYS);
        int expired = itemTradeService.expireOlderThan(cutoff);
        if (expired > 0) {
            log.info("Expired {} pending item trades older than {} days", expired, EXPIRY_DAYS);
        }
    }
}
