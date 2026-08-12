package com.wedu.product.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PopularProductTest {

    @Test
    @DisplayName("인기 상품 수집 결과를 등록한다")
    void collect() {
        PopularProduct popularProduct =
                PopularProduct.collect("스냅 촬영 패키지", 300_000, "인스타그램", "http://source/1", "http://img/1", 1);

        assertThat(popularProduct.getName()).isEqualTo("스냅 촬영 패키지");
        assertThat(popularProduct.getRank()).isEqualTo(1);
    }

    @Test
    @DisplayName("순위가 1 미만이면 등록할 수 없다")
    void rejectInvalidRank() {
        assertThatThrownBy(() ->
                PopularProduct.collect("상품", 1000, "출처", "http://source", null, 0))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("가격이 음수면 등록할 수 없다")
    void rejectNegativePrice() {
        assertThatThrownBy(() ->
                PopularProduct.collect("상품", -1, "출처", "http://source", null, 1))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("상품명이 255자를 넘으면 등록할 수 없다")
    void rejectTooLongName() {
        String tooLongName = "가".repeat(256);

        assertThatThrownBy(() ->
                PopularProduct.collect(tooLongName, 1000, "출처", "http://source", null, 1))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_INVALID_NAME));
    }

    @Test
    @DisplayName("출처 URL이 1000자를 넘으면 등록할 수 없다")
    void rejectTooLongSourceUrl() {
        String tooLongUrl = "http://source/" + "a".repeat(990);

        assertThatThrownBy(() ->
                PopularProduct.collect("상품", 1000, "출처", tooLongUrl, null, 1))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_INVALID_SOURCE));
    }

    @Test
    @DisplayName("자체 카탈로그 상품의 순위는 상품 식별자를 함께 남긴다")
    void rankKeepsProductId() {
        PopularProduct popularProduct =
                PopularProduct.rank(7L, "커플링", 100_000, "링업체", "/api/products/7", "/products/7.jpg", 1);

        assertThat(popularProduct.getProductId()).isEqualTo(7L);
        assertThat(popularProduct.getRank()).isEqualTo(1);
    }

    @Test
    @DisplayName("상품 식별자 없이는 카탈로그 순위를 만들 수 없다")
    void rejectRankWithoutProductId() {
        assertThatThrownBy(() ->
                PopularProduct.rank(null, "커플링", 100_000, "링업체", "/api/products/7", null, 1))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT));
    }

    @Test
    @DisplayName("외부 출처에서 수집한 순위는 대응하는 상품 식별자가 없다")
    void collectHasNoProductId() {
        PopularProduct popularProduct =
                PopularProduct.collect("외부 상품", 100_000, "외부몰", "https://source/1", null, 1);

        assertThat(popularProduct.getProductId()).isNull();
    }

    @Test
    @DisplayName("다음 수집 배치 결과로 가격/순위를 갱신한다")
    void refresh() {
        PopularProduct popularProduct =
                PopularProduct.collect("상품", 1000, "출처", "http://source", null, 3);

        popularProduct.refresh(2000, 1);

        assertThat(popularProduct.getPrice()).isEqualTo(2000);
        assertThat(popularProduct.getRank()).isEqualTo(1);
    }
}
