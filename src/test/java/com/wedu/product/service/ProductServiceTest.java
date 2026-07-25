package com.wedu.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.product.domain.Product;
import com.wedu.product.domain.ProductCategory;
import com.wedu.product.dto.ProductSummaryResponse;
import com.wedu.product.repository.ProductRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository);
    }

    @Test
    @DisplayName("조건에 맞는 상품 목록을 조회한다")
    void search() {
        Pageable pageable = PageRequest.of(0, 20);
        Product product = Product.create("커플링", ProductCategory.RING, 100_000, "업체", null, null);
        when(productRepository.search(eq(ProductCategory.RING), eq("커플"), isNull(), isNull(), eq(pageable)))
                .thenReturn(List.of(product));

        List<ProductSummaryResponse> result =
                productService.search(ProductCategory.RING, " 커플 ", null, null, pageable);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("커플링");
    }

    @Test
    @DisplayName("빈 키워드는 무시하고 전체 조회한다")
    void searchWithBlankKeyword() {
        Pageable pageable = PageRequest.of(0, 20);
        when(productRepository.search(isNull(), isNull(), isNull(), isNull(), eq(pageable)))
                .thenReturn(List.of());

        List<ProductSummaryResponse> result = productService.search(null, "   ", null, null, pageable);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("상품 상세 정보를 조회한다")
    void getDetail() {
        Product product = Product.create("커플링", ProductCategory.RING, 100_000, "업체", null, "설명");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        var response = productService.getDetail(1L);

        assertThat(response.name()).isEqualTo("커플링");
        assertThat(response.description()).isEqualTo("설명");
    }

    @Test
    @DisplayName("존재하지 않는 상품을 조회하면 예외가 발생한다")
    void rejectDetailOfMissingProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getDetail(1L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Test
    @DisplayName("최소 가격이 최대 가격보다 크면 예외가 발생한다")
    void rejectInvalidPriceRange() {
        Pageable pageable = PageRequest.of(0, 20);

        assertThatThrownBy(() -> productService.search(null, null, 50_000, 10_000, pageable))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.PRODUCT_INVALID_PRICE_RANGE));
    }

    @Test
    @DisplayName("최소 가격이 음수면 예외가 발생한다")
    void rejectNegativeMinPrice() {
        Pageable pageable = PageRequest.of(0, 20);

        assertThatThrownBy(() -> productService.search(null, null, -1, null, pageable))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_INVALID_PRICE));
    }

    @Test
    @DisplayName("최대 가격이 음수면 예외가 발생한다")
    void rejectNegativeMaxPrice() {
        Pageable pageable = PageRequest.of(0, 20);

        assertThatThrownBy(() -> productService.search(null, null, null, -1, pageable))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_INVALID_PRICE));
    }
}
