package com.wedu.community.dto;

import com.wedu.community.domain.PostTheme;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 커뮤니티 게시글 생성 요청. */
public record CommunityPostCreateRequest(
        @Schema(description = "게시글 제목", example = "예식장 계약 전 확인할 점")
        @NotBlank(message = "게시글 제목은 필수입니다.")
        String title,
        @Schema(description = "게시글 본문", example = "계약 전 확인해야 할 숨은 비용이 궁금합니다.")
        @NotBlank(message = "게시글 본문은 필수입니다.")
        String content,
        @Schema(description = "게시글 테마", example = "WEDDING_PREPARATION")
        @NotNull(message = "게시글 테마는 필수입니다.")
        PostTheme theme,
        @Schema(description = "익명 작성 여부", example = "true")
        @NotNull(message = "익명 여부는 필수입니다.")
        Boolean anonymous) {
}
