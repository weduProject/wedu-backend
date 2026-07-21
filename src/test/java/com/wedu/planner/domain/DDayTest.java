package com.wedu.planner.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wedu.global.error.BusinessException;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DDayTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 21);

    @Test
    @DisplayName("결혼식 날짜로 D-day를 생성한다")
    void create() {
        DDay dDay = DDay.create(1L, LocalDate.of(2026, 11, 14), TODAY);

        assertThat(dDay.getUserId()).isEqualTo(1L);
        assertThat(dDay.getWeddingDate()).isEqualTo(LocalDate.of(2026, 11, 14));
        assertThat(dDay.daysRemaining(TODAY)).isEqualTo(116);
    }

    @Test
    @DisplayName("과거 날짜로 D-day를 생성할 수 없다")
    void rejectPastWeddingDate() {
        assertThatThrownBy(() ->
                DDay.create(1L, TODAY.minusDays(1), TODAY))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("결혼식 당일의 남은 일수는 0이다")
    void weddingDayIsDday() {
        DDay dDay = DDay.create(1L, TODAY, TODAY);

        assertThat(dDay.daysRemaining(TODAY)).isZero();
    }

    @Test
    @DisplayName("결혼식 날짜를 미래 날짜로 변경한다")
    void changeWeddingDate() {
        DDay dDay = DDay.create(1L, TODAY.plusDays(10), TODAY);

        dDay.changeWeddingDate(TODAY.plusDays(20), TODAY);

        assertThat(dDay.getWeddingDate()).isEqualTo(TODAY.plusDays(20));
    }
}
