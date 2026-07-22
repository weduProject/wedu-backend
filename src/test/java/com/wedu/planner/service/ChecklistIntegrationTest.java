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

import com.wedu.planner.domain.ChecklistCategory;
import com.wedu.planner.domain.ChecklistItem;
import com.wedu.planner.repository.ChecklistItemRepository;
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
class ChecklistIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChecklistItemRepository checklistItemRepository;

    private final UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                    1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

    @BeforeEach
    void setUp() {
        checklistItemRepository.deleteAll();
    }

    @Test
    @DisplayName("초기 목록은 비어 있고 진행률은 0퍼센트다")
    void getEmptyChecklist() throws Exception {
        mockMvc.perform(get("/api/checklist-items")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(0))
                .andExpect(jsonPath("$.data.progressPercentage").value(0))
                .andExpect(jsonPath("$.data.items.length()").value(0));
    }

    @Test
    @DisplayName("HTTP 요청으로 생성·필터·완료·수정·삭제하고 전체 진행률을 계산한다")
    void manageChecklistThroughHttp() throws Exception {
        create("예식장 계약", "CEREMONY");
        create("스튜디오 예약", "SHOOTING");
        checklistItemRepository.save(ChecklistItem.create(
                2L, "다른 사용자 할 일", ChecklistCategory.CEREMONY));
        List<ChecklistItem> items = checklistItemRepository.findAllByUserIdOrderByIdAsc(1L);
        Long ceremonyItemId = items.getFirst().getId();

        mockMvc.perform(patch("/api/checklist-items/{itemId}/completion", ceremonyItemId)
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"completed":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completed").value(true));

        mockMvc.perform(get("/api/checklist-items")
                        .param("category", "CEREMONY")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andExpect(jsonPath("$.data.completedCount").value(1))
                .andExpect(jsonPath("$.data.progressPercentage").value(50))
                .andExpect(jsonPath("$.data.items.length()").value(1));

        mockMvc.perform(put("/api/checklist-items/{itemId}", ceremonyItemId)
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"예식장 계약 완료","category":"CEREMONY"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("예식장 계약 완료"))
                .andExpect(jsonPath("$.data.completed").value(true));

        mockMvc.perform(delete("/api/checklist-items/{itemId}", ceremonyItemId)
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertThat(checklistItemRepository.findById(ceremonyItemId)).isEmpty();
        mockMvc.perform(get("/api/checklist-items")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.completedCount").value(0))
                .andExpect(jsonPath("$.data.progressPercentage").value(0));
    }

    private void create(String title, String category) throws Exception {
        mockMvc.perform(post("/api/checklist-items")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"%s","category":"%s"}
                                """.formatted(title, category)))
                .andExpect(status().isOk());
    }
}
