package com.wedu.community.dto;

import com.wedu.community.domain.CommunityPost;
import com.wedu.community.domain.PostTheme;
import com.wedu.user.dto.UserPublicProfileResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/** 커뮤니티 게시글 상세 응답. */
public record CommunityPostDetailResponse(
        Long postId,
        String title,
        String content,
        PostTheme theme,
        boolean anonymous,
        CommunityPostAuthorResponse author,
        boolean isMine,
        long likeCount,
        long commentCount,
        @Schema(description = "UTC 기준 작성 시각") LocalDateTime createdAt,
        @Schema(description = "UTC 기준 수정 시각") LocalDateTime updatedAt) {

    /** 게시글과 실제 댓글 수를 상세 응답으로 변환한다. */
    public static CommunityPostDetailResponse from(
            CommunityPost post,
            Long viewerId,
            UserPublicProfileResponse profile,
            long commentCount) {
        CommunityPostSummaryResponse summary =
                CommunityPostSummaryResponse.from(post, viewerId, profile, commentCount);
        return new CommunityPostDetailResponse(
                summary.postId(),
                summary.title(),
                summary.content(),
                summary.theme(),
                summary.anonymous(),
                summary.author(),
                summary.isMine(),
                summary.likeCount(),
                summary.commentCount(),
                summary.createdAt(),
                post.getUpdatedAt());
    }
}
