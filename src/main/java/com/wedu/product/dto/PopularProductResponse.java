package com.wedu.product.dto;

import com.wedu.product.domain.PopularProduct;
import com.wedu.product.domain.ProductCategory;

/**
 * 인기 상품 영역에 노출되는 요약 정보.
 *
 * <p>{@code id} 는 순위 한 줄의 식별자다. 상세 페이지 이동·찜·담기처럼 상품을 가리켜야 하는
 * 동작에는 {@code productId} 를 쓴다. {@code productId}/{@code category} 는 외부 출처에서
 * 수집한 순위에는 없어 null 일 수 있다.
 */
public record PopularProductResponse(
        Long id,
        Long productId,
        String name,
        int price,
        String sourceName,
        String thumbnailUrl,
        int rank,
        ProductCategory category) {

    public static PopularProductResponse from(PopularProduct popularProduct, ProductCategory category) {
        return new PopularProductResponse(
                popularProduct.getId(),
                popularProduct.getProductId(),
                popularProduct.getName(),
                popularProduct.getPrice(),
                popularProduct.getSourceName(),
                popularProduct.getThumbnailUrl(),
                popularProduct.getRank(),
                category);
    }
}
