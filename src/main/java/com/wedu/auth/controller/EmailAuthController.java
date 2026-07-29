package com.wedu.auth.controller;

import com.wedu.auth.dto.EmailAuthResponse;
import com.wedu.auth.dto.EmailLoginRequest;
import com.wedu.auth.dto.EmailSignupRequest;
import com.wedu.auth.service.EmailAuthService;
import com.wedu.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class EmailAuthController {

    private final EmailAuthService emailAuthService;

    @Operation(summary = "이메일 회원가입")
    @PostMapping("/signup")
    public ApiResponse<EmailAuthResponse> signup(
            @Valid @RequestBody EmailSignupRequest request) {
        return ApiResponse.ok(emailAuthService.signup(request));
    }

    @Operation(summary = "이메일 로그인")
    @PostMapping("/login")
    public ApiResponse<EmailAuthResponse> login(
            @Valid @RequestBody EmailLoginRequest request) {
        return ApiResponse.ok(emailAuthService.login(request));
    }
}
