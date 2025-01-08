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

import me.qamulex.easyratelimiter.impl.FixedDelayRateLimiter;
import me.qamulex.easyratelimiter.util.ClockDependentRateLimiterTestBase;

class FixedDelayRateLimiterTest extends ClockDependentRateLimiterTestBase {

    @Test
    void testTryRequest() {
        FixedDelayRateLimiter rateLimiter = initCDRL(() -> new FixedDelayRateLimiter(1000));
        assertTrue(rateLimiter.tryRequest());
        assertFalse(rateLimiter.tryRequest());
        sleep(1000);
        assertTrue(rateLimiter.tryRequest());
    }

    @Test
    void testGetTimeUntilNextRequest() {
        FixedDelayRateLimiter rateLimiter = initCDRL(() -> new FixedDelayRateLimiter(1000));
        assertTrue(rateLimiter.tryRequest());
        assertEquals(1000, rateLimiter.getTimeUntilNextRequest());
        sleep(1000);
        assertEquals(0, rateLimiter.getTimeUntilNextRequest());
    }

    @Test
    void testReset() {
        FixedDelayRateLimiter rateLimiter = initCDRL(() -> new FixedDelayRateLimiter(1000));
        assertTrue(rateLimiter.tryRequest());
        rateLimiter.reset();
        assertTrue(rateLimiter.tryRequest());
    }

}
