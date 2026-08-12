package com.wedu.recommendation.repository;

import com.wedu.recommendation.domain.PsychologicalTestResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PsychologicalTestResultRepository
        extends JpaRepository<PsychologicalTestResult, Long> {

    Optional<PsychologicalTestResult> findByUserId(Long userId);

}