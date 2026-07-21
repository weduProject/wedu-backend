package com.wedu.planner.repository;

import com.wedu.planner.domain.DDay;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** 결혼식 D-day 영속성 접근 인터페이스. */
public interface DDayRepository extends JpaRepository<DDay, Long> {

    /** 사용자 식별자로 등록된 D-day를 조회한다. */
    Optional<DDay> findByUserId(Long userId);

    /** 사용자의 D-day 등록 여부를 확인한다. */
    boolean existsByUserId(Long userId);
}
