package com.wedu.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.wedu.product.domain.PopularProduct;
import com.wedu.product.dto.PopularProductResponse;
import com.wedu.product.repository.PopularProductRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PopularProductServiceTest {

    @Mock
    private PopularProductRepository popularProductRepository;

    private PopularProductService popularProductService;

    @BeforeEach
    void setUp() {
        popularProductService = new PopularProductService(popularProductRepository);
    }

    @Test
    @DisplayName("순위 순으로 인기 상품 목록을 조회한다")
    void getPopularProducts() {
        PopularProduct first = PopularProduct.collect("1위 상품", 100_000, "출처", "http://s/1", null, 1);
        PopularProduct second = PopularProduct.collect("2위 상품", 200_000, "출처", "http://s/2", null, 2);
        when(popularProductRepository.findTop20ByOrderByRankAsc()).thenReturn(List.of(first, second));

        List<PopularProductResponse> result = popularProductService.getPopularProducts();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).rank()).isEqualTo(1);
        assertThat(result.get(1).rank()).isEqualTo(2);
    }
}
