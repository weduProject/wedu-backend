package com.wedu.planner.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** 사용자가 설정할 전체 목표 예산 요청. */
public record BudgetTargetRequest(
        @NotNull(message = "전체 목표 예산은 필수입니다.")
        @DecimalMin(value = "0", message = "전체 목표 예산은 0원 이상이어야 합니다.")
        @Digits(integer = 18, fraction = 0, message = "전체 목표 예산은 원 단위 정수여야 합니다.")
        BigDecimal totalBudget) {
}
