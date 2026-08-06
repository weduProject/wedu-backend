package com.wedu.community.domain;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사용자가 커뮤니티 댓글 또는 답글에 남긴 좋아요를 나타낸다. */
@Getter
@Entity
@Table(
        name = "community_comment_likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_community_comment_likes_comment_user",
                columnNames = {"comment_id", "user_id"}),
        indexes = @Index(name = "idx_community_comment_likes_user_id", columnList = "user_id,id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityCommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comment_id", nullable = false)
    private Long commentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private CommunityCommentLike(Long commentId, Long userId) {
        this.commentId = validateId(commentId, "댓글 식별자");
        this.userId = validateId(userId, "사용자 식별자");
    }

    public static CommunityCommentLike create(Long commentId, Long userId) {
        return new CommunityCommentLike(commentId, userId);
    }

    private static Long validateId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, fieldName + "는 양수여야 합니다.");
        }
        return id;
    }
}
