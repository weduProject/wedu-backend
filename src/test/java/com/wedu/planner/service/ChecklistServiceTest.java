package com.wedu.planner.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wedu.global.error.BusinessException;
import com.wedu.planner.domain.ChecklistCategory;
import com.wedu.planner.domain.ChecklistItem;
import com.wedu.planner.dto.ChecklistCompletionRequest;
import com.wedu.planner.dto.ChecklistItemCreateRequest;
import com.wedu.planner.dto.ChecklistItemUpdateRequest;
import com.wedu.planner.dto.ChecklistOverviewResponse;
import com.wedu.planner.repository.ChecklistItemRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChecklistServiceTest {

    @Mock
    private ChecklistItemRepository checklistItemRepository;

    private ChecklistService checklistService;

    @BeforeEach
    void setUp() {
        checklistService = new ChecklistService(checklistItemRepository);
    }

    @Test
    @DisplayName("새 체크리스트 항목을 저장한다")
    void create() {
        ChecklistItemCreateRequest request =
                new ChecklistItemCreateRequest("예식장 계약", ChecklistCategory.CEREMONY);
        when(checklistItemRepository.save(any(ChecklistItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = checklistService.create(1L, request);

        assertThat(result.title()).isEqualTo("예식장 계약");
        assertThat(result.completed()).isFalse();
        verify(checklistItemRepository).save(any(ChecklistItem.class));
    }

    @Test
    @DisplayName("카테고리 필터와 관계없이 전체 진행률을 계산한다")
    void getChecklist() {
        ChecklistItem shooting =
                ChecklistItem.create(1L, "스튜디오 예약", ChecklistCategory.SHOOTING);
        when(checklistItemRepository.findAllByUserIdAndCategoryOrderByIdAsc(
                1L, ChecklistCategory.SHOOTING)).thenReturn(List.of(shooting));
        when(checklistItemRepository.countByUserId(1L)).thenReturn(3L);
        when(checklistItemRepository.countByUserIdAndCompletedTrue(1L)).thenReturn(1L);

        ChecklistOverviewResponse result =
                checklistService.getChecklist(1L, ChecklistCategory.SHOOTING);

        assertThat(result.totalCount()).isEqualTo(3);
        assertThat(result.completedCount()).isEqualTo(1);
        assertThat(result.remainingCount()).isEqualTo(2);
        assertThat(result.progressPercentage()).isEqualTo(33);
        assertThat(result.items()).hasSize(1);
    }

    @Test
    @DisplayName("항목이 없으면 진행률은 0퍼센트다")
    void getEmptyChecklist() {
        when(checklistItemRepository.findAllByUserIdOrderByIdAsc(1L)).thenReturn(List.of());
        when(checklistItemRepository.countByUserId(1L)).thenReturn(0L);
        when(checklistItemRepository.countByUserIdAndCompletedTrue(1L)).thenReturn(0L);

        ChecklistOverviewResponse result = checklistService.getChecklist(1L, null);

        assertThat(result.progressPercentage()).isZero();
        assertThat(result.items()).isEmpty();
    }

    @Test
    @DisplayName("소유한 항목의 정보와 완료 상태를 수정하고 삭제한다")
    void updateCompleteAndDelete() {
        ChecklistItem item = ChecklistItem.create(1L, "웨딩밴드", ChecklistCategory.JEWELRY);
        when(checklistItemRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(item));

        var updated = checklistService.update(
                1L, 10L, new ChecklistItemUpdateRequest("웨딩밴드 맞추기", ChecklistCategory.JEWELRY));
        var completed = checklistService.changeCompletion(
                1L, 10L, new ChecklistCompletionRequest(true));
        checklistService.delete(1L, 10L);

        assertThat(updated.title()).isEqualTo("웨딩밴드 맞추기");
        assertThat(completed.completed()).isTrue();
        verify(checklistItemRepository).delete(item);
    }

    @Test
    @DisplayName("소유하지 않은 항목은 수정하거나 삭제할 수 없다")
    void rejectUnownedItem() {
        when(checklistItemRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> checklistService.update(
                        1L,
                        10L,
                        new ChecklistItemUpdateRequest("수정", ChecklistCategory.BASIC)))
                .isInstanceOf(BusinessException.class);

        verify(checklistItemRepository, never()).delete(any());
    }
}
