package com.wedu.planner.dto;

import com.wedu.planner.domain.BudgetCategory;
import com.wedu.planner.domain.BudgetItem;
import java.math.BigDecimal;

/** 예산 항목의 원 단위 예정·집행 금액과 완료 상태 응답. */
public record BudgetItemResponse(
        Long itemId,
        String title,
        BudgetCategory category,
        BigDecimal plannedAmount,
        BigDecimal spentAmount,
        boolean completed) {

    public static BudgetItemResponse from(BudgetItem item) {
        return new BudgetItemResponse(
                item.getId(),
                item.getTitle(),
                item.getCategory(),
                item.getPlannedAmount(),
                item.getSpentAmount(),
                item.isCompleted());
    }
}
