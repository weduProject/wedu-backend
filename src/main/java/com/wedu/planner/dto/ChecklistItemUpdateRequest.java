package com.wedu.planner.dto;

import com.wedu.planner.domain.ChecklistCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 체크리스트 항목 정보 수정 요청. */
public record ChecklistItemUpdateRequest(
        @NotBlank String title,
        @NotNull ChecklistCategory category) {
}
