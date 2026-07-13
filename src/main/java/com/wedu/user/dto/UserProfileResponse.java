package com.wedu.user.dto;

import com.wedu.user.domain.User;

/**
 * 사용자 프로필 API 응답 본문. {@link User} 엔티티를 그대로 노출하지 않고, 응답에 필요한 값만 옮긴다
 * (엔티티 → DTO 변환은 이 {@code from} 에 모은다).
 */
public record UserProfileResponse(
        Long id,
        String provider,
        String email,
        String nickname,
        String profileImageUrl,
        boolean onboardingCompleted) {

    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getProvider().name(),
                user.getEmail(),
                user.getNickname().getValue(),
                user.getProfileImageUrl(),
                user.isOnboardingCompleted());
    }
}
