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
import me.qamulex.easyratelimiter.impl.FixedDelayWindowBasedRateLimiter;
import me.qamulex.easyratelimiter.impl.FixedWindowRateLimiter;
import me.qamulex.easyratelimiter.util.ClockDependentRateLimiterTestBase;

class FixedDelayWindowBasedRateLimiterTest extends ClockDependentRateLimiterTestBase {

    @Test
    void testClockPropagation() {
        FixedDelayRateLimiter fixedDelayRateLimiter = new FixedDelayRateLimiter(100);
        FixedWindowRateLimiter fixedWindowRateLimiter = new FixedWindowRateLimiter(5, 1000);

        FixedDelayWindowBasedRateLimiter combinedRateLimiter = new FixedDelayWindowBasedRateLimiter(fixedDelayRateLimiter, fixedWindowRateLimiter);

        combinedRateLimiter.setClock(getClock());
        assertEquals(getClock(), fixedDelayRateLimiter.getClock());
        assertEquals(getClock(), fixedWindowRateLimiter.getClock());
    }

    @Test
    void testCombinedBehavior() {
        FixedDelayRateLimiter fixedDelayRateLimiter = new FixedDelayRateLimiter(100);
        FixedWindowRateLimiter fixedWindowRateLimiter = new FixedWindowRateLimiter(5, 1000);

        FixedDelayWindowBasedRateLimiter combinedRateLimiter = initCDRL(() -> new FixedDelayWindowBasedRateLimiter(fixedDelayRateLimiter, fixedWindowRateLimiter));

        for (int i = 0; i < 5; i++) {
            assertTrue(combinedRateLimiter.tryRequest());
            assertFalse(combinedRateLimiter.isRequestAllowed());
            assertEquals(i == 4 ? 600 : 100, combinedRateLimiter.getTimeUntilNextRequest());
            sleep(100);
            assertTrue(i == 4 || combinedRateLimiter.isRequestAllowed());
        }
        assertEquals(500, combinedRateLimiter.getTimeUntilNextRequest());
        sleep(500);
        assertTrue(combinedRateLimiter.tryRequest());
    }

}
