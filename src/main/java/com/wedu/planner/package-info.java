/**
 * planner — 일정 관리 Bounded Context (담당: 경환).
 *
 * <p>포함 기능
 * <ul>
 *   <li>WEDU-012 D-day 관리 — 프로포즈 일정 D-day</li>
 *   <li>WEDU-013 캘린더 — 일정 등록/조회</li>
 *   <li>WEDU-014 체크리스트 — 준비 항목 관리</li>
 *   <li>WEDU-015 예산 관리 — 예산 등록·지출 관리</li>
 * </ul>
 *
 * <p>예상 애그리게이트: {@code DDay}, {@code CalendarEvent}, {@code Checklist}(+항목), {@code Budget}(+지출).
 * 예산의 금액·지출 합계는 {@code Money} 값 객체로 다룬다. 모든 개인 데이터는 {@code userId} 로 소유를 구분한다.
 *
 * <p>계층 구조는 user 컨텍스트를 참고.
 */
package com.wedu.planner;
