package com.wedu.global.security.oauth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class FrontendRedirectUriResolverTest {

    private static final String DEFAULT_REDIRECT =
            "https://wedu.io.kr/auth/callback";

    private FrontendRedirectUriResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new FrontendRedirectUriResolver(
                DEFAULT_REDIRECT,
                List.of(
                        "https://wedu.io.kr",
                        "https://www.wedu.io.kr",
                        "http://localhost:5173",
                        "http://localhost:3000"));
    }

    @Test
    @DisplayName("allowlist에 있는 전체 URL이면 그대로 사용한다")
    void resolveAllowedFullUrl() {
        assertThat(resolver.resolve("http://localhost:5173/auth/callback"))
                .contains("http://localhost:5173/auth/callback");
        assertThat(resolver.resolve("https://www.wedu.io.kr/auth/callback"))
                .contains("https://www.wedu.io.kr/auth/callback");
    }

    @Test
    @DisplayName("origin만 넘기면 /auth/callback 을 붙인다")
    void resolveOriginOnlyAppendsCallbackPath() {
        assertThat(resolver.resolve("http://localhost:5173"))
                .contains("http://localhost:5173/auth/callback");
        assertThat(resolver.resolve("http://localhost:5173/"))
                .contains("http://localhost:5173/auth/callback");
    }

    @Test
    @DisplayName("기본 frontend-redirect-uri 도 allowlist에 포함된다")
    void defaultRedirectIsAlwaysAllowed() {
        FrontendRedirectUriResolver withDefaultOnly = new FrontendRedirectUriResolver(
                "https://custom.example.com/auth/callback",
                List.of("http://localhost:5173"));

        assertThat(withDefaultOnly.resolve("https://custom.example.com/auth/callback"))
                .contains("https://custom.example.com/auth/callback");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("없거나 공백이면 empty 를 반환한다")
    void resolveBlankReturnsEmpty(String candidate) {
        assertThat(resolver.resolve(candidate)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "http://evil.com/auth/callback",
                "https://wedu.io.kr.evil.com/auth/callback",
                "http://localhost:5173/auth/callback?evil=1",
                "http://localhost:5173/auth/callback#frag",
                "http://user@localhost:5173/auth/callback",
                "http://localhost:5173/auth/callback/",
                "http://localhost:5173/admin",
                "//evil.com/auth/callback",
                "/auth/callback",
                "javascript:alert(1)"
            })
    @DisplayName("불허·조작된 URL은 empty 를 반환한다")
    void resolveRejectsUnsafeOrDisallowed(String candidate) {
        assertThat(resolver.resolve(candidate)).isEmpty();
    }

    @Test
    @DisplayName("불허면 기본 frontend-redirect-uri 로 fallback 한다")
    void resolveOrDefaultFallsBack() {
        assertThat(resolver.resolveOrDefault("http://evil.com/auth/callback"))
                .isEqualTo(DEFAULT_REDIRECT);
        assertThat(resolver.resolveOrDefault(null)).isEqualTo(DEFAULT_REDIRECT);
        assertThat(resolver.resolveOrDefault("http://localhost:5173/auth/callback"))
                .isEqualTo("http://localhost:5173/auth/callback");
    }
}
