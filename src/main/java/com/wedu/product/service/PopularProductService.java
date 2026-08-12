package com.wedu.product.service;

import com.wedu.product.domain.PopularProduct;
import com.wedu.product.domain.Product;
import com.wedu.product.domain.ProductCategory;
import com.wedu.product.dto.PopularProductResponse;
import com.wedu.product.repository.PopularProductRepository;
import com.wedu.product.repository.ProductRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 인기 상품 조회 유스케이스를 처리한다. */
@Service
@RequiredArgsConstructor
public class PopularProductService {

    private final PopularProductRepository popularProductRepository;
    private final ProductRepository productRepository;

    /** 순위 오름차순으로 인기 상품 목록을 조회한다. */
    @Transactional(readOnly = true)
    public List<PopularProductResponse> getPopularProducts() {
        List<PopularProduct> rankings = popularProductRepository.findTop20ByOrderByRankAsc();
        Map<Long, ProductCategory> categories = categoriesOf(rankings);
        return rankings.stream()
                .map(ranking -> PopularProductResponse.from(ranking, categories.get(ranking.getProductId())))
                .toList();
    }

    private Map<Long, ProductCategory> categoriesOf(List<PopularProduct> rankings) {
        List<Long> productIds = rankings.stream()
                .map(PopularProduct::getProductId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, ProductCategory> categories = new HashMap<>();
        for (Product product : productRepository.findAllById(productIds)) {
            categories.put(product.getId(), product.getCategory());
        }
        return categories;
    }
}
