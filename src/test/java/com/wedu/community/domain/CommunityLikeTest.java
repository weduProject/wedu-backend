package com.wedu.community.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommunityLikeTest {

    @Test
    @DisplayName("게시글과 댓글 좋아요는 대상과 사용자 식별자를 보관한다")
    void createLikes() {
        CommunityPostLike postLike = CommunityPostLike.create(10L, 1L);
        CommunityCommentLike commentLike = CommunityCommentLike.create(20L, 1L);

        assertThat(postLike.getPostId()).isEqualTo(10L);
        assertThat(postLike.getUserId()).isEqualTo(1L);
        assertThat(commentLike.getCommentId()).isEqualTo(20L);
        assertThat(commentLike.getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("좋아요의 대상과 사용자 식별자는 양수여야 한다")
    void rejectInvalidIds() {
        assertThatThrownBy(() -> CommunityPostLike.create(0L, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
        assertThatThrownBy(() -> CommunityCommentLike.create(20L, null))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }
}
