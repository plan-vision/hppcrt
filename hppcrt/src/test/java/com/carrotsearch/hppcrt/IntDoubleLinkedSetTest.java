package com.carrotsearch.hppcrt;

import static com.carrotsearch.hppcrt.TestUtils.assertSortedListEquals;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.*;
import org.junit.rules.MethodRule;

import com.carrotsearch.hppcrt.cursors.IntCursor;
import com.carrotsearch.hppcrt.predicates.IntPredicate;
import com.carrotsearch.hppcrt.sets.DoubleLinkedIntSet;
import com.carrotsearch.hppcrt.sets.IntOpenHashSet;

/**
 * Unit tests for {@link DoubleLinkedIntSet}.
 */
public class IntDoubleLinkedSetTest<KType>
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
        set = new DoubleLinkedIntSet();
    }

    /* */
    @Test
    public void testInitiallyEmpty()
    {
        Assert.assertEquals(0, set.size());
    }

    /* */
    @Test
    public void testAdd()
    {
        Assert.assertTrue(set.add(key1));
        Assert.assertFalse(set.add(key1));
        Assert.assertEquals(1, set.size());

        Assert.assertTrue(set.contains(key1));
        Assert.assertFalse(set.contains(key2));
    }

    /* */
    @Test
    public void testAdd2()
    {
        set.add(key1, key1);
        Assert.assertEquals(1, set.size());
        Assert.assertEquals(1, set.add(key1, key2));
        Assert.assertEquals(2, set.size());
    }

    /* */
    @Test
    public void testAddVarArgs()
    {
        set.add(0, 1, 2, 1, 0);
        Assert.assertEquals(3, set.size());
        TestUtils.assertSortedListEquals(set.toArray(), 0, 1, 2);
    }

    /* */
    @Test
    public void testAddAll()
    {
        final DoubleLinkedIntSet set2 = new DoubleLinkedIntSet();
        set2.add(1, 2);
        set.add(0, 1);

        Assert.assertEquals(1, set.addAll(set2));
        Assert.assertEquals(0, set.addAll(set2));

        Assert.assertEquals(3, set.size());
        TestUtils.assertSortedListEquals(set.toArray(), 0, 1, 2);
    }

    /* */
    @Test
    public void testRemove()
    {
        set.add(0, 1, 2, 3, 4);

        Assert.assertTrue(set.remove(2));
        Assert.assertFalse(set.remove(2));
        Assert.assertEquals(4, set.size());
        TestUtils.assertSortedListEquals(set.toArray(), 0, 1, 3, 4);
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
        set.add(0, 1, 2, 3, 4);

        final IntOpenHashSet list2 = new IntOpenHashSet();
        list2.add(1, 3, 5);

        Assert.assertEquals(2, set.removeAll(list2));
        Assert.assertEquals(3, set.size());
        TestUtils.assertSortedListEquals(set.toArray(), 0, 2, 4);
    }

    /* */
    @Test
    public void testRemoveAllWithPredicate()
    {
        set.add(0, key1, key2);

        Assert.assertEquals(1, set.removeAll(new IntPredicate()
        {
            @Override
            public boolean apply(final int v)
            {
                return v == key1;
            };
        }));

        TestUtils.assertSortedListEquals(set.toArray(), 0, key2);
    }

    /* */
    @Test
    public void testRetainAllWithPredicate()
    {
        set.add(0, key1, key2, 3, 4, 5);

        Assert.assertEquals(4, set.retainAll(new IntPredicate()
        {
            @Override
            public boolean apply(final int v)
            {
                return v == key1 || v == key2;
            };
        }));

        TestUtils.assertSortedListEquals(set.toArray(), key1, key2);
    }

    /* */
    @Test
    public void testClear()
    {
        set.add(1, 2, 3);
        set.clear();
        Assert.assertEquals(0, set.size());
        Assert.assertEquals(0, set.toArray().length);
    }

    /* */
    @Test
    public void testIterable()
    {
        set.add(1, 2, 2, 3, 4);
        set.remove(2);
        Assert.assertEquals(3, set.size());

        int count = 0;
        for (final IntCursor cursor : set)
        {
            count++;
            Assert.assertTrue(set.contains(cursor.value));
        }
        Assert.assertEquals(count, set.size());

        set.clear();
        Assert.assertFalse(set.iterator().hasNext());
    }

    /* */
    @Test
    public void testConstructorFromContainer()
    {
        final IntOpenHashSet list2 = new IntOpenHashSet();
        list2.add(1, 3, 5);

        set = new DoubleLinkedIntSet(list2);
        Assert.assertEquals(3, set.size());
        TestUtils.assertSortedListEquals(list2.toArray(), set.toArray());
    }

    /* */
    @Test
    public void testFromMethod()
    {
        final IntOpenHashSet list2 = new IntOpenHashSet();
        list2.add(1, 3, 5);

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
        set.add(1, 2, 3);

        final DoubleLinkedIntSet cloned = set.clone();
        cloned.remove(1);

        TestUtils.assertSortedListEquals(set.toArray(), 1, 2, 3);
        TestUtils.assertSortedListEquals(cloned.toArray(), 2, 3);
    }

    /**
     * Run some random insertions/ deletions and compare the results
     * against <code>java.util.HashSet</code>.
     */
    @Test
    public void testAgainstHashMap()
    {
        final java.util.Random rnd = new java.util.Random(0x11223344);
        final java.util.HashSet<Integer> other = new java.util.HashSet<Integer>();

        for (int size = 1000; size < 20000; size += 4000)
        {
            other.clear();
            set.clear();

            for (int round = 0; round < size * 20; round++)
            {
                final Integer key = rnd.nextInt(size);

                if (rnd.nextBoolean())
                {
                    Assert.assertEquals(other.add(key), set.add(key));
                    Assert.assertTrue(set.contains(key));
                }
                else
                {
                    Assert.assertEquals(other.remove(key), set.remove(key));
                }

                Assert.assertEquals(other.size(), set.size());
            }

            final int[] actual = set.toArray();
            final int[] expected = new int[other.size()];
            int i = 0;
            for (final Integer v : other)
                expected[i++] = v;
            Arrays.sort(expected);
            Arrays.sort(actual);
            Assert.assertArrayEquals(expected, actual);
        }
    }
}
