package com.wedu.community.repository;

import com.wedu.community.domain.CommunityCommentLike;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 커뮤니티 댓글·답글 좋아요 저장, 조회와 집계를 담당한다. */
public interface CommunityCommentLikeRepository extends JpaRepository<CommunityCommentLike, Long> {

    boolean existsByCommentIdAndUserId(Long commentId, Long userId);

    long countByCommentId(Long commentId);

    long deleteByCommentIdAndUserId(Long commentId, Long userId);

    @Query("""
            SELECT l.commentId AS commentId, COUNT(l) AS likeCount
            FROM CommunityCommentLike l
            WHERE l.commentId IN :commentIds
            GROUP BY l.commentId
            """)
    List<CommunityCommentLikeCountProjection> countByCommentIds(
            @Param("commentIds") Collection<Long> commentIds);

    @Query("""
            SELECT l.commentId
            FROM CommunityCommentLike l
            WHERE l.userId = :userId AND l.commentId IN :commentIds
            """)
    List<Long> findLikedCommentIds(
            @Param("userId") Long userId,
            @Param("commentIds") Collection<Long> commentIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM CommunityCommentLike l
            WHERE l.commentId = :commentId
               OR l.commentId IN (
                    SELECT c.id FROM CommunityComment c WHERE c.parentId = :commentId)
            """)
    void deleteByCommentIdAndReplies(@Param("commentId") Long commentId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM CommunityCommentLike l
            WHERE l.commentId IN (
                    SELECT c.id FROM CommunityComment c WHERE c.postId = :postId)
            """)
    void deleteByPostId(@Param("postId") Long postId);
}
