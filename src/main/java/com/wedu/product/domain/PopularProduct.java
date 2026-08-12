package com.wedu.product.domain;

import com.wedu.global.common.BaseTimeEntity;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 인기 상품 순위 한 줄을 담는 Aggregate Root.
 *
 * <p>순위 산출(집계/스케줄러)은 외부 관심사이므로 이 엔티티는 산출 결과를 받아 저장하는
 * 형태로만 관여한다. 순위는 갱신 시점의 스냅샷이며, 이름·가격은 그때의 값을 그대로 남긴다.
 */
@Getter
@Entity
@Table(name = "popular_products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularProduct extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 순위에 오른 자체 카탈로그 상품. 외부 출처에서 수집한 순위는 대응 상품이 없어 null 이다. */
    @Column(name = "product_id")
    private Long productId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(name = "source_name", nullable = false)
    private String sourceName;

    @Column(name = "source_url", nullable = false, length = 1000)
    private String sourceUrl;

    @Column(name = "thumbnail_url", length = 1000)
    private String thumbnailUrl;

    @Column(name = "rank_no", nullable = false)
    private int rank;

    private PopularProduct(
            Long productId,
            String name,
            int price,
            String sourceName,
            String sourceUrl,
            String thumbnailUrl,
            int rank) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.rank = rank;
    }

    /** 자체 카탈로그 상품의 인기 순위를 등록한다. */
    public static PopularProduct rank(
            Long productId,
            String name,
            int price,
            String sourceName,
            String sourceUrl,
            String thumbnailUrl,
            int rank) {
        if (productId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "상품 식별자는 필수입니다.");
        }
        return collect(productId, name, price, sourceName, sourceUrl, thumbnailUrl, rank);
    }

    /** 외부 출처에서 수집한 인기 상품 항목을 등록한다. 대응하는 자체 상품이 없다. */
    public static PopularProduct collect(
            String name, int price, String sourceName, String sourceUrl, String thumbnailUrl, int rank) {
        return collect(null, name, price, sourceName, sourceUrl, thumbnailUrl, rank);
    }

    private static PopularProduct collect(
            Long productId,
            String name,
            int price,
            String sourceName,
            String sourceUrl,
            String thumbnailUrl,
            int rank) {
        validateName(name);
        validatePrice(price);
        validateSource(sourceName, sourceUrl, thumbnailUrl);
        validateRank(rank);
        return new PopularProduct(productId, name, price, sourceName, sourceUrl, thumbnailUrl, rank);
    }

    /** 다음 수집 배치에서 갱신된 순위/가격을 반영한다. */
    public void refresh(int price, int rank) {
        validatePrice(price);
        validateRank(rank);
        this.price = price;
        this.rank = rank;
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank() || name.length() > 255) {
            throw new BusinessException(ErrorCode.PRODUCT_INVALID_NAME);
        }
    }

    private static void validatePrice(int price) {
        if (price < 0) {
            throw new BusinessException(ErrorCode.PRODUCT_INVALID_PRICE);
        }
    }

    private static void validateSource(String sourceName, String sourceUrl, String thumbnailUrl) {
        if (sourceName == null || sourceName.isBlank() || sourceName.length() > 255) {
            throw new BusinessException(ErrorCode.PRODUCT_INVALID_SOURCE);
        }
        if (sourceUrl == null || sourceUrl.isBlank() || sourceUrl.length() > 1000) {
            throw new BusinessException(ErrorCode.PRODUCT_INVALID_SOURCE);
        }
        if (thumbnailUrl != null && thumbnailUrl.length() > 1000) {
            throw new BusinessException(ErrorCode.PRODUCT_INVALID_SOURCE);
        }
    }

    private static void validateRank(int rank) {
        if (rank < 1) {
            throw new BusinessException(ErrorCode.PRODUCT_INVALID_RANK);
        }
    }
}
