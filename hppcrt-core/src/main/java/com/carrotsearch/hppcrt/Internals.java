package com.carrotsearch.hppcrt;

import java.util.Arrays;

import com.carrotsearch.hppcrt.hash.MurmurHash3;
import com.carrotsearch.hppcrt.hash.PhiMix;

/**
 * Internal utilities.
 */
public final class Internals
{
    final public static int NB_OF_PROCESSORS = Runtime.getRuntime().availableProcessors();

    /**
     * Rehash with perturbations methods
     * @param o
     * @param p
     * @return
     */
    public static int rehash(final Object o, final int p) {
        return o == null ? 0 : PhiMix.hash(o.hashCode() ^ p);
    }

    public static int rehash(final byte v, final int p) {
        return PhiMix.hash(v ^ p);
    }

    public static int rehash(final short v, final int p) {
        return PhiMix.hash(v ^ p);
    }

    public static int rehash(final int v, final int p) {
        return PhiMix.hash(v ^ p);
    }

    public static int rehash(final long v, final int p) {
        return (int) PhiMix.hash(v ^ p);
    }

    public static int rehash(final char v, final int p) {
        return PhiMix.hash(v ^ p);
    }

    public static int rehash(final float v, final int p) {
        return PhiMix.hash(Float.floatToIntBits(v) ^ p);
    }

    public static int rehash(final double v, final int p) {
        return (int) PhiMix.hash(Double.doubleToLongBits(v) ^ p);
    }

    public static int rehash(final boolean b, final int p) {
        return PhiMix.hash((b ? 1 : 0) ^ p);
    }

    /**
     * Keep using MurmurHash3 for better scrambling if the Object is
     * bad-behaved
     * @param o
     * @return
     */
    public static int rehash(final Object o) {
        return o == null ? 0 : MurmurHash3.hash(o.hashCode());
    }

    public static int rehash(final byte v) {
        return PhiMix.hash(v);
    }

    public static int rehash(final short v) {
        return PhiMix.hash(v);
    }

    public static int rehash(final int v) {
        return PhiMix.hash(v);
    }

    public static int rehash(final long v) {
        return (int) PhiMix.hash(v);
    }

    public static int rehash(final char v) {
        return PhiMix.hash(v);
    }

    public static int rehash(final float v) {
        return PhiMix.hash(Float.floatToIntBits(v));
    }

    public static int rehash(final double v) {
        return (int) PhiMix.hash(Double.doubleToLongBits(v));
    }

    public static int rehash(final boolean b) {
        return (b ? PhiMix.HASH_1 : PhiMix.HASH_0);
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
     * Returns the greatest common divisor between m and n (Euclid method)
     * @param m
     * @param n
     * @return
     */
    public static int gcd(int m, int n) {

        while (n != 0) {

            final int t = m % n;
            m = n;
            n = t;
        }

        return m;
    }
}
