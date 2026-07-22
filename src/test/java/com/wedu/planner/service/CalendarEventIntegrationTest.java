package com.wedu.planner.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.wedu.planner.domain.CalendarEventCategory;
import com.wedu.planner.dto.CalendarEventCreateRequest;
import com.wedu.planner.dto.CalendarEventResponse;
import com.wedu.planner.dto.CalendarEventUpdateRequest;
import com.wedu.planner.repository.CalendarEventRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CalendarEventIntegrationTest {

    @Autowired
    private CalendarEventService calendarEventService;

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @Autowired
    private Clock clock;

    @BeforeEach
    void setUp() {
        calendarEventRepository.deleteAll();
    }

    @Test
    @DisplayName("월 경계·사용자·카테고리를 지키며 날짜와 시간순으로 조회한다")
    void getMonthlyEvents() {
        create(1L, "이전 달", LocalDate.of(2026, 6, 30), null, CalendarEventCategory.OTHER);
        create(1L, "시간 없음", LocalDate.of(2026, 7, 12), null, CalendarEventCategory.STUDIO_DRESS);
        create(1L, "오전 일정", LocalDate.of(2026, 7, 12), LocalTime.of(11, 0), CalendarEventCategory.STUDIO_DRESS);
        create(1L, "오후 일정", LocalDate.of(2026, 7, 12), LocalTime.of(14, 0), CalendarEventCategory.STUDIO_DRESS);
        create(1L, "다른 카테고리", LocalDate.of(2026, 7, 20), null, CalendarEventCategory.HONEYMOON);
        create(2L, "다른 사용자", LocalDate.of(2026, 7, 12), null, CalendarEventCategory.STUDIO_DRESS);

        List<CalendarEventResponse> result = calendarEventService.getMonthlyEvents(
                1L, 2026, 7, CalendarEventCategory.STUDIO_DRESS);

        assertThat(result).extracting(CalendarEventResponse::title)
                .containsExactly("시간 없음", "오전 일정", "오후 일정");
    }

    @Test
    @DisplayName("다가오는 일정 조회와 수정·삭제가 DB까지 반영된다")
    void upcomingUpdateAndDelete() {
        CalendarEventResponse created = create(
                1L,
                "허니문 출발",
                today().plusDays(1),
                LocalTime.of(9, 0),
                CalendarEventCategory.HONEYMOON);

        assertThat(calendarEventService.getUpcomingEvents(
                1L, CalendarEventCategory.HONEYMOON, 10))
                .extracting(CalendarEventResponse::eventId)
                .contains(created.eventId());

        CalendarEventResponse updated = calendarEventService.update(
                1L,
                created.eventId(),
                new CalendarEventUpdateRequest(
                        "허니문 출발 변경",
                        today().plusDays(2),
                        null,
                        CalendarEventCategory.HONEYMOON,
                        null));
        assertThat(updated.title()).isEqualTo("허니문 출발 변경");
        assertThat(updated.eventTime()).isNull();

        calendarEventService.delete(1L, created.eventId());
        assertThat(calendarEventRepository.findById(created.eventId())).isEmpty();
    }

    private CalendarEventResponse create(
            Long userId,
            String title,
            LocalDate date,
            LocalTime time,
            CalendarEventCategory category) {
        return calendarEventService.create(
                userId,
                new CalendarEventCreateRequest(title, date, time, category, null));
    }

    private LocalDate today() {
        return LocalDate.now(clock.withZone(ZoneId.of("Asia/Seoul")));
    }
}
