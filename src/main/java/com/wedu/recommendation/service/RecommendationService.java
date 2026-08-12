package com.wedu.recommendation.service;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.product.domain.Product;
import com.wedu.product.repository.ProductRepository;
import com.wedu.recommendation.domain.PsychologicalTestResult;
import com.wedu.recommendation.domain.enums.BudgetRange;
import com.wedu.recommendation.repository.PsychologicalTestResultRepository;
import com.wedu.recommendation.dto.AiRecommendationRequest;
import com.wedu.recommendation.dto.AiRecommendationResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final ProductRepository productRepository;
    private final PsychologicalTestResultRepository psychologicalTestResultRepository;
    private final GeminiRecommendationClient geminiRecommendationClient;

    /**
     * 사용자의 심리테스트 결과를 기반으로
     * 예산 조건에 맞는 후보 상품을 추린 뒤 Gemini에게 최종 추천을 요청한다.
     */
    @Transactional(readOnly = true)
    public AiRecommendationResponse recommend(Long userId) {

        // 1. 사용자의 심리테스트 결과 조회
        PsychologicalTestResult testResult =
                psychologicalTestResultRepository.findByUserId(userId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.INVALID_INPUT,
                                        "심리테스트 결과를 찾을 수 없습니다."
                                ));

        // 2. 예산 범위에 맞는 후보 상품 조회
        List<Product> candidateProducts =
                findCandidateProducts(testResult.getBudgetRange());

        if (candidateProducts.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "추천 가능한 상품이 없습니다."
            );
        }

        // 3. 심리테스트 결과 + 후보 상품을 AI 요청 데이터로 변환
        AiRecommendationRequest aiRequest =
                AiRecommendationRequest.from(
                        testResult,
                        candidateProducts
                );

        // 4. Gemini가 후보 중 최종 추천 상품을 선택
        return geminiRecommendationClient.recommend(aiRequest);
    }

    /**
     * 예산 범위에 맞는 상품을 최대 20개까지 후보로 조회한다.
     */
    private List<Product> findCandidateProducts(BudgetRange budgetRange) {

        Integer minPrice = getMinPrice(budgetRange);
        Integer maxPrice = getMaxPrice(budgetRange);

        return productRepository.search(
                null,
                null,
                minPrice,
                maxPrice,
                PageRequest.of(0, 20)
        );
    }

    private Integer getMinPrice(BudgetRange budgetRange) {
        return switch (budgetRange) {
            case UNDER_200000 -> 0;
            case FROM_200000_TO_500000 -> 200000;
            case FROM_500000_TO_1000000 -> 500000;
            case FROM_1000000_TO_2000000 -> 1000000;
            case FROM_2000000_TO_3000000 -> 2000000;
            case OVER_3000000 -> 3000000;
            case UNDECIDED -> null;
        };
    }

    private Integer getMaxPrice(BudgetRange budgetRange) {
        return switch (budgetRange) {
            case UNDER_200000 -> 199999;
            case FROM_200000_TO_500000 -> 499999;
            case FROM_500000_TO_1000000 -> 999999;
            case FROM_1000000_TO_2000000 -> 1999999;
            case FROM_2000000_TO_3000000 -> 2999999;
            case OVER_3000000, UNDECIDED -> null;
        };
    }
}