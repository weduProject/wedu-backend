package com.wedu.planner.dto;

import java.util.List;

/** 전체 진행률과 선택한 카테고리의 체크리스트 항목 응답. */
public record ChecklistOverviewResponse(
        long totalCount,
        long completedCount,
        long remainingCount,
        int progressPercentage,
        List<ChecklistItemResponse> items) {

    public static ChecklistOverviewResponse of(
            long totalCount,
            long completedCount,
            List<ChecklistItemResponse> items) {
        long remainingCount = totalCount - completedCount;
        int progressPercentage = totalCount == 0
                ? 0
                : (int) Math.round(completedCount * 100.0 / totalCount);
        return new ChecklistOverviewResponse(
                totalCount,
                completedCount,
                remainingCount,
                progressPercentage,
                List.copyOf(items));
    }
}
