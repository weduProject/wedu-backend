package com.wedu.user.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 프로필 수정 요청 본문 (마이페이지 018 · 온보딩 002 초기 설정 공용).
 *
 * <p>형식 검증(공백 여부)은 여기서, 도메인 규칙(길이 등)은
 * {@link com.wedu.user.domain.Nickname} 이 담당한다. 온보딩 전용 API 는 두지 않고 이 요청을 재사용한다.
 */
public record UpdateProfileRequest(
        @NotBlank(message = "닉네임은 필수입니다.") String nickname,
        String profileImageUrl) {
}
