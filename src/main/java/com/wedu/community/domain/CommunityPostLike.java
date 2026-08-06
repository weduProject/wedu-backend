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

/** 사용자가 커뮤니티 게시글에 남긴 좋아요를 나타낸다. */
@Getter
@Entity
@Table(
        name = "community_post_likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_community_post_likes_post_user",
                columnNames = {"post_id", "user_id"}),
        indexes = @Index(name = "idx_community_post_likes_user_id", columnList = "user_id,id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityPostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private CommunityPostLike(Long postId, Long userId) {
        this.postId = validateId(postId, "게시글 식별자");
        this.userId = validateId(userId, "사용자 식별자");
    }

    public static CommunityPostLike create(Long postId, Long userId) {
        return new CommunityPostLike(postId, userId);
    }

    private static Long validateId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, fieldName + "는 양수여야 합니다.");
        }
        return id;
    }
}
