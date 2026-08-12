package com.wedu.planner.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wedu.friend.service.FriendAccessService;
import com.wedu.planner.dto.BudgetTargetRequest;
import com.wedu.planner.dto.BudgetTargetResponse;
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

@WebMvcTest(BudgetTargetController.class)
class BudgetTargetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BudgetService budgetService;

    @MockBean
    private FriendAccessService friendAccessService;

    private final UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                    1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

    @Test
    @DisplayName("내 전체 목표 예산을 설정한다")
    void setMyTarget() throws Exception {
        when(budgetService.setTarget(eq(1L), any(BudgetTargetRequest.class)))
                .thenReturn(new BudgetTargetResponse(new BigDecimal("30000000")));

        mockMvc.perform(put("/api/budgets/me")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"totalBudget":30000000}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalBudget").value(30000000));
    }

    @Test
    @DisplayName("친구의 목표 예산은 편집 권한을 확인한 뒤 설정한다")
    void setFriendTarget() throws Exception {
        when(budgetService.setTarget(eq(2L), any(BudgetTargetRequest.class)))
                .thenReturn(new BudgetTargetResponse(new BigDecimal("40000000")));

        mockMvc.perform(put("/api/budgets/friends/2")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"totalBudget":40000000}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalBudget").value(40000000));

        verify(friendAccessService).assertEditable(1L, 2L);
    }

    @Test
    @DisplayName("잘못된 목표 예산을 거부한다")
    void rejectInvalidTarget() throws Exception {
        mockMvc.perform(put("/api/budgets/me")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"totalBudget":-1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_400"));
    }
}
