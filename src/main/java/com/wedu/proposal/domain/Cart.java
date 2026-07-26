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
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사용자가 담은 상품과 수량, 견적 합계를 관리하는 장바구니(견적함) Aggregate Root. */
@Getter
@Entity
@Table(
        name = "carts",
        uniqueConstraints = @UniqueConstraint(name = "uk_carts_user_id", columnNames = "user_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ElementCollection
    @CollectionTable(name = "cart_items", joinColumns = @JoinColumn(name = "cart_id"))
    @MapKeyColumn(name = "product_id")
    private Map<Long, CartItem> items = new HashMap<>();

    private Cart(Long userId) {
        this.userId = userId;
    }

    /** 사용자의 장바구니를 새로 만든다. */
    public static Cart create(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "사용자 식별자는 필수입니다.");
        }
        return new Cart(userId);
    }

    /** 상품을 담는다. 이미 담긴 상품이면 수량을 더한다. */
    public void addItem(Long productId, String name, int price, int quantity) {
        if (productId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "상품 식별자는 필수입니다.");
        }
        CartItem existing = items.get(productId);
        CartItem updated = existing == null
                ? CartItem.of(name, price, quantity)
                : existing.plusQuantity(quantity);
        items.put(productId, updated);
    }

    /** 담긴 상품의 수량을 지정한 값으로 변경한다. */
    public void changeQuantity(Long productId, int quantity) {
        CartItem item = items.get(productId);
        if (item == null) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        items.put(productId, item.withQuantity(quantity));
    }

    /** 담긴 상품을 장바구니에서 제거한다. */
    public void removeItem(Long productId) {
        if (!items.containsKey(productId)) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        items.remove(productId);
    }

    /** 담긴 상품들의 견적 합계를 계산한다. */
    public int totalPrice() {
        return items.values().stream().mapToInt(CartItem::subtotal).sum();
    }

    public Map<Long, CartItem> getItems() {
        return Map.copyOf(items);
    }
}
