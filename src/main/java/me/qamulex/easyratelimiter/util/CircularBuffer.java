/*
 * Created on Jan 08, 2025
 *
 * Copyright (c) qamulex
 */
package me.qamulex.easyratelimiter.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A circular buffer implementation with a fixed capacity.
 * 
 * <p>
 * Maintains a FIFO (First In, First Out) order, automatically overwriting
 * the oldest elements when the buffer is full.
 * </p>
 * 
 * <p>
 * Supports random access, iteration, and clearing operations.
 * </p>
 * 
 * @param <T> the type of elements stored in the buffer.
 */
public class CircularBuffer<T> implements Iterable<T> {

    private final T[] buffer;

    private int headIndex = 0;
    private int tailIndex = 0;
    private int size      = 0;

    public CircularBuffer(T[] buffer) {
        if (buffer.length <= 0)
            throw new IllegalArgumentException("buffer length must be greater than 0");

        this.buffer = buffer;
    }

    @SuppressWarnings("unchecked")
    public CircularBuffer(int capacity) {
        if (capacity <= 0)
            throw new IllegalArgumentException("capacity must be greater than 0");

        this.buffer = (T[]) new Object[capacity];
    }

    public int capacity() {
        return buffer.length;
    }

    public int size() {
        return size;
    }

    public boolean isFull() {
        return size == buffer.length;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void add(T item) {
        buffer[tailIndex] = item;
        tailIndex = (tailIndex + 1) % buffer.length;
        if (isFull())
            headIndex = (headIndex + 1) % buffer.length;
        else
            size++;
    }

    public T get(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException(String.format("Index: %d, Size: %d", index, size));

        return buffer[(headIndex + index) % buffer.length];
    }

    public T getFirst() {
        return get(0);
    }

    public T getLast() {
        return get(size - 1);
    }

    public void clear() {
        headIndex = 0;
        tailIndex = 0;
        size = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < size;
            }

            @Override
            public T next() {
                if (!hasNext())
                    throw new NoSuchElementException();

                return get(index++);
            }

        };
    }

    @Override
    public String toString() {
        Iterator<T> it = iterator();
        if (!it.hasNext())
            return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        while (true) {
            sb.append(it.next());
            if (!it.hasNext())
                return sb.append(']').toString();
            sb.append(',').append(' ');
        }
    }

}