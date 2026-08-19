package com.wedu.invitation.service;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.invitation.domain.GalleryImage;
import com.wedu.invitation.domain.Invitation;
import com.wedu.invitation.dto.GalleryImageCreateRequest;
import com.wedu.invitation.dto.GalleryImageResponse;
import com.wedu.invitation.repository.GalleryImageRepository;
import com.wedu.invitation.repository.InvitationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 청첩장 갤러리 이미지 등록·조회·삭제 유스케이스를 처리한다. */
@Service
@RequiredArgsConstructor
public class GalleryService {

    private final GalleryImageRepository galleryImageRepository;
    private final InvitationRepository invitationRepository;

    /** 로그인 사용자의 청첩장에 갤러리 이미지를 추가한다. */
    @Transactional
    public GalleryImageResponse create(
            Long userId,
            GalleryImageCreateRequest request) {

        Invitation invitation = invitationRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.INVALID_INPUT,
                                "청첩장을 찾을 수 없습니다."
                        ));

        GalleryImage galleryImage = GalleryImage.create(
                invitation,
                request.imageUrl(),
                request.sortOrder()
        );

        GalleryImage saved = galleryImageRepository.save(galleryImage);

        return GalleryImageResponse.from(saved);
    }

    /** 로그인 사용자의 청첩장 갤러리를 순서대로 조회한다. */
    @Transactional(readOnly = true)
    public List<GalleryImageResponse> getMine(Long userId) {

        Invitation invitation = invitationRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.INVALID_INPUT,
                                "청첩장을 찾을 수 없습니다."
                        ));

        return galleryImageRepository
                .findByInvitationOrderBySortOrderAsc(invitation)
                .stream()
                .map(GalleryImageResponse::from)
                .toList();
    }

    /** 로그인 사용자의 청첩장 갤러리 이미지를 삭제한다. */
    @Transactional
    public void delete(Long userId, Long imageId) {
        validateUserId(userId);
        if (imageId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "갤러리 이미지 식별자는 필수입니다.");
        }

        GalleryImage galleryImage = galleryImageRepository
                .findByIdAndInvitationUserId(imageId, userId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.INVITATION_GALLERY_IMAGE_NOT_FOUND));

        galleryImageRepository.delete(galleryImage);
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "사용자 식별자는 필수입니다.");
        }
    }
}