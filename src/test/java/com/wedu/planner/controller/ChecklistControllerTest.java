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
import com.wedu.planner.domain.ChecklistCategory;
import com.wedu.planner.dto.ChecklistCompletionRequest;
import com.wedu.planner.dto.ChecklistItemCreateRequest;
import com.wedu.planner.dto.ChecklistItemResponse;
import com.wedu.planner.dto.ChecklistItemUpdateRequest;
import com.wedu.planner.dto.ChecklistOverviewResponse;
import com.wedu.planner.service.ChecklistService;
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

@WebMvcTest(ChecklistController.class)
class ChecklistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChecklistService checklistService;

    private final UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                    1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

    @Test
    @DisplayName("체크리스트 항목을 생성한다")
    void create() throws Exception {
        when(checklistService.create(eq(1L), any(ChecklistItemCreateRequest.class)))
                .thenReturn(itemResponse());

        mockMvc.perform(post("/api/checklist-items")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.itemId").value(10))
                .andExpect(jsonPath("$.data.category").value("CEREMONY"))
                .andExpect(jsonPath("$.data.completed").value(false));
    }

    @Test
    @DisplayName("빈 제목과 누락된 카테고리를 거부한다")
    void rejectInvalidCreateRequest() throws Exception {
        mockMvc.perform(post("/api/checklist-items")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":" "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_400"));
    }

    @Test
    @DisplayName("카테고리별 목록과 전체 진행률을 조회한다")
    void getChecklist() throws Exception {
        when(checklistService.getChecklist(1L, ChecklistCategory.CEREMONY))
                .thenReturn(new ChecklistOverviewResponse(2, 1, 1, 50, List.of(itemResponse())));

        mockMvc.perform(get("/api/checklist-items")
                        .param("category", "CEREMONY")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.progressPercentage").value(50))
                .andExpect(jsonPath("$.data.items.length()").value(1));
    }

    @Test
    @DisplayName("항목 정보와 완료 상태를 수정한다")
    void updateAndComplete() throws Exception {
        when(checklistService.update(eq(1L), eq(10L), any(ChecklistItemUpdateRequest.class)))
                .thenReturn(itemResponse());
        when(checklistService.changeCompletion(eq(1L), eq(10L), any(ChecklistCompletionRequest.class)))
                .thenReturn(new ChecklistItemResponse(
                        10L, "예식장 계약", ChecklistCategory.CEREMONY, true));

        mockMvc.perform(put("/api/checklist-items/10")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemBody()))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/checklist-items/10/completion")
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
        doNothing().when(checklistService).delete(1L, 10L);

        mockMvc.perform(delete("/api/checklist-items/10")
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isOk());

        when(checklistService.update(eq(1L), eq(11L), any(ChecklistItemUpdateRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.PLANNER_CHECKLIST_ITEM_NOT_FOUND));
        mockMvc.perform(put("/api/checklist-items/11")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemBody()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("PLANNER_CHECKLIST_404"));
    }

    private ChecklistItemResponse itemResponse() {
        return new ChecklistItemResponse(
                10L, "예식장 계약", ChecklistCategory.CEREMONY, false);
    }

    private String itemBody() {
        return """
                {
                  "title":"예식장 계약",
                  "category":"CEREMONY"
                }
                """;
    }
}
