package com.wedu.proposal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.wedu.proposal.domain.Cart;
import com.wedu.proposal.dto.CartItemAddRequest;
import com.wedu.proposal.dto.CartResponse;
import com.wedu.proposal.repository.CartRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartRepository);
    }

    @Test
    @DisplayName("장바구니가 없으면 새로 만들고 상품을 담는다")
    void addItemCreatesCartIfMissing() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CartItemAddRequest request = new CartItemAddRequest(10L, "커플링", 150_000, 2);

        CartResponse response = cartService.addItem(1L, request);

        assertThat(response.items()).hasSize(1);
        assertThat(response.totalPrice()).isEqualTo(300_000);
    }

    @Test
    @DisplayName("장바구니가 없으면 빈 장바구니를 조회 결과로 반환한다")
    void getMyCartReturnsEmptyWhenMissing() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

        CartResponse response = cartService.getMyCart(1L);

        assertThat(response.items()).isEmpty();
        assertThat(response.totalPrice()).isZero();
    }

    @Test
    @DisplayName("담긴 상품을 제거한다")
    void removeItem() {
        Cart cart = Cart.create(1L);
        cart.addItem(10L, "커플링", 150_000, 1);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        CartResponse response = cartService.removeItem(1L, 10L);

        assertThat(response.items()).isEmpty();
    }
}
