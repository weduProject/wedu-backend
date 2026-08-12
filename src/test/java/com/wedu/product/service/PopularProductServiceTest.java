package com.wedu.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.wedu.product.domain.PopularProduct;
import com.wedu.product.domain.Product;
import com.wedu.product.domain.ProductCategory;
import com.wedu.product.dto.PopularProductResponse;
import com.wedu.product.repository.PopularProductRepository;
import com.wedu.product.repository.ProductRepository;
import com.wedu.review.service.ProductRatingService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PopularProductServiceTest {

    @Mock
    private PopularProductRepository popularProductRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductRatingService productRatingService;

    private PopularProductService popularProductService;

    @BeforeEach
    void setUp() {
        popularProductService = new PopularProductService(popularProductRepository, productRepository, productRatingService);
    }

    @Test
    @DisplayName("순위 순으로 인기 상품 목록을 조회한다")
    void getPopularProducts() {
        PopularProduct first = PopularProduct.rank(1L, "1위 상품", 100_000, "출처", "/api/products/1", null, 1);
        PopularProduct second = PopularProduct.rank(2L, "2위 상품", 200_000, "출처", "/api/products/2", null, 2);
        when(popularProductRepository.findTop20ByOrderByRankAsc()).thenReturn(List.of(first, second));
        when(productRepository.findAllById(any()))
                .thenReturn(List.of(product(1L, ProductCategory.RING), product(2L, ProductCategory.FLOWER)));

        List<PopularProductResponse> result = popularProductService.getPopularProducts();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).rank()).isEqualTo(1);
        assertThat(result.get(0).productId()).isEqualTo(1L);
        assertThat(result.get(0).name()).isEqualTo("1위 상품");
        assertThat(result.get(0).price()).isEqualTo(100_000);
        assertThat(result.get(0).sourceName()).isEqualTo("출처");
        assertThat(result.get(0).thumbnailUrl()).isNull();
        assertThat(result.get(0).category()).isEqualTo(ProductCategory.RING);
        assertThat(result.get(1).rank()).isEqualTo(2);
        assertThat(result.get(1).category()).isEqualTo(ProductCategory.FLOWER);
    }

    @Test
    @DisplayName("대응하는 상품이 없는 순위는 상품 정보 없이 내려준다")
    void getPopularProductsCollectedFromExternalSource() {
        PopularProduct collected =
                PopularProduct.collect("외부 수집 상품", 300_000, "외부몰", "https://source/1", null, 1);
        when(popularProductRepository.findTop20ByOrderByRankAsc()).thenReturn(List.of(collected));
        when(productRepository.findAllById(any())).thenReturn(List.of());

        List<PopularProductResponse> result = popularProductService.getPopularProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).productId()).isNull();
        assertThat(result.get(0).category()).isNull();
        assertThat(result.get(0).name()).isEqualTo("외부 수집 상품");
    }

    private Product product(Long id, ProductCategory category) {
        Product product = Product.create("상품", category, 100_000, "업체", null, null);
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
