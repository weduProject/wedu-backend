package com.wedu.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.product.service.ProductService;
import com.wedu.review.domain.ProductReview;
import com.wedu.review.dto.ProductReviewCreateRequest;
import com.wedu.review.dto.ProductReviewResponse;
import com.wedu.review.dto.ProductReviewUpdateRequest;
import com.wedu.review.repository.ProductReviewRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductReviewServiceTest {

    @Mock
    private ProductReviewRepository productReviewRepository;

    @Mock
    private ProductRatingService productRatingService;

    @Mock
    private ProductService productService;

    private ProductReviewService productReviewService;

    @BeforeEach
    void setUp() {
        productReviewService = new ProductReviewService(
                productReviewRepository, productRatingService, productService);
    }

    @Test
    @DisplayName("상품에 리뷰를 남긴다")
    void create() {
        when(productService.exists(1L)).thenReturn(true);
        when(productReviewRepository.existsByUserIdAndProductId(7L, 1L)).thenReturn(false);

        ProductReviewResponse response = productReviewService.create(
                7L, new ProductReviewCreateRequest(1L, 5, "좋았어요"));

        assertThat(response.productId()).isEqualTo(1L);
        assertThat(response.rating()).isEqualTo(5);
        assertThat(response.content()).isEqualTo("좋았어요");
        assertThat(response.mine()).isTrue();
        verify(productReviewRepository).save(any(ProductReview.class));
    }

    @Test
    @DisplayName("없는 상품에는 리뷰를 남길 수 없다")
    void rejectCreateForMissingProduct() {
        when(productService.exists(99L)).thenReturn(false);

        assertThatThrownBy(() -> productReviewService.create(
                7L, new ProductReviewCreateRequest(99L, 5, null)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND));
        verify(productReviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("같은 상품에 두 번째 리뷰는 남길 수 없다")
    void rejectDuplicateReview() {
        when(productService.exists(1L)).thenReturn(true);
        when(productReviewRepository.existsByUserIdAndProductId(7L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> productReviewService.create(
                7L, new ProductReviewCreateRequest(1L, 5, null)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_ALREADY_EXISTS));
        verify(productReviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("비로그인 상태로는 리뷰를 남길 수 없다")
    void rejectCreateWithoutLogin() {
        assertThatThrownBy(() -> productReviewService.create(
                null, new ProductReviewCreateRequest(1L, 5, null)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED));
    }

    @Test
    @DisplayName("작성자가 자기 리뷰를 수정한다")
    void update() {
        when(productReviewRepository.findById(10L))
                .thenReturn(Optional.of(ProductReview.create(1L, 7L, 5, "좋았어요")));

        ProductReviewResponse response = productReviewService.update(
                7L, 10L, new ProductReviewUpdateRequest(2, "아쉬웠어요"));

        assertThat(response.rating()).isEqualTo(2);
        assertThat(response.content()).isEqualTo("아쉬웠어요");
    }

    @Test
    @DisplayName("남의 리뷰는 수정할 수 없다")
    void rejectUpdateByOthers() {
        when(productReviewRepository.findById(10L))
                .thenReturn(Optional.of(ProductReview.create(1L, 7L, 5, "좋았어요")));

        assertThatThrownBy(() -> productReviewService.update(
                8L, 10L, new ProductReviewUpdateRequest(1, null)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_FORBIDDEN));
    }

    @Test
    @DisplayName("없는 리뷰는 수정할 수 없다")
    void rejectUpdateForMissingReview() {
        when(productReviewRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productReviewService.update(
                7L, 10L, new ProductReviewUpdateRequest(1, null)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_NOT_FOUND));
    }

    @Test
    @DisplayName("작성자가 자기 리뷰를 삭제한다")
    void delete() {
        ProductReview review = ProductReview.create(1L, 7L, 5, null);
        when(productReviewRepository.findById(10L)).thenReturn(Optional.of(review));

        productReviewService.delete(7L, 10L);

        verify(productReviewRepository).delete(review);
    }

    @Test
    @DisplayName("남의 리뷰는 삭제할 수 없다")
    void rejectDeleteByOthers() {
        when(productReviewRepository.findById(10L))
                .thenReturn(Optional.of(ProductReview.create(1L, 7L, 5, null)));

        assertThatThrownBy(() -> productReviewService.delete(8L, 10L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_FORBIDDEN));
        verify(productReviewRepository, never()).delete(any());
    }
}
