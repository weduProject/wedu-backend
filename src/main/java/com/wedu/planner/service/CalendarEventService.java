package com.wedu.planner.service;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.planner.domain.CalendarEvent;
import com.wedu.planner.domain.CalendarEventCategory;
import com.wedu.planner.dto.CalendarEventCreateRequest;
import com.wedu.planner.dto.CalendarEventResponse;
import com.wedu.planner.dto.CalendarEventUpdateRequest;
import com.wedu.planner.repository.CalendarEventRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 캘린더 일정 생성·조회·수정·삭제 유스케이스를 처리한다. */
@Service
@RequiredArgsConstructor
public class CalendarEventService {

    private static final ZoneId WEDDING_ZONE = ZoneId.of("Asia/Seoul");
    private static final int MAX_UPCOMING_LIMIT = 50;

    private final CalendarEventRepository calendarEventRepository;
    private final Clock clock;

    /** 사용자에게 날짜 단위 일정을 추가한다. */
    @Transactional
    public CalendarEventResponse create(Long userId, CalendarEventCreateRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "일정 생성 요청은 필수입니다.");
        }
        CalendarEvent event = CalendarEvent.create(
                userId,
                request.title(),
                request.eventDate(),
                request.eventTime(),
                request.category(),
                request.memo());
        return CalendarEventResponse.from(calendarEventRepository.save(event));
    }

    /** 지정한 연월에 속하는 사용자 일정을 날짜·시간·등록 순서대로 조회한다. */
    @Transactional(readOnly = true)
    public List<CalendarEventResponse> getMonthlyEvents(
            Long userId,
            Integer year,
            Integer month,
            CalendarEventCategory category) {
        validateUserId(userId);
        YearMonth yearMonth = toYearMonth(year, month);
        List<CalendarEvent> events = category == null
                ? calendarEventRepository
                        .findAllByUserIdAndEventDateBetweenOrderByEventDateAscEventTimeAscIdAsc(
                                userId, yearMonth.atDay(1), yearMonth.atEndOfMonth())
                : calendarEventRepository
                        .findAllByUserIdAndCategoryAndEventDateBetweenOrderByEventDateAscEventTimeAscIdAsc(
                                userId,
                                category,
                                yearMonth.atDay(1),
                                yearMonth.atEndOfMonth());
        return events
                .stream()
                .map(CalendarEventResponse::from)
                .toList();
    }

    /** 오늘부터의 다가오는 일정을 날짜·시간·등록 순서대로 조회한다. */
    @Transactional(readOnly = true)
    public List<CalendarEventResponse> getUpcomingEvents(
            Long userId,
            CalendarEventCategory category,
            Integer limit) {
        validateUserId(userId);
        validateUpcomingLimit(limit);
        LocalDate today = LocalDate.now(clock.withZone(WEDDING_ZONE));
        PageRequest page = PageRequest.of(0, limit);
        List<CalendarEvent> events = category == null
                ? calendarEventRepository
                        .findAllByUserIdAndEventDateGreaterThanEqualOrderByEventDateAscEventTimeAscIdAsc(
                                userId, today, page)
                : calendarEventRepository
                        .findAllByUserIdAndCategoryAndEventDateGreaterThanEqualOrderByEventDateAscEventTimeAscIdAsc(
                                userId, category, today, page);
        return events.stream().map(CalendarEventResponse::from).toList();
    }

    /** 소유한 일정의 폼 값을 교체한다. */
    @Transactional
    public CalendarEventResponse update(
            Long userId,
            Long eventId,
            CalendarEventUpdateRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "일정 수정 요청은 필수입니다.");
        }
        CalendarEvent event = findOwnedEvent(userId, eventId);
        event.update(
                request.title(),
                request.eventDate(),
                request.eventTime(),
                request.category(),
                request.memo());
        return CalendarEventResponse.from(event);
    }

    /** 소유한 일정을 삭제한다. */
    @Transactional
    public void delete(Long userId, Long eventId) {
        calendarEventRepository.delete(findOwnedEvent(userId, eventId));
    }

    private CalendarEvent findOwnedEvent(Long userId, Long eventId) {
        validateUserId(userId);
        if (eventId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "일정 식별자는 필수입니다.");
        }
        return calendarEventRepository.findByIdAndUserId(eventId, userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.PLANNER_CALENDAR_EVENT_NOT_FOUND));
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "사용자 식별자는 필수입니다.");
        }
    }

    private void validateUpcomingLimit(Integer limit) {
        if (limit == null || limit < 1 || limit > MAX_UPCOMING_LIMIT) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "조회 개수는 1개 이상 50개 이하여야 합니다.");
        }
    }

    private YearMonth toYearMonth(Integer year, Integer month) {
        if (year == null || month == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "조회 연도와 월은 필수입니다.");
        }
        if (year < CalendarEvent.MIN_SUPPORTED_YEAR
                || year > CalendarEvent.MAX_SUPPORTED_YEAR
                || month < 1
                || month > 12) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "조회 연도 또는 월이 올바르지 않습니다.");
        }
        return YearMonth.of(year, month);
    }
}
