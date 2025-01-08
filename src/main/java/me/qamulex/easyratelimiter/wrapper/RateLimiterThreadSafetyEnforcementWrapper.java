/*
 * Created on Jan 06, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter.wrapper;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import lombok.NonNull;
import me.qamulex.easyratelimiter.RateLimiter;

public class RateLimiterThreadSafetyEnforcementWrapper implements RateLimiter {

    private final RateLimiter wrappedRateLimiter;

    public RateLimiterThreadSafetyEnforcementWrapper(@NonNull RateLimiter wrappedRateLimiter) {
        this.wrappedRateLimiter = wrappedRateLimiter;
    }

    public RateLimiter getWrappedRateLimiter() {
        return wrappedRateLimiter;
    }

    @Override
    public synchronized long getTimeUntilNextRequest() {
        return wrappedRateLimiter.getTimeUntilNextRequest();
    }

    @Override
    public synchronized boolean isRequestAllowed() {
        return wrappedRateLimiter.isRequestAllowed();
    }

    @Override
    public synchronized boolean tryRequest() {
        return wrappedRateLimiter.tryRequest();
    }

    @Override
    public void blockUntilRequestAllowed() throws InterruptedException {
        wrappedRateLimiter.blockUntilRequestAllowed();
    }

    @Override
    public boolean blockUntilRequestAllowed(long timeout, TimeUnit unit) throws InterruptedException {
        return wrappedRateLimiter.blockUntilRequestAllowed(timeout, unit);
    }

    @Override
    public boolean blockUntilRequestAllowed(Duration duration) throws InterruptedException {
        return wrappedRateLimiter.blockUntilRequestAllowed(duration);
    }

    @Override
    public synchronized void reset() {
        wrappedRateLimiter.reset();
    }

    @Override
    public String toString() {
        return String.format("RateLimiterThreadSafetyEnforcementWrapper [wrappedRateLimiter=%s]", wrappedRateLimiter);
    }

}
