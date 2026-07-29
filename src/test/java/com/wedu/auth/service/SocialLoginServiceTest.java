package com.wedu.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wedu.auth.dto.SocialLoginResult;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.global.security.jwt.JwtTokenProvider;
import com.wedu.global.security.oauth.OAuth2UserInfo;
import com.wedu.user.domain.Nickname;
import com.wedu.user.domain.SocialProvider;
import com.wedu.user.domain.User;
import com.wedu.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SocialLoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private SocialUserRegistrar socialUserRegistrar;

    @InjectMocks
    private SocialLoginService socialLoginService;

    @Test
    @DisplayName("신규 사용자는 가입 후 JWT를 발급한다")
    void registerNewUserAndIssueToken() {
        OAuth2UserInfo info = new OAuth2UserInfo(
                SocialProvider.KAKAO,
                "kakao-1",
                "new@example.com",
                "신규",
                "https://img.example/a.png");
        User saved = user(1L, SocialProvider.KAKAO, "kakao-1", "new@example.com", "신규", false);

        when(userRepository.findByProviderAndSocialId(SocialProvider.KAKAO, "kakao-1"))
                .thenReturn(Optional.empty());
        when(socialUserRegistrar.register(info)).thenReturn(saved);
        when(jwtTokenProvider.createAccessToken(1L)).thenReturn("access-token");

        SocialLoginResult result = socialLoginService.loginOrRegister(info);

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("new@example.com");
        assertThat(result.nickname()).isEqualTo("신규");
        assertThat(result.onboardingCompleted()).isFalse();
        verify(socialUserRegistrar).register(info);
    }

    @Test
    @DisplayName("기존 사용자는 재사용하고 JWT만 발급한다")
    void reuseExistingUser() {
        OAuth2UserInfo info = new OAuth2UserInfo(
                SocialProvider.GOOGLE,
                "google-1",
                "old@example.com",
                "새닉",
                null);
        User existing = user(2L, SocialProvider.GOOGLE, "google-1", "old@example.com", "기존", true);

        when(userRepository.findByProviderAndSocialId(SocialProvider.GOOGLE, "google-1"))
                .thenReturn(Optional.of(existing));
        when(jwtTokenProvider.createAccessToken(2L)).thenReturn("new-token");

        SocialLoginResult result = socialLoginService.loginOrRegister(info);

        assertThat(result.accessToken()).isEqualTo("new-token");
        assertThat(result.userId()).isEqualTo(2L);
        assertThat(result.nickname()).isEqualTo("기존");
        assertThat(result.onboardingCompleted()).isTrue();
        verify(socialUserRegistrar, never()).register(any());
    }

    @Test
    @DisplayName("동시 가입으로 unique 충돌이 나면 기존 사용자를 다시 조회한다")
    void recoverFromConcurrentRegistration() {
        OAuth2UserInfo info = new OAuth2UserInfo(
                SocialProvider.KAKAO,
                "kakao-race",
                "race@example.com",
                "레이스",
                null);
        User existing = user(3L, SocialProvider.KAKAO, "kakao-race", "race@example.com", "레이스", false);

        when(userRepository.findByProviderAndSocialId(SocialProvider.KAKAO, "kakao-race"))
                .thenReturn(Optional.empty());
        when(socialUserRegistrar.register(info))
                .thenThrow(new DataIntegrityViolationException("duplicate"));
        when(socialUserRegistrar.findExisting(SocialProvider.KAKAO, "kakao-race"))
                .thenReturn(Optional.of(existing));
        when(jwtTokenProvider.createAccessToken(3L)).thenReturn("race-token");

        SocialLoginResult result = socialLoginService.loginOrRegister(info);

        assertThat(result.accessToken()).isEqualTo("race-token");
        assertThat(result.userId()).isEqualTo(3L);
        verify(socialUserRegistrar).findExisting(SocialProvider.KAKAO, "kakao-race");
    }

    @Test
    @DisplayName("다른 로그인 방식으로 이미 가입된 이메일이면 소셜 신규 가입을 거부한다")
    void rejectDuplicateEmailAcrossProviders() {
        OAuth2UserInfo info = new OAuth2UserInfo(
                SocialProvider.GOOGLE,
                "google-dup",
                "dup@example.com",
                "중복",
                null);

        when(userRepository.findByProviderAndSocialId(SocialProvider.GOOGLE, "google-dup"))
                .thenReturn(Optional.empty());
        when(userRepository.existsByEmail("dup@example.com")).thenReturn(true);

        assertThatThrownBy(() -> socialLoginService.loginOrRegister(info))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
        verify(socialUserRegistrar, never()).register(any());
    }

    private User user(
            Long id,
            SocialProvider provider,
            String socialId,
            String email,
            String nickname,
            boolean onboarded) {
        User user = User.register(provider, socialId, email, new Nickname(nickname), null);
        ReflectionTestUtils.setField(user, "id", id);
        if (onboarded) {
            user.completeOnboarding();
        }
        return user;
    }
}
