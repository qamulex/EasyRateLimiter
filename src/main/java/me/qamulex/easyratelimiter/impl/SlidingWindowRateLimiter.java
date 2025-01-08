/*
 * Created on Jan 06, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter.impl;

import me.qamulex.easyratelimiter.util.CircularBuffer;
import me.qamulex.easyratelimiter.util.WindowType;

/**
 * A rate limiter that allows a fixed number of requests over a sliding time window.
 * 
 * <p>
 * Requests are tracked dynamically, and the quota is evaluated
 * over the last specified time period.
 * 
 * <p>
 * Provides finer control compared to fixed window rate limiting.
 */
public class SlidingWindowRateLimiter extends WindowBasedRateLimiter {

    private CircularBuffer<Long> window;

    private long nextPossibleRequestTimeMillis = 0;

    public SlidingWindowRateLimiter(int maxQuota, long windowSizeMillis) {
        super(maxQuota, windowSizeMillis);
    }

    @Override
    public WindowType getWindowType() {
        return WindowType.SLIDING;
    }

    @Override
    public void setMaxQuota(int maxQuota) {
        super.setMaxQuota(maxQuota);

        window = new CircularBuffer<>(new Long[maxQuota]);
        nextPossibleRequestTimeMillis = 0;
    }

    public int getUsedQuota() {
        long now = currentTimeMillis();

        int usedQuota = 0;
        for (long timestamp : window) {
            if (timestamp != 0 && now - timestamp <= getWindowSizeMillis())
                usedQuota++;
        }

        return usedQuota;
    }

    @Override
    public void setWindowSizeMillis(long windowSizeMillis) {
        super.setWindowSizeMillis(windowSizeMillis);

        reset();
    }

    public long getNextPossibleRequestTimeMillis() {
        return nextPossibleRequestTimeMillis;
    }

    @Override
    protected long getTimeUntilNextRequest(long timeMillis) {
        return Math.max(0, nextPossibleRequestTimeMillis - timeMillis);
    }

    @Override
    protected boolean isRequestAllowed(long timeMillis) {
        return nextPossibleRequestTimeMillis <= timeMillis;
    }

    @Override
    public boolean tryRequest() {
        long now = currentTimeMillis();

        if (!isRequestAllowed(now))
            return false;

        window.add(now);

        if (window.isFull()) {
            long oldestTimestamp = window.getFirst();
            if (oldestTimestamp != 0)
                nextPossibleRequestTimeMillis = oldestTimestamp + getWindowSizeMillis();
        }

        return true;
    }

    @Override
    public void reset() {
        window.clear();
        nextPossibleRequestTimeMillis = 0;
    }

    @Override
    public String toString() {
        return String.format(
                "SlidingWindowRateLimiter [maxQuota=%d, windowSizeMillis=%d, usedQuota=%d, nextPossibleRequestTimeMillis=%d, timeUntilNextRequest=%d]",

                getMaxQuota(),
                getWindowSizeMillis(),
                getUsedQuota(),
                nextPossibleRequestTimeMillis,
                getTimeUntilNextRequest()
        );
    }

}
