package com.wedu.planner.dto;

import com.wedu.planner.domain.CalendarEvent;
import com.wedu.planner.domain.CalendarEventCategory;
import java.time.Instant;
import java.time.LocalDate;

/** 캘린더 일정 응답. */
public record CalendarEventResponse(
        Long eventId,
        String title,
        LocalDate eventDate,
        Instant eventAt,
        CalendarEventCategory category,
        String memo) {

    /** 엔티티를 외부 응답으로 변환한다. */
    public static CalendarEventResponse from(CalendarEvent event) {
        return new CalendarEventResponse(
                event.getId(),
                event.getTitle(),
                event.getEventDate(),
                event.getEventAt(),
                event.getCategory(),
                event.getMemo());
    }
}
