package com.wedu.auth.dto;

/**
 * 소셜 로그인 성공 결과. 핸들러가 프론트 콜백 URL 쿼리로 전달한다.
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
}
