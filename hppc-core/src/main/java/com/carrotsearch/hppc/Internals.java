package com.carrotsearch.hppc;

import java.util.Arrays;

import com.carrotsearch.hppc.hash.MurmurHash3;

/**
 * Internal utilities.
 */
final public class Internals
{
    final public static int NB_OF_PROCESSORS = Runtime.getRuntime().availableProcessors();

    final public static int BLANK_ARRAY_SIZE_IN_BIT_SHIFT = 10;

    /**
     * Batch blanking array size
     */
    final public static int BLANK_ARRAY_SIZE = 1 << Internals.BLANK_ARRAY_SIZE_IN_BIT_SHIFT;

    /**
     * Batch blanking array with Object nulls
     */
    final public static Object[] BLANKING_OBJECT_ARRAY = new Object[Internals.BLANK_ARRAY_SIZE];

    /**
     * Batch blanking array with boolean false
     */
    final public static boolean[] BLANKING_BOOLEAN_ARRAY = new boolean[Internals.BLANK_ARRAY_SIZE];

    /**
     * Batch blanking array with int == -1
     */
    final public static int[] BLANKING_INT_ARRAY_MINUS_ONE = new int[Internals.BLANK_ARRAY_SIZE];

    static {

        Arrays.fill(Internals.BLANKING_INT_ARRAY_MINUS_ONE, -1);
    }

    /**
     * rehash() with perturbation, version inlined for KType in generated code.
     * @param o
     * @param p
     * @return
     */
    public static <T> int rehashKType(final T o, final int p) {
        return o == null ? 0 : MurmurHash3.hash(o.hashCode() ^ p);
    }

    /**
     * rehash() with perturbation, version inlined for VType in generated code.
     * @param o
     * @param p
     * @return
     */
    public static <T> int rehashVType(final T o, final int p) {
        return o == null ? 0 : MurmurHash3.hash(o.hashCode() ^ p);
    }

    public static int rehash(final Object o, final int p) {
        return o == null ? 0 : MurmurHash3.hash(o.hashCode() ^ p);
    }

    public static int rehash(final byte v, final int p) {
        return MurmurHash3.hash(v ^ p);
    }

    public static int rehash(final short v, final int p) {
        return MurmurHash3.hash(v ^ p);
    }

    public static int rehash(final int v, final int p) {
        return MurmurHash3.hash(v ^ p);
    }

    public static int rehash(final long v, final int p) {
        return (int) MurmurHash3.hash(v ^ p);
    }

    public static int rehash(final char v, final int p) {
        return MurmurHash3.hash(v ^ p);
    }

    public static int rehash(final float v, final int p) {
        return MurmurHash3.hash(Float.floatToIntBits(v) ^ p);
    }

    public static int rehash(final double v, final int p) {
        return (int) MurmurHash3.hash(Double.doubleToLongBits(v) ^ p);
    }

    public static int rehash(final boolean b, final int p) {
        return MurmurHash3.hash((b ? 1 : 0) ^ p);
    }

    public static int rehash(final Object o) {
        return o == null ? 0 : MurmurHash3.hash(o.hashCode());
    }

    /**
     * rehash() version inlined for KType in generated code.
     * @param o
     * @return
     */
    public static <T> int rehashKType(final T o) {
        return o == null ? 0 : MurmurHash3.hash(o.hashCode());
    }

    /**
     * rehash() version inlined for VType in generated code.
     * @param o
     * @return
     */
    public static <T> int rehashVType(final T o) {
        return o == null ? 0 : MurmurHash3.hash(o.hashCode());
    }

    public static int rehash(final byte v) {
        return MurmurHash3.hash(v);
    }

    public static int rehash(final short v) {
        return MurmurHash3.hash(v);
    }

    public static int rehash(final int v) {
        return MurmurHash3.hash(v);
    }

    public static int rehash(final long v) {
        return (int) MurmurHash3.hash(v);
    }

    public static int rehash(final char v) {
        return MurmurHash3.hash(v);
    }

    public static int rehash(final float v) {
        return MurmurHash3.hash(Float.floatToIntBits(v));
    }

    public static int rehash(final double v) {
        return (int) MurmurHash3.hash(Double.doubleToLongBits(v));
    }

