package com.wedu.planner.service;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.planner.domain.CalendarEvent;
import com.wedu.planner.dto.CalendarEventResponse;
import com.wedu.planner.repository.CalendarEventRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 캘린더 일정 생성과 월별 조회 유스케이스를 처리한다. */
@Service
@RequiredArgsConstructor
public class CalendarEventService {

    private final CalendarEventRepository calendarEventRepository;

    /** 사용자에게 날짜 단위 일정을 추가한다. */
    @Transactional
    public CalendarEventResponse create(Long userId, String title, LocalDate eventDate) {
        CalendarEvent event = CalendarEvent.create(userId, title, eventDate);
        return CalendarEventResponse.from(calendarEventRepository.save(event));
    }

    /** 지정한 연월에 속하는 사용자 일정을 날짜와 등록 순서대로 조회한다. */
    @Transactional(readOnly = true)
    public List<CalendarEventResponse> getMonthlyEvents(Long userId, Integer year, Integer month) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "사용자 식별자는 필수입니다.");
        }
        YearMonth yearMonth = toYearMonth(year, month);
        return calendarEventRepository
                .findAllByUserIdAndEventDateBetweenOrderByEventDateAscIdAsc(
                        userId, yearMonth.atDay(1), yearMonth.atEndOfMonth())
                .stream()
                .map(CalendarEventResponse::from)
                .toList();
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
