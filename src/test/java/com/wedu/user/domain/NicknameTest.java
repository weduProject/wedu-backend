package com.wedu.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wedu.global.error.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NicknameTest {

    @Test
    @DisplayName("앞뒤 공백은 제거되어 저장된다")
    void trimsSurroundingWhitespace() {
        Nickname nickname = new Nickname("  완규  ");
        assertThat(nickname.getValue()).isEqualTo("완규");
    }

    @Test
    @DisplayName("빈 값이면 예외가 발생한다")
    void rejectsBlank() {
        assertThatThrownBy(() -> new Nickname("   "))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("20자를 초과하면 예외가 발생한다")
    void rejectsTooLong() {
        String tooLong = "가".repeat(21);
        assertThatThrownBy(() -> new Nickname(tooLong))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("값이 같으면 동등하다(값 객체)")
    void equalsByValue() {
        assertThat(new Nickname("완규")).isEqualTo(new Nickname("완규"));
    }
}
