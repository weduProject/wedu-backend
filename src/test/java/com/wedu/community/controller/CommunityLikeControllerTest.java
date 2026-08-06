package com.wedu.community.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wedu.community.dto.CommunityLikeResponse;
import com.wedu.community.service.CommunityLikeService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CommunityLikeController.class)
class CommunityLikeControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private CommunityLikeService likeService;

    private final UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                    1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

    @Test
    @DisplayName("게시글 좋아요를 등록하고 취소한다")
    void managePostLike() throws Exception {
        when(likeService.likePost(1L, 10L))
                .thenReturn(new CommunityLikeResponse(10L, 3L, true));
        when(likeService.unlikePost(1L, 10L))
                .thenReturn(new CommunityLikeResponse(10L, 2L, false));

        mockMvc.perform(post("/api/community/posts/10/likes")
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetId").value(10))
                .andExpect(jsonPath("$.data.likeCount").value(3))
                .andExpect(jsonPath("$.data.likedByMe").value(true));
        mockMvc.perform(delete("/api/community/posts/10/likes")
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likeCount").value(2))
                .andExpect(jsonPath("$.data.likedByMe").value(false));
    }

    @Test
    @DisplayName("댓글 또는 답글 좋아요를 등록하고 취소한다")
    void manageCommentLike() throws Exception {
        when(likeService.likeComment(1L, 20L))
                .thenReturn(new CommunityLikeResponse(20L, 1L, true));
        when(likeService.unlikeComment(1L, 20L))
                .thenReturn(new CommunityLikeResponse(20L, 0L, false));

        mockMvc.perform(post("/api/community/comments/20/likes")
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetId").value(20))
                .andExpect(jsonPath("$.data.likedByMe").value(true));
        mockMvc.perform(delete("/api/community/comments/20/likes")
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likedByMe").value(false));
    }

    @Test
    @DisplayName("CSRF 토큰이 없으면 좋아요 변경을 거절한다")
    void requireCsrf() throws Exception {
        mockMvc.perform(post("/api/community/posts/10/likes")
                        .with(authentication(authentication)))
                .andExpect(status().isForbidden());
    }
}
