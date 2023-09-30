package me.qamulex.erl;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import lombok.AllArgsConstructor;

public class SingleChannelMultithreadingTest {

    private int nThreads          = 4;
    private int requestsPerThread = 8;
    private int maximumBandwidth  = 5;

    SingleChannelRateLimiter limiterWithoutDelay = SingleChannelRateLimiter
            .builder()
            .setMaximumBandwidth(maximumBandwidth)
            .setTimeRange(1, TimeUnit.SECONDS)
            // .useRequestAccumulator(CopyOnWriteArrayList::new)
            .build();

    @Test
    public void multithreadingWithoutDelay() throws InterruptedException {
        Results results = run(limiterWithoutDelay);

        assertEquals(nThreads * requestsPerThread, results.counter);

        assertEquals(
                limiterWithoutDelay.getTimeRangeInMillis(),
                getDifference(0, Math::max, results.timestamps, maximumBandwidth),
                10
        );
    }

    SingleChannelRateLimiter limiterWithDelay = SingleChannelRateLimiter
            .builder()
            .setMaximumBandwidth(maximumBandwidth)
            .setTimeRange(1, TimeUnit.SECONDS)
            .setDelayBetweenRequests(100)
            // .useRequestAccumulator(CopyOnWriteArrayList::new)
            .build();

    @Test
    public void multithreadingWithDelay() throws InterruptedException {
        Results results = run(limiterWithDelay);

        assertEquals(nThreads * requestsPerThread, results.counter);

        assertEquals(
                limiterWithDelay.getTimeRangeInMillis(),
                getDifference(0, Math::max, results.timestamps, maximumBandwidth),
                10
        );

        assertEquals(
                limiterWithDelay.getDelayBetweenRequestsInMillis(),
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

    private Results run(SingleChannelRateLimiter limiter) throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        List<Long> timestamps = new CopyOnWriteArrayList<>();

        List<Thread> threads = new LinkedList<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < nThreads; i++) {
            Thread thread = new Thread(() -> {
                int requests = requestsPerThread;
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
