package com.wedu.planner.dto;

import jakarta.validation.constraints.NotNull;

/** 예산 항목의 결제 완료 상태 변경 요청. */
public record BudgetCompletionRequest(
        @NotNull(message = "완료 상태는 필수입니다.") Boolean completed) {
}
