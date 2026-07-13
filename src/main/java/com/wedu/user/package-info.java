/**
 * user — 사용자/인증 도메인 (담당: 미미).
 *
 * <p>포함 기능
 * <ul>
 *   <li>WEDU-001 회원 관리 — 소셜 로그인(KAKAO/NAVER/GOOGLE) 기반 가입/식별, JWT 발급</li>
 *   <li>WEDU-002 온보딩 — 최초 로그인 여부·초기 설정</li>
 *   <li>WEDU-018 마이페이지 — 프로필 조회/수정</li>
 * </ul>
 *
 * <p>핵심 엔티티: {@link com.wedu.user.domain.User}. 인증 토큰 발급/검증 자체는 공통 기술이라
 * {@code com.wedu.global.security} 에 두고, 소셜 로그인 성공 후 회원 식별/가입·토큰 발급 유스케이스가
 * 이 도메인으로 성장할 자리다(OAuth2 success handler → 회원 조회/가입 → JWT 발급).
 *
 * <p>이 도메인이 전 계층(controller/dto/service/domain/repository)을 채운 레퍼런스 구현이다.
 * 다른 도메인은 이 구조를 본떠 확장한다.
 */
package com.wedu.user;
