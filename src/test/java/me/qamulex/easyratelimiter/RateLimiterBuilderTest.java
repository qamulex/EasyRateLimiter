/*
 * Created on Jan 08, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import me.qamulex.easyratelimiter.impl.FixedDelayRateLimiter;
import me.qamulex.easyratelimiter.impl.FixedDelayWindowBasedRateLimiter;
import me.qamulex.easyratelimiter.impl.FixedWindowRateLimiter;
import me.qamulex.easyratelimiter.impl.SlidingWindowRateLimiter;
import me.qamulex.easyratelimiter.wrapper.RateLimiterThreadSafetyEnforcementWrapper;

class RateLimiterBuilderTest {

    @Test
    void testBuildWithoutConfiguration() {
        assertThrows(IllegalStateException.class, RateLimiterBuilder.newBuilder()::build);
    }

    @Test
    void testBuildFixedDelayRateLimiter() {
        RateLimiter rateLimiter;

        rateLimiter = RateLimiterBuilder.newBuilder()
                .withDelay(100)
                .build();
        assertTrue(rateLimiter instanceof FixedDelayRateLimiter);
        assertEquals(100, ((FixedDelayRateLimiter) rateLimiter).getDelayMillis());

        rateLimiter = RateLimiterBuilder.newBuilder()
                .withWindowSize(150)
                .build();
        assertTrue(rateLimiter instanceof FixedDelayRateLimiter);
        assertEquals(150, ((FixedDelayRateLimiter) rateLimiter).getDelayMillis());
    }

    @Test
    void testBuildFixedWindowRateLimiter() {
        RateLimiter rateLimiter = RateLimiterBuilder.newBuilder()
                .withWindowSize(1000)
                .withMaxQuota(5)
                .useFixedWindow()
                .build();
        assertTrue(rateLimiter instanceof FixedWindowRateLimiter);
        assertEquals(1000, ((FixedWindowRateLimiter) rateLimiter).getWindowSizeMillis());
        assertEquals(5, ((FixedWindowRateLimiter) rateLimiter).getMaxQuota());
    }

    @Test
    void testBuildSlidingWindowRateLimiter() {
        RateLimiter rateLimiter = RateLimiterBuilder.newBuilder()
                .withWindowSize(1000)
                .withMaxQuota(5)
                .useSlidingWindow()
                .build();
        assertTrue(rateLimiter instanceof SlidingWindowRateLimiter);
        assertEquals(1000, ((SlidingWindowRateLimiter) rateLimiter).getWindowSizeMillis());
        assertEquals(5, ((SlidingWindowRateLimiter) rateLimiter).getMaxQuota());
    }

    @Test
    void testBuildFixedDelayWindowBasedRateLimiter() {
        RateLimiter rateLimiter = RateLimiterBuilder.newBuilder()
                .withDelay(100)
                .withWindowSize(1000)
                .withMaxQuota(5)
                .useSlidingWindow()
                .build();
        assertTrue(rateLimiter instanceof FixedDelayWindowBasedRateLimiter);
        FixedDelayWindowBasedRateLimiter fdwbRateLimiter = (FixedDelayWindowBasedRateLimiter) rateLimiter;
        assertEquals(100, fdwbRateLimiter.getFixedDelayRateLimiter().getDelayMillis());
        assertEquals(1000, ((SlidingWindowRateLimiter) fdwbRateLimiter.getWindowBasedRateLimiter()).getWindowSizeMillis());
        assertEquals(5, ((SlidingWindowRateLimiter) fdwbRateLimiter.getWindowBasedRateLimiter()).getMaxQuota());
    }

    @Test
    void testBuildThreadSafeRateLimiter() {
        RateLimiter rateLimiter = RateLimiterBuilder.newBuilder()
                .withDelay(100)
                .enforceThreadSafety(true)
                .build();
        assertTrue(rateLimiter instanceof RateLimiterThreadSafetyEnforcementWrapper);
        assertTrue(((RateLimiterThreadSafetyEnforcementWrapper) rateLimiter).getWrappedRateLimiter() instanceof FixedDelayRateLimiter);
    }

}
