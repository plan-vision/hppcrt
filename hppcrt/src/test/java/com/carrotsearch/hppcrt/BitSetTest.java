package com.carrotsearch.hppcrt;

import java.util.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.carrotsearch.hppcrt.cursors.IntCursor;
import com.carrotsearch.hppcrt.cursors.LongCursor;
import com.carrotsearch.hppcrt.predicates.IntPredicate;
import com.carrotsearch.hppcrt.predicates.LongPredicate;
import com.carrotsearch.randomizedtesting.RandomizedTest;

/**
 * Regression tests against <code>java.util.BitSet</code>.
 */
public class BitSetTest extends RandomizedTest
{
    private BitSet hppc;
    private java.util.BitSet jre;

    /* */
    @Before
    public void before()
    {
        this.hppc = new BitSet();
        this.jre = new java.util.BitSet();
    }

    /**
     * Test to string conversion.
     */
    @Test
    public void testToString()
    {
        Assert.assertEquals(this.jre.toString(), this.hppc.toString());

        for (final int i : new int[] { 1, 10, 20, 5000 }) {
            this.hppc.set(i);
            this.jre.set(i);
        }
        Assert.assertEquals(this.jre.toString(), this.hppc.toString());
    }

    /**
     * Test random insertions into the bitset.
     */
    @Test
    public void testAgainstJREBitSet() throws Exception
    {
        final int rounds = 100;
        final int bits = 1000;
        final int bitSpace = bits * 10;

        for (int i = 0; i < rounds; i++)
        {
            for (int bit = 0; bit < bits; bit++)
            {
                final int index = RandomizedTest.randomInt(bitSpace);
                this.jre.set(index);
                this.hppc.set(index);

                Assert.assertEquals(this.jre.length(), this.hppc.length());
            }

            assertSame(this.jre, this.hppc);
            assertIntLookupContainer(this.jre, this.hppc.asIntLookupContainer());
            assertLongLookupContainer(this.jre, this.hppc.asLongLookupContainer());
        }
    }

    /** */
    @Test
    public void testHashCodeEquals()
    {
        final BitSet bs1 = new BitSet(200);
        final BitSet bs2 = new BitSet(64);
        bs1.set(3);
        bs2.set(3);

        Assert.assertEquals(bs1, bs2);
        Assert.assertEquals(bs1.hashCode(), bs2.hashCode());
    }

    /**
     * Assert that the two bitsets are identical.
     */
    private void assertSame(final java.util.BitSet jre, final BitSet hppc)
    {
        // Cardinality and emptiness status.
        Assert.assertEquals(jre.cardinality(), hppc.cardinality());
        Assert.assertEquals(jre.isEmpty(), hppc.isEmpty());

        // Check bit-by-bit.
        for (int i = 0; i < jre.size() - 1; i++)
            Assert.assertEquals(jre.get(i), hppc.get(i));

        // Check iterator indices.
        int i = jre.nextSetBit(0);
        int j = hppc.nextSetBit(0);
        final BitSetIterator k = hppc.iterator();
        while (i >= 0)
        {
            Assert.assertEquals(i, j);
            Assert.assertEquals(i, k.nextSetBit());

            i = jre.nextSetBit(i + 1);
            j = hppc.nextSetBit(j + 1);
        }
        Assert.assertEquals(-1, k.nextSetBit());
        Assert.assertEquals(-1, j);
    }

    private void assertIntLookupContainer(final java.util.BitSet jre, final IntLookupContainer ilc)
    {
        int i, j;

        // Check adapter to IntLookupContainer
        Assert.assertEquals(ilc.size(), jre.cardinality());

        i = jre.nextSetBit(0);
        final Iterator<IntCursor> ilcCursor = ilc.iterator();
        while (i >= 0)
        {
            Assert.assertTrue(ilcCursor.hasNext());
            final IntCursor c = ilcCursor.next();
            Assert.assertEquals(i, c.index);
            Assert.assertEquals(i, c.value);

            i = jre.nextSetBit(i + 1);
        }
        Assert.assertEquals(-1, i);
        Assert.assertFalse(ilcCursor.hasNext());
        try
        {
            ilcCursor.next();
            Assert.fail();
        }
        catch (final NoSuchElementException e)
        {
            // expected.
        }

        // Check toArray()
        final int[] setIndexes = ilc.toArray();
        final int[] expected = new int[jre.cardinality()];
        for (j = 0, i = jre.nextSetBit(0); i >= 0; i = jre.nextSetBit(i + 1))
        {
            expected[j++] = i;
        }
        Assert.assertArrayEquals(expected, setIndexes);

        // Test for-each predicates.
        ilc.forEach(new IntPredicate()
        {
            int i = jre.nextSetBit(0);

            @Override
            public boolean apply(final int setBit)
            {
                Assert.assertEquals(this.i, setBit);
                this.i = jre.nextSetBit(this.i + 1);
                return true;
            }
        });

        // Test contains.
        for (i = 0; i < jre.size() + 65; i++)
        {
            Assert.assertEquals(this.hppc.get(i), ilc.contains(i));
        }

        // IntLookupContainer must not throw exceptions on negative arguments.
        ilc.contains(-1);
    }

    private void assertLongLookupContainer(final java.util.BitSet jre, final LongLookupContainer llc)
    {
        int i, j;

        // Check adapter to IntLookupContainer
        Assert.assertEquals(llc.size(), jre.cardinality());

        i = jre.nextSetBit(0);
        final Iterator<LongCursor> llcCursor = llc.iterator();
        while (i >= 0)
        {
            Assert.assertTrue(llcCursor.hasNext());
            final LongCursor c = llcCursor.next();
            Assert.assertEquals(i, c.index);
            Assert.assertEquals(i, c.value);

            i = jre.nextSetBit(i + 1);
        }
        Assert.assertEquals(-1, i);
        Assert.assertFalse(llcCursor.hasNext());
        try
        {
            llcCursor.next();
            Assert.fail();
        }
        catch (final NoSuchElementException e)
        {
            // expected.
        }

        // Check toArray()
        final long[] setIndexes = llc.toArray();
        final long[] expected = new long[jre.cardinality()];
        for (j = 0, i = jre.nextSetBit(0); i >= 0; i = jre.nextSetBit(i + 1))
        {
            expected[j++] = i;
        }
        Assert.assertArrayEquals(expected, setIndexes);

        // Test for-each predicates.
        llc.forEach(new LongPredicate()
        {
            int i = jre.nextSetBit(0);

            @Override
            public boolean apply(final long setBit)
            {
                Assert.assertEquals(this.i, setBit);
                this.i = jre.nextSetBit(this.i + 1);
                return true;
            }
        });

        // Test contains.
        for (i = 0; i < jre.size() + 65; i++)
        {
            Assert.assertEquals(this.hppc.get(i), llc.contains(i));
        }

        // IntLookupContainer must not throw exceptions on negative arguments.
        llc.contains(-1);
    }
}
