/*
 * Created on Jan 06, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter.impl;

import me.qamulex.easyratelimiter.util.WindowType;

public abstract class WindowBasedRateLimiter extends ClockDependentRateLimiter {

    private int  maxQuota;
    private long windowSizeMillis;

    public WindowBasedRateLimiter(int maxQuota, long windowSizeMillis) {
        setMaxQuota(maxQuota);
        setWindowSizeMillis(windowSizeMillis);
    }

    public abstract WindowType getWindowType();

    public int getMaxQuota() {
        return maxQuota;
    }

    public void setMaxQuota(int maxQuota) {
        if (maxQuota <= 1)
            throw new IllegalArgumentException("maxQuota must be greater than 1");

        this.maxQuota = maxQuota;
    }

    public long getWindowSizeMillis() {
        return windowSizeMillis;
    }

    public void setWindowSizeMillis(long windowSizeMillis) {
        if (windowSizeMillis <= 0)
            throw new IllegalArgumentException("windowSizeMillis must be greater than 0");

        this.windowSizeMillis = windowSizeMillis;
    }

}
