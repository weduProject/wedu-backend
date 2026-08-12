package com.wedu.invitation.domain;

import jakarta.persistence.*;
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
        return new GalleryImage(
                invitation,
                imageUrl,
                sortOrder
        );
    }
}