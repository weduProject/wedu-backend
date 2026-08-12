package com.wedu.product.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.wedu.product.domain.Product;
import com.wedu.product.domain.ProductCategory;
import com.wedu.product.dto.PopularProductResponse;
import com.wedu.product.repository.PopularProductRepository;
import com.wedu.product.repository.ProductRepository;
import com.wedu.proposal.domain.Cart;
import com.wedu.proposal.domain.Wishlist;
import com.wedu.proposal.repository.CartRepository;
import com.wedu.proposal.repository.WishlistRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PopularProductRankingIntegrationTest {

    @Autowired private PopularProductRankingService popularProductRankingService;
    @Autowired private PopularProductService popularProductService;
    @Autowired private PopularProductRepository popularProductRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private WishlistRepository wishlistRepository;
    @Autowired private CartRepository cartRepository;

    @BeforeEach
    void setUp() {
        popularProductRepository.deleteAll();
        wishlistRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("찜과 담기를 합친 관심 수 순으로 인기 상품 순위를 만든다")
    void refreshRanksByInterest() {
        Product ring = productRepository.save(
                Product.create("커플링", ProductCategory.RING, 100_000, "링업체", "/products/1.jpg", null));
        Product flower = productRepository.save(
                Product.create("꽃다발", ProductCategory.FLOWER, 50_000, "꽃업체", "/products/2.jpg", null));

        Wishlist firstWish = Wishlist.create(11L);
        firstWish.add(flower.getId());
        Wishlist secondWish = Wishlist.create(12L);
        secondWish.add(flower.getId());
        wishlistRepository.saveAll(List.of(firstWish, secondWish));

        Cart cart = Cart.create(13L);
        cart.addItem(ring.getId(), "커플링", 100_000, 3);
        cartRepository.save(cart);

        int ranked = popularProductRankingService.refresh();

        assertThat(ranked).isEqualTo(2);
        List<PopularProductResponse> populars = popularProductService.getPopularProducts();
        assertThat(populars).hasSize(2);
        assertThat(populars.get(0).productId()).isEqualTo(flower.getId());
        assertThat(populars.get(0).rank()).isEqualTo(1);
        assertThat(populars.get(0).category()).isEqualTo(ProductCategory.FLOWER);
        assertThat(populars.get(0).sourceName()).isEqualTo("꽃업체");
        assertThat(populars.get(1).productId()).isEqualTo(ring.getId());
        assertThat(populars.get(1).rank()).isEqualTo(2);
        assertThat(populars.get(1).category()).isEqualTo(ProductCategory.RING);
    }

    @Test
    @DisplayName("찜·담기가 없어도 카탈로그 상품으로 순위를 채운다")
    void refreshFillsFromCatalogWithoutInterest() {
        productRepository.save(
                Product.create("커플링", ProductCategory.RING, 100_000, "링업체", "/products/1.jpg", null));

        int ranked = popularProductRankingService.refresh();

        assertThat(ranked).isEqualTo(1);
        assertThat(popularProductService.getPopularProducts().get(0).rank()).isEqualTo(1);
    }

    @Test
    @DisplayName("순위를 다시 만들어도 상품 수만큼만 남는다")
    void refreshReplacesPreviousRanking() {
        productRepository.save(
                Product.create("커플링", ProductCategory.RING, 100_000, "링업체", "/products/1.jpg", null));

        popularProductRankingService.refresh();
        popularProductRankingService.refresh();

        assertThat(popularProductRepository.count()).isEqualTo(1);
    }
}
