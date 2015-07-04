package com.carrotsearch.hppcrt;

public final class HashContainers
{
    /**
     * Maximum array size for hash containers (power-of-two and still
     * allocable in Java, not a negative int).
     */
    public final static int MAX_HASH_ARRAY_LENGTH = 0x80000000 >>> 1;

    /**
     * Minimum hash buffer size. (must be a power-of-two !)
     */
    public final static int MIN_HASH_ARRAY_LENGTH = 1 << 3;

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
     * Compute and return the maximum number of elements (inclusive) that can be
     * stored in a hash container for a given load factor.
     * @param loadFactor
     */
    public static int maxElements(final double loadFactor) {

        HashContainers.checkLoadFactor(loadFactor, HashContainers.MIN_LOAD_FACTOR, HashContainers.MAX_LOAD_FACTOR);
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

        HashContainers.checkPowerOfTwo(arraySize);

        if (arraySize == HashContainers.MAX_HASH_ARRAY_LENGTH) {
            throw new BufferAllocationException(
                    "Maximum array size exceeded for this load factor (elements: %d, load factor: %f)",
                    elements,
                    loadFactor);
        }

        return arraySize << 1;
    }

    /**
     * Compute the max number of elements
     * that can be put into a hash container from a power of two arraySize,
     * given a load factor loadFactor
     * @param arraySize
     * @param loadFactor
     * @return
     */
    public static int expandAtCount(final int arraySize, final double loadFactor) {

        HashContainers.checkPowerOfTwo(arraySize);
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
    private static void checkPowerOfTwo(final int arraySize) {

        if (BitUtil.nextHighestPowerOfTwo(arraySize) != arraySize) {

            throw new IllegalArgumentException("arraySize must be a power of two !");
        }
    }

    /**
     * <p>
     * Compute a unique identifier associated with Object instance, valid in the
     * same process.
     * @param instance
     */
    public static int computeUniqueIdentifier(final Object instance) {
        final long longId = System.identityHashCode(instance) ^ Containers.randomSeed64();

        //fold to 32 bit
        return (int) ((longId >>> 32) ^ longId);
    }
}
