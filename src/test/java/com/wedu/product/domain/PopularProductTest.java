package com.wedu.product.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wedu.global.error.BusinessException;
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
    @DisplayName("다음 수집 배치 결과로 가격/순위를 갱신한다")
    void refresh() {
        PopularProduct popularProduct =
                PopularProduct.collect("상품", 1000, "출처", "http://source", null, 3);

        popularProduct.refresh(2000, 1);

        assertThat(popularProduct.getPrice()).isEqualTo(2000);
        assertThat(popularProduct.getRank()).isEqualTo(1);
    }
}
