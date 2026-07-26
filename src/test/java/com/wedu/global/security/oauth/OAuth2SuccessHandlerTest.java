package com.wedu.global.security.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wedu.auth.dto.SocialLoginResult;
import com.wedu.auth.service.SocialLoginService;
import com.wedu.global.security.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import com.wedu.global.security.oauth.OAuth2UserInfo;
import com.wedu.global.security.oauth.OAuth2UserInfoExtractor;
import com.wedu.global.security.oauth.OAuthLoginCodeStore;
import com.wedu.user.domain.SocialProvider;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OAuth2SuccessHandlerTest {

    @Mock
    private OAuth2UserInfoExtractor userInfoExtractor;

    @Mock
    private SocialLoginService socialLoginService;

    @Mock
    private OAuthLoginCodeStore oAuthLoginCodeStore;

    @Mock
    private HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;

    @InjectMocks
    private OAuth2SuccessHandler successHandler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                successHandler, "frontendRedirectUri", "http://localhost:3000/auth/callback");
    }

    @Test
    @DisplayName("로그인 성공 시 일회용 code 를 프론트 콜백으로 리다이렉트한다")
    void redirectWithOneTimeCode() throws Exception {
        Map<String, Object> attributes = Map.of(
                "sub", "google-1",
                "email", "user@example.com",
                "name", "유저");
        OAuth2User oauth2User = new DefaultOAuth2User(
                java.util.List.of(), attributes, "sub");
        OAuth2AuthenticationToken authentication =
                new OAuth2AuthenticationToken(oauth2User, java.util.List.of(), "google");

        OAuth2UserInfo info = new OAuth2UserInfo(
                SocialProvider.GOOGLE, "google-1", "user@example.com", "유저", null);
        SocialLoginResult result =
                SocialLoginResult.of("jwt-token", 10L, "user@example.com", "유저", false);
        when(userInfoExtractor.extract(eq("google"), eq(attributes))).thenReturn(info);
        when(socialLoginService.loginOrRegister(info)).thenReturn(result);
        when(oAuthLoginCodeStore.issue(result)).thenReturn("one-time-code");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        successHandler.onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getRedirectedUrl())
                .startsWith("http://localhost:3000/auth/callback?")
                .contains("code=one-time-code")
                .contains("userId=10")
                .contains("onboardingCompleted=false")
                .doesNotContain("accessToken=");
        verify(authorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
    }

    @Test
    @DisplayName("비즈니스 예외 시 error 쿼리로 리다이렉트한다")
    void redirectWithErrorOnBusinessException() throws Exception {
        Map<String, Object> attributes = Map.of("sub", "google-1");
        OAuth2User oauth2User = new DefaultOAuth2User(
                java.util.List.of(), attributes, "sub");
        OAuth2AuthenticationToken authentication =
                new OAuth2AuthenticationToken(oauth2User, java.util.List.of(), "google");

        when(userInfoExtractor.extract(eq("google"), any()))
                .thenThrow(new com.wedu.global.error.BusinessException(
                        com.wedu.global.error.ErrorCode.AUTH_EMAIL_REQUIRED));

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        successHandler.onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getRedirectedUrl())
                .contains("error=AUTH_400_EMAIL");
    }

    @Test
    @DisplayName("예상치 못한 예외 시 AUTH_OAUTH2_FAILED 로 리다이렉트한다")
    void redirectWithErrorOnUnexpectedException() throws Exception {
        Map<String, Object> attributes = Map.of(
                "sub", "google-1",
                "email", "user@example.com",
                "name", "유저");
        OAuth2User oauth2User = new DefaultOAuth2User(
                java.util.List.of(), attributes, "sub");
        OAuth2AuthenticationToken authentication =
                new OAuth2AuthenticationToken(oauth2User, java.util.List.of(), "google");

        OAuth2UserInfo info = new OAuth2UserInfo(
                SocialProvider.GOOGLE, "google-1", "user@example.com", "유저", null);
        when(userInfoExtractor.extract(eq("google"), eq(attributes))).thenReturn(info);
        when(socialLoginService.loginOrRegister(info)).thenThrow(new RuntimeException("boom"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        successHandler.onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getRedirectedUrl())
                .contains("error=AUTH_401_OAUTH2");
    }
}
