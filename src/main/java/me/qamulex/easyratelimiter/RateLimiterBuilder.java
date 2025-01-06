/*
 * Created on Jan 06, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter;

import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter(AccessLevel.PRIVATE)
@Accessors(chain = true)
public class RateLimiterBuilder {

    private Clock      clock               = null;
    private long       delayMillis         = 0;
    private long       windowSizeMillis    = 0;
    private WindowType windowType          = WindowType.SLIDING;
    private int        maxQuota            = 1;
    private boolean    enforceThreadSafety = false;

    public RateLimiterBuilder withClock(Clock clock) {
        return setClock(clock);
    }

    public RateLimiterBuilder withDelay(long delayMillis) {
        if (delayMillis < 0)
            throw new IllegalArgumentException("delay must be greater or equal to zero");

        return setDelayMillis(delayMillis);
    }

    public RateLimiterBuilder withDelay(long delay, @NonNull TimeUnit unit) {
        return withDelay(unit.toMillis(delay));
    }

    public RateLimiterBuilder withDelay(@NonNull Duration delay) {
        return withDelay(delay.toMillis());
    }

    public RateLimiterBuilder withWindowSize(long windowSizeMillis) {
        if (windowSizeMillis <= 0)
            throw new IllegalArgumentException("windowSize must be greater than zero");

        return setWindowSizeMillis(windowSizeMillis);
    }

    public RateLimiterBuilder withWindowSize(long windowSize, @NonNull TimeUnit unit) {
        return withWindowSize(unit.toMillis(windowSize));
    }

    public RateLimiterBuilder withWindowSize(@NonNull Duration windowSize) {
        return withWindowSize(windowSize.toMillis());
    }

    public RateLimiterBuilder useFixedWindow() {
        return setWindowType(WindowType.FIXED);
    }

    public RateLimiterBuilder useSlidingWindow() {
        return setWindowType(WindowType.SLIDING);
    }

    public RateLimiterBuilder withMaxQuota(int maxQuota) {
        if (maxQuota < 1)
            throw new IllegalArgumentException("maxQuota must be greater than zero");

        return setMaxQuota(maxQuota);
    }

    public RateLimiterBuilder enforceThreadSafety(boolean enforceThreadSafety) {
        return setEnforceThreadSafety(enforceThreadSafety);
    }

    public RateLimiter build() {
        FixedDelayRateLimiter fixedDelayRateLimiter = null;
        if (windowSizeMillis > 0 && maxQuota == 1)
            fixedDelayRateLimiter = new FixedDelayRateLimiter(windowSizeMillis);
        else if (delayMillis > 0)
            fixedDelayRateLimiter = new FixedDelayRateLimiter(delayMillis);

        WindowBasedRateLimiter windowBasedRateLimiter = null;
        if (windowSizeMillis > 0 && maxQuota > 1) {
            if (windowType == WindowType.SLIDING)
                windowBasedRateLimiter = new SlidingWindowRateLimiter(maxQuota, windowSizeMillis);
            else if (windowType == WindowType.FIXED)
                windowBasedRateLimiter = new FixedWindowRateLimiter(maxQuota, windowSizeMillis);
            else
                throw new IllegalStateException("unknown window type: " + windowType);
        }

        RateLimiter rateLimiter = null;
        if (fixedDelayRateLimiter != null && windowBasedRateLimiter != null) {
            rateLimiter = new FixedDelayWindowBasedRateLimiter(fixedDelayRateLimiter, windowBasedRateLimiter);
        } else if (fixedDelayRateLimiter != null) {
            rateLimiter = fixedDelayRateLimiter;
        } else if (windowBasedRateLimiter != null) {
            rateLimiter = windowBasedRateLimiter;
        }

        if (rateLimiter == null)
            return UselessRateLimiter.INSTANCE;

        if (clock != null && rateLimiter instanceof ClockDependentRateLimiter)
            ((ClockDependentRateLimiter) rateLimiter).setClock(clock);

        if (enforceThreadSafety)
            rateLimiter = new RateLimiterThreadSafetyEnforcementWrapper(rateLimiter);

        return rateLimiter;
    }

    public <K> RateLimiterMap<K> buildMap(@NonNull Map<K, RateLimiter> map) {
        return new RateLimiterMap<>(this, map);
    }

    public <K> RateLimiterMap<K> buildMap() {
        return buildMap(
                enforceThreadSafety
                        ? new ConcurrentHashMap<>()
                        : new HashMap<>()
        );
    }

}
