/*
 * Created on Jan 08, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter;


import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import me.qamulex.easyratelimiter.util.CircularBuffer;

class CircularBufferTest {

    private static final int CAPACITY = 5;

    private CircularBuffer<Integer> buffer;

    @BeforeEach
    void setUp() {
        buffer = new CircularBuffer<>(CAPACITY);
    }

    @Test
    void testInitialState() {
        assertEquals(CAPACITY, buffer.capacity());
        assertEquals(0, buffer.size());
        assertTrue(buffer.isEmpty());
        assertFalse(buffer.isFull());
    }

    @Test
    void testAddElements() {
        buffer.add(1);
        buffer.add(2);
        buffer.add(3);

        assertEquals(3, buffer.size());
        assertFalse(buffer.isEmpty());
        assertFalse(buffer.isFull());
    }

    @Test
    void testBufferFull() {
        for (int i = 1; i <= CAPACITY; i++)
            buffer.add(i);

        assertEquals(CAPACITY, buffer.size());
        assertTrue(buffer.isFull());
    }

    @Test
    void testBufferOverwrite() {
        for (int i = 1; i <= CAPACITY * 2; i++)
            buffer.add(i);

        assertEquals(CAPACITY, buffer.size());
        assertTrue(buffer.isFull());
        assertEquals(CAPACITY + 1, buffer.getFirst());
        assertEquals(CAPACITY * 2, buffer.getLast());
    }

    @Test
    void testGetByIndex() {
        buffer.add(10);
        buffer.add(20);
        buffer.add(30);

        assertEquals(10, buffer.get(0));
        assertEquals(20, buffer.get(1));
        assertEquals(30, buffer.get(2));
    }

    @Test
    void testGetInvalidIndex() {
        buffer.add(1);
        buffer.add(2);

        assertThrows(IndexOutOfBoundsException.class, () -> buffer.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.get(2));
    }

    @Test
    void testGetFirstAndLast() {
        buffer.add(10);
        buffer.add(20);
        buffer.add(30);

        assertEquals(10, buffer.getFirst());
        assertEquals(30, buffer.getLast());
    }

    @Test
    void testClear() {
        buffer.add(1);
        buffer.add(2);
        buffer.clear();

        assertEquals(0, buffer.size());
        assertTrue(buffer.isEmpty());
        assertFalse(buffer.isFull());
    }

    @Test
    void testIterator() {
        buffer.add(5);
        buffer.add(10);
        buffer.add(15);

        Iterator<Integer> it = buffer.iterator();
        assertTrue(it.hasNext());
        assertEquals(5, it.next());
        assertTrue(it.hasNext());
        assertEquals(10, it.next());
        assertTrue(it.hasNext());
        assertEquals(15, it.next());
        assertFalse(it.hasNext());

        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void testToString() {
        assertEquals("[]", buffer.toString());

        buffer.add(1);
        assertEquals("[1]", buffer.toString());

        buffer.add(2);
        buffer.add(3);
        assertEquals("[1, 2, 3]", buffer.toString());

        buffer.add(4);
        buffer.add(5);
        assertEquals("[1, 2, 3, 4, 5]", buffer.toString());

        buffer.add(6);
        assertEquals("[2, 3, 4, 5, 6]", buffer.toString());
    }

    @Test
    void testConstructorExceptions() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CircularBuffer<>(new Object[0])
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new CircularBuffer<>(0)
        );
    }

}
