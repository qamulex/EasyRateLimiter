/*
 * Created on Fri Sep 22 2023
 *
 * Copyright (c) qamulex
 */

package me.qamulex.erl;

import java.util.Arrays;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
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

    @Test
    public void bobHasTwoAvailableRequestsButAliceHasOnlyOneRequest() {
        simpleLimiter.reset();

        simpleLimiter.request(ALICE);

        assertEquals(2, simpleLimiter.availableRequestsOf(BOB));
        assertEquals(1, simpleLimiter.availableRequestsOf(ALICE));
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
    public void bobAndAliceHasOneAvailableRequestAfterThreeRequestsOnLimiterWithDelay() {
        limiterWithDelay.reset();

        for (int i = 0; i < 3; i++) 
            entities.forEach(limiterWithDelay::request);

        assertEquals(1, limiterWithDelay.availableRequestsOf(BOB));
        assertEquals(1, limiterWithDelay.availableRequestsOf(ALICE));
    }

    @Test
    public void bobAndAliceCanRequestAfterSleepOnLimiterWithDelay() throws InterruptedException {
        limiterWithDelay.reset();

        entities.forEach(limiterWithDelay::request);

        Thread.sleep(110);

        assertTrue(limiterWithDelay.canRequest(BOB));
        assertTrue(limiterWithDelay.canRequest(ALICE));
    }

    MultiChannelRateLimiter<Object> limiterWithWeakChannels = MultiChannelRateLimiter
            .<Object>builder()
            .useRateLimiter(
                    builder -> builder
                            .setMaximumBandwidth(1)
                            .setTimeRange(1, TimeUnit.SECONDS)
            )
            .useChannelsMap(WeakHashMap::new)
            .build();

    @Test 
    public void limiterWithWeakChannelsWorks() throws InterruptedException {
        limiterWithWeakChannels.reset();

        Object object = new Object();
        
        limiterWithWeakChannels.request(object);

        assertFalse(limiterWithWeakChannels.canRequest(object));

        object = null;
        System.gc();
        Thread.sleep(100);

        assertEquals(0, limiterWithWeakChannels.countChannels());
    }

}
