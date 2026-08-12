package com.wedu.planner.dto;

import com.wedu.planner.domain.BudgetCategory;
import com.wedu.planner.domain.BudgetItem;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/** 전체 예산 요약과 5개 고정 카테고리의 상세 현황 응답. */
public record BudgetOverviewResponse(
        BigDecimal totalBudget,
        boolean totalBudgetConfigured,
        BudgetSummaryResponse summary,
        List<BudgetCategoryResponse> categories) {

    /** 전체 항목을 집계하고 빈 카테고리까지 Figma 고정 순서로 구성한다. */
    public static BudgetOverviewResponse from(List<BudgetItem> items) {
        return from(items, null);
    }

    /** 저장된 전체 목표 예산과 항목 집계를 함께 구성한다. */
    public static BudgetOverviewResponse from(List<BudgetItem> items, BigDecimal totalBudget) {
        boolean totalBudgetConfigured = totalBudget != null;
        BigDecimal normalizedTotalBudget = totalBudgetConfigured ? totalBudget : BigDecimal.ZERO;
        List<BudgetCategoryResponse> categories = Arrays.stream(BudgetCategory.values())
                .map(category -> BudgetCategoryResponse.from(
                        category,
                        items.stream()
                                .filter(item -> item.getCategory() == category)
                                .toList()))
                .toList();
        return new BudgetOverviewResponse(
                normalizedTotalBudget,
                totalBudgetConfigured,
                totalBudgetConfigured
                        ? BudgetSummaryResponse.from(items, normalizedTotalBudget)
                        : BudgetSummaryResponse.from(items),
                categories);
    }
}
