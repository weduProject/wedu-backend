package com.wedu.product.dto;

import com.wedu.product.domain.Product;
import com.wedu.product.domain.ProductCategory;
import com.wedu.review.dto.ProductRatingSummary;

/** 상품 상세 화면에 노출되는 상세 정보. 리뷰가 없으면 평점이 null, 리뷰 수가 0 이다. */
public record ProductDetailResponse(
        Long id,
        String name,
        ProductCategory category,
        int price,
        String vendorName,
        String thumbnailUrl,
        String description,
        Double rating,
        long reviewCount) {

    public static ProductDetailResponse from(
            Product product, String publicBaseUrl, ProductRatingSummary rating) {
        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getVendorName(),
                ProductThumbnailUrls.toPublicUrl(product.getThumbnailUrl(), publicBaseUrl),
                product.getDescription(),
                rating == null ? null : rating.rating(),
                rating == null ? 0 : rating.reviewCount());
    }
}
