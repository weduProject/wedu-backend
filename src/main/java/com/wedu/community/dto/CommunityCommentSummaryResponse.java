package com.wedu.community.dto;

import com.wedu.community.domain.CommunityComment;
import com.wedu.user.dto.UserPublicProfileResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

/** 최상위 댓글 목록 응답. */
public record CommunityCommentSummaryResponse(
        Long commentId,
        Long postId,
        String content,
        boolean anonymous,
        CommunityCommentAuthorResponse author,
        boolean isMine,
        @Schema(description = "게시글 작성자가 작성한 댓글인지 여부") boolean isPostAuthor,
        @Schema(description = "댓글의 1단계 답글 수") long replyCount,
        @Schema(description = "UTC 기준 작성 시각", example = "2026-08-06T01:00:00Z") OffsetDateTime createdAt,
        @Schema(description = "UTC 기준 수정 시각", example = "2026-08-06T01:00:00Z") OffsetDateTime updatedAt) {

    public static CommunityCommentSummaryResponse from(
            CommunityComment comment,
            Long viewerId,
            Long postAuthorId,
            UserPublicProfileResponse profile,
            long replyCount) {
        CommunityCommentResponse response =
                CommunityCommentResponse.from(comment, viewerId, postAuthorId, profile);
        return new CommunityCommentSummaryResponse(
                response.commentId(),
                response.postId(),
                response.content(),
                response.anonymous(),
                response.author(),
                response.isMine(),
                response.isPostAuthor(),
                replyCount,
                response.createdAt(),
                response.updatedAt());
    }
}
