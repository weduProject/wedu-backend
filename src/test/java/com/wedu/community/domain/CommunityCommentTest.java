package com.wedu.community.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wedu.global.error.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommunityCommentTest {

    @Test
    @DisplayName("최상위 댓글과 1단계 답글을 생성한다")
    void createCommentAndReply() {
        CommunityComment comment = CommunityComment.createComment(10L, 1L, " 댓글 ", true);
        CommunityComment reply = CommunityComment.createReply(10L, 2L, 20L, " 답글 ", false);

        assertThat(comment.getContent()).isEqualTo("댓글");
        assertThat(comment.isReply()).isFalse();
        assertThat(reply.getParentId()).isEqualTo(20L);
        assertThat(reply.isReply()).isTrue();
    }

    @Test
    @DisplayName("댓글 내용은 비어 있거나 1000자를 초과할 수 없다")
    void validateContent() {
        CommunityComment boundary = CommunityComment.createComment(
                10L, 1L, "😀".repeat(CommunityComment.MAX_CONTENT_LENGTH), false);

        assertThat(boundary.getContent().codePointCount(0, boundary.getContent().length()))
                .isEqualTo(CommunityComment.MAX_CONTENT_LENGTH);
        assertThatThrownBy(() -> CommunityComment.createComment(10L, 1L, " ", false))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> CommunityComment.createComment(
                        10L, 1L, "😀".repeat(CommunityComment.MAX_CONTENT_LENGTH + 1), false))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("작성자만 자신의 댓글을 소유한다")
    void ownership() {
        CommunityComment comment = CommunityComment.createComment(10L, 1L, "댓글", false);

        assertThat(comment.isOwnedBy(1L)).isTrue();
        assertThat(comment.isOwnedBy(2L)).isFalse();
        assertThat(comment.isOwnedBy(null)).isFalse();
    }
}
