package com.wedu.community.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/** 최상위 댓글 페이지와 각 댓글의 1단계 답글 응답. */
public record CommunityCommentPageResponse(
        List<CommunityCommentSummaryResponse> comments,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext) {

    public static CommunityCommentPageResponse from(
            Page<?> page, List<CommunityCommentSummaryResponse> comments) {
        return new CommunityCommentPageResponse(
                comments,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext());
    }
}
