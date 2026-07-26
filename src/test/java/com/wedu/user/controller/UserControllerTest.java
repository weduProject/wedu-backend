package com.wedu.user.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.user.dto.UserProfileResponse;
import com.wedu.user.service.UserService;
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

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private final UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                    1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

    private UserProfileResponse profile(boolean onboardingCompleted) {
        return new UserProfileResponse(
                1L, "KAKAO", "wedu@example.com", "완규", null, onboardingCompleted);
    }

    @Test
    @DisplayName("GET /api/users/me 로 온보딩 여부를 확인할 수 있다")
    void getMyProfileExposesOnboardingFlag() throws Exception {
        when(userService.getProfile(1L)).thenReturn(profile(false));

        mockMvc.perform(get("/api/users/me").with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.onboardingCompleted").value(false))
                .andExpect(jsonPath("$.data.nickname").value("완규"));
    }

    @Test
    @DisplayName("POST /api/users/me/onboarding 으로 온보딩을 완료한다")
    void completeOnboarding() throws Exception {
        when(userService.completeOnboarding(1L)).thenReturn(profile(true));

        mockMvc.perform(post("/api/users/me/onboarding")
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.onboardingCompleted").value(true));
    }

    @Test
    @DisplayName("이미 온보딩한 사용자는 USER_409 로 거절한다")
    void rejectDuplicateOnboarding() throws Exception {
        when(userService.completeOnboarding(1L))
                .thenThrow(new BusinessException(ErrorCode.USER_ALREADY_ONBOARDED));

        mockMvc.perform(post("/api/users/me/onboarding")
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("USER_409"));
    }

    @Test
    @DisplayName("온보딩 중 닉네임은 PATCH /api/users/me 로 설정한다")
    void updateProfileDuringOnboarding() throws Exception {
        when(userService.updateProfile(eq(1L), eq("새닉네임"), isNull()))
                .thenReturn(new UserProfileResponse(
                        1L, "KAKAO", "wedu@example.com", "새닉네임", null, false));

        mockMvc.perform(patch("/api/users/me")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname":"새닉네임"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("새닉네임"))
                .andExpect(jsonPath("$.data.onboardingCompleted").value(false));
    }
}
