package com.wedu.recommendation.presentation.dto;

import java.util.List;

public record AiRecommendationResponse(
        List<RecommendedProduct> recommendations
) {

    public record RecommendedProduct(
            Long productId,
            String reason
    ) {
    }
}