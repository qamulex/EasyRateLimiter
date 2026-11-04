/*
 * Created on Jan 08, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import me.qamulex.easyratelimiter.util.ExecutionTimeMeasurer;
import me.qamulex.easyratelimiter.wrapper.RateLimitingExecutorService;

class RateLimitingExecutorServiceTest implements ExecutionTimeMeasurer {

    static final long delayMillis = 100;
    static final int  taskCount   = 10;

    RateLimitingExecutorService executorService;

    @BeforeEach
    void setUp() {
        executorService = RateLimiterBuilder.newBuilder()
                .withDelay(delayMillis)
                .buildExecutorService();
    }

    @AfterEach
    void tearDown() {
        if (executorService != null)
            executorService.shutdownNow();
    }

    @Test
    void testRateLimiting() {
        AtomicInteger counter = new AtomicInteger(0);

        long expectedWaitingTime = delayMillis * (taskCount - 1);
        assertExecutionTimeInRange(expectedWaitingTime - 5, expectedWaitingTime + 100, () -> {
            CountDownLatch latch = new CountDownLatch(taskCount);

            for (int i = 0; i < taskCount; i++) {
                executorService.submit(() -> {
                    counter.incrementAndGet();
                    latch.countDown();
                });
            }

            assertTrue(latch.await(delayMillis * taskCount + 1000, TimeUnit.MILLISECONDS));
        });

        assertEquals(taskCount, counter.get());
    }

    @Test
    void testConcurrentSubmissions() {
        AtomicInteger counter = new AtomicInteger(0);

        long expectedWaitingTime = delayMillis * (taskCount - 1);
        assertExecutionTimeInRange(expectedWaitingTime - 5, expectedWaitingTime + 100, () -> {
            CountDownLatch latch = new CountDownLatch(taskCount);

            for (int i = 0; i < taskCount; i++) {
                new Thread(() -> {
                    executorService.submit(() -> {
                        counter.incrementAndGet();
                        latch.countDown();
                    });
                }).start();
            }

            assertTrue(latch.await(delayMillis * taskCount + 1000, TimeUnit.MILLISECONDS));
        });

        assertEquals(taskCount, counter.get());
    }

    @Test
    void testTaskExecutionOrder() {
        StringBuilder executionOrder = new StringBuilder();

        long expectedWaitingTime = delayMillis * (taskCount - 1);
        assertExecutionTimeInRange(expectedWaitingTime - 5, expectedWaitingTime + 100, () -> {
            CountDownLatch latch = new CountDownLatch(taskCount);

            for (int i = 0; i < taskCount; i++) {
                int taskNumber = i + 1;
                executorService.submit(() -> {
                    executionOrder.append(taskNumber);
                    latch.countDown();
                });
            }

            assertTrue(latch.await(delayMillis * taskCount + 1000, TimeUnit.MILLISECONDS));
        });

        assertEquals(
                IntStream.range(1, taskCount + 1)
                        .mapToObj(Integer::toString)
                        .collect(Collectors.joining()),
                executionOrder.toString()
        );
    }

    @Test
    void testWindowBasedRateLimiting() throws InterruptedException {
        executorService = RateLimiterBuilder.newBuilder()
                .withWindowSize(200, TimeUnit.MILLISECONDS)
                .withMaxQuota(3)
                .useFixedWindow()
                .buildExecutorService();

        int taskCount = 8; // bursts of 3, 3, 2 within consecutive 200 ms windows
        CountDownLatch latch = new CountDownLatch(taskCount);
        List<Long> startTimes = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < taskCount; i++)
            executorService.submit(() -> {
                startTimes.add(System.currentTimeMillis());
                latch.countDown();
            });

        assertTrue(latch.await(3 * 200 + 1000, TimeUnit.MILLISECONDS));

        synchronized (startTimes) {
            // tasks within each burst start almost simultaneously
            assertTrue(startTimes.get(2) - startTimes.get(0) < 100);
            assertTrue(startTimes.get(5) - startTimes.get(3) < 100);
            assertTrue(startTimes.get(7) - startTimes.get(6) < 100);

            // bursts are separated by window boundaries
            assertTrue(startTimes.get(3) - startTimes.get(0) >= 150);
            assertTrue(startTimes.get(6) - startTimes.get(0) >= 350);
        }
    }

    @Test
    void testTaskExceptionDoesNotAffectOtherTasks() throws InterruptedException {
        int taskCount = 2;
        CountDownLatch latch = new CountDownLatch(taskCount);

        executorService.submit(() -> {
            throw new RuntimeException("boom");
        });
        for (int i = 0; i < taskCount; i++)
            executorService.submit(latch::countDown);

        assertTrue(latch.await(delayMillis * (taskCount + 1) + 1000, TimeUnit.MILLISECONDS));
    }

}
