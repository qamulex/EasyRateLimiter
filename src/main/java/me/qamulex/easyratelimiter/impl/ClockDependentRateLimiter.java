/*
 * Created on Jan 06, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter.impl;

import java.time.Clock;

import lombok.NonNull;
import me.qamulex.easyratelimiter.util.NanoTimeClock;

/**
 * An abstract rate limiter that relies on a Clock instance for time-based calculations.
 * 
 * <p>
 * Subclasses can use the provided clock to control the timing of requests
 * and customize rate-limiting logic.
 * </p>
 * 
 * <p>
 * Allows easy integration of custom time sources for testing or special scenarios.
 * </p>
 */
public abstract class ClockDependentRateLimiter extends AbstractRateLimiter {

    private Clock clock = NanoTimeClock.INSTANCE;

    public Clock getClock() {
        return clock;
    }

    protected long currentTimeMillis() {
        return clock.millis();
    }

    public void setClock(@NonNull Clock clock) {
        this.clock = clock;
    }

    @Override
    public long getTimeUntilNextRequest() {
        return getTimeUntilNextRequest(currentTimeMillis());
    }

    protected abstract long getTimeUntilNextRequest(long timeMillis);

    @Override
    public boolean isRequestAllowed() {
        return isRequestAllowed(currentTimeMillis());
    }

    protected abstract boolean isRequestAllowed(long timeMillis);

}
