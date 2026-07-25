package com.wedu.product.domain;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 프로포즈 편집샵에서 판매하는 상품 Aggregate Root. */
@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductCategory category;

    @Column(nullable = false)
    private int price;

    @Column(name = "vendor_name", nullable = false)
    private String vendorName;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(length = 2000)
    private String description;

    private Product(
            String name,
            ProductCategory category,
            int price,
            String vendorName,
            String thumbnailUrl,
            String description) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.vendorName = vendorName;
        this.thumbnailUrl = thumbnailUrl;
        this.description = description;
    }

    /** 판매 상품을 등록한다. */
    public static Product create(
            String name,
            ProductCategory category,
            int price,
            String vendorName,
            String thumbnailUrl,
            String description) {
        validateName(name);
        validateCategory(category);
        validatePrice(price);
        validateVendorName(vendorName);
        return new Product(name, category, price, vendorName, thumbnailUrl, description);
    }

    /** 상품 가격을 변경한다. */
    public void changePrice(int newPrice) {
        validatePrice(newPrice);
        this.price = newPrice;
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "상품명은 필수입니다.");
        }
    }

    private static void validateCategory(ProductCategory category) {
        if (category == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "상품 카테고리는 필수입니다.");
        }
    }

    private static void validatePrice(int price) {
        if (price < 0) {
            throw new BusinessException(ErrorCode.PRODUCT_INVALID_PRICE);
        }
    }

    private static void validateVendorName(String vendorName) {
        if (vendorName == null || vendorName.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "판매처명은 필수입니다.");
        }
    }
}
