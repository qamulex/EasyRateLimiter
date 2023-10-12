package me.qamulex.erl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BasicTest {

    private static final int  MAXIMUM_BANDWIDTH      = 5;
    private static final long AFFECTED_TIME_RANGE    = 500;
    private static final long DELAY_BETWEEN_REQUESTS = 50;

    RateLimiter delayLimiter = RateLimiterBuilder.newBuilder()
            .setMaximumBandwidth(1)
            .setAffectedTimeRange(DELAY_BETWEEN_REQUESTS)
            .setDelayBetweenRequests(0)
            .build();

    RateLimiter bandwidthLimiter = RateLimiterBuilder.newBuilder()
            .setMaximumBandwidth(MAXIMUM_BANDWIDTH)
            .setAffectedTimeRange(AFFECTED_TIME_RANGE)
            .setDelayBetweenRequests(0)
            .build();

    RateLimiter bandwidthLimiterWithDelay = RateLimiterBuilder.newBuilder()
            .setMaximumBandwidth(MAXIMUM_BANDWIDTH)
            .setAffectedTimeRange(AFFECTED_TIME_RANGE)
            .setDelayBetweenRequests(DELAY_BETWEEN_REQUESTS)
            .build();

    @Test
    public void instanceTest() {

        assertEquals(DelayLimiter.class, delayLimiter.getClass());

        assertEquals(BandwidthLimiter.class, bandwidthLimiter.getClass());

        assertEquals(BandwidthLimiterWithDelay.class, bandwidthLimiterWithDelay.getClass());

    }

    @Test
    public void delayLimiterTest() throws InterruptedException {
        RateLimiter limiter = delayLimiter;

        limiter.reset();
        assertTrue(limiter.canRequest());

        assertTrue(limiter.request());
        assertFalse(limiter.request());

        Thread.sleep(DELAY_BETWEEN_REQUESTS + 1L);
        assertTrue(limiter.request());

        assertEquals(DELAY_BETWEEN_REQUESTS, limiter.remainingTimeInMillis(), 10L);
    }

    @Test
    public void bandwidthLimiterTest() throws InterruptedException {
        RateLimiter limiter = bandwidthLimiter;

        limiter.reset();
        assertTrue(limiter.canRequest());

        for (int i = 1; i < MAXIMUM_BANDWIDTH; i++)
            limiter.request();
        assertTrue(limiter.request());
        assertFalse(limiter.request());

        Thread.sleep(AFFECTED_TIME_RANGE + 1L);
        assertTrue(limiter.request());

        limiter.reset();
        assertTrue(limiter.canRequest());

        int counter = 0;
        for (int i = 0; i < MAXIMUM_BANDWIDTH * 2; i++) {
            if (limiter.request())
                counter++;
        }
        assertEquals(MAXIMUM_BANDWIDTH, counter);

        limiter.reset();
        assertTrue(limiter.canRequest());

        counter = 0;
        for (int i = 0; i < MAXIMUM_BANDWIDTH * 2; i++) {
            if (limiter.request())
                counter++;
            else {
                Thread.sleep(AFFECTED_TIME_RANGE + 1L);
                i--;
            }
        }
        assertEquals(MAXIMUM_BANDWIDTH * 2, counter);
    }

    @Test
    public void bandwidthLimiterWithDelayTest() throws InterruptedException {
        RateLimiter limiter = bandwidthLimiterWithDelay;

        limiter.reset();
        assertTrue(limiter.canRequest());

        int counter = 0;
        for (int i = 0; i < MAXIMUM_BANDWIDTH; i++) {
            if (limiter.request())
                counter++;
        }
        assertEquals(1, counter);
        assertFalse(limiter.request());
        assertEquals(DELAY_BETWEEN_REQUESTS, limiter.remainingTimeInMillis(), 10L);

        Thread.sleep(DELAY_BETWEEN_REQUESTS + 1L);
        assertTrue(limiter.request());

        limiter.reset();
        assertTrue(limiter.canRequest());

        counter = 0;
        for (int i = 0; i < MAXIMUM_BANDWIDTH; i++) {
            if (limiter.request())
                counter++;
            Thread.sleep(DELAY_BETWEEN_REQUESTS + 1L);
        }
        assertEquals(MAXIMUM_BANDWIDTH, counter);
        assertFalse(limiter.request());

        Thread.sleep(limiter.remainingTimeInMillis() + 1L);
        assertTrue(limiter.request());
    }

}
