package com.wedu.proposal.controller;

import com.wedu.global.response.ApiResponse;
import com.wedu.proposal.dto.WishlistResponse;
import com.wedu.proposal.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 인증 사용자의 찜하기(위시리스트) HTTP 요청을 처리한다. */
@Tag(name = "Wishlist", description = "찜하기")
@RestController
@RequestMapping("/api/wishlists")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    /** 내가 찜한 상품 목록을 조회한다. */
    @Operation(summary = "내 찜 목록 조회")
    @GetMapping("/me")
    public ApiResponse<WishlistResponse> getMyWishlist(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(wishlistService.getMyWishlist(userId));
    }

    /** 상품을 찜 목록에 추가한다. */
    @Operation(summary = "상품 찜하기")
    @PostMapping("/items/{productId}")
    public ApiResponse<WishlistResponse> add(
            @AuthenticationPrincipal Long userId, @PathVariable Long productId) {
        return ApiResponse.ok(wishlistService.add(userId, productId));
    }

    /** 찜한 상품을 목록에서 제거한다. */
    @Operation(summary = "상품 찜 취소")
    @DeleteMapping("/items/{productId}")
    public ApiResponse<WishlistResponse> remove(
            @AuthenticationPrincipal Long userId, @PathVariable Long productId) {
        return ApiResponse.ok(wishlistService.remove(userId, productId));
    }
}
