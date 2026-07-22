package com.wedu.planner.dto;

import com.wedu.planner.domain.CalendarEvent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** 캘린더 일정 생성 요청. */
public record CalendarEventCreateRequest(
        @NotBlank(message = "일정 제목은 필수입니다.")
        @Size(max = CalendarEvent.MAX_TITLE_LENGTH, message = "일정 제목은 100자 이하여야 합니다.")
        String title,
        @NotNull(message = "일정 날짜는 필수입니다.") LocalDate eventDate) {
}
