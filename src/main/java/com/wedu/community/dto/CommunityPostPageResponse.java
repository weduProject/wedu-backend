package com.wedu.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** 게시글 목록과 페이징 메타데이터 응답. */
public record CommunityPostPageResponse(
        List<CommunityPostSummaryResponse> posts,
        int page,
        int size,
        long totalElements,
        int totalPages,
        @Schema(description = "다음 페이지 존재 여부") boolean hasNext) {
}
