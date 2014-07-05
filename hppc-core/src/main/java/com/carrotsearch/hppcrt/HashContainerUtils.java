package com.carrotsearch.hppcrt;

import java.util.concurrent.Callable;

import com.carrotsearch.hppcrt.hash.MurmurHash3;

public final class HashContainerUtils
{
    /**
     * Maximum capacity for an array that is of power-of-two size and still
     * allocable in Java (not a negative int).
     */
    public final static int MAX_CAPACITY = 0x80000000 >>> 1;

    /**
     * Minimum capacity for a hash container.
     */
    public final static int MIN_CAPACITY = 4;

    /**
     * Default capacity for a hash container.
     */
    public final static int DEFAULT_CAPACITY = 16;

    /**
     * Default load factor.
     */
    public final static float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * Computer static perturbations table.
     */
    public final static int[] PERTURBATIONS = new Callable<int[]>() {
        @Override
        public int[] call() {
            final int[] result = new int[32];
            for (int i = 0; i < result.length; i++) {
                result[i] = MurmurHash3.hash(17 + i);
            }
            return result;
        }
    }.call();

    /**
     * Round the capacity to the next allowed value.
     */
    public static int roundCapacity(final int requestedCapacity)
    {
        if (requestedCapacity > HashContainerUtils.MAX_CAPACITY)
            return HashContainerUtils.MAX_CAPACITY;

        return Math.max(HashContainerUtils.MIN_CAPACITY, BitUtil.nextHighestPowerOfTwo(requestedCapacity));
    }

    /**
     * Return the next possible capacity, counting from the current buffers'
     * size.
     */
    public static int nextCapacity(int current)
    {
        assert current > 0 && Long.bitCount(current) == 1 : "Capacity must be a power of two.";

        if (current < HashContainerUtils.MIN_CAPACITY / 2)
        {
            current = HashContainerUtils.MIN_CAPACITY / 2;
        }

        current <<= 1;
        if (current < 0)
        {
            throw new RuntimeException("Maximum capacity exceeded.");
        }

        return current;
    }
}
