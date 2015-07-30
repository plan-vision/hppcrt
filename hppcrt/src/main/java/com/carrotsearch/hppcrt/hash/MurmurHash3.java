package com.carrotsearch.hppcrt.hash;

/**
 * Hash-mixing routines for primitive types. The implementation is based on the
 * finalization step from Austin Appleby's <code>MurmurHash3</code>. and David
 * Stafford variant 9 of 64bit mix function (MH3 finalization step, with
 * different shifts and constants)
 * 
 * @see "http://sites.google.com/site/murmurhash/"
 */
public final class MurmurHash3 
{
    /**
     * = hash((int)0)
     */
    public static final int HASH_0 = 0;

    /**
     * = hash((int)1)
     */
    public static final int HASH_1 = 1364076727;

    private static final int MUL1_INT = 0x85ebca6b;
    private static final int MUL2_INT = 0xc2b2ae35;

    private static final long MUL1_LONG = 0x4cd6944c5cc20b6dL;
    private static final long MUL2_LONG = 0xfc12c5b19d3259e9L;

    private MurmurHash3() {
        // no instances.
    }

    /**
     * Mix a 4-byte sequence (Java int), MH3's plain finalization step.
     */
    public static int mix32(int k) {

        k = (k ^ (k >>> 16)) * MurmurHash3.MUL1_INT;
        k = (k ^ (k >>> 13)) * MurmurHash3.MUL2_INT;

        return k ^ (k >>> 16);
    }

    /**
     * Mix an 8-byte sequence (Java long): Computes David Stafford variant 9 of
     * 64bit mix function (MH3 finalization step, with different shifts and
     * constants).
     * 
     * Variant 9 is picked because it contains two 32-bit shifts which could be
     * possibly optimized into better machine code.
     * 
     * @see "http://zimbry.blogspot.com/2011/09/better-bit-mixing-improving-on.html"
     */
    public static long mix64(long z) {

        z = (z ^ (z >>> 32)) * MurmurHash3.MUL1_LONG;
        z = (z ^ (z >>> 29)) * MurmurHash3.MUL2_LONG;
        return z ^ (z >>> 32);
    }
}
