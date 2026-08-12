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
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사용자가 설정한 웨딩 전체 목표 예산을 관리한다. */
@Getter
@Entity
@Table(
        name = "budgets",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_budgets_user_id",
                columnNames = "user_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Budget extends BaseTimeEntity {

    public static final int MONEY_PRECISION = 18;
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("999999999999999999");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "total_budget", nullable = false, precision = MONEY_PRECISION, scale = 0)
    private BigDecimal totalBudget;

    private Budget(Long userId, BigDecimal totalBudget) {
        this.userId = validateUserId(userId);
        this.totalBudget = normalizeAmount(totalBudget);
    }

    public static Budget create(Long userId, BigDecimal totalBudget) {
        return new Budget(userId, totalBudget);
    }

    /** 전체 목표 예산을 원 단위 금액으로 변경한다. */
    public void updateTotalBudget(BigDecimal totalBudget) {
        this.totalBudget = normalizeAmount(totalBudget);
    }

    private static Long validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "사용자 식별자는 양수여야 합니다.");
        }
        return userId;
    }

    private static BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null || amount.signum() < 0 || amount.stripTrailingZeros().scale() > 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "전체 목표 예산은 0 이상의 원 단위 정수여야 합니다.");
        }
        BigDecimal normalized = amount.setScale(0);
        if (normalized.compareTo(MAX_AMOUNT) > 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "전체 목표 예산이 허용 범위를 초과했습니다.");
        }
        return normalized;
    }
}
