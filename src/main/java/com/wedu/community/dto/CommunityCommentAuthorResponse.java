package com.wedu.community.dto;

import com.wedu.user.dto.UserPublicProfileResponse;
import io.swagger.v3.oas.annotations.media.Schema;

/** 익명 여부가 적용된 댓글 작성자 공개 정보. */
public record CommunityCommentAuthorResponse(
        @Schema(description = "비익명 작성자 ID. 익명이면 null") Long userId,
        @Schema(description = "작성자 닉네임. 익명이면 '익명'", example = "예비신랑") String nickname,
        @Schema(description = "비익명 작성자 프로필 이미지 URL") String profileImageUrl) {

    public static CommunityCommentAuthorResponse anonymous() {
        return new CommunityCommentAuthorResponse(null, "익명", null);
    }

    public static CommunityCommentAuthorResponse from(UserPublicProfileResponse profile) {
        return new CommunityCommentAuthorResponse(
                profile.userId(), profile.nickname(), profile.profileImageUrl());
    }
}
