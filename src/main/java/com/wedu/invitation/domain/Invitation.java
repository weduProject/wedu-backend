package com.wedu.invitation.domain;

import com.wedu.global.common.BaseTimeEntity;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사용자가 생성한 모바일 청첩장 Aggregate Root. */
@Getter
@Entity
@Table(name = "invitations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Invitation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "template_id", nullable = false)
    private String templateId;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvitationStatus status;

    // 신랑·신부 정보
    @Column(name = "groom_name")
    private String groomName;

    @Column(name = "bride_name")
    private String brideName;

    @Column(name = "groom_photo", columnDefinition = "TEXT")
    private String groomPhoto;

    @Column(name = "bride_photo", columnDefinition = "TEXT")
    private String bridePhoto;

    @Column(name = "groom_contact")
    private String groomContact;

    @Column(name = "bride_contact")
    private String brideContact;

    @Column(name = "groom_parents")
    private String groomParents;

    @Column(name = "bride_parents")
    private String brideParents;

    // 예식 정보
    @Column(name = "wedding_date")
    private LocalDate weddingDate;

    @Column(name = "wedding_time")
    private String weddingTime;

    @Column(name = "venue_name")
    private String venueName;

    @Column(name = "venue_address", length = 500)
    private String venueAddress;

    @Column(name = "venue_detail")
    private String venueDetail;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    // 청첩장 문구
    @Column(name = "main_greeting", columnDefinition = "TEXT")
    private String mainGreeting;

    @Column(name = "invitation_message", columnDefinition = "TEXT")
    private String invitationMessage;

    @Column(name = "additional_message", columnDefinition = "TEXT")
    private String additionalMessage;

    // 계좌 정보
    @Column(name = "groom_bank")
    private String groomBank;

    @Column(name = "groom_account")
    private String groomAccount;

    @Column(name = "groom_account_holder")
    private String groomAccountHolder;

    @Column(name = "bride_bank")
    private String brideBank;

    @Column(name = "bride_account")
    private String brideAccount;

    @Column(name = "bride_account_holder")
    private String brideAccountHolder;

    // 혼주 연락처
    @Column(name = "groom_parent_contact")
    private String groomParentContact;

    @Column(name = "bride_parent_contact")
    private String brideParentContact;

    // 오시는 길
    @Column(name = "transport_guide", columnDefinition = "TEXT")
    private String transportGuide;

    @Column(name = "parking_guide", columnDefinition = "TEXT")
    private String parkingGuide;

    @Column(name = "public_transport_guide", columnDefinition = "TEXT")
    private String publicTransportGuide;

    // 디자인 설정
    @Column(name = "main_color")
    private String mainColor;

    @Column(name = "font_family")
    private String fontFamily;

    @Column(name = "bgm_url", columnDefinition = "TEXT")
    private String bgmUrl;

    private Invitation(
            Long userId,
            String templateId,
            String title,
            String groomName,
            String brideName,
            String groomPhoto,
            String bridePhoto,
            String groomContact,
            String brideContact,
            String groomParents,
            String brideParents,
            LocalDate weddingDate,
            String weddingTime,
            String venueName,
            String venueAddress,
            String venueDetail,
            Double latitude,
            Double longitude,
            String mainGreeting,
            String invitationMessage,
            String additionalMessage,
            String groomBank,
            String groomAccount,
            String groomAccountHolder,
            String brideBank,
            String brideAccount,
            String brideAccountHolder,
            String groomParentContact,
            String brideParentContact,
            String transportGuide,
            String parkingGuide,
            String publicTransportGuide,
            String mainColor,
            String fontFamily,
            String bgmUrl,
            InvitationStatus status) {

        this.userId = userId;
        this.templateId = templateId;
        this.title = title;

        this.groomName = groomName;
        this.brideName = brideName;
        this.groomPhoto = groomPhoto;
        this.bridePhoto = bridePhoto;
        this.groomContact = groomContact;
        this.brideContact = brideContact;
        this.groomParents = groomParents;
        this.brideParents = brideParents;

        this.weddingDate = weddingDate;
        this.weddingTime = weddingTime;
        this.venueName = venueName;
        this.venueAddress = venueAddress;
        this.venueDetail = venueDetail;
        this.latitude = latitude;
        this.longitude = longitude;

        this.mainGreeting = mainGreeting;
        this.invitationMessage = invitationMessage;
        this.additionalMessage = additionalMessage;

        this.groomBank = groomBank;
        this.groomAccount = groomAccount;
        this.groomAccountHolder = groomAccountHolder;
        this.brideBank = brideBank;
        this.brideAccount = brideAccount;
        this.brideAccountHolder = brideAccountHolder;

        this.groomParentContact = groomParentContact;
        this.brideParentContact = brideParentContact;

        this.transportGuide = transportGuide;
        this.parkingGuide = parkingGuide;
        this.publicTransportGuide = publicTransportGuide;

        this.mainColor = mainColor;
        this.fontFamily = fontFamily;
        this.bgmUrl = bgmUrl;

        this.status = status;
    }

    /** 새 청첩장을 생성한다. */
    public static Invitation create(
            Long userId,
            String templateId,
            String title,
            String groomName,
            String brideName,
            String groomPhoto,
            String bridePhoto,
            String groomContact,
            String brideContact,
            String groomParents,
            String brideParents,
            LocalDate weddingDate,
            String weddingTime,
            String venueName,
            String venueAddress,
            String venueDetail,
            Double latitude,
            Double longitude,
            String mainGreeting,
            String invitationMessage,
            String additionalMessage,
            String groomBank,
            String groomAccount,
            String groomAccountHolder,
            String brideBank,
            String brideAccount,
            String brideAccountHolder,
            String groomParentContact,
            String brideParentContact,
            String transportGuide,
            String parkingGuide,
            String publicTransportGuide,
            String mainColor,
            String fontFamily,
            String bgmUrl) {

        validateUserId(userId);
        validateTemplateId(templateId);
        validateTitle(title);

        return new Invitation(
                userId,
                templateId,
                title,
                groomName,
                brideName,
                groomPhoto,
                bridePhoto,
                groomContact,
                brideContact,
                groomParents,
                brideParents,
                weddingDate,
                weddingTime,
                venueName,
                venueAddress,
                venueDetail,
                latitude,
                longitude,
                mainGreeting,
                invitationMessage,
                additionalMessage,
                groomBank,
                groomAccount,
                groomAccountHolder,
                brideBank,
                brideAccount,
                brideAccountHolder,
                groomParentContact,
                brideParentContact,
                transportGuide,
                parkingGuide,
                publicTransportGuide,
                mainColor,
                fontFamily,
                bgmUrl,
                InvitationStatus.DRAFT
        );
    }

    /** 청첩장 내용을 수정한다. */
    public void update(
            String templateId,
            String title,
            String groomName,
            String brideName,
            String groomPhoto,
            String bridePhoto,
            String groomContact,
            String brideContact,
            String groomParents,
            String brideParents,
            LocalDate weddingDate,
            String weddingTime,
            String venueName,
            String venueAddress,
            String venueDetail,
            Double latitude,
            Double longitude,
            String mainGreeting,
            String invitationMessage,
            String additionalMessage,
            String groomBank,
            String groomAccount,
            String groomAccountHolder,
            String brideBank,
            String brideAccount,
            String brideAccountHolder,
            String groomParentContact,
            String brideParentContact,
            String transportGuide,
            String parkingGuide,
            String publicTransportGuide,
            String mainColor,
            String fontFamily,
            String bgmUrl) {

        validateTemplateId(templateId);
        validateTitle(title);

        this.templateId = templateId;
        this.title = title;

        this.groomName = groomName;
        this.brideName = brideName;
        this.groomPhoto = groomPhoto;
        this.bridePhoto = bridePhoto;
        this.groomContact = groomContact;
        this.brideContact = brideContact;
        this.groomParents = groomParents;
        this.brideParents = brideParents;

        this.weddingDate = weddingDate;
        this.weddingTime = weddingTime;
        this.venueName = venueName;
        this.venueAddress = venueAddress;
        this.venueDetail = venueDetail;
        this.latitude = latitude;
        this.longitude = longitude;

        this.mainGreeting = mainGreeting;
        this.invitationMessage = invitationMessage;
        this.additionalMessage = additionalMessage;

        this.groomBank = groomBank;
        this.groomAccount = groomAccount;
        this.groomAccountHolder = groomAccountHolder;
        this.brideBank = brideBank;
        this.brideAccount = brideAccount;
        this.brideAccountHolder = brideAccountHolder;

        this.groomParentContact = groomParentContact;
        this.brideParentContact = brideParentContact;

        this.transportGuide = transportGuide;
        this.parkingGuide = parkingGuide;
        this.publicTransportGuide = publicTransportGuide;

        this.mainColor = mainColor;
        this.fontFamily = fontFamily;
        this.bgmUrl = bgmUrl;
    }

    /** 청첩장을 발행 상태로 변경한다. */
    public void publish() {
        this.status = InvitationStatus.PUBLISHED;
    }

    private static void validateUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "사용자 ID는 필수입니다."
            );
        }
    }

    private static void validateTemplateId(String templateId) {
        if (templateId == null || templateId.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "템플릿 ID는 필수입니다."
            );
        }
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "청첩장 제목은 필수입니다."
            );
        }
    }
}