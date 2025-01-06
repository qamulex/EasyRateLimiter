/*
 * Created on Jan 06, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter;

import java.time.Clock;

import lombok.NonNull;

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
