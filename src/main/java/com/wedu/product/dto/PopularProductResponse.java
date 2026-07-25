package com.wedu.product.dto;

import com.wedu.product.domain.PopularProduct;

/** 인기 상품 영역에 노출되는 요약 정보. */
public record PopularProductResponse(
        Long id, String name, int price, String sourceName, String thumbnailUrl, int rank) {

    public static PopularProductResponse from(PopularProduct popularProduct) {
        return new PopularProductResponse(
                popularProduct.getId(),
                popularProduct.getName(),
                popularProduct.getPrice(),
                popularProduct.getSourceName(),
                popularProduct.getThumbnailUrl(),
                popularProduct.getRank());
    }
}
