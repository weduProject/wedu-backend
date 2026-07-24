package com.wedu.planner.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.planner.domain.BudgetCategory;
import com.wedu.planner.domain.BudgetItem;
import com.wedu.planner.dto.BudgetCompletionRequest;
import com.wedu.planner.dto.BudgetItemCreateRequest;
import com.wedu.planner.dto.BudgetItemResponse;
import com.wedu.planner.dto.BudgetItemUpdateRequest;
import com.wedu.planner.dto.BudgetOverviewResponse;
import com.wedu.planner.repository.BudgetItemRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetItemRepository budgetItemRepository;

    private BudgetService budgetService;

    @BeforeEach
    void setUp() {
        budgetService = new BudgetService(budgetItemRepository);
    }

    @Test
    @DisplayName("집행 금액을 생략하면 0원으로 예산 항목을 생성한다")
    void createWithDefaultSpentAmount() {
        when(budgetItemRepository.save(any(BudgetItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        BudgetItemCreateRequest request = new BudgetItemCreateRequest(
                "예식장 계약금",
                BudgetCategory.VENUE,
                amount("2000000"),
                null);

        BudgetItemResponse response = budgetService.create(1L, request);

        assertThat(response.spentAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("사용자의 항목만 등록 순서로 조회해 전체 현황을 만든다")
    void getOverview() {
        when(budgetItemRepository.findAllByUserIdOrderByIdAsc(1L))
                .thenReturn(List.of(item()));

        BudgetOverviewResponse response = budgetService.getOverview(1L);

        assertThat(response.summary().totalCount()).isEqualTo(1);
        assertThat(response.categories().getFirst().items()).hasSize(1);
    }

    @Test
    @DisplayName("소유한 항목의 정보와 완료 여부를 변경한다")
    void updateAndComplete() {
        BudgetItem item = item();
        when(budgetItemRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(item));

        BudgetItemResponse updated = budgetService.update(
                1L,
                10L,
                new BudgetItemUpdateRequest(
                        "예식장 잔금",
                        BudgetCategory.VENUE,
                        amount("3000000"),
                        amount("3500000")));
        BudgetItemResponse completed = budgetService.changeCompletion(
                1L, 10L, new BudgetCompletionRequest(true));

        assertThat(updated.spentAmount()).isEqualByComparingTo("3500000");
        assertThat(completed.completed()).isTrue();
    }

    @Test
    @DisplayName("소유한 항목을 삭제한다")
    void delete() {
        BudgetItem item = item();
        when(budgetItemRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(item));

        budgetService.delete(1L, 10L);

        verify(budgetItemRepository).delete(item);
    }

    @Test
    @DisplayName("다른 사용자의 항목은 수정하거나 삭제할 수 없다")
    void rejectUnownedItem() {
        when(budgetItemRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.update(
                        1L,
                        10L,
                        new BudgetItemUpdateRequest(
                                "항목",
                                BudgetCategory.OTHER,
                                BigDecimal.ZERO,
                                BigDecimal.ZERO)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.PLANNER_BUDGET_ITEM_NOT_FOUND));
        assertThatThrownBy(() -> budgetService.delete(1L, 10L))
                .isInstanceOf(BusinessException.class);
    }

    private BudgetItem item() {
        return BudgetItem.create(
                1L,
                "예식장 계약금",
                BudgetCategory.VENUE,
                amount("2000000"),
                BigDecimal.ZERO);
    }

    private BigDecimal amount(String value) {
        return new BigDecimal(value);
    }
}
