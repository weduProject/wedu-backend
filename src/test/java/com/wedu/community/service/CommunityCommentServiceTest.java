package com.wedu.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.wedu.community.domain.CommunityComment;
import com.wedu.community.domain.CommunityPost;
import com.wedu.community.domain.PostTheme;
import com.wedu.community.dto.CommunityCommentCreateRequest;
import com.wedu.community.dto.CommunityCommentUpdateRequest;
import com.wedu.community.repository.CommunityCommentLikeRepository;
import com.wedu.community.repository.CommunityCommentRepository;
import com.wedu.community.repository.CommunityPostRepository;
import com.wedu.community.repository.CommunityReplyCountProjection;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.user.dto.UserPublicProfileResponse;
import com.wedu.user.service.UserService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CommunityCommentServiceTest {

    @Mock private CommunityCommentRepository commentRepository;
    @Mock private CommunityCommentLikeRepository commentLikeRepository;
    @Mock private CommunityPostRepository postRepository;
    @Mock private UserService userService;

    private CommunityCommentService commentService;

    @BeforeEach
    void setUp() {
        commentService = new CommunityCommentService(
                commentRepository, commentLikeRepository, postRepository, userService);
    }

    @Test
    @DisplayName("익명 댓글을 생성하면 작성자 신원을 공개하지 않는다")
    void createAnonymousComment() {
        CommunityPost post = CommunityPost.create(2L, "제목", "본문", PostTheme.PROPOSAL, false);
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(CommunityComment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = commentService.create(
                1L, 10L, new CommunityCommentCreateRequest("댓글", true));

        assertThat(response.author().userId()).isNull();
        assertThat(response.author().nickname()).isEqualTo("익명");
        assertThat(response.isMine()).isTrue();
        assertThat(response.isPostAuthor()).isFalse();
        verify(userService, never()).getPublicProfile(any());
    }

    @Test
    @DisplayName("게시글 작성자의 익명 답글은 신원 없이 게시글 작성자 여부만 반환한다")
    void createAnonymousReplyByPostAuthor() {
        CommunityPost post = CommunityPost.create(2L, "제목", "본문", PostTheme.PROPOSAL, true);
        CommunityComment parent = CommunityComment.createComment(10L, 1L, "부모", false);
        ReflectionTestUtils.setField(parent, "id", 20L);
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(commentRepository.findById(20L)).thenReturn(Optional.of(parent));
        when(commentRepository.save(any(CommunityComment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = commentService.createReply(
                2L, 10L, 20L, new CommunityCommentCreateRequest("답글", true));

        assertThat(response.parentId()).isEqualTo(20L);
        assertThat(response.author().userId()).isNull();
        assertThat(response.isPostAuthor()).isTrue();
    }

    @Test
    @DisplayName("답글에 다시 답글을 작성할 수 없다")
    void rejectNestedReply() {
        CommunityPost post = CommunityPost.create(1L, "제목", "본문", PostTheme.PROPOSAL, false);
        CommunityComment reply = CommunityComment.createReply(10L, 2L, 20L, "답글", false);
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(commentRepository.findById(30L)).thenReturn(Optional.of(reply));

        assertThatThrownBy(() -> commentService.createReply(
                        1L, 10L, 30L, new CommunityCommentCreateRequest("중첩", false)))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.COMMUNITY_COMMENT_REPLY_NOT_ALLOWED);
    }

    @Test
    @DisplayName("최상위 댓글과 실제 답글 수를 오래된 순으로 조회한다")
    void getComments() {
        CommunityPost post = CommunityPost.create(1L, "제목", "본문", PostTheme.PROPOSAL, false);
        CommunityComment parent = CommunityComment.createComment(10L, 2L, "부모", false);
        ReflectionTestUtils.setField(parent, "id", 20L);
        CommunityReplyCountProjection count = mock(CommunityReplyCountProjection.class);
        when(count.getParentId()).thenReturn(20L);
        when(count.getReplyCount()).thenReturn(1L);
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(commentRepository.findByPostIdAndParentIdIsNullOrderByCreatedAtAscIdAsc(
                        any(Long.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(parent)));
        when(commentRepository.countRepliesByParentIds(any())).thenReturn(List.of(count));
        when(userService.getPublicProfiles(Set.of(2L))).thenReturn(Map.of(
                2L, new UserPublicProfileResponse(2L, "댓글러", null)));

        var response = commentService.getComments(3L, 10L, 0, 20);

        assertThat(response.comments()).hasSize(1);
        assertThat(response.comments().getFirst().replyCount()).isEqualTo(1);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(commentRepository)
                .findByPostIdAndParentIdIsNullOrderByCreatedAtAscIdAsc(
                        eq(10L), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort()).isEqualTo(Sort.unsorted());
    }

    @Test
    @DisplayName("최상위 댓글의 답글을 오래된 순으로 페이징 조회한다")
    void getReplies() {
        CommunityPost post = CommunityPost.create(1L, "제목", "본문", PostTheme.PROPOSAL, false);
        CommunityComment parent = CommunityComment.createComment(10L, 2L, "부모", false);
        CommunityComment reply = CommunityComment.createReply(10L, 1L, 20L, "답글", false);
        ReflectionTestUtils.setField(parent, "id", 20L);
        ReflectionTestUtils.setField(reply, "id", 21L);
        when(commentRepository.findById(20L)).thenReturn(Optional.of(parent));
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(commentRepository.findByParentIdOrderByCreatedAtAscIdAsc(
                        any(Long.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(reply)));
        when(userService.getPublicProfiles(Set.of(1L))).thenReturn(Map.of(
                1L, new UserPublicProfileResponse(1L, "글쓴이", null)));

        var response = commentService.getReplies(3L, 20L, 0, 20);

        assertThat(response.replies()).hasSize(1);
        assertThat(response.replies().getFirst().isPostAuthor()).isTrue();
    }

    @Test
    @DisplayName("답글 ID로 답글 목록을 조회할 수 없다")
    void rejectRepliesLookupWithReplyId() {
        CommunityComment reply = CommunityComment.createReply(10L, 1L, 20L, "답글", false);
        when(commentRepository.findById(21L)).thenReturn(Optional.of(reply));

        assertThatThrownBy(() -> commentService.getReplies(1L, 21L, 0, 20))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.COMMUNITY_COMMENT_PARENT_REQUIRED);
    }

    @Test
    @DisplayName("다른 게시글의 댓글에는 답글을 작성할 수 없다")
    void rejectReplyToCommentFromAnotherPost() {
        CommunityPost post = CommunityPost.create(1L, "제목", "본문", PostTheme.PROPOSAL, false);
        CommunityComment parent = CommunityComment.createComment(11L, 2L, "부모", false);
        ReflectionTestUtils.setField(parent, "id", 20L);
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(commentRepository.findById(20L)).thenReturn(Optional.of(parent));

        assertThatThrownBy(() -> commentService.createReply(
                        1L, 10L, 20L, new CommunityCommentCreateRequest("답글", false)))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.COMMUNITY_COMMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("페이지 크기가 허용 범위를 벗어나면 조회를 거절한다")
    void rejectInvalidPageSize() {
        assertThatThrownBy(() -> commentService.getComments(1L, 10L, 0, 51))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);

        verifyNoInteractions(postRepository, commentRepository, userService);
    }

    @Test
    @DisplayName("페이지 번호와 크기의 하한을 벗어나면 조회를 거절한다")
    void rejectInvalidPageLowerBounds() {
        assertThatThrownBy(() -> commentService.getComments(1L, 10L, -1, 20))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
        assertThatThrownBy(() -> commentService.getComments(1L, 10L, 0, 0))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);

        verifyNoInteractions(postRepository, commentRepository, userService);
    }

    @Test
    @DisplayName("비익명 댓글 작성자 프로필이 누락되면 사용자 없음 오류를 반환한다")
    void rejectMissingPublicProfile() {
        CommunityPost post = CommunityPost.create(1L, "제목", "본문", PostTheme.PROPOSAL, false);
        CommunityComment comment = CommunityComment.createComment(10L, 2L, "댓글", false);
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(commentRepository.findByPostIdAndParentIdIsNullOrderByCreatedAtAscIdAsc(
                        any(Long.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(comment)));
        when(userService.getPublicProfiles(Set.of(2L))).thenReturn(Map.of());

        assertThatThrownBy(() -> commentService.getComments(3L, 10L, 0, 20))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("작성자는 자신의 댓글을 수정할 수 있다")
    void updateOwnedComment() {
        CommunityPost post = CommunityPost.create(2L, "제목", "본문", PostTheme.PROPOSAL, false);
        CommunityComment comment = CommunityComment.createComment(10L, 1L, "댓글", false);
        ReflectionTestUtils.setField(comment, "id", 20L);
        when(commentRepository.findById(20L)).thenReturn(Optional.of(comment));
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        var updated = commentService.update(
                1L, 20L, new CommunityCommentUpdateRequest("수정", true));

        assertThat(updated.content()).isEqualTo("수정");
        assertThat(updated.anonymous()).isTrue();
    }

    @Test
    @DisplayName("작성자가 최상위 댓글을 삭제하면 답글도 함께 삭제한다")
    void deleteOwnedCommentWithReplies() {
        CommunityComment comment = CommunityComment.createComment(10L, 1L, "댓글", false);
        ReflectionTestUtils.setField(comment, "id", 20L);
        when(commentRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(comment));

        commentService.delete(1L, 20L);

        verify(commentRepository).findByIdForUpdate(20L);
        verify(commentLikeRepository).deleteByCommentIdAndReplies(20L);
        verify(commentRepository).deleteByParentId(20L);
        verify(commentRepository).delete(comment);
    }

    @Test
    @DisplayName("다른 사용자의 댓글은 수정할 수 없다")
    void rejectUnownedComment() {
        CommunityComment comment = CommunityComment.createComment(10L, 2L, "댓글", false);
        when(commentRepository.findById(20L)).thenReturn(Optional.of(comment));
        when(commentRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.update(
                        1L, 20L, new CommunityCommentUpdateRequest("수정", false)))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.COMMUNITY_COMMENT_FORBIDDEN);

        assertThatThrownBy(() -> commentService.delete(1L, 20L))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.COMMUNITY_COMMENT_FORBIDDEN);
    }
}
