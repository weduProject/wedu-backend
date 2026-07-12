/**
 * product — 상품 Bounded Context (담당: 완규).
 *
 * <p>포함 기능
 * <ul>
 *   <li>WEDU-006 프로포즈 편집샵 — 상품 목록/검색/필터링</li>
 *   <li>WEDU-007 상품 상세 — 상세 정보·가격·옵션 조회</li>
 *   <li>WEDU-008 인기 추천 / 크롤링 — 인기 상품 데이터 수집·제공</li>
 * </ul>
 *
 * <p>예상 애그리게이트: {@code Product}(Root, 카테고리·가격·옵션·이미지), {@code Vendor}(업체).
 * 크롤링/수집은 외부 관심사이므로 infrastructure 의 어댑터(스케줄러·수집기)로 격리하고,
 * 도메인은 수집 결과를 받는 형태로만 의존한다.
 *
 * <p>계층 구조는 user 컨텍스트를 참고.
 */
package com.wedu.product;
