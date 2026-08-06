package com.wedu.community.repository;

import com.wedu.community.domain.CommunityComment;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 커뮤니티 댓글 저장, 조회, 집계를 담당한다. */
public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {

    /** 좋아요 변경과 댓글 삭제가 같은 댓글에서 일관된 순서로 실행되도록 잠근다. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CommunityComment c WHERE c.id = :commentId")
    Optional<CommunityComment> findByIdForUpdate(@Param("commentId") Long commentId);

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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM CommunityComment c WHERE c.parentId = :parentId")
    void deleteByParentId(@Param("parentId") Long parentId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM CommunityComment c WHERE c.postId = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}
