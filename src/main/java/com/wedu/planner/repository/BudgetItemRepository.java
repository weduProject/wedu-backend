package com.wedu.planner.repository;

import com.wedu.planner.domain.BudgetItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** 예산 항목의 사용자별 저장과 조회를 담당한다. */
public interface BudgetItemRepository extends JpaRepository<BudgetItem, Long> {

    List<BudgetItem> findAllByUserIdOrderByIdAsc(Long userId);

    Optional<BudgetItem> findByIdAndUserId(Long id, Long userId);
}
