/*
 * Created on Jan 06, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter;

public abstract class WindowBasedRateLimiter extends ClockDependentRateLimiter {

    private final int  maxQuota;
    private final long windowSizeMillis;

    public WindowBasedRateLimiter(int maxQuota, long windowSizeMillis) {
        if (maxQuota <= 1)
            throw new IllegalArgumentException("maxQuota must be greater than 1");
        if (windowSizeMillis <= 0)
            throw new IllegalArgumentException("windowSizeMillis must be greater than 0");

        this.maxQuota = maxQuota;
        this.windowSizeMillis = windowSizeMillis;
    }

    public int getMaxQuota() {
        return maxQuota;
    }

    public long getWindowSizeMillis() {
        return windowSizeMillis;
    }

}
