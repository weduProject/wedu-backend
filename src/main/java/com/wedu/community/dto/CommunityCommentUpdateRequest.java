package com.wedu.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 댓글 또는 답글 수정 요청. */
public record CommunityCommentUpdateRequest(
        @Schema(description = "수정할 댓글 내용", example = "추가 비용과 환불 규정을 함께 확인해 보세요.")
        @NotBlank(message = "댓글 내용은 필수입니다.")
        String content,
        @Schema(description = "익명 작성 여부", example = "false")
        @NotNull(message = "익명 여부는 필수입니다.")
        Boolean anonymous) {
}
