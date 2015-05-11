package com.carrotsearch.hppcrt;

import java.util.concurrent.Callable;

import com.carrotsearch.hppcrt.hash.MurmurHash3;

public final class HashContainers
{
    /**
     * Maximum array size for hash containers (power-of-two and still
     * allocable in Java, not a negative int).
     */
    public final static int MAX_HASH_ARRAY_LENGTH = 0x80000000 >>> 1;

    /**
     * Minimum hash buffer size.
     */
    public final static int MIN_HASH_ARRAY_LENGTH = Containers.DEFAULT_EXPECTED_ELEMENTS;
    /**
     * Default load factor.
     */
    public final static float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * Minimal sane load factor (90 empty slots per 100).
     */
    public final static double MIN_LOAD_FACTOR = 10.0 / 100.0;

    /**
     * Maximum sane load factor (10 empty slot per 100).
     */
    public final static double MAX_LOAD_FACTOR = 90.0 / 100.0;

    /**
     * Computer static perturbations table.
     */
    private final static int[] PERTURBATIONS = new Callable<int[]>() {
        @Override
        public int[] call() {
            final int[] result = new int[32];
            for (int i = 0; i < result.length; i++) {
                result[i] = MurmurHash3.mix32(17 + i);
            }
            return result;
        }
    }.call();

    /**
     * Compute and return the maximum number of elements (inclusive) that can be
     * stored in a hash container for a given load factor.
     */
    public static int maxElements(final double loadFactor) {

        HashContainers.checkLoadFactor(loadFactor, 0, 1);
        return HashContainers.expandAtCount(HashContainers.MAX_HASH_ARRAY_LENGTH, loadFactor) - 1;
    }

    /**
     * Gives the minimum size if internal buffers able to accommodate elements,
     * given loadFactor.
     * 
     * @param elements
     * @param loadFactor
     */
    @SuppressWarnings("boxing")
    public static int minBufferSize(final int elements, final double loadFactor) {

        if (elements < 0) {
            throw new IllegalArgumentException("Number of elements must be >= 0: " + elements);
        }

        HashContainers.checkLoadFactor(loadFactor, HashContainers.MIN_LOAD_FACTOR, HashContainers.MAX_LOAD_FACTOR);

        //Assure room for one additional slot (marking the not-allocated) + one more as safety margin.
        long length = (long) (elements / loadFactor) + 2;

        //Then, round it to the next power of 2.
        length = Math.max(HashContainers.MIN_HASH_ARRAY_LENGTH, BitUtil.nextHighestPowerOfTwo(length));

        if (length > HashContainers.MAX_HASH_ARRAY_LENGTH) {

            throw new BufferAllocationException(
                    "Maximum array size exceeded for this load factor (elements: %d, load factor: %f)",
                    elements,
                    loadFactor);
        }

        return (int) length;
    }

    /**
     * Gives the next (bigger) size authorized for a buffer
     * 
     * @param arraySize
     * @param elements
     * @param loadFactor
     */
    @SuppressWarnings("boxing")
    public static int nextBufferSize(final int arraySize, final int elements, final double loadFactor) {

        assert HashContainers.checkPowerOfTwo(arraySize);

        if (arraySize == HashContainers.MAX_HASH_ARRAY_LENGTH) {
            throw new BufferAllocationException(
                    "Maximum array size exceeded for this load factor (elements: %d, load factor: %f)",
                    elements,
                    loadFactor);
        }

        return arraySize << 1;
    }

    /**
     * Protected purpose for Unit test visibility
     * 
     * @param arraySize
     * @param loadFactor
     * @return
     */
    protected static int expandAtCount(final int arraySize, final double loadFactor) {

        assert HashContainers.checkPowerOfTwo(arraySize);
        // Take care of hash container invariant (there has to be at least one empty slot to ensure
        // the lookup loop finds either the element or an empty slot).
        return Math.min(arraySize - 1, (int) Math.ceil(arraySize * loadFactor));
    }

    /** */
    @SuppressWarnings("boxing")
    private static void checkLoadFactor(final double loadFactor, final double minAllowedInclusive,
            final double maxAllowedInclusive) {

        if (loadFactor < minAllowedInclusive || loadFactor > maxAllowedInclusive) {

            throw new BufferAllocationException(
                    "The load factor should be in range [%.2f, %.2f]: %f",
                    minAllowedInclusive,
                    maxAllowedInclusive,
                    loadFactor);
        }
    }

    /** */
    private static boolean checkPowerOfTwo(final int arraySize) {
        // These are internals, we can just assert without retrying.
        assert arraySize > 1;
        assert BitUtil.nextHighestPowerOfTwo(arraySize) == arraySize;
        return true;
    }

    /**
     * <p>
     * Compute the a key perturbation value, unique per-power-of-2 capacity to
     * be applied before hashing.
     */
    public static int computePerturbationValue(final int capacity) {
        return HashContainers.PERTURBATIONS[Integer.numberOfLeadingZeros(capacity)];
    }

    /**
     * <p>
     * Compute a unique identifier associated with Object instance, valid in the
     * same process.
     */
    public static int computeUniqueIdentifier(final Object instance) {
        final long longId = System.identityHashCode(instance) ^ Containers.randomSeed64();

        //fold to 32 bit
        return (int) ((longId >>> 32) ^ longId);
    }
}
