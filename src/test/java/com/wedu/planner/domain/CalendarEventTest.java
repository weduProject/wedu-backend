package com.wedu.planner.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wedu.global.error.BusinessException;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CalendarEventTest {

    @Test
    @DisplayName("캘린더 일정을 생성하며 제목 양끝 공백을 제거한다")
    void create() {
        CalendarEvent event = CalendarEvent.create(
                1L, "  예식장 계약금 납부  ", LocalDate.of(2026, 8, 3));

        assertThat(event.getUserId()).isEqualTo(1L);
        assertThat(event.getTitle()).isEqualTo("예식장 계약금 납부");
        assertThat(event.getEventDate()).isEqualTo(LocalDate.of(2026, 8, 3));
    }

    @Test
    @DisplayName("필수 값이 없거나 제목이 너무 길면 생성할 수 없다")
    void rejectInvalidValues() {
        LocalDate eventDate = LocalDate.of(2026, 8, 3);

        assertThatThrownBy(() -> CalendarEvent.create(null, "일정", eventDate))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> CalendarEvent.create(1L, " ", eventDate))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> CalendarEvent.create(1L, "가".repeat(101), eventDate))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> CalendarEvent.create(1L, "일정", null))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> CalendarEvent.create(1L, "일정", LocalDate.of(999, 12, 31)))
                .isInstanceOf(BusinessException.class);
    }
}
