/*
 * Created on Jan 06, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter;

public class SlidingWindowRateLimiter extends WindowBasedRateLimiter {

    private long[] window              = new long[0];
    private int    lastWindowSlotIndex = 0;

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
    }

    @Override
    public void setWindowSizeMillis(long windowSizeMillis) {
        super.setWindowSizeMillis(windowSizeMillis);

        reset();
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

        for (int windowSlotIndex = 1; windowSlotIndex < window.length; windowSlotIndex++)
            window[windowSlotIndex] = window[windowSlotIndex - 1];
        window[0] = now;

        return true;
    }

    @Override
    public void reset() {
        for (int windowSlotIndex = 0; windowSlotIndex < window.length; windowSlotIndex++)
            window[windowSlotIndex] = 0;
    }

    @Override
    public String toString() {
        long now = currentTimeMillis();

        int usedQuota = 0;
        for (int windowSlotIndex = 0; windowSlotIndex <= lastWindowSlotIndex; windowSlotIndex++) {
            long timestamp = window[windowSlotIndex];
            long elapsed = now - timestamp;
            if (elapsed >= getWindowSizeMillis())
                break;
            usedQuota++;
        }

        return String.format(
                "SlidingWindowRateLimiter [maxQuota=%d, windowSizeMillis=%d, usedQuota=%d, timeUntilNextRequest=%d]",

                getMaxQuota(),
                getWindowSizeMillis(),
                usedQuota,
                getTimeUntilNextRequest()
        );
    }

}
