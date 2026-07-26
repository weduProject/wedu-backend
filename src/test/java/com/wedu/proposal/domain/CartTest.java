package com.wedu.proposal.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CartTest {

    @Test
    @DisplayName("상품을 장바구니에 담는다")
    void addItem() {
        Cart cart = Cart.create(1L);

        cart.addItem(10L, "커플링", 150_000, 2);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.totalPrice()).isEqualTo(300_000);
    }

    @Test
    @DisplayName("이미 담긴 상품을 다시 담으면 수량이 더해진다")
    void addSameItemAccumulatesQuantity() {
        Cart cart = Cart.create(1L);
        cart.addItem(10L, "커플링", 150_000, 1);

        cart.addItem(10L, "커플링", 150_000, 2);

        assertThat(cart.getItems().get(10L).getQuantity()).isEqualTo(3);
        assertThat(cart.totalPrice()).isEqualTo(450_000);
    }

    @Test
    @DisplayName("담긴 상품의 수량을 변경한다")
    void changeQuantity() {
        Cart cart = Cart.create(1L);
        cart.addItem(10L, "커플링", 150_000, 1);

        cart.changeQuantity(10L, 5);

        assertThat(cart.getItems().get(10L).getQuantity()).isEqualTo(5);
        assertThat(cart.totalPrice()).isEqualTo(750_000);
    }

    @Test
    @DisplayName("담기지 않은 상품의 수량은 변경할 수 없다")
    void rejectChangeQuantityOfMissingItem() {
        Cart cart = Cart.create(1L);

        assertThatThrownBy(() -> cart.changeQuantity(10L, 2))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND));
    }

    @Test
    @DisplayName("담긴 상품을 제거한다")
    void removeItem() {
        Cart cart = Cart.create(1L);
        cart.addItem(10L, "커플링", 150_000, 1);

        cart.removeItem(10L);

        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    @DisplayName("수량을 0 이하로 변경할 수 없다")
    void rejectNonPositiveQuantity() {
        Cart cart = Cart.create(1L);
        cart.addItem(10L, "커플링", 150_000, 1);

        assertThatThrownBy(() -> cart.changeQuantity(10L, 0)).isInstanceOf(BusinessException.class);
    }
}
