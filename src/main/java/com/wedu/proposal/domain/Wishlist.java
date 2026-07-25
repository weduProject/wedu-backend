package com.wedu.proposal.domain;

import com.wedu.global.common.BaseTimeEntity;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사용자가 찜한 상품 id 목록을 관리하는 Wishlist Aggregate Root. */
@Getter
@Entity
@Table(
        name = "wishlists",
        uniqueConstraints = @UniqueConstraint(name = "uk_wishlists_user_id", columnNames = "user_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wishlist extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ElementCollection
    @CollectionTable(name = "wishlist_items", joinColumns = @JoinColumn(name = "wishlist_id"))
    @Column(name = "product_id")
    private Set<Long> productIds = new HashSet<>();

    private Wishlist(Long userId) {
        this.userId = userId;
    }

    /** 사용자의 찜 목록을 새로 만든다. */
    public static Wishlist create(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "사용자 식별자는 필수입니다.");
        }
        return new Wishlist(userId);
    }

    /** 상품을 찜 목록에 추가한다. */
    public void add(Long productId) {
        if (productId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "상품 식별자는 필수입니다.");
        }
        if (productIds.contains(productId)) {
            throw new BusinessException(ErrorCode.WISHLIST_ITEM_ALREADY_EXISTS);
        }
        productIds.add(productId);
    }

    /** 찜한 상품을 목록에서 제거한다. */
    public void remove(Long productId) {
        if (!productIds.contains(productId)) {
            throw new BusinessException(ErrorCode.WISHLIST_ITEM_NOT_FOUND);
        }
        productIds.remove(productId);
    }

    public Set<Long> getProductIds() {
        return Set.copyOf(productIds);
    }
}
