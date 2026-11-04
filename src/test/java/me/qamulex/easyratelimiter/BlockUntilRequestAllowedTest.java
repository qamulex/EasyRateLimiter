/*
 * Created on Jan 08, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import me.qamulex.easyratelimiter.impl.FixedDelayRateLimiter;
import me.qamulex.easyratelimiter.impl.FixedDelayWindowBasedRateLimiter;
import me.qamulex.easyratelimiter.impl.FixedWindowRateLimiter;
import me.qamulex.easyratelimiter.impl.SlidingWindowRateLimiter;
import me.qamulex.easyratelimiter.util.ExecutionTimeMeasurer;

class BlockUntilRequestAllowedTest implements ExecutionTimeMeasurer {

    @Test
    void testFixedDelayRateLimiter() {
        FixedDelayRateLimiter rateLimiter = new FixedDelayRateLimiter(50);

        assertTrue(rateLimiter.tryRequest());

        for (int i = 0; i < 10; i++) {
            long expectedWaitingTime = rateLimiter.getTimeUntilNextRequest();
            assertExecutionTimeInRange(expectedWaitingTime - 5, expectedWaitingTime + 100, rateLimiter::blockUntilRequestAllowed);
        }
    }

    @Test
    void testFixedWindowRateLimiter() {
        FixedWindowRateLimiter rateLimiter = new FixedWindowRateLimiter(5, 500);

        for (int i = 0; i < 5; i++)
            assertTrue(rateLimiter.tryRequest());

        long expectedWaitingTime = rateLimiter.getTimeUntilNextRequest();
        assertExecutionTimeInRange(expectedWaitingTime - 5, expectedWaitingTime + 100, rateLimiter::blockUntilRequestAllowed);
    }

    @Test
    void testSlidingWindowRateLimiter() throws InterruptedException {
        SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(5, 500);

        for (int i = 0; i < 5; i++) {
            Thread.sleep(50);
            assertTrue(rateLimiter.tryRequest());
        }

        long expectedWaitingTime = rateLimiter.getTimeUntilNextRequest();
        assertExecutionTimeInRange(expectedWaitingTime - 5, expectedWaitingTime + 100, rateLimiter::blockUntilRequestAllowed);
    }

    @Test
    void testFixedDelayWindowBasedRateLimiter() {
        FixedDelayWindowBasedRateLimiter rateLimiter = new FixedDelayWindowBasedRateLimiter(
                new FixedDelayRateLimiter(50),
                new FixedWindowRateLimiter(5, 500)
        );

        for (int i = 0; i < 5; i++) {
            long expectedWaitingTime = rateLimiter.getTimeUntilNextRequest();
            assertExecutionTimeInRange(expectedWaitingTime - 5, expectedWaitingTime + 100, rateLimiter::blockUntilRequestAllowed);
        }

        long expectedWaitingTime = rateLimiter.getTimeUntilNextRequest();
        assertExecutionTimeInRange(expectedWaitingTime - 5, expectedWaitingTime + 100, rateLimiter::blockUntilRequestAllowed);
    }

}
