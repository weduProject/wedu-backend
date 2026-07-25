package com.wedu.proposal.controller;

import com.wedu.global.response.ApiResponse;
import com.wedu.proposal.dto.CartItemAddRequest;
import com.wedu.proposal.dto.CartItemQuantityRequest;
import com.wedu.proposal.dto.CartResponse;
import com.wedu.proposal.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 인증 사용자의 장바구니(견적함) HTTP 요청을 처리한다. */
@Tag(name = "Cart", description = "장바구니 / 견적함")
@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /** 내 장바구니(견적함)를 조회한다. */
    @Operation(summary = "내 장바구니 조회")
    @GetMapping("/me")
    public ApiResponse<CartResponse> getMyCart(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(cartService.getMyCart(userId));
    }

    /** 상품을 장바구니에 담는다. */
    @Operation(summary = "장바구니에 상품 담기")
    @PostMapping("/items")
    public ApiResponse<CartResponse> addItem(
            @AuthenticationPrincipal Long userId, @Valid @RequestBody CartItemAddRequest request) {
        return ApiResponse.ok(cartService.addItem(userId, request));
    }

    /** 담긴 상품의 수량을 변경한다. */
    @Operation(summary = "장바구니 상품 수량 변경")
    @PatchMapping("/items/{productId}")
    public ApiResponse<CartResponse> changeQuantity(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long productId,
            @Valid @RequestBody CartItemQuantityRequest request) {
        return ApiResponse.ok(cartService.changeQuantity(userId, productId, request.quantity()));
    }

    /** 담긴 상품을 장바구니에서 제거한다. */
    @Operation(summary = "장바구니 상품 삭제")
    @DeleteMapping("/items/{productId}")
    public ApiResponse<CartResponse> removeItem(
            @AuthenticationPrincipal Long userId, @PathVariable Long productId) {
        return ApiResponse.ok(cartService.removeItem(userId, productId));
    }
}
