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
        return from(items, null);
    }

    /** 전체 현황은 저장된 목표 예산을 기준으로 잔액과 집행률을 계산한다. */
    public static BudgetSummaryResponse from(List<BudgetItem> items, BigDecimal totalBudget) {
        BigDecimal plannedAmount = sumPlannedAmount(items);
        BigDecimal executionBase = totalBudget == null ? plannedAmount : totalBudget;
        BigDecimal spentAmount = items.stream()
                .map(BudgetItem::getSpentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long completedCount = items.stream().filter(BudgetItem::isCompleted).count();
        BigDecimal executionRatePercentage = executionBase.signum() == 0
                ? BigDecimal.ZERO
                : spentAmount
                        .multiply(BigDecimal.valueOf(100))
                        .divide(executionBase, 0, RoundingMode.HALF_UP);
        return new BudgetSummaryResponse(
                plannedAmount,
                spentAmount,
                executionBase.subtract(spentAmount),
                completedCount,
                items.size(),
                executionRatePercentage);
    }

    private static BigDecimal sumPlannedAmount(List<BudgetItem> items) {
        return items.stream()
                .map(BudgetItem::getPlannedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
