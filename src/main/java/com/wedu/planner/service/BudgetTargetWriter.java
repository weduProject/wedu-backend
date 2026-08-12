package com.wedu.planner.service;

import com.wedu.planner.domain.Budget;
import com.wedu.planner.dto.BudgetTargetResponse;
import com.wedu.planner.repository.BudgetRepository;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** 목표 예산 쓰기를 독립 트랜잭션으로 실행해 유니크 키 충돌 후 안전하게 재시도할 수 있게 한다. */
@Service
@RequiredArgsConstructor
public class BudgetTargetWriter {

    private final BudgetRepository budgetRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BudgetTargetResponse write(Long userId, BigDecimal totalBudget) {
        Optional<Budget> existingBudget = budgetRepository.findByUserId(userId);
        Budget budget = existingBudget.orElseGet(() -> Budget.create(userId, totalBudget));
        existingBudget.ifPresent(existing -> existing.updateTotalBudget(totalBudget));
        return BudgetTargetResponse.from(budgetRepository.saveAndFlush(budget));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BudgetTargetResponse updateExisting(Long userId, BigDecimal totalBudget) {
        Budget budget = budgetRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "Target budget was not found after a unique-key conflict"));
        budget.updateTotalBudget(totalBudget);
        return BudgetTargetResponse.from(budgetRepository.saveAndFlush(budget));
    }
}
