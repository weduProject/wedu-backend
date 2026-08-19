package com.wedu.invitation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.invitation.domain.GalleryImage;
import com.wedu.invitation.domain.Invitation;
import com.wedu.invitation.repository.GalleryImageRepository;
import com.wedu.invitation.repository.InvitationRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GalleryServiceTest {

    @Mock
    private GalleryImageRepository galleryImageRepository;

    @Mock
    private InvitationRepository invitationRepository;

    private GalleryService galleryService;

    @BeforeEach
    void setUp() {
        galleryService = new GalleryService(galleryImageRepository, invitationRepository);
    }

    @Test
    @DisplayName("본인 청첩장 갤러리 이미지를 삭제한다")
    void delete() {
        GalleryImage image = galleryImage(1L);
        when(galleryImageRepository.findByIdAndInvitationUserId(10L, 1L))
                .thenReturn(Optional.of(image));

        galleryService.delete(1L, 10L);

        verify(galleryImageRepository).delete(image);
    }

    @Test
    @DisplayName("없거나 다른 사용자의 갤러리 이미지는 삭제할 수 없다")
    void rejectUnownedOrMissingImage() {
        when(galleryImageRepository.findByIdAndInvitationUserId(10L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> galleryService.delete(1L, 10L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.INVITATION_GALLERY_IMAGE_NOT_FOUND));

        verify(galleryImageRepository, never()).delete(any());
    }

    @Test
    @DisplayName("사용자 ID나 이미지 ID가 없으면 삭제할 수 없다")
    void rejectMissingIds() {
        assertThatThrownBy(() -> galleryService.delete(null, 10L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT));
        assertThatThrownBy(() -> galleryService.delete(1L, null))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT));

        verify(galleryImageRepository, never()).findByIdAndInvitationUserId(any(), any());
    }

    private GalleryImage galleryImage(Long userId) {
        return GalleryImage.create(
                invitation(userId),
                "https://cdn.example.com/gallery/1.jpg",
                1);
    }

    private Invitation invitation(Long userId) {
        return Invitation.create(
                userId,
                "template-1",
                "우리 청첩장",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }
}
