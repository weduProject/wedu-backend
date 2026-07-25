package com.wedu.proposal.dto;

import com.wedu.proposal.domain.Proposal;
import java.util.List;

/** 나만의 프로포즈 선택 현황과 예상 견적 합계 응답. */
public record ProposalResponse(Long id, List<ProposalItemResponse> items, long totalPrice) {

    public static ProposalResponse from(Proposal proposal) {
        List<ProposalItemResponse> items = proposal.getItems().entrySet().stream()
                .map(entry -> ProposalItemResponse.from(entry.getKey(), entry.getValue()))
                .toList();
        return new ProposalResponse(proposal.getId(), items, proposal.totalPrice());
    }
}
