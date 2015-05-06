package com.carrotsearch.hppcrt;

import java.util.Random;

import com.carrotsearch.hppcrt.hash.MurmurHash3;

/**
 * A fast pseudo-random number generator. This class is not thread-safe and
 * should be used from a single thread only.
 * 
 * @see "http://xorshift.di.unimi.it/"
 * @see "http://xorshift.di.unimi.it/xorshift128plus.c"
 */
@SuppressWarnings("serial")
public class XorShift128P extends Random {
    /*
     * 128 bits of state.
     */
    private long state0, state1;

    public XorShift128P(final long seed) {

        this.state0 = XorShift128P.notZero(MurmurHash3.mix64(seed));
        this.state1 = XorShift128P.notZero(MurmurHash3.mix64(seed + 1));
    }

    public XorShift128P() {
        this(Containers.randomSeed64());
    }

    @Override
    public long nextLong() {
        long s1 = this.state0;
        final long s0 = this.state1;
        this.state0 = s0;
        s1 ^= s1 << 23;
        return (this.state1 = (s1 ^ s0 ^ (s1 >>> 17) ^ (s0 >>> 26))) + s0;
    }

    @Override
    public int nextInt() {
        return (int) nextLong();
    }

    @Override
    protected int next(final int bits) {
        return (int) (nextLong() & ((1L << bits) - 1));
    }

    private static long notZero(final long value) {
        return value == 0 ? 0xdeadbeefbabeL : value;
    }

    @Override
    public int nextInt(final int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException();
        }

        int r = (nextInt() >>> 1);
        final int m = bound - 1;
        if ((bound & m) == 0) {
            r = (int) ((bound * (long) r) >> 31);
        } else {
            for (int u = r; u - (r = u % bound) + m < 0; u = nextInt() >>> 1) {
            }
        }

        return r;
    }
}
