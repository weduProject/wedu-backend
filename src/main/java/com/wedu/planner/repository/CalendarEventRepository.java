package com.wedu.planner.repository;

import com.wedu.planner.domain.CalendarEvent;
import com.wedu.planner.domain.CalendarEventCategory;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 캘린더 일정 영속성 접근 지점. */
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    List<CalendarEvent> findAllByUserIdAndEventDateBetweenOrderByEventDateAscEventAtAscIdAsc(
            Long userId, LocalDate startDate, LocalDate endDate);

    List<CalendarEvent>
            findAllByUserIdAndCategoryAndEventDateBetweenOrderByEventDateAscEventAtAscIdAsc(
                    Long userId,
                    CalendarEventCategory category,
                    LocalDate startDate,
                    LocalDate endDate);

    @Query("""
            SELECT event
            FROM CalendarEvent event
            WHERE event.userId = :userId
              AND (event.eventDate > :today
                   OR (event.eventDate = :today
                       AND (event.eventAt IS NULL OR event.eventAt >= :now)))
            ORDER BY event.eventDate ASC,
                     CASE WHEN event.eventAt IS NULL THEN 0 ELSE 1 END ASC,
                     event.eventAt ASC,
                     event.id ASC
            """)
    List<CalendarEvent> findUpcoming(
            @Param("userId") Long userId,
            @Param("today") LocalDate today,
            @Param("now") Instant now,
            Pageable pageable);

    @Query("""
            SELECT event
            FROM CalendarEvent event
            WHERE event.userId = :userId
              AND event.category = :category
              AND (event.eventDate > :today
                   OR (event.eventDate = :today
                       AND (event.eventAt IS NULL OR event.eventAt >= :now)))
            ORDER BY event.eventDate ASC,
                     CASE WHEN event.eventAt IS NULL THEN 0 ELSE 1 END ASC,
                     event.eventAt ASC,
                     event.id ASC
            """)
    List<CalendarEvent> findUpcomingByCategory(
            @Param("userId") Long userId,
            @Param("category") CalendarEventCategory category,
            @Param("today") LocalDate today,
            @Param("now") Instant now,
            Pageable pageable);

    Optional<CalendarEvent> findByIdAndUserId(Long id, Long userId);
}
