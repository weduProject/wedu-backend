package com.wedu.planner.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.planner.domain.DDay;
import com.wedu.planner.dto.DDayResponse;
import com.wedu.planner.repository.DDayRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class DDayServiceTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-07-21T00:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate WEDDING_DATE = LocalDate.of(2026, 11, 14);

    @Mock
    private DDayRepository dDayRepository;

    private DDayService dDayService;

    @BeforeEach
    void setUp() {
        dDayService = new DDayService(dDayRepository, CLOCK);
    }

    @Test
    @DisplayName("결혼식 D-day를 생성한다")
    void create() {
        when(dDayRepository.existsByUserId(1L)).thenReturn(false);
        when(dDayRepository.saveAndFlush(any(DDay.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DDayResponse response = dDayService.create(1L, WEDDING_DATE);

        assertThat(response.weddingDate()).isEqualTo(WEDDING_DATE);
        assertThat(response.targetAt()).isEqualTo(Instant.parse("2026-11-13T15:00:00Z"));
        assertThat(response.daysRemaining()).isEqualTo(116);
        verify(dDayRepository).saveAndFlush(any(DDay.class));
    }

    @Test
    @DisplayName("사용자에게 이미 D-day가 있으면 중복 생성할 수 없다")
    void rejectDuplicate() {
        when(dDayRepository.existsByUserId(1L)).thenReturn(true);

        assertThatThrownBy(() -> dDayService.create(1L, WEDDING_DATE))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.PLANNER_DDAY_ALREADY_EXISTS));
    }

    @Test
    @DisplayName("동시 생성으로 DB 유니크 제약이 충돌해도 중복 D-day 예외로 변환한다")
    void convertUniqueConstraintViolation() {
        when(dDayRepository.existsByUserId(1L)).thenReturn(false);
        when(dDayRepository.saveAndFlush(any(DDay.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate user_id"));

        assertThatThrownBy(() -> dDayService.create(1L, WEDDING_DATE))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.PLANNER_DDAY_ALREADY_EXISTS));
    }

    @Test
    @DisplayName("내 결혼식 D-day를 조회한다")
    void getMyDDay() {
        DDay dDay = DDay.create(1L, WEDDING_DATE, LocalDate.of(2026, 7, 21));
        when(dDayRepository.findByUserId(1L)).thenReturn(Optional.of(dDay));

        DDayResponse response = dDayService.getMyDDay(1L);

        assertThat(response.weddingDate()).isEqualTo(WEDDING_DATE);
        assertThat(response.daysRemaining()).isEqualTo(116);
    }

    @Test
    @DisplayName("내 결혼식 날짜를 수정한다")
    void update() {
        DDay dDay = DDay.create(1L, WEDDING_DATE, LocalDate.of(2026, 7, 21));
        when(dDayRepository.findByUserId(1L)).thenReturn(Optional.of(dDay));
        LocalDate changedDate = LocalDate.of(2026, 12, 25);

        DDayResponse response = dDayService.update(1L, changedDate);

        assertThat(dDay.getWeddingDate()).isEqualTo(changedDate);
        assertThat(response.targetAt()).isEqualTo(Instant.parse("2026-12-24T15:00:00Z"));
    }

    @Test
    @DisplayName("내 결혼식 D-day를 삭제한다")
    void delete() {
        DDay dDay = DDay.create(1L, WEDDING_DATE, LocalDate.of(2026, 7, 21));
        when(dDayRepository.findByUserId(1L)).thenReturn(Optional.of(dDay));

        dDayService.delete(1L);

        verify(dDayRepository).delete(dDay);
    }
}
