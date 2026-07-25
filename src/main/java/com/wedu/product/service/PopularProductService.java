package com.wedu.product.service;

import com.wedu.product.dto.PopularProductResponse;
import com.wedu.product.repository.PopularProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 크롤링으로 수집된 인기 상품 조회 유스케이스를 처리한다. */
@Service
@RequiredArgsConstructor
public class PopularProductService {

    private final PopularProductRepository popularProductRepository;

    /** 순위 오름차순으로 인기 상품 목록을 조회한다. */
    @Transactional(readOnly = true)
    public List<PopularProductResponse> getPopularProducts() {
        return popularProductRepository.findTop20ByOrderByRankAsc().stream()
                .map(PopularProductResponse::from)
                .toList();
    }
}
