package com.wedu.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wedu.community.domain.CommunityCommentLike;
import com.wedu.community.domain.CommunityComment;
import com.wedu.community.domain.CommunityPostLike;
import com.wedu.community.domain.CommunityPost;
import com.wedu.community.domain.PostTheme;
import com.wedu.community.repository.CommunityCommentLikeRepository;
import com.wedu.community.repository.CommunityCommentRepository;
import com.wedu.community.repository.CommunityPostLikeRepository;
import com.wedu.community.repository.CommunityPostRepository;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CommunityLikeServiceTest {

    @Mock private CommunityPostRepository postRepository;
    @Mock private CommunityCommentRepository commentRepository;
    @Mock private CommunityPostLikeRepository postLikeRepository;
    @Mock private CommunityCommentLikeRepository commentLikeRepository;

    private CommunityLikeService likeService;

    @BeforeEach
    void setUp() {
        likeService = new CommunityLikeService(
                postRepository, commentRepository, postLikeRepository, commentLikeRepository);
    }

    @Test
    @DisplayName("게시글 좋아요를 등록하고 중복 요청은 멱등하게 처리한다")
    void likePostIdempotently() {
        when(postRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(post()));
        when(postLikeRepository.countByPostId(10L)).thenReturn(1L);

        var created = likeService.likePost(1L, 10L);

        assertThat(created.likeCount()).isEqualTo(1L);
        assertThat(created.likedByMe()).isTrue();
        verify(postLikeRepository).save(any(CommunityPostLike.class));

        when(postLikeRepository.existsByPostIdAndUserId(10L, 1L)).thenReturn(true);
        likeService.likePost(1L, 10L);

        verify(postLikeRepository).save(any(CommunityPostLike.class));
    }

    @Test
    @DisplayName("게시글 좋아요 취소는 좋아요가 없어도 성공한다")
    void unlikePostIdempotently() {
        when(postRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(post()));
        when(postLikeRepository.countByPostId(10L)).thenReturn(2L);

        var response = likeService.unlikePost(1L, 10L);

        verify(postLikeRepository).deleteByPostIdAndUserId(10L, 1L);
        assertThat(response.likeCount()).isEqualTo(2L);
        assertThat(response.likedByMe()).isFalse();
    }

    @Test
    @DisplayName("댓글과 답글에 같은 좋아요 API 규칙을 적용한다")
    void manageCommentLike() {
        when(commentRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(comment()));
        when(commentLikeRepository.countByCommentId(20L)).thenReturn(1L, 0L);

        var liked = likeService.likeComment(1L, 20L);
        var unliked = likeService.unlikeComment(1L, 20L);

        verify(commentLikeRepository).save(any(CommunityCommentLike.class));
        verify(commentLikeRepository).deleteByCommentIdAndUserId(20L, 1L);
        assertThat(liked.likedByMe()).isTrue();
        assertThat(unliked.likedByMe()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 대상에는 좋아요를 등록할 수 없다")
    void rejectMissingTarget() {
        when(postRepository.findByIdForUpdate(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.likePost(1L, 10L))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.COMMUNITY_POST_NOT_FOUND);
        verify(postLikeRepository, never()).save(any());
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 대상 조회 전에 거절한다")
    void rejectUnauthenticatedUser() {
        assertThatThrownBy(() -> likeService.likeComment(null, 20L))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.UNAUTHORIZED);
        verify(commentRepository, never()).findByIdForUpdate(any());
    }

    private CommunityPost post() {
        return CommunityPost.create(1L, "제목", "본문", PostTheme.PROPOSAL, false);
    }

    private CommunityComment comment() {
        return CommunityComment.createComment(10L, 1L, "댓글", false);
    }
}
