/**
 * recommendation — 테스트/추천 Bounded Context (담당: 다은).
 *
 * <p>포함 기능
 * <ul>
 *   <li>WEDU-003 심리테스트 — 추천에 필요한 사용자 성향 데이터 생성/저장/조회</li>
 *   <li>WEDU-004 맞춤 추천 — 테스트 결과 기반 프로포즈 스타일/상품 추천</li>
 * </ul>
 *
 * <p>예상 애그리게이트: {@code PsychTest}(문항/보기), {@code TestResult}(사용자별 성향 결과),
 * {@code Recommendation}(결과 기반 추천). 사용자는 {@code userId} 로만 참조한다(컨텍스트 간 결합 최소화).
 *
 * <p>계층 구조는 user 컨텍스트를 참고:
 * presentation(컨트롤러/DTO) · application(유스케이스) · domain(애그리게이트/VO/Repository 포트) ·
 * infrastructure(JPA 어댑터/외부 연동).
 */
package com.wedu.recommendation;
