package com.wedu.review.service;

import com.wedu.review.dto.ProductRatingSummary;
import com.wedu.review.repository.ProductReviewRepository;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상품별 평점 집계를 다른 도메인에 제공한다.
 *
 * <p>상품 목록·상세·인기 목록이 모두 별점을 표기하므로, 각 화면이 리뷰 테이블을 직접 조회하지
 * 않도록 이 경계에서 한 번에 집계해 넘긴다.
 */
@Service
@RequiredArgsConstructor
public class ProductRatingService {

    private final ProductReviewRepository productReviewRepository;

    /** 여러 상품의 평점을 한 번에 집계한다. 리뷰가 없는 상품은 결과에 없다. */
    @Transactional(readOnly = true)
    public Map<Long, ProductRatingSummary> summariesOf(Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, ProductRatingSummary> summaries = new HashMap<>();
        for (ProductRatingSummary summary : productReviewRepository.summarizeByProductIds(productIds)) {
            summaries.put(summary.productId(), summary);
        }
        return summaries;
    }

    /** 상품 하나의 평점을 집계한다. 리뷰가 없으면 빈 집계를 돌려준다. */
    @Transactional(readOnly = true)
    public ProductRatingSummary summaryOf(Long productId) {
        return summariesOf(List.of(productId))
                .getOrDefault(productId, ProductRatingSummary.none(productId));
    }
}
