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

import com.wedu.community.domain.PostTheme;
import com.wedu.community.dto.CommunityPostAuthorResponse;
import com.wedu.community.dto.CommunityPostCreateRequest;
import com.wedu.community.dto.CommunityPostDetailResponse;
import com.wedu.community.dto.CommunityPostPageResponse;
import com.wedu.community.dto.CommunityPostSummaryResponse;
import com.wedu.community.dto.CommunityPostUpdateRequest;
import com.wedu.community.service.CommunityPostService;
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

@WebMvcTest(CommunityPostController.class)
class CommunityPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommunityPostService communityPostService;

    private final UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                    1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

    @Test
    @DisplayName("게시글을 생성한다")
    void create() throws Exception {
        when(communityPostService.create(eq(1L), any(CommunityPostCreateRequest.class)))
                .thenReturn(detail());

        mockMvc.perform(post("/api/community/posts")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postId").value(10))
                .andExpect(jsonPath("$.data.theme").value("WEDDING_PREPARATION"));
    }

    @Test
    @DisplayName("잘못된 게시글 생성 요청을 거절한다")
    void rejectInvalidCreate() throws Exception {
        mockMvc.perform(post("/api/community/posts")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\" \",\"content\":\"본문\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_400"));
    }

    @Test
    @DisplayName("정규화 후 코드 포인트 길이가 경계값인 생성 요청은 검증을 통과한다")
    void acceptNormalizedCodePointLimit() throws Exception {
        when(communityPostService.create(eq(1L), any(CommunityPostCreateRequest.class)))
                .thenReturn(detail());

        mockMvc.perform(post("/api/community/posts")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(normalizedLimitBody()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("정규화 후 코드 포인트 길이가 경계값인 수정 요청은 검증을 통과한다")
    void acceptNormalizedCodePointLimitOnUpdate() throws Exception {
        when(communityPostService.update(eq(1L), eq(10L), any(CommunityPostUpdateRequest.class)))
                .thenReturn(detail());

        mockMvc.perform(put("/api/community/posts/10")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(normalizedLimitBody()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("테마와 키워드로 게시글 목록을 조회한다")
    void search() throws Exception {
        when(communityPostService.search(
                        1L, PostTheme.WEDDING_PREPARATION, "예식장", 0, 20))
                .thenReturn(new CommunityPostPageResponse(
                        List.of(summary()), 0, 20, 1, 1, false));

        mockMvc.perform(get("/api/community/posts")
                        .param("theme", "WEDDING_PREPARATION")
                        .param("keyword", "예식장")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.posts.length()").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("게시글 상세를 조회하고 수정·삭제한다")
    void detailUpdateDelete() throws Exception {
        when(communityPostService.getDetail(1L, 10L)).thenReturn(detail());
        when(communityPostService.update(eq(1L), eq(10L), any(CommunityPostUpdateRequest.class)))
                .thenReturn(detail());
        doNothing().when(communityPostService).delete(1L, 10L);

        mockMvc.perform(get("/api/community/posts/10")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("예식장 계약 팁을 알려주세요."));
        mockMvc.perform(put("/api/community/posts/10")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body()))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/community/posts/10")
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    private String body() {
        return """
                {
                  "title":"예식장 질문",
                  "content":"예식장 계약 팁을 알려주세요.",
                  "theme":"WEDDING_PREPARATION",
                  "anonymous":false
                }
                """;
    }

    private String normalizedLimitBody() {
        return """
                {"title":"  %s  ","content":"  %s  ","theme":"TIP_SHARING","anonymous":false}
                """.formatted("😀".repeat(100), "😀".repeat(5000));
    }

    private CommunityPostSummaryResponse summary() {
        return new CommunityPostSummaryResponse(
                10L,
                "예식장 질문",
                "예식장 계약 팁을 알려주세요.",
                PostTheme.WEDDING_PREPARATION,
                false,
                new CommunityPostAuthorResponse(1L, "예비신랑", null),
                true,
                0,
                0,
                OffsetDateTime.parse("2026-08-05T01:00:00Z"));
    }

    private CommunityPostDetailResponse detail() {
        CommunityPostSummaryResponse summary = summary();
        return new CommunityPostDetailResponse(
                summary.postId(), summary.title(), summary.content(), summary.theme(),
                summary.anonymous(), summary.author(), summary.isMine(), summary.likeCount(),
                summary.commentCount(), summary.createdAt(), summary.createdAt());
    }
}
