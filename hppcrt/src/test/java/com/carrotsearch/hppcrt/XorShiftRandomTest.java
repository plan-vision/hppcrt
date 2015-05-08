package com.carrotsearch.hppcrt;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class XorShiftRandomTest
{
    /** */
    @Test
    public void testApproxEqualBucketFill() {

        checkApproxEqualBucketFill(new XorShiftRandom(0xdeadbeef));
    }

    @Test
    public void testApproxEqualBucketFill128P() {

        checkApproxEqualBucketFill(new XorShift128P(0xdeadbeef));
    }

    @Test
    public void testNext() {

        checkNext(new XorShiftRandom());
    }

    @Test
    public void testNext128P() {

        checkNext(new XorShift128P());
    }

    private void checkNext(final Random random) {

        for (int bits = 1; bits <= 32; bits++) {
            final long max = (1L << bits) - 1;
            long mask = 0;
            for (int i = 0; i < 10000; i++) {

                long val = -1L;

                if (random instanceof XorShiftRandom) {

                    val = (((XorShiftRandom) random).next(bits)) & 0xffffffffL;

                } else if (random instanceof XorShift128P) {

                    val = (((XorShift128P) random).next(bits)) & 0xffffffffL;

                }

                mask |= val;
                Assert.assertTrue(val + " >= " + max + "?", val <= max);
            }
            Assert.assertEquals(max, mask);
        }
    }

    private void checkApproxEqualBucketFill(final Random rnd) {

        final int[] buckets = new int[(1 << 8)];
        final int mask = buckets.length - 1;

        final int hits = 1000000;

        for (int count = hits * buckets.length; --count >= 0;) {
            buckets[rnd.nextInt() & mask]++;
        }

        // every bucket should get +- 1% * hits
        final int limit = hits / 100;
        for (final int bucketCount : buckets) {
            Assert.assertTrue(Math.abs(bucketCount - hits) + " > " + limit + "?", Math.abs(bucketCount - hits) <= limit);

        }
    }
}
