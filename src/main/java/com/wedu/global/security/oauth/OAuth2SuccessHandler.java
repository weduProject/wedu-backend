package com.wedu.global.security.oauth;

import com.wedu.auth.dto.SocialLoginResult;
import com.wedu.auth.service.OAuthLoginCodeStore;
import com.wedu.auth.service.SocialLoginService;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 소셜 로그인 성공 후 회원 조회/가입 → JWT 발급 → 일회용 코드로 프론트 콜백 리다이렉트한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2UserInfoExtractor userInfoExtractor;
    private final SocialLoginService socialLoginService;
    private final OAuthLoginCodeStore oAuthLoginCodeStore;
    private final HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;

    @Value("${wedu.oauth2.frontend-redirect-uri}")
    private String frontendRedirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);

        try {
            if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
                throw new BusinessException(ErrorCode.AUTH_OAUTH2_FAILED);
            }
            OAuth2User oauth2User = oauthToken.getPrincipal();
            OAuth2UserInfo userInfo = userInfoExtractor.extract(
                    oauthToken.getAuthorizedClientRegistrationId(), oauth2User.getAttributes());
            SocialLoginResult result = socialLoginService.loginOrRegister(userInfo);
            String code = oAuthLoginCodeStore.issue(result);
            response.sendRedirect(buildRedirectUri(code, result));
        } catch (BusinessException ex) {
            log.warn("OAuth2 success handling failed: {}", ex.getMessage());
            response.sendRedirect(buildErrorRedirectUri(ex.getErrorCode().getCode()));
        } catch (Exception ex) {
            log.error("Unexpected OAuth2 success handling error", ex);
            response.sendRedirect(buildErrorRedirectUri(ErrorCode.AUTH_OAUTH2_FAILED.getCode()));
        }
    }

    private String buildRedirectUri(String code, SocialLoginResult result) {
        return UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("code", code)
                .queryParam("userId", result.userId())
                .queryParam("onboardingCompleted", result.onboardingCompleted())
                .build(true)
                .toUriString();
    }

    private String buildErrorRedirectUri(String errorCode) {
        return UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("error", errorCode)
                .build(true)
                .toUriString();
    }
}
