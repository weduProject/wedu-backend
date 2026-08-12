package com.wedu.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** 게시글 또는 댓글의 좋아요 변경 결과. */
public record CommunityLikeResponse(
        Long targetId,
        @Schema(description = "대상의 현재 좋아요 수") long likeCount,
        @Schema(description = "현재 사용자의 좋아요 여부") boolean likedByMe) {
}
