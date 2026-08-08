package com.wedu.invitation.dto;

import com.wedu.invitation.domain.GalleryImage;

public record GalleryImageResponse(
        Long id,
        String imageUrl,
        Integer sortOrder
) {

    public static GalleryImageResponse from(GalleryImage galleryImage) {
        return new GalleryImageResponse(
                galleryImage.getId(),
                galleryImage.getImageUrl(),
                galleryImage.getSortOrder()
        );
    }
}