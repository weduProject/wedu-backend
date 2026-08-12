package com.wedu.planner.dto;

import com.wedu.planner.domain.Budget;
import java.math.BigDecimal;

/** 저장된 사용자별 전체 목표 예산 응답. */
public record BudgetTargetResponse(BigDecimal totalBudget) {

    public static BudgetTargetResponse from(Budget budget) {
        return new BudgetTargetResponse(budget.getTotalBudget());
    }
}
