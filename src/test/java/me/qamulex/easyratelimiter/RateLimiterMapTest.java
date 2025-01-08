/*
 * Created on Jan 08, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import me.qamulex.easyratelimiter.impl.FixedDelayRateLimiter;
import me.qamulex.easyratelimiter.wrapper.RateLimiterMap;

class RateLimiterMapTest {

    @Test
    void testGet() {
        RateLimiterMap<Integer> rlMap = RateLimiterBuilder.newBuilder()
                .withDelay(100)
                .buildMap();
        
        assertFalse(rlMap.containsKey(1));
        assertNotNull(rlMap.get(1));
        assertTrue(rlMap.get(1) instanceof FixedDelayRateLimiter);
    }

}
