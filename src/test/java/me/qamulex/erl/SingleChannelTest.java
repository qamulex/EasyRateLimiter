/*
 * Created on Fri Sep 22 2023
 *
 * Copyright (c) qamulex
 */

package me.qamulex.erl;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SingleChannelTest {

    SingleChannelRateLimiter simpleLimiter = SingleChannelRateLimiter
            .builder()
            .setMaximumBandwidth(3)
            .setTimeRange(1, TimeUnit.SECONDS)
            .build();

    @Test
    public void canRequestIsTrueAfterSingleRequest() {
        simpleLimiter.reset();

        simpleLimiter.request();

        assertTrue(simpleLimiter.canRequest());
    }

    @Test
    public void canRequestIsFalseAfterThreeRequests() {
        simpleLimiter.reset();

        simpleLimiter.request();
        simpleLimiter.request();
        simpleLimiter.request();

        assertFalse(simpleLimiter.canRequest());
    }

    @Test
    public void requestIsTrueAfterTwoRequests() {
        simpleLimiter.reset();

        simpleLimiter.request();
        simpleLimiter.request();

        assertTrue(simpleLimiter.request());
    }

    @Test
    public void canRequestIsTrueAfterThreeRequestsAndSleep() throws InterruptedException {
        simpleLimiter.reset();

        simpleLimiter.request();
        simpleLimiter.request();
        simpleLimiter.request();

        Thread.sleep(1010);

        assertTrue(simpleLimiter.canRequest());
    }

    SingleChannelRateLimiter limiterWithDelay = SingleChannelRateLimiter
            .builder()
            .setMaximumBandwidth(3)
            .setTimeRange(1, TimeUnit.SECONDS)
            .setDelayBetweenRequests(100, TimeUnit.MILLISECONDS)
            .build();

    @Test
    public void canRequestIsFalseAfterSingleRequestOnLimiterWithDelay() {
        limiterWithDelay.reset();

        limiterWithDelay.request();

        assertFalse(limiterWithDelay.canRequest());
    }

    @Test
    public void counterMustBeOneAfterFiveRequestsOnLimiterWithDelay() {
        limiterWithDelay.reset();

        int counter = 0;
        for (int i = 0; i < 5; i++) {
            if (limiterWithDelay.request())
                counter++;
        }

        assertEquals(1, counter);
    }

    @Test
    public void counterMustBeThreeAfterFiveRequestsOnLimiterWithDelay() throws InterruptedException {
        limiterWithDelay.reset();

        int counter = 0;
        for (int i = 0; i < 5; i++) {
            if (limiterWithDelay.request())
                counter++;
            Thread.sleep(200);
        }

        assertEquals(3, counter);
    }

}
