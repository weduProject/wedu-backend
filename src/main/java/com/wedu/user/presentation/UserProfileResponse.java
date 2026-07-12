package com.wedu.user.presentation;

import com.wedu.user.application.UserProfileResult;

/**
 * 사용자 프로필 API 응답 표현. 애플리케이션 결과({@link UserProfileResult})를 클라이언트 계약으로 옮긴다.
 */
public record UserProfileResponse(
        Long id,
        String provider,
        String email,
        String nickname,
        String profileImageUrl,
        boolean onboardingCompleted) {

    public static UserProfileResponse from(UserProfileResult result) {
        return new UserProfileResponse(
                result.id(),
                result.provider().name(),
                result.email(),
                result.nickname(),
                result.profileImageUrl(),
                result.onboardingCompleted());
    }
}
