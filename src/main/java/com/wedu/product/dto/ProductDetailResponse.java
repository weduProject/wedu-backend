package com.wedu.product.dto;

import com.wedu.product.domain.Product;
import com.wedu.product.domain.ProductCategory;

/** 상품 상세 화면에 노출되는 상세 정보. */
public record ProductDetailResponse(
        Long id,
        String name,
        ProductCategory category,
        int price,
        String vendorName,
        String thumbnailUrl,
        String description) {

    public static ProductDetailResponse from(Product product) {
        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getVendorName(),
                product.getThumbnailUrl(),
                product.getDescription());
    }
}
