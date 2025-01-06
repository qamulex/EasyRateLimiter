/*
 * Created on Jan 06, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter;

public class SlidingWindowRateLimiter extends WindowBasedRateLimiter {

    private long[] window;
    private int    lastWindowSlotIndex;

    public SlidingWindowRateLimiter(int maxQuota, long windowSizeMillis) {
        super(maxQuota, windowSizeMillis);
    }

    private long getOldestTimestamp() {
        return window[lastWindowSlotIndex];
    }

    @Override
    protected long getTimeUntilNextRequest(long timeMillis) {
        long oldest = getOldestTimestamp();

        if (oldest == 0)
            return 0;

        long elapsed = timeMillis - oldest;
        long remaining = getWindowSizeMillis() - elapsed;

        return Math.max(0, remaining);
    }

    @Override
    protected boolean isRequestAllowed(long timeMillis) {
        return getOldestTimestamp() == 0
                || (timeMillis - getOldestTimestamp()) >= getWindowSizeMillis();
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
        window = new long[getMaxQuota()];
        lastWindowSlotIndex = getMaxQuota() - 1;
    }

}
