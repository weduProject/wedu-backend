package com.wedu.community.dto;

import java.util.List;

/** 특정 최상위 댓글의 1단계 답글 페이지 응답. */
public record CommunityReplyPageResponse(
        List<CommunityCommentResponse> replies,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext) {
}
