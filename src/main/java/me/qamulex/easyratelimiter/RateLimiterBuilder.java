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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.qamulex.easyratelimiter.impl.ClockDependentRateLimiter;
import me.qamulex.easyratelimiter.impl.FixedDelayRateLimiter;
import me.qamulex.easyratelimiter.impl.FixedDelayWindowBasedRateLimiter;
import me.qamulex.easyratelimiter.impl.FixedWindowRateLimiter;
import me.qamulex.easyratelimiter.impl.SlidingWindowRateLimiter;
import me.qamulex.easyratelimiter.impl.UselessRateLimiter;
import me.qamulex.easyratelimiter.impl.WindowBasedRateLimiter;
import me.qamulex.easyratelimiter.util.NanoTimeClock;
import me.qamulex.easyratelimiter.util.WindowType;
import me.qamulex.easyratelimiter.wrapper.RateLimiterMap;
import me.qamulex.easyratelimiter.wrapper.RateLimiterThreadSafetyEnforcementWrapper;
import me.qamulex.easyratelimiter.wrapper.RateLimitingExecutorService;

@Setter(AccessLevel.PRIVATE)
@Accessors(chain = true)
public final class RateLimiterBuilder {

    private RateLimiterBuilder() {}

    /**
     * Creates a new instance of RateLimiterBuilder.
     * 
     * <p>
     * By default:
     * <ul>
     * <li>clock is {@code null} (see {@link RateLimiterBuilder#withClock}).</li>
     * <li>delay is {@code 0}.</li>
     * <li>windowSize is {@code 0}.</li>
     * <li>windowType is {@code WindowType.SLIDING}.</li>
     * <li>maxQuota is {@code 1}.</li>
     * <li>enforceThreadSafety is {@code false}.</li>
     * </ul>
     * 
     * @return a new RateLimiterBuilder instance.
     */
    public static RateLimiterBuilder newBuilder() {
        return new RateLimiterBuilder();
    }

    private Clock      clock               = null;
    private long       delayMillis         = 0;
    private long       windowSizeMillis    = 0;
    private WindowType windowType          = WindowType.SLIDING;
    private int        maxQuota            = 1;
    private boolean    enforceThreadSafety = false;

    /**
     * Sets the clock to be used by the RateLimiter.
     * 
     * <p>
     * Default: {@code null}.
     * 
     * <p>
     * If {@code clock} is {@code null}, any instance of {@link ClockDependentRateLimiter} created by this builder will use
     * {@link NanoTimeClock#INSTANCE} as the default clock.
     * 
     * @param clock the clock to be used.
     * @return the current RateLimiterBuilder instance for method chaining.
     */
    public RateLimiterBuilder withClock(Clock clock) {
        return setClock(clock);
    }

    /**
     * Sets the delay between requests in milliseconds.
     * 
     * <p>
     * Default: {@code 0}.
     * 
     * @param delayMillis the delay in milliseconds. Must be greater than or equal to zero.
     * @return the current RateLimiterBuilder instance for method chaining.
     * @throws IllegalArgumentException if delayMillis is negative.
     */
    public RateLimiterBuilder withDelay(long delayMillis) {
        if (delayMillis < 0)
            throw new IllegalArgumentException("delay must be greater or equal to zero");

        return setDelayMillis(delayMillis);
    }

    /**
     * Sets the delay between requests using a specified time unit.
     * 
     * <p>
     * Default: {@code 0}.
     * 
     * @param delay the delay value.
     * @param unit  the time unit for the delay. Must not be null.
     * @return the current RateLimiterBuilder instance for method chaining.
     * @throws IllegalArgumentException if delay is negative.
     * @throws NullPointerException     if unit is null.
     */
    public RateLimiterBuilder withDelay(long delay, @NonNull TimeUnit unit) {
        return withDelay(unit.toMillis(delay));
    }

    /**
     * Sets the delay between requests using a Duration.
     * 
     * <p>
     * Default: {@code 0}.
     * 
     * @param delay the delay duration. Must not be null.
     * @return the current RateLimiterBuilder instance for method chaining.
     * @throws IllegalArgumentException if delay is negative.
     * @throws NullPointerException     if delay is null.
     */
    public RateLimiterBuilder withDelay(@NonNull Duration delay) {
        return withDelay(delay.toMillis());
    }

    /**
     * Sets the size of the time window in milliseconds.
     * 
     * <p>
     * Default: {@code 0}.
     * 
     * @param windowSizeMillis the window size in milliseconds. Must be greater than zero.
     * @return the current RateLimiterBuilder instance for method chaining.
     * @throws IllegalArgumentException if windowSizeMillis is zero or negative.
     */
    public RateLimiterBuilder withWindowSize(long windowSizeMillis) {
        if (windowSizeMillis <= 0)
            throw new IllegalArgumentException("windowSize must be greater than zero");

        return setWindowSizeMillis(windowSizeMillis);
    }

    /**
     * Sets the size of the time window using a specified time unit.
     * 
     * <p>
     * Default: {@code 0}.
     * 
     * @param windowSize the window size value.
     * @param unit       the time unit for the window size. Must not be null.
     * @return the current RateLimiterBuilder instance for method chaining.
     * @throws IllegalArgumentException if windowSize is zero or negative.
     * @throws NullPointerException     if unit is null.
     */
    public RateLimiterBuilder withWindowSize(long windowSize, @NonNull TimeUnit unit) {
        return withWindowSize(unit.toMillis(windowSize));
    }

    /**
     * Sets the size of the time window using a Duration.
     * 
     * <p>
     * Default: {@code 0}.
     * 
     * @param windowSize the window size duration. Must not be null.
     * @return the current RateLimiterBuilder instance for method chaining.
     * @throws IllegalArgumentException if windowSize is zero or negative.
     * @throws NullPointerException     if windowSize is null.
     */
    public RateLimiterBuilder withWindowSize(@NonNull Duration windowSize) {
        return withWindowSize(windowSize.toMillis());
    }

