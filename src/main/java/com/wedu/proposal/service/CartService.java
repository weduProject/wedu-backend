package com.wedu.proposal.service;

import com.wedu.proposal.domain.Cart;
import com.wedu.proposal.dto.CartItemAddRequest;
import com.wedu.proposal.dto.CartResponse;
import com.wedu.proposal.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 장바구니(견적함) 유스케이스를 처리한다. */
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    /** 상품을 장바구니에 담는다. 이미 담긴 상품이면 수량을 더한다. */
    @Transactional
    public CartResponse addItem(Long userId, CartItemAddRequest request) {
        Cart cart = findOrCreate(userId);
        cart.addItem(request.productId(), request.name(), request.price(), request.quantity());
        return CartResponse.from(cart);
    }

    /** 담긴 상품의 수량을 변경한다. */
    @Transactional
    public CartResponse changeQuantity(Long userId, Long productId, int quantity) {
        Cart cart = findOrCreate(userId);
        cart.changeQuantity(productId, quantity);
        return CartResponse.from(cart);
    }

    /** 담긴 상품을 장바구니에서 제거한다. */
    @Transactional
    public CartResponse removeItem(Long userId, Long productId) {
        Cart cart = findOrCreate(userId);
        cart.removeItem(productId);
        return CartResponse.from(cart);
    }

    /** 내 장바구니(견적함)를 조회한다. */
    @Transactional(readOnly = true)
    public CartResponse getMyCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .map(CartResponse::from)
                .orElseGet(() -> CartResponse.from(Cart.create(userId)));
    }

    private Cart findOrCreate(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> cartRepository.save(Cart.create(userId)));
    }
}
