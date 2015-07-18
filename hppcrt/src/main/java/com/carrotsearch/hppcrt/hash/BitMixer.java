package com.carrotsearch.hppcrt.hash;

/**
 * Bit mixing utilities. The purpose of these methods is to evenly distribute
 * key space over int32 range.
 */
public final class BitMixer
{

    /**
     * No instances.
     */
    private BitMixer() {
        //nothing
    }

    // Don't bother mixing very small key domains much.
    public static int mix(final byte key) {
        return key * PhiMix.INT_PHI;
    }

    public static int mix(final byte key, final int seed) {
        return (key ^ seed) * PhiMix.INT_PHI;
    }

    public static int mix(final short key) {
        return PhiMix.mix32(key);
    }

    public static int mix(final short key, final int seed) {
        return PhiMix.mix32(key ^ seed);
    }

    public static int mix(final char key) {
        return PhiMix.mix32(key);
    }

    public static int mix(final char key, final int seed) {
        return PhiMix.mix32(key ^ seed);
    }

    // Better mix for larger key domains.
    public static int mix(final int key) {
        return MurmurHash3.mix32(key);
    }

    public static int mix(final int key, final int seed) {
        return MurmurHash3.mix32(key ^ seed);
    }

    public static int mix(final float key) {
        return MurmurHash3.mix32(Float.floatToIntBits(key));
    }

    public static int mix(final float key, final int seed) {
        return MurmurHash3.mix32(Float.floatToIntBits(key) ^ seed);
    }

    public static int mix(final double key) {
        return (int) MurmurHash3.mix64(Double.doubleToLongBits(key));
    }

    public static int mix(final double key, final int seed) {
        return (int) MurmurHash3.mix64(Double.doubleToLongBits(key) ^ seed);
    }

    public static int mix(final long key) {
        return (int) MurmurHash3.mix64(key);
    }

    public static int mix(final boolean key) {
        return key ? MurmurHash3.HASH_1 : MurmurHash3.HASH_0;
    }

    public static int mix(final long key, final int seed) {
        return (int) MurmurHash3.mix64(key ^ seed);
    }

    public static int mix(final Object key) {
        return key == null ? 0 : MurmurHash3.mix32(key.hashCode());
    }

    public static int mix(final Object key, final int seed) {
        return key == null ? 0 : MurmurHash3.mix32(key.hashCode() ^ seed);
    }

}
