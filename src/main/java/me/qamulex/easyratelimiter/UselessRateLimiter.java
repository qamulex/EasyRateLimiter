/*
 * Created on Jan 06, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class UselessRateLimiter implements RateLimiter {

    public static final UselessRateLimiter INSTANCE = new UselessRateLimiter();

    private UselessRateLimiter() {}

    @Override
    public long getTimeUntilNextRequest() {
        return 0;
    }

    @Override
    public boolean isRequestAllowed() {
        return true;
    }

    @Override
    public boolean tryRequest() {
        return true;
    }

    @Override
    public void blockUntilRequestAllowed() throws InterruptedException {}

    @Override
    public boolean blockUntilRequestAllowed(long timeout, TimeUnit unit) throws InterruptedException {
        return true;
    }

    @Override
    public boolean blockUntilRequestAllowed(Duration duration) throws InterruptedException {
        return true;
    }

    @Override
    public void reset() {}

    @Override
    public String toString() {
        return String.format("UselessRateLimiter []");
    }

}
