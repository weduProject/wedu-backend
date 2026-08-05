package com.wedu.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wedu.community.domain.CommunityPost;
import com.wedu.community.domain.PostTheme;
import com.wedu.community.repository.CommunityPostRepository;
import com.wedu.user.domain.Nickname;
import com.wedu.user.domain.SocialProvider;
import com.wedu.user.domain.User;
import com.wedu.user.repository.UserRepository;
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
class CommunityPostIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CommunityPostRepository communityPostRepository;

    @Autowired
    private UserRepository userRepository;

    private Long userId;
    private Long otherUserId;

    @BeforeEach
    void setUp() {
        communityPostRepository.deleteAll();
        userRepository.deleteAll();
        userId = saveUser("author-1", "author@example.com", "예비신랑").getId();
        otherUserId = saveUser("author-2", "other@example.com", "다른사용자").getId();
    }

    @Test
    @DisplayName("HTTP로 게시글 CRUD와 검색·테마 필터·익명 처리를 수행한다")
    void managePostsThroughHttp() throws Exception {
        create("예식장 계약 질문", "숨은 비용이 궁금합니다.", "WEDDING_PREPARATION", false);
        create("신혼집 고민", "지역 추천 부탁드립니다.", "NEWLYWED_LIFE", true);
        communityPostRepository.save(CommunityPost.create(
                otherUserId, "다른 글", "검색에서 함께 노출되면 안 됩니다.", PostTheme.PROPOSAL, false));

        mockMvc.perform(get("/api/community/posts")
                        .param("theme", "WEDDING_PREPARATION")
                        .param("keyword", "숨은 비용")
                        .with(authentication(authenticationToken(userId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.posts.length()").value(1))
                .andExpect(jsonPath("$.data.posts[0].author.nickname").value("예비신랑"));

        CommunityPost anonymous = communityPostRepository.findAll().stream()
                .filter(CommunityPost::isAnonymous)
                .findFirst()
                .orElseThrow();
        mockMvc.perform(get("/api/community/posts/{postId}", anonymous.getId())
                        .with(authentication(authenticationToken(otherUserId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.author.userId").doesNotExist())
                .andExpect(jsonPath("$.data.author.nickname").value("익명"))
                .andExpect(jsonPath("$.data.isMine").value(false));

        mockMvc.perform(put("/api/community/posts/{postId}", anonymous.getId())
                        .with(authentication(authenticationToken(userId)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"수정된 고민","content":"내용 수정","theme":"CONCERN_COUNSELING","anonymous":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("수정된 고민"));

        mockMvc.perform(delete("/api/community/posts/{postId}", anonymous.getId())
                        .with(authentication(authenticationToken(userId)))
                        .with(csrf()))
                .andExpect(status().isOk());
        assertThat(communityPostRepository.findById(anonymous.getId())).isEmpty();
    }

    @Test
    @DisplayName("비로그인 사용자는 게시글을 조회할 수 없다")
    void requireAuthentication() throws Exception {
        mockMvc.perform(get("/api/community/posts"))
                .andExpect(status().isUnauthorized());
    }

    private void create(String title, String content, String theme, boolean anonymous) throws Exception {
        mockMvc.perform(post("/api/community/posts")
                        .with(authentication(authenticationToken(userId)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"%s","content":"%s","theme":"%s","anonymous":%s}
                                """.formatted(title, content, theme, anonymous)))
                .andExpect(status().isOk());
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
