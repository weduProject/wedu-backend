package com.wedu.product.dto;

import com.wedu.product.domain.Product;
import com.wedu.product.domain.ProductCategory;

/** 상품 목록 화면에 노출되는 요약 정보. */
public record ProductSummaryResponse(
        Long id,
        String name,
        ProductCategory category,
        int price,
        String vendorName,
        String thumbnailUrl) {

    public static ProductSummaryResponse from(Product product, String publicBaseUrl) {
        return new ProductSummaryResponse(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getVendorName(),
                ProductThumbnailUrls.toPublicUrl(product.getThumbnailUrl(), publicBaseUrl));
    }
}
