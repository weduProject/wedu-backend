package com.wedu.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wedu.product.domain.PopularProduct;
import com.wedu.product.domain.Product;
import com.wedu.product.domain.ProductCategory;
import com.wedu.product.repository.PopularProductRepository;
import com.wedu.product.repository.ProductRepository;
import com.wedu.proposal.dto.ProductInterestCount;
import com.wedu.proposal.service.ProductInterestService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PopularProductRankingServiceTest {

    @Mock
    private PopularProductRepository popularProductRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductInterestService productInterestService;

    private PopularProductRankingService popularProductRankingService;

    @BeforeEach
    void setUp() {
        popularProductRankingService = new PopularProductRankingService(
                popularProductRepository, productRepository, productInterestService);
    }

    @Test
    @DisplayName("관심이 많은 상품이 앞 순위가 된다")
    void refreshOrdersByInterest() {
        Product ring = product(1L, "커플링", 100_000);
        Product flower = product(2L, "꽃다발", 50_000);
        when(productInterestService.countInterestsByProduct()).thenReturn(List.of(
                new ProductInterestCount(1L, 3), new ProductInterestCount(2L, 7)));
        when(productRepository.findAllById(any())).thenReturn(List.of(ring, flower));
        when(productRepository.findAll(any(Pageable.class))).thenReturn(emptyPage());

        int ranked = popularProductRankingService.refresh();

        assertThat(ranked).isEqualTo(2);
        List<PopularProduct> saved = captureSaved();
        assertThat(saved.get(0).getProductId()).isEqualTo(2L);
        assertThat(saved.get(0).getName()).isEqualTo("꽃다발");
        assertThat(saved.get(0).getRank()).isEqualTo(1);
        assertThat(saved.get(1).getProductId()).isEqualTo(1L);
        assertThat(saved.get(1).getRank()).isEqualTo(2);
    }

    @Test
    @DisplayName("관심 수가 같으면 먼저 등록된 상품이 앞 순위가 된다")
    void refreshBreaksTieByProductId() {
        Product ring = product(1L, "커플링", 100_000);
        Product flower = product(2L, "꽃다발", 50_000);
        when(productInterestService.countInterestsByProduct()).thenReturn(List.of(
                new ProductInterestCount(2L, 5), new ProductInterestCount(1L, 5)));
        when(productRepository.findAllById(any())).thenReturn(List.of(flower, ring));
        when(productRepository.findAll(any(Pageable.class))).thenReturn(emptyPage());

        popularProductRankingService.refresh();

        List<PopularProduct> saved = captureSaved();
        assertThat(saved.get(0).getProductId()).isEqualTo(1L);
        assertThat(saved.get(1).getProductId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("관심 데이터가 없으면 카탈로그 등록 순서로 순위를 채운다")
    void refreshFallsBackToCatalogOrder() {
        Product ring = product(1L, "커플링", 100_000);
        Product flower = product(2L, "꽃다발", 50_000);
        when(productInterestService.countInterestsByProduct()).thenReturn(List.of());
        when(productRepository.findAllById(any())).thenReturn(List.of());
        when(productRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ring, flower)));

        int ranked = popularProductRankingService.refresh();

        assertThat(ranked).isEqualTo(2);
        List<PopularProduct> saved = captureSaved();
        assertThat(saved.get(0).getProductId()).isEqualTo(1L);
        assertThat(saved.get(1).getProductId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("관심 상품과 카탈로그 상품이 겹치면 중복 없이 한 번만 순위에 오른다")
    void refreshDoesNotDuplicateProducts() {
        Product ring = product(1L, "커플링", 100_000);
        Product flower = product(2L, "꽃다발", 50_000);
        when(productInterestService.countInterestsByProduct())
                .thenReturn(List.of(new ProductInterestCount(2L, 4)));
        when(productRepository.findAllById(any())).thenReturn(List.of(flower));
        when(productRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ring, flower)));

        popularProductRankingService.refresh();

        List<PopularProduct> saved = captureSaved();
        assertThat(saved).hasSize(2);
        assertThat(saved.get(0).getProductId()).isEqualTo(2L);
        assertThat(saved.get(1).getProductId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("카탈로그가 비어 있으면 기존 순위를 지우지 않는다")
    void refreshKeepsExistingRankingWhenCatalogIsEmpty() {
        when(productInterestService.countInterestsByProduct()).thenReturn(List.of());
        when(productRepository.findAllById(any())).thenReturn(List.of());
        when(productRepository.findAll(any(Pageable.class))).thenReturn(emptyPage());

        int ranked = popularProductRankingService.refresh();

        assertThat(ranked).isZero();
        verify(popularProductRepository, never()).deleteAllInBatch();
        verify(popularProductRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("순위가 이미 있으면 최초 생성은 건너뛴다")
    void refreshIfEmptySkipsWhenRankingExists() {
        when(popularProductRepository.count()).thenReturn(20L);

        int ranked = popularProductRankingService.refreshIfEmpty();

        assertThat(ranked).isZero();
        verify(popularProductRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("순위 갱신은 기존 순위를 지운 뒤 다시 만든다")
    void refreshReplacesExistingRanking() {
        Product ring = product(1L, "커플링", 100_000);
        when(productInterestService.countInterestsByProduct()).thenReturn(List.of());
        when(productRepository.findAllById(any())).thenReturn(List.of());
        when(productRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(ring)));

        popularProductRankingService.refresh();

        verify(popularProductRepository).deleteAllInBatch();
        assertThat(captureSaved()).hasSize(1);
    }

    @Test
    @DisplayName("순위에는 상품의 업체명과 상세 경로를 함께 남긴다")
    void refreshKeepsProductSource() {
        Product ring = product(1L, "커플링", 100_000);
        when(productInterestService.countInterestsByProduct()).thenReturn(List.of());
        when(productRepository.findAllById(any())).thenReturn(List.of());
        when(productRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(ring)));

        popularProductRankingService.refresh();

        PopularProduct saved = captureSaved().get(0);
        assertThat(saved.getSourceName()).isEqualTo("업체");
        assertThat(saved.getSourceUrl()).isEqualTo("/api/products/1");
        assertThat(saved.getThumbnailUrl()).isEqualTo("/products/1.jpg");
    }

    @SuppressWarnings("unchecked")
    private List<PopularProduct> captureSaved() {
        ArgumentCaptor<List<PopularProduct>> captor = ArgumentCaptor.forClass(List.class);
        verify(popularProductRepository).saveAll(captor.capture());
        return captor.getValue();
    }

    private Page<Product> emptyPage() {
        return new PageImpl<>(List.of());
    }

    private Product product(Long id, String name, int price) {
        Product product = Product.create(
                name, ProductCategory.RING, price, "업체", "/products/" + id + ".jpg", null);
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
