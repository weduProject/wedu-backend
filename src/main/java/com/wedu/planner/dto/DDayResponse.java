package com.wedu.planner.dto;

import com.wedu.planner.domain.DDay;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/** 결혼식 날짜와 한국 시간 자정 기준 카운트다운 정보를 제공하는 응답. */
public record DDayResponse(
        Long ddayId,
        LocalDate weddingDate,
        Instant targetAt,
        long daysRemaining) {

    private static final ZoneId WEDDING_ZONE = ZoneId.of("Asia/Seoul");

    /** D-day 엔티티를 목표 UTC 시각과 남은 일수가 포함된 응답으로 변환한다. */
    public static DDayResponse from(DDay dDay, LocalDate today) {
        Instant targetAt = dDay.getWeddingDate()
                .atStartOfDay(WEDDING_ZONE)
                .toInstant();
        return new DDayResponse(
                dDay.getId(),
                dDay.getWeddingDate(),
                targetAt,
                dDay.daysRemaining(today));
    }
}
