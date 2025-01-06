/*
 * Created on Jan 06, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter.impl;

public class FixedDelayRateLimiter extends ClockDependentRateLimiter {

    private long delayMillis;

    private long nextRequestTime = 0;

    public FixedDelayRateLimiter(long delayMillis) {
        setDelayMillis(delayMillis);
    }

    public long getDelayMillis() {
        return delayMillis;
    }

    public void setDelayMillis(long delayMillis) {
        if (delayMillis <= 0)
            throw new IllegalArgumentException("delayMillis must be greater than 0");

        this.delayMillis = delayMillis;
        reset();
    }

    @Override
    protected long getTimeUntilNextRequest(long timeMillis) {
        return Math.max(0, nextRequestTime - timeMillis);
    }

    @Override
    protected boolean isRequestAllowed(long timeMillis) {
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

    @Override
    public String toString() {
        return String.format(
                "FixedDelayRateLimiter [delayMillis=%d, nextRequestTime=%d, timeUntilNextRequest=%d]",
                
                delayMillis,
                nextRequestTime,
                getTimeUntilNextRequest()
        );
    }

}
