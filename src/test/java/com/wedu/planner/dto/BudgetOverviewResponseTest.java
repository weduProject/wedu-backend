package com.wedu.planner.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.wedu.planner.domain.BudgetCategory;
import com.wedu.planner.domain.BudgetItem;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BudgetOverviewResponseTest {

    @Test
    @DisplayName("Figma 기준 전체 및 카테고리별 예산 현황을 집계한다")
    void aggregate() {
        BudgetItem venueOne = completed(item(
                "예식장 계약금", BudgetCategory.VENUE, "20000000", "20000000"));
        BudgetItem venueTwo = completed(item(
                "예식장 잔금", BudgetCategory.VENUE, "20000000", "20000000"));
        BudgetItem studioOne = completed(item(
                "스튜디오 촬영", BudgetCategory.STUDIO_DRESS, "15000000", "5000000"));
        BudgetItem studioTwo = item(
                "드레스 대여", BudgetCategory.STUDIO_DRESS, "20000000", "0");
        BudgetItem studioThree = item(
                "메이크업", BudgetCategory.STUDIO_DRESS, "8000000", "0");
        BudgetItem studioFour = completed(item(
                "스튜디오 촬영 2", BudgetCategory.STUDIO_DRESS, "15000000", "5000000"));
        BudgetItem studioFive = item(
                "드레스 대여 2", BudgetCategory.STUDIO_DRESS, "20000000", "0");
        BudgetItem studioSix = item(
                "메이크업 2", BudgetCategory.STUDIO_DRESS, "8000000", "0");
        BudgetItem honeymoonOne = completed(item(
                "허니문 예약", BudgetCategory.HONEYMOON, "40000000", "10000000"));
        BudgetItem honeymoonTwo = completed(item(
                "허니문 잔금", BudgetCategory.HONEYMOON, "40000000", "10000000"));
        BudgetItem jewelryOne = item(
                "웨딩밴드", BudgetCategory.JEWELRY_GIFTS, "12000000", "0");
        BudgetItem jewelryTwo = item(
                "예단", BudgetCategory.JEWELRY_GIFTS, "12000000", "0");
        BudgetItem otherOne = completed(item(
                "청첩장 제작", BudgetCategory.OTHER, "3000000", "3000000"));
        BudgetItem otherTwo = completed(item(
                "청첩장 우편", BudgetCategory.OTHER, "3000000", "3000000"));

        BudgetOverviewResponse response = BudgetOverviewResponse.from(List.of(
                venueOne,
                venueTwo,
                studioOne,
                studioTwo,
                studioThree,
                studioFour,
                studioFive,
                studioSix,
                honeymoonOne,
                honeymoonTwo,
                jewelryOne,
                jewelryTwo,
                otherOne,
                otherTwo));

        assertThat(response.summary().plannedAmount()).isEqualByComparingTo("236000000");
        assertThat(response.summary().spentAmount()).isEqualByComparingTo("76000000");
        assertThat(response.summary().balance()).isEqualByComparingTo("160000000");
        assertThat(response.summary().completedCount()).isEqualTo(8);
        assertThat(response.summary().totalCount()).isEqualTo(14);
        assertThat(response.summary().executionRatePercentage()).isEqualByComparingTo("32");
        assertThat(response.categories()).extracting(BudgetCategoryResponse::category)
                .containsExactly(BudgetCategory.values());

        BudgetCategoryResponse studio = response.categories().get(1);
        assertThat(studio.summary().plannedAmount()).isEqualByComparingTo("86000000");
        assertThat(studio.summary().spentAmount()).isEqualByComparingTo("10000000");
        assertThat(studio.summary().executionRatePercentage()).isEqualByComparingTo("12");
    }

    @Test
    @DisplayName("항목이 없어도 5개 카테고리와 0원 요약을 반환한다")
    void emptyOverview() {
        BudgetOverviewResponse response = BudgetOverviewResponse.from(List.of());

        assertThat(response.summary().plannedAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.summary().executionRatePercentage()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.categories()).hasSize(5);
        assertThat(response.categories()).allSatisfy(category ->
                assertThat(category.items()).isEmpty());
    }

    @Test
    @DisplayName("초과 지출은 100퍼센트 초과 집행률과 음수 잔액으로 계산한다")
    void overspending() {
        BudgetOverviewResponse response = BudgetOverviewResponse.from(List.of(
                item("초과 지출", BudgetCategory.OTHER, "10000", "15000")));

        assertThat(response.summary().executionRatePercentage()).isEqualByComparingTo("150");
        assertThat(response.summary().balance()).isEqualByComparingTo("-5000");
    }

    private BudgetItem item(
            String title,
            BudgetCategory category,
            String plannedAmount,
            String spentAmount) {
        return BudgetItem.create(
                1L,
                title,
                category,
                new BigDecimal(plannedAmount),
                new BigDecimal(spentAmount));
    }

    private BudgetItem completed(BudgetItem item) {
        item.changeCompletion(true);
        return item;
    }
}
