package com.wedu.recommendation.presentation.dto;

import com.wedu.recommendation.domain.enums.BudgetRange;
import com.wedu.recommendation.domain.enums.ExcludedElement;
import com.wedu.recommendation.domain.enums.LocationType;
import com.wedu.recommendation.domain.enums.MoodType;
import com.wedu.recommendation.domain.enums.PartnerMbti;
import com.wedu.recommendation.domain.enums.PreparationType;
import com.wedu.recommendation.domain.enums.RegionType;
import com.wedu.recommendation.domain.enums.RequiredService;
import com.wedu.recommendation.domain.enums.ScheduleRange;

import java.util.List;

public record PsychologicalTestSubmitRequest(

        MoodType moodType,

        LocationType locationType,

        RegionType region,

        PreparationType preparationType,

        List<RequiredService> requiredServices,

        List<PrioritySelectionRequest> priorityValues,

        BudgetRange budgetRange,

        List<ExcludedElement> excludedElements,

        ScheduleRange scheduleRange,

        PartnerMbti partnerMbti
) {
}