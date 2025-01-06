/*
 * Created on Jan 06, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import me.qamulex.easyratelimiter.RateLimiter;

public class CompositeRateLimiter extends AbstractRateLimiter {

    private final List<RateLimiter> rateLimiters;

    public CompositeRateLimiter(Collection<RateLimiter> rateLimiters) {
        this.rateLimiters = new ArrayList<>(rateLimiters);
    }

    @Override
    public long getTimeUntilNextRequest() {
        return rateLimiters.stream()
                .mapToLong(RateLimiter::getTimeUntilNextRequest)
                .max()
                .orElse(0);
    }

    @Override
    public boolean isRequestAllowed() {
        return rateLimiters.stream().allMatch(RateLimiter::isRequestAllowed);
    }

    @Override
    public synchronized boolean tryRequest() {
        return isRequestAllowed()
                && rateLimiters.stream().allMatch(RateLimiter::tryRequest);
    }

    @Override
    public void reset() {
        rateLimiters.forEach(RateLimiter::reset);
    }

}
