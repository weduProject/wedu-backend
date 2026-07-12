package com.wedu.global.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 모든 Aggregate 가 공유하는 생성/수정 시각 감사(auditing) 필드.
 *
 * <p>JPA Auditing({@link com.wedu.global.config.JpaAuditingConfig})으로 자동 채워진다.
 * 시각은 애플리케이션 전체에서 UTC 기준으로 다룬다(응답 직렬화 시 ISO-8601).
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
