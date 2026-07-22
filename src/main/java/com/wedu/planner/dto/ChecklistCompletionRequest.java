package com.wedu.planner.dto;

import jakarta.validation.constraints.NotNull;

/** 체크리스트 완료 상태 변경 요청. */
public record ChecklistCompletionRequest(@NotNull Boolean completed) {
}
