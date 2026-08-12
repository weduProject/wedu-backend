package com.wedu.review.repository;

import com.wedu.review.domain.ProductReview;
import com.wedu.review.dto.ProductRatingSummary;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    Page<ProductReview> findByProductIdOrderByCreatedAtDescIdDesc(Long productId, Pageable pageable);

    Page<ProductReview> findByUserIdOrderByCreatedAtDescIdDesc(Long userId, Pageable pageable);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    @Query("""
            SELECT new com.wedu.review.dto.ProductRatingSummary(
                review.productId, AVG(review.rating), COUNT(review))
            FROM ProductReview review
            WHERE review.productId IN :productIds
            GROUP BY review.productId
            """)
    List<ProductRatingSummary> summarizeByProductIds(@Param("productIds") Collection<Long> productIds);
}
