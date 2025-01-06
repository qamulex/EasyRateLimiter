/*
 * Created on Jan 07, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RateLimitingExecutorService extends AbstractExecutorService {

    private final ExecutorService executorService;
    private final RateLimiter     rateLimiter;

    private final ExecutorService rateLimiterExecutor = Executors.newSingleThreadExecutor();

    public RateLimitingExecutorService(ExecutorService executorService, RateLimiter rateLimiter) {
        this.executorService = executorService;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        boolean result = rateLimiterExecutor.awaitTermination(timeout, unit);
        result &= executorService.awaitTermination(timeout, unit);
        return result;
    }

    @Override
    public boolean isShutdown() {
        return executorService.isShutdown() && rateLimiterExecutor.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return executorService.isTerminated() && rateLimiterExecutor.isTerminated();
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

    @Override
    public void execute(Runnable command) {
        rateLimiterExecutor.submit(() -> {
            try {
                rateLimiter.blockUntilRequestAllowed();
                executorService.submit(command);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(exception);
            }
        });
    }

}
