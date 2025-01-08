/*
 * Created on Jan 08, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import me.qamulex.easyratelimiter.util.ClockDependentRateLimiterTestBase;

class ReadmeExamplesTest extends ClockDependentRateLimiterTestBase {

    @Test
    void testFixedDelayRateLimiter() {
        RateLimiter rateLimiter = RateLimiterBuilder.newBuilder()
                .withClock(getClock())
                .withDelay(100, TimeUnit.MILLISECONDS)
                .build();

        assertTrue(rateLimiter.tryRequest());
        assertFalse(rateLimiter.isRequestAllowed());
        sleep(100);
        assertTrue(rateLimiter.isRequestAllowed());
    }

    @Test
    void testFixedWindowRateLimiter() {
        RateLimiter rateLimiter = RateLimiterBuilder.newBuilder()
                .withClock(getClock())
                .withWindowSize(1, TimeUnit.SECONDS)
                .withMaxQuota(5)
                .useFixedWindow()
                .build();

        for (int i = 0; i < 5; i++)
            assertTrue(rateLimiter.tryRequest());
        assertFalse(rateLimiter.tryRequest());
        sleep(1000);
        assertTrue(rateLimiter.tryRequest());
    }

    @Test
    void testSlidingWindowRateLimiter() {
        RateLimiter rateLimiter = RateLimiterBuilder.newBuilder()
                .withClock(getClock())
                .withWindowSize(500, TimeUnit.MILLISECONDS)
                .withMaxQuota(5)
                .useSlidingWindow()
                .build();

        for (int i = 0; i < 5; i++)
            assertTrue(rateLimiter.tryRequest());
        assertFalse(rateLimiter.tryRequest());
        sleep(500);
        assertTrue(rateLimiter.tryRequest());

        rateLimiter.reset();
        for (int i = 0; i < 10; i++) {
            assertTrue(rateLimiter.tryRequest());
            sleep(150);
        }
    }

    @Test
    void testFixedDelayWindowBasedRateLimiter() {
        RateLimiter rateLimiter = RateLimiterBuilder.newBuilder()
                .withClock(getClock())
                .withDelay(100, TimeUnit.MILLISECONDS)
                .withWindowSize(1, TimeUnit.SECONDS)
                .withMaxQuota(5)
                .build();

        for (int i = 0; i < 5; i++) {
            boolean request = rateLimiter.tryRequest();
            if (i == 0)
                assertTrue(request);
            else
                assertFalse(request);
        }
        rateLimiter.reset();
        for (int i = 0; i < 5; i++) {
            sleep(100);
            assertTrue(rateLimiter.tryRequest());
        }
        assertFalse(rateLimiter.isRequestAllowed());
        sleep(600);
        assertTrue(rateLimiter.isRequestAllowed());
    }

}
