package com.wedu.proposal.dto;

import com.wedu.proposal.domain.CartItem;

/** 장바구니에 담긴 상품 한 종류에 대한 응답. */
public record CartItemResponse(Long productId, String name, int price, int quantity, int subtotal) {

    public static CartItemResponse of(Long productId, CartItem item) {
        return new CartItemResponse(
                productId, item.getName(), item.getPrice(), item.getQuantity(),
                item.getPrice() * item.getQuantity());
    }
}
