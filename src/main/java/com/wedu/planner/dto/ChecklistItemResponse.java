package com.wedu.planner.dto;

import com.wedu.planner.domain.ChecklistCategory;
import com.wedu.planner.domain.ChecklistItem;

/** 체크리스트 항목 응답. */
public record ChecklistItemResponse(
        Long itemId,
        String title,
        ChecklistCategory category,
        boolean completed) {

    public static ChecklistItemResponse from(ChecklistItem item) {
        return new ChecklistItemResponse(
                item.getId(), item.getTitle(), item.getCategory(), item.isCompleted());
    }
}
