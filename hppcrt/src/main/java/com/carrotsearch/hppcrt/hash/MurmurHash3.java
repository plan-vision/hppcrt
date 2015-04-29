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
     * = hash((int)0)
     */
    public static final int HASH_0 = 0;

    /**
     * = hash((int)1)
     */
    public static final int HASH_1 = 1364076727;

    private MurmurHash3()
    {
        // no instances.
    }

    /**
     * Hashes a 4-byte sequence (Java int).
     * @param x an integer.
     * @return a int hash value obtained by mixing the bits of {@code x}.
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
     * @param x a long integer.
     * @return a long hash value obtained by mixing the bits of {@code x}.
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

    /**
     * Mixes a int perturbated by a seed.
     * @param k an integer.
     * @param seed a perturbation value
     * @return a int hash value obtained by mixing the bits of {@code lk}.
     */
    public static int mix(int k, final int seed)
    {
        k ^= seed;
        k ^= k >>> 16;
        k *= 0x85ebca6b;
        k ^= k >>> 13;
        k *= 0xc2b2ae35;
        k ^= k >>> 16;

        return k;
    }

    /**
     * Mixes a long perturbated by a seed.
     * @param lk a long integer.
     * @param seed a perturbation value
     * @return a int hash value obtained by mixing the bits of {@code lk}.
     */
    public static int mix(final long lk, final int seed)
    {
        //reduce to 32bit
        int k = (int) ((lk >>> 32) ^ lk);

        k ^= seed;
        k ^= k >>> 16;
        k *= 0x85ebca6b;
        k ^= k >>> 13;
        k *= 0xc2b2ae35;
        k ^= k >>> 16;

        return k;
    }

    /**
     * Mixes an float perturbated by a seed.
     * @param x a float.
     * @param seed a perturbation value
     * @return a int hash value obtained by mixing the bits of {@code x}.
     */
    public static int mix(final float x, final int seed)
    {
        int k = Float.floatToIntBits(x);

        k ^= seed;
        k ^= k >>> 16;
        k *= 0x85ebca6b;
        k ^= k >>> 13;
        k *= 0xc2b2ae35;
        k ^= k >>> 16;

        return k;
    }

    /**
     * Mix an double perturbated by a seed.
     * @param x a double.
     * @param seed a perturbation value
     * @return a int hash value obtained by mixing the bits of {@code x}.
     */
    public static int mix(final double x, final int seed)
    {
        final long lk = Double.doubleToLongBits(x);

        //reduce to 32bit
        int k = (int) ((lk >>> 32) ^ lk);

        k ^= seed;
        k ^= k >>> 16;
        k *= 0x85ebca6b;
        k ^= k >>> 13;
        k *= 0xc2b2ae35;
        k ^= k >>> 16;

        return k;
    }

}
