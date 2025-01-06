/*
 * Created on Jan 06, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public abstract class AbstractRateLimiter implements RateLimiter {

    @Override
    public void blockUntilRequestAllowed() throws InterruptedException {
        while (!tryRequest())
            Thread.sleep(getTimeUntilNextRequest());
    }

    @Override
    public boolean blockUntilRequestAllowed(long timeout, TimeUnit unit) throws InterruptedException {
        long startTime = System.nanoTime();
        long timeoutNanos = unit.toNanos(timeout);

        while (!tryRequest()) {
            long elapsed = System.nanoTime() - startTime;

            if (elapsed >= timeoutNanos)
                return false;

            long remainingNanos = timeoutNanos - elapsed;
            long remainingMillis = Math.min(getTimeUntilNextRequest(), TimeUnit.NANOSECONDS.toMillis(remainingNanos));

            if (remainingMillis > 0) {
                Thread.sleep(remainingMillis);
            } else {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean blockUntilRequestAllowed(Duration duration) throws InterruptedException {
        return !(duration.isNegative() || duration.isZero())
                && blockUntilRequestAllowed(duration.toMillis(), TimeUnit.MILLISECONDS);
    }

}
