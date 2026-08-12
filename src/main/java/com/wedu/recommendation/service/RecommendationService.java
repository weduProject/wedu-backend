package com.wedu.recommendation.service;

import com.wedu.recommendation.dto.AiRecommendationRequest;
import com.wedu.recommendation.dto.AiRecommendationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationQueryService recommendationQueryService;
    private final GeminiRecommendationClient geminiRecommendationClient;

    /**
     * DB에서 추천에 필요한 데이터를 조회한 뒤,
     * 트랜잭션이 종료된 상태에서 Gemini에게 최종 추천을 요청한다.
     */
    public AiRecommendationResponse recommend(Long userId) {

        // 1. DB 조회 및 AI 요청 데이터 생성
        AiRecommendationRequest aiRequest =
                recommendationQueryService.createAiRequest(userId);

        // 2. DB 트랜잭션 종료 후 Gemini 호출
        return geminiRecommendationClient.recommend(aiRequest);
    }
}