package com.streamhub.payment.common.scheduler;

import com.streamhub.payment.idempotency.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotencyCleanupScheduler {

    private final IdempotencyService idempotencyService;

    @Scheduled(cron = "${idempotency.cleanup.cron}")
    public void cleanupExpiredKeys(){

        log.info("Starting idempotency cleanup.");
        idempotencyService.deleteExpiredKeys();
        log.info("Completed idempotency cleanup.");
    }
}
