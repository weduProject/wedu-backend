package com.wedu.invitation.dto;

public record GalleryImageCreateRequest(
        String imageUrl,
        Integer sortOrder
) {
}