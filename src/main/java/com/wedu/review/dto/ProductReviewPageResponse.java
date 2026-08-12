package com.wedu.review.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/** 리뷰 목록 페이지 응답. 상품별 목록에는 평점 집계를 함께 담는다. */
public record ProductReviewPageResponse(
        List<ProductReviewResponse> reviews,
        Double rating,
        long reviewCount,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext) {

    public static ProductReviewPageResponse from(
            Page<?> page, List<ProductReviewResponse> reviews, ProductRatingSummary summary) {
        return new ProductReviewPageResponse(
                reviews,
                summary == null ? null : summary.rating(),
                summary == null ? page.getTotalElements() : summary.reviewCount(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext());
    }
}
