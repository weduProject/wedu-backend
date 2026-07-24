package com.wedu.planner.dto;

import com.wedu.planner.domain.BudgetItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** 전체 또는 카테고리별 예산 집계 응답. */
public record BudgetSummaryResponse(
        BigDecimal plannedAmount,
        BigDecimal spentAmount,
        BigDecimal balance,
        long completedCount,
        long totalCount,
        BigDecimal executionRatePercentage) {

    /** 항목들의 합계, 잔액, 완료 건수와 반올림한 집행률을 계산한다. */
    public static BudgetSummaryResponse from(List<BudgetItem> items) {
        BigDecimal plannedAmount = items.stream()
                .map(BudgetItem::getPlannedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal spentAmount = items.stream()
                .map(BudgetItem::getSpentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long completedCount = items.stream().filter(BudgetItem::isCompleted).count();
        BigDecimal executionRatePercentage = plannedAmount.signum() == 0
                ? BigDecimal.ZERO
                : spentAmount
                        .multiply(BigDecimal.valueOf(100))
                        .divide(plannedAmount, 0, RoundingMode.HALF_UP);
        return new BudgetSummaryResponse(
                plannedAmount,
                spentAmount,
                plannedAmount.subtract(spentAmount),
                completedCount,
                items.size(),
                executionRatePercentage);
    }
}
