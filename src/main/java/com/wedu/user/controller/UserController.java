package com.wedu.user.controller;

import com.wedu.global.response.ApiResponse;
import com.wedu.user.dto.UpdateProfileRequest;
import com.wedu.user.dto.UserProfileResponse;
import com.wedu.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사용자 프로필/온보딩 API (마이페이지 018, 온보딩 002).
 *
 * <p>{@code userId} 는 JWT 인증에서 채워진 principal 에서 가져온다
 * ({@link com.wedu.global.security.jwt.JwtAuthenticationFilter}). 컨트롤러는 요청/응답 변환과
 * 서비스 위임만 하고 비즈니스 로직을 두지 않는다.
 */
@Tag(name = "User", description = "사용자 프로필·온보딩")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 프로필 조회")
    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getMyProfile(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(userService.getProfile(userId));
    }

    @Operation(summary = "온보딩 완료")
    @PostMapping("/me/onboarding")
    public ApiResponse<Void> completeOnboarding(@AuthenticationPrincipal Long userId) {
        userService.completeOnboarding(userId);
        return ApiResponse.ok();
    }

    @Operation(summary = "내 프로필 수정")
    @PatchMapping("/me")
    public ApiResponse<UserProfileResponse> updateMyProfile(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok(
                userService.updateProfile(userId, request.nickname(), request.profileImageUrl()));
    }
}
