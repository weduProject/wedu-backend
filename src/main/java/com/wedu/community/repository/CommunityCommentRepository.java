package com.wedu.community.repository;

import com.wedu.community.domain.CommunityComment;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 커뮤니티 댓글 저장, 조회, 집계를 담당한다. */
public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {

    Page<CommunityComment> findByPostIdAndParentIdIsNullOrderByCreatedAtAscIdAsc(
            Long postId, Pageable pageable);

    Page<CommunityComment> findByParentIdOrderByCreatedAtAscIdAsc(
            Long parentId, Pageable pageable);

    long countByPostId(Long postId);

    @Query("""
            SELECT c.postId AS postId, COUNT(c) AS commentCount
            FROM CommunityComment c
            WHERE c.postId IN :postIds
            GROUP BY c.postId
            """)
    List<CommunityCommentCountProjection> countByPostIds(@Param("postIds") Collection<Long> postIds);

    @Query("""
            SELECT c.parentId AS parentId, COUNT(c) AS replyCount
            FROM CommunityComment c
            WHERE c.parentId IN :parentIds
            GROUP BY c.parentId
            """)
    List<CommunityReplyCountProjection> countRepliesByParentIds(
            @Param("parentIds") Collection<Long> parentIds);

    @Modifying
    @Query("DELETE FROM CommunityComment c WHERE c.parentId = :parentId")
    void deleteByParentId(@Param("parentId") Long parentId);

    @Modifying
    @Query("DELETE FROM CommunityComment c WHERE c.postId = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}
