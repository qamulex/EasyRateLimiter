/*
 * Created on Jan 06, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter;

import java.time.Clock;

import lombok.NonNull;

public abstract class ClockDependentRateLimiter extends AbstractRateLimiter {

    private Clock clock = new NanoTimeClock();

    protected final Clock getClock() {
        return clock;
    }

    protected final long currentTimeMillis() {
        return clock.millis();
    }

    public final void setClock(@NonNull Clock clock) {
        this.clock = clock;
    }
    
}
