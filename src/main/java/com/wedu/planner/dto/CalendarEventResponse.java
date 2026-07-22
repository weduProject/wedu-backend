package com.wedu.planner.dto;

import com.wedu.planner.domain.CalendarEvent;
import java.time.LocalDate;

/** 캘린더 일정 응답. */
public record CalendarEventResponse(Long eventId, String title, LocalDate eventDate) {

    /** 엔티티를 외부 응답으로 변환한다. */
    public static CalendarEventResponse from(CalendarEvent event) {
        return new CalendarEventResponse(event.getId(), event.getTitle(), event.getEventDate());
    }
}
