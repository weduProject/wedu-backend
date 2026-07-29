package com.wedu.global.security.oauth;

import com.wedu.auth.support.EmailNormalizer;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.user.domain.SocialProvider;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * OAuth2 제공자별 attributes → {@link OAuth2UserInfo} 변환.
 *
 * <p>카카오는 중첩 맵({@code kakao_account.profile}), 구글은 flat OIDC 클레임 구조를 쓴다.
 */
@Component
public class OAuth2UserInfoExtractor {

    private static final int NICKNAME_MAX_LENGTH = 20;

    public OAuth2UserInfo extract(String registrationId, Map<String, Object> attributes) {
        if (registrationId == null || registrationId.isBlank()) {
            throw new BusinessException(ErrorCode.AUTH_UNSUPPORTED_PROVIDER);
        }
        return switch (registrationId.toLowerCase()) {
            case "kakao" -> fromKakao(attributes);
            case "google" -> fromGoogle(attributes);
            default -> throw new BusinessException(ErrorCode.AUTH_UNSUPPORTED_PROVIDER);
        };
    }

    @SuppressWarnings("unchecked")
    private OAuth2UserInfo fromKakao(Map<String, Object> attributes) {
        Object id = attributes.get("id");
        if (id == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "카카오 사용자 id 가 없습니다.");
        }
        Map<String, Object> account = asMap(attributes.get("kakao_account"));
        Map<String, Object> profile = asMap(account.get("profile"));

        String email = stringValue(account.get("email"));
        String nickname = firstNonBlank(
                stringValue(profile.get("nickname")),
                nicknameFromEmail(email));
        String imageUrl = firstNonBlank(
                stringValue(profile.get("profile_image_url")),
                stringValue(profile.get("thumbnail_image_url")));

        return new OAuth2UserInfo(
                SocialProvider.KAKAO,
                String.valueOf(id),
                requireEmail(email),
                truncateNickname(nickname),
                blankToNull(imageUrl));
    }

    private OAuth2UserInfo fromGoogle(Map<String, Object> attributes) {
        String socialId = stringValue(attributes.get("sub"));
        if (!StringUtils.hasText(socialId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "구글 사용자 sub 가 없습니다.");
        }
        String email = stringValue(attributes.get("email"));
        String nickname = firstNonBlank(
                stringValue(attributes.get("name")),
                stringValue(attributes.get("given_name")),
                nicknameFromEmail(email));
        String imageUrl = stringValue(attributes.get("picture"));

        return new OAuth2UserInfo(
                SocialProvider.GOOGLE,
                socialId,
                requireEmail(email),
                truncateNickname(nickname),
                blankToNull(imageUrl));
    }

    private String requireEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_REQUIRED);
        }
        return EmailNormalizer.normalize(email);
    }

    private String nicknameFromEmail(String email) {
        if (!StringUtils.hasText(email) || !email.contains("@")) {
            return "웨드유저";
        }
        return email.substring(0, email.indexOf('@'));
    }

    private String truncateNickname(String nickname) {
        String value = nickname == null ? "웨드유저" : nickname.trim();
        if (value.isEmpty()) {
            value = "웨드유저";
        }
        return value.length() <= NICKNAME_MAX_LENGTH
                ? value
                : value.substring(0, NICKNAME_MAX_LENGTH);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }
}
