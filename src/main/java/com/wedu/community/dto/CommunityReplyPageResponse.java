package com.wedu.community.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/** 특정 최상위 댓글의 1단계 답글 페이지 응답. */
public record CommunityReplyPageResponse(
        List<CommunityCommentResponse> replies,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext) {

    public static CommunityReplyPageResponse from(
            Page<?> page, List<CommunityCommentResponse> replies) {
        return new CommunityReplyPageResponse(
                replies,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext());
    }
}
