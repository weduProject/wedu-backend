package com.wedu.community.repository;

/** 댓글별 좋아요 수 집계 결과. */
public interface CommunityCommentLikeCountProjection {
    Long getCommentId();

    long getLikeCount();
}
