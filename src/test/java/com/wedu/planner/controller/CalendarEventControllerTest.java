package com.wedu.planner.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.planner.domain.CalendarEventCategory;
import com.wedu.planner.dto.CalendarEventCreateRequest;
import com.wedu.planner.dto.CalendarEventResponse;
import com.wedu.planner.dto.CalendarEventUpdateRequest;
import com.wedu.planner.service.CalendarEventService;
import java.time.LocalDate;
import java.time.LocalTime;
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
    @DisplayName("Figma 폼의 일정 정보를 생성한다")
    void create() throws Exception {
        when(calendarEventService.create(eq(1L), any(CalendarEventCreateRequest.class)))
                .thenReturn(response());

        mockMvc.perform(post("/api/calendar-events")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventTime").value("14:00"))
                .andExpect(jsonPath("$.data.category").value("STUDIO_DRESS"))
                .andExpect(jsonPath("$.data.memo").value("피팅 준비"));
    }

    @Test
    @DisplayName("필수 입력과 시간·카테고리 형식을 검증한다")
    void rejectInvalidCreateRequest() throws Exception {
        mockMvc.perform(post("/api/calendar-events")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":" ","eventDate":"2026-07-12"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_400"));

        mockMvc.perform(post("/api/calendar-events")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"일정",
                                  "eventDate":"2026-07-12",
                                  "eventTime":"25:00",
                                  "category":"UNKNOWN"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("카테고리로 월간 일정을 조회한다")
    void getMonthlyEvents() throws Exception {
        when(calendarEventService.getMonthlyEvents(
                1L, 2026, 7, CalendarEventCategory.STUDIO_DRESS))
                .thenReturn(List.of(response()));

        mockMvc.perform(get("/api/calendar-events")
                        .param("year", "2026")
                        .param("month", "7")
                        .param("category", "STUDIO_DRESS")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("드레스 2차 피팅"));
    }

    @Test
    @DisplayName("다가오는 일정을 조회한다")
    void getUpcomingEvents() throws Exception {
        when(calendarEventService.getUpcomingEvents(1L, null, 10))
                .thenReturn(List.of(response()));

        mockMvc.perform(get("/api/calendar-events/upcoming")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("일정을 수정한다")
    void update() throws Exception {
        when(calendarEventService.update(eq(1L), eq(10L), any(CalendarEventUpdateRequest.class)))
                .thenReturn(response());

        mockMvc.perform(put("/api/calendar-events/10")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventId").value(10));
    }

    @Test
    @DisplayName("일정을 삭제한다")
    void deleteEvent() throws Exception {
        doNothing().when(calendarEventService).delete(1L, 10L);

        mockMvc.perform(delete("/api/calendar-events/10")
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("소유하지 않은 일정 수정은 404로 응답한다")
    void rejectUnownedUpdate() throws Exception {
        when(calendarEventService.update(eq(1L), eq(10L), any(CalendarEventUpdateRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.PLANNER_CALENDAR_EVENT_NOT_FOUND));

        mockMvc.perform(put("/api/calendar-events/10")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("PLANNER_CALENDAR_404"));
    }

    private CalendarEventResponse response() {
        return new CalendarEventResponse(
                10L,
                "드레스 2차 피팅",
                LocalDate.of(2026, 7, 12),
                LocalTime.of(14, 0),
                CalendarEventCategory.STUDIO_DRESS,
                "피팅 준비");
    }

    private String requestBody() {
        return """
                {
                  "title":"드레스 2차 피팅",
                  "eventDate":"2026-07-12",
                  "eventTime":"14:00",
                  "category":"STUDIO_DRESS",
                  "memo":"피팅 준비"
                }
                """;
    }
}
