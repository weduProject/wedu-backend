package com.wedu.product.service;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.product.domain.ProductCategory;
import com.wedu.product.dto.ProductSummaryResponse;
import com.wedu.product.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 상품 목록/검색/필터 유스케이스를 처리한다. */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /** 카테고리·키워드·가격 범위로 상품 목록을 조회한다. */
    @Transactional(readOnly = true)
    public List<ProductSummaryResponse> search(
            ProductCategory category,
            String keyword,
            Integer minPrice,
            Integer maxPrice,
            Pageable pageable) {
        validatePriceRange(minPrice, maxPrice);
        String normalizedKeyword = normalizeKeyword(keyword);
        return productRepository.search(category, normalizedKeyword, minPrice, maxPrice, pageable)
                .stream()
                .map(ProductSummaryResponse::from)
                .toList();
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    private void validatePriceRange(Integer minPrice, Integer maxPrice) {
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new BusinessException(ErrorCode.PRODUCT_INVALID_PRICE_RANGE);
        }
    }
}
