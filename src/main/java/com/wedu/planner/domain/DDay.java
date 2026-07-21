package com.wedu.planner.domain;

import com.wedu.global.common.BaseTimeEntity;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사용자의 결혼식 날짜를 관리하는 D-day Aggregate Root. */
@Getter
@Entity
@Table(
        name = "ddays",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ddays_user_id",
                columnNames = "user_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DDay extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "wedding_date", nullable = false)
    private LocalDate weddingDate;

    private DDay(Long userId, LocalDate weddingDate) {
        this.userId = userId;
        this.weddingDate = weddingDate;
    }

    public static DDay create(Long userId, LocalDate weddingDate, LocalDate today) {
        validateUserId(userId);
        validateWeddingDate(weddingDate, today);
        return new DDay(userId, weddingDate);
    }

    public void changeWeddingDate(LocalDate weddingDate, LocalDate today) {
        validateWeddingDate(weddingDate, today);
        this.weddingDate = weddingDate;
    }

    public long daysRemaining(LocalDate today) {
        if (today == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "기준 날짜는 필수입니다.");
        }
        return Math.max(0, ChronoUnit.DAYS.between(today, weddingDate));
    }

    private static void validateUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "사용자 식별자는 필수입니다.");
        }
    }

    private static void validateWeddingDate(LocalDate weddingDate, LocalDate today) {
        if (weddingDate == null || today == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "결혼식 날짜는 필수입니다.");
        }
        if (weddingDate.isBefore(today)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "결혼식 날짜는 과거일 수 없습니다.");
        }
    }
}
