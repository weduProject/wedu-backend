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
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 웨딩 준비 항목별 예정 금액, 집행 금액과 결제 완료 상태를 관리한다. */
@Getter
@Entity
@Table(
        name = "budget_items",
        indexes = {
            @Index(name = "idx_budget_items_user_id", columnList = "user_id,id"),
            @Index(
                    name = "idx_budget_items_user_category_id",
                    columnList = "user_id,category,id"),
            @Index(
                    name = "idx_budget_items_user_completed",
                    columnList = "user_id,completed")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BudgetItem extends BaseTimeEntity {

    public static final int MAX_TITLE_LENGTH = 100;
    public static final int MONEY_PRECISION = 18;
    private static final BigDecimal MAX_AMOUNT =
            new BigDecimal("999999999999999999");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = MAX_TITLE_LENGTH)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BudgetCategory category;

    @Column(name = "planned_amount", nullable = false, precision = MONEY_PRECISION, scale = 0)
    private BigDecimal plannedAmount;

    @Column(name = "spent_amount", nullable = false, precision = MONEY_PRECISION, scale = 0)
    private BigDecimal spentAmount;

    @Column(nullable = false)
    private boolean completed;

    private BudgetItem(
            Long userId,
            String title,
            BudgetCategory category,
            BigDecimal plannedAmount,
            BigDecimal spentAmount,
            boolean completed) {
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.plannedAmount = plannedAmount;
        this.spentAmount = spentAmount;
        this.completed = completed;
    }

    /** 필수값과 원 단위 금액을 검증해 미완료 예산 항목을 생성한다. */
    public static BudgetItem create(
            Long userId,
            String title,
            BudgetCategory category,
            BigDecimal plannedAmount,
            BigDecimal spentAmount) {
        validateUserId(userId);
        String normalizedTitle = normalizeTitle(title);
        validateCategory(category);
        BigDecimal normalizedPlannedAmount = normalizeAmount(
                plannedAmount, "전체 예산");
        BigDecimal normalizedSpentAmount = spentAmount == null
                ? BigDecimal.ZERO
                : normalizeAmount(spentAmount, "집행 금액");
        return new BudgetItem(
                userId,
                normalizedTitle,
                category,
                normalizedPlannedAmount,
                normalizedSpentAmount,
                false);
    }

    /** 항목명, 카테고리와 금액을 검증한 값으로 함께 변경한다. */
    public void update(
            String title,
            BudgetCategory category,
            BigDecimal plannedAmount,
            BigDecimal spentAmount) {
        String normalizedTitle = normalizeTitle(title);
        validateCategory(category);
        BigDecimal normalizedPlannedAmount = normalizeAmount(
                plannedAmount, "전체 예산");
        BigDecimal normalizedSpentAmount = normalizeAmount(
                spentAmount, "집행 금액");
        this.title = normalizedTitle;
        this.category = category;
        this.plannedAmount = normalizedPlannedAmount;
        this.spentAmount = normalizedSpentAmount;
    }

    /** 재시도해도 같은 결과가 되도록 결제 완료 상태를 명시한 값으로 변경한다. */
    public void changeCompletion(boolean completed) {
        this.completed = completed;
    }

    private static void validateUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "사용자 식별자는 필수입니다.");
        }
    }

    private static String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "예산 항목명은 필수입니다.");
        }
        String normalizedTitle = title.trim();
        if (normalizedTitle.codePointCount(0, normalizedTitle.length()) > MAX_TITLE_LENGTH) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "예산 항목명은 " + MAX_TITLE_LENGTH + "자 이하여야 합니다.");
        }
        return normalizedTitle;
    }

    private static void validateCategory(BudgetCategory category) {
        if (category == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "예산 카테고리는 필수입니다.");
        }
    }

    private static BigDecimal normalizeAmount(BigDecimal amount, String fieldName) {
        if (amount == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, fieldName + "은 필수입니다.");
        }
        if (amount.signum() < 0 || amount.stripTrailingZeros().scale() > 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    fieldName + "은 0 이상의 원 단위 정수여야 합니다.");
        }
        BigDecimal normalizedAmount = amount.setScale(0);
        if (normalizedAmount.compareTo(MAX_AMOUNT) > 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    fieldName + "이 허용 범위를 초과했습니다.");
        }
        return normalizedAmount;
    }
}
