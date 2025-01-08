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

import me.qamulex.easyratelimiter.impl.FixedWindowRateLimiter;
import me.qamulex.easyratelimiter.util.ClockDependentRateLimiterTestBase;

class FixedWindowRateLimiterTest extends ClockDependentRateLimiterTestBase {

    @Test
    void testTryRequest() {
        FixedWindowRateLimiter rateLimiter = initCDRL(() -> new FixedWindowRateLimiter(5, 1000));
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.tryRequest());
            assertEquals(i + 1, rateLimiter.getUsedQuota());
        }
        assertFalse(rateLimiter.tryRequest());
        sleep(1000);
        assertTrue(rateLimiter.tryRequest());
        assertEquals(1, rateLimiter.getUsedQuota());
    }

    @Test
    void testGetTimeUntilNextRequest() {
        FixedWindowRateLimiter rateLimiter = initCDRL(() -> new FixedWindowRateLimiter(5, 1000));
        for (int i = 0; i < 5; i++)
            assertTrue(rateLimiter.tryRequest());
        assertEquals(1000, rateLimiter.getTimeUntilNextRequest());
        sleep(1000);
        assertEquals(0, rateLimiter.getTimeUntilNextRequest());
    }

    @Test
    void testReset() {
        FixedWindowRateLimiter rateLimiter = initCDRL(() -> new FixedWindowRateLimiter(5, 1000));
        for (int i = 0; i < 5; i++)
            assertTrue(rateLimiter.tryRequest());
        assertFalse(rateLimiter.tryRequest());
        rateLimiter.reset();
        assertTrue(rateLimiter.tryRequest());
    }

}
