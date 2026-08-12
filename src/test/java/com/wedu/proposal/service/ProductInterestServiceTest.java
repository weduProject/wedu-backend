package com.wedu.proposal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.wedu.proposal.dto.ProductInterestCount;
import com.wedu.proposal.repository.CartRepository;
import com.wedu.proposal.repository.WishlistRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductInterestServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private CartRepository cartRepository;

    private ProductInterestService productInterestService;

    @BeforeEach
    void setUp() {
        productInterestService = new ProductInterestService(wishlistRepository, cartRepository);
    }

    @Test
    @DisplayName("같은 상품의 찜 수와 담긴 수를 합쳐 관심 수로 집계한다")
    void countInterestsByProduct() {
        when(wishlistRepository.countWishesByProduct())
                .thenReturn(List.of(new ProductInterestCount(1L, 2), new ProductInterestCount(2L, 1)));
        when(cartRepository.countCartsByProduct())
                .thenReturn(List.of(new ProductInterestCount(1L, 3), new ProductInterestCount(3L, 5)));

        List<ProductInterestCount> result = productInterestService.countInterestsByProduct();

        assertThat(result).containsExactlyInAnyOrder(
                new ProductInterestCount(1L, 5),
                new ProductInterestCount(2L, 1),
                new ProductInterestCount(3L, 5));
    }

    @Test
    @DisplayName("찜과 담기가 모두 없으면 집계 결과가 비어 있다")
    void countInterestsByProductWithoutSignals() {
        when(wishlistRepository.countWishesByProduct()).thenReturn(List.of());
        when(cartRepository.countCartsByProduct()).thenReturn(List.of());

        assertThat(productInterestService.countInterestsByProduct()).isEmpty();
    }
}
