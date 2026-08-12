package com.wedu.proposal.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProposalTest {

    @Test
    @DisplayName("사용자의 나만의 프로포즈를 시작한다")
    void start() {
        Proposal proposal = Proposal.start(1L);

        assertThat(proposal.getUserId()).isEqualTo(1L);
        assertThat(proposal.getItems()).isEmpty();
        assertThat(proposal.totalPrice()).isZero();
    }

    @Test
    @DisplayName("사용자 식별자 없이는 시작할 수 없다")
    void rejectStartWithoutUserId() {
        assertThatThrownBy(() -> Proposal.start(null)).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("카테고리별로 옵션을 선택하면 견적 합계에 반영된다")
    void selectOption() {
        Proposal proposal = Proposal.start(1L);

        proposal.selectOption(ProposalItemCategory.RING, 10L, "커플링", 150_000);
        proposal.selectOption(ProposalItemCategory.PHOTO, 20L, "스냅 촬영", 300_000);

        assertThat(proposal.getItems()).hasSize(2);
        assertThat(proposal.totalPrice()).isEqualTo(450_000);
    }

    @Test
    @DisplayName("카테고리 없이는 옵션을 선택할 수 없다")
    void rejectSelectOptionWithoutCategory() {
        Proposal proposal = Proposal.start(1L);

        assertThatThrownBy(() -> proposal.selectOption(null, 10L, "커플링", 150_000))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("상품 식별자 없이는 옵션을 선택할 수 없다")
    void rejectSelectOptionWithoutProductId() {
        Proposal proposal = Proposal.start(1L);

        assertThatThrownBy(() -> proposal.selectOption(ProposalItemCategory.RING, null, "커플링", 150_000))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("0 이하 상품 식별자로는 옵션을 선택할 수 없다")
    void rejectSelectOptionWithNonPositiveProductId() {
        Proposal proposal = Proposal.start(1L);

        assertThatThrownBy(() -> proposal.selectOption(ProposalItemCategory.RING, 0L, "커플링", 150_000))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("빈 이름으로는 옵션을 선택할 수 없다")
    void rejectSelectOptionWithBlankName() {
        Proposal proposal = Proposal.start(1L);

        assertThatThrownBy(() -> proposal.selectOption(ProposalItemCategory.RING, 10L, " ", 150_000))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("음수 가격으로는 옵션을 선택할 수 없다")
    void rejectSelectOptionWithNegativePrice() {
        Proposal proposal = Proposal.start(1L);

        assertThatThrownBy(() -> proposal.selectOption(ProposalItemCategory.RING, 10L, "커플링", -1))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("같은 카테고리를 다시 선택하면 기존 선택을 교체한다")
    void replaceOptionInSameCategory() {
        Proposal proposal = Proposal.start(1L);
        proposal.selectOption(ProposalItemCategory.RING, 10L, "커플링", 150_000);

        proposal.selectOption(ProposalItemCategory.RING, 11L, "다이아 반지", 500_000);

        assertThat(proposal.getItems()).hasSize(1);
        assertThat(proposal.totalPrice()).isEqualTo(500_000);
    }

    @Test
    @DisplayName("선택한 옵션을 취소한다")
    void removeOption() {
        Proposal proposal = Proposal.start(1L);
        proposal.selectOption(ProposalItemCategory.RING, 10L, "커플링", 150_000);

        proposal.removeOption(ProposalItemCategory.RING);

        assertThat(proposal.getItems()).isEmpty();
    }

    @Test
    @DisplayName("카테고리 없이는 옵션을 취소할 수 없다")
    void rejectRemoveOptionWithoutCategory() {
        Proposal proposal = Proposal.start(1L);

        assertThatThrownBy(() -> proposal.removeOption(null)).isInstanceOfSatisfying(
                BusinessException.class,
                exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT));
    }

    @Test
    @DisplayName("선택하지 않은 카테고리는 취소할 수 없다")
    void rejectRemovingUnselectedCategory() {
        Proposal proposal = Proposal.start(1L);

        assertThatThrownBy(() -> proposal.removeOption(ProposalItemCategory.RING))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.PROPOSAL_OPTION_NOT_SELECTED));
    }
}
