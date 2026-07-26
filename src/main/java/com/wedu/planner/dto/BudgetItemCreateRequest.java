package com.wedu.planner.dto;

import com.wedu.planner.domain.BudgetCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** 예산 항목 생성 요청. 집행 금액을 생략하면 0원으로 생성한다. */
public record BudgetItemCreateRequest(
        @NotBlank(message = "예산 항목명은 필수입니다.")
        String title,
        @NotNull(message = "예산 카테고리는 필수입니다.")
        BudgetCategory category,
        @NotNull(message = "전체 예산은 필수입니다.")
        @DecimalMin(value = "0", message = "전체 예산은 0원 이상이어야 합니다.")
        @Digits(integer = 18, fraction = 0, message = "전체 예산은 원 단위 정수여야 합니다.")
        BigDecimal plannedAmount,
        @DecimalMin(value = "0", message = "집행 금액은 0원 이상이어야 합니다.")
        @Digits(integer = 18, fraction = 0, message = "집행 금액은 원 단위 정수여야 합니다.")
        BigDecimal spentAmount) {
}
