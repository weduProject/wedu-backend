package com.wedu.friend.domain;

import com.wedu.global.common.BaseTimeEntity;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 한 명이 상대 사용자를 친구로 등록한 관계 한 방향을 나타내는 Aggregate Root.
 *
 * <p>양방향 친구 관계는 {@code (userId, friendUserId)} 와 {@code (friendUserId, userId)} 두 행으로
 * 표현한다. 요청/수락 단계 없이 추가 즉시 성립하는 단순 모델이다.
 */
@Getter
@Entity
@Table(
        name = "friendships",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_friendships_user_friend",
                columnNames = {"user_id", "friend_user_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friendship extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "friend_user_id", nullable = false)
    private Long friendUserId;

    private Friendship(Long userId, Long friendUserId) {
        this.userId = userId;
        this.friendUserId = friendUserId;
    }

    /** 두 사용자 사이의 친구 관계 한 방향을 생성한다. */
    public static Friendship create(Long userId, Long friendUserId) {
        if (userId == null || friendUserId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "사용자 식별자는 필수입니다.");
        }
        if (userId.equals(friendUserId)) {
            throw new BusinessException(ErrorCode.FRIEND_SELF_NOT_ALLOWED);
        }
        return new Friendship(userId, friendUserId);
    }
}
