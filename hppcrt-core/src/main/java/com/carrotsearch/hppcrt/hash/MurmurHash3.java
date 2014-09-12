package com.carrotsearch.hppcrt.hash;

/**
 * Hash routines for primitive types. The implementation is based on the finalization step
 * from Austin Appleby's <code>MurmurHash3</code>.
 * 
 * @see "http://sites.google.com/site/murmurhash/"
 */
public final class MurmurHash3
{

    /**
     * = MurmurHash3.hash(0)
     */
    public static final int HASH_0 = 0;

    /**
     * = MurmurHash3.hash(1)
     */
    public static final int HASH_1 = 1364076727;

    private MurmurHash3()
    {
        // no instances.
    }

    /**
     * Hashes a 4-byte sequence (Java int).
     */
    public static int hash(int k)
    {
        k ^= k >>> 16;
        k *= 0x85ebca6b;
        k ^= k >>> 13;
        k *= 0xc2b2ae35;
        k ^= k >>> 16;
        return k;
    }

    /**
     * Hashes an 8-byte sequence (Java long).
     */
    public static long hash(long k)
    {
        k ^= k >>> 33;
        k *= 0xff51afd7ed558ccdL;
        k ^= k >>> 33;
        k *= 0xc4ceb9fe1a85ec53L;
        k ^= k >>> 33;

        return k;
    }
}
