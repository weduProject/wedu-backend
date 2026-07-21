package com.wedu.planner.dto;

import com.wedu.planner.domain.DDay;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public record DDayResponse(
        Long ddayId,
        LocalDate weddingDate,
        Instant targetAt,
        long daysRemaining) {

    private static final ZoneId WEDDING_ZONE = ZoneId.of("Asia/Seoul");

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
