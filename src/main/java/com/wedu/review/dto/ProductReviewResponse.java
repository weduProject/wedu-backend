package com.wedu.review.dto;

import com.wedu.review.domain.ProductReview;
import java.time.LocalDateTime;

/** 리뷰 한 건의 응답. {@code mine} 은 조회한 사용자가 쓴 리뷰인지 알려준다. */
public record ProductReviewResponse(
        Long id,
        Long productId,
        Long userId,
        int rating,
        String content,
        boolean mine,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static ProductReviewResponse from(ProductReview review, Long viewerId) {
        return new ProductReviewResponse(
                review.getId(),
                review.getProductId(),
                review.getUserId(),
                review.getRating(),
                review.getContent(),
                viewerId != null && review.isWrittenBy(viewerId),
                review.getCreatedAt(),
                review.getUpdatedAt());
    }
}
