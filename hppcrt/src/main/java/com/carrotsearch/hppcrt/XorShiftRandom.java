package com.carrotsearch.hppcrt;

import java.util.Random;

/**
 * XorShift pseudo random number generator. This class is not thread-safe and
 * should be used from a single thread only.
 * 
 * @see "http://en.wikipedia.org/wiki/Xorshift"
 * @see "http://www.jstatsoft.org/v08/i14/paper"
 * @see "http://www.javamex.com/tutorials/random_numbers/xorshift.shtml"
 */
@SuppressWarnings("serial")
public class XorShiftRandom extends Random 
{

    private long x;

    public XorShiftRandom() {
        this(Containers.randomSeed64());
    }

    public XorShiftRandom(final long seed) {
        setSeed(seed);
    }

    @Override
    public long nextLong() {
        return this.x = XorShiftRandom.next(this.x);
    }

    @Override
    protected int next(final int bits) {
        return (int) (nextLong() & ((1L << bits) - 1));
    }

    @Override
    public void setSeed(final long seed) {
        this.x = seed;
    }

    private static long next(long x) {
        x ^= (x << 21);
        x ^= (x >>> 35);
        x ^= (x << 4);
        return x;
    }
}
