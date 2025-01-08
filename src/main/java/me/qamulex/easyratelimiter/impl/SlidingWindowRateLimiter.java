/*
 * Created on Jan 06, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter.impl;

import me.qamulex.easyratelimiter.util.WindowType;

public class SlidingWindowRateLimiter extends WindowBasedRateLimiter {

    private long[] window              = new long[0];
    private int    lastWindowSlotIndex = 0;

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

        window = new long[maxQuota];
        lastWindowSlotIndex = maxQuota - 1;

        nextPossibleRequestTimeMillis = 0;
    }

    public int getUsedQuota() {
        long now = currentTimeMillis();

        int usedQuota = 0;
        for (int windowSlotIndex = 0; windowSlotIndex < window.length; windowSlotIndex++) {
            long timestamp = window[windowSlotIndex];
            if (timestamp == 0 || now - timestamp >= getWindowSizeMillis())
                break;
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

        for (int windowSlotIndex = 1; windowSlotIndex < window.length; windowSlotIndex++)
            window[windowSlotIndex] = window[windowSlotIndex - 1];
        window[0] = now;

        long oldestTimestamp = window[lastWindowSlotIndex];
        if (oldestTimestamp != 0)
            nextPossibleRequestTimeMillis = oldestTimestamp + getWindowSizeMillis();

        return true;
    }

    @Override
    public void reset() {
        for (int windowSlotIndex = 0; windowSlotIndex < window.length; windowSlotIndex++)
            window[windowSlotIndex] = 0;

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
