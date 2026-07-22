package com.wedu.planner.domain;

import com.wedu.global.common.BaseTimeEntity;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사용자의 기념일과 준비 일정을 날짜 단위로 관리한다. */
@Getter
@Entity
@Table(
        name = "calendar_events",
        indexes = @Index(
                name = "idx_calendar_events_user_date_time_id",
                columnList = "user_id,event_date,event_time,id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CalendarEvent extends BaseTimeEntity {

    public static final int MAX_TITLE_LENGTH = 100;
    public static final int MAX_MEMO_LENGTH = 500;
    public static final int MIN_SUPPORTED_YEAR = 1000;
    public static final int MAX_SUPPORTED_YEAR = 9999;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = MAX_TITLE_LENGTH)
    private String title;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "event_time")
    private LocalTime eventTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CalendarEventCategory category;

    @Column(length = MAX_MEMO_LENGTH)
    private String memo;

    private CalendarEvent(
            Long userId,
            String title,
            LocalDate eventDate,
            LocalTime eventTime,
            CalendarEventCategory category,
            String memo) {
        this.userId = userId;
        this.title = title;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.category = category;
        this.memo = memo;
    }

    /** 필수 값과 제목 길이를 검증한 뒤 일정을 생성한다. */
    public static CalendarEvent create(
            Long userId,
            String title,
            LocalDate eventDate,
            LocalTime eventTime,
            CalendarEventCategory category,
            String memo) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "사용자 식별자는 필수입니다.");
        }
        String normalizedTitle = normalizeTitle(title);
        validateEventDate(eventDate);
        validateCategory(category);
        return new CalendarEvent(
                userId,
                normalizedTitle,
                eventDate,
                eventTime,
                category,
                normalizeMemo(memo));
    }

    /** 수정 폼의 값으로 일정 정보를 교체한다. */
    public void update(
            String title,
            LocalDate eventDate,
            LocalTime eventTime,
            CalendarEventCategory category,
            String memo) {
        String normalizedTitle = normalizeTitle(title);
        validateEventDate(eventDate);
        validateCategory(category);
        String normalizedMemo = normalizeMemo(memo);
        this.title = normalizedTitle;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.category = category;
        this.memo = normalizedMemo;
    }

    private static String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "일정 제목은 필수입니다.");
        }
        String normalizedTitle = title.trim();
        if (normalizedTitle.length() > MAX_TITLE_LENGTH) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "일정 제목은 " + MAX_TITLE_LENGTH + "자 이하여야 합니다.");
        }
        return normalizedTitle;
    }

    private static void validateEventDate(LocalDate eventDate) {
        if (eventDate == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "일정 날짜는 필수입니다.");
        }
        if (eventDate.getYear() < MIN_SUPPORTED_YEAR
                || eventDate.getYear() > MAX_SUPPORTED_YEAR) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "지원하지 않는 일정 연도입니다.");
        }
    }

    private static void validateCategory(CalendarEventCategory category) {
        if (category == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "일정 카테고리는 필수입니다.");
        }
    }

    private static String normalizeMemo(String memo) {
        if (memo == null || memo.isBlank()) {
            return null;
        }
        String normalizedMemo = memo.trim();
        if (normalizedMemo.length() > MAX_MEMO_LENGTH) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "일정 메모는 " + MAX_MEMO_LENGTH + "자 이하여야 합니다.");
        }
        return normalizedMemo;
    }
}
