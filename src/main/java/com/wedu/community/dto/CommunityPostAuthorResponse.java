package com.wedu.community.dto;

import com.wedu.user.dto.UserPublicProfileResponse;
import io.swagger.v3.oas.annotations.media.Schema;

/** 익명 여부가 적용된 게시글 작성자 공개 정보. */
public record CommunityPostAuthorResponse(
        @Schema(description = "비익명 작성자 ID. 익명이면 null") Long userId,
        @Schema(description = "작성자 닉네임. 익명이면 '익명'", example = "예비신랑") String nickname,
        @Schema(description = "비익명 작성자 프로필 이미지 URL") String profileImageUrl) {

    /** 익명 작성자를 식별 정보 없이 표현한다. */
    public static CommunityPostAuthorResponse anonymous() {
        return new CommunityPostAuthorResponse(null, "익명", null);
    }

    /** 사용자 공개 프로필을 게시글 작성자 응답으로 변환한다. */
    public static CommunityPostAuthorResponse from(UserPublicProfileResponse profile) {
        return new CommunityPostAuthorResponse(
                profile.userId(), profile.nickname(), profile.profileImageUrl());
    }
}
