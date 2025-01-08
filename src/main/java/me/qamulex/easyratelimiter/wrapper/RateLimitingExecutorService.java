/*
 * Created on Jan 07, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter.wrapper;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import me.qamulex.easyratelimiter.RateLimiter;

public class RateLimitingExecutorService extends AbstractExecutorService {

    private final ExecutorService executorService;
    private final RateLimiter     rateLimiter;

    private final ExecutorService rateLimiterExecutor = Executors.newSingleThreadExecutor();

    public RateLimitingExecutorService(ExecutorService executorService, RateLimiter rateLimiter) {
        this.executorService = executorService;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void execute(Runnable command) {
        rateLimiterExecutor.execute(() -> {
            try {
                rateLimiter.blockUntilRequestAllowed();
                executorService.execute(command);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(exception);
            }
        });
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return rateLimiterExecutor.awaitTermination(timeout, unit) && executorService.awaitTermination(timeout, unit);
    }

    @Override
    public boolean isShutdown() {
        return rateLimiterExecutor.isShutdown() && executorService.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return rateLimiterExecutor.isTerminated() && executorService.isTerminated();
    }

    @Override
    public void shutdown() {
        rateLimiterExecutor.shutdown();
        executorService.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        rateLimiterExecutor.shutdownNow();
        return executorService.shutdownNow();
    }

}
