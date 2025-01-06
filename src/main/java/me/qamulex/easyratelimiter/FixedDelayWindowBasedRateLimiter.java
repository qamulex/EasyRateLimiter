/*
 * Created on Jan 06, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter;

import java.time.Clock;

import lombok.NonNull;

public class FixedDelayWindowBasedRateLimiter extends ClockDependentRateLimiter {

    private final FixedDelayRateLimiter  fixedDelayRateLimiter;
    private final WindowBasedRateLimiter windowBasedRateLimiter;

    public FixedDelayWindowBasedRateLimiter(
            @NonNull FixedDelayRateLimiter fixedDelayRateLimiter,
            @NonNull WindowBasedRateLimiter windowBasedRateLimiter
    ) {
        this.fixedDelayRateLimiter = fixedDelayRateLimiter;
        this.windowBasedRateLimiter = windowBasedRateLimiter;
    }

    @Override
    public void setClock(@NonNull Clock clock) {
        super.setClock(clock);
        fixedDelayRateLimiter.setClock(clock);
        windowBasedRateLimiter.setClock(clock);
    }

    @Override
    protected long getTimeUntilNextRequest(long timeMillis) {
        return Math.max(
                fixedDelayRateLimiter.getTimeUntilNextRequest(timeMillis),
                windowBasedRateLimiter.getTimeUntilNextRequest(timeMillis)
        );
    }

    @Override
    protected boolean isRequestAllowed(long timeMillis) {
        return fixedDelayRateLimiter.isRequestAllowed(timeMillis)
                && windowBasedRateLimiter.isRequestAllowed(timeMillis);
    }

    @Override
    public synchronized boolean tryRequest() {
        return isRequestAllowed()
                && fixedDelayRateLimiter.tryRequest()
                && windowBasedRateLimiter.tryRequest();
    }

    @Override
    public void reset() {
        fixedDelayRateLimiter.reset();
        windowBasedRateLimiter.reset();
    }

    @Override
    public String toString() {
        return String.format(
                "FixedDelayWindowBasedRateLimiter [%s, %s]",

                fixedDelayRateLimiter,
                windowBasedRateLimiter
        );
    }

}
