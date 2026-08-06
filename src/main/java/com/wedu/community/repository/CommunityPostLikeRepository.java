package com.wedu.community.repository;

import com.wedu.community.domain.CommunityPostLike;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 커뮤니티 게시글 좋아요 저장, 조회와 집계를 담당한다. */
public interface CommunityPostLikeRepository extends JpaRepository<CommunityPostLike, Long> {

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    long countByPostId(Long postId);

    long deleteByPostIdAndUserId(Long postId, Long userId);

    @Query("""
            SELECT l.postId AS postId, COUNT(l) AS likeCount
            FROM CommunityPostLike l
            WHERE l.postId IN :postIds
            GROUP BY l.postId
            """)
    List<CommunityPostLikeCountProjection> countByPostIds(
            @Param("postIds") Collection<Long> postIds);

    @Query("""
            SELECT l.postId
            FROM CommunityPostLike l
            WHERE l.userId = :userId AND l.postId IN :postIds
            """)
    List<Long> findLikedPostIds(
            @Param("userId") Long userId,
            @Param("postIds") Collection<Long> postIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM CommunityPostLike l WHERE l.postId = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}
