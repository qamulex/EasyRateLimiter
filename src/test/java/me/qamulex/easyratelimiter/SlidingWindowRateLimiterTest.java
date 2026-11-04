/*
 * Created on Jan 08, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import me.qamulex.easyratelimiter.impl.SlidingWindowRateLimiter;
import me.qamulex.easyratelimiter.util.ClockDependentRateLimiterTestBase;

class SlidingWindowRateLimiterTest extends ClockDependentRateLimiterTestBase {

    @Test
    void testTryRequest() {
        SlidingWindowRateLimiter rateLimiter = initCDRL(() -> new SlidingWindowRateLimiter(5, 1000));
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.tryRequest());
            assertEquals(i + 1, rateLimiter.getUsedQuota());
            sleep(100);
        }
        assertFalse(rateLimiter.tryRequest());
        sleep(1000);
        assertTrue(rateLimiter.tryRequest());
        assertEquals(1, rateLimiter.getUsedQuota());
    }

    @Test
    void testGetTimeUntilNextRequest() {
        SlidingWindowRateLimiter rateLimiter = initCDRL(() -> new SlidingWindowRateLimiter(5, 1000));
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.tryRequest());
            sleep(100);
        }
        assertEquals(500, rateLimiter.getTimeUntilNextRequest());
        sleep(1000);
        assertEquals(0, rateLimiter.getTimeUntilNextRequest());
    }

    @Test
    void testReset() {
        SlidingWindowRateLimiter rateLimiter = initCDRL(() -> new SlidingWindowRateLimiter(5, 1000));
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.tryRequest());
            sleep(100);
        }
        assertFalse(rateLimiter.tryRequest());
        rateLimiter.reset();
        assertTrue(rateLimiter.tryRequest());
    }

    @Test
    void testWindowBoundaryIsExclusive() {
        SlidingWindowRateLimiter rateLimiter = initCDRL(() -> new SlidingWindowRateLimiter(5, 500));

        for (int i = 0; i < 5; i++)
            assertTrue(rateLimiter.tryRequest()); // burst of 5 at the same instant

        sleep(499);
        assertFalse(rateLimiter.tryRequest());
        assertEquals(5, rateLimiter.getUsedQuota());

        sleep(1); // exactly 500 ms after the oldest request: it has just expired
        assertTrue(rateLimiter.tryRequest());
        assertEquals(1, rateLimiter.getUsedQuota());
    }

    @Test
    void testAdmissionConsistentWithUsedQuota() {
        SlidingWindowRateLimiter rateLimiter = initCDRL(() -> new SlidingWindowRateLimiter(3, 1000));

        long startTime = getTime();
        for (int i = 0; i < 50; i++) {
            setTime(startTime + i * 137);

            boolean allowedByUsedQuota = rateLimiter.getUsedQuota() < 3;
            boolean admitted = rateLimiter.tryRequest();
            assertEquals(allowedByUsedQuota, admitted);
        }
    }

}
