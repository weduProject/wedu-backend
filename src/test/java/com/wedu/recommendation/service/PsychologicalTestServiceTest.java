package com.wedu.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wedu.recommendation.domain.PsychologicalTestResult;
import com.wedu.recommendation.domain.enums.BudgetRange;
import com.wedu.recommendation.domain.enums.ExcludedElement;
import com.wedu.recommendation.domain.enums.LocationType;
import com.wedu.recommendation.domain.enums.MoodType;
import com.wedu.recommendation.domain.enums.PartnerMbti;
import com.wedu.recommendation.domain.enums.PreparationType;
import com.wedu.recommendation.domain.enums.PriorityValue;
import com.wedu.recommendation.domain.enums.RegionType;
import com.wedu.recommendation.domain.enums.RequiredService;
import com.wedu.recommendation.domain.enums.ScheduleRange;
import com.wedu.recommendation.dto.PrioritySelectionRequest;
import com.wedu.recommendation.dto.PsychologicalTestSubmitRequest;
import com.wedu.recommendation.repository.PsychologicalTestResultRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PsychologicalTestServiceTest {

    @Mock
    private PsychologicalTestResultRepository psychologicalTestResultRepository;

    private PsychologicalTestService psychologicalTestService;

    @BeforeEach
    void setUp() {
        psychologicalTestService = new PsychologicalTestService(psychologicalTestResultRepository);
    }

    @Test
    @DisplayName("처음 제출하면 새 결과를 저장한다")
    void submitCreatesResult() {
        when(psychologicalTestResultRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(psychologicalTestResultRepository.save(any(PsychologicalTestResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        psychologicalTestService.submit(1L, request(MoodType.LUXURY_EVENT));

        verify(psychologicalTestResultRepository).save(any(PsychologicalTestResult.class));
        verify(psychologicalTestResultRepository, never()).delete(any());
    }

    @Test
    @DisplayName("다시 제출하면 기존 결과를 삭제하지 않고 덮어쓴다")
    void resubmitReplacesExistingResult() {
        PsychologicalTestResult existing = PsychologicalTestResult.create(
                1L,
                MoodType.LUXURY_EVENT,
                LocationType.RESTAURANT,
                RegionType.UNDECIDED,
                PreparationType.VENUE_AND_DIY,
                List.of(RequiredService.VIDEO),
                List.of(PriorityValue.MEMORY, PriorityValue.CONVENIENCE),
                BudgetRange.FROM_1000000_TO_2000000,
                List.of(ExcludedElement.PUBLIC_EVENT),
                ScheduleRange.WITHIN_3_MONTHS,
                PartnerMbti.ENFP);
        when(psychologicalTestResultRepository.findByUserId(1L)).thenReturn(Optional.of(existing));

        psychologicalTestService.submit(1L, request(MoodType.COZY_SINCERE));

        assertThat(existing.getMoodType()).isEqualTo(MoodType.COZY_SINCERE);
        verify(psychologicalTestResultRepository, never()).delete(any());
        verify(psychologicalTestResultRepository, never()).save(any());
    }

    private PsychologicalTestSubmitRequest request(MoodType moodType) {
        return new PsychologicalTestSubmitRequest(
                moodType,
                LocationType.RESTAURANT,
                RegionType.UNDECIDED,
                PreparationType.VENUE_AND_DIY,
                List.of(RequiredService.VIDEO),
                List.of(
                        new PrioritySelectionRequest(PriorityValue.MEMORY, 1),
                        new PrioritySelectionRequest(PriorityValue.CONVENIENCE, 2)),
                BudgetRange.FROM_1000000_TO_2000000,
                List.of(ExcludedElement.PUBLIC_EVENT, ExcludedElement.FAMILY_FRIEND_PARTICIPATION),
                ScheduleRange.WITHIN_3_MONTHS,
                PartnerMbti.ENFP);
    }
}
