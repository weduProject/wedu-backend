package com.wedu.product.dto;

/** DB에 저장된 상대 경로 썸네일을 공개 URL로 만든다. */
final class ProductThumbnailUrls {

    private ProductThumbnailUrls() {}

    static String toPublicUrl(String thumbnailUrl, String publicBaseUrl) {
        if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
            return null;
        }
        String trimmed = thumbnailUrl.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        String base = publicBaseUrl == null ? "" : publicBaseUrl.strip();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String path = trimmed.startsWith("/") ? trimmed : "/" + trimmed;
        return base + path;
    }
}
