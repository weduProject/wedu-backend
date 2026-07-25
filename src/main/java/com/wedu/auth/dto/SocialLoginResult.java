package com.wedu.auth.dto;

import com.wedu.user.domain.User;

/**
 * 소셜 로그인 성공 결과. 일회용 코드 교환 API 응답으로도 쓰인다.
 */
public record SocialLoginResult(
        String tokenType,
        String accessToken,
        Long userId,
        String email,
        String nickname,
        boolean onboardingCompleted) {

    public static SocialLoginResult of(
            String accessToken,
            Long userId,
            String email,
            String nickname,
            boolean onboardingCompleted) {
        return new SocialLoginResult(
                "Bearer", accessToken, userId, email, nickname, onboardingCompleted);
    }

    public static SocialLoginResult from(User user, String accessToken) {
        return of(
                accessToken,
                user.getId(),
                user.getEmail(),
                user.getNickname().getValue(),
                user.isOnboardingCompleted());
    }
}
