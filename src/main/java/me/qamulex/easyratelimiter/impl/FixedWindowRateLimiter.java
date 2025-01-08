/*
 * Created on Jan 06, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter.impl;

import me.qamulex.easyratelimiter.util.WindowType;

/**
 * A rate limiter that allows a fixed number of requests within a predefined time window.
 * 
 * <p>
 * All requests in the same window share the same quota. Once the quota
 * is exceeded, requests are denied until the next window starts.
 * 
 * <p>
 * Suitable for scenarios where rate limits are applied in discrete time periods.
 */
public class FixedWindowRateLimiter extends WindowBasedRateLimiter {

    private int  usedQuota            = 0;
    private long nextWindowTimeMillis = 0;

    public FixedWindowRateLimiter(int maxQuota, long windowSizeMillis) {
        super(maxQuota, windowSizeMillis);
    }

    @Override
    public WindowType getWindowType() {
        return WindowType.FIXED;
    }

    @Override
    public void setMaxQuota(int maxQuota) {
        super.setMaxQuota(maxQuota);

        usedQuota = 0;
    }

    @Override
    public void setWindowSizeMillis(long windowSizeMillis) {
        super.setWindowSizeMillis(windowSizeMillis);

        nextWindowTimeMillis = 0;
    }

    public int getUsedQuota() {
        return usedQuota;
    }

    public long getNextWindowTimeMillis() {
        return nextWindowTimeMillis;
    }

    @Override
    protected long getTimeUntilNextRequest(long timeMillis) {
        return usedQuota < getMaxQuota()
                ? 0
                : Math.max(0, nextWindowTimeMillis - timeMillis);
    }

    @Override
    protected boolean isRequestAllowed(long timeMillis) {
        return usedQuota < getMaxQuota() || nextWindowTimeMillis <= timeMillis;
    }

    @Override
    public boolean tryRequest() {
        long now = currentTimeMillis();

        if (!isRequestAllowed(now))
            return false;

        if (nextWindowTimeMillis <= now) {
            usedQuota = 0;
            nextWindowTimeMillis = now + getWindowSizeMillis();
        }

        usedQuota++;

        return true;
    }

    @Override
    public void reset() {
        usedQuota = 0;
        nextWindowTimeMillis = 0;
    }

    @Override
    public String toString() {
        return String.format(
                "FixedWindowRateLimiter [maxQuota=%d, windowSizeMillis=%d, usedQuota=%d, nextWindowTimeMillis=%d, timeUntilNextRequest=%d]",

                getMaxQuota(),
                getWindowSizeMillis(),
                usedQuota,
                nextWindowTimeMillis,
                getTimeUntilNextRequest()
        );
    }

}
