package com.wedu.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.user.domain.Nickname;
import com.wedu.user.domain.SocialProvider;
import com.wedu.user.domain.User;
import com.wedu.user.dto.UserProfileResponse;
import com.wedu.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User sampleUser() {
        return User.register(
                SocialProvider.KAKAO, "kakao-1", "wedu@example.com", new Nickname("완규"), null);
    }

    @Test
    @DisplayName("프로필 조회 시 사용자 정보와 온보딩 여부를 응답으로 반환한다")
    void getProfile() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser()));

        UserProfileResponse response = userService.getProfile(1L);

        assertThat(response.nickname()).isEqualTo("완규");
        assertThat(response.provider()).isEqualTo("KAKAO");
        assertThat(response.onboardingCompleted()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 사용자를 조회하면 예외가 발생한다")
    void getProfileNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile(99L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("온보딩 완료 유스케이스가 엔티티 상태를 바꾸고 프로필을 반환한다")
    void completeOnboarding() {
        User user = sampleUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserProfileResponse response = userService.completeOnboarding(1L);

        assertThat(user.isOnboardingCompleted()).isTrue();
        assertThat(response.onboardingCompleted()).isTrue();
        assertThat(response.nickname()).isEqualTo("완규");
    }

    @Test
    @DisplayName("이미 온보딩한 사용자는 USER_409 로 거절한다")
    void completeOnboardingTwice() {
        User user = sampleUser();
        user.completeOnboarding();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.completeOnboarding(1L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.USER_ALREADY_ONBOARDED);
    }

    @Test
    @DisplayName("프로필 수정으로 온보딩 중 닉네임을 바꿀 수 있다")
    void updateProfile() {
        User user = sampleUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserProfileResponse response =
                userService.updateProfile(1L, "새닉네임", "https://img/new.png");

        assertThat(response.nickname()).isEqualTo("새닉네임");
        assertThat(response.profileImageUrl()).isEqualTo("https://img/new.png");
        assertThat(response.onboardingCompleted()).isFalse();
    }
}
