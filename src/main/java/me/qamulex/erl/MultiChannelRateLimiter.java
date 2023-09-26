/*
 * Created on Fri Sep 22 2023
 *
 * Copyright (c) qamulex
 */

package me.qamulex.erl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MultiChannelRateLimiter<T> {

    private final Supplier<SingleChannelRateLimiter> singleChannelRateLimiterSupplier;

    @Getter
    private final int maximumBandwidth;

    private final Map<T, SingleChannelRateLimiter> channels;

    /**
     * @return amount of {@link #channels}
     */
    public int countChannels() {
        return channels.size();
    }

    /**
     * Calls {@link SingleChannelRateLimiter#evict()} method in all {@link #channels}
     */
    public void evict() {
        channels.values().forEach(SingleChannelRateLimiter::evict);
    }

    /**
     * Calls {@link SingleChannelRateLimiter#reset()} method in all channels and clears {@link #channels channels map}
     */
    public void reset() {
        channels.values().forEach(SingleChannelRateLimiter::reset);
        channels.clear();
    }

    /**
     * @param target
     * @return {@link SingleChannelRateLimiter#availableRequests()} of target channel or {@link #maximumBandwidth default maximum
     *         bandwidth} if channel is not present
     */
    public int availableRequestsOf(T target) {
        return channels.containsKey(target)
                ? channels.get(target).availableRequests()
                : maximumBandwidth;
    }

    /**
     * @param target
     * @return {@link SingleChannelRateLimiter#canRequest()} method result for target channel or <b>false</b> if channel is not
     *         present
     */
    public boolean canRequest(T target) {
        return !channels.containsKey(target) || channels.get(target).canRequest();
    }

    /**
     * @param target
     * @return {@link SingleChannelRateLimiter#request()} method result for target channel or <b>false</b> if channel is not present
     */
    public boolean request(T target) {
        return channels
                .computeIfAbsent(target, ignored -> singleChannelRateLimiterSupplier.get())
                .request();
    }

    /**
     * @param <T> type of channels keys
     * @return {@link Builder MultiChannelRateLimiter.Builder}
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * Builds MultiChannelRateLimiter with {@link SingleChannelRateLimiter#buildDefault()} rate limiter and <b>HashMap</b> as
     * {@link #channels channels map}
     * 
     * @param <T> type of channels keys
     * @return {@link MultiChannelRateLimiter MultiChannelRateLimiter with default settings}
     */
    public static <T> MultiChannelRateLimiter<T> buildDefault() {
        return MultiChannelRateLimiter.<T>builder().build();
    }

    @Setter(AccessLevel.PRIVATE)
    @Accessors(fluent = true, chain = true)
    public static class Builder<T> {

        private Supplier<SingleChannelRateLimiter>         singleChannelRateLimiterSupplier = SingleChannelRateLimiter::buildDefault;
        private Supplier<Map<T, SingleChannelRateLimiter>> channelsMapSupplier              = HashMap::new;

        /**
         * @param builderOperator - used to build {@link SingleChannelRateLimiter}
         * @return {@link Builder}
         */
        public Builder<T> useRateLimiter(
                UnaryOperator<SingleChannelRateLimiter.Builder> builderOperator
        ) {
            return singleChannelRateLimiterSupplier(
                    () -> builderOperator
                            .apply(SingleChannelRateLimiter.builder())
                            .build()
            );
        }

        /**
         * @param mapSupplier - supplier of the map used to store channels
         * @return {@link Builder}
         */
        public Builder<T> useChannelsMap(Supplier<Map<T, SingleChannelRateLimiter>> mapSupplier) {
            return channelsMapSupplier(mapSupplier);
        }

        /**
         * @return {@link MultiChannelRateLimiter}
         */
        public MultiChannelRateLimiter<T> build() {
            return new MultiChannelRateLimiter<>(
                    singleChannelRateLimiterSupplier,
                    singleChannelRateLimiterSupplier.get().getMaximumBandwidth(),
                    channelsMapSupplier.get()
            );
        }

    }

}
