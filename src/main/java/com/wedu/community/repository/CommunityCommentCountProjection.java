package com.wedu.community.repository;

/** 게시글별 댓글·답글 수 집계 결과. */
public interface CommunityCommentCountProjection {
    Long getPostId();

    long getCommentCount();
}
