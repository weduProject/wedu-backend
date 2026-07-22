package com.wedu.planner.repository;

import com.wedu.planner.domain.CalendarEvent;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** 캘린더 일정 영속성 접근 지점. */
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    List<CalendarEvent> findAllByUserIdAndEventDateBetweenOrderByEventDateAscIdAsc(
            Long userId, LocalDate startDate, LocalDate endDate);
}
