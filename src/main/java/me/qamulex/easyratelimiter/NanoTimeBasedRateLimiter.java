/*
 * Created on Jan 06, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter;

import java.util.concurrent.TimeUnit;

public abstract class NanoTimeBasedRateLimiter extends AbstractRateLimiter {

    protected abstract long getNanoTimeUntilNextRequest();

    @Override
    public long getTimeUntilNextRequest() {
        return TimeUnit.NANOSECONDS.toMillis(
                Math.max(
                        0,
                        getNanoTimeUntilNextRequest()
                )
        );
    }
    
}
