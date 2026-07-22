package com.wedu.planner.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wedu.global.error.BusinessException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
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
                OffsetDateTime.parse("2026-07-12T08:00:00+09:00"),
                CalendarEventCategory.STUDIO_DRESS,
                "  피팅 드레스 사진 촬영  ");

        assertThat(event.getTitle()).isEqualTo("드레스 2차 피팅");
        assertThat(event.getEventAt()).isEqualTo(Instant.parse("2026-07-11T23:00:00Z"));
        assertThat(event.getCategory()).isEqualTo(CalendarEventCategory.STUDIO_DRESS);
        assertThat(event.getMemo()).isEqualTo("피팅 드레스 사진 촬영");
    }

    @Test
    @DisplayName("시간과 메모가 없는 날짜 전용 일정을 생성할 수 있다")
    void createDateOnlyEvent() {
        CalendarEvent event = event("청첩장 발송 마감", null, " ");

        assertThat(event.getEventAt()).isNull();
        assertThat(event.getMemo()).isNull();
    }

    @Test
    @DisplayName("일정의 모든 폼 값을 수정하고 선택 값은 비울 수 있다")
    void update() {
        CalendarEvent event = event(
                "드레스 피팅", OffsetDateTime.parse("2026-07-12T14:00:00+09:00"), "준비물 확인");

        event.update(
                "웨딩밴드 픽업",
                LocalDate.of(2026, 7, 20),
                null,
                CalendarEventCategory.JEWELRY_GIFTS,
                null);

        assertThat(event.getTitle()).isEqualTo("웨딩밴드 픽업");
        assertThat(event.getEventDate()).isEqualTo(LocalDate.of(2026, 7, 20));
        assertThat(event.getEventAt()).isNull();
        assertThat(event.getCategory()).isEqualTo(CalendarEventCategory.JEWELRY_GIFTS);
        assertThat(event.getMemo()).isNull();
    }

    @Test
    @DisplayName("일정 수정 시 입력 오프셋을 보존해 검증하고 UTC 시각으로 변환한다")
    void updateWithOffsetDateTime() {
        CalendarEvent event = event("드레스 피팅", null, null);

        event.update(
                "드레스 피팅",
                LocalDate.of(2026, 7, 13),
                OffsetDateTime.parse("2026-07-13T08:00:00+09:00"),
                CalendarEventCategory.STUDIO_DRESS,
                null);

        assertThat(event.getEventDate()).isEqualTo(LocalDate.of(2026, 7, 13));
        assertThat(event.getEventAt()).isEqualTo(Instant.parse("2026-07-12T23:00:00Z"));
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
        assertInvalid(() -> CalendarEvent.create(
                1L,
                "일정",
                eventDate,
                OffsetDateTime.parse("2026-08-04T00:00:00Z"),
                CalendarEventCategory.OTHER,
                null));
    }

    @Test
    @DisplayName("제목 길이는 이모지를 포함해 유니코드 문자 수로 검증한다")
    void validateTitleByCodePoint() {
        String oneHundredEmoji = "😀".repeat(100);

        CalendarEvent event = CalendarEvent.create(
                1L,
                oneHundredEmoji,
                LocalDate.of(2026, 8, 3),
                null,
                CalendarEventCategory.OTHER,
                null);

        assertThat(event.getTitle()).isEqualTo(oneHundredEmoji);
        assertInvalid(() -> CalendarEvent.create(
                1L,
                "😀".repeat(101),
                LocalDate.of(2026, 8, 3),
                null,
                CalendarEventCategory.OTHER,
                null));
    }

    private CalendarEvent event(String title, OffsetDateTime eventAt, String memo) {
        return CalendarEvent.create(
                1L,
                title,
                LocalDate.of(2026, 7, 12),
                eventAt,
                CalendarEventCategory.STUDIO_DRESS,
                memo);
    }

    private void assertInvalid(org.assertj.core.api.ThrowableAssert.ThrowingCallable callable) {
        assertThatThrownBy(callable).isInstanceOf(BusinessException.class);
    }
}
