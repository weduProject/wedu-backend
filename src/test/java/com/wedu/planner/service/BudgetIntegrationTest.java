package com.wedu.planner.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wedu.planner.domain.BudgetCategory;
import com.wedu.planner.domain.BudgetItem;
import com.wedu.planner.repository.BudgetItemRepository;
import java.math.BigDecimal;
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
class BudgetIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BudgetItemRepository budgetItemRepository;

    private final UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                    1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

    @BeforeEach
    void setUp() {
        budgetItemRepository.deleteAll();
    }

    @Test
    @DisplayName("초기 예산 현황은 0원이며 5개 카테고리를 모두 반환한다")
    void getEmptyOverview() throws Exception {
        mockMvc.perform(get("/api/budget-items")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.plannedAmount").value(0))
                .andExpect(jsonPath("$.data.summary.spentAmount").value(0))
                .andExpect(jsonPath("$.data.summary.balance").value(0))
                .andExpect(jsonPath("$.data.categories.length()").value(5));
    }

    @Test
    @DisplayName("HTTP 요청으로 예산 항목 CRUD와 완료 상태 및 집계를 관리한다")
    void manageBudgetThroughHttp() throws Exception {
        create("예식장 계약금", "VENUE", 2000000, 2000000);
        create("드레스 대여", "STUDIO_DRESS", 3000000, 500000);
        budgetItemRepository.save(BudgetItem.create(
                2L,
                "다른 사용자 항목",
                BudgetCategory.OTHER,
                new BigDecimal("9999999"),
                new BigDecimal("9999999")));
        List<BudgetItem> items = budgetItemRepository.findAllByUserIdOrderByIdAsc(1L);
        Long venueItemId = items.getFirst().getId();

        mockMvc.perform(patch("/api/budget-items/{itemId}/completion", venueItemId)
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"completed":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completed").value(true));

        mockMvc.perform(get("/api/budget-items")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.plannedAmount").value(5000000))
                .andExpect(jsonPath("$.data.summary.spentAmount").value(2500000))
                .andExpect(jsonPath("$.data.summary.balance").value(2500000))
                .andExpect(jsonPath("$.data.summary.completedCount").value(1))
                .andExpect(jsonPath("$.data.summary.totalCount").value(2))
                .andExpect(jsonPath("$.data.summary.executionRatePercentage").value(50))
                .andExpect(jsonPath("$.data.categories[0].category").value("VENUE"))
                .andExpect(jsonPath("$.data.categories[0].items.length()").value(1));

        mockMvc.perform(put("/api/budget-items/{itemId}", venueItemId)
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"예식장 잔금",
                                  "category":"VENUE",
                                  "plannedAmount":2000000,
                                  "spentAmount":2500000
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.spentAmount").value(2500000))
                .andExpect(jsonPath("$.data.completed").value(true));

        mockMvc.perform(delete("/api/budget-items/{itemId}", venueItemId)
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertThat(budgetItemRepository.findById(venueItemId)).isEmpty();
    }

    private void create(
            String title,
            String category,
            long plannedAmount,
            long spentAmount) throws Exception {
        mockMvc.perform(post("/api/budget-items")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"%s",
                                  "category":"%s",
                                  "plannedAmount":%d,
                                  "spentAmount":%d
                                }
                                """.formatted(title, category, plannedAmount, spentAmount)))
                .andExpect(status().isOk());
    }
}
