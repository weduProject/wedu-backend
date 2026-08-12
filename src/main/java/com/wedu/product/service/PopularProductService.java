package com.wedu.product.service;

import com.wedu.product.domain.PopularProduct;
import com.wedu.product.domain.Product;
import com.wedu.product.domain.ProductCategory;
import com.wedu.product.dto.PopularProductResponse;
import com.wedu.product.repository.PopularProductRepository;
import com.wedu.product.repository.ProductRepository;
import com.wedu.review.dto.ProductRatingSummary;
import com.wedu.review.service.ProductRatingService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 인기 상품 조회 유스케이스를 처리한다. */
@Service
public class PopularProductService {

    private final PopularProductRepository popularProductRepository;
    private final ProductRepository productRepository;
    private final ProductRatingService productRatingService;
    private final String publicBaseUrl;

    public PopularProductService(
            PopularProductRepository popularProductRepository,
            ProductRepository productRepository,
            ProductRatingService productRatingService,
            @Value("${wedu.public-base-url:http://localhost:8080}") String publicBaseUrl) {
        this.popularProductRepository = popularProductRepository;
        this.productRepository = productRepository;
        this.productRatingService = productRatingService;
        this.publicBaseUrl = publicBaseUrl;
    }

    /** 순위 오름차순으로 인기 상품 목록을 조회한다. */
    @Transactional(readOnly = true)
    public List<PopularProductResponse> getPopularProducts() {
        List<PopularProduct> rankings = popularProductRepository.findTop20ByOrderByRankAsc();
        List<Long> productIds = productIdsOf(rankings);
        Map<Long, ProductCategory> categories = categoriesOf(productIds);
        Map<Long, ProductRatingSummary> ratings = productRatingService.summariesOf(productIds);
        return rankings.stream()
                .map(ranking -> PopularProductResponse.from(
                        ranking,
                        publicBaseUrl,
                        categories.get(ranking.getProductId()),
                        ratings.get(ranking.getProductId())))
                .toList();
    }

    private List<Long> productIdsOf(List<PopularProduct> rankings) {
        return rankings.stream()
                .map(PopularProduct::getProductId)
                .filter(Objects::nonNull)
                .toList();
    }

    private Map<Long, ProductCategory> categoriesOf(List<Long> productIds) {
        Map<Long, ProductCategory> categories = new HashMap<>();
        for (Product product : productRepository.findAllById(productIds)) {
            categories.put(product.getId(), product.getCategory());
        }
        return categories;
    }
}
