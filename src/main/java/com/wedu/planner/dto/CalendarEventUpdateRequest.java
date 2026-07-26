package com.wedu.planner.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wedu.planner.domain.CalendarEventCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/** 캘린더 일정 수정 요청. 선택 필드에 null을 전달하면 기존 값을 비운다. */
public record CalendarEventUpdateRequest(
        @NotBlank(message = "일정 제목은 필수입니다.")
        String title,
        @NotNull(message = "일정 날짜는 필수입니다.") LocalDate eventDate,
        @JsonFormat(without = JsonFormat.Feature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        OffsetDateTime eventAt,
        @NotNull(message = "일정 카테고리는 필수입니다.") CalendarEventCategory category,
        String memo) {
}
