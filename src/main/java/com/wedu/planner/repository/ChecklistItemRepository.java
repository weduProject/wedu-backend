package com.wedu.planner.repository;

import com.wedu.planner.domain.ChecklistCategory;
import com.wedu.planner.domain.ChecklistItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** 체크리스트 항목의 사용자별 저장과 조회를 담당한다. */
public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {

    List<ChecklistItem> findAllByUserIdOrderByIdAsc(Long userId);

    List<ChecklistItem> findAllByUserIdAndCategoryOrderByIdAsc(
            Long userId, ChecklistCategory category);

    long countByUserId(Long userId);

    long countByUserIdAndCompletedTrue(Long userId);

    Optional<ChecklistItem> findByIdAndUserId(Long id, Long userId);
}
