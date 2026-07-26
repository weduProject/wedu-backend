package com.wedu.proposal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.wedu.proposal.domain.Wishlist;
import com.wedu.proposal.dto.WishlistResponse;
import com.wedu.proposal.repository.WishlistRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    private WishlistService wishlistService;

    @BeforeEach
    void setUp() {
        wishlistService = new WishlistService(wishlistRepository);
    }

    @Test
    @DisplayName("찜 목록이 없으면 새로 만들고 상품을 추가한다")
    void addCreatesWishlistIfMissing() {
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(wishlistRepository.save(any(Wishlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WishlistResponse response = wishlistService.add(1L, 10L);

        assertThat(response.productIds()).containsExactly(10L);
    }

    @Test
    @DisplayName("찜 목록이 없으면 빈 목록을 조회 결과로 반환한다")
    void getMyWishlistReturnsEmptyWhenMissing() {
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.empty());

        WishlistResponse response = wishlistService.getMyWishlist(1L);

        assertThat(response.productIds()).isEmpty();
    }

    @Test
    @DisplayName("찜한 상품을 제거한다")
    void remove() {
        Wishlist wishlist = Wishlist.create(1L);
        wishlist.add(10L);
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(wishlist));

        WishlistResponse response = wishlistService.remove(1L, 10L);

        assertThat(response.productIds()).isEmpty();
    }
}
