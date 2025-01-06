/*
 * Created on Jan 06, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter;

public class FixedWindowRateLimiter extends WindowBasedRateLimiter {

    private int  usedQuota            = 0;
    private long nextWindowTimeMillis = 0;

    public FixedWindowRateLimiter(int maxQuota, long windowSizeMillis) {
        super(maxQuota, windowSizeMillis);
    }

    @Override
    public long getTimeUntilNextRequest() {
        return usedQuota < getMaxQuota()
                ? 0
                : Math.max(0, nextWindowTimeMillis - currentTimeMillis());
    }

    @Override
    public boolean isRequestAllowed() {
        return isRequestAllowed(currentTimeMillis());
    }

    private boolean isRequestAllowed(long timeMillis) {
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

}
