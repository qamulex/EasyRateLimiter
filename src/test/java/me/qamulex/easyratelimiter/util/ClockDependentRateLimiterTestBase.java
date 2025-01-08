/*
 * Created on Jan 08, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter.util;

import java.time.Clock;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;

import me.qamulex.easyratelimiter.impl.ClockDependentRateLimiter;

public class ClockDependentRateLimiterTestBase {

    private final ControllableClock clock = new ControllableClock();

    protected Clock getClock() {
        return clock;
    }

    protected long getTime() {
        return clock.millis();
    }

    protected void setTime(long millis) {
        clock.setMillis(millis);
    }

    protected void sleep(long millis) {
        setTime(getTime() + millis);
    }

    protected <T extends ClockDependentRateLimiter> T initCDRL(Supplier<T> initializer) {
        T rateLimiter = initializer.get();
        rateLimiter.setClock(clock);
        return rateLimiter;
    }

    @BeforeEach
    void resetClock() {
        setTime(System.currentTimeMillis());
    }
    
}
