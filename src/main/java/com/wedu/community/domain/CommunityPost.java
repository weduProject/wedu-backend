package com.wedu.community.domain;

import com.wedu.global.common.BaseTimeEntity;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사용자가 작성한 커뮤니티 글과 익명 공개 규칙을 관리한다. */
@Getter
@Entity
@Table(
        name = "community_posts",
        indexes = {
            @Index(name = "idx_community_posts_created_id", columnList = "created_at,id"),
            @Index(
                    name = "idx_community_posts_theme_created_id",
                    columnList = "theme,created_at,id"),
            @Index(name = "idx_community_posts_author_id", columnList = "author_id,id")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityPost extends BaseTimeEntity {

    public static final int MAX_TITLE_LENGTH = 100;
    public static final int MAX_CONTENT_LENGTH = 5000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(nullable = false, length = MAX_TITLE_LENGTH)
    private String title;

    @Column(nullable = false, length = MAX_CONTENT_LENGTH)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PostTheme theme;

    @Column(nullable = false)
    private boolean anonymous;

    private CommunityPost(
            Long authorId,
            String title,
            String content,
            PostTheme theme,
            boolean anonymous) {
        this.authorId = authorId;
        this.title = title;
        this.content = content;
        this.theme = theme;
        this.anonymous = anonymous;
    }

    /** 필수값과 글자 수를 검증해 게시글을 생성한다. */
    public static CommunityPost create(
            Long authorId,
            String title,
            String content,
            PostTheme theme,
            boolean anonymous) {
        validateAuthorId(authorId);
        return new CommunityPost(
                authorId,
                normalizeText(title, "게시글 제목", MAX_TITLE_LENGTH),
                normalizeText(content, "게시글 본문", MAX_CONTENT_LENGTH),
                validateTheme(theme),
                anonymous);
    }

    /** 제목, 본문, 테마와 익명 여부를 검증된 값으로 교체한다. */
    public void update(
            String title,
            String content,
            PostTheme theme,
            boolean anonymous) {
        this.title = normalizeText(title, "게시글 제목", MAX_TITLE_LENGTH);
        this.content = normalizeText(content, "게시글 본문", MAX_CONTENT_LENGTH);
        this.theme = validateTheme(theme);
        this.anonymous = anonymous;
    }

    /** 주어진 사용자가 이 게시글의 작성자인지 확인한다. */
    public boolean isOwnedBy(Long userId) {
        return userId != null && authorId.equals(userId);
    }

    private static void validateAuthorId(Long authorId) {
        if (authorId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "작성자 식별자는 필수입니다.");
        }
    }

    private static PostTheme validateTheme(PostTheme theme) {
        if (theme == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "게시글 테마는 필수입니다.");
        }
        return theme;
    }

    private static String normalizeText(String value, String fieldName, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, fieldName + "은 필수입니다.");
        }
        String normalized = value.trim();
        if (normalized.codePointCount(0, normalized.length()) > maxLength) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    fieldName + "은 " + maxLength + "자 이하여야 합니다.");
        }
        return normalized;
    }
}
