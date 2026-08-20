package com.wedu.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wedu.recommendation.domain.PsychologicalTestResult;
import com.wedu.recommendation.domain.enums.MoodType;
import com.wedu.recommendation.repository.PsychologicalTestResultRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PsychologicalTestIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PsychologicalTestResultRepository psychologicalTestResultRepository;

    private final UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                    1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

    @BeforeEach
    void setUp() {
        psychologicalTestResultRepository.deleteAll();
    }

    @Test
    @DisplayName("이미 제출한 사용자가 다시 제출하면 기존 결과를 덮어쓴다")
    void resubmitReplacesExistingResult() throws Exception {
        submit("""
                {
                  "moodType": "LUXURY_EVENT",
                  "locationType": "RESTAURANT",
                  "region": "UNDECIDED",
                  "preparationType": "VENUE_AND_DIY",
                  "requiredServices": ["VIDEO"],
                  "priorityValues": [
                    {"value": "MEMORY", "rank": 1},
                    {"value": "CONVENIENCE", "rank": 2}
                  ],
                  "budgetRange": "FROM_1000000_TO_2000000",
                  "excludedElements": ["PUBLIC_EVENT", "FAMILY_FRIEND_PARTICIPATION"],
                  "scheduleRange": "WITHIN_3_MONTHS",
                  "partnerMbti": "ENFP"
                }
                """);

        mockMvc.perform(post("/api/psychological-tests")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "moodType": "COZY_SINCERE",
                                  "locationType": "HOTEL",
                                  "region": "SEOUL",
                                  "preparationType": "FULL_SERVICE",
                                  "requiredServices": ["VENUE", "FLOWER"],
                                  "priorityValues": [
                                    {"value": "EMOTION", "rank": 1},
                                    {"value": "COST_EFFECTIVENESS", "rank": 2}
                                  ],
                                  "budgetRange": "UNDER_200000",
                                  "excludedElements": ["HIGH_COST"],
                                  "scheduleRange": "WITHIN_1_MONTH",
                                  "partnerMbti": "ISTJ"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        List<PsychologicalTestResult> results = psychologicalTestResultRepository.findAll();
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getMoodType()).isEqualTo(MoodType.COZY_SINCERE);
        assertThat(results.getFirst().getUserId()).isEqualTo(1L);
    }

    private void submit(String body) throws Exception {
        mockMvc.perform(post("/api/psychological-tests")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }
}
