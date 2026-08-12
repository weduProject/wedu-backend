package com.wedu.planner.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;

import com.wedu.planner.dto.BudgetTargetRequest;
import com.wedu.planner.dto.BudgetTargetResponse;
import com.wedu.planner.repository.BudgetRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

@SpringBootTest
class BudgetConcurrencyTest {

    @Autowired
    private BudgetService budgetService;

    @SpyBean
    private BudgetRepository budgetRepository;

    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        budgetRepository.deleteAll();
        executor = Executors.newFixedThreadPool(2);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        executor.shutdownNow();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("목표 예산 최초 설정이 동시에 요청돼도 모두 성공하고 하나의 행만 남는다")
    void setTargetConcurrently() throws Exception {
        CyclicBarrier firstLookupBarrier = new CyclicBarrier(2);
        AtomicInteger lookupCount = new AtomicInteger();
        doAnswer(invocation -> {
            if (lookupCount.incrementAndGet() <= 2) {
                firstLookupBarrier.await(5, TimeUnit.SECONDS);
                return Optional.empty();
            }
            return budgetRepository.findAll().stream()
                    .filter(budget -> budget.getUserId().equals(1L))
                    .findFirst();
        }).when(budgetRepository).findByUserId(eq(1L));

        Callable<BudgetTargetResponse> first = () -> budgetService.setTarget(
                1L, new BudgetTargetRequest(new BigDecimal("30000000")));
        Callable<BudgetTargetResponse> second = () -> budgetService.setTarget(
                1L, new BudgetTargetRequest(new BigDecimal("35000000")));

        Future<BudgetTargetResponse> firstResult = executor.submit(first);
        Future<BudgetTargetResponse> secondResult = executor.submit(second);

        assertThat(List.of(
                        firstResult.get(10, TimeUnit.SECONDS).totalBudget(),
                        secondResult.get(10, TimeUnit.SECONDS).totalBudget()))
                .containsExactlyInAnyOrder(
                        new BigDecimal("30000000"),
                        new BigDecimal("35000000"));
        assertThat(budgetRepository.count()).isEqualTo(1);
    }
}
