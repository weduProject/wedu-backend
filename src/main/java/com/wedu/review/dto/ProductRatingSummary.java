package com.wedu.review.dto;

/**
 * 상품 한 건의 평점 집계. 상품 조회 응답의 별점 표기에 쓴다.
 *
 * <p>{@code averageRating} 은 집계 원값이므로 화면에 그대로 쓰지 말고 {@link #rating()} 을 쓴다.
 */
public record ProductRatingSummary(Long productId, Double averageRating, long reviewCount) {

    /** 리뷰가 아직 없는 상품의 집계. */
    public static ProductRatingSummary none(Long productId) {
        return new ProductRatingSummary(productId, null, 0);
    }

    /** 화면 표기용 평점(소수 첫째 자리). 리뷰가 없으면 null. */
    public Double rating() {
        if (averageRating == null) {
            return null;
        }
        return Math.round(averageRating * 10) / 10.0;
    }
}
