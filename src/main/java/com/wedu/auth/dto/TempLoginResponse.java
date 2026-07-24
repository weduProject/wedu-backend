package com.wedu.auth.dto;

import com.wedu.user.domain.User;

public record TempLoginResponse(
        String tokenType,
        String accessToken,
        Long userId,
        String email,
        String nickname) {

    public static TempLoginResponse of(String accessToken, User user) {
        return new TempLoginResponse(
                "Bearer",
                accessToken,
                user.getId(),
                user.getEmail(),
                user.getNickname().getValue());
    }
}
