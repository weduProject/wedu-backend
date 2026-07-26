package com.wedu.global.security.oauth;

import com.wedu.global.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 소셜 로그인 실패 시 프론트 콜백으로 error 코드를 실어 보낸다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    private final HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;

    @Value("${wedu.oauth2.frontend-redirect-uri}")
    private String frontendRedirectUri;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException {
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
        log.warn("OAuth2 login failed: {}", exception.getMessage());
        String redirect = UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("error", ErrorCode.AUTH_OAUTH2_FAILED.getCode())
                .build(true)
                .toUriString();
        response.sendRedirect(redirect);
    }
}
