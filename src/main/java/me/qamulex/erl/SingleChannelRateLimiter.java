/*
 * Created on Fri Sep 22 2023
 *
 * Copyright (c) qamulex
 */

package me.qamulex.erl;

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

    private final int  maximumBandwidth;
    private final long timeRangeInMillis;
    private final long delayBetweenRequestsInMillis;

    @Getter(AccessLevel.PRIVATE)
    private final List<Long> accumulatedRequests;
    @Getter(AccessLevel.PRIVATE)
    private long             lastAccumulatedRequestTimeInMillis = 0L;

    /**
     * Clears accumulated requests outside the time range
     */
    public void evict() {
        long leftTimeRangeBorderInMillis = System.currentTimeMillis() - timeRangeInMillis;
        accumulatedRequests.removeIf(
                accumulatedRequestTimeInMillis -> accumulatedRequestTimeInMillis < leftTimeRangeBorderInMillis
        );
    }

    /**
     * Clears all accumulated requests
     */
    public void reset() {
        accumulatedRequests.clear();
        lastAccumulatedRequestTimeInMillis = 0L;
    }

    /**
     * @return available number of requests within the time range
     */
    public int availableRequests() {
        evict();

        return maximumBandwidth - accumulatedRequests.size();
    }

    /**
     * Checks whether another request is possible
     * 
     * @see #evict()
     * 
     * @return <b>true</b> if the request is possible
     */
    public boolean canRequest() {
        evict();

        if (accumulatedRequests.isEmpty())
            return true;

        if (
            delayBetweenRequestsInMillis != 0L
                    && lastAccumulatedRequestTimeInMillis != 0L
                    && System.currentTimeMillis() - lastAccumulatedRequestTimeInMillis < delayBetweenRequestsInMillis
        )
            return false;

        return accumulatedRequests.size() + 1 <= maximumBandwidth;
    }

    /**
     * Checks whether the request is possible and accumulates request timestamp
     * 
     * @see #canRequest()
     * @see #evict()
     * 
     * @return <b>true</b> if the request was accumulated
     */
    public boolean request() {
        if (!canRequest())
            return false;
        accumulatedRequests.add(lastAccumulatedRequestTimeInMillis = System.currentTimeMillis());
        return true;
    }

    /**
     * @return {@link Builder SingeChannelRateLimiter.Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builds SingeChannelRateLimiter with {@link #maximumBandwidth}=<b>5</b>, {@link #timeRangeInMillis}=<b>1000</b> and
     * {@link #accumulatedRequests requestAccumulator}=<b>LinkedList</b>
     * 
     * @return {@link SingleChannelRateLimiter SingleChannelRateLimiter with default settings}
     */
    public static SingleChannelRateLimiter buildDefault() {
        return builder().build();
    }

    @Setter(AccessLevel.PRIVATE)
    @Accessors(fluent = true, chain = true)
    public static class Builder {

        private int                  maximumBandwidth             = 5;
        private long                 timeRangeInMillis            = 1000;
        private long                 delayBetweenRequestsInMillis = 0;
        private Supplier<List<Long>> requestAccumulatorSupplier   = LinkedList::new;

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
         * @param requestAccumulatorSupplier - supplier of the list used to accumulate requests
         * @return {@link Builder}
         */
        public Builder useRequestAccumulator(Supplier<List<Long>> requestAccumulatorSupplier) {
            return requestAccumulatorSupplier(requestAccumulatorSupplier);
        }

        /**
         * @return {@link SingleChannelRateLimiter}
         */
        public SingleChannelRateLimiter build() {
            return new SingleChannelRateLimiter(
                    maximumBandwidth,
                    timeRangeInMillis,
                    delayBetweenRequestsInMillis,
                    requestAccumulatorSupplier.get()
            );
        }

    }

}
