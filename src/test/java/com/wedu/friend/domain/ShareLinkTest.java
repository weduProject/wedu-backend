package com.wedu.friend.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ShareLinkTest {

    @Test
    @DisplayName("소유자의 공유 링크를 발급한다")
    void issue() {
        ShareLink shareLink = ShareLink.issue(1L);

        assertThat(shareLink.getOwnerId()).isEqualTo(1L);
        assertThat(shareLink.getToken()).isNotBlank();
    }

    @Test
    @DisplayName("링크를 재발급하면 토큰이 바뀐다")
    void reissue() {
        ShareLink shareLink = ShareLink.issue(1L);
        String originalToken = shareLink.getToken();

        shareLink.reissue();

        assertThat(shareLink.getToken()).isNotEqualTo(originalToken);
    }
}
