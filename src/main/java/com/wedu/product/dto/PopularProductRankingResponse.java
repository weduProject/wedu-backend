package com.wedu.product.dto;

/** 인기 상품 순위 갱신 결과. */
public record PopularProductRankingResponse(int rankedCount) {

    public static PopularProductRankingResponse of(int rankedCount) {
        return new PopularProductRankingResponse(rankedCount);
    }
}
