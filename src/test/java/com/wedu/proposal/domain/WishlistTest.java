package com.wedu.proposal.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WishlistTest {

    @Test
    @DisplayName("상품을 찜 목록에 추가한다")
    void add() {
        Wishlist wishlist = Wishlist.create(1L);

        wishlist.add(10L);

        assertThat(wishlist.getProductIds()).containsExactly(10L);
    }

    @Test
    @DisplayName("이미 찜한 상품은 다시 찜할 수 없다")
    void rejectDuplicateAdd() {
        Wishlist wishlist = Wishlist.create(1L);
        wishlist.add(10L);

        assertThatThrownBy(() -> wishlist.add(10L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.WISHLIST_ITEM_ALREADY_EXISTS));
    }

    @Test
    @DisplayName("찜한 상품을 제거한다")
    void remove() {
        Wishlist wishlist = Wishlist.create(1L);
        wishlist.add(10L);

        wishlist.remove(10L);

        assertThat(wishlist.getProductIds()).isEmpty();
    }

    @Test
    @DisplayName("찜하지 않은 상품은 제거할 수 없다")
    void rejectRemovingUnknownItem() {
        Wishlist wishlist = Wishlist.create(1L);

        assertThatThrownBy(() -> wishlist.remove(10L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.WISHLIST_ITEM_NOT_FOUND));
    }
}
