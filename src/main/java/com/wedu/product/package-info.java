/**
 * product — 상품 Bounded Context (담당: 완규).
 *
 * <p>포함 기능
 * <ul>
 *   <li>WEDU-006 프로포즈 편집샵 — 상품 목록/검색/필터링</li>
 *   <li>WEDU-007 상품 상세 — 상세 정보·가격·옵션 조회</li>
 *   <li>WEDU-008 인기 추천 — 찜·담기 관심 수 기준 순위 산출·제공</li>
 * </ul>
 *
 * <p>예상 애그리게이트: {@code Product}(Root, 카테고리·가격·옵션·이미지), {@code Vendor}(업체).
 * 순위 산출은 주기 배치(scheduler)로 격리하고, 도메인은 산출 결과를 받는 형태로만 의존한다.
 * 평점·리뷰 수는 review 컨텍스트의 집계 서비스에서 가져와 응답에 얹는다.
 *
 * <p>계층 구조는 user 컨텍스트를 참고.
 */
package com.wedu.product;
