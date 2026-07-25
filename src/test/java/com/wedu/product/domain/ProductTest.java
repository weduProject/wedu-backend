package com.wedu.product.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wedu.global.error.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    @DisplayName("상품을 생성한다")
    void create() {
        Product product = Product.create(
                "심플 커플링", ProductCategory.RING, 150_000, "웨딩반지공방", "http://img/ring.jpg", "설명");

        assertThat(product.getName()).isEqualTo("심플 커플링");
        assertThat(product.getCategory()).isEqualTo(ProductCategory.RING);
        assertThat(product.getPrice()).isEqualTo(150_000);
    }

    @Test
    @DisplayName("가격이 음수면 생성할 수 없다")
    void rejectNegativePrice() {
        assertThatThrownBy(() ->
                Product.create("커플링", ProductCategory.RING, -1, "업체", null, null))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("상품명이 비어있으면 생성할 수 없다")
    void rejectBlankName() {
        assertThatThrownBy(() ->
                Product.create(" ", ProductCategory.RING, 1000, "업체", null, null))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("상품 가격을 변경한다")
    void changePrice() {
        Product product = Product.create("커플링", ProductCategory.RING, 1000, "업체", null, null);

        product.changePrice(2000);

        assertThat(product.getPrice()).isEqualTo(2000);
    }

    @Test
    @DisplayName("음수 가격으로 변경할 수 없다")
    void rejectChangeToNegativePrice() {
        Product product = Product.create("커플링", ProductCategory.RING, 1000, "업체", null, null);

        assertThatThrownBy(() -> product.changePrice(-1)).isInstanceOf(BusinessException.class);
    }
}
