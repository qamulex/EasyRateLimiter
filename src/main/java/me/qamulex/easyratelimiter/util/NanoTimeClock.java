/*
 * Created on Jan 06, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter.util;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

public class NanoTimeClock extends Clock {

    public static final NanoTimeClock INSTANCE = new NanoTimeClock();

    @Override
    public ZoneId getZone() {
        throw new UnsupportedOperationException("getZone");
    }

    @Override
    public long millis() {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
    }

    @Override
    public Instant instant() {
        return Instant.ofEpochMilli(millis());
    }

    @Override
    public Clock withZone(ZoneId zone) {
        throw new UnsupportedOperationException("withZone");
    }

}
