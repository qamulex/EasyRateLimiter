/*
 * Created on Nov 05, 2026
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterThreadSafetyEnforcementWrapperTest {

    static final long delayMillis = 500;

    RateLimiter rateLimiter;

    RateLimiter rateLimiter() {
        return RateLimiterBuilder.newBuilder()
                .withDelay(delayMillis, TimeUnit.MILLISECONDS)
                .enforceThreadSafety(true)
                .build();
    }

    @Test
    void testNonBlockingMethodsRespondWhileAnotherThreadWaits() throws Exception {
        rateLimiter = rateLimiter();

        CountDownLatch insideBlockLatch = new CountDownLatch(1);
        Thread waiter = new Thread(() -> {
            insideBlockLatch.countDown();
            try {
                rateLimiter.blockUntilRequestAllowed();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        waiter.start();

        assertTrue(insideBlockLatch.await(1, TimeUnit.SECONDS));

        long startTime = System.nanoTime();
        rateLimiter.isRequestAllowed();
        rateLimiter.getTimeUntilNextRequest();
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

        waiter.join(2 * delayMillis + 1000);

        assertTrue(
                elapsedMillis < 100,
                () -> "non-blocking methods stalled for " + elapsedMillis + " ms behind a blocked caller"
        );
    }

    @Test
    void testTimeoutIsHonoredUnderContention() throws Exception {
        rateLimiter = rateLimiter();

        CountDownLatch insideBlockLatch = new CountDownLatch(1);
        Thread waiter = new Thread(() -> {
            insideBlockLatch.countDown();
            try {
                rateLimiter.blockUntilRequestAllowed();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        waiter.start();

        assertTrue(insideBlockLatch.await(1, TimeUnit.SECONDS));

        long timeoutMillis = 100;
        long startTime = System.nanoTime();
        boolean allowed = rateLimiter.blockUntilRequestAllowed(timeoutMillis, TimeUnit.MILLISECONDS);
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

        waiter.join(2 * delayMillis + 1000);

        assertFalse(allowed);
        assertTrue(
                elapsedMillis < 2 * timeoutMillis,
                () -> "timed wait took " + elapsedMillis + " ms, exceeding the requested timeout of " + timeoutMillis + " ms"
        );
    }

}
