package com.wedu.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wedu.auth.dto.EmailAuthResponse;
import com.wedu.auth.service.EmailAuthService;
import com.wedu.global.config.SecurityConfig;
import com.wedu.global.security.jwt.JwtTokenProvider;
import com.wedu.global.security.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import com.wedu.global.security.oauth.OAuth2FailureHandler;
import com.wedu.global.security.oauth.OAuth2SuccessHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EmailAuthController.class)
@Import(SecurityConfig.class)
class EmailAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailAuthService emailAuthService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockBean
    private OAuth2FailureHandler oAuth2FailureHandler;

    @MockBean
    private HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Test
    @DisplayName("이메일 회원가입 후 JWT를 반환한다")
    void signup() throws Exception {
        when(emailAuthService.signup(any()))
                .thenReturn(new EmailAuthResponse(
                        "Bearer", "access-token", 1L, "bride@example.com", "신부", false));

        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "신부",
                                  "email": "bride@example.com",
                                  "password": "secret1",
                                  "passwordConfirm": "secret1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.email").value("bride@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("신부"))
                .andExpect(jsonPath("$.data.onboardingCompleted").value(false));
    }

    @Test
    @DisplayName("이메일 로그인 후 JWT를 반환한다")
    void login() throws Exception {
        when(emailAuthService.login(any()))
                .thenReturn(new EmailAuthResponse(
                        "Bearer", "login-token", 2L, "bride@example.com", "신부", true));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "bride@example.com",
                                  "password": "secret1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("login-token"))
                .andExpect(jsonPath("$.data.userId").value(2))
                .andExpect(jsonPath("$.data.onboardingCompleted").value(true));
    }

    @Test
    @DisplayName("회원가입 요청 형식이 잘못되면 거부한다")
    void rejectInvalidSignupRequest() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": " ",
                                  "email": "not-email",
                                  "password": "123",
                                  "passwordConfirm": "123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_400"));
    }
}
