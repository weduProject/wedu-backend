package com.wedu.user.application;

import com.wedu.user.domain.SocialProvider;
import com.wedu.user.domain.User;

/**
 * 애플리케이션 계층이 반환하는 사용자 프로필 결과.
 *
 * <p>도메인 애그리게이트를 프레젠테이션 계층에 그대로 노출하지 않기 위한 경계 타입이다.
 * HTTP 표현(응답 DTO)은 presentation 계층이 이 결과로부터 다시 만든다.
 */
public record UserProfileResult(
        Long id,
        SocialProvider provider,
        String email,
        String nickname,
        String profileImageUrl,
        boolean onboardingCompleted) {

    public static UserProfileResult from(User user) {
        return new UserProfileResult(
                user.getId(),
                user.getProvider(),
                user.getEmail(),
                user.getNickname().getValue(),
                user.getProfileImageUrl(),
                user.isOnboardingCompleted());
    }
}
