package com.wedu.planner.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.wedu.planner.dto.CalendarEventResponse;
import com.wedu.planner.repository.CalendarEventRepository;
import java.time.LocalDate;
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

    @BeforeEach
    void setUp() {
        calendarEventRepository.deleteAll();
    }

    @Test
    @DisplayName("월 경계와 사용자 범위를 지키며 날짜와 등록 순서대로 조회한다")
    void getMonthlyEventsWithBoundariesAndUserIsolation() {
        calendarEventService.create(1L, "이전 달", LocalDate.of(2026, 7, 31));
        calendarEventService.create(1L, "첫날 첫 일정", LocalDate.of(2026, 8, 1));
        calendarEventService.create(1L, "첫날 두 번째 일정", LocalDate.of(2026, 8, 1));
        calendarEventService.create(1L, "마지막 날", LocalDate.of(2026, 8, 31));
        calendarEventService.create(1L, "다음 달", LocalDate.of(2026, 9, 1));
        calendarEventService.create(2L, "다른 사용자", LocalDate.of(2026, 8, 15));

        List<CalendarEventResponse> result = calendarEventService.getMonthlyEvents(1L, 2026, 8);

        assertThat(result).extracting(CalendarEventResponse::title)
                .containsExactly("첫날 첫 일정", "첫날 두 번째 일정", "마지막 날");
    }
}
