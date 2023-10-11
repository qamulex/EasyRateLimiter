/*
 * Created on Fri Sep 22 2023
 *
 * Copyright (c) qamulex
 */

package me.qamulex.erl;

import java.time.Clock;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class SingleChannelRateLimiter {

    private final Clock clock;
    private final int   maximumBandwidth;
    private final long  timeRangeInMillis;
    private final long  delayBetweenRequestsInMillis;

    @Getter(AccessLevel.PRIVATE)
    private final List<Long> capturedRequests;
    @Getter(AccessLevel.PRIVATE)
    private long             lastRequestTimeInMillis = 0L;
    private long             nextRequestTimeInMillis = 0L;

    /**
     * Clears all captured requests
     */
    public void reset() {
        capturedRequests.clear();
        lastRequestTimeInMillis = 0L;
        nextRequestTimeInMillis = 0L;
    }

    private void recalculateNextRequestTime() {
        long currentTimeMillis = clock.millis();

        // calculate if amount of requests is exceeded
        if (capturedRequests.size() >= maximumBandwidth) {
            long oldestRequestTimeInMillis = capturedRequests.get(0);
            nextRequestTimeInMillis = oldestRequestTimeInMillis + timeRangeInMillis;
            return;
        }

        // calculate if there is a delay and it is not sustained
        long lastRequestTimePlusDelayInMillis = lastRequestTimeInMillis + delayBetweenRequestsInMillis;
        if (
            delayBetweenRequestsInMillis != 0
                    && lastRequestTimeInMillis != 0L
                    && lastRequestTimePlusDelayInMillis > currentTimeMillis
        ) {
            nextRequestTimeInMillis = lastRequestTimePlusDelayInMillis;
            return;
        }

        // next request time is current time if there are not "obstacles"
        nextRequestTimeInMillis = currentTimeMillis;
    }

    private void refresh() {
        long currentTimeMillis = clock.millis();

        if (nextRequestTimeInMillis > currentTimeMillis)
            return;

        long leftTimeRangeBorderInMillis = currentTimeMillis - timeRangeInMillis;
        capturedRequests.removeIf(
                capturedRequestTimeInMillis -> capturedRequestTimeInMillis <= leftTimeRangeBorderInMillis
        );
    }

    /**
     * @return estimated time in millis after which another request is possible
     */
    public long remainingTimeInMillis() {
        return Math.max(0L, clock.millis() - nextRequestTimeInMillis);
    }

    /**
     * Checks whether another request is possible
     * 
     * @return <b>true</b> if the request is possible
     */
    public boolean canRequest() {
        return nextRequestTimeInMillis <= clock.millis();
    }

    /**
     * Checks whether the request is possible and captures request timestamp
     * 
     * @return <b>true</b> if the request was captured
     */
    public boolean request() {
        if (!canRequest())
            return false;

        refresh();
        capturedRequests.add(lastRequestTimeInMillis = clock.millis());
        recalculateNextRequestTime();

        return true;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static SingleChannelRateLimiter buildDefault() {
        return builder().build();
    }

    @Setter(AccessLevel.PRIVATE)
    @Accessors(fluent = true, chain = true)
    public static class Builder {

        private Clock                clock                        = Clock.systemUTC();
        private int                  maximumBandwidth             = 5;
        private long                 timeRangeInMillis            = 1000;
        private long                 delayBetweenRequestsInMillis = 0;
        private Supplier<List<Long>> requestStorageSupplier       = LinkedList::new;

        /**
         * @param clock - time source used for the limiter
         * @return {@link Builder}
         */
        public Builder useClock(Clock clock) {
            return clock(clock);
        }

        /**
         * @param maximumBandwidth - maximum number of requests within a time range
         * @return {@link Builder}
         */
        public Builder setMaximumBandwidth(int maximumBandwidth) {
            return maximumBandwidth(Math.max(1, maximumBandwidth));
        }

        /**
         * @param timeInMillis - time range in millis
         * @return {@link Builder}
         */
        public Builder setTimeRange(long timeInMillis) {
            return timeRangeInMillis(Math.max(1L, timeInMillis));
        }

        /**
         * @param time - time range
         * @param unit - time unit
         * @return {@link Builder}
         */
        public Builder setTimeRange(long time, TimeUnit unit) {
            return setTimeRange(unit.toMillis(time));
        }

        /**
         * @param timeInMillis - delay between requests in millis (0 - disabled)
         * @return {@link Builder}
         */
        public Builder setDelayBetweenRequests(long timeInMillis) {
            return delayBetweenRequestsInMillis(Math.max(1L, timeInMillis));
        }

        /**
         * @param time - delay between requests (0 - disabled)
         * @param unit - time unit
         * @return {@link Builder}
         */
        public Builder setDelayBetweenRequests(long time, TimeUnit unit) {
            return setDelayBetweenRequests(unit.toMillis(time));
        }

        /**
         * @param requestStorageSupplier - supplier of the list used to capture requests
         * @return {@link Builder}
         */
        public Builder useRequestStorage(Supplier<List<Long>> requestStorageSupplier) {
            return requestStorageSupplier(requestStorageSupplier);
        }

        /**
         * @return {@link SingleChannelRateLimiter}
         */
        public SingleChannelRateLimiter build() {
            return new SingleChannelRateLimiter(
                    clock,
                    maximumBandwidth,
                    timeRangeInMillis,
                    delayBetweenRequestsInMillis,
                    requestStorageSupplier.get()
            );
        }

    }

}
