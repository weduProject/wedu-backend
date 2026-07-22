package com.wedu.planner.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wedu.planner.domain.CalendarEvent;
import com.wedu.planner.domain.CalendarEventCategory;
import java.time.LocalDate;
import java.time.LocalTime;

/** 캘린더 일정 응답. */
public record CalendarEventResponse(
        Long eventId,
        String title,
        LocalDate eventDate,
        @JsonFormat(pattern = "HH:mm") LocalTime eventTime,
        CalendarEventCategory category,
        String memo) {

    /** 엔티티를 외부 응답으로 변환한다. */
    public static CalendarEventResponse from(CalendarEvent event) {
        return new CalendarEventResponse(
                event.getId(),
                event.getTitle(),
                event.getEventDate(),
                event.getEventTime(),
                event.getCategory(),
                event.getMemo());
    }
}
