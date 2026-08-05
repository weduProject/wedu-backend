package com.wedu.community.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wedu.global.error.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommunityPostTest {

    @Test
    @DisplayName("게시글을 생성할 때 제목과 본문의 앞뒤 공백을 제거한다")
    void create() {
        CommunityPost post = CommunityPost.create(
                1L,
                "  결혼 준비 질문  ",
                "  예식장은 언제 예약하면 좋을까요?  ",
                PostTheme.WEDDING_PREPARATION,
                true);

        assertThat(post.getAuthorId()).isEqualTo(1L);
        assertThat(post.getTitle()).isEqualTo("결혼 준비 질문");
        assertThat(post.getContent()).isEqualTo("예식장은 언제 예약하면 좋을까요?");
        assertThat(post.isAnonymous()).isTrue();
    }

    @Test
    @DisplayName("게시글의 제목, 본문, 테마와 익명 여부를 수정한다")
    void update() {
        CommunityPost post = post();

        post.update("신혼집 질문", "지역을 고민 중입니다.", PostTheme.NEWLYWED_LIFE, true);

        assertThat(post.getTitle()).isEqualTo("신혼집 질문");
        assertThat(post.getContent()).isEqualTo("지역을 고민 중입니다.");
        assertThat(post.getTheme()).isEqualTo(PostTheme.NEWLYWED_LIFE);
        assertThat(post.isAnonymous()).isTrue();
    }

    @Test
    @DisplayName("작성자만 자신의 게시글 소유자로 판단한다")
    void ownership() {
        CommunityPost post = post();

        assertThat(post.isOwnedBy(1L)).isTrue();
        assertThat(post.isOwnedBy(2L)).isFalse();
    }

    @Test
    @DisplayName("필수값이 없거나 공백이면 게시글을 생성할 수 없다")
    void rejectMissingValues() {
        assertThatThrownBy(() -> CommunityPost.create(
                        1L, " ", "본문", PostTheme.PROPOSAL, false))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> CommunityPost.create(
                        1L, "제목", " ", PostTheme.PROPOSAL, false))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> CommunityPost.create(
                        1L, "제목", "본문", null, false))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("제목 100자와 본문 5000자를 초과할 수 없다")
    void rejectTooLongValues() {
        assertThatThrownBy(() -> CommunityPost.create(
                        1L, "가".repeat(101), "본문", PostTheme.TIP_SHARING, false))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> CommunityPost.create(
                        1L, "제목", "가".repeat(5001), PostTheme.TIP_SHARING, false))
                .isInstanceOf(BusinessException.class);
    }

    private CommunityPost post() {
        return CommunityPost.create(
                1L, "프로포즈 장소 추천", "서울 지역을 찾고 있습니다.", PostTheme.PROPOSAL, false);
    }
}
