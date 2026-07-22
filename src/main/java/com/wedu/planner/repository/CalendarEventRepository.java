package com.wedu.planner.repository;

import com.wedu.planner.domain.CalendarEvent;
import com.wedu.planner.domain.CalendarEventCategory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** 캘린더 일정 영속성 접근 지점. */
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    List<CalendarEvent> findAllByUserIdAndEventDateBetweenOrderByEventDateAscEventTimeAscIdAsc(
            Long userId, LocalDate startDate, LocalDate endDate);

    List<CalendarEvent>
            findAllByUserIdAndCategoryAndEventDateBetweenOrderByEventDateAscEventTimeAscIdAsc(
                    Long userId,
                    CalendarEventCategory category,
                    LocalDate startDate,
                    LocalDate endDate);

    List<CalendarEvent> findAllByUserIdAndEventDateGreaterThanEqualOrderByEventDateAscEventTimeAscIdAsc(
            Long userId, LocalDate startDate, Pageable pageable);

    List<CalendarEvent>
            findAllByUserIdAndCategoryAndEventDateGreaterThanEqualOrderByEventDateAscEventTimeAscIdAsc(
                    Long userId,
                    CalendarEventCategory category,
                    LocalDate startDate,
                    Pageable pageable);

    Optional<CalendarEvent> findByIdAndUserId(Long id, Long userId);
}
