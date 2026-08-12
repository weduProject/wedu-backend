package com.wedu.recommendation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.recommendation.dto.AiRecommendationRequest;
import com.wedu.recommendation.dto.AiRecommendationResponse;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class GeminiRecommendationClient {

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent";

    private final ObjectMapper objectMapper;
    private final RestClient geminiRestClient;

    @Value("${gemini.api-key}")
    private String apiKey;

    public AiRecommendationResponse recommend(AiRecommendationRequest request) {

        String prompt = createPrompt(request);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "response_mime_type", "application/json"
                )
        );

        String responseBody;

        try {
            responseBody = geminiRestClient
                    .post()
                    .uri(GEMINI_URL)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            (requestSpec, response) -> {
                                throw new BusinessException(
                                        ErrorCode.RECOMMENDATION_AI_REQUEST_FAILED
                                );
                            }
                    )
                    .body(String.class);

        } catch (RestClientException e) {
            throw new BusinessException(
                    ErrorCode.RECOMMENDATION_AI_REQUEST_FAILED
            );
        }

        if (responseBody == null || responseBody.isBlank()) {
            throw new BusinessException(
                    ErrorCode.RECOMMENDATION_AI_INVALID_RESPONSE
            );
        }

        AiRecommendationResponse response = parseResponse(responseBody);

        List<Long> candidateIds = request.candidateProducts().stream()
                .map(AiRecommendationRequest.CandidateProduct::id)
                .toList();

        List<AiRecommendationResponse.RecommendedProduct> filteredRecommendations =
                response.recommendations().stream()
                        .filter(recommendation ->
                                candidateIds.contains(recommendation.productId()))
                        .toList();

        return new AiRecommendationResponse(filteredRecommendations);
    }

    private String createPrompt(AiRecommendationRequest request) {
        try {
            String requestJson =
                    objectMapper.writeValueAsString(request);

            return """
                    당신은 프로포즈 상품 추천 AI입니다.

                    아래에는 사용자의 심리테스트 결과와
                    백엔드에서 예산 조건으로 필터링한 후보 상품 목록이 있습니다.

                    사용자의 분위기 취향, 장소 선호, 준비 방식,
                    필요한 서비스, 우선순위, 제외 요소,
                    예정 시기, 파트너 MBTI를 종합적으로 고려하세요.

                    반드시 candidateProducts 안에 존재하는 상품 중에서만
                    가장 적합한 상품 최대 3개를 선택하세요.

                    productId는 후보 상품의 실제 id를 그대로 사용해야 합니다.
                    존재하지 않는 상품을 새로 만들면 안 됩니다.

                    각 상품마다 짧고 구체적인 추천 이유를 작성하세요.

                    반드시 아래 JSON 형식으로만 응답하세요.

                    {
                      "recommendations": [
                        {
                          "productId": 1,
                          "reason": "추천 이유"
                        }
                      ]
                    }

                    입력 데이터:
                    %s
                    """.formatted(requestJson);

        } catch (JsonProcessingException e) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "AI 추천 요청 데이터를 생성할 수 없습니다."
            );
        }
    }

    private AiRecommendationResponse parseResponse(String responseBody) {

        try {
            JsonNode root = objectMapper.readTree(responseBody);

            String generatedText = root
                    .path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text")
                    .asText();

            if (generatedText.isBlank()) {
                throw new BusinessException(
                        ErrorCode.RECOMMENDATION_AI_INVALID_RESPONSE
                );
            }

            return objectMapper.readValue(
                    generatedText,
                    AiRecommendationResponse.class
            );

        } catch (JsonProcessingException e) {
            throw new BusinessException(
                    ErrorCode.RECOMMENDATION_AI_INVALID_RESPONSE
            );
        }
    }
}