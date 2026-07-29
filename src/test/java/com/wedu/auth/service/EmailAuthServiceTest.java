package com.wedu.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wedu.auth.dto.EmailAuthResponse;
import com.wedu.auth.dto.EmailLoginRequest;
import com.wedu.auth.dto.EmailSignupRequest;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.global.security.jwt.JwtTokenProvider;
import com.wedu.user.domain.Nickname;
import com.wedu.user.domain.SocialProvider;
import com.wedu.user.domain.User;
import com.wedu.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EmailAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("이메일 회원가입 시 비밀번호를 암호화하고 JWT를 발급한다")
    void signup() {
        EmailAuthService emailAuthService =
                new EmailAuthService(userRepository, passwordEncoder, jwtTokenProvider);
        EmailSignupRequest request =
                new EmailSignupRequest("신부", "Bride@Example.com", "secret1", "secret1");
        User saved = localUser(1L, "bride@example.com", "신부", "encoded-password");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepository.existsByEmail("bride@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret1")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(jwtTokenProvider.createAccessToken(1L)).thenReturn("access-token");

        EmailAuthResponse response = emailAuthService.signup(request);

        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getProvider()).isEqualTo(SocialProvider.LOCAL);
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("bride@example.com");
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("encoded-password");
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.nickname()).isEqualTo("신부");
        assertThat(response.onboardingCompleted()).isFalse();
    }

    @Test
    @DisplayName("이미 가입된 이메일이면 회원가입을 거부한다")
    void signupRejectsDuplicateEmail() {
        EmailAuthService emailAuthService =
                new EmailAuthService(userRepository, passwordEncoder, jwtTokenProvider);
        EmailSignupRequest request =
                new EmailSignupRequest("신부", "bride@example.com", "secret1", "secret1");

        when(userRepository.existsByEmail("bride@example.com")).thenReturn(true);

        assertThatThrownBy(() -> emailAuthService.signup(request))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("비밀번호 확인이 다르면 회원가입을 거부한다")
    void signupRejectsPasswordConfirmMismatch() {
        EmailAuthService emailAuthService =
                new EmailAuthService(userRepository, passwordEncoder, jwtTokenProvider);
        EmailSignupRequest request =
                new EmailSignupRequest("신부", "bride@example.com", "secret1", "secret2");

        assertThatThrownBy(() -> emailAuthService.signup(request))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_PASSWORD_CONFIRM_MISMATCH);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("이메일과 비밀번호가 맞으면 JWT를 발급한다")
    void login() {
        EmailAuthService emailAuthService =
                new EmailAuthService(userRepository, passwordEncoder, jwtTokenProvider);
        EmailLoginRequest request = new EmailLoginRequest("Bride@Example.com", "secret1");
        User user = localUser(2L, "bride@example.com", "신부", "encoded-password");

        when(userRepository.findByProviderAndSocialId(SocialProvider.LOCAL, "bride@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret1", "encoded-password")).thenReturn(true);
        when(jwtTokenProvider.createAccessToken(2L)).thenReturn("login-token");

        EmailAuthResponse response = emailAuthService.login(request);

        assertThat(response.accessToken()).isEqualTo("login-token");
        assertThat(response.userId()).isEqualTo(2L);
        assertThat(response.email()).isEqualTo("bride@example.com");
    }

    @Test
    @DisplayName("이메일 또는 비밀번호가 틀리면 동일한 인증 실패 예외를 던진다")
    void loginRejectsInvalidCredentials() {
        EmailAuthService emailAuthService =
                new EmailAuthService(userRepository, passwordEncoder, jwtTokenProvider);
        EmailLoginRequest request = new EmailLoginRequest("missing@example.com", "secret1");

        when(userRepository.findByProviderAndSocialId(SocialProvider.LOCAL, "missing@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> emailAuthService.login(request))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_INVALID_CREDENTIALS);
        verify(jwtTokenProvider, never()).createAccessToken(any());
    }

    private User localUser(Long id, String email, String nickname, String passwordHash) {
        User user = User.registerLocal(email, new Nickname(nickname), passwordHash);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
