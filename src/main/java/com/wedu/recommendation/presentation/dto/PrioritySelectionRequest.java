package com.wedu.recommendation.presentation.dto;

import com.wedu.recommendation.domain.enums.PriorityValue;

public record PrioritySelectionRequest(
        PriorityValue value,
        int rank
) {
}