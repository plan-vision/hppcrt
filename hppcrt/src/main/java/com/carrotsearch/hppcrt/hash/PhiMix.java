package com.carrotsearch.hppcrt.hash;

/**
 * Quickly mixes the bits of integers.
 * <p>
 * Those methods mixes the bits of the argument by multiplying by the golden
 * ratio and xorshifting the result.
 * <p>
 * It is borrowed from <a
 * href="https://github.com/OpenHFT/Koloboke">Koloboke</a>, and it has slightly
 * worse behaviour than {@link MurmurHash3} (in open-addressing hash tables the
 * average number of probes is slightly larger), but it's much faster.
 * <p>
 * Reciprocal mixing functions are borrowed from <a
 * href="http://fastutil.di.unimi.it/">fastutil</a>.
 */
public final class PhiMix 
{
  /**
   * 2<sup>32</sup> &middot; &phi;, &phi; = (&#x221A;5 &minus; 1)/2. (package
   * visibility)
   */
  static final int INT_PHI = 0x9E3779B9;

  /**
   * 2<sup>64</sup> &middot; &phi;, &phi; = (&#x221A;5 &minus; 1)/2. (package
   * visibility)
   */
  static final long LONG_PHI = 0x9E3779B97F4A7C15L;

  /**
   * The reciprocal of {@link #INT_PHI} modulo 2<sup>32</sup>.
   */
  private static final int INV_INT_PHI = 0x144cbc89;

  /**
   * The reciprocal of {@link #LONG_PHI} modulo 2<sup>64</sup>.
   */
  private static final long INV_LONG_PHI = 0xf1de83e19937733dL;

  /**
   * = hash((int)0)
   */
  public static final int HASH_0 = 0;

  /**
   * = hash((int)1)
   */
  public static final int HASH_1 = 1640556430;

  private PhiMix() {
    // no instances.
  }

  /**
   * Hashes a 4-byte sequence (Java int).
   * 
   * @param x
   *          an integer.
   * @return a hash value obtained by mixing the bits of {@code x}.
   */
  public static int mix32(final int x) {
    final int h = x * PhiMix.INT_PHI;
    return h ^ (h >> 16);
  }

  /**
   * The inverse of {@link #hash(int)}. This method is mainly useful to create
   * unit tests.
   * 
   * @param x
   *          an integer.
   * @return a value that passed through {@link #hash(int)} would give {@code x}
   *         .
   */
  public static int invMix32(final int x) {
    return (x ^ x >>> 16) * PhiMix.INV_INT_PHI;
  }

  /**
   * Hashes an 8-byte sequence (Java long).
   * 
   * @param x
   *          a long integer.
   * @return a hash value obtained by mixing the bits of {@code x}.
   */
  public static long mix64(final long x) {
    long h = x * PhiMix.LONG_PHI;
    h ^= h >> 32;
    return h ^ (h >> 16);
  }

  /**
   * The inverse of {@link #hash(long)}. This method is mainly useful to create
   * unit tests.
   * 
   * @param x
   *          a long integer.
   * @return a value that passed through {@link #hash(long)} would give
   *         {@code x}.
   */
  public static long invMix64(long x) {
    x ^= x >>> 32;
    x ^= x >>> 16;
    return (x ^ x >>> 32) * PhiMix.INV_LONG_PHI;
  }
}
