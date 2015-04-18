package com.carrotsearch.hppcrt.sets;

import static com.carrotsearch.hppcrt.TestUtils.*;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import com.carrotsearch.hppcrt.RequireAssertionsRule;
import com.carrotsearch.hppcrt.TestUtils;
import com.carrotsearch.hppcrt.cursors.IntCursor;
import com.carrotsearch.hppcrt.predicates.IntPredicate;
import com.carrotsearch.randomizedtesting.RandomizedTest;

/**
 * Unit tests for {@link DoubleLinkedIntSet}.
 */
public class DoubleLinkedIntSetTest<KType> extends RandomizedTest
{
    /**
     * Per-test fresh initialized instance.
     */
    public DoubleLinkedIntSet set;

    int key1 = 1;
    int key2 = 2;
    int defaultValue = 0;

    /**
     * Require assertions for all tests.
     */
    @Rule
    public MethodRule requireAssertions = new RequireAssertionsRule();

    /* */
    @Before
    public void initialize()
    {
        this.set = new DoubleLinkedIntSet();
    }

    /* */
    @Test
    public void testInitiallyEmpty()
    {
        Assert.assertEquals(0, this.set.size());
    }

    /* */
    @Test
    public void testAdd()
    {
        Assert.assertTrue(this.set.add(this.key1));
        Assert.assertFalse(this.set.add(this.key1));
        Assert.assertEquals(1, this.set.size());

        Assert.assertTrue(this.set.contains(this.key1));
        Assert.assertFalse(this.set.contains(this.key2));
    }

    /* */
    @Test
    public void testAdd2()
    {
        this.set.add(this.key1, this.key1);
        Assert.assertEquals(1, this.set.size());
        Assert.assertEquals(1, this.set.add(this.key1, this.key2));
        Assert.assertEquals(2, this.set.size());
    }

    /* */
    @Test
    public void testAddVarArgs()
    {
        this.set.add(0, 1, 2, 1, 0);
        Assert.assertEquals(3, this.set.size());
        TestUtils.assertSortedListEquals(this.set.toArray(), 0, 1, 2);
    }

    /* */
    @Test
    public void testAddAll()
    {
        final DoubleLinkedIntSet set2 = new DoubleLinkedIntSet();
        set2.add(1, 2);
        this.set.add(0, 1);

        Assert.assertEquals(1, this.set.addAll(set2));
        Assert.assertEquals(0, this.set.addAll(set2));

        Assert.assertEquals(3, this.set.size());
        TestUtils.assertSortedListEquals(this.set.toArray(), 0, 1, 2);
    }

    /* */
    @Test
    public void testRemove()
    {
        this.set.add(0, 1, 2, 3, 4);

        Assert.assertTrue(this.set.remove(2));
        Assert.assertFalse(this.set.remove(2));
        Assert.assertEquals(4, this.set.size());
        TestUtils.assertSortedListEquals(this.set.toArray(), 0, 1, 3, 4);
    }

    /* */
    @Test
    public void testInitialCapacityAndGrowth()
    {
        for (int i = 0; i < 256; i++)
        {
            final DoubleLinkedIntSet set = new DoubleLinkedIntSet(i, i);

            for (int j = 0; j < i; j++)
            {
                set.add(/* intrinsic:ktypecast */j);
            }

            Assert.assertEquals(i, set.size());
        }
    }

    /* */
    @Test
    public void testRemoveAllFromLookupContainer()
    {
        this.set.add(0, 1, 2, 3, 4);

        final IntOpenHashSet list2 = new IntOpenHashSet();
        IntOpenHashSet.from(1, 3, 5);

        Assert.assertEquals(2, this.set.removeAll(list2));
        Assert.assertEquals(3, this.set.size());
        TestUtils.assertSortedListEquals(this.set.toArray(), 0, 2, 4);
    }

    /* */
    @Test
    public void testRemoveAllWithPredicate()
    {
        this.set.add(0, this.key1, this.key2);

        Assert.assertEquals(1, this.set.removeAll(new IntPredicate()
        {
            @Override
            public boolean apply(final int v)
            {
                return v == DoubleLinkedIntSetTest.this.key1;
            };
        }));

        TestUtils.assertSortedListEquals(this.set.toArray(), 0, this.key2);
    }

    /* */
    @Test
    public void testRetainAllWithPredicate()
    {
        this.set.add(0, this.key1, this.key2, 3, 4, 5);

        Assert.assertEquals(4, this.set.retainAll(new IntPredicate()
        {
            @Override
            public boolean apply(final int v)
            {
                return v == DoubleLinkedIntSetTest.this.key1 || v == DoubleLinkedIntSetTest.this.key2;
            };
        }));

        TestUtils.assertSortedListEquals(this.set.toArray(), this.key1, this.key2);
    }

