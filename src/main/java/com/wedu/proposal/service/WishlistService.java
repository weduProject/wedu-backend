package com.wedu.proposal.service;

import com.wedu.proposal.domain.Wishlist;
import com.wedu.proposal.dto.WishlistResponse;
import com.wedu.proposal.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 찜하기(위시리스트) 유스케이스를 처리한다. */
@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;

    /** 상품을 찜 목록에 추가한다. */
    @Transactional
    public WishlistResponse add(Long userId, Long productId) {
        Wishlist wishlist = findOrCreate(userId);
        wishlist.add(productId);
        return WishlistResponse.from(wishlist);
    }

    /** 찜한 상품을 목록에서 제거한다. */
    @Transactional
    public WishlistResponse remove(Long userId, Long productId) {
        Wishlist wishlist = findOrCreate(userId);
        wishlist.remove(productId);
        return WishlistResponse.from(wishlist);
    }

    /** 내가 찜한 상품 목록을 조회한다. */
    @Transactional(readOnly = true)
    public WishlistResponse getMyWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId)
                .map(WishlistResponse::from)
                .orElseGet(() -> WishlistResponse.from(Wishlist.create(userId)));
    }

    private Wishlist findOrCreate(Long userId) {
        return wishlistRepository.findByUserId(userId)
                .orElseGet(() -> wishlistRepository.save(Wishlist.create(userId)));
    }
}
