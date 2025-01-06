/*
 * Created on Jan 06, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter;

public abstract class WindowBasedRateLimiter extends ClockDependentRateLimiter {

    private int  maxQuota;
    private long windowSizeMillis;

    public WindowBasedRateLimiter(int maxQuota, long windowSizeMillis) {
        setMaxQuota(maxQuota);
        setWindowSizeMillis(windowSizeMillis);
    }

    public int getMaxQuota() {
        return maxQuota;
    }

    public void setMaxQuota(int maxQuota) {
        if (maxQuota <= 1)
            throw new IllegalArgumentException("maxQuota must be greater than 1");

        this.maxQuota = maxQuota;
        reset();
    }

    public long getWindowSizeMillis() {
        return windowSizeMillis;
    }

    public void setWindowSizeMillis(long windowSizeMillis) {
        if (windowSizeMillis <= 0)
            throw new IllegalArgumentException("windowSizeMillis must be greater than 0");

        this.windowSizeMillis = windowSizeMillis;
        reset();
    }

}
