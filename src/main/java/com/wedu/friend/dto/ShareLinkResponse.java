package com.wedu.friend.dto;

import com.wedu.friend.domain.ShareLink;

/** 내 플래너 조회 전용 공유 링크의 토큰. */
public record ShareLinkResponse(String token) {

    public static ShareLinkResponse from(ShareLink shareLink) {
        return new ShareLinkResponse(shareLink.getToken());
    }
}
