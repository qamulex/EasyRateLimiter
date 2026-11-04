/*
 * Created on Nov 05, 2026
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import me.qamulex.easyratelimiter.util.ControllableClock;
import me.qamulex.easyratelimiter.wrapper.RateLimitingExecutorService;

class RateLimitingExecutorServiceLifecycleTest {

    static final long delayMillis = 50;
    static final int  taskCount   = 5;

    RateLimitingExecutorService executorService;

    RateLimitingExecutorService buildExecutorService() {
        return RateLimiterBuilder.newBuilder()
                .withDelay(delayMillis, TimeUnit.MILLISECONDS)
                .buildExecutorService();
    }

    @AfterEach
    void tearDown() {
        if (executorService != null)
            executorService.shutdownNow();
    }

    @Test
    void testShutdownDoesNotLoseTasks() throws InterruptedException {
        executorService = buildExecutorService();

        AtomicInteger executedCount = new AtomicInteger(0);
        for (int i = 0; i < taskCount; i++)
            executorService.submit(executedCount::incrementAndGet);

        executorService.shutdown();

        assertTrue(executorService.awaitTermination(5, TimeUnit.SECONDS));
        assertEquals(taskCount, executedCount.get());
    }

    @Test
    void testShutdownNowReturnsAllPendingTasks() throws InterruptedException {
        ControllableClock clock = new ControllableClock();
        clock.setMillis(System.currentTimeMillis());

        executorService = RateLimiterBuilder.newBuilder()
                .withClock(clock)
                .withDelay(60, TimeUnit.SECONDS) // permits never arrive within this test
                .buildExecutorService();

        AtomicInteger executedCount = new AtomicInteger(0);
        for (int i = 0; i < taskCount; i++)
            executorService.submit(executedCount::incrementAndGet);

        // the dispatcher executes task #1 instantly (the first permit is always granted)
        // and then waits forever for task #2's permit
        long deadlineNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (executedCount.get() < 1 && System.nanoTime() < deadlineNanos)
            Thread.sleep(1);
        assertEquals(1, executedCount.get());

        List<Runnable> pendingTasks = executorService.shutdownNow();

        assertEquals(taskCount - 1, pendingTasks.size());
        assertEquals(1, executedCount.get());
    }

    @Test
    void testAwaitTerminationRespectsTimeout() throws InterruptedException {
        ExecutorService innerExecutorService =
                Executors.newSingleThreadExecutor();

        CountDownLatch innerTaskStartedLatch = new CountDownLatch(1);
        CountDownLatch innerTaskBlockerLatch = new CountDownLatch(1);
        innerExecutorService.submit(() -> {
            innerTaskStartedLatch.countDown();
            try {
                innerTaskBlockerLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        assertTrue(innerTaskStartedLatch.await(1, TimeUnit.SECONDS));

        executorService = RateLimiterBuilder.newBuilder()
                .withDelay(200, TimeUnit.MILLISECONDS)
                .buildExecutorService(innerExecutorService);

        executorService.submit(() -> {});
        executorService.submit(() -> {}); // second task waits 200 ms for a permit

        executorService.shutdown();

        long timeoutMillis = 300;
        long startTime = System.nanoTime();
        executorService.awaitTermination(timeoutMillis, TimeUnit.MILLISECONDS);
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

        innerTaskBlockerLatch.countDown();

        assertTrue(
                elapsedMillis < 1.5 * timeoutMillis,
                () -> "awaitTermination blocked for " + elapsedMillis + " ms, exceeding the requested timeout of " + timeoutMillis + " ms"
        );

        assertTrue(executorService.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    void testSubmitAfterShutdownIsRejected() {
        executorService = buildExecutorService();
        executorService.shutdown();

        assertThrows(RejectedExecutionException.class, () -> executorService.submit(() -> {}));
    }

    @Test
    void testFutureCancellationSkipsTaskExecution() throws InterruptedException {
        executorService = buildExecutorService();

        AtomicInteger executedCount = new AtomicInteger(0);
        Future<?> firstTask = executorService.submit(executedCount::incrementAndGet);
        Future<?> cancelledTask = executorService.submit(executedCount::incrementAndGet);
        cancelledTask.cancel(false);

        executorService.shutdown();

        assertTrue(executorService.awaitTermination(5, TimeUnit.SECONDS));
        assertTrue(firstTask.isDone());
        assertTrue(cancelledTask.isCancelled());
        assertEquals(1, executedCount.get());
    }

    @Test
    void testStateTransitions() throws InterruptedException {
        executorService = buildExecutorService();

        assertFalse(executorService.isShutdown());
        assertFalse(executorService.isTerminated());

        executorService.submit(() -> {});
        executorService.submit(() -> {}); // keeps the dispatcher busy for at least one delay interval
        executorService.shutdown();

        assertTrue(executorService.isShutdown());
        assertFalse(executorService.isTerminated());

        assertTrue(executorService.awaitTermination(5, TimeUnit.SECONDS));

        assertTrue(executorService.isShutdown());
        assertTrue(executorService.isTerminated());
    }

    @Test
    void testAwaitTerminationReturnsFalseOnTimeout() throws InterruptedException {
        executorService = RateLimiterBuilder.newBuilder()
                .withDelay(10 * delayMillis, TimeUnit.MILLISECONDS)
                .buildExecutorService();

        AtomicInteger executedCount = new AtomicInteger(0);
        executorService.submit(executedCount::incrementAndGet);
        executorService.submit(executedCount::incrementAndGet); // waits 500 ms for a permit, keeping the dispatcher busy

        executorService.shutdown();

        assertFalse(executorService.awaitTermination(delayMillis, TimeUnit.MILLISECONDS));
        assertTrue(executorService.awaitTermination(5, TimeUnit.SECONDS));
        assertEquals(2, executedCount.get());
    }

    @Test
    void testShutdownNowOnIdleExecutor() throws InterruptedException {
        executorService = buildExecutorService();

        assertTrue(executorService.shutdownNow().isEmpty());
        assertTrue(executorService.awaitTermination(5, TimeUnit.SECONDS));
        assertTrue(executorService.isTerminated());
    }

    @Test
    void testShutdownIsIdempotent() throws InterruptedException {
        executorService = buildExecutorService();

        executorService.shutdown();
        executorService.shutdown(); // must not throw

        assertTrue(executorService.awaitTermination(5, TimeUnit.SECONDS));
        assertTrue(executorService.shutdownNow().isEmpty());
    }

}
