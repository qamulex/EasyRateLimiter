/*
 * Created on Jan 08, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter.util;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;

public interface ExecutionTimeMeasurer {

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
