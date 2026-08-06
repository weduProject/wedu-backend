package com.wedu.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wedu.community.domain.CommunityPost;
import com.wedu.community.domain.CommunityPostSort;
import com.wedu.community.domain.PostTheme;
import com.wedu.community.dto.CommunityPostCreateRequest;
import com.wedu.community.dto.CommunityPostUpdateRequest;
import com.wedu.community.repository.CommunityCommentCountProjection;
import com.wedu.community.repository.CommunityCommentLikeRepository;
import com.wedu.community.repository.CommunityCommentRepository;
import com.wedu.community.repository.CommunityPostLikeCountProjection;
import com.wedu.community.repository.CommunityPostLikeRepository;
import com.wedu.community.repository.CommunityPostRepository;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CommunityPostServiceTest {

    @Mock
    private CommunityPostRepository communityPostRepository;

    @Mock
    private UserService userService;

    @Mock
    private CommunityCommentRepository communityCommentRepository;

    @Mock
    private CommunityPostLikeRepository postLikeRepository;

    @Mock
    private CommunityCommentLikeRepository commentLikeRepository;

    private CommunityPostService communityPostService;

    @BeforeEach
    void setUp() {
        communityPostService = new CommunityPostService(
                communityPostRepository,
                communityCommentRepository,
                postLikeRepository,
                commentLikeRepository,
                userService);
    }

    @Test
    @DisplayName("비익명 게시글을 생성하면 공개 작성자 정보를 반환한다")
    void createPublicPost() {
        when(communityPostRepository.save(any(CommunityPost.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(userService.getPublicProfile(1L))
                .thenReturn(new UserPublicProfileResponse(1L, "예비신랑", "https://img/profile.png"));

        var response = communityPostService.create(
                1L,
                new CommunityPostCreateRequest(
                        "예식장 질문", "계약 전 확인할 점이 궁금합니다.", PostTheme.WEDDING_PREPARATION, false));

        assertThat(response.author().nickname()).isEqualTo("예비신랑");
        assertThat(response.anonymous()).isFalse();
        assertThat(response.isMine()).isTrue();
    }

    @Test
    @DisplayName("익명 게시글은 작성자 프로필을 조회하거나 노출하지 않는다")
    void createAnonymousPost() {
        when(communityPostRepository.save(any(CommunityPost.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = communityPostService.create(
                1L,
                new CommunityPostCreateRequest(
                        "고민 상담", "결혼 준비가 어렵습니다.", PostTheme.CONCERN_COUNSELING, true));

        assertThat(response.author().userId()).isNull();
        assertThat(response.author().nickname()).isEqualTo("익명");
        verify(userService, never()).getPublicProfile(any());
    }

    @Test
    @DisplayName("테마와 제목·본문 키워드로 최신 게시글을 페이징 조회한다")
    void search() {
        CommunityPost post = CommunityPost.create(
                2L, "웨딩 준비", "예식장 계약 팁", PostTheme.WEDDING_PREPARATION, false);
        ReflectionTestUtils.setField(post, "id", 10L);
        CommunityCommentCountProjection count = mock(CommunityCommentCountProjection.class);
        CommunityPostLikeCountProjection likeCount = mock(CommunityPostLikeCountProjection.class);
        when(count.getPostId()).thenReturn(10L);
        when(count.getCommentCount()).thenReturn(3L);
        when(likeCount.getPostId()).thenReturn(10L);
        when(likeCount.getLikeCount()).thenReturn(5L);
        when(communityPostRepository.search(
                        eq(PostTheme.WEDDING_PREPARATION), eq("예식!%"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(post)));
        when(userService.getPublicProfiles(Set.of(2L)))
                .thenReturn(Map.of(2L, new UserPublicProfileResponse(2L, "정보왕", null)));
        when(communityCommentRepository.countByPostIds(List.of(10L)))
                .thenReturn(List.of(count));
        when(postLikeRepository.countByPostIds(List.of(10L))).thenReturn(List.of(likeCount));
        when(postLikeRepository.findLikedPostIds(1L, List.of(10L))).thenReturn(List.of(10L));

        var result = communityPostService.search(
                1L,
                PostTheme.WEDDING_PREPARATION,
                " 예식% ",
                CommunityPostSort.LATEST,
                0,
                20);

        assertThat(result.posts()).hasSize(1);
        assertThat(result.posts().getFirst().author().nickname()).isEqualTo("정보왕");
        assertThat(result.posts().getFirst().commentCount()).isEqualTo(3L);
        assertThat(result.posts().getFirst().likeCount()).isEqualTo(5L);
        assertThat(result.posts().getFirst().likedByMe()).isTrue();
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("게시글 상세 조회는 실제 댓글 수를 반환한다")
    void getDetailWithCommentCount() {
        CommunityPost post = CommunityPost.create(
                2L, "제목", "본문", PostTheme.PROPOSAL, false);
        ReflectionTestUtils.setField(post, "id", 10L);
        when(communityPostRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userService.getPublicProfile(2L))
                .thenReturn(new UserPublicProfileResponse(2L, "작성자", null));
        when(communityCommentRepository.countByPostId(10L)).thenReturn(4L);
        when(postLikeRepository.countByPostId(10L)).thenReturn(6L);
        when(postLikeRepository.existsByPostIdAndUserId(10L, 1L)).thenReturn(true);

        var response = communityPostService.getDetail(1L, 10L);

        assertThat(response.commentCount()).isEqualTo(4L);
        assertThat(response.likeCount()).isEqualTo(6L);
        assertThat(response.likedByMe()).isTrue();
    }

    @Test
    @DisplayName("작성자는 게시글을 수정하고 삭제할 수 있다")
    void updateAndDelete() {
        CommunityPost post = CommunityPost.create(
                1L, "제목", "본문", PostTheme.PROPOSAL, false);
        when(communityPostRepository.findById(10L)).thenReturn(Optional.of(post));
        when(communityPostRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(post));
        when(userService.getPublicProfile(1L))
                .thenReturn(new UserPublicProfileResponse(1L, "작성자", null));

        var updated = communityPostService.update(
                1L,
                10L,
                new CommunityPostUpdateRequest(
                        "수정 제목", "수정 본문", PostTheme.TIP_SHARING, false));
        communityPostService.delete(1L, 10L);

        assertThat(updated.title()).isEqualTo("수정 제목");
        verify(commentLikeRepository).deleteByPostId(10L);
        verify(communityCommentRepository).deleteByPostId(10L);
        verify(postLikeRepository).deleteByPostId(10L);
        verify(communityPostRepository).findByIdForUpdate(10L);
        verify(communityPostRepository).delete(post);
    }

    @Test
    @DisplayName("다른 사용자의 게시글은 수정하거나 삭제할 수 없다")
    void rejectUnownedPost() {
        CommunityPost post = CommunityPost.create(
                2L, "제목", "본문", PostTheme.PROPOSAL, false);
        when(communityPostRepository.findById(10L)).thenReturn(Optional.of(post));
        when(communityPostRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> communityPostService.update(
                        1L,
                        10L,
                        new CommunityPostUpdateRequest(
                                "수정", "본문", PostTheme.PROPOSAL, false)))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.COMMUNITY_POST_FORBIDDEN);
        assertThatThrownBy(() -> communityPostService.delete(1L, 10L))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.COMMUNITY_POST_FORBIDDEN);
    }
}
