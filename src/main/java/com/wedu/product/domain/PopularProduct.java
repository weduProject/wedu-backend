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
 * 외부 수집(크롤링)으로 얻은 인기 상품 정보를 담는 Aggregate Root.
 *
 * <p>수집(스케줄러/크롤러)은 외부 관심사이므로 이 엔티티는 수집 결과를 받아 저장하는
 * 형태로만 관여하고, 크롤링 자체는 다루지 않는다.
 */
@Getter
@Entity
@Table(name = "popular_products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularProduct extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
            String name, int price, String sourceName, String sourceUrl, String thumbnailUrl, int rank) {
        this.name = name;
        this.price = price;
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.rank = rank;
    }

    /** 크롤링으로 수집한 인기 상품 항목을 등록한다. */
    public static PopularProduct collect(
            String name, int price, String sourceName, String sourceUrl, String thumbnailUrl, int rank) {
        validateName(name);
        validatePrice(price);
        validateSource(sourceName, sourceUrl, thumbnailUrl);
        validateRank(rank);
        return new PopularProduct(name, price, sourceName, sourceUrl, thumbnailUrl, rank);
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