    /**
     * Configures the RateLimiter to use a fixed time window.
     * 
     * @return the current RateLimiterBuilder instance for method chaining.
     */
    public RateLimiterBuilder useFixedWindow() {
        return setWindowType(WindowType.FIXED);
    }

    /**
     * Configures the RateLimiter to use a sliding time window.
     * 
     * @return the current RateLimiterBuilder instance for method chaining.
     */
    public RateLimiterBuilder useSlidingWindow() {
        return setWindowType(WindowType.SLIDING);
    }

    /**
     * Sets the maximum number of requests allowed within a time window.
     * 
     * <p>
     * Default: {@code 1}.
     * 
     * @param maxQuota the maximum quota. Must be greater than zero.
     * @return the current RateLimiterBuilder instance for method chaining.
     * @throws IllegalArgumentException if maxQuota is less than one.
     */
    public RateLimiterBuilder withMaxQuota(int maxQuota) {
        if (maxQuota < 1)
            throw new IllegalArgumentException("maxQuota must be greater than zero");

        return setMaxQuota(maxQuota);
    }

    /**
     * Enables or disables thread-safety enforcement for the RateLimiter.
     * 
     * <p>
     * Default: {@code false}.
     * 
     * @param enforceThreadSafety true to enable thread safety, false to disable it.
     * @return the current RateLimiterBuilder instance for method chaining.
     */
    public RateLimiterBuilder enforceThreadSafety(boolean enforceThreadSafety) {
        return setEnforceThreadSafety(enforceThreadSafety);
    }

    /**
     * Builds a RateLimiter instance based on the current configuration.
     * 
     * @return the configured RateLimiter instance.
     * @throws IllegalStateException if neither delay nor windowSize is greater than zero.
     */
    public RateLimiter build() {
        if (delayMillis == 0 && windowSizeMillis == 0)
            throw new IllegalStateException("delay or windowSize must be greater than zero");

        FixedDelayRateLimiter fixedDelayRateLimiter = tryCreateFixedDelayRateLimiter();
        WindowBasedRateLimiter windowBasedRateLimiter = tryCreateWindowBasedRateLimiter();

        RateLimiter rateLimiter = tryCombineRateLimiters(fixedDelayRateLimiter, windowBasedRateLimiter);
        if (rateLimiter == null)
            return UselessRateLimiter.INSTANCE;

        if (rateLimiter instanceof ClockDependentRateLimiter && clock != null)
            ((ClockDependentRateLimiter) rateLimiter).setClock(clock);

        if (enforceThreadSafety)
            rateLimiter = new RateLimiterThreadSafetyEnforcementWrapper(rateLimiter);

        return rateLimiter;
    }

    private FixedDelayRateLimiter tryCreateFixedDelayRateLimiter() {
        if (windowSizeMillis > 0 && maxQuota == 1)
            return new FixedDelayRateLimiter(windowSizeMillis);
        else if (delayMillis > 0)
            return new FixedDelayRateLimiter(delayMillis);

        return null;
    }

    private WindowBasedRateLimiter tryCreateWindowBasedRateLimiter() {
        if (windowSizeMillis > 0 && maxQuota > 1) {
            if (windowType == WindowType.SLIDING)
                return new SlidingWindowRateLimiter(maxQuota, windowSizeMillis);
            else if (windowType == WindowType.FIXED)
                return new FixedWindowRateLimiter(maxQuota, windowSizeMillis);
        }

        return null;
    }

    private RateLimiter tryCombineRateLimiters(
            FixedDelayRateLimiter fixedDelayRateLimiter,
            WindowBasedRateLimiter windowBasedRateLimiter
    ) {
        if (fixedDelayRateLimiter != null && windowBasedRateLimiter != null)
            return new FixedDelayWindowBasedRateLimiter(fixedDelayRateLimiter, windowBasedRateLimiter);
        else if (fixedDelayRateLimiter != null)
            return fixedDelayRateLimiter;
        else if (windowBasedRateLimiter != null)
            return windowBasedRateLimiter;

        return null;
    }

    /**
     * Builds a RateLimiterMap using the provided map.
     * 
     * @param <K> the type of keys maintained by the map.
     * @param map the map to use for RateLimiter instances. Must not be null.
     * @return a RateLimiterMap instance.
     * @throws NullPointerException if map is null.
     */
    public <K> RateLimiterMap<K> buildMap(@NonNull Map<K, RateLimiter> map) {
        return new RateLimiterMap<>(this, map);
    }

    /**
     * Builds a RateLimiterMap with a default internal map implementation.
     * 
     * @param <K> the type of keys maintained by the map.
     * @return a RateLimiterMap instance.
     */
    public <K> RateLimiterMap<K> buildMap() {
        return buildMap(
                enforceThreadSafety
                        ? new ConcurrentHashMap<>()
                        : new HashMap<>()
        );
    }

    /**
     * Builds a RateLimitingExecutorService using the provided ExecutorService.
     * 
     * @param executorService the ExecutorService to use. Must not be null.
     * @return a RateLimitingExecutorService instance.
     * @throws NullPointerException if executorService is null.
     */
    public RateLimitingExecutorService buildExecutorService(@NonNull ExecutorService executorService) {
        return new RateLimitingExecutorService(executorService, build());
    }

    /**
     * Builds a RateLimitingExecutorService with a default single-threaded executor.
     * 
     * @return a RateLimitingExecutorService instance.
     */
    public RateLimitingExecutorService buildExecutorService() {
        return buildExecutorService(Executors.newSingleThreadExecutor());
    }

}
