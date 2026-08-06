package com.wedu.community.repository;

/** 게시글별 좋아요 수 집계 결과. */
public interface CommunityPostLikeCountProjection {
    Long getPostId();

    long getLikeCount();
}
