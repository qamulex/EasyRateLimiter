/*
 * Created on Fri Sep 22 2023
 *
 * Copyright (c) qamulex
 */

package me.qamulex.erl;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MultiChannelTest {

    final String BOB = "bob";
    final String ALICE = "alice";

    final List<String> entities = Arrays.asList(BOB, ALICE);

    MultiChannelRateLimiter<String> simpleLimiter = MultiChannelRateLimiter
            .<String>builder()
            .useRateLimiter(
                    builder -> builder
                            .setMaximumBandwidth(2)
                            .setTimeRange(1, TimeUnit.SECONDS)
            )
            .build();

    @Test
    public void bobCannotRequestButAliceCan() {
        simpleLimiter.reset();

        simpleLimiter.request(BOB);
        simpleLimiter.request(BOB);

        assertFalse(simpleLimiter.canRequest(BOB));
        assertTrue(simpleLimiter.canRequest(ALICE));
    }

    MultiChannelRateLimiter<String> limiterWithDelay = MultiChannelRateLimiter
            .<String>builder()
            .useRateLimiter(
                    builder -> builder
                            .setMaximumBandwidth(2)
                            .setTimeRange(1, TimeUnit.SECONDS)
                            .setDelayBetweenRequests(100, TimeUnit.MILLISECONDS)
            )
            .build();

    @Test
    public void bobAndAliceCanRequestAfterSleepOnLimiterWithDelay() throws InterruptedException {
        limiterWithDelay.reset();

        entities.forEach(limiterWithDelay::request);

        Thread.sleep(110);

        assertTrue(limiterWithDelay.canRequest(BOB));
        assertTrue(limiterWithDelay.canRequest(ALICE));
    }

}
