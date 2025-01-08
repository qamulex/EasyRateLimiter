/*
 * Created on Jan 08, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import me.qamulex.easyratelimiter.util.ExecutionTimeMeasurer;

class RateLimiterConcurrencyTest implements ExecutionTimeMeasurer {

    static final long delayMillis = 100;

    RateLimiter     rateLimiter;
    ExecutorService executorService;

    @BeforeEach
    void setUp() {
        rateLimiter = RateLimiterBuilder.newBuilder()
                .withDelay(delayMillis)
                .enforceThreadSafety(true)
                .build();

        executorService = Executors.newFixedThreadPool(32);
    }

    @AfterEach
    void tearDown() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
        if (rateLimiter != null) {
            rateLimiter.reset();
        }
    }

    @Test
    void testConcurrentTryRequest() throws InterruptedException {
        int numberOfThreads = 100;

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    if (rateLimiter.tryRequest()) {
                        successCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();

        assertTrue(doneLatch.await(5, TimeUnit.SECONDS));
        assertEquals(1, successCount.get());
    }

    @Test
    void testConcurrentBlockUntilRequestAllowed() {
        int numberOfThreads = 16;
        AtomicInteger successCount = new AtomicInteger(0);

        assertExecutionTimeEquals(delayMillis * (numberOfThreads - 1), 100, () -> {
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

            for (int i = 0; i < numberOfThreads; i++) {
                executorService.submit(() -> {
                    try {
                        startLatch.await();
                        rateLimiter.blockUntilRequestAllowed();
                        successCount.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();

            assertTrue(doneLatch.await(delayMillis * numberOfThreads + 1000, TimeUnit.MILLISECONDS));
        });

        assertEquals(numberOfThreads, successCount.get());
    }

}