    public static int rehash(final boolean b) {
        return (b ? MurmurHash3.BOOLEAN_TRUE_HASH : MurmurHash3.BOOLEAN_FALSE_HASH);
    }

    /**
     * Create and return an array of template objects (<code>Object</code>s in the generic
     * version, corresponding key-primitive type in the generated version).
     * 
     * @param arraySize The size of the array to return.
     */
    @SuppressWarnings("unchecked")
    public static <T> T newArray(final int arraySize)
    {
        return (T) new Object[arraySize];
    }

    /**
     * Method to blank any Object[] array to "null"
     * from [startIndex; endIndex[, equivalent to {@link Arrays}.fill(objectArray, startIndex, endIndex, null)
     */
    public static <T> void blankObjectArray(final T[] objectArray, final int startIndex, final int endIndex) {

        assert startIndex <= endIndex;

        final int size = endIndex - startIndex;
        final int nbChunks = size >> Internals.BLANK_ARRAY_SIZE_IN_BIT_SHIFT;
        //compute remainder
        final int rem = size & (Internals.BLANK_ARRAY_SIZE - 1);

        for (int i = 0; i < nbChunks; i++) {

            System.arraycopy(Internals.BLANKING_OBJECT_ARRAY, 0,
                    objectArray, startIndex + (i << Internals.BLANK_ARRAY_SIZE_IN_BIT_SHIFT),
                    Internals.BLANK_ARRAY_SIZE);
        } //end for

        //fill the reminder
        if (rem > 0) {
            Arrays.fill(objectArray, startIndex + (nbChunks << Internals.BLANK_ARRAY_SIZE_IN_BIT_SHIFT),
                    startIndex + (nbChunks << Internals.BLANK_ARRAY_SIZE_IN_BIT_SHIFT) + rem, null);
        }
    }

    /**
     * Method to blank any boolean[] array to false
     * from [startIndex; endIndex[, equivalent to {@link Arrays}.fill(boolArray, startIndex, endIndex, false)
     */
    public static void blankBooleanArray(final boolean[] boolArray, final int startIndex, final int endIndex) {

        assert startIndex <= endIndex;

        final int size = endIndex - startIndex;
        final int nbChunks = size >> Internals.BLANK_ARRAY_SIZE_IN_BIT_SHIFT;
        //compute remainder
        final int rem = size & (Internals.BLANK_ARRAY_SIZE - 1);

        for (int i = 0; i < nbChunks; i++) {

            System.arraycopy(Internals.BLANKING_BOOLEAN_ARRAY, 0,
                    boolArray, startIndex + (i << Internals.BLANK_ARRAY_SIZE_IN_BIT_SHIFT),
                    Internals.BLANK_ARRAY_SIZE);
        } //end for

        //fill the reminder
        if (rem > 0) {
            Arrays.fill(boolArray, startIndex + (nbChunks << Internals.BLANK_ARRAY_SIZE_IN_BIT_SHIFT),
                    startIndex + (nbChunks << Internals.BLANK_ARRAY_SIZE_IN_BIT_SHIFT) + rem, false);
        }

    }

    /**
     * Method to blank any int[] array to -1
     * from [startIndex; endIndex[, equivalent to {@link Arrays}.fill(intArray, startIndex, endIndex, -1)
     */
    public static void blankIntArrayMinusOne(final int[] intArray, final int startIndex, final int endIndex) {

        assert startIndex <= endIndex;

        final int size = endIndex - startIndex;
        final int nbChunks = size >> Internals.BLANK_ARRAY_SIZE_IN_BIT_SHIFT;
        //compute remainder
        final int rem = size & (Internals.BLANK_ARRAY_SIZE - 1);

        for (int i = 0; i < nbChunks; i++) {

            System.arraycopy(Internals.BLANKING_INT_ARRAY_MINUS_ONE, 0,
                    intArray, startIndex + (i << Internals.BLANK_ARRAY_SIZE_IN_BIT_SHIFT),
                    Internals.BLANK_ARRAY_SIZE);
        } //end for

        //fill the reminder
        if (rem > 0) {
            Arrays.fill(intArray, startIndex + (nbChunks << Internals.BLANK_ARRAY_SIZE_IN_BIT_SHIFT),
                    startIndex + (nbChunks << Internals.BLANK_ARRAY_SIZE_IN_BIT_SHIFT) + rem, -1);
        }
    }

}
