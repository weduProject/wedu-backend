package com.wedu.proposal.domain;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Proposal 애그리게이트 내부에서만 존재하는, 카테고리별 선택 옵션 값 객체. */
@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProposalItem {

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "item_name", nullable = false)
    private String name;

    @Column(name = "item_price", nullable = false)
    private long price;

    private ProposalItem(Long productId, String name, long price) {
        this.productId = productId;
        this.name = name;
        this.price = price;
    }

    static ProposalItem of(Long productId, String name, long price) {
        if (productId == null || productId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "상품 식별자는 1 이상이어야 합니다.");
        }
        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "선택 항목 이름은 필수입니다.");
        }
        if (price < 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "선택 항목 가격은 0 이상이어야 합니다.");
        }
        return new ProposalItem(productId, name, price);
    }
}
