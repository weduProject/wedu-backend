package com.wedu.planner.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wedu.global.error.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ChecklistItemTest {

    @Test
    @DisplayName("체크리스트 항목을 미완료 상태로 생성하고 제목을 정규화한다")
    void create() {
        ChecklistItem item = ChecklistItem.create(1L, "  예식장 계약하기  ", ChecklistCategory.CEREMONY);

        assertThat(item.getUserId()).isEqualTo(1L);
        assertThat(item.getTitle()).isEqualTo("예식장 계약하기");
        assertThat(item.getCategory()).isEqualTo(ChecklistCategory.CEREMONY);
        assertThat(item.isCompleted()).isFalse();
    }

    @Test
    @DisplayName("제목과 카테고리를 수정하고 완료 상태를 명시한 값으로 변경한다")
    void update() {
        ChecklistItem item = ChecklistItem.create(1L, "스튜디오 예약", ChecklistCategory.SHOOTING);

        item.update("  드레스 촬영 예약  ", ChecklistCategory.SHOOTING);
        item.changeCompletion(true);
        item.changeCompletion(true);

        assertThat(item.getTitle()).isEqualTo("드레스 촬영 예약");
        assertThat(item.isCompleted()).isTrue();

        item.changeCompletion(false);
        assertThat(item.isCompleted()).isFalse();
    }

    @Test
    @DisplayName("필수값과 유니코드 제목 길이를 검증한다")
    void rejectInvalidValues() {
        assertInvalid(() -> ChecklistItem.create(null, "할 일", ChecklistCategory.BASIC));
        assertInvalid(() -> ChecklistItem.create(1L, " ", ChecklistCategory.BASIC));
        assertInvalid(() -> ChecklistItem.create(1L, "할 일", null));
        assertInvalid(() -> ChecklistItem.create(
                1L, "😀".repeat(ChecklistItem.MAX_TITLE_LENGTH + 1), ChecklistCategory.BASIC));

        ChecklistItem item = ChecklistItem.create(
                1L, "😀".repeat(ChecklistItem.MAX_TITLE_LENGTH), ChecklistCategory.BASIC);
        assertThat(item.getTitle().codePointCount(0, item.getTitle().length()))
                .isEqualTo(ChecklistItem.MAX_TITLE_LENGTH);
    }

    @Test
    @DisplayName("수정값 검증 실패 시 기존 상태를 유지한다")
    void updateAtomically() {
        ChecklistItem item = ChecklistItem.create(1L, "기존 할 일", ChecklistCategory.BASIC);

        assertInvalid(() -> item.update("변경할 제목", null));

        assertThat(item.getTitle()).isEqualTo("기존 할 일");
        assertThat(item.getCategory()).isEqualTo(ChecklistCategory.BASIC);
    }

    private void assertInvalid(org.assertj.core.api.ThrowableAssert.ThrowingCallable callable) {
        assertThatThrownBy(callable).isInstanceOf(BusinessException.class);
    }
}
