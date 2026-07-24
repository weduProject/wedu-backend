package com.wedu.planner.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.planner.domain.BudgetCategory;
import com.wedu.planner.dto.BudgetCompletionRequest;
import com.wedu.planner.dto.BudgetItemCreateRequest;
import com.wedu.planner.dto.BudgetItemResponse;
import com.wedu.planner.dto.BudgetItemUpdateRequest;
import com.wedu.planner.dto.BudgetOverviewResponse;
import com.wedu.planner.service.BudgetService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BudgetController.class)
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BudgetService budgetService;

    private final UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                    1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

    @Test
    @DisplayName("예산 항목을 생성하고 원 단위 금액을 응답한다")
    void create() throws Exception {
        when(budgetService.create(eq(1L), any(BudgetItemCreateRequest.class)))
                .thenReturn(itemResponse(false));

        mockMvc.perform(post("/api/budget-items")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.itemId").value(10))
                .andExpect(jsonPath("$.data.plannedAmount").value(2000000))
                .andExpect(jsonPath("$.data.spentAmount").value(0))
                .andExpect(jsonPath("$.data.completed").value(false));
    }

    @Test
    @DisplayName("음수 또는 소수 금액과 누락된 필수값을 거부한다")
    void rejectInvalidCreateRequest() throws Exception {
        mockMvc.perform(post("/api/budget-items")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":" ",
                                  "category":"OTHER",
                                  "plannedAmount":-1,
                                  "spentAmount":1.5
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_400"));
    }

    @Test
    @DisplayName("전체 및 5개 카테고리 예산 현황을 조회한다")
    void getOverview() throws Exception {
        when(budgetService.getOverview(1L))
                .thenReturn(BudgetOverviewResponse.from(List.of()));

        mockMvc.perform(get("/api/budget-items")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.plannedAmount").value(0))
                .andExpect(jsonPath("$.data.categories.length()").value(5));
    }

    @Test
    @DisplayName("항목 정보와 완료 상태를 수정한다")
    void updateAndComplete() throws Exception {
        when(budgetService.update(eq(1L), eq(10L), any(BudgetItemUpdateRequest.class)))
                .thenReturn(itemResponse(false));
        when(budgetService.changeCompletion(
                eq(1L), eq(10L), any(BudgetCompletionRequest.class)))
                .thenReturn(itemResponse(true));

        mockMvc.perform(put("/api/budget-items/10")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemBody()))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/budget-items/10/completion")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"completed":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completed").value(true));
    }

    @Test
    @DisplayName("항목을 삭제하고 소유하지 않은 항목은 404로 응답한다")
    void deleteAndRejectUnownedItem() throws Exception {
        doNothing().when(budgetService).delete(1L, 10L);

        mockMvc.perform(delete("/api/budget-items/10")
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isOk());

        when(budgetService.update(eq(1L), eq(11L), any(BudgetItemUpdateRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.PLANNER_BUDGET_ITEM_NOT_FOUND));
        mockMvc.perform(put("/api/budget-items/11")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemBody()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("PLANNER_BUDGET_404"));
    }

    private BudgetItemResponse itemResponse(boolean completed) {
        return new BudgetItemResponse(
                10L,
                "예식장 계약금",
                BudgetCategory.VENUE,
                new BigDecimal("2000000"),
                BigDecimal.ZERO,
                completed);
    }

    private String itemBody() {
        return """
                {
                  "title":"예식장 계약금",
                  "category":"VENUE",
                  "plannedAmount":2000000,
                  "spentAmount":0
                }
                """;
    }
}
