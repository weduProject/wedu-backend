package com.wedu.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wedu.community.domain.CommunityComment;
import com.wedu.community.domain.CommunityPost;
import com.wedu.community.domain.PostTheme;
import com.wedu.community.repository.CommunityCommentRepository;
import com.wedu.community.repository.CommunityPostRepository;
import com.wedu.user.domain.Nickname;
import com.wedu.user.domain.SocialProvider;
import com.wedu.user.domain.User;
import com.wedu.user.repository.UserRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CommunityCommentIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private CommunityCommentRepository commentRepository;
    @Autowired private CommunityPostRepository postRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private Clock clock;

    private Long authorId;
    private Long commenterId;
    private Long postId;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
        authorId = saveUser("post-author", "post-author@example.com", "글쓴이").getId();
        commenterId = saveUser("commenter", "commenter@example.com", "댓글러").getId();
        postId = postRepository.save(CommunityPost.create(
                authorId, "제목", "본문", PostTheme.WEDDING_PREPARATION, false)).getId();
    }

    @Test
    @DisplayName("HTTP로 익명 답글을 포함한 댓글 CRUD를 수행하고 게시글 댓글 수를 갱신한다")
    void manageCommentsThroughHttp() throws Exception {
        mockMvc.perform(post("/api/community/posts/{postId}/comments", postId)
                        .with(authentication(authenticationToken(commenterId)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"첫 댓글\",\"anonymous\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.author.nickname").value("댓글러"))
                .andExpect(jsonPath("$.data.createdAt", endsWith("Z")));

        CommunityComment parent = commentRepository.findAll().getFirst();
        LocalDateTime now = LocalDateTime.now(clock);
        assertThat(parent.getCreatedAt()).isBetween(now.minusSeconds(5), now.plusSeconds(5));
        mockMvc.perform(post(
                                "/api/community/posts/{postId}/comments/{commentId}/replies",
                                postId,
                                parent.getId())
                        .with(authentication(authenticationToken(authorId)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"글쓴이 답글\",\"anonymous\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commentId").isNumber())
                .andExpect(jsonPath("$.data.parentId").value(parent.getId()))
                .andExpect(jsonPath("$.data.author.nickname").value("익명"))
                .andExpect(jsonPath("$.data.isPostAuthor").value(true));

        CommunityComment reply = commentRepository.findAll().stream()
                .filter(CommunityComment::isReply)
                .findFirst()
                .orElseThrow();

        mockMvc.perform(get("/api/community/posts/{postId}/comments", postId)
                        .with(authentication(authenticationToken(commenterId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.comments.length()").value(1))
                .andExpect(jsonPath("$.data.comments[0].replyCount").value(1));
        mockMvc.perform(get("/api/community/comments/{commentId}/replies", parent.getId())
                        .with(authentication(authenticationToken(commenterId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.replies.length()").value(1))
                .andExpect(jsonPath("$.data.replies[0].isPostAuthor").value(true));
        mockMvc.perform(get("/api/community/posts/{postId}", postId)
                        .with(authentication(authenticationToken(commenterId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commentCount").value(2));
        mockMvc.perform(get("/api/community/posts")
                        .with(authentication(authenticationToken(commenterId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.posts[0].commentCount").value(2));

        mockMvc.perform(put("/api/community/comments/{commentId}", parent.getId())
                        .with(authentication(authenticationToken(commenterId)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"수정 댓글\",\"anonymous\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("수정 댓글"));
        mockMvc.perform(put("/api/community/comments/{commentId}", reply.getId())
                        .with(authentication(authenticationToken(authorId)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"수정 답글\",\"anonymous\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commentId").value(reply.getId()))
                .andExpect(jsonPath("$.data.parentId").value(parent.getId()));

        mockMvc.perform(delete("/api/community/comments/{commentId}", parent.getId())
                        .with(authentication(authenticationToken(commenterId)))
                        .with(csrf()))
                .andExpect(status().isOk());
        assertThat(commentRepository.countByPostId(postId)).isZero();
    }

    @Test
    @DisplayName("게시글을 삭제하면 그 게시글의 댓글과 답글도 함께 삭제한다")
    void cascadeCommentsWhenDeletingPost() throws Exception {
        CommunityComment parent = commentRepository.save(
                CommunityComment.createComment(postId, commenterId, "부모", false));
        commentRepository.save(
                CommunityComment.createReply(postId, authorId, parent.getId(), "답글", false));

        mockMvc.perform(delete("/api/community/posts/{postId}", postId)
                        .with(authentication(authenticationToken(authorId)))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertThat(commentRepository.countByPostId(postId)).isZero();
        assertThat(postRepository.findById(postId)).isEmpty();
    }

    @Test
    @DisplayName("다른 사용자의 댓글 수정과 답글의 답글 생성을 거절한다")
    void enforceOwnershipAndReplyDepth() throws Exception {
        CommunityComment parent = commentRepository.save(
                CommunityComment.createComment(postId, commenterId, "부모", false));
        CommunityComment reply = commentRepository.save(
                CommunityComment.createReply(postId, authorId, parent.getId(), "답글", false));

        mockMvc.perform(put("/api/community/comments/{commentId}", parent.getId())
                        .with(authentication(authenticationToken(authorId)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"수정\",\"anonymous\":false}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("COMMUNITY_COMMENT_403"));
        mockMvc.perform(post(
                                "/api/community/posts/{postId}/comments/{commentId}/replies",
                                postId,
                                reply.getId())
                        .with(authentication(authenticationToken(commenterId)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"중첩 답글\",\"anonymous\":false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMUNITY_COMMENT_400_REPLY"));
    }

    @Test
    @DisplayName("답글 하나를 삭제해도 부모 댓글은 유지한다")
    void deleteOnlyReply() throws Exception {
        CommunityComment parent = commentRepository.save(
                CommunityComment.createComment(postId, commenterId, "부모", false));
        CommunityComment reply = commentRepository.save(
                CommunityComment.createReply(postId, authorId, parent.getId(), "답글", false));

        mockMvc.perform(delete("/api/community/comments/{commentId}", reply.getId())
                        .with(authentication(authenticationToken(authorId)))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertThat(commentRepository.findById(parent.getId())).isPresent();
        assertThat(commentRepository.findById(reply.getId())).isEmpty();
    }

    @Test
    @DisplayName("비로그인 사용자는 댓글과 답글을 조회할 수 없다")
    void requireAuthentication() throws Exception {
        mockMvc.perform(get("/api/community/posts/{postId}/comments", postId))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/community/comments/1/replies"))
                .andExpect(status().isUnauthorized());
    }

    private UsernamePasswordAuthenticationToken authenticationToken(Long id) {
        return new UsernamePasswordAuthenticationToken(
                id, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private User saveUser(String socialId, String email, String nickname) {
        return userRepository.save(User.register(
                SocialProvider.KAKAO, socialId, email, new Nickname(nickname), null));
    }
}
