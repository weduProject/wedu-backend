package com.wedu.planner.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wedu.global.error.BusinessException;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CalendarEventTest {

    @Test
    @DisplayName("시간·카테고리·메모를 포함한 일정을 생성하고 문자열을 정규화한다")
    void create() {
        CalendarEvent event = CalendarEvent.create(
                1L,
                "  드레스 2차 피팅  ",
                LocalDate.of(2026, 7, 12),
                LocalTime.of(14, 0),
                CalendarEventCategory.STUDIO_DRESS,
                "  피팅 드레스 사진 촬영  ");

        assertThat(event.getTitle()).isEqualTo("드레스 2차 피팅");
        assertThat(event.getEventTime()).isEqualTo(LocalTime.of(14, 0));
        assertThat(event.getCategory()).isEqualTo(CalendarEventCategory.STUDIO_DRESS);
        assertThat(event.getMemo()).isEqualTo("피팅 드레스 사진 촬영");
    }

    @Test
    @DisplayName("시간과 메모가 없는 날짜 전용 일정을 생성할 수 있다")
    void createDateOnlyEvent() {
        CalendarEvent event = event("청첩장 발송 마감", null, " ");

        assertThat(event.getEventTime()).isNull();
        assertThat(event.getMemo()).isNull();
    }

    @Test
    @DisplayName("일정의 모든 폼 값을 수정하고 선택 값은 비울 수 있다")
    void update() {
        CalendarEvent event = event("드레스 피팅", LocalTime.of(14, 0), "준비물 확인");

        event.update(
                "웨딩밴드 픽업",
                LocalDate.of(2026, 7, 20),
                null,
                CalendarEventCategory.JEWELRY_GIFTS,
                null);

        assertThat(event.getTitle()).isEqualTo("웨딩밴드 픽업");
        assertThat(event.getEventDate()).isEqualTo(LocalDate.of(2026, 7, 20));
        assertThat(event.getEventTime()).isNull();
        assertThat(event.getCategory()).isEqualTo(CalendarEventCategory.JEWELRY_GIFTS);
        assertThat(event.getMemo()).isNull();
    }

    @Test
    @DisplayName("필수 값과 길이 및 DB 날짜 범위를 검증한다")
    void rejectInvalidValues() {
        LocalDate eventDate = LocalDate.of(2026, 8, 3);

        assertInvalid(() -> CalendarEvent.create(
                null, "일정", eventDate, null, CalendarEventCategory.OTHER, null));
        assertInvalid(() -> CalendarEvent.create(
                1L, " ", eventDate, null, CalendarEventCategory.OTHER, null));
        assertInvalid(() -> CalendarEvent.create(
                1L, "가".repeat(101), eventDate, null, CalendarEventCategory.OTHER, null));
        assertInvalid(() -> CalendarEvent.create(
                1L, "일정", null, null, CalendarEventCategory.OTHER, null));
        assertInvalid(() -> CalendarEvent.create(
                1L, "일정", eventDate, null, null, null));
        assertInvalid(() -> CalendarEvent.create(
                1L, "일정", eventDate, null, CalendarEventCategory.OTHER, "가".repeat(501)));
        assertInvalid(() -> CalendarEvent.create(
                1L,
                "일정",
                LocalDate.of(999, 12, 31),
                null,
                CalendarEventCategory.OTHER,
                null));
    }

    private CalendarEvent event(String title, LocalTime eventTime, String memo) {
        return CalendarEvent.create(
                1L,
                title,
                LocalDate.of(2026, 7, 12),
                eventTime,
                CalendarEventCategory.STUDIO_DRESS,
                memo);
    }

    private void assertInvalid(org.assertj.core.api.ThrowableAssert.ThrowingCallable callable) {
        assertThatThrownBy(callable).isInstanceOf(BusinessException.class);
    }
}
