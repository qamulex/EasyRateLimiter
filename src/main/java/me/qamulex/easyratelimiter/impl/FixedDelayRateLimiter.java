/*
 * Created on Jan 06, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter.impl;

/**
 * A rate limiter that enforces a fixed delay between requests.
 * 
 * <p>
 * Requests are allowed only after a specified delay has elapsed since the
 * last successful request.
 * </p>
 * 
 * <p>
 * Useful for controlling the frequency of events or actions.
 * </p>
 */
public class FixedDelayRateLimiter extends ClockDependentRateLimiter {

    private long delayMillis;

    private long nextRequestTimeMillis = 0;

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

    public long getNextRequestTimeMillis() {
        return nextRequestTimeMillis;
    }

    @Override
    protected long getTimeUntilNextRequest(long timeMillis) {
        return Math.max(0, nextRequestTimeMillis - timeMillis);
    }

    @Override
    protected boolean isRequestAllowed(long timeMillis) {
        return timeMillis >= nextRequestTimeMillis;
    }

    @Override
    public boolean tryRequest() {
        long now = currentTimeMillis();

        if (!isRequestAllowed(now))
            return false;

        nextRequestTimeMillis = now + delayMillis;
        return true;
    }

    @Override
    public void reset() {
        nextRequestTimeMillis = 0;
    }

    @Override
    public String toString() {
        return String.format(
                "FixedDelayRateLimiter [delayMillis=%d, nextRequestTimeMillis=%d, timeUntilNextRequest=%d]",

                delayMillis,
                nextRequestTimeMillis,
                getTimeUntilNextRequest()
        );
    }

}
