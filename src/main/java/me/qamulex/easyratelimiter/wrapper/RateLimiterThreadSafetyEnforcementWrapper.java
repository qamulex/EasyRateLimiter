/*
 * Created on Jan 06, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter.wrapper;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import lombok.NonNull;
import lombok.Synchronized;
import me.qamulex.easyratelimiter.RateLimiter;

public class RateLimiterThreadSafetyEnforcementWrapper implements RateLimiter {

    private final Object      lock = new Object();
    private final RateLimiter wrappedRateLimiter;

    public RateLimiterThreadSafetyEnforcementWrapper(@NonNull RateLimiter wrappedRateLimiter) {
        this.wrappedRateLimiter = wrappedRateLimiter;
    }

    @Override
    @Synchronized("lock")
    public long getTimeUntilNextRequest() {
        return wrappedRateLimiter.getTimeUntilNextRequest();
    }

    @Override
    @Synchronized("lock")
    public boolean isRequestAllowed() {
        return wrappedRateLimiter.isRequestAllowed();
    }

    @Override
    @Synchronized("lock")
    public boolean tryRequest() {
        return wrappedRateLimiter.tryRequest();
    }

    @Override
    public void blockUntilRequestAllowed() throws InterruptedException {
        while (true) {
            long waitMillis;
            synchronized (lock) {
                if (wrappedRateLimiter.tryRequest())
                    return;

                waitMillis = wrappedRateLimiter.getTimeUntilNextRequest();
            }

            Thread.sleep(waitMillis);
        }
    }

    @Override
    public boolean blockUntilRequestAllowed(long timeout, TimeUnit unit) throws InterruptedException {
        long deadlineNanos = System.nanoTime() + unit.toNanos(timeout);

        while (true) {
            long waitMillis;
            synchronized (lock) {
                if (wrappedRateLimiter.tryRequest())
                    return true;

                waitMillis = wrappedRateLimiter.getTimeUntilNextRequest();
            }

            long remainingNanos = deadlineNanos - System.nanoTime();
            if (remainingNanos <= 0)
                return false;

            long sleepMillis = Math.min(waitMillis, TimeUnit.NANOSECONDS.toMillis(remainingNanos));
            if (sleepMillis <= 0)
                return false;

            Thread.sleep(sleepMillis);
        }
    }

    @Override
    public boolean blockUntilRequestAllowed(Duration duration) throws InterruptedException {
        return !(duration.isNegative() || duration.isZero())
                && blockUntilRequestAllowed(duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    @Synchronized("lock")
    public void reset() {
        wrappedRateLimiter.reset();
    }

    @Override
    public String toString() {
        return String.format("RateLimiterThreadSafetyEnforcementWrapper [wrappedRateLimiter=%s]", wrappedRateLimiter);
    }

}
