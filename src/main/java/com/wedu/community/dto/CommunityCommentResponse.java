package com.wedu.community.dto;

import com.wedu.community.domain.CommunityComment;
import com.wedu.user.dto.UserPublicProfileResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/** 댓글 또는 답글 응답. */
public record CommunityCommentResponse(
        Long commentId,
        Long postId,
        Long parentId,
        String content,
        boolean anonymous,
        CommunityCommentAuthorResponse author,
        boolean isMine,
        @Schema(description = "게시글 작성자가 작성한 댓글인지 여부") boolean isPostAuthor,
        @Schema(description = "UTC 기준 작성 시각", example = "2026-08-06T01:00:00Z") OffsetDateTime createdAt,
        @Schema(description = "UTC 기준 수정 시각", example = "2026-08-06T01:00:00Z") OffsetDateTime updatedAt) {

    /** 댓글과 조회자·게시글 작성자 정보를 응답으로 변환한다. */
    public static CommunityCommentResponse from(
            CommunityComment comment,
            Long viewerId,
            Long postAuthorId,
            UserPublicProfileResponse profile) {
        return new CommunityCommentResponse(
                comment.getId(),
                comment.getPostId(),
                comment.getParentId(),
                comment.getContent(),
                comment.isAnonymous(),
                comment.isAnonymous()
                        ? CommunityCommentAuthorResponse.anonymous()
                        : CommunityCommentAuthorResponse.from(profile),
                comment.isOwnedBy(viewerId),
                comment.getAuthorId().equals(postAuthorId),
                toUtc(comment.getCreatedAt()),
                toUtc(comment.getUpdatedAt()));
    }

    private static OffsetDateTime toUtc(LocalDateTime value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }
}
