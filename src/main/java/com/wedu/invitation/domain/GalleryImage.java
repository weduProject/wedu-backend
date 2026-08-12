package com.wedu.invitation.domain;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "gallery_images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GalleryImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitation_id", nullable = false)
    private Invitation invitation;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private Integer sortOrder;

    private GalleryImage(
            Invitation invitation,
            String imageUrl,
            Integer sortOrder
    ) {
        this.invitation = invitation;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }

    public static GalleryImage create(
            Invitation invitation,
            String imageUrl,
            Integer sortOrder
    ) {
        validate(invitation, imageUrl, sortOrder);

        return new GalleryImage(
                invitation,
                imageUrl,
                sortOrder
        );
    }

    private static void validate(
            Invitation invitation,
            String imageUrl,
            Integer sortOrder
    ) {
        if (invitation == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "청첩장 정보는 필수입니다."
            );
        }

        if (imageUrl == null || imageUrl.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "이미지 URL은 필수입니다."
            );
        }

        if (sortOrder == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "정렬 순서는 필수입니다."
            );
        }
    }
}