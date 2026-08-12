package com.wedu.community.domain;

import com.wedu.global.common.BaseTimeEntity;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 커뮤니티 게시글의 댓글과 1단계 답글을 관리한다. */
@Getter
@Entity
@Table(
        name = "community_comments",
        indexes = {
            @Index(
                    name = "idx_community_comments_post_parent_created_id",
                    columnList = "post_id,parent_id,created_at,id"),
            @Index(name = "idx_community_comments_parent_created_id", columnList = "parent_id,created_at,id"),
            @Index(name = "idx_community_comments_author_id", columnList = "author_id,id")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityComment extends BaseTimeEntity {

    public static final int MAX_CONTENT_LENGTH = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = false, length = MAX_CONTENT_LENGTH)
    private String content;

    @Column(nullable = false)
    private boolean anonymous;

    private CommunityComment(
            Long postId,
            Long authorId,
            Long parentId,
            String content,
            boolean anonymous) {
        this.postId = validateId(postId, "게시글 식별자");
        this.authorId = validateId(authorId, "작성자 식별자");
        this.parentId = parentId;
        this.content = normalizeContent(content);
        this.anonymous = anonymous;
    }

    /** 최상위 댓글을 생성한다. */
    public static CommunityComment createComment(
            Long postId, Long authorId, String content, boolean anonymous) {
        return new CommunityComment(postId, authorId, null, content, anonymous);
    }

    /** 부모 댓글을 가리키는 1단계 답글을 생성한다. */
    public static CommunityComment createReply(
            Long postId, Long authorId, Long parentId, String content, boolean anonymous) {
        return new CommunityComment(
                postId,
                authorId,
                validateId(parentId, "부모 댓글 식별자"),
                content,
                anonymous);
    }

    /** 댓글 내용과 익명 여부를 변경한다. */
    public void update(String content, boolean anonymous) {
        this.content = normalizeContent(content);
        this.anonymous = anonymous;
    }

    public boolean isReply() {
        return parentId != null;
    }

    public boolean isOwnedBy(Long userId) {
        return userId != null && authorId.equals(userId);
    }

    private static Long validateId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, fieldName + "는 양수여야 합니다.");
        }
        return id;
    }

    private static String normalizeContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "댓글 내용은 필수입니다.");
        }
        String normalized = content.trim();
        if (normalized.codePointCount(0, normalized.length()) > MAX_CONTENT_LENGTH) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "댓글 내용은 " + MAX_CONTENT_LENGTH + "자 이하여야 합니다.");
        }
        return normalized;
    }
}
