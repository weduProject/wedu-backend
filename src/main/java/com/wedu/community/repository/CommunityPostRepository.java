package com.wedu.community.repository;

import com.wedu.community.domain.CommunityPost;
import com.wedu.community.domain.PostTheme;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 커뮤니티 게시글 저장과 검색을 담당한다. */
public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

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
}
