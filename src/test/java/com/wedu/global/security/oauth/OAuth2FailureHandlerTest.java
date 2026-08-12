package com.wedu.global.security.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

@ExtendWith(MockitoExtension.class)
class OAuth2FailureHandlerTest {

    private static final String DEFAULT_REDIRECT = "https://wedu.io.kr/auth/callback";
    private static final String ALLOWED_REDIRECT = "http://localhost:5173/auth/callback";

    @Mock
    private HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;

    @Mock
    private FrontendRedirectUriResolver frontendRedirectUriResolver;

    @InjectMocks
    private OAuth2FailureHandler failureHandler;

    @Test
    @DisplayName("실패 시 기본 프론트 콜백으로 error 를 보낸다")
    void redirectToDefaultOnFailure() throws Exception {
        when(authorizationRequestRepository.loadFrontendRedirectUri(any())).thenReturn(null);
        when(frontendRedirectUriResolver.resolveOrDefault(null)).thenReturn(DEFAULT_REDIRECT);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        failureHandler.onAuthenticationFailure(
                request, response, new BadCredentialsException("denied"));

        assertThat(response.getRedirectedUrl())
                .isEqualTo(DEFAULT_REDIRECT + "?error=AUTH_401_OAUTH2");
        verify(authorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
    }

    @Test
    @DisplayName("실패 시 allowlist 쿠키가 있으면 그 프론트 콜백으로 보낸다")
    void redirectToAllowedFrontendRedirectOnFailure() throws Exception {
        when(authorizationRequestRepository.loadFrontendRedirectUri(any()))
                .thenReturn(ALLOWED_REDIRECT);
        when(frontendRedirectUriResolver.resolveOrDefault(ALLOWED_REDIRECT))
                .thenReturn(ALLOWED_REDIRECT);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        failureHandler.onAuthenticationFailure(
                request, response, new BadCredentialsException("denied"));

        assertThat(response.getRedirectedUrl())
                .isEqualTo(ALLOWED_REDIRECT + "?error=AUTH_401_OAUTH2");
    }
}
