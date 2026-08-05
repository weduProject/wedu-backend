package com.wedu.friend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.wedu.friend.domain.ShareLink;
import com.wedu.friend.dto.ShareLinkResponse;
import com.wedu.friend.repository.ShareLinkRepository;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShareLinkServiceTest {

    @Mock
    private ShareLinkRepository shareLinkRepository;

    private ShareLinkService shareLinkService;

    @BeforeEach
    void setUp() {
        shareLinkService = new ShareLinkService(shareLinkRepository);
    }

    @Test
    @DisplayName("링크가 없으면 새로 발급한다")
    void issueWhenMissing() {
        when(shareLinkRepository.findByOwnerId(1L)).thenReturn(Optional.empty());
        when(shareLinkRepository.save(any(ShareLink.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShareLinkResponse response = shareLinkService.getOrIssueMyLink(1L);

        assertThat(response.token()).isNotBlank();
    }

    @Test
    @DisplayName("이미 링크가 있으면 그대로 반환한다")
    void returnsExistingLink() {
        ShareLink existing = ShareLink.issue(1L);
        when(shareLinkRepository.findByOwnerId(1L)).thenReturn(Optional.of(existing));

        ShareLinkResponse response = shareLinkService.getOrIssueMyLink(1L);

        assertThat(response.token()).isEqualTo(existing.getToken());
    }

    @Test
    @DisplayName("토큰으로 소유자를 조회한다")
    void resolveOwnerId() {
        ShareLink shareLink = ShareLink.issue(1L);
        when(shareLinkRepository.findByToken(shareLink.getToken())).thenReturn(Optional.of(shareLink));

        Long ownerId = shareLinkService.resolveOwnerId(shareLink.getToken());

        assertThat(ownerId).isEqualTo(1L);
    }

    @Test
    @DisplayName("존재하지 않는 토큰이면 예외가 발생한다")
    void rejectUnknownToken() {
        when(shareLinkRepository.findByToken("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shareLinkService.resolveOwnerId("unknown"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SHARE_LINK_NOT_FOUND));
    }
}
