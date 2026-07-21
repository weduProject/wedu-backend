package com.wedu.planner.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wedu.planner.dto.DDayResponse;
import com.wedu.planner.service.DDayService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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

@WebMvcTest(DDayController.class)
class DDayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DDayService dDayService;

    private final UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                    1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

    @Test
    @DisplayName("결혼식 D-day를 생성한다")
    void create() throws Exception {
        LocalDate weddingDate = LocalDate.of(2026, 11, 14);
        when(dDayService.create(eq(1L), eq(weddingDate))).thenReturn(response(weddingDate));

        mockMvc.perform(post("/api/ddays")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"weddingDate":"2026-11-14"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.weddingDate").value("2026-11-14"))
                .andExpect(jsonPath("$.data.targetAt").value("2026-11-13T15:00:00Z"))
                .andExpect(jsonPath("$.data.daysRemaining").value(116));
    }

    @Test
    @DisplayName("결혼식 날짜가 없으면 생성 요청이 실패한다")
    void createWithoutWeddingDate() throws Exception {
        mockMvc.perform(post("/api/ddays")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_400"));
    }

    @Test
    @DisplayName("내 결혼식 D-day를 조회한다")
    void getMyDDay() throws Exception {
        LocalDate weddingDate = LocalDate.of(2026, 11, 14);
        when(dDayService.getMyDDay(1L)).thenReturn(response(weddingDate));

        mockMvc.perform(get("/api/ddays/me")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.weddingDate").value("2026-11-14"));
    }

    @Test
    @DisplayName("내 결혼식 날짜를 수정한다")
    void update() throws Exception {
        LocalDate weddingDate = LocalDate.of(2026, 12, 25);
        when(dDayService.update(eq(1L), eq(weddingDate))).thenReturn(response(weddingDate));

        mockMvc.perform(patch("/api/ddays/me")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"weddingDate":"2026-12-25"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.weddingDate").value("2026-12-25"));
    }

    @Test
    @DisplayName("내 결혼식 D-day를 삭제한다")
    void deleteMyDDay() throws Exception {
        mockMvc.perform(delete("/api/ddays/me")
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private DDayResponse response(LocalDate weddingDate) {
        return new DDayResponse(
                1L,
                weddingDate,
                weddingDate.atStartOfDay(java.time.ZoneId.of("Asia/Seoul")).toInstant(),
                ChronoUnit.DAYS.between(LocalDate.of(2026, 7, 21), weddingDate));
    }
}
