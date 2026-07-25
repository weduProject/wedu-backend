package com.wedu.proposal.dto;

import com.wedu.proposal.domain.ProposalItem;
import com.wedu.proposal.domain.ProposalItemCategory;

/** 선택된 카테고리 옵션 하나에 대한 응답. */
public record ProposalItemResponse(ProposalItemCategory category, Long productId, String name, long price) {

    public static ProposalItemResponse from(ProposalItemCategory category, ProposalItem item) {
        return new ProposalItemResponse(category, item.getProductId(), item.getName(), item.getPrice());
    }
}
