package com.wedu.user.dto;

import com.wedu.user.domain.User;

/** 다른 도메인에서 공개 가능한 사용자 프로필 정보. */
public record UserPublicProfileResponse(
        Long userId,
        String nickname,
        String profileImageUrl) {

    /** 사용자 엔티티를 공개 프로필로 변환한다. */
    public static UserPublicProfileResponse from(User user) {
        return new UserPublicProfileResponse(
                user.getId(), user.getNickname().getValue(), user.getProfileImageUrl());
    }
}
