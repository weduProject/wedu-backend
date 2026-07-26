package com.wedu.planner.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.planner.domain.CalendarEvent;
import com.wedu.planner.domain.CalendarEventCategory;
import com.wedu.planner.dto.CalendarEventCreateRequest;
import com.wedu.planner.dto.CalendarEventResponse;
import com.wedu.planner.dto.CalendarEventUpdateRequest;
import com.wedu.planner.repository.CalendarEventRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class CalendarEventServiceTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-07-21T16:00:00Z"), ZoneOffset.UTC);

    @Mock
    private CalendarEventRepository calendarEventRepository;

    private CalendarEventService calendarEventService;

    @BeforeEach
    void setUp() {
        calendarEventService = new CalendarEventService(calendarEventRepository, CLOCK);
    }

    @Test
    @DisplayName("Figma 입력 필드를 포함한 캘린더 일정을 생성한다")
    void create() {
        when(calendarEventRepository.save(any(CalendarEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CalendarEventResponse response = calendarEventService.create(1L, createRequest());

        assertThat(response.title()).isEqualTo("드레스 2차 피팅");
        assertThat(response.eventAt()).isEqualTo(Instant.parse("2026-07-11T23:00:00Z"));
        assertThat(response.category()).isEqualTo(CalendarEventCategory.STUDIO_DRESS);
        assertThat(response.memo()).isEqualTo("피팅 준비");
    }

    @Test
    @DisplayName("카테고리 없이 월간 전체 일정을 조회한다")
    void getAllMonthlyEvents() {
        LocalDate firstDay = LocalDate.of(2026, 2, 1);
        LocalDate lastDay = LocalDate.of(2026, 2, 28);
        when(calendarEventRepository
                .findAllByUserIdAndEventDateBetweenOrderByEventDateAscEventAtAscIdAsc(
                        1L, firstDay, lastDay))
                .thenReturn(List.of(event(
                        "드레스 투어", firstDay, CalendarEventCategory.STUDIO_DRESS)));

        List<CalendarEventResponse> result =
                calendarEventService.getMonthlyEvents(1L, 2026, 2, null);

        assertThat(result).extracting(CalendarEventResponse::title)
                .containsExactly("드레스 투어");
    }

    @Test
    @DisplayName("카테고리로 월간 일정을 필터링한다")
    void getMonthlyEventsByCategory() {
        LocalDate firstDay = LocalDate.of(2026, 7, 1);
        LocalDate lastDay = LocalDate.of(2026, 7, 31);
        when(calendarEventRepository
                .findAllByUserIdAndCategoryAndEventDateBetweenOrderByEventDateAscEventAtAscIdAsc(
                        1L, CalendarEventCategory.HONEYMOON, firstDay, lastDay))
                .thenReturn(List.of(event(
                        "허니문 출발",
                        LocalDate.of(2026, 7, 20),
                        CalendarEventCategory.HONEYMOON)));

        List<CalendarEventResponse> result = calendarEventService.getMonthlyEvents(
                1L, 2026, 7, CalendarEventCategory.HONEYMOON);

        assertThat(result).extracting(CalendarEventResponse::category)
                .containsOnly(CalendarEventCategory.HONEYMOON);
    }

    @Test
    @DisplayName("한국 날짜와 UTC 현재 시각부터 다가오는 일정을 제한 개수만큼 조회한다")
    void getUpcomingEvents() {
        LocalDate koreaToday = LocalDate.of(2026, 7, 22);
        Instant now = CLOCK.instant();
        PageRequest page = PageRequest.of(0, 10);
        when(calendarEventRepository.findUpcoming(1L, koreaToday, now, page))
                .thenReturn(List.of(event(
                        "청첩장 발송", koreaToday, CalendarEventCategory.OTHER)));

        List<CalendarEventResponse> result =
                calendarEventService.getUpcomingEvents(1L, null, 10);

        assertThat(result).extracting(CalendarEventResponse::title)
                .containsExactly("청첩장 발송");
    }

    @Test
    @DisplayName("소유한 일정을 수정하고 선택 필드를 비운다")
    void update() {
        CalendarEvent event = event(
                "드레스 피팅",
                LocalDate.of(2026, 7, 12),
                CalendarEventCategory.STUDIO_DRESS);
        when(calendarEventRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(event));
        CalendarEventUpdateRequest request = new CalendarEventUpdateRequest(
                "허니문 출발",
                LocalDate.of(2026, 8, 10),
                null,
                CalendarEventCategory.HONEYMOON,
                null);

        CalendarEventResponse response = calendarEventService.update(1L, 10L, request);

        assertThat(response.title()).isEqualTo("허니문 출발");
        assertThat(response.eventAt()).isNull();
        assertThat(response.memo()).isNull();
    }

    @Test
    @DisplayName("소유한 일정을 삭제한다")
    void delete() {
        CalendarEvent event = event(
                "허니문 출발",
                LocalDate.of(2026, 8, 10),
                CalendarEventCategory.HONEYMOON);
        when(calendarEventRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(event));

        calendarEventService.delete(1L, 10L);

        verify(calendarEventRepository).delete(event);
    }

    @Test
    @DisplayName("다른 사용자의 일정은 수정하거나 삭제할 수 없다")
    void rejectUnownedEvent() {
        when(calendarEventRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> calendarEventService.update(
                        1L,
                        10L,
                        new CalendarEventUpdateRequest(
                                "일정",
                                LocalDate.of(2026, 8, 10),
                                null,
                                CalendarEventCategory.OTHER,
                                null)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.PLANNER_CALENDAR_EVENT_NOT_FOUND));
        assertThatThrownBy(() -> calendarEventService.delete(1L, 10L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("조회 연월과 다가오는 일정 개수를 검증한다")
    void rejectInvalidQuery() {
        assertThatThrownBy(() -> calendarEventService.getMonthlyEvents(1L, 2026, 13, null))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> calendarEventService.getUpcomingEvents(1L, null, 0))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> calendarEventService.getUpcomingEvents(1L, null, 51))
                .isInstanceOf(BusinessException.class);
    }

    private CalendarEventCreateRequest createRequest() {
        return new CalendarEventCreateRequest(
                "드레스 2차 피팅",
                LocalDate.of(2026, 7, 12),
                OffsetDateTime.parse("2026-07-12T08:00:00+09:00"),
                CalendarEventCategory.STUDIO_DRESS,
                "피팅 준비");
    }

    private CalendarEvent event(
            String title,
            LocalDate eventDate,
            CalendarEventCategory category) {
        return CalendarEvent.create(1L, title, eventDate, null, category, null);
    }
}
