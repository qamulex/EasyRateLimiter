package me.qamulex.erl;

import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter(AccessLevel.PRIVATE)
@Accessors(fluent = true, chain = true)
public class RateLimiterBuilder {

    public static RateLimiterBuilder newBuilder() {
        return new RateLimiterBuilder();
    }

    private Clock                clock                             = Clock.systemUTC();
    private int                  maximumBandwidth                  = 5;
    private long                 affectedTimeRangeInMillis         = 1000;
    private long                 delayBetweenRequestsInMillis      = 0;
    private Supplier<List<Long>> capturedTimestampsStorageSupplier = LinkedList::new;

    /**
     * @param clock - time source used for the limiter calculations
     * @return {@link RateLimiterBuilder}
     */
    public RateLimiterBuilder useClock(Clock clock) {
        return clock(clock);
    }

    /**
     * @param maximumBandwidth - maximum number of requests within a time range
     * @return {@link RateLimiterBuilder}
     */
    public RateLimiterBuilder setMaximumBandwidth(int maximumBandwidth) {
        return maximumBandwidth(Math.max(1, maximumBandwidth));
    }

    /**
     * @param timeInMillis - affected time range in millis
     * @return {@link RateLimiterBuilder}
     */
    public RateLimiterBuilder setAffectedTimeRange(long timeInMillis) {
        return affectedTimeRangeInMillis(Math.max(1L, timeInMillis));
    }

    /**
     * @param time - time range
     * @param unit - time unit
     * @return {@link RateLimiterBuilder}
     */
    public RateLimiterBuilder setAffectedTimeRange(long time, TimeUnit unit) {
        return setAffectedTimeRange(unit.toMillis(time));
    }

    /**
     * @param duration - affected time range
     * @return {@link RateLimiterBuilder}
     */
    public RateLimiterBuilder setAffectedTimeRange(Duration duration) {
        return setAffectedTimeRange(duration.toMillis());
    }

    /**
     * @param timeInMillis - delay between requests in millis (0 - disabled)
     * @return {@link RateLimiterBuilder}
     */
    public RateLimiterBuilder setDelayBetweenRequests(long timeInMillis) {
        return delayBetweenRequestsInMillis(Math.max(0, timeInMillis));
    }

    /**
     * @param time - delay between requests (0 - disabled)
     * @param unit - time unit
     * @return {@link RateLimiterBuilder}
     */
    public RateLimiterBuilder setDelayBetweenRequests(long time, TimeUnit unit) {
        return setDelayBetweenRequests(unit.toMillis(time));
    }

    /**
     * @param duration - delay between requests
     * @return {@link RateLimiterBuilder}
     */
    public RateLimiterBuilder setDelayBetweenRequests(Duration duration) {
        return delayBetweenRequestsInMillis(duration.toMillis());
    }

    /**
     * @param capturedTimestampsStorageSupplier - supplier of the list used to capture request timestamps
     * @return {@link RateLimiterBuilder}
     */
    public RateLimiterBuilder useCapturedTimestampsStorage(Supplier<List<Long>> capturedTimestampsStorageSupplier) {
        return capturedTimestampsStorageSupplier(capturedTimestampsStorageSupplier);
    }

    public RateLimiter build() {
        if (maximumBandwidth == 1 && delayBetweenRequestsInMillis == 0)
            return new DelayLimiter(
                    clock,
                    affectedTimeRangeInMillis
            );

        if (delayBetweenRequestsInMillis == 0)
            return new BandwidthLimiter(
                    clock,
                    affectedTimeRangeInMillis,
                    maximumBandwidth,
                    capturedTimestampsStorageSupplier.get()
            );

        return new BandwidthLimiterWithDelay(
                clock,
                affectedTimeRangeInMillis,
                maximumBandwidth,
                capturedTimestampsStorageSupplier.get(),
                delayBetweenRequestsInMillis
        );
    }

    public <K> RateLimiterMap<K> buildMap(Map<K, RateLimiter> mapInstance) {
        return new RateLimiterMap<>(this, mapInstance);
    }

    public <K> RateLimiterMap<K> buildMap() {
        return buildMap(new HashMap<>());
    }

}
