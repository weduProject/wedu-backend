package com.wedu.review.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.wedu.product.domain.Product;
import com.wedu.product.domain.ProductCategory;
import com.wedu.product.dto.ProductDetailResponse;
import com.wedu.product.dto.ProductSummaryResponse;
import com.wedu.product.repository.ProductRepository;
import com.wedu.product.service.ProductService;
import com.wedu.review.dto.ProductReviewCreateRequest;
import com.wedu.review.dto.ProductReviewPageResponse;
import com.wedu.review.dto.ProductReviewUpdateRequest;
import com.wedu.review.repository.ProductReviewRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

@SpringBootTest
class ProductReviewIntegrationTest {

    @Autowired private ProductReviewService productReviewService;
    @Autowired private ProductRatingService productRatingService;
    @Autowired private ProductService productService;
    @Autowired private ProductReviewRepository productReviewRepository;
    @Autowired private ProductRepository productRepository;

    private Long productId;

    @BeforeEach
    void setUp() {
        productReviewRepository.deleteAll();
        productRepository.deleteAll();
        productId = productRepository.save(
                        Product.create("커플링", ProductCategory.RING, 100_000, "링업체", "/products/1.jpg", "설명"))
                .getId();
    }

    @Test
    @DisplayName("평점 평균은 소수 첫째 자리로 집계된다")
    void summarizeAverageRating() {
        productReviewService.create(11L, new ProductReviewCreateRequest(productId, 5, "좋아요"));
        productReviewService.create(12L, new ProductReviewCreateRequest(productId, 4, null));
        productReviewService.create(13L, new ProductReviewCreateRequest(productId, 4, "괜찮아요"));

        assertThat(productRatingService.summaryOf(productId).rating()).isEqualTo(4.3);
        assertThat(productRatingService.summaryOf(productId).reviewCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("리뷰가 없는 상품은 평점이 null 이고 리뷰 수가 0 이다")
    void summarizeWithoutReviews() {
        assertThat(productRatingService.summaryOf(productId).rating()).isNull();
        assertThat(productRatingService.summaryOf(productId).reviewCount()).isZero();

        ProductDetailResponse detail = productService.getDetail(productId);
        assertThat(detail.rating()).isNull();
        assertThat(detail.reviewCount()).isZero();
    }

    @Test
    @DisplayName("상품 목록과 상세에 평점과 리뷰 수가 함께 내려간다")
    void exposeRatingOnProductResponses() {
        productReviewService.create(11L, new ProductReviewCreateRequest(productId, 5, "좋아요"));
        productReviewService.create(12L, new ProductReviewCreateRequest(productId, 4, null));

        List<ProductSummaryResponse> products =
                productService.search(null, null, null, null, PageRequest.of(0, 20));
        assertThat(products).hasSize(1);
        assertThat(products.get(0).rating()).isEqualTo(4.5);
        assertThat(products.get(0).reviewCount()).isEqualTo(2);

        ProductDetailResponse detail = productService.getDetail(productId);
        assertThat(detail.rating()).isEqualTo(4.5);
        assertThat(detail.reviewCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("상품 리뷰 목록은 최신순이고 평점 집계를 함께 담는다")
    void getProductReviews() {
        productReviewService.create(11L, new ProductReviewCreateRequest(productId, 5, "먼저 쓴 리뷰"));
        productReviewService.create(12L, new ProductReviewCreateRequest(productId, 3, "나중 쓴 리뷰"));

        ProductReviewPageResponse page =
                productReviewService.getProductReviews(11L, productId, 0, 20);

        assertThat(page.reviews()).hasSize(2);
        assertThat(page.reviews().get(0).content()).isEqualTo("나중 쓴 리뷰");
        assertThat(page.reviews().get(0).mine()).isFalse();
        assertThat(page.reviews().get(1).mine()).isTrue();
        assertThat(page.rating()).isEqualTo(4.0);
        assertThat(page.reviewCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("수정하면 평점 집계도 함께 바뀐다")
    void updateChangesSummary() {
        Long reviewId = productReviewService
                .create(11L, new ProductReviewCreateRequest(productId, 5, "좋아요"))
                .id();

        productReviewService.update(11L, reviewId, new ProductReviewUpdateRequest(1, "아쉬워요"));

        assertThat(productRatingService.summaryOf(productId).rating()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("삭제하면 집계에서 빠진다")
    void deleteChangesSummary() {
        Long reviewId = productReviewService
                .create(11L, new ProductReviewCreateRequest(productId, 5, null))
                .id();

        productReviewService.delete(11L, reviewId);

        assertThat(productRatingService.summaryOf(productId).reviewCount()).isZero();
    }

    @Test
    @DisplayName("내 리뷰 목록에는 내가 쓴 것만 담긴다")
    void getMyReviews() {
        productReviewService.create(11L, new ProductReviewCreateRequest(productId, 5, "내 리뷰"));
        productReviewService.create(12L, new ProductReviewCreateRequest(productId, 2, "남의 리뷰"));

        ProductReviewPageResponse page = productReviewService.getMyReviews(11L, 0, 20);

        assertThat(page.reviews()).hasSize(1);
        assertThat(page.reviews().get(0).content()).isEqualTo("내 리뷰");
        assertThat(page.reviews().get(0).mine()).isTrue();
    }
}
