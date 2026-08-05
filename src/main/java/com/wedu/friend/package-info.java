/**
 * friend — 친구 Bounded Context.
 *
 * <p>포함 기능: 이메일로 사용자를 찾아 친구 추가, 친구 목록 조회, 친구 삭제.
 * 요청/수락 단계 없이 추가 즉시 양방향 관계가 성립하는 단순 모델이다.
 * 초대 링크, 공유 페이지 권한(체크리스트/캘린더 공동 편집)은 이후 별도 컨텍스트에서 확장한다.
 *
 * <p>애그리게이트: {@code Friendship}(Root, userId·friendUserId 쌍). 사용자는 id 로만 참조하고,
 * 표시에 필요한 프로필 정보는 조회 시점에 user 컨텍스트에서 가져온다.
 *
 * <p>계층 구조는 user 컨텍스트를 참고.
 */
package com.wedu.friend;
