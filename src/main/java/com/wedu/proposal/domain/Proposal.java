package com.wedu.proposal.domain;

import com.wedu.global.common.BaseTimeEntity;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사용자가 카테고리별로 옵션을 선택해 견적을 구성하는 "나만의 프로포즈" Aggregate Root. */
@Getter
@Entity
@Table(
        name = "proposals",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_proposals_user_id",
                columnNames = "user_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Proposal extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ElementCollection
    @CollectionTable(name = "proposal_items", joinColumns = @JoinColumn(name = "proposal_id"))
    @MapKeyColumn(name = "category")
    @MapKeyEnumerated(EnumType.STRING)
    private Map<ProposalItemCategory, ProposalItem> items = new HashMap<>();

    private Proposal(Long userId) {
        this.userId = userId;
    }

    /** 사용자의 나만의 프로포즈를 새로 시작한다. */
    public static Proposal start(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "사용자 식별자는 필수입니다.");
        }
        return new Proposal(userId);
    }

    /** 카테고리별 옵션을 선택하거나, 이미 선택되어 있으면 교체한다. */
    public void selectOption(ProposalItemCategory category, Long productId, String name, long price) {
        if (category == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "옵션 카테고리는 필수입니다.");
        }
        items.put(category, ProposalItem.of(productId, name, price));
    }

    /** 선택했던 카테고리 옵션을 취소한다. */
    public void removeOption(ProposalItemCategory category) {
        if (category == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "옵션 카테고리는 필수입니다.");
        }
        if (!items.containsKey(category)) {
            throw new BusinessException(ErrorCode.PROPOSAL_OPTION_NOT_SELECTED);
        }
        items.remove(category);
    }

    /** 현재까지 선택한 옵션들의 예상 견적 합계를 계산한다. */
    public long totalPrice() {
        return items.values().stream().mapToLong(ProposalItem::getPrice).sum();
    }

    public Map<ProposalItemCategory, ProposalItem> getItems() {
        return Map.copyOf(items);
    }
}
