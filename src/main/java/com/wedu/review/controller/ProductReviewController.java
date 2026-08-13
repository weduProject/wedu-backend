package com.wedu.review.controller;

import com.wedu.global.response.ApiResponse;
import com.wedu.review.dto.ProductReviewCreateRequest;
import com.wedu.review.dto.ProductReviewPageResponse;
import com.wedu.review.dto.ProductReviewResponse;
import com.wedu.review.dto.ProductReviewUpdateRequest;
import com.wedu.review.service.ProductReviewService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 사용자의 상품 리뷰 쓰기 요청을 처리한다.
 *
 * <p>상품 조회 경로(`/api/products/**`)는 비로그인에게 열려 있으므로, 쓰기는 인증이 필요한
 * `/api/reviews` 아래에 둔다.
 */
@Hidden
@Tag(name = "Product Review", description = "상품 리뷰 작성·수정·삭제·내 리뷰")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ProductReviewController {

    private final ProductReviewService productReviewService;

    /** 상품에 리뷰를 남긴다. */
    @Operation(summary = "상품 리뷰 작성")
    @PostMapping
    public ApiResponse<ProductReviewResponse> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ProductReviewCreateRequest request) {
        return ApiResponse.ok(productReviewService.create(userId, request));
    }

    /** 내가 쓴 리뷰 목록을 조회한다. */
    @Operation(summary = "내 리뷰 목록 조회")
    @GetMapping("/me")
    public ApiResponse<ProductReviewPageResponse> getMyReviews(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "0부터 시작하는 페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "페이지 크기(1~50)", example = "20")
            @RequestParam(defaultValue = "20") Integer size) {
        return ApiResponse.ok(productReviewService.getMyReviews(userId, page, size));
    }

    /** 작성자가 리뷰를 수정한다. */
    @Operation(summary = "상품 리뷰 수정")
    @PatchMapping("/{reviewId}")
    public ApiResponse<ProductReviewResponse> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long reviewId,
            @Valid @RequestBody ProductReviewUpdateRequest request) {
        return ApiResponse.ok(productReviewService.update(userId, reviewId, request));
    }

    /** 작성자가 리뷰를 삭제한다. */
    @Operation(summary = "상품 리뷰 삭제")
    @DeleteMapping("/{reviewId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal Long userId, @PathVariable Long reviewId) {
        productReviewService.delete(userId, reviewId);
        return ApiResponse.ok();
    }
}
