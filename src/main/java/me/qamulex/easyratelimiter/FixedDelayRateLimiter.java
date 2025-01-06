/*
 * Created on Jan 06, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter;

public class FixedDelayRateLimiter extends ClockDependentRateLimiter {

    private final long delayMillis;

    private long nextRequestTime = 0;

    public FixedDelayRateLimiter(long delayMillis) {
        this.delayMillis = delayMillis;
    }

    @Override
    public long getTimeUntilNextRequest() {
        return nextRequestTime - currentTimeMillis();
    }

    @Override
    public boolean isRequestAllowed() {
        return isRequestAllowed(currentTimeMillis());
    }

    private boolean isRequestAllowed(long timeMillis) {
        return timeMillis >= nextRequestTime;
    }

    @Override
    public boolean tryRequest() {
        long now = currentTimeMillis();

        if (!isRequestAllowed(now))
            return false;

        nextRequestTime = now + delayMillis;
        return true;
    }

    @Override
    public void reset() {
        nextRequestTime = 0;
    }

}
