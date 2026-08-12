-- 인기 상품 순위가 자체 카탈로그 상품을 가리키도록 product_id 를 추가한다.
-- 외부 출처에서 수집한 행은 대응하는 상품이 없을 수 있어 NULL 을 허용한다.
ALTER TABLE popular_products
    ADD COLUMN product_id BIGINT NULL AFTER id,
    ADD KEY idx_popular_products_product_id (product_id);
