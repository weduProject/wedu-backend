package com.wedu.planner.repository;

import com.wedu.planner.domain.DDay;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DDayRepository extends JpaRepository<DDay, Long> {

    Optional<DDay> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
