package com.wedu.recommendation.domain;

import com.wedu.global.common.BaseTimeEntity;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.recommendation.domain.enums.BudgetRange;
import com.wedu.recommendation.domain.enums.ExcludedElement;
import com.wedu.recommendation.domain.enums.LocationType;
import com.wedu.recommendation.domain.enums.MoodType;
import com.wedu.recommendation.domain.enums.PartnerMbti;
import com.wedu.recommendation.domain.enums.PreparationType;
import com.wedu.recommendation.domain.enums.PriorityValue;
import com.wedu.recommendation.domain.enums.RegionType;
import com.wedu.recommendation.domain.enums.RequiredService;
import com.wedu.recommendation.domain.enums.ScheduleRange;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "psychological_test_results")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PsychologicalTestResult extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User Aggregate를 객체로 참조하지 않고 식별자로만 참조한다.
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "mood_type", nullable = false, length = 30)
    private MoodType moodType;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false, length = 30)
    private LocationType locationType;

    /**
     * 여행 장소를 선택하지 않은 경우 null을 허용한다.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "region", length = 30)
    private RegionType region;

    @Enumerated(EnumType.STRING)
    @Column(name = "preparation_type", nullable = false, length = 30)
    private PreparationType preparationType;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "psychological_test_required_services",
            joinColumns = @JoinColumn(name = "psychological_test_result_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "required_service", nullable = false, length = 30)
    private List<RequiredService> requiredServices = new ArrayList<>();

    /**
     * 리스트 순서가 곧 우선순위다.
     * 0번째가 1순위, 1번째가 2순위다.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "psychological_test_priorities",
            joinColumns = @JoinColumn(name = "psychological_test_result_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "priority_value", nullable = false, length = 30)
    @OrderColumn(name = "priority_order")
    private List<PriorityValue> priorityValues = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "budget_range", nullable = false, length = 40)
    private BudgetRange budgetRange;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "psychological_test_excluded_elements",
            joinColumns = @JoinColumn(name = "psychological_test_result_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "excluded_element", nullable = false, length = 40)
    private List<ExcludedElement> excludedElements = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_range", nullable = false, length = 30)
    private ScheduleRange scheduleRange;

    @Enumerated(EnumType.STRING)
    @Column(name = "partner_mbti", nullable = false, length = 10)
    private PartnerMbti partnerMbti;

    private PsychologicalTestResult(
            Long userId,
            MoodType moodType,
            LocationType locationType,
            RegionType region,
            PreparationType preparationType,
            List<RequiredService> requiredServices,
            List<PriorityValue> priorityValues,
            BudgetRange budgetRange,
            List<ExcludedElement> excludedElements,
            ScheduleRange scheduleRange,
            PartnerMbti partnerMbti) {

        this.userId = userId;
        this.moodType = moodType;
        this.locationType = locationType;
        this.region = region;
        this.preparationType = preparationType;
        this.requiredServices = new ArrayList<>(requiredServices);
        this.priorityValues = new ArrayList<>(priorityValues);
        this.budgetRange = budgetRange;
        this.excludedElements = new ArrayList<>(excludedElements);
        this.scheduleRange = scheduleRange;
        this.partnerMbti = partnerMbti;
    }

    public static PsychologicalTestResult create(
            Long userId,
            MoodType moodType,
            LocationType locationType,
            RegionType region,
            PreparationType preparationType,
            List<RequiredService> requiredServices,
            List<PriorityValue> priorityValues,
            BudgetRange budgetRange,
            List<ExcludedElement> excludedElements,
            ScheduleRange scheduleRange,
            PartnerMbti partnerMbti) {

        validate(
                userId,
                moodType,
                locationType,
                region,
                preparationType,
                requiredServices,
                priorityValues,
                budgetRange,
                excludedElements,
                scheduleRange,
                partnerMbti);

        return new PsychologicalTestResult(
                userId,
                moodType,
                locationType,
                region,
                preparationType,
                requiredServices,
                priorityValues,
                budgetRange,
                excludedElements,
                scheduleRange,
                partnerMbti);
    }

    private static void validate(
            Long userId,
            MoodType moodType,
            LocationType locationType,
            RegionType region,
            PreparationType preparationType,
            List<RequiredService> requiredServices,
            List<PriorityValue> priorityValues,
            BudgetRange budgetRange,
            List<ExcludedElement> excludedElements,
            ScheduleRange scheduleRange,
            PartnerMbti partnerMbti) {

        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "사용자 ID는 필수입니다.");
        }
        if (moodType == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "원하는 분위기는 필수입니다.");
        }
        if (locationType == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "원하는 장소는 필수입니다.");
        }
        if (locationType == LocationType.TRAVEL && region == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "여행 장소를 선택한 경우 지역 선택은 필수입니다.");
        }
        if (preparationType == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "준비 방식은 필수입니다.");
        }
        if (requiredServices == null || requiredServices.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "필요 서비스는 한 개 이상 선택해야 합니다.");
        }
        if (priorityValues == null || priorityValues.size() != 2) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "우선순위는 정확히 두 개를 선택해야 합니다.");
        }
        if (priorityValues.get(0) == priorityValues.get(1)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "동일한 우선순위를 중복 선택할 수 없습니다.");
        }
        if (budgetRange == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "예산 범위는 필수입니다.");
        }
        if (excludedElements == null || excludedElements.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "제외 요소는 한 개 이상 선택해야 합니다.");
        }
        if (scheduleRange == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "예정 시기는 필수입니다.");
        }
        if (partnerMbti == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "파트너 MBTI는 필수입니다.");
        }
    }
}