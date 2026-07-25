package com.wedu.proposal.domain;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Cart 애그리게이트 내부에서만 존재하는, 담긴 상품 한 종류의 값 객체. */
@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem {

    @Column(name = "item_name", nullable = false)
    private String name;

    @Column(name = "item_price", nullable = false)
    private int price;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    private CartItem(String name, int price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    static CartItem of(String name, int price, int quantity) {
        validate(name, price, quantity);
        return new CartItem(name, price, quantity);
    }

    CartItem withQuantity(int newQuantity) {
        validate(name, price, newQuantity);
        return new CartItem(name, price, newQuantity);
    }

    CartItem plusQuantity(int amount) {
        return withQuantity(this.quantity + amount);
    }

    int subtotal() {
        return price * quantity;
    }

    private static void validate(String name, int price, int quantity) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "상품명은 필수입니다.");
        }
        if (price < 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "상품 가격은 0 이상이어야 합니다.");
        }
        if (quantity < 1) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "수량은 1 이상이어야 합니다.");
        }
    }
}
