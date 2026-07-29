package com.wedu.auth.dto;

import com.wedu.user.domain.User;

/** 이메일 회원가입/로그인 성공 응답. */
public record EmailAuthResponse(
        String tokenType,
        String accessToken,
        Long userId,
        String email,
        String nickname,
        boolean onboardingCompleted) {

    public static EmailAuthResponse from(String accessToken, User user) {
        return new EmailAuthResponse(
                "Bearer",
                accessToken,
                user.getId(),
                user.getEmail(),
                user.getNickname().getValue(),
                user.isOnboardingCompleted());
    }
}
