package com.wedu.product.scheduler;

import com.wedu.product.service.PopularProductRankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 인기 상품 순위를 주기적으로 갱신한다. */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "wedu.popular-product.ranking.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class PopularProductRankingScheduler {

    private final PopularProductRankingService popularProductRankingService;

    @Scheduled(cron = "${wedu.popular-product.ranking.cron:0 0 * * * *}", zone = "UTC")
    public void refreshPeriodically() {
        int ranked = popularProductRankingService.refresh();
        log.info("인기 상품 순위 갱신 완료: {}건", ranked);
    }

    /** 순위가 아직 없는 환경(신규 배포·DB 초기화 직후)에서 첫 스케줄까지 기다리지 않게 한다. */
    @EventListener(ApplicationReadyEvent.class)
    public void refreshOnStartup() {
        int ranked = popularProductRankingService.refreshIfEmpty();
        if (ranked > 0) {
            log.info("인기 상품 순위 최초 생성: {}건", ranked);
        }
    }
}
