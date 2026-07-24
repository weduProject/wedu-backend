package com.wedu.planner.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wedu.global.error.BusinessException;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BudgetItemTest {

    @Test
    @DisplayName("예산 항목을 원 단위 금액과 미완료 상태로 생성한다")
    void create() {
        BudgetItem item = BudgetItem.create(
                1L,
                "  예식장 계약금  ",
                BudgetCategory.VENUE,
                amount("2000000"),
                null);

        assertThat(item.getTitle()).isEqualTo("예식장 계약금");
        assertThat(item.getPlannedAmount()).isEqualByComparingTo("2000000");
        assertThat(item.getSpentAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(item.isCompleted()).isFalse();
    }

    @Test
    @DisplayName("초과 지출을 허용하고 항목 정보와 완료 상태를 독립적으로 변경한다")
    void updateAndComplete() {
        BudgetItem item = BudgetItem.create(
                1L,
                "스튜디오 촬영",
                BudgetCategory.STUDIO_DRESS,
                amount("1500000"),
                amount("500000"));

        item.update(
                "스튜디오 촬영 변경",
                BudgetCategory.STUDIO_DRESS,
                amount("1500000"),
                amount("1800000"));
        item.changeCompletion(true);

        assertThat(item.getSpentAmount()).isEqualByComparingTo("1800000");
        assertThat(item.isCompleted()).isTrue();
    }

    @Test
    @DisplayName("필수값과 음수·소수·최대 범위 초과 금액을 거부한다")
    void rejectInvalidValues() {
        assertInvalid(() -> BudgetItem.create(
                null, "항목", BudgetCategory.OTHER, BigDecimal.ZERO, BigDecimal.ZERO));
        assertInvalid(() -> BudgetItem.create(
                1L, " ", BudgetCategory.OTHER, BigDecimal.ZERO, BigDecimal.ZERO));
        assertInvalid(() -> BudgetItem.create(
                1L, "가".repeat(101), BudgetCategory.OTHER, BigDecimal.ZERO, BigDecimal.ZERO));
        assertInvalid(() -> BudgetItem.create(
                1L, "항목", null, BigDecimal.ZERO, BigDecimal.ZERO));
        assertInvalid(() -> BudgetItem.create(
                1L, "항목", BudgetCategory.OTHER, null, BigDecimal.ZERO));
        assertInvalid(() -> BudgetItem.create(
                1L, "항목", BudgetCategory.OTHER, amount("-1"), BigDecimal.ZERO));
        assertInvalid(() -> BudgetItem.create(
                1L, "항목", BudgetCategory.OTHER, amount("1.5"), BigDecimal.ZERO));
        assertInvalid(() -> BudgetItem.create(
                1L,
                "항목",
                BudgetCategory.OTHER,
                amount("1000000000000000000"),
                BigDecimal.ZERO));
    }

    private BigDecimal amount(String value) {
        return new BigDecimal(value);
    }

    private void assertInvalid(org.assertj.core.api.ThrowableAssert.ThrowingCallable callable) {
        assertThatThrownBy(callable).isInstanceOf(BusinessException.class);
    }
}
