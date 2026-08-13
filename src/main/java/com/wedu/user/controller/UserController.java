package com.wedu.user.controller;

import com.wedu.global.response.ApiResponse;
import com.wedu.user.dto.UpdateProfileRequest;
import com.wedu.user.dto.UserProfileResponse;
import com.wedu.user.service.UserService;
import io.swagger.v3.oas.annotations.Hidden;
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
 *
 * <h2>WEDU-002 프론트 계약</h2>
 * <ol>
 *   <li>소셜 콜백 {@code ?onboardingCompleted=false} 또는 {@code POST /api/auth/oauth/token} 응답의
 *       {@code onboardingCompleted=false} 이면 온보딩 화면으로 이동</li>
 *   <li>닉네임·프로필 이미지는 {@code PATCH /api/users/me} 로 설정 (온보딩 전용 body 없음)</li>
 *   <li>확인 후 {@code POST /api/users/me/onboarding} 으로 완료. 이미 완료면 {@code USER_409}</li>
 *   <li>이후 진입 시 {@code GET /api/users/me} 의 {@code onboardingCompleted} 로 재확인 가능</li>
 * </ol>
 */
@Tag(name = "User", description = "사용자 프로필·온보딩")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "내 프로필 조회",
            description = "온보딩 여부 확인용. 응답의 onboardingCompleted 로 온보딩 화면 분기 가능.")
    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getMyProfile(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(userService.getProfile(userId));
    }

    @Operation(
            summary = "온보딩 완료",
            description = """
                    온보딩 완료 플래그만 켠다. 닉네임·이미지는 PATCH /api/users/me 를 먼저(또는 병행) 사용한다.
                    이미 완료한 사용자는 USER_409.
                    """)
    @PostMapping("/me/onboarding")
    public ApiResponse<UserProfileResponse> completeOnboarding(
            @AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(userService.completeOnboarding(userId));
    }

    @Hidden
    @Operation(
            summary = "내 프로필 수정",
            description = "마이페이지(018)와 온보딩(002) 초기 닉네임·이미지 설정에 공통으로 사용한다.")
    @PatchMapping("/me")
    public ApiResponse<UserProfileResponse> updateMyProfile(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok(
                userService.updateProfile(userId, request.nickname(), request.profileImageUrl()));
    }
}
