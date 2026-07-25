package com.wedu.proposal.dto;

import com.wedu.proposal.domain.Wishlist;
import java.util.List;

/** 찜한 상품 id 목록 응답. */
public record WishlistResponse(List<Long> productIds) {

    public static WishlistResponse from(Wishlist wishlist) {
        return new WishlistResponse(wishlist.getProductIds().stream().sorted().toList());
    }
}
