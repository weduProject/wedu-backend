package com.wedu.planner.domain;

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

/** 사용자가 직접 추가한 웨딩 준비 할 일과 완료 상태를 관리한다. */
@Getter
@Entity
@Table(
        name = "checklist_items",
        indexes = {
            @Index(name = "idx_checklist_items_user_id", columnList = "user_id,id"),
            @Index(
                    name = "idx_checklist_items_user_category_id",
                    columnList = "user_id,category,id"),
            @Index(
                    name = "idx_checklist_items_user_completed",
                    columnList = "user_id,completed")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChecklistItem extends BaseTimeEntity {

    public static final int MAX_TITLE_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = MAX_TITLE_LENGTH)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChecklistCategory category;

    @Column(nullable = false)
    private boolean completed;

    private ChecklistItem(
            Long userId,
            String title,
            ChecklistCategory category,
            boolean completed) {
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.completed = completed;
    }

    /** 필수값을 검증하고 미완료 상태의 체크리스트 항목을 생성한다. */
    public static ChecklistItem create(
            Long userId,
            String title,
            ChecklistCategory category) {
        validateUserId(userId);
        String normalizedTitle = normalizeTitle(title);
        validateCategory(category);
        return new ChecklistItem(userId, normalizedTitle, category, false);
    }

    /** 제목과 카테고리를 검증한 뒤 함께 변경한다. */
    public void update(String title, ChecklistCategory category) {
        String normalizedTitle = normalizeTitle(title);
        validateCategory(category);
        this.title = normalizedTitle;
        this.category = category;
    }

    /** 재시도해도 같은 결과가 되도록 완료 상태를 명시한 값으로 변경한다. */
    public void changeCompletion(boolean completed) {
        this.completed = completed;
    }

    private static void validateUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "사용자 식별자는 필수입니다.");
        }
    }

    private static String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "체크리스트 제목은 필수입니다.");
        }
        String normalizedTitle = title.trim();
        if (normalizedTitle.codePointCount(0, normalizedTitle.length()) > MAX_TITLE_LENGTH) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "체크리스트 제목은 " + MAX_TITLE_LENGTH + "자 이하여야 합니다.");
        }
        return normalizedTitle;
    }

    private static void validateCategory(ChecklistCategory category) {
        if (category == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "체크리스트 카테고리는 필수입니다.");
        }
    }
}
