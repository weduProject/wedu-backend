package com.wedu.planner.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wedu.planner.domain.CalendarEventCategory;
import com.wedu.planner.dto.CalendarEventCreateRequest;
import com.wedu.planner.dto.CalendarEventResponse;
import com.wedu.planner.dto.CalendarEventUpdateRequest;
import com.wedu.planner.repository.CalendarEventRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(CalendarEventIntegrationTest.FixedClockConfig.class)
class CalendarEventIntegrationTest {

    private static final Instant NOW = Instant.parse("2026-07-21T16:30:00Z");
    private static final LocalDate TODAY = NOW.atZone(ZoneId.of("Asia/Seoul")).toLocalDate();

    @Autowired
    private CalendarEventService calendarEventService;

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @Autowired
    private MockMvc mockMvc;

    private final UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                    1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

    @BeforeEach
    void setUp() {
        calendarEventRepository.deleteAll();
    }

    @Test
    @DisplayName("월별 조회는 사용자와 카테고리를 제한하고 날짜와 시각순으로 정렬한다")
    void getMonthlyEvents() {
        create(1L, "이전 달", LocalDate.of(2026, 6, 30), null, CalendarEventCategory.OTHER);
        create(1L, "시간 없음", LocalDate.of(2026, 7, 12), null, CalendarEventCategory.STUDIO_DRESS);
        create(1L, "오전 일정", LocalDate.of(2026, 7, 12),
                OffsetDateTime.parse("2026-07-12T11:00:00Z"), CalendarEventCategory.STUDIO_DRESS);
        create(1L, "오후 일정", LocalDate.of(2026, 7, 12),
                OffsetDateTime.parse("2026-07-12T14:00:00Z"), CalendarEventCategory.STUDIO_DRESS);
        create(1L, "다른 카테고리", LocalDate.of(2026, 7, 20), null, CalendarEventCategory.HONEYMOON);
        create(2L, "다른 사용자", LocalDate.of(2026, 7, 12), null, CalendarEventCategory.STUDIO_DRESS);

        List<CalendarEventResponse> result = calendarEventService.getMonthlyEvents(
                1L, 2026, 7, CalendarEventCategory.STUDIO_DRESS);

        assertThat(result).extracting(CalendarEventResponse::title)
                .containsExactly("시간 없음", "오전 일정", "오후 일정");
    }

    @Test
    @DisplayName("다가오는 일정은 한국의 오늘 종일 일정과 현재 이후 일정만 조회한다")
    void getUpcomingEventsExcludesPastTimedEvents() {
        create(1L, "한국 기준 어제 종일", TODAY.minusDays(1), null, CalendarEventCategory.OTHER);
        create(1L, "오늘 종일", TODAY, null, CalendarEventCategory.OTHER);
        create(1L, "이미 지난 일정", TODAY,
                OffsetDateTime.parse("2026-07-22T00:30:00+09:00"), CalendarEventCategory.OTHER);
        create(1L, "현재 시각 일정", TODAY,
                OffsetDateTime.parse("2026-07-22T01:30:00+09:00"), CalendarEventCategory.OTHER);
        create(1L, "오늘 이후 일정", TODAY,
                OffsetDateTime.parse("2026-07-22T02:30:00+09:00"), CalendarEventCategory.OTHER);
        create(1L, "내일 일정", TODAY.plusDays(1), null, CalendarEventCategory.OTHER);
        create(2L, "다른 사용자 일정", TODAY, null, CalendarEventCategory.OTHER);

        List<CalendarEventResponse> result = calendarEventService.getUpcomingEvents(1L, null, 10);

        assertThat(result).extracting(CalendarEventResponse::title)
                .containsExactly("오늘 종일", "현재 시각 일정", "오늘 이후 일정", "내일 일정");
    }

    @Test
    @DisplayName("일정 수정과 삭제 결과를 DB에 반영한다")
    void updateAndDelete() {
        CalendarEventResponse created = create(
                1L,
                "허니문 출발",
                TODAY.plusDays(1),
                OffsetDateTime.parse("2026-07-23T09:00:00+09:00"),
                CalendarEventCategory.HONEYMOON);

        CalendarEventResponse updated = calendarEventService.update(
                1L,
                created.eventId(),
                new CalendarEventUpdateRequest(
                        "허니문 출발 변경",
                        TODAY.plusDays(2),
                        null,
                        CalendarEventCategory.HONEYMOON,
                        null));
        assertThat(updated.title()).isEqualTo("허니문 출발 변경");
        assertThat(updated.eventAt()).isNull();

        calendarEventService.delete(1L, created.eventId());
        assertThat(calendarEventRepository.findById(created.eventId())).isEmpty();
    }

    @Test
    @DisplayName("HTTP 요청이 컨트롤러와 서비스를 거쳐 일정을 DB에 저장한다")
    void createThroughHttp() throws Exception {
        mockMvc.perform(post("/api/calendar-events")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"드레스 2차 피팅",
                                  "eventDate":"2026-07-22",
                                  "eventAt":"2026-07-22T08:00:00+09:00",
                                  "category":"STUDIO_DRESS",
                                  "memo":"피팅 준비"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventAt").value("2026-07-21T23:00:00Z"))
                .andExpect(jsonPath("$.data.title").value("드레스 2차 피팅"));

        assertThat(calendarEventRepository.count()).isEqualTo(1);
        assertThat(calendarEventRepository.findAll().getFirst().getEventAt()).isEqualTo(
                Instant.parse("2026-07-21T23:00:00Z"));
    }

    private CalendarEventResponse create(
            Long userId,
            String title,
            LocalDate date,
            OffsetDateTime eventAt,
            CalendarEventCategory category) {
        return calendarEventService.create(
                userId,
                new CalendarEventCreateRequest(title, date, eventAt, category, null));
    }

    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(NOW, ZoneOffset.UTC);
        }
    }
}
