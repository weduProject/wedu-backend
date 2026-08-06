/**
 * community — 커뮤니티 Bounded Context (담당: 다은 / 커뮤니티 게시판은 후순위).
 *
 * <p>포함 기능
 * <ul>
 *   <li>WEDU-017 후기 / 평점 — 상품·업체 후기와 평점 관리</li>
 *   <li>WEDU-016 커뮤니티 — 게시글·댓글·좋아요 (후순위)</li>
 * </ul>
 *
 * <p>애그리게이트: {@code Review}(평점+본문, 대상 상품/업체를 id 로 참조),
 * {@code CommunityPost}, {@code CommunityComment}, 게시글·댓글 좋아요. 댓글과 좋아요는
 * 게시글·사용자를 객체로 직접 참조하지 않고 식별자만 보관해 경계를 분리한다.
 *
 * <p>계층 구조는 user 컨텍스트를 참고.
 */
package com.wedu.community;
