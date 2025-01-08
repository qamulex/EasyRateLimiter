/*
 * Created on Jan 08, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import me.qamulex.easyratelimiter.wrapper.RateLimitingExecutorService;

class RateLimitingExecutorServiceTest {

    static final long delayMillis = 100;
    static final int  taskCount   = 10;

    RateLimitingExecutorService executorService;

    void assertExecutionTimeEquals(long expectedExecutionTime, double timeDelta, Executable executable) {
        long startTime = System.nanoTime();
        try {
            executable.execute();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        long endTime = System.nanoTime();

        long elapsedNanos = endTime - startTime;
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(elapsedNanos);

        assertEquals(expectedExecutionTime, elapsedMillis, timeDelta);
    }

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

        assertExecutionTimeEquals(delayMillis * (taskCount - 1), 100, () -> {
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

        assertExecutionTimeEquals(delayMillis * (taskCount - 1), 100, () -> {
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

        assertExecutionTimeEquals(delayMillis * (taskCount - 1), 100, () -> {
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

}
