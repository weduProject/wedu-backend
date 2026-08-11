package com.wedu.product.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductThumbnailUrlsTest {

    @Test
    @DisplayName("상대 경로 썸네일은 공개 베이스 URL을 붙인다")
    void prependPublicBaseUrlToRelativePath() {
        assertThat(ProductThumbnailUrls.toPublicUrl("/products/1.jpg", "https://api.example.com"))
                .isEqualTo("https://api.example.com/products/1.jpg");
    }

    @Test
    @DisplayName("이미 절대 URL이면 그대로 둔다")
    void keepAbsoluteUrl() {
        assertThat(ProductThumbnailUrls.toPublicUrl(
                        "https://cdn.example.com/a.jpg", "https://api.example.com"))
                .isEqualTo("https://cdn.example.com/a.jpg");
    }

    @Test
    @DisplayName("스킴이 대문자여도 절대 URL로 보고 베이스 URL을 붙이지 않는다")
    void keepAbsoluteUrlWithUppercaseScheme() {
        assertThat(ProductThumbnailUrls.toPublicUrl(
                        "HTTPS://cdn.example.com/a.jpg", "https://api.example.com"))
                .isEqualTo("HTTPS://cdn.example.com/a.jpg");
    }

    @Test
    @DisplayName("비어 있으면 null을 반환한다")
    void blankBecomesNull() {
        assertThat(ProductThumbnailUrls.toPublicUrl(" ", "https://api.example.com")).isNull();
        assertThat(ProductThumbnailUrls.toPublicUrl(null, "https://api.example.com")).isNull();
    }
}
