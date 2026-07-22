package com.wedu.planner.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wedu.planner.domain.CalendarEventCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/** 캘린더 일정 생성 요청. */
public record CalendarEventCreateRequest(
        @NotBlank(message = "일정 제목은 필수입니다.")
        String title,
        @NotNull(message = "일정 날짜는 필수입니다.") LocalDate eventDate,
        @JsonFormat(without = JsonFormat.Feature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        OffsetDateTime eventAt,
        @NotNull(message = "일정 카테고리는 필수입니다.") CalendarEventCategory category,
        String memo) {
}
