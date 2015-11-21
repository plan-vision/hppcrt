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
    /**
     * Mix a byte.
     * 
     * @param k
     *          a byte.
     * @return an int hash value obtained by mixing the bits of {@code k}.
     */
    public static int mix(final byte k) {
        return k * PhiMix.INT_PHI;
    }

    /**
     * Mix a byte perturbated by a seed.
     * 
     * @param k
     *          a byte.
     * @param seed
     *          a perturbation value
     * @return an int hash value obtained by mixing the bits of {@code k}.
     */
    public static int mix(final byte k, final int seed) {
        return (k ^ seed) * PhiMix.INT_PHI;
    }

    /**
     * Mix a short.
     * 
     * @param k
     *          a short.
     * @return an int hash value obtained by mixing the bits of {@code k}.
     */
    public static int mix(final short k) {
        return PhiMix.mix32(k);
    }

    /**
     * Mix a short perturbated by a seed.
     * 
     * @param k
     *          a short.
     * @param seed
     *          a perturbation value
     * @return an int hash value obtained by mixing the bits of {@code k}.
     */
    public static int mix(final short k, final int seed) {
        return PhiMix.mix32(k ^ seed);
    }

    /**
     * Mix a char.
     * 
     * @param k
     *          a char.
     * @return an int hash value obtained by mixing the bits of {@code k}.
     */
    public static int mix(final char k) {
        return PhiMix.mix32(k);
    }

    /**
     * Mix a char perturbated by a seed.
     * 
     * @param k
     *          a char.
     * @param seed
     *          a perturbation value
     * @return an int hash value obtained by mixing the bits of {@code k}.
     */
    public static int mix(final char k, final int seed) {
        return PhiMix.mix32(k ^ seed);
    }

    // Better mix for larger key domains.
    /**
     * Mix an int.
     * 
     * @param k
     *          an integer.
     * @param seed
     *          a perturbation value
     * @return an int hash value obtained by mixing the bits of {@code k}.
     */
    public static int mix(final int key) {
        return MurmurHash3.mix32(key);
    }

    /**
     * Mix an int perturbated by a seed.
     * 
     * @param k
     *          an integer.
     * @param seed
     *          a perturbation value
     * @return an int hash value obtained by mixing the bits of {@code k}.
     */
    public static int mix(final int k, final int seed) {
        return MurmurHash3.mix32(k ^ seed);
    }

    /**
     * Mix a float.
     * 
     * @param x
     *          a float.
     * @param seed
     *          a perturbation value
     * @return an int hash value obtained by mixing the bits of {@code x}.
     */
    public static int mix(final float x) {
        return MurmurHash3.mix32(Float.floatToIntBits(x));
    }

    /**
     * Mix a float perturbated by a seed.
     * 
     * @param x
     *          a float.
     * @param seed
     *          a perturbation value
     * @return an int hash value obtained by mixing the bits of {@code x}.
     */
    public static int mix(final float x, final int seed) {
        return MurmurHash3.mix32(Float.floatToIntBits(x) ^ seed);
    }

    /**
     * Mix a double.
     * 
     * @param x
     *          a double.
     * @return an int hash value obtained by mixing the bits of {@code x}.
     */
    public static int mix(final double x) {
        return (int) MurmurHash3.mix64(Double.doubleToLongBits(x));
    }

    /**
     * Mix a double perturbated by a seed.
     * 
     * @param x
     *          a double.
     * @param seed
     *          a perturbation value
     * @return a int hash value obtained by mixing the bits of {@code x}.
     */
    public static int mix(final double x, final int seed) {
        return (int) MurmurHash3.mix64(Double.doubleToLongBits(x) ^ seed);
    }

    /**
     * Mix a long.
     * 
     * @param z
     *          a long integer.
     * @param seed
     *          a perturbation value
     * @return an int hash value obtained by mixing the bits of {@code z}.
     */
    public static int mix(final long z) {
        return (int) MurmurHash3.mix64(z);
    }

    /**
     * Mix a long perturbated by a seed.
     * 
     * @param z
     *          a long integer.
     * @param seed
     *          a perturbation value
     * @return an int hash value obtained by mixing the bits of {@code z}.
     */
    public static int mix(final long z, final int seed) {
        return (int) MurmurHash3.mix64(z ^ seed);
    }

    /**
     * Mix an Object instance {@link hashCode()}.
     * 
     * @param key
     *          an Object instance.
     * @return an int hash value obtained by mixing the bits of {@code key.hashCode()}, or 0 if key is null.
     */
    public static int mix(final Object key) {
        return key == null ? 0 : MurmurHash3.mix32(key.hashCode());
    }
}
