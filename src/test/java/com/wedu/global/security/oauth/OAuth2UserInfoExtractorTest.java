package com.wedu.global.security.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.user.domain.SocialProvider;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OAuth2UserInfoExtractorTest {

    private final OAuth2UserInfoExtractor extractor = new OAuth2UserInfoExtractor();

    @Test
    @DisplayName("카카오 속성에서 소셜 프로필을 추출한다")
    void extractKakaoProfile() {
        Map<String, Object> attributes = Map.of(
                "id", 123456789L,
                "kakao_account", Map.of(
                        "email", "kakao@example.com",
                        "profile", Map.of(
                                "nickname", "카카오닉",
                                "profile_image_url", "https://img.example/kakao.png")));

        OAuth2UserInfo info = extractor.extract("kakao", attributes);

        assertThat(info.provider()).isEqualTo(SocialProvider.KAKAO);
        assertThat(info.socialId()).isEqualTo("123456789");
        assertThat(info.email()).isEqualTo("kakao@example.com");
        assertThat(info.nickname()).isEqualTo("카카오닉");
        assertThat(info.profileImageUrl()).isEqualTo("https://img.example/kakao.png");
    }

    @Test
    @DisplayName("구글 속성에서 소셜 프로필을 추출한다")
    void extractGoogleProfile() {
        Map<String, Object> attributes = Map.of(
                "sub", "google-sub-001",
                "email", " Google@Example.com ",
                "name", "구글닉",
                "picture", "https://img.example/google.png");

        OAuth2UserInfo info = extractor.extract("google", attributes);

        assertThat(info.provider()).isEqualTo(SocialProvider.GOOGLE);
        assertThat(info.socialId()).isEqualTo("google-sub-001");
        assertThat(info.email()).isEqualTo("google@example.com");
        assertThat(info.nickname()).isEqualTo("구글닉");
        assertThat(info.profileImageUrl()).isEqualTo("https://img.example/google.png");
    }

    @Test
    @DisplayName("이메일이 없으면 예외를 던진다")
    void rejectMissingEmail() {
        Map<String, Object> attributes = Map.of("sub", "google-sub-001", "name", "닉");

        assertThatThrownBy(() -> extractor.extract("google", attributes))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_EMAIL_REQUIRED);
    }

    @Test
    @DisplayName("지원하지 않는 provider 면 예외를 던진다")
    void rejectUnsupportedProvider() {
        for (String provider : new String[] {"naver", "facebook"}) {
            assertThatThrownBy(() -> extractor.extract(provider, Map.of("id", "1")))
                    .isInstanceOf(BusinessException.class)
                    .extracting(ex -> ((BusinessException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_UNSUPPORTED_PROVIDER);
        }
    }

    @Test
    @DisplayName("닉네임이 없으면 이메일 로컬파트로 대체하고 20자로 자른다")
    void fallbackNicknameFromEmail() {
        String longLocal = "a".repeat(30);
        Map<String, Object> attributes = Map.of(
                "sub", "google-sub-002",
                "email", longLocal + "@example.com");

        OAuth2UserInfo info = extractor.extract("google", attributes);

        assertThat(info.nickname()).isEqualTo("a".repeat(20));
    }
}
