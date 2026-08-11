package com.wedu.recommendation.presentation.dto;

import com.wedu.product.domain.Product;
import com.wedu.recommendation.domain.PsychologicalTestResult;
import java.util.List;

public record AiRecommendationRequest(
        UserPreference userPreference,
        List<CandidateProduct> candidateProducts
) {

    public static AiRecommendationRequest from(
            PsychologicalTestResult result,
            List<Product> products) {

        return new AiRecommendationRequest(
                UserPreference.from(result),
                products.stream()
                        .map(CandidateProduct::from)
                        .toList()
        );
    }

    public record UserPreference(
            String moodType,
            String locationType,
            String region,
            String preparationType,
            List<String> requiredServices,
            List<String> priorityValues,
            String budgetRange,
            List<String> excludedElements,
            String scheduleRange,
            String partnerMbti
    ) {

        public static UserPreference from(PsychologicalTestResult result) {
            return new UserPreference(
                    result.getMoodType().name(),
                    result.getLocationType().name(),
                    result.getRegion() != null
                            ? result.getRegion().name()
                            : null,
                    result.getPreparationType().name(),
                    result.getRequiredServices().stream()
                            .map(Enum::name)
                            .toList(),
                    result.getPriorityValues().stream()
                            .map(Enum::name)
                            .toList(),
                    result.getBudgetRange().name(),
                    result.getExcludedElements().stream()
                            .map(Enum::name)
                            .toList(),
                    result.getScheduleRange().name(),
                    result.getPartnerMbti().name()
            );
        }
    }

    public record CandidateProduct(
            Long id,
            String name,
            String category,
            int price,
            String vendorName,
            String description
    ) {

        public static CandidateProduct from(Product product) {
            return new CandidateProduct(
                    product.getId(),
                    product.getName(),
                    product.getCategory().name(),
                    product.getPrice(),
                    product.getVendorName(),
                    product.getDescription()
            );
        }
    }
}