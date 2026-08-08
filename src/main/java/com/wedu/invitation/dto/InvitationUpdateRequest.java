package com.wedu.invitation.dto;

import java.time.LocalDate;

public record InvitationUpdateRequest(

        // 기본 정보
        String templateId,
        String title,

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

        // 디자인
        String mainColor,
        String fontFamily,
        String bgmUrl
) {
}