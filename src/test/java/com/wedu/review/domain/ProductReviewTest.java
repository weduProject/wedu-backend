package com.wedu.review.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductReviewTest {

    @Test
    @DisplayName("상품에 평점과 후기를 남긴다")
    void create() {
        ProductReview review = ProductReview.create(1L, 7L, 5, " 정말 좋았어요 ");

        assertThat(review.getProductId()).isEqualTo(1L);
        assertThat(review.getUserId()).isEqualTo(7L);
        assertThat(review.getRating()).isEqualTo(5);
        assertThat(review.getContent()).isEqualTo("정말 좋았어요");
    }

    @Test
    @DisplayName("후기 없이 평점만 남길 수 있다")
    void createWithoutContent() {
        assertThat(ProductReview.create(1L, 7L, 4, null).getContent()).isNull();
        assertThat(ProductReview.create(1L, 7L, 4, "   ").getContent()).isNull();
    }

    @Test
    @DisplayName("평점이 1점 미만이거나 5점을 넘으면 남길 수 없다")
    void rejectInvalidRating() {
        assertThatThrownBy(() -> ProductReview.create(1L, 7L, 0, null))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_INVALID_RATING));
        assertThatThrownBy(() -> ProductReview.create(1L, 7L, 6, null))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_INVALID_RATING));
    }

    @Test
    @DisplayName("후기가 1000자를 넘으면 남길 수 없다")
    void rejectTooLongContent() {
        String tooLong = "가".repeat(ProductReview.MAX_CONTENT_LENGTH + 1);

        assertThatThrownBy(() -> ProductReview.create(1L, 7L, 5, tooLong))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_INVALID_CONTENT));
    }

    @Test
    @DisplayName("상품 또는 사용자 식별자가 없으면 남길 수 없다")
    void rejectMissingIds() {
        assertThatThrownBy(() -> ProductReview.create(null, 7L, 5, null))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> ProductReview.create(1L, null, 5, null))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("작성자가 평점과 후기를 고쳐 쓴다")
    void update() {
        ProductReview review = ProductReview.create(1L, 7L, 5, "좋았어요");

        review.update(3, "다시 보니 아쉬워요");

        assertThat(review.getRating()).isEqualTo(3);
        assertThat(review.getContent()).isEqualTo("다시 보니 아쉬워요");
    }

    @Test
    @DisplayName("작성자만 자기 리뷰로 인정된다")
    void isWrittenBy() {
        ProductReview review = ProductReview.create(1L, 7L, 5, null);

        assertThat(review.isWrittenBy(7L)).isTrue();
        assertThat(review.isWrittenBy(8L)).isFalse();
    }
}
