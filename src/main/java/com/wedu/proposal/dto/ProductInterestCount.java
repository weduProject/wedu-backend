package com.wedu.proposal.dto;

/** 상품 하나가 받은 관심(찜 또는 담기) 수. */
public record ProductInterestCount(Long productId, long count) {}
