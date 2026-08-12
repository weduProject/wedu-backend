package com.wedu.review.domain;

import com.wedu.global.common.BaseTimeEntity;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자가 상품에 남긴 평점과 후기를 담는 Aggregate Root.
 *
 * <p>평점은 필수, 후기 본문은 선택이다. 상품당 한 사용자가 한 건만 남길 수 있으며 그 제약은
 * DB 유니크 키가 최종 보증한다.
 */
@Getter
@Entity
@Table(
        name = "product_reviews",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_product_reviews_user_product",
                columnNames = {"user_id", "product_id"}),
        indexes = {
            @Index(
                    name = "idx_product_reviews_product_created_id",
                    columnList = "product_id,created_at,id"),
            @Index(name = "idx_product_reviews_user_created_id", columnList = "user_id,created_at,id")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductReview extends BaseTimeEntity {

    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;
    public static final int MAX_CONTENT_LENGTH = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private int rating;

    @Column(length = MAX_CONTENT_LENGTH)
    private String content;

    private ProductReview(Long productId, Long userId, int rating, String content) {
        this.productId = validateId(productId, "상품 식별자");
        this.userId = validateId(userId, "사용자 식별자");
        this.rating = validateRating(rating);
        this.content = normalizeContent(content);
    }

    /** 상품에 평점과 후기를 남긴다. */
    public static ProductReview create(Long productId, Long userId, int rating, String content) {
        return new ProductReview(productId, userId, rating, content);
    }

    /** 작성자가 평점과 후기를 고쳐 쓴다. */
    public void update(int rating, String content) {
        this.rating = validateRating(rating);
        this.content = normalizeContent(content);
    }

    /** 이 리뷰를 쓴 사용자인지 확인한다. */
    public boolean isWrittenBy(Long userId) {
        return this.userId.equals(userId);
    }

    private static Long validateId(Long id, String name) {
        if (id == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, name + "는 필수입니다.");
        }
        return id;
    }

    private static int validateRating(int rating) {
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new BusinessException(ErrorCode.REVIEW_INVALID_RATING);
        }
        return rating;
    }

    /** 공백만 남은 본문은 후기를 쓰지 않은 것으로 본다. */
    private static String normalizeContent(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        String trimmed = content.strip();
        if (trimmed.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException(ErrorCode.REVIEW_INVALID_CONTENT);
        }
        return trimmed;
    }
}
