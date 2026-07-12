/**
 * proposal — 견적/장바구니 Bounded Context (담당: 완규).
 *
 * <p>포함 기능
 * <ul>
 *   <li>WEDU-009 나만의 프로포즈 만들기 — 선택 옵션 저장·견적 계산</li>
 *   <li>WEDU-010 장바구니 / 견적함 — 상품 담기·견적 관리</li>
 *   <li>WEDU-011 찜하기 — 관심 상품 저장/삭제/조회</li>
 * </ul>
 *
 * <p>예상 애그리게이트: {@code Proposal}(Root, 선택 항목 묶음+견적 합계), {@code Wishlist}(찜 목록).
 * 금액은 {@code Money} 같은 값 객체로 다뤄 계산 규칙(합계·통화)을 도메인에 둔다.
 * 상품은 {@code productId} 로만 참조하고, 표시에 필요한 상품 정보는 조회 시점에 product 컨텍스트에서 가져온다.
 *
 * <p>계층 구조는 user 컨텍스트를 참고.
 */
package com.wedu.proposal;