    /* */
    @Test
    public void testClear()
    {
        this.set.add(1, 2, 3);
        this.set.clear();
        Assert.assertEquals(0, this.set.size());
        Assert.assertEquals(0, this.set.toArray().length);
    }

    /* */
    @Test
    public void testIterable()
    {
        this.set.add(1, 2, 2, 3, 4);
        this.set.remove(2);
        Assert.assertEquals(3, this.set.size());

        int count = 0;
        for (final IntCursor cursor : this.set)
        {
            count++;
            Assert.assertTrue(this.set.contains(cursor.value));
        }
        Assert.assertEquals(count, this.set.size());

        this.set.clear();
        Assert.assertFalse(this.set.iterator().hasNext());
    }

    /* */
    @Test
    public void testConstructorFromContainer()
    {
        final IntOpenHashSet list2 = new IntOpenHashSet();
        IntOpenHashSet.from(1, 3, 5);

        this.set = new DoubleLinkedIntSet(list2);
        Assert.assertEquals(3, this.set.size());
        TestUtils.assertSortedListEquals(list2.toArray(), this.set.toArray());
    }

    /* */
    @Test
    public void testFromMethod()
    {
        final IntOpenHashSet list2 = new IntOpenHashSet();
        IntOpenHashSet.from(1, 3, 5);

        final DoubleLinkedIntSet s1 = DoubleLinkedIntSet.from(1, 3, 5);
        final DoubleLinkedIntSet s2 = DoubleLinkedIntSet.from(1, 3, 5);

        TestUtils.assertSortedListEquals(list2.toArray(), s1.toArray());
        TestUtils.assertSortedListEquals(list2.toArray(), s2.toArray());
    }

    /* */
    @Test
    public void testToString()
    {
        Assert.assertEquals("[1, 3, 5]", DoubleLinkedIntSet.from(1, 3, 5).toString());
    }

    /* */
    @Test
    public void testClone()
    {
        this.set.add(1, 2, 3);

        final DoubleLinkedIntSet cloned = this.set.clone();
        cloned.remove(1);

        TestUtils.assertSortedListEquals(this.set.toArray(), 1, 2, 3);
        TestUtils.assertSortedListEquals(cloned.toArray(), 2, 3);
    }

    /**
     * Run some random insertions/ deletions and compare the results
     * against <code>java.util.HashSet</code>.
     */
    @Test
    public void testAgainstHashMap()
    {
        final java.util.Random rnd = new java.util.Random(RandomizedTest.randomLong());
        final java.util.HashSet<Integer> other = new java.util.HashSet<Integer>();

        for (int size = 1000; size < 20000; size += 4000)
        {
            other.clear();
            this.set.clear();

            for (int round = 0; round < size * 20; round++)
            {
                final Integer key = rnd.nextInt(size);

                if (rnd.nextBoolean())
                {
                    Assert.assertEquals(other.add(key), this.set.add(key));
                    Assert.assertTrue(this.set.contains(key));
                }
                else
                {
                    Assert.assertEquals(other.remove(key), this.set.remove(key));
                }

                Assert.assertEquals(other.size(), this.set.size());
            }

            final int[] actual = this.set.toArray();
            final int[] expected = new int[other.size()];
            int i = 0;
            for (final Integer v : other)
                expected[i++] = v;
            Arrays.sort(expected);
            Arrays.sort(actual);
            Assert.assertArrayEquals(expected, actual);
        }
    }

    /* */
    @Test
    public void testEqualsSameClass()
    {
        final DoubleLinkedIntSet l1 = DoubleLinkedIntSet.from(1, 2, 3);
        final DoubleLinkedIntSet l2 = DoubleLinkedIntSet.from(1, 2, 3);
        final DoubleLinkedIntSet l3 = DoubleLinkedIntSet.from(1, 2, 4);

        Assert.assertEquals(l1, l2);
        Assert.assertEquals(l1.hashCode(), l2.hashCode());

        Assert.assertNotEquals(l1, l3);
    }

    /* */
    @Test
    public void testEqualsSubClass()
    {
        class Sub extends DoubleLinkedIntSet
        {
        };

        final DoubleLinkedIntSet l1 = DoubleLinkedIntSet.from(1, 2, 3);
        final DoubleLinkedIntSet l2 = new Sub();
        final DoubleLinkedIntSet l3 = new Sub();
        l2.addAll(l1);
        l3.addAll(l1);

        Assert.assertEquals(l2, l3);

        Assert.assertNotEquals(l1, l2);
    }
}
