/*
 * Created on Jan 06, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter;

import java.util.concurrent.TimeUnit;

public class SlidingWindowRateLimiter extends NanoTimeBasedRateLimiter {

    private final long[] window;
    private final long   windowSizeNanos;
    private final int    lastWindowSlotIndex;

    public SlidingWindowRateLimiter(int maxRequests, long windowSizeMillis) {
        if (maxRequests <= 1)
            throw new IllegalArgumentException("maxRequests must be greater than 1");
        if (windowSizeMillis <= 0)
            throw new IllegalArgumentException("windowSizeMillis must be greater than 0");

        window = new long[maxRequests];
        windowSizeNanos = TimeUnit.MILLISECONDS.toNanos(windowSizeMillis);
        lastWindowSlotIndex = maxRequests - 1;
    }

    private long getOldestTimestamp() {
        return window[lastWindowSlotIndex];
    }

    @Override
    protected long getNanoTimeUntilNextRequest() {
        long oldest = getOldestTimestamp();

        if (oldest == 0)
            return 0;

        long elapsed = System.nanoTime() - oldest;
        long remaining = windowSizeNanos - elapsed;

        return Math.max(0, remaining);
    }

    @Override
    public boolean isRequestAllowed() {
        return isRequestAllowed(System.nanoTime());
    }

    private boolean isRequestAllowed(long nanoTime) {
        return getOldestTimestamp() == 0
                || (nanoTime - getOldestTimestamp()) >= windowSizeNanos;
    }

    @Override
    public boolean tryRequest() {
        long now = System.nanoTime();

        if (!isRequestAllowed(now))
            return false;

        for (int i = 1; i < window.length; i++)
            window[i] = window[i - 1];
        window[0] = now;

        return true;
    }

    @Override
    public void reset() {
        for (int i = 0; i < window.length; i++)
            window[i] = 0;
    }

}
