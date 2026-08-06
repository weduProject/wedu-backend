package com.wedu.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wedu.community.domain.CommunityComment;
import com.wedu.community.domain.CommunityPost;
import com.wedu.community.domain.PostTheme;
import com.wedu.community.repository.CommunityCommentLikeRepository;
import com.wedu.community.repository.CommunityCommentRepository;
import com.wedu.community.repository.CommunityPostLikeRepository;
import com.wedu.community.repository.CommunityPostRepository;
import com.wedu.user.domain.Nickname;
import com.wedu.user.domain.SocialProvider;
import com.wedu.user.domain.User;
import com.wedu.user.repository.UserRepository;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CommunityLikeIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private CommunityPostRepository postRepository;
    @Autowired private CommunityCommentRepository commentRepository;
    @Autowired private CommunityPostLikeRepository postLikeRepository;
    @Autowired private CommunityCommentLikeRepository commentLikeRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CommunityLikeService likeService;

    private Long authorId;
    private Long viewerId;
    private Long otherUserId;
    private CommunityPost post;
    private CommunityComment comment;
    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        commentLikeRepository.deleteAll();
        postLikeRepository.deleteAll();
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
        authorId = saveUser("like-author", "like-author@example.com", "글쓴이").getId();
        viewerId = saveUser("like-viewer", "like-viewer@example.com", "조회자").getId();
        otherUserId = saveUser("like-other", "like-other@example.com", "다른사용자").getId();
        post = postRepository.save(CommunityPost.create(
                authorId, "첫 게시글", "첫 본문", PostTheme.PROPOSAL, false));
        comment = commentRepository.save(
                CommunityComment.createComment(post.getId(), authorId, "첫 댓글", false));
        executor = Executors.newFixedThreadPool(2);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        executor.shutdownNow();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("게시글과 댓글 좋아요를 멱등하게 변경하고 조회 응답에 반영한다")
    void manageLikesAndExposeThemInResponses() throws Exception {
        CommunityComment reply = commentRepository.save(CommunityComment.createReply(
                post.getId(), otherUserId, comment.getId(), "첫 답글", false));
        likePost(post.getId(), viewerId, 1);
        likePost(post.getId(), viewerId, 1);
        likePost(post.getId(), otherUserId, 2);
        likeComment(comment.getId(), viewerId, 1);
        likeComment(reply.getId(), viewerId, 1);

        assertThat(postLikeRepository.countByPostId(post.getId())).isEqualTo(2L);
        assertThat(commentLikeRepository.countByCommentId(comment.getId())).isEqualTo(1L);

        mockMvc.perform(get("/api/community/posts/{postId}", post.getId())
                        .with(authentication(authenticationToken(viewerId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likeCount").value(2))
                .andExpect(jsonPath("$.data.likedByMe").value(true));
        mockMvc.perform(get("/api/community/posts/{postId}/comments", post.getId())
                        .with(authentication(authenticationToken(viewerId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.comments[0].likeCount").value(1))
                .andExpect(jsonPath("$.data.comments[0].likedByMe").value(true));
        mockMvc.perform(get("/api/community/comments/{commentId}/replies", comment.getId())
                        .with(authentication(authenticationToken(viewerId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.replies[0].likeCount").value(1))
                .andExpect(jsonPath("$.data.replies[0].likedByMe").value(true));

        mockMvc.perform(delete("/api/community/posts/{postId}/likes", post.getId())
                        .with(authentication(authenticationToken(viewerId)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likeCount").value(1))
                .andExpect(jsonPath("$.data.likedByMe").value(false));
    }

    @Test
    @DisplayName("게시글 목록을 좋아요 수 내림차순으로 조회한다")
    void sortPostsByLikeCount() throws Exception {
        CommunityPost popular = postRepository.save(CommunityPost.create(
                authorId, "인기 게시글", "인기 본문", PostTheme.PROPOSAL, false));
        likePost(popular.getId(), viewerId, 1);
        likePost(popular.getId(), otherUserId, 2);
        likePost(post.getId(), viewerId, 1);

        mockMvc.perform(get("/api/community/posts")
                        .param("sort", "MOST_LIKED")
                        .with(authentication(authenticationToken(viewerId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.posts[0].postId").value(popular.getId()))
                .andExpect(jsonPath("$.data.posts[0].likeCount").value(2))
                .andExpect(jsonPath("$.data.posts[0].likedByMe").value(true));
    }

    @Test
    @DisplayName("댓글과 게시글 삭제 시 연관 좋아요를 함께 정리한다")
    void deleteLikesWithTargets() throws Exception {
        CommunityComment reply = commentRepository.save(CommunityComment.createReply(
                post.getId(), otherUserId, comment.getId(), "삭제될 답글", false));
        likePost(post.getId(), viewerId, 1);
        likeComment(comment.getId(), viewerId, 1);
        likeComment(reply.getId(), viewerId, 1);

        mockMvc.perform(delete("/api/community/comments/{commentId}", comment.getId())
                        .with(authentication(authenticationToken(authorId)))
                        .with(csrf()))
                .andExpect(status().isOk());
        assertThat(commentLikeRepository.countByCommentId(comment.getId())).isZero();
        assertThat(commentLikeRepository.countByCommentId(reply.getId())).isZero();

        CommunityComment remainingComment = commentRepository.save(
                CommunityComment.createComment(post.getId(), otherUserId, "남은 댓글", false));
        likeComment(remainingComment.getId(), viewerId, 1);
        mockMvc.perform(delete("/api/community/posts/{postId}", post.getId())
                        .with(authentication(authenticationToken(authorId)))
                        .with(csrf()))
                .andExpect(status().isOk());
        assertThat(postLikeRepository.countByPostId(post.getId())).isZero();
        assertThat(commentLikeRepository.countByCommentId(remainingComment.getId())).isZero();
    }

    @Test
    @DisplayName("비로그인 사용자는 좋아요를 변경할 수 없다")
    void requireAuthentication() throws Exception {
        mockMvc.perform(post("/api/community/posts/{postId}/likes", post.getId()).with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("같은 사용자가 게시글에 동시에 좋아요해도 하나만 저장한다")
    void likePostConcurrently() throws Exception {
        CyclicBarrier barrier = new CyclicBarrier(2);
        Callable<Long> like = () -> {
            barrier.await(5, TimeUnit.SECONDS);
            return likeService.likePost(viewerId, post.getId()).likeCount();
        };

        Future<Long> first = executor.submit(like);
        Future<Long> second = executor.submit(like);

        assertThat(List.of(
                        first.get(10, TimeUnit.SECONDS),
                        second.get(10, TimeUnit.SECONDS)))
                .containsOnly(1L);
        assertThat(postLikeRepository.countByPostId(post.getId())).isEqualTo(1L);
    }

    @Test
    @DisplayName("같은 사용자가 댓글에 동시에 좋아요해도 하나만 저장한다")
    void likeCommentConcurrently() throws Exception {
        CyclicBarrier barrier = new CyclicBarrier(2);
        Callable<Long> like = () -> {
            barrier.await(5, TimeUnit.SECONDS);
            return likeService.likeComment(viewerId, comment.getId()).likeCount();
        };

        Future<Long> first = executor.submit(like);
        Future<Long> second = executor.submit(like);

        assertThat(List.of(
                        first.get(10, TimeUnit.SECONDS),
                        second.get(10, TimeUnit.SECONDS)))
                .containsOnly(1L);
        assertThat(commentLikeRepository.countByCommentId(comment.getId())).isEqualTo(1L);
    }

    private void likePost(Long postId, Long userId, int expectedCount) throws Exception {
        mockMvc.perform(post("/api/community/posts/{postId}/likes", postId)
                        .with(authentication(authenticationToken(userId)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likeCount").value(expectedCount))
                .andExpect(jsonPath("$.data.likedByMe").value(true));
    }

    private void likeComment(Long commentId, Long userId, int expectedCount) throws Exception {
        mockMvc.perform(post("/api/community/comments/{commentId}/likes", commentId)
                        .with(authentication(authenticationToken(userId)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likeCount").value(expectedCount))
                .andExpect(jsonPath("$.data.likedByMe").value(true));
    }

    private UsernamePasswordAuthenticationToken authenticationToken(Long userId) {
        return new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private User saveUser(String socialId, String email, String nickname) {
        return userRepository.save(User.register(
                SocialProvider.KAKAO, socialId, email, new Nickname(nickname), null));
    }
}
