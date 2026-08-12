package com.wedu.review.controller;

import com.wedu.global.response.ApiResponse;
import com.wedu.review.dto.ProductReviewPageResponse;
import com.wedu.review.service.ProductReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 상품 상세 화면의 후기 목록 조회를 처리한다. 비로그인도 볼 수 있다. */
@Tag(name = "Product Review", description = "상품 리뷰 작성·수정·삭제·내 리뷰")
@RestController
@RequestMapping("/api/products/{productId}/reviews")
@RequiredArgsConstructor
public class ProductReviewQueryController {

    private final ProductReviewService productReviewService;

    /** 상품의 리뷰를 최신순으로 조회한다. 응답에 평점 집계를 함께 담는다. */
    @Operation(summary = "상품 리뷰 목록 조회")
    @GetMapping
    public ApiResponse<ProductReviewPageResponse> getProductReviews(
            @AuthenticationPrincipal Long viewerId,
            @PathVariable Long productId,
            @Parameter(description = "0부터 시작하는 페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "페이지 크기(1~50)", example = "20")
            @RequestParam(defaultValue = "20") Integer size) {
        return ApiResponse.ok(
                productReviewService.getProductReviews(viewerId, productId, page, size));
    }
}
