package com.wedu.planner.domain;

import com.wedu.global.common.BaseTimeEntity;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사용자의 기념일과 준비 일정을 날짜 단위로 관리한다. */
@Getter
@Entity
@Table(
        name = "calendar_events",
        indexes = @Index(
                name = "idx_calendar_events_user_date_id",
                columnList = "user_id,event_date,id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CalendarEvent extends BaseTimeEntity {

    public static final int MAX_TITLE_LENGTH = 100;
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

    private CalendarEvent(Long userId, String title, LocalDate eventDate) {
        this.userId = userId;
        this.title = title;
        this.eventDate = eventDate;
    }

    /** 필수 값과 제목 길이를 검증한 뒤 일정을 생성한다. */
    public static CalendarEvent create(Long userId, String title, LocalDate eventDate) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "사용자 식별자는 필수입니다.");
        }
        if (title == null || title.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "일정 제목은 필수입니다.");
        }
        String normalizedTitle = title.trim();
        if (normalizedTitle.length() > MAX_TITLE_LENGTH) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "일정 제목은 " + MAX_TITLE_LENGTH + "자 이하여야 합니다.");
        }
        if (eventDate == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "일정 날짜는 필수입니다.");
        }
        if (eventDate.getYear() < MIN_SUPPORTED_YEAR
                || eventDate.getYear() > MAX_SUPPORTED_YEAR) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "지원하지 않는 일정 연도입니다.");
        }
        return new CalendarEvent(userId, normalizedTitle, eventDate);
    }
}
