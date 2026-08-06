package com.wedu.community.repository;

import com.wedu.community.domain.CommunityPost;
import com.wedu.community.domain.PostTheme;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 커뮤니티 게시글 저장과 검색을 담당한다. */
public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    /** 좋아요 변경 중 같은 게시글에 대한 중복 등록 경쟁을 직렬화한다. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM CommunityPost p WHERE p.id = :postId")
    Optional<CommunityPost> findByIdForUpdate(@Param("postId") Long postId);

    /** 테마와 제목·본문의 리터럴 부분 일치 조건으로 최신 게시글을 조회한다. */
    @Query(
            value = """
                    SELECT p FROM CommunityPost p
                    WHERE (:theme IS NULL OR p.theme = :theme)
                      AND (:keyword IS NULL
                           OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '!'
                           OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '!')
                    ORDER BY p.createdAt DESC, p.id DESC
                    """,
            countQuery = """
                    SELECT COUNT(p) FROM CommunityPost p
                    WHERE (:theme IS NULL OR p.theme = :theme)
                      AND (:keyword IS NULL
                           OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '!'
                           OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '!')
                    """)
    Page<CommunityPost> search(
            @Param("theme") PostTheme theme,
            @Param("keyword") String keyword,
            Pageable pageable);

    /** 테마와 제목·본문의 리터럴 부분 일치 조건으로 좋아요가 많은 게시글을 조회한다. */
    @Query(
            value = """
                    SELECT p FROM CommunityPost p
                    LEFT JOIN CommunityPostLike l ON l.postId = p.id
                    WHERE (:theme IS NULL OR p.theme = :theme)
                      AND (:keyword IS NULL
                           OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '!'
                           OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '!')
                    GROUP BY p
                    ORDER BY COUNT(l) DESC, p.createdAt DESC, p.id DESC
                    """,
            countQuery = """
                    SELECT COUNT(p) FROM CommunityPost p
                    WHERE (:theme IS NULL OR p.theme = :theme)
                      AND (:keyword IS NULL
                           OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '!'
                           OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '!')
                    """)
    Page<CommunityPost> searchMostLiked(
            @Param("theme") PostTheme theme,
            @Param("keyword") String keyword,
            Pageable pageable);
}
