/*
 * Created on Jan 06, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter.impl;

import me.qamulex.easyratelimiter.util.WindowType;

/**
 * An abstract rate limiter that enforces limits based on a time window.
 * 
 * <p>
 * Defines properties such as maximum quota and window size, and provides
 * basic logic for managing them.
 * </p>
 * 
 * <p>
 * Subclasses can implement specific windowing strategies, such as fixed
 * or sliding windows, by overriding abstract methods.
 * </p>
 */
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
