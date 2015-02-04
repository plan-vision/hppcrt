package com.carrotsearch.hppcrt;

/**
 * Something implementing a map interface (int-int).
 * (or OBJ - int)
 */
public abstract class MapImplementation<IMPLEM>
{
    public enum HASH_QUALITY
    {
        NORMAL(0),
        BAD(6);

        public final int shift;

        private HASH_QUALITY(final int bitshift)
        {
            this.shift = bitshift;
        }
    }

    /**
     * A Int holder with variable Hash Qualities.
     * @author Vincent
     *
     */
    public static class ComparableInt implements Comparable<ComparableInt>
    {
        public int value;
        public final int bitshift;

        public ComparableInt(final int initValue, final HASH_QUALITY quality)
        {
            this.value = initValue;
            this.bitshift = quality.shift;
        }

        @Override
        public int compareTo(final ComparableInt other)
        {
            if (this.value < other.value)
            {
                return -1;
            }
            else if (this.value > other.value)
            {
                return 1;
            }

            return 0;
        }

        @Override
        public int hashCode()
        {
            return this.value << this.bitshift;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (obj instanceof ComparableInt)
            {
                return ((ComparableInt) obj).value == this.value;
            }

            return false;
        }
    }

    public final IMPLEM instance;

    protected MapImplementation(final IMPLEM instance)
    {
        this.instance = instance;
    }

    /**
     * Contains bench to run, setup() must prepare the K,V set before
     */
    public abstract int benchContainKeys();

    /**
     * removed bench to run, setup() must prepare the K,V set before
     */
    public abstract int benchRemoveKeys();

    /**
     * put  bench to run, setup() must prepare the K,V set before
     */
    public abstract int benchPutAll();

    /**
     * Preparation of a set of keys before executing the benchXXX() methods
     * @param keysToInsert the array of int or ComparableInts of HASH_QUALITY hashQ
     * to insert in the map on test
     * @param keysForContainsQuery the array of of int or ComparableInts to which the filled map
     * will be queried for contains()
     * @param keysForRemovalQuery the array of of int or ComparableInts to which the filled map
     * will be queried for remove()
     */
    public abstract void setup(int[] keysToInsert, HASH_QUALITY hashQ, int[] keysForContainsQuery, int[] keysForRemovalQuery);

    //// Convenience methods to implement
    //// to ease setup() implementation.
    //// used for setup()

    public abstract void clear();

    public abstract int size();

}