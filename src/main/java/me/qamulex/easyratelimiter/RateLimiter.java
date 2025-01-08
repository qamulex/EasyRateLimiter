/*
 * Created on Jan 06, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public interface RateLimiter {

    /**
     * Returns the estimated time in milliseconds until the next request is allowed.
     * 
     * @return the remaining time in milliseconds, or 0 if a request is currently allowed.
     */
    long getTimeUntilNextRequest();

    /**
     * Checks if a request is allowed without blocking.
     * 
     * @return true if the request is allowed, false otherwise.
     */
    boolean isRequestAllowed();

    /**
     * Attempts to perform a request without blocking.
     * Captures the request timestamp if allowed.
     * 
     * @return true if the request was successful, false otherwise.
     */
    boolean tryRequest();

    /**
     * Blocks until a request is allowed and performs the request.
     * Captures the request timestamp when the request is successful.
     * 
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    void blockUntilRequestAllowed() throws InterruptedException;

    /**
     * Blocks for a specified maximum time until a request is allowed.
     * Captures the request timestamp if the request is successful.
     * 
     * <p>If the timeout is less than the actual time required for the next request 
     * to be allowed, the method will not block and will return {@code false} immediately.</p>
     * 
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the timeout argument
     * @return true if the request was allowed within the timeout, false otherwise.
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    boolean blockUntilRequestAllowed(long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * Blocks for a specified maximum time until a request is allowed.
     * Captures the request timestamp if the request is successful.
     * 
     * <p>If the timeout is less than the actual time required for the next request 
     * to be allowed, the method will not block and will return {@code false} immediately.</p>
     * 
     * @param duration the maximum duration to wait
     * @return true if the request was allowed within the specified duration, false otherwise.
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    boolean blockUntilRequestAllowed(Duration duration) throws InterruptedException;

    /**
     * Resets the limiter state.
     */
    void reset();

}
