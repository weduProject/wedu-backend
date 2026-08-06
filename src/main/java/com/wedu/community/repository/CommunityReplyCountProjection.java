package com.wedu.community.repository;

/** 최상위 댓글별 답글 수 집계 결과. */
public interface CommunityReplyCountProjection {
    Long getParentId();

    long getReplyCount();
}
