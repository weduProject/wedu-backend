package com.wedu.invitation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GalleryImageCreateRequest(

        @NotBlank(message = "이미지 URL은 필수입니다.")
        String imageUrl,

        @NotNull(message = "정렬 순서는 필수입니다.")
        Integer sortOrder

) {
}