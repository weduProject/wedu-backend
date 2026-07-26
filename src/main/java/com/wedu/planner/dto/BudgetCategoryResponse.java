package com.wedu.planner.dto;

import com.wedu.planner.domain.BudgetCategory;
import com.wedu.planner.domain.BudgetItem;
import java.util.List;

/** 고정 카테고리 하나의 예산 집계와 등록 순서별 항목 응답. */
public record BudgetCategoryResponse(
        BudgetCategory category,
        BudgetSummaryResponse summary,
        List<BudgetItemResponse> items) {

    public static BudgetCategoryResponse from(
            BudgetCategory category,
            List<BudgetItem> items) {
        return new BudgetCategoryResponse(
                category,
                BudgetSummaryResponse.from(items),
                items.stream().map(BudgetItemResponse::from).toList());
    }
}
