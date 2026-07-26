package com.wedu.planner.service;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.planner.domain.ChecklistCategory;
import com.wedu.planner.domain.ChecklistItem;
import com.wedu.planner.dto.ChecklistCompletionRequest;
import com.wedu.planner.dto.ChecklistItemCreateRequest;
import com.wedu.planner.dto.ChecklistItemResponse;
import com.wedu.planner.dto.ChecklistItemUpdateRequest;
import com.wedu.planner.dto.ChecklistOverviewResponse;
import com.wedu.planner.repository.ChecklistItemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 체크리스트 항목과 전체 진행률 유스케이스를 처리한다. */
@Service
@RequiredArgsConstructor
public class ChecklistService {

    private final ChecklistItemRepository checklistItemRepository;

    /** 사용자가 입력한 체크리스트 항목을 미완료 상태로 저장한다. */
    @Transactional
    public ChecklistItemResponse create(Long userId, ChecklistItemCreateRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "체크리스트 생성 요청은 필수입니다.");
        }
        ChecklistItem item = ChecklistItem.create(userId, request.title(), request.category());
        return ChecklistItemResponse.from(checklistItemRepository.save(item));
    }

    /** 전체 진행률과 전체 또는 카테고리별 항목을 등록 순서로 조회한다. */
    @Transactional(readOnly = true)
    public ChecklistOverviewResponse getChecklist(
            Long userId,
            ChecklistCategory category) {
        validateUserId(userId);
        List<ChecklistItem> items = category == null
                ? checklistItemRepository.findAllByUserIdOrderByIdAsc(userId)
                : checklistItemRepository.findAllByUserIdAndCategoryOrderByIdAsc(
                        userId, category);
        long totalCount = checklistItemRepository.countByUserId(userId);
        long completedCount = checklistItemRepository.countByUserIdAndCompletedTrue(userId);
        return ChecklistOverviewResponse.of(
                totalCount,
                completedCount,
                items.stream().map(ChecklistItemResponse::from).toList());
    }

    /** 소유한 체크리스트 항목의 제목과 카테고리를 변경한다. */
    @Transactional
    public ChecklistItemResponse update(
            Long userId,
            Long itemId,
            ChecklistItemUpdateRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "체크리스트 수정 요청은 필수입니다.");
        }
        ChecklistItem item = findOwnedItem(userId, itemId);
        item.update(request.title(), request.category());
        return ChecklistItemResponse.from(item);
    }

    /** 소유한 항목의 완료 여부를 요청값과 동일하게 변경한다. */
    @Transactional
    public ChecklistItemResponse changeCompletion(
            Long userId,
            Long itemId,
            ChecklistCompletionRequest request) {
        if (request == null || request.completed() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "완료 상태는 필수입니다.");
        }
        ChecklistItem item = findOwnedItem(userId, itemId);
        item.changeCompletion(request.completed());
        return ChecklistItemResponse.from(item);
    }

    /** 소유한 체크리스트 항목을 삭제한다. */
    @Transactional
    public void delete(Long userId, Long itemId) {
        checklistItemRepository.delete(findOwnedItem(userId, itemId));
    }

    private ChecklistItem findOwnedItem(Long userId, Long itemId) {
        validateUserId(userId);
        if (itemId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "체크리스트 항목 식별자는 필수입니다.");
        }
        return checklistItemRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.PLANNER_CHECKLIST_ITEM_NOT_FOUND));
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "사용자 식별자는 필수입니다.");
        }
    }
}
