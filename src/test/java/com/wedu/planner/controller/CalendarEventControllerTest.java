package com.wedu.planner.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.planner.dto.CalendarEventResponse;
import com.wedu.planner.service.CalendarEventService;
import java.time.LocalDate;
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

@WebMvcTest(CalendarEventController.class)
class CalendarEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CalendarEventService calendarEventService;

    private final UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                    1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

    @Test
    @DisplayName("캘린더 일정을 생성한다")
    void create() throws Exception {
        LocalDate eventDate = LocalDate.of(2026, 8, 3);
        when(calendarEventService.create(eq(1L), eq("상견례"), eq(eventDate)))
                .thenReturn(new CalendarEventResponse(10L, "상견례", eventDate));

        mockMvc.perform(post("/api/calendar-events")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"상견례","eventDate":"2026-08-03"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.eventId").value(10))
                .andExpect(jsonPath("$.data.title").value("상견례"))
                .andExpect(jsonPath("$.data.eventDate").value("2026-08-03"));
    }

    @Test
    @DisplayName("일정 제목이나 날짜가 없으면 400으로 응답한다")
    void rejectInvalidCreateRequest() throws Exception {
        mockMvc.perform(post("/api/calendar-events")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":" "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_400"));

        mockMvc.perform(post("/api/calendar-events")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"상견례","eventDate":"잘못된-날짜"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_400"));
    }

    @Test
    @DisplayName("지정한 연월의 캘린더 일정을 조회한다")
    void getMonthlyEvents() throws Exception {
        when(calendarEventService.getMonthlyEvents(1L, 2026, 8)).thenReturn(List.of(
                new CalendarEventResponse(10L, "상견례", LocalDate.of(2026, 8, 3)),
                new CalendarEventResponse(11L, "예식장 방문", LocalDate.of(2026, 8, 3))));

        mockMvc.perform(get("/api/calendar-events")
                        .param("year", "2026")
                        .param("month", "8")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("상견례"))
                .andExpect(jsonPath("$.data[1].title").value("예식장 방문"));
    }

    @Test
    @DisplayName("조회 연월이 누락되거나 유효하지 않으면 400으로 응답한다")
    void rejectInvalidYearMonth() throws Exception {
        when(calendarEventService.getMonthlyEvents(1L, 2026, 13))
                .thenThrow(new BusinessException(ErrorCode.INVALID_INPUT));
        mockMvc.perform(get("/api/calendar-events")
                        .param("year", "2026")
                        .param("month", "13")
                        .with(authentication(authentication)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_400"));

        mockMvc.perform(get("/api/calendar-events")
                        .with(authentication(authentication)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_400"));

        mockMvc.perform(get("/api/calendar-events")
                        .param("year", "2026")
                        .param("month", "abc")
                        .with(authentication(authentication)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_400"));
    }
}
