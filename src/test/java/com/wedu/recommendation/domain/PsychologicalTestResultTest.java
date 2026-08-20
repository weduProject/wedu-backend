package com.wedu.recommendation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wedu.global.error.BusinessException;
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
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PsychologicalTestResultTest {

    @Test
    @DisplayName("심리테스트 응답을 생성한다")
    void create() {
        PsychologicalTestResult result = sample();

        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getMoodType()).isEqualTo(MoodType.LUXURY_EVENT);
        assertThat(result.getPriorityValues()).containsExactly(
                PriorityValue.MEMORY, PriorityValue.CONVENIENCE);
    }

    @Test
    @DisplayName("기존 응답을 새 답변으로 교체한다")
    void replace() {
        PsychologicalTestResult result = sample();

        result.replace(
                MoodType.COZY_SINCERE,
                LocationType.HOTEL,
                RegionType.SEOUL,
                PreparationType.FULL_SERVICE,
                List.of(RequiredService.VENUE, RequiredService.FLOWER),
                List.of(PriorityValue.EMOTION, PriorityValue.COST_EFFECTIVENESS),
                BudgetRange.UNDER_200000,
                List.of(ExcludedElement.HIGH_COST),
                ScheduleRange.WITHIN_1_MONTH,
                PartnerMbti.ISTJ);

        assertThat(result.getMoodType()).isEqualTo(MoodType.COZY_SINCERE);
        assertThat(result.getLocationType()).isEqualTo(LocationType.HOTEL);
        assertThat(result.getRegion()).isEqualTo(RegionType.SEOUL);
        assertThat(result.getPreparationType()).isEqualTo(PreparationType.FULL_SERVICE);
        assertThat(result.getRequiredServices())
                .containsExactly(RequiredService.VENUE, RequiredService.FLOWER);
        assertThat(result.getPriorityValues())
                .containsExactly(PriorityValue.EMOTION, PriorityValue.COST_EFFECTIVENESS);
        assertThat(result.getBudgetRange()).isEqualTo(BudgetRange.UNDER_200000);
        assertThat(result.getExcludedElements()).containsExactly(ExcludedElement.HIGH_COST);
        assertThat(result.getScheduleRange()).isEqualTo(ScheduleRange.WITHIN_1_MONTH);
        assertThat(result.getPartnerMbti()).isEqualTo(PartnerMbti.ISTJ);
    }

    @Test
    @DisplayName("여행 장소를 선택했는데 지역이 없으면 교체에 실패한다")
    void replaceRequiresRegionForTravel() {
        PsychologicalTestResult result = sample();

        assertThatThrownBy(() -> result.replace(
                MoodType.TRAVEL_NATURAL,
                LocationType.TRAVEL,
                null,
                PreparationType.DIY_PACKAGE,
                List.of(RequiredService.VENUE),
                List.of(PriorityValue.MEMORY, PriorityValue.CONVENIENCE),
                BudgetRange.UNDECIDED,
                List.of(ExcludedElement.NONE),
                ScheduleRange.UNDECIDED,
                PartnerMbti.UNKNOWN))
                .isInstanceOf(BusinessException.class);
    }

    private PsychologicalTestResult sample() {
        return PsychologicalTestResult.create(
                1L,
                MoodType.LUXURY_EVENT,
                LocationType.RESTAURANT,
                RegionType.UNDECIDED,
                PreparationType.VENUE_AND_DIY,
                List.of(RequiredService.VIDEO),
                List.of(PriorityValue.MEMORY, PriorityValue.CONVENIENCE),
                BudgetRange.FROM_1000000_TO_2000000,
                List.of(ExcludedElement.PUBLIC_EVENT, ExcludedElement.FAMILY_FRIEND_PARTICIPATION),
                ScheduleRange.WITHIN_3_MONTHS,
                PartnerMbti.ENFP);
    }
}
