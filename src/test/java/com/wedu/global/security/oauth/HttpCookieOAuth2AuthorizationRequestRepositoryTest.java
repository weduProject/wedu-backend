package com.wedu.global.security.oauth;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

class HttpCookieOAuth2AuthorizationRequestRepositoryTest {

    private HttpCookieOAuth2AuthorizationRequestRepository repository;

    @BeforeEach
    void setUp() {
        FrontendRedirectUriResolver resolver = new FrontendRedirectUriResolver(
                "https://wedu.io.kr/auth/callback",
                List.of(
                        "https://wedu.io.kr",
                        "http://localhost:5173",
                        "http://localhost:3000"));
        repository = new HttpCookieOAuth2AuthorizationRequestRepository(new ObjectMapper(), resolver);
    }

    @Test
    @DisplayName("허용된 redirect_uri 는 별도 쿠키에 저장한다")
    void savesAllowedFrontendRedirectCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(
                FrontendRedirectUriResolver.REDIRECT_URI_PARAM,
                "http://localhost:5173/auth/callback");
        MockHttpServletResponse response = new MockHttpServletResponse();

        repository.saveAuthorizationRequest(sampleAuthorizationRequest(), request, response);

        assertThat(response.getCookie(
                        HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTH_REQUEST_COOKIE_NAME))
                .isNotNull();
        MockHttpServletRequest loadRequest = new MockHttpServletRequest();
        loadRequest.setCookies(response.getCookies());
        assertThat(repository.loadFrontendRedirectUri(loadRequest))
                .isEqualTo("http://localhost:5173/auth/callback");
    }

    @Test
    @DisplayName("불허 redirect_uri 는 프론트 콜백 쿠키를 유지하지 않는다")
    void doesNotSaveDisallowedFrontendRedirectCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(
                FrontendRedirectUriResolver.REDIRECT_URI_PARAM,
                "http://evil.com/auth/callback");
        MockHttpServletResponse response = new MockHttpServletResponse();

        repository.saveAuthorizationRequest(sampleAuthorizationRequest(), request, response);

        assertThat(response.getCookie(
                        HttpCookieOAuth2AuthorizationRequestRepository.FRONTEND_REDIRECT_COOKIE_NAME))
                .isNotNull()
                .extracting(cookie -> cookie.getMaxAge())
                .isEqualTo(0);
        MockHttpServletRequest loadRequest = new MockHttpServletRequest();
        loadRequest.setCookies(response.getCookies());
        assertThat(repository.loadFrontendRedirectUri(loadRequest)).isNull();
    }

    @Test
    @DisplayName("저장된 프론트 콜백 쿠키를 읽고 삭제할 수 있다")
    void loadAndRemoveFrontendRedirectCookie() {
        MockHttpServletRequest saveRequest = new MockHttpServletRequest();
        saveRequest.setParameter(
                FrontendRedirectUriResolver.REDIRECT_URI_PARAM, "http://localhost:5173");
        MockHttpServletResponse saveResponse = new MockHttpServletResponse();
        repository.saveAuthorizationRequest(sampleAuthorizationRequest(), saveRequest, saveResponse);

        MockHttpServletRequest loadRequest = new MockHttpServletRequest();
        loadRequest.setCookies(saveResponse.getCookies());
        assertThat(repository.loadFrontendRedirectUri(loadRequest))
                .isEqualTo("http://localhost:5173/auth/callback");

        MockHttpServletResponse clearResponse = new MockHttpServletResponse();
        repository.removeAuthorizationRequestCookies(loadRequest, clearResponse);

        assertThat(clearResponse.getCookie(
                        HttpCookieOAuth2AuthorizationRequestRepository.FRONTEND_REDIRECT_COOKIE_NAME))
                .isNotNull()
                .extracting(cookie -> cookie.getMaxAge())
                .isEqualTo(0);
    }

    private OAuth2AuthorizationRequest sampleAuthorizationRequest() {
        return OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://kauth.kakao.com/oauth/authorize")
                .clientId("client-id")
                .redirectUri("http://localhost:8080/login/oauth2/code/kakao")
                .state("state")
                .build();
    }
}
