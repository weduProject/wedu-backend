package com.wedu.planner.service;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.planner.domain.DDay;
import com.wedu.planner.dto.DDayResponse;
import com.wedu.planner.repository.DDayRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 인증 사용자의 결혼식 D-day 유스케이스를 처리한다. */
@Service
@RequiredArgsConstructor
public class DDayService {

    private static final ZoneId WEDDING_ZONE = ZoneId.of("Asia/Seoul");

    private final DDayRepository dDayRepository;
    private final Clock clock;

    /** 사용자의 결혼식 D-day를 하나만 생성한다. */
    @Transactional
    public DDayResponse create(Long userId, LocalDate weddingDate) {
        if (dDayRepository.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.PLANNER_DDAY_ALREADY_EXISTS);
        }
        LocalDate today = today();
        DDay dDay = DDay.create(userId, weddingDate, today);
        try {
            return DDayResponse.from(dDayRepository.saveAndFlush(dDay), today);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.PLANNER_DDAY_ALREADY_EXISTS);
        }
    }

    /** 사용자가 등록한 결혼식 D-day를 조회한다. */
    @Transactional(readOnly = true)
    public DDayResponse getMyDDay(Long userId) {
        return DDayResponse.from(findByUserIdOrThrow(userId), today());
    }

    /** 사용자의 결혼식 날짜를 변경한다. */
    @Transactional
    public DDayResponse update(Long userId, LocalDate weddingDate) {
        DDay dDay = findByUserIdOrThrow(userId);
        LocalDate today = today();
        dDay.changeWeddingDate(weddingDate, today);
        return DDayResponse.from(dDay, today);
    }

    /** 사용자의 결혼식 D-day를 삭제한다. */
    @Transactional
    public void delete(Long userId) {
        dDayRepository.delete(findByUserIdOrThrow(userId));
    }

    private DDay findByUserIdOrThrow(Long userId) {
        return dDayRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLANNER_DDAY_NOT_FOUND));
    }

    private LocalDate today() {
        return LocalDate.now(clock.withZone(WEDDING_ZONE));
    }
}
