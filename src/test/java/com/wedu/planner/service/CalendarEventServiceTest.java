package com.wedu.planner.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.planner.domain.CalendarEvent;
import com.wedu.planner.dto.CalendarEventResponse;
import com.wedu.planner.repository.CalendarEventRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalendarEventServiceTest {

    @Mock
    private CalendarEventRepository calendarEventRepository;

    private CalendarEventService calendarEventService;

    @BeforeEach
    void setUp() {
        calendarEventService = new CalendarEventService(calendarEventRepository);
    }

    @Test
    @DisplayName("같은 날짜에도 여러 캘린더 일정을 생성할 수 있다")
    void createMultipleEventsOnSameDate() {
        when(calendarEventRepository.save(any(CalendarEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        LocalDate eventDate = LocalDate.of(2026, 8, 3);

        CalendarEventResponse first = calendarEventService.create(1L, "상견례", eventDate);
        CalendarEventResponse second = calendarEventService.create(1L, "예식장 방문", eventDate);

        assertThat(first.title()).isEqualTo("상견례");
        assertThat(second.title()).isEqualTo("예식장 방문");
        verify(calendarEventRepository, times(2)).save(any(CalendarEvent.class));
    }

    @Test
    @DisplayName("월의 첫날부터 마지막 날까지 사용자 일정을 조회한다")
    void getMonthlyEvents() {
        LocalDate firstDay = LocalDate.of(2026, 2, 1);
        LocalDate lastDay = LocalDate.of(2026, 2, 28);
        when(calendarEventRepository
                .findAllByUserIdAndEventDateBetweenOrderByEventDateAscIdAsc(
                        1L, firstDay, lastDay))
                .thenReturn(List.of(
                        CalendarEvent.create(1L, "드레스 투어", firstDay),
                        CalendarEvent.create(1L, "청첩장 수령", lastDay)));

        List<CalendarEventResponse> result = calendarEventService.getMonthlyEvents(1L, 2026, 2);

        assertThat(result).extracting(CalendarEventResponse::eventDate)
                .containsExactly(firstDay, lastDay);
    }

    @Test
    @DisplayName("조회 연월이 누락되거나 유효하지 않으면 공통 입력 오류를 반환한다")
    void rejectInvalidYearMonth() {
        assertInvalidYearMonth(null, 7);
        assertInvalidYearMonth(2026, null);
        assertInvalidYearMonth(2026, 0);
        assertInvalidYearMonth(2026, 13);
        assertInvalidYearMonth(999, 1);
    }

    private void assertInvalidYearMonth(Integer year, Integer month) {
        assertThatThrownBy(() -> calendarEventService.getMonthlyEvents(1L, year, month))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT));
    }
}
