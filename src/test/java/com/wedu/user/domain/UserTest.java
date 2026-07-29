package com.wedu.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {

    private User registerUser() {
        return User.register(
                SocialProvider.KAKAO, "kakao-1", "wedu@example.com", new Nickname("완규"), null);
    }

    @Test
    @DisplayName("가입 직후에는 온보딩이 완료되지 않은 상태다")
    void registeredUserIsNotOnboarded() {
        User user = registerUser();
        assertThat(user.isOnboardingCompleted()).isFalse();
        assertThat(user.getProvider()).isEqualTo(SocialProvider.KAKAO);
    }

    @Test
    @DisplayName("이메일 회원가입은 LOCAL 제공자와 비밀번호 해시를 가진다")
    void registerLocalUser() {
        User user = User.registerLocal("wedu@example.com", new Nickname("완규"), "encoded-password");

        assertThat(user.getProvider()).isEqualTo(SocialProvider.LOCAL);
        assertThat(user.getSocialId()).isEqualTo("wedu@example.com");
        assertThat(user.getEmail()).isEqualTo("wedu@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(user.isOnboardingCompleted()).isFalse();
    }

    @Test
    @DisplayName("필수 값이 없으면 가입에 실패한다")
    void registerRequiresMandatoryFields() {
        assertThatThrownBy(() ->
                User.register(null, "kakao-1", "wedu@example.com", new Nickname("완규"), null))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("이메일 회원가입은 비밀번호 해시가 필수다")
    void registerLocalRequiresPasswordHash() {
        assertThatThrownBy(() -> User.registerLocal("wedu@example.com", new Nickname("완규"), " "))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("온보딩을 완료하면 상태가 true 로 바뀐다")
    void completeOnboarding() {
        User user = registerUser();
        user.completeOnboarding();
        assertThat(user.isOnboardingCompleted()).isTrue();
    }

    @Test
    @DisplayName("이미 온보딩한 사용자를 다시 온보딩하면 USER_409 가 발생한다")
    void completeOnboardingTwiceFails() {
        User user = registerUser();
        user.completeOnboarding();
        assertThatThrownBy(user::completeOnboarding)
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.USER_ALREADY_ONBOARDED);
    }

    @Test
    @DisplayName("프로필 수정 시 닉네임과 이미지가 갱신된다")
    void updateProfile() {
        User user = registerUser();
        user.updateProfile(new Nickname("newname"), "https://img/new.png");
        assertThat(user.getNickname().getValue()).isEqualTo("newname");
        assertThat(user.getProfileImageUrl()).isEqualTo("https://img/new.png");
    }
}
