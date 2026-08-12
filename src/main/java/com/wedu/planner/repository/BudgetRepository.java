package com.wedu.planner.repository;

import com.wedu.planner.domain.Budget;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByUserId(Long userId);
}
