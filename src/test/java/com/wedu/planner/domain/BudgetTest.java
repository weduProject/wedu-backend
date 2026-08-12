package com.wedu.planner.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wedu.global.error.BusinessException;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BudgetTest {

    @Test
    @DisplayName("전체 목표 예산을 생성하고 변경한다")
    void createAndUpdate() {
        Budget budget = Budget.create(1L, new BigDecimal("30000000"));

        budget.updateTotalBudget(new BigDecimal("35000000"));

        assertThat(budget.getUserId()).isEqualTo(1L);
        assertThat(budget.getTotalBudget()).isEqualByComparingTo("35000000");
    }

    @Test
    @DisplayName("음수, 소수 또는 범위를 초과한 목표 예산을 거부한다")
    void rejectInvalidAmount() {
        assertThatThrownBy(() -> Budget.create(1L, new BigDecimal("-1")))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> Budget.create(1L, new BigDecimal("1.5")))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> Budget.create(1L, new BigDecimal("1000000000000000000")))
                .isInstanceOf(BusinessException.class);
    }
}
