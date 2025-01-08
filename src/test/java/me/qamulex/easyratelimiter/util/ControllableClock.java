/*
 * Created on Jan 08, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter.util;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public class ControllableClock extends Clock {

    private long millis = 0;

    public void setMillis(long millis) {
        this.millis = millis;
    }

    @Override
    public ZoneId getZone() {
        throw new UnsupportedOperationException("getZone");
    }

    @Override
    public Clock withZone(ZoneId zone) {
        throw new UnsupportedOperationException("withZone");
    }

    @Override
    public long millis() {
        return millis;
    }

    @Override
    public Instant instant() {
        return Instant.ofEpochMilli(millis);
    }

}
