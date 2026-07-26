package com.wedu.planner.service;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.planner.domain.BudgetItem;
import com.wedu.planner.dto.BudgetCompletionRequest;
import com.wedu.planner.dto.BudgetItemCreateRequest;
import com.wedu.planner.dto.BudgetItemResponse;
import com.wedu.planner.dto.BudgetItemUpdateRequest;
import com.wedu.planner.dto.BudgetOverviewResponse;
import com.wedu.planner.repository.BudgetItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 예산 항목 CRUD와 사용자별 예산 현황 조회 유스케이스를 처리한다. */
@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetItemRepository budgetItemRepository;

    /** 사용자의 예산 항목을 미완료 상태로 저장한다. */
    @Transactional
    public BudgetItemResponse create(Long userId, BudgetItemCreateRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "예산 항목 생성 요청은 필수입니다.");
        }
        BudgetItem item = BudgetItem.create(
                userId,
                request.title(),
                request.category(),
                request.plannedAmount(),
                request.spentAmount());
        return BudgetItemResponse.from(budgetItemRepository.save(item));
    }

    /** 전체 요약과 5개 카테고리의 항목을 등록 순서로 조회한다. */
    @Transactional(readOnly = true)
    public BudgetOverviewResponse getOverview(Long userId) {
        validateUserId(userId);
        return BudgetOverviewResponse.from(
                budgetItemRepository.findAllByUserIdOrderByIdAsc(userId));
    }

    /** 소유한 예산 항목의 이름, 분류와 금액을 변경한다. */
    @Transactional
    public BudgetItemResponse update(
            Long userId,
            Long itemId,
            BudgetItemUpdateRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "예산 항목 수정 요청은 필수입니다.");
        }
        BudgetItem item = findOwnedItem(userId, itemId);
        item.update(
                request.title(),
                request.category(),
                request.plannedAmount(),
                request.spentAmount());
        return BudgetItemResponse.from(item);
    }

    /** 소유한 예산 항목의 결제 완료 여부를 요청값과 동일하게 변경한다. */
    @Transactional
    public BudgetItemResponse changeCompletion(
            Long userId,
            Long itemId,
            BudgetCompletionRequest request) {
        if (request == null || request.completed() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "완료 상태는 필수입니다.");
        }
        BudgetItem item = findOwnedItem(userId, itemId);
        item.changeCompletion(request.completed());
        return BudgetItemResponse.from(item);
    }

    /** 소유한 예산 항목을 삭제한다. */
    @Transactional
    public void delete(Long userId, Long itemId) {
        budgetItemRepository.delete(findOwnedItem(userId, itemId));
    }

    private BudgetItem findOwnedItem(Long userId, Long itemId) {
        validateUserId(userId);
        if (itemId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "예산 항목 식별자는 필수입니다.");
        }
        return budgetItemRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.PLANNER_BUDGET_ITEM_NOT_FOUND));
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "사용자 식별자는 필수입니다.");
        }
    }
}
