package com.wedu.proposal.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 장바구니에 상품을 담는 요청. */
public record CartItemAddRequest(
        @NotNull Long productId,
        @NotBlank String name,
        @Min(0) int price,
        @Min(1) int quantity) {
}
