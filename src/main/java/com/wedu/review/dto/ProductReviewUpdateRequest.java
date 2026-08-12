package com.wedu.review.dto;

import com.wedu.review.domain.ProductReview;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 상품 리뷰 수정 요청. */
public record ProductReviewUpdateRequest(
        @NotNull @Min(ProductReview.MIN_RATING) @Max(ProductReview.MAX_RATING) Integer rating,
        @Size(max = ProductReview.MAX_CONTENT_LENGTH) String content) {}
