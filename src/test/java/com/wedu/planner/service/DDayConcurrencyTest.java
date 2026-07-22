package com.wedu.planner.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;

import com.wedu.global.error.BusinessException;
import com.wedu.planner.repository.DDayRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

@SpringBootTest
class DDayConcurrencyTest {

    @Autowired
    private DDayService dDayService;

    @SpyBean
    private DDayRepository dDayRepository;

    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        dDayRepository.deleteAll();
        executor = Executors.newFixedThreadPool(2);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        executor.shutdownNow();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("동시에 같은 사용자의 D-day를 생성하면 하나만 저장되고 하나는 중복 오류가 된다")
    void createConcurrently() throws Exception {
        CyclicBarrier barrier = new CyclicBarrier(2);
        doAnswer(invocation -> {
            barrier.await(5, TimeUnit.SECONDS);
            return false;
        }).when(dDayRepository).existsByUserId(eq(1L));

        Callable<String> create = () -> {
            try {
                dDayService.create(1L, LocalDate.now().plusYears(1));
                return "SUCCESS";
            } catch (BusinessException e) {
                return e.getErrorCode().name();
            }
        };

        Future<String> first = executor.submit(create);
        Future<String> second = executor.submit(create);
        List<String> results = List.of(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS));

        assertThat(results).containsExactlyInAnyOrder(
                "SUCCESS", "PLANNER_DDAY_ALREADY_EXISTS");
        assertThat(dDayRepository.count()).isEqualTo(1);
    }
}
