package com.wedu.proposal.dto;

import jakarta.validation.constraints.Min;

/** 장바구니 상품 수량 변경 요청. */
public record CartItemQuantityRequest(@Min(1) int quantity) {
}
