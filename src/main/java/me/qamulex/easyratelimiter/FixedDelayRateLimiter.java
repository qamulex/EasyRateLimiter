/*
 * Created on Jan 06, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter;

import java.util.concurrent.TimeUnit;

public class FixedDelayRateLimiter extends NanoTimeBasedRateLimiter {

    private final long delayNanos;

    private long nextRequestNanoTime = 0;

    public FixedDelayRateLimiter(long delayMillis) {
        delayNanos = TimeUnit.MILLISECONDS.toNanos(delayMillis);
    }

    @Override
    protected long getNanoTimeUntilNextRequest() {
        return nextRequestNanoTime - System.nanoTime();
    }

    @Override
    public boolean isRequestAllowed() {
        return isRequestAllowed(System.nanoTime());
    }

    private boolean isRequestAllowed(long nanoTime) {
        return nanoTime >= nextRequestNanoTime;
    }

    @Override
    public boolean tryRequest() {
        long now = System.nanoTime();

        if (!isRequestAllowed(now))
            return false;

        nextRequestNanoTime = now + delayNanos;
        return true;
    }

    @Override
    public void reset() {
        nextRequestNanoTime = 0;
    }

}
