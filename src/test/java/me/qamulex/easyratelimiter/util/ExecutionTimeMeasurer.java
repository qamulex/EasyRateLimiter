/*
 * Created on Jan 08, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter.util;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public interface ExecutionTimeMeasurer {

    default void assertExecutionTimeInRange(long minExpectedExecutionTime, long maxExpectedExecutionTime, Executable executable) {
        long startTime = System.nanoTime();
        try {
            executable.execute();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        long endTime = System.nanoTime();

        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        assertTrue(
                elapsedMillis >= minExpectedExecutionTime && elapsedMillis <= maxExpectedExecutionTime,
                () -> String.format(
                        "expected execution time in range [%d, %d] but was %d",
                        minExpectedExecutionTime, maxExpectedExecutionTime, elapsedMillis
                )
        );
    }

    default void assertExecutionTimeEquals(long expectedExecutionTime, double timeDelta, Executable executable) {
        long startTime = System.nanoTime();
        try {
            executable.execute();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        long endTime = System.nanoTime();

        long elapsedNanos = endTime - startTime;
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(elapsedNanos);

        assertEquals(expectedExecutionTime, elapsedMillis, timeDelta);
    }

    default void assertExecutionTimeEquals(long expectedExecutionTime, Executable executable) {
        assertExecutionTimeEquals(expectedExecutionTime, 20, executable);
    }

}
