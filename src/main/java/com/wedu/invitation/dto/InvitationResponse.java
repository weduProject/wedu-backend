package com.wedu.invitation.dto;

import com.wedu.invitation.domain.Invitation;
import com.wedu.invitation.domain.InvitationStatus;
import java.time.LocalDate;

public record InvitationResponse(
        Long id,
        Long userId,
        String templateId,
        String title,
        InvitationStatus status,

        // 신랑·신부 정보
        String groomName,
        String brideName,
        String groomPhoto,
        String bridePhoto,
        String groomContact,
        String brideContact,
        String groomParents,
        String brideParents,

        // 예식 정보
        LocalDate weddingDate,
        String weddingTime,
        String venueName,
        String venueAddress,
        String venueDetail,
        Double latitude,
        Double longitude,

        // 청첩장 문구
        String mainGreeting,
        String invitationMessage,
        String additionalMessage,

        // 계좌 정보
        String groomBank,
        String groomAccount,
        String groomAccountHolder,
        String brideBank,
        String brideAccount,
        String brideAccountHolder,

        // 혼주 연락처
        String groomParentContact,
        String brideParentContact,

        // 오시는 길
        String transportGuide,
        String parkingGuide,
        String publicTransportGuide,

        // 디자인 설정
        String mainColor,
        String fontFamily,
        String bgmUrl
) {

    public static InvitationResponse from(Invitation invitation) {
        return new InvitationResponse(
                invitation.getId(),
                invitation.getUserId(),
                invitation.getTemplateId(),
                invitation.getTitle(),
                invitation.getStatus(),

                invitation.getGroomName(),
                invitation.getBrideName(),
                invitation.getGroomPhoto(),
                invitation.getBridePhoto(),
                invitation.getGroomContact(),
                invitation.getBrideContact(),
                invitation.getGroomParents(),
                invitation.getBrideParents(),

                invitation.getWeddingDate(),
                invitation.getWeddingTime(),
                invitation.getVenueName(),
                invitation.getVenueAddress(),
                invitation.getVenueDetail(),
                invitation.getLatitude(),
                invitation.getLongitude(),

                invitation.getMainGreeting(),
                invitation.getInvitationMessage(),
                invitation.getAdditionalMessage(),

                invitation.getGroomBank(),
                invitation.getGroomAccount(),
                invitation.getGroomAccountHolder(),
                invitation.getBrideBank(),
                invitation.getBrideAccount(),
                invitation.getBrideAccountHolder(),

                invitation.getGroomParentContact(),
                invitation.getBrideParentContact(),

                invitation.getTransportGuide(),
                invitation.getParkingGuide(),
                invitation.getPublicTransportGuide(),

                invitation.getMainColor(),
                invitation.getFontFamily(),
                invitation.getBgmUrl()
        );
    }
}