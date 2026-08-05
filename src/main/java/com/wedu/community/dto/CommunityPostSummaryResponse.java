package com.wedu.community.dto;

import com.wedu.community.domain.CommunityPost;
import com.wedu.community.domain.PostTheme;
import com.wedu.user.dto.UserPublicProfileResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/** 커뮤니티 게시글 목록 카드 응답. */
public record CommunityPostSummaryResponse(
        Long postId,
        String title,
        String content,
        PostTheme theme,
        boolean anonymous,
        CommunityPostAuthorResponse author,
        boolean isMine,
        @Schema(description = "게시글 좋아요 수. 좋아요 PR 전에는 0") long likeCount,
        @Schema(description = "게시글 댓글 수. 댓글 PR 전에는 0") long commentCount,
        @Schema(description = "UTC 기준 작성 시각") LocalDateTime createdAt) {

    /** 게시글과 조회자·작성자 공개 정보를 목록 응답으로 변환한다. */
    public static CommunityPostSummaryResponse from(
            CommunityPost post,
            Long viewerId,
            UserPublicProfileResponse profile) {
        return new CommunityPostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getTheme(),
                post.isAnonymous(),
                post.isAnonymous()
                        ? CommunityPostAuthorResponse.anonymous()
                        : CommunityPostAuthorResponse.from(profile),
                post.isOwnedBy(viewerId),
                0,
                0,
                post.getCreatedAt());
    }
}
