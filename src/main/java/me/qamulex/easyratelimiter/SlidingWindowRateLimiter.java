/*
 * Created on Jan 06, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter;

public class SlidingWindowRateLimiter extends ClockDependentRateLimiter {

    private final long[] window;
    private final long   windowSizeMillis;
    private final int    lastWindowSlotIndex;

    public SlidingWindowRateLimiter(int quota, long windowSizeMillis) {
        if (quota <= 1)
            throw new IllegalArgumentException("maxRequests must be greater than 1");
        if (windowSizeMillis <= 0)
            throw new IllegalArgumentException("windowSizeMillis must be greater than 0");

        window = new long[quota];
        this.windowSizeMillis = windowSizeMillis;
        lastWindowSlotIndex = quota - 1;
    }

    private long getOldestTimestamp() {
        return window[lastWindowSlotIndex];
    }

    @Override
    public long getTimeUntilNextRequest() {
        long oldest = getOldestTimestamp();

        if (oldest == 0)
            return 0;

        long elapsed = currentTimeMillis() - oldest;
        long remaining = windowSizeMillis - elapsed;

        return Math.max(0, remaining);
    }

    @Override
    public boolean isRequestAllowed() {
        return isRequestAllowed(currentTimeMillis());
    }

    private boolean isRequestAllowed(long timeMillis) {
        return getOldestTimestamp() == 0
                || (timeMillis - getOldestTimestamp()) >= windowSizeMillis;
    }

    @Override
    public boolean tryRequest() {
        long now = currentTimeMillis();

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
