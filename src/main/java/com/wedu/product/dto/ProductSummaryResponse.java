package com.wedu.product.dto;

import com.wedu.product.domain.Product;
import com.wedu.product.domain.ProductCategory;
import com.wedu.review.dto.ProductRatingSummary;

/** 상품 목록 화면에 노출되는 요약 정보. 리뷰가 없는 상품은 평점이 null, 리뷰 수가 0 이다. */
public record ProductSummaryResponse(
        Long id,
        String name,
        ProductCategory category,
        int price,
        String vendorName,
        String thumbnailUrl,
        Double rating,
        long reviewCount) {

    public static ProductSummaryResponse from(
            Product product, String publicBaseUrl, ProductRatingSummary rating) {
        return new ProductSummaryResponse(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getVendorName(),
                ProductThumbnailUrls.toPublicUrl(product.getThumbnailUrl(), publicBaseUrl),
                rating == null ? null : rating.rating(),
                rating == null ? 0 : rating.reviewCount());
    }
}
