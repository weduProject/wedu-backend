package com.wedu.proposal.dto;

import com.wedu.proposal.domain.ProposalItemCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** 카테고리별 옵션 선택/변경 요청. 표시용 이름·가격은 서버가 productId 로 조회한 값을 사용한다. */
public record ProposalOptionRequest(@NotNull ProposalItemCategory category, @NotNull @Positive Long productId) {
}
