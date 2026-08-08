package com.wedu.invitation.service;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.invitation.domain.Invitation;
import com.wedu.invitation.dto.InvitationCreateRequest;
import com.wedu.invitation.dto.InvitationResponse;
import com.wedu.invitation.dto.InvitationUpdateRequest;
import com.wedu.invitation.repository.InvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 청첩장 생성·조회·수정·발행 유스케이스를 처리한다. */
@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;

    /** 새 청첩장을 생성한다. */
    @Transactional
    public InvitationResponse create(Long userId, InvitationCreateRequest request) {

        Invitation invitation = Invitation.create(
                userId,
                request.templateId(),
                request.title(),

                request.groomName(),
                request.brideName(),
                request.groomPhoto(),
                request.bridePhoto(),
                request.groomContact(),
                request.brideContact(),
                request.groomParents(),
                request.brideParents(),

                request.weddingDate(),
                request.weddingTime(),
                request.venueName(),
                request.venueAddress(),
                request.venueDetail(),
                request.latitude(),
                request.longitude(),

                request.mainGreeting(),
                request.invitationMessage(),
                request.additionalMessage(),

                request.groomBank(),
                request.groomAccount(),
                request.groomAccountHolder(),
                request.brideBank(),
                request.brideAccount(),
                request.brideAccountHolder(),

                request.groomParentContact(),
                request.brideParentContact(),

                request.transportGuide(),
                request.parkingGuide(),
                request.publicTransportGuide(),

                request.mainColor(),
                request.fontFamily(),
                request.bgmUrl()
        );

        Invitation saved = invitationRepository.save(invitation);

        return InvitationResponse.from(saved);
    }

    /** 로그인 사용자의 청첩장을 조회한다. */
    @Transactional(readOnly = true)
    public InvitationResponse getMine(Long userId) {

        Invitation invitation = invitationRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.INVALID_INPUT,
                                "청첩장을 찾을 수 없습니다."
                        ));

        return InvitationResponse.from(invitation);
    }

    /** 로그인 사용자의 청첩장을 수정한다. */
    @Transactional
    public InvitationResponse update(
            Long userId,
            InvitationUpdateRequest request) {

        Invitation invitation = invitationRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.INVALID_INPUT,
                                "청첩장을 찾을 수 없습니다."
                        ));

        invitation.update(
                request.templateId(),
                request.title(),

                request.groomName(),
                request.brideName(),
                request.groomPhoto(),
                request.bridePhoto(),
                request.groomContact(),
                request.brideContact(),
                request.groomParents(),
                request.brideParents(),

                request.weddingDate(),
                request.weddingTime(),
                request.venueName(),
                request.venueAddress(),
                request.venueDetail(),
                request.latitude(),
                request.longitude(),

                request.mainGreeting(),
                request.invitationMessage(),
                request.additionalMessage(),

                request.groomBank(),
                request.groomAccount(),
                request.groomAccountHolder(),
                request.brideBank(),
                request.brideAccount(),
                request.brideAccountHolder(),

                request.groomParentContact(),
                request.brideParentContact(),

                request.transportGuide(),
                request.parkingGuide(),
                request.publicTransportGuide(),

                request.mainColor(),
                request.fontFamily(),
                request.bgmUrl()
        );

        return InvitationResponse.from(invitation);
    }

    /** 작성 중인 청첩장을 발행한다. */
    @Transactional
    public InvitationResponse publish(Long userId) {

        Invitation invitation = invitationRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.INVALID_INPUT,
                                "청첩장을 찾을 수 없습니다."
                        ));

        invitation.publish();

        return InvitationResponse.from(invitation);
    }
}