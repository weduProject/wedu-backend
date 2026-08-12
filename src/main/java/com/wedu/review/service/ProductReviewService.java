package com.wedu.review.service;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.product.service.ProductService;
import com.wedu.review.domain.ProductReview;
import com.wedu.review.dto.ProductRatingSummary;
import com.wedu.review.dto.ProductReviewCreateRequest;
import com.wedu.review.dto.ProductReviewPageResponse;
import com.wedu.review.dto.ProductReviewResponse;
import com.wedu.review.dto.ProductReviewUpdateRequest;
import com.wedu.review.repository.ProductReviewRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 상품 리뷰 작성·수정·삭제·조회 유스케이스를 처리한다. */
@Service
@RequiredArgsConstructor
public class ProductReviewService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    private final ProductReviewRepository productReviewRepository;
    private final ProductRatingService productRatingService;
    private final ProductService productService;

    /** 상품에 리뷰를 남긴다. 한 사용자는 상품당 한 건만 쓸 수 있다. */
    @Transactional
    public ProductReviewResponse create(Long userId, ProductReviewCreateRequest request) {
        validateUserId(userId);
        Long productId = request.productId();
        if (!productService.exists(productId)) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        if (productReviewRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }
        ProductReview review =
                ProductReview.create(productId, userId, request.rating(), request.content());
        productReviewRepository.save(review);
        return ProductReviewResponse.from(review, userId);
    }

    /** 상품의 리뷰를 최신순으로 페이징 조회한다. 비로그인도 볼 수 있다. */
    @Transactional(readOnly = true)
    public ProductReviewPageResponse getProductReviews(
            Long viewerId, Long productId, Integer page, Integer size) {
        if (!productService.exists(productId)) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        Page<ProductReview> reviews =
                productReviewRepository.findByProductIdOrderByCreatedAtDescIdDesc(
                        productId, PageRequest.of(normalizePage(page), normalizeSize(size)));
        ProductRatingSummary summary = productRatingService.summaryOf(productId);
        return ProductReviewPageResponse.from(reviews, toResponses(reviews, viewerId), summary);
    }

    /** 내가 쓴 리뷰를 최신순으로 페이징 조회한다. */
    @Transactional(readOnly = true)
    public ProductReviewPageResponse getMyReviews(Long userId, Integer page, Integer size) {
        validateUserId(userId);
        Page<ProductReview> reviews =
                productReviewRepository.findByUserIdOrderByCreatedAtDescIdDesc(
                        userId, PageRequest.of(normalizePage(page), normalizeSize(size)));
        return ProductReviewPageResponse.from(reviews, toResponses(reviews, userId), null);
    }

    /** 작성자가 자기 리뷰의 평점과 후기를 수정한다. */
    @Transactional
    public ProductReviewResponse update(
            Long userId, Long reviewId, ProductReviewUpdateRequest request) {
        validateUserId(userId);
        ProductReview review = findOwnedReview(userId, reviewId);
        review.update(request.rating(), request.content());
        return ProductReviewResponse.from(review, userId);
    }

    /** 작성자가 자기 리뷰를 삭제한다. */
    @Transactional
    public void delete(Long userId, Long reviewId) {
        validateUserId(userId);
        productReviewRepository.delete(findOwnedReview(userId, reviewId));
    }

    private ProductReview findOwnedReview(Long userId, Long reviewId) {
        ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
        if (!review.isWrittenBy(userId)) {
            throw new BusinessException(ErrorCode.REVIEW_FORBIDDEN);
        }
        return review;
    }

    private List<ProductReviewResponse> toResponses(Page<ProductReview> reviews, Long viewerId) {
        return reviews.getContent().stream()
                .map(review -> ProductReviewResponse.from(review, viewerId))
                .toList();
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    private int normalizePage(Integer page) {
        return page == null || page < 0 ? 0 : page;
    }

    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
