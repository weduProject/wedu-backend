package com.wedu.community.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wedu.community.dto.CommunityCommentCreateRequest;
import com.wedu.community.dto.CommunityCommentPageResponse;
import com.wedu.community.dto.CommunityCommentResponse;
import com.wedu.community.dto.CommunityCommentSummaryResponse;
import com.wedu.community.dto.CommunityCommentUpdateRequest;
import com.wedu.community.dto.CommunityCommentAuthorResponse;
import com.wedu.community.dto.CommunityReplyPageResponse;
import com.wedu.community.service.CommunityCommentService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CommunityCommentController.class)
class CommunityCommentControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private CommunityCommentService commentService;

    private final UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                    1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

    @Test
    @DisplayName("댓글과 답글을 생성한다")
    void createCommentAndReply() throws Exception {
        when(commentService.create(eq(1L), eq(10L), any(CommunityCommentCreateRequest.class)))
                .thenReturn(response(null));
        when(commentService.createReply(
                        eq(1L), eq(10L), eq(20L), any(CommunityCommentCreateRequest.class)))
                .thenReturn(response(20L));

        mockMvc.perform(post("/api/community/posts/10/comments")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("댓글"));
        mockMvc.perform(post("/api/community/posts/10/comments/20/replies")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commentId").value(30))
                .andExpect(jsonPath("$.data.parentId").value(20));
    }

    @Test
    @DisplayName("댓글 목록을 조회하고 댓글을 수정·삭제한다")
    void listUpdateDelete() throws Exception {
        when(commentService.getComments(1L, 10L, 0, 20))
                .thenReturn(new CommunityCommentPageResponse(
                        List.of(summaryResponse()), 0, 20, 1, 1, false));
        when(commentService.update(eq(1L), eq(30L), any(CommunityCommentUpdateRequest.class)))
                .thenReturn(response(null));
        when(commentService.getReplies(1L, 30L, 0, 20))
                .thenReturn(new CommunityReplyPageResponse(
                        List.of(response(30L)), 0, 20, 1, 1, false));
        doNothing().when(commentService).delete(1L, 30L);

        mockMvc.perform(get("/api/community/posts/10/comments")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.comments.length()").value(1));
        mockMvc.perform(get("/api/community/comments/30/replies")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.replies.length()").value(1));
        mockMvc.perform(put("/api/community/comments/30")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body()))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/community/comments/30")
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("비어 있는 댓글과 익명 여부가 없는 요청을 거절한다")
    void rejectInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/community/posts/10/comments")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_400"));
    }

    private String body() {
        return "{\"content\":\"댓글\",\"anonymous\":false}";
    }

    private CommunityCommentResponse response(Long parentId) {
        OffsetDateTime now = OffsetDateTime.parse("2026-08-05T01:00:00Z");
        return new CommunityCommentResponse(
                30L,
                10L,
                parentId,
                "댓글",
                false,
                new CommunityCommentAuthorResponse(1L, "작성자", null),
                true,
                true,
                0,
                false,
                now,
                now);
    }

    private CommunityCommentSummaryResponse summaryResponse() {
        CommunityCommentResponse response = response(null);
        return new CommunityCommentSummaryResponse(
                response.commentId(),
                response.postId(),
                response.content(),
                response.anonymous(),
                response.author(),
                response.isMine(),
                response.isPostAuthor(),
                response.likeCount(),
                response.likedByMe(),
                0,
                response.createdAt(),
                response.updatedAt());
    }

}
