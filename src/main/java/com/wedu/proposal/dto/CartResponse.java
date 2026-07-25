package com.wedu.proposal.dto;

import com.wedu.proposal.domain.Cart;
import java.util.List;

/** 장바구니(견적함) 목록과 총 견적 금액 응답. */
public record CartResponse(Long id, List<CartItemResponse> items, int totalPrice) {

    public static CartResponse from(Cart cart) {
        List<CartItemResponse> items = cart.getItems().entrySet().stream()
                .map(entry -> CartItemResponse.of(entry.getKey(), entry.getValue()))
                .toList();
        return new CartResponse(cart.getId(), items, cart.totalPrice());
    }
}
