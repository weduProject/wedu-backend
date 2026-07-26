package com.wedu.recommendation.application;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.recommendation.domain.PsychologicalTestResult;
import com.wedu.recommendation.domain.enums.PriorityValue;
import com.wedu.recommendation.infrastructure.PsychologicalTestResultRepository;
import com.wedu.recommendation.presentation.dto.PrioritySelectionRequest;
import com.wedu.recommendation.presentation.dto.PsychologicalTestSubmitRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PsychologicalTestService {

    private final PsychologicalTestResultRepository psychologicalTestResultRepository;

    /**
     * 사용자의 심리테스트 응답을 저장한다.
     *
     * <p>같은 사용자가 다시 제출하면 기존 결과를 삭제하고
     * 새로운 결과로 교체한다.
     *
     * @return 저장된 심리테스트 결과 ID
     */
    @Transactional
    public Long submit(
            Long userId,
            PsychologicalTestSubmitRequest request
    ) {
        if (request == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "심리테스트 응답은 필수입니다."
            );
        }

        List<PriorityValue> sortedPriorityValues =
                convertPriorityValues(request.priorityValues());

        psychologicalTestResultRepository
                .findByUserId(userId)
                .ifPresent(psychologicalTestResultRepository::delete);

        PsychologicalTestResult result = PsychologicalTestResult.create(
                userId,
                request.moodType(),
                request.locationType(),
                request.region(),
                request.preparationType(),
                request.requiredServices(),
                sortedPriorityValues,
                request.budgetRange(),
                request.excludedElements(),
                request.scheduleRange(),
                request.partnerMbti()
        );

        PsychologicalTestResult savedResult =
                psychologicalTestResultRepository.save(result);

        return savedResult.getId();
    }

    /**
     * DTO의 rank를 기준으로 우선순위를 정렬한 뒤,
     * Entity에 저장할 PriorityValue 목록으로 변환한다.
     */
    private List<PriorityValue> convertPriorityValues(
            List<PrioritySelectionRequest> prioritySelections
    ) {
        if (prioritySelections == null || prioritySelections.size() != 2) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "우선순위는 정확히 두 개를 선택해야 합니다."
            );
        }

        boolean hasFirstRank = prioritySelections.stream()
                .anyMatch(selection -> selection.rank() == 1);

        boolean hasSecondRank = prioritySelections.stream()
                .anyMatch(selection -> selection.rank() == 2);

        if (!hasFirstRank || !hasSecondRank) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "우선순위 등수는 1과 2를 각각 한 번씩 입력해야 합니다."
            );
        }

        if (prioritySelections.stream()
                .anyMatch(selection -> selection.value() == null)) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "우선순위 항목은 필수입니다."
            );
        }

        long distinctValueCount = prioritySelections.stream()
                .map(PrioritySelectionRequest::value)
                .distinct()
                .count();

        if (distinctValueCount != prioritySelections.size()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "동일한 우선순위 항목을 중복 선택할 수 없습니다."
            );
        }

        return prioritySelections.stream()
                .sorted(Comparator.comparingInt(
                        PrioritySelectionRequest::rank
                ))
                .map(PrioritySelectionRequest::value)
                .toList();
    }
}