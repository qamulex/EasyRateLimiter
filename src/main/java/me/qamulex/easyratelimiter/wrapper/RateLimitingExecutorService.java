/*
 * Created on Jan 07, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter.wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.NonNull;
import me.qamulex.easyratelimiter.RateLimiter;

/**
 * An {@link ExecutorService} decorator that rate limits the execution of submitted tasks.
 *
 * <p>
 * Submitted tasks are queued internally and dispatched one at a time by a single
 * dispatcher thread, which waits for a rate limiter permit before handing each task
 * to the underlying executor service. Tasks waiting for a permit do not occupy
 * threads of the underlying executor.
 *
 * <p>
 * The task queue is unbounded: {@code execute} never blocks and never rejects
 * while the service is running.
 *
 * <p>
 * Shutdown semantics:
 * <ul>
 * <li>{@code shutdown()} lets all queued tasks complete (each still waiting for
 * its permit) and then shuts down the underlying executor service.</li>
 * <li>{@code shutdownNow()} drops all pending tasks without executing them and
 * returns them, together with the tasks pending in the underlying executor
 * service. A task already being dispatched is returned as well.</li>
 * </ul>
 */
public class RateLimitingExecutorService extends AbstractExecutorService {

    private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger(0);

    private final ExecutorService executorService;
    private final RateLimiter     rateLimiter;

    private final Thread dispatcher;

    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    private final Object                  lock  = new Object();

    private volatile boolean shutdown = false;
    private volatile boolean stopNow  = false;

    public RateLimitingExecutorService(@NonNull ExecutorService executorService, @NonNull RateLimiter rateLimiter) {
        this.executorService = executorService;
        this.rateLimiter = rateLimiter;

        this.dispatcher = new Thread(
                this::dispatchLoop,
                "easyratelimiter-dispatcher-" + INSTANCE_COUNTER.incrementAndGet()
        );
        this.dispatcher.start();
    }

    /**
     * Returns the rate limiter used to gate task execution.
     *
     * <p>
     * The returned instance is the one passed to the constructor; this service
     * only acquires permits from it and never mutates its configuration.
     *
     * @return the rate limiter backing this executor service
     */
    public RateLimiter getRateLimiter() {
        return rateLimiter;
    }

    @Override
    public void execute(Runnable command) {
        synchronized (lock) {
            if (shutdown)
                throw new RejectedExecutionException("RateLimitingExecutorService is shut down");

            queue.offer(command);
        }
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long deadlineNanos = System.nanoTime() + unit.toNanos(timeout);

        dispatcher.join(Math.max(1, unit.toMillis(timeout)));

        long remainingNanos = deadlineNanos - System.nanoTime();
        if (remainingNanos <= 0)
            return isTerminated();

        return !dispatcher.isAlive() && executorService.awaitTermination(remainingNanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public boolean isTerminated() {
        return shutdown && !dispatcher.isAlive() && executorService.isTerminated();
    }

    @Override
    public void shutdown() {
        synchronized (lock) {
            if (shutdown)
                return;

            shutdown = true;
        }

        dispatcher.interrupt();
    }

    @Override
    public List<Runnable> shutdownNow() {
        synchronized (lock) {
            shutdown = true;
            stopNow = true;
        }

        dispatcher.interrupt();

        try {
            dispatcher.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<Runnable> pendingTasks = new ArrayList<>(queue.size());
        queue.drainTo(pendingTasks);
        pendingTasks.addAll(executorService.shutdownNow());

        return pendingTasks;
    }

    private void dispatchLoop() {
        try {
            while (!stopNow) {
                Runnable command = queue.take();
                submitToExecutorService(command);
            }
        } catch (InterruptedException ignored) {
            // shutdown() or shutdownNow() requested, handled in the finally block
        } finally {
            if (!stopNow) {
                Runnable command;
                while (!stopNow && (command = queue.poll()) != null)
                    submitToExecutorService(command);

                executorService.shutdown();
            }
        }
    }

    private void submitToExecutorService(Runnable command) {
        boolean interruptedDuringWait = false;

        try {
            while (!stopNow) {
                try {
                    rateLimiter.blockUntilRequestAllowed();
                    break;
                } catch (InterruptedException e) {
                    // woke up to re-check the shutdown flags
                    interruptedDuringWait = true;
                }
            }

            if (!stopNow) {
                try {
                    executorService.execute(command);
                } catch (RejectedExecutionException ignored) {
                    // the underlying executor service was shut down externally, task is dropped
                }
            }
        } finally {
            if (stopNow) {
                // return the task to the queue so shutdownNow() can hand it back to the caller
                synchronized (lock) {
                    queue.offer(command);
                }
            }
        }

        // propagate the wake-up signal so dispatchLoop re-checks its termination condition
        if (interruptedDuringWait)
            Thread.currentThread().interrupt();
    }

}
