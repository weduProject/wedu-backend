package com.wedu.planner.dto;

import com.wedu.planner.domain.ChecklistCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 체크리스트 항목 생성 요청. */
public record ChecklistItemCreateRequest(
        @NotBlank String title,
        @NotNull ChecklistCategory category) {
}
