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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DDayService {

    private static final ZoneId WEDDING_ZONE = ZoneId.of("Asia/Seoul");

    private final DDayRepository dDayRepository;
    private final Clock clock;

    @Transactional
    public DDayResponse create(Long userId, LocalDate weddingDate) {
        if (dDayRepository.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.PLANNER_DDAY_ALREADY_EXISTS);
        }
        LocalDate today = today();
        DDay dDay = DDay.create(userId, weddingDate, today);
        return DDayResponse.from(dDayRepository.save(dDay), today);
    }

    @Transactional(readOnly = true)
    public DDayResponse getMyDDay(Long userId) {
        return DDayResponse.from(findByUserIdOrThrow(userId), today());
    }

    @Transactional
    public DDayResponse update(Long userId, LocalDate weddingDate) {
        DDay dDay = findByUserIdOrThrow(userId);
        LocalDate today = today();
        dDay.changeWeddingDate(weddingDate, today);
        return DDayResponse.from(dDay, today);
    }

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
