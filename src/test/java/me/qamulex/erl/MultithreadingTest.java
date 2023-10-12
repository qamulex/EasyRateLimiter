package me.qamulex.erl;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import lombok.AllArgsConstructor;

public class MultithreadingTest {

    private static final int  MAXIMUM_BANDWIDTH      = 5;
    private static final long AFFECTED_TIME_RANGE    = 500;
    private static final long DELAY_BETWEEN_REQUESTS = 50;

    private static final int N_THREADS = 4;
    private static final int REQUESTS_PER_THREAD = 8;

    RateLimiter delayLimiter = RateLimiterBuilder.newBuilder()
            .setMaximumBandwidth(1)
            .setAffectedTimeRange(DELAY_BETWEEN_REQUESTS)
            .setDelayBetweenRequests(0)
            .build();

    RateLimiter bandwidthLimiter = RateLimiterBuilder.newBuilder()
            .setMaximumBandwidth(MAXIMUM_BANDWIDTH)
            .setAffectedTimeRange(AFFECTED_TIME_RANGE)
            .setDelayBetweenRequests(0)
            .build();

    RateLimiter bandwidthLimiterWithDelay = RateLimiterBuilder.newBuilder()
            .setMaximumBandwidth(MAXIMUM_BANDWIDTH)
            .setAffectedTimeRange(AFFECTED_TIME_RANGE)
            .setDelayBetweenRequests(DELAY_BETWEEN_REQUESTS)
            .build();

    @Test
    public void delayLimiterMultithreadingTest() throws InterruptedException {
        Results results = run(delayLimiter);

        assertEquals(N_THREADS * REQUESTS_PER_THREAD, results.counter);

        assertEquals(
                DELAY_BETWEEN_REQUESTS,
                getDifference(Long.MAX_VALUE, Math::min, results.timestamps, 1),
                10
        );
    }

    @Test
    public void bandwidthLimiterMultithreadingTest() throws InterruptedException {
        Results results = run(bandwidthLimiter);

        assertEquals(N_THREADS * REQUESTS_PER_THREAD, results.counter);

        assertEquals(
                AFFECTED_TIME_RANGE,
                getDifference(0, Math::max, results.timestamps, MAXIMUM_BANDWIDTH),
                10
        );
    }

    @Test
    public void bandwidthLimiterWithDelayMultithreadingTest() throws InterruptedException {
        Results results = run(bandwidthLimiterWithDelay);

        assertEquals(N_THREADS * REQUESTS_PER_THREAD, results.counter);

        assertEquals(
                AFFECTED_TIME_RANGE,
                getDifference(0, Math::max, results.timestamps, MAXIMUM_BANDWIDTH),
                10
        );

        assertEquals(
                DELAY_BETWEEN_REQUESTS,
                getDifference(Long.MAX_VALUE, Math::min, results.timestamps, 1),
                10
        );
    }

    //

    @AllArgsConstructor
    public class Results {

        final int        counter;
        final List<Long> timestamps;
        final long       time;

    }

    private Results run(RateLimiter limiter) throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        List<Long> timestamps = new CopyOnWriteArrayList<>();

        List<Thread> threads = new LinkedList<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < N_THREADS; i++) {
            Thread thread = new Thread(() -> {
                int requests = REQUESTS_PER_THREAD;
                while (requests > 0) {
                    if (limiter.request()) {
                        counter.incrementAndGet();
                        requests--;
                        timestamps.add(System.currentTimeMillis() - startTime);
                    }
                }
            });
            threads.add(thread);
            thread.run();
        }

        for (Thread thread : threads)
            thread.join();

        long endTime = System.currentTimeMillis();

        return new Results(
                counter.get(),
                timestamps,
                endTime - startTime
        );
    }

    private long getDifference(long initialDifference, BiFunction<Long, Long, Long> comparator, List<Long> timestamps, int offset) {
        long difference = initialDifference;

        for (int i = 0; i + offset < timestamps.size(); i++) {
            long a = timestamps.get(i);
            long b = timestamps.get(i + offset);
            difference = comparator.apply(difference, b - a);
        }

        return difference;
    }

}
