package com.wedu.friend.domain;

import com.wedu.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자가 자신의 플래너 데이터(D-day/캘린더/체크리스트/예산)를 조회 전용으로 공유하기 위한 링크.
 *
 * <p>친구가 아닌, 링크만 아는 사람에게는 조회 권한만 부여한다("친구=편집가능, 링크=조회가능").
 * 사용자당 하나의 링크만 가진다(재발급 시 토큰을 새로 만든다).
 */
@Getter
@Entity
@Table(
        name = "share_links",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_share_links_owner_id", columnNames = "owner_id"),
            @UniqueConstraint(name = "uk_share_links_token", columnNames = "token")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShareLink extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(nullable = false, length = 36)
    private String token;

    private ShareLink(Long ownerId, String token) {
        this.ownerId = ownerId;
        this.token = token;
    }

    /** 소유자의 공유 링크를 새로 발급한다. */
    public static ShareLink issue(Long ownerId) {
        return new ShareLink(ownerId, UUID.randomUUID().toString());
    }

    /** 토큰을 새로 발급해 이전 링크를 무효화한다. */
    public void reissue() {
        this.token = UUID.randomUUID().toString();
    }
}
