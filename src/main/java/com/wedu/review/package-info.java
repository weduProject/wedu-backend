/**
 * review — 상품 리뷰 Bounded Context.
 *
 * <p>포함 기능
 * <ul>
 *   <li>WEDU-012 상품 리뷰 — 평점·후기 작성/수정/삭제, 상품별 목록, 내 리뷰 목록</li>
 * </ul>
 *
 * <p>애그리게이트: {@code ProductReview}(Root). 상품은 id 로만 참조한다.
 *
 * <p>상품 조회 화면에 필요한 평균 평점·리뷰 수는 {@code ProductRatingService} 로만 노출하고,
 * product 컨텍스트가 이 경계를 통해 집계를 가져간다. 반대로 review 는 product 의 서비스를
 * 호출하지 않는다.
 */
package com.wedu.review;
