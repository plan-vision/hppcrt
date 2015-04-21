package com.carrotsearch.hppcrt.lists;

import java.util.*;

import org.junit.*;

import static com.carrotsearch.hppcrt.TestUtils.*;
import static org.junit.Assert.*;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.lists.*;
import com.carrotsearch.hppcrt.TestUtils;
import com.carrotsearch.hppcrt.cursors.*;
import com.carrotsearch.hppcrt.predicates.*;
import com.carrotsearch.hppcrt.procedures.*;
import com.carrotsearch.hppcrt.sets.*;
import com.carrotsearch.hppcrt.sorting.*;
import com.carrotsearch.hppcrt.strategies.*;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
// ${TemplateOptions.doNotGenerateKType("BOOLEAN")}
/**
 * Unit tests for {@link KTypeArrayDeque}.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeArrayDequeTest<KType> extends AbstractKTypeTest<KType>
{
    /**
     * Per-test fresh initialized instance.
     */
    public KTypeArrayDeque<KType> deque;

    public volatile long guard;

    /**
     * Some sequence values for tests.
     */
    private KTypeArrayList<KType> sequence;

    @BeforeClass
    public static void configure()
    {
        IteratorPool.configureInitialPoolSize(8);
    }

    /* */
    @Before
    public void initialize()
    {
        this.deque = KTypeArrayDeque.newInstance();
        this.sequence = KTypeArrayList.newInstance();

        for (int i = 0; i < 10000; i++) {
            this.sequence.add(cast(i));
        }
    }

    /**
     * Move one index to the right, wrapping around buffer of size modulus
     */
    private int oneRight(final int index, final int modulus)
    {
        return (index + 1 == modulus) ? 0 : index + 1;
    }

    @After
    public void checkConsistency()
    {
        if (this.deque != null)
        {
            for (int i = this.deque.tail; i < this.deque.head; i = oneRight(i, this.deque.buffer.length))
            {
                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                Assert.assertTrue(Intrinsics.<KType> defaultKTypeValue() == this.deque.buffer[i]);
                /*! #end !*/
            }
        }
    }

    /* */
    @Test
    public void testInitiallyEmpty()
    {
        Assert.assertEquals(0, this.deque.size());
    }

    /* */
    @Test
    public void testAddFirst()
    {
        this.deque.addFirst(this.k1);
        this.deque.addFirst(this.k2);
        this.deque.addFirst(this.k3);
        TestUtils.assertListEquals(this.deque.toArray(), 3, 2, 1);
        Assert.assertEquals(3, this.deque.size());
    }

    /* */
    @Test
    public void testAddLast()
    {
        this.deque.addLast(this.k1);
        this.deque.addLast(this.k2);
        this.deque.addLast(this.k3);
        TestUtils.assertListEquals(this.deque.toArray(), 1, 2, 3);
        Assert.assertEquals(3, this.deque.size());
    }

    /* */
    @Test
    public void testAddWithGrowth()
    {
        for (int i = 0; i < this.sequence.size(); i++)
        {
            this.deque.addFirst(this.sequence.buffer[i]);
        }

        TestUtils.assertListEquals(TestUtils.reverse(this.sequence.toArray()), this.deque.toArray());
    }

    /* */
    @Test
    public void testAddLastWithGrowth()
    {
        for (int i = 0; i < this.sequence.size(); i++) {
            this.deque.addLast(this.sequence.buffer[i]);
        }

        TestUtils.assertListEquals(this.sequence.toArray(), this.deque.toArray());
    }

    /* */
    @Test
    public void testAddAllFirst()
    {
        final KTypeArrayList<KType> list2 = KTypeArrayList.newInstance();
        list2.add(asArray(0, 1, 2));

        this.deque.addFirst(list2);
        TestUtils.assertListEquals(this.deque.toArray(), 2, 1, 0);
        this.deque.addFirst(list2);
        TestUtils.assertListEquals(this.deque.toArray(), 2, 1, 0, 2, 1, 0);

        this.deque.clear();
        final KTypeArrayDeque<KType> deque2 = new KTypeArrayDeque<KType>();
        deque2.addLast(asArray(0, 1, 2));
        this.deque.addFirst(deque2);
        TestUtils.assertListEquals(this.deque.toArray(), 2, 1, 0);
    }

    /* */
    @Test
    public void testAddAllLast()
    {
        final KTypeArrayList<KType> list2 = KTypeArrayList.newInstance();
        list2.add(asArray(0, 1, 2));

        this.deque.addLast(list2);
        TestUtils.assertListEquals(this.deque.toArray(), 0, 1, 2);
        this.deque.addLast(list2);
        TestUtils.assertListEquals(this.deque.toArray(), 0, 1, 2, 0, 1, 2);

        this.deque.clear();
        final KTypeArrayDeque<KType> deque2 = new KTypeArrayDeque<KType>();
        deque2.addLast(asArray(0, 1, 2));
        this.deque.addLast(deque2);
        TestUtils.assertListEquals(this.deque.toArray(), 0, 1, 2);
    }

    /* */
    @Test
    public void testRemoveFirst()
    {
        this.deque.addLast(this.k1);
        this.deque.addLast(this.k2);
        this.deque.addLast(this.k3);

        this.deque.removeFirst();
        TestUtils.assertListEquals(this.deque.toArray(), 2, 3);
        Assert.assertEquals(2, this.deque.size());

        this.deque.addFirst(this.k4);
        TestUtils.assertListEquals(this.deque.toArray(), 4, 2, 3);
        Assert.assertEquals(3, this.deque.size());

        this.deque.removeFirst();
        this.deque.removeFirst();
        this.deque.removeFirst();
        Assert.assertEquals(0, this.deque.toArray().length);
        Assert.assertEquals(0, this.deque.size());
    }

    /* */
    @Test(expected = AssertionError.class)
    public void testRemoveFirstEmpty()
    {
        this.deque.removeFirst();
    }

    /* */
    @Test
    public void testRemoveLast()
    {
        this.deque.addLast(this.k1);
        this.deque.addLast(this.k2);
        this.deque.addLast(this.k3);

        this.deque.removeLast();
        TestUtils.assertListEquals(this.deque.toArray(), 1, 2);
        Assert.assertEquals(2, this.deque.size());

        this.deque.addLast(this.k4);
        TestUtils.assertListEquals(this.deque.toArray(), 1, 2, 4);
        Assert.assertEquals(3, this.deque.size());

        this.deque.removeLast();
        this.deque.removeLast();
        this.deque.removeLast();
        Assert.assertEquals(0, this.deque.toArray().length);
        Assert.assertEquals(0, this.deque.size());
    }

    /* */
    @Test(expected = AssertionError.class)
    public void testRemoveLastEmpty()
    {
        this.deque.removeLast();
    }

    /* */
    @Test
    public void testGetFirst()
    {
        this.deque.addLast(this.k1);
        this.deque.addLast(this.k2);
        TestUtils.assertEquals2(this.k1, this.deque.getFirst());
        this.deque.addFirst(this.k3);
        TestUtils.assertEquals2(this.k3, this.deque.getFirst());
    }

    /* */
    @Test(expected = AssertionError.class)
    public void testGetFirstEmpty()
    {
        this.deque.getFirst();
    }

    /* */
    @Test
    public void testGetLast()
    {
        this.deque.addLast(this.k1);
        this.deque.addLast(this.k2);
        TestUtils.assertEquals2(this.k2, this.deque.getLast());
        this.deque.addLast(this.k3);
        TestUtils.assertEquals2(this.k3, this.deque.getLast());
    }

    /* */
    @Test(expected = AssertionError.class)
    public void testGetLastEmpty()
    {
        this.deque.getLast();
    }

    /* */
    @Test
    public void testIndexOf()
    {
        this.deque.addLast(asArray(0, 1, 2, 1, 0, 8, 7, 4, 3, 2));

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        this.deque.add((KType) null);
        Assert.assertEquals(10, this.deque.indexOf(null));
        /*! #end !*/

        Assert.assertEquals(0, this.deque.indexOf(this.k0));
        Assert.assertEquals(8, this.deque.indexOf(this.k3));
        Assert.assertEquals(-1, this.deque.indexOf(this.k9));
        Assert.assertEquals(2, this.deque.indexOf(this.k2));
        Assert.assertEquals(5, this.deque.indexOf(this.k8));
        Assert.assertEquals(7, this.deque.indexOf(this.k4));
    }

    /* */
    @Test
    public void testContains()
    {
        this.deque.addLast(asArray(0, 1, 2, 7, 4, 3));

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        this.deque.add((KType) null);
        Assert.assertTrue(this.deque.contains(null));
        /*! #end !*/

        Assert.assertTrue(this.deque.contains(this.k0));
        Assert.assertTrue(this.deque.contains(this.k3));
        Assert.assertTrue(this.deque.contains(this.k2));

        Assert.assertFalse(this.deque.contains(this.k5));
        Assert.assertFalse(this.deque.contains(this.k6));
        Assert.assertFalse(this.deque.contains(this.k8));
    }

    /* */
    @Test
    public void testLastIndexOf()
    {
        this.deque.addLast(asArray(0, 1, 2, 1, 0, 8, 3, 4, 8, 2));

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        this.deque.add((KType) null);
        Assert.assertEquals(10, this.deque.lastIndexOf(null));
        /*! #end !*/

        Assert.assertEquals(4, this.deque.lastIndexOf(this.k0));
        Assert.assertEquals(6, this.deque.lastIndexOf(this.k3));
        Assert.assertEquals(-1, this.deque.indexOf(this.k9));
        Assert.assertEquals(9, this.deque.lastIndexOf(this.k2));
        Assert.assertEquals(8, this.deque.lastIndexOf(this.k8));
    }

    /* */
    @Test
    public void testSet()
    {
        this.deque.addLast(asArray(0, 1, 2));

        TestUtils.assertEquals2(0, this.deque.set(0, this.k3));
        TestUtils.assertEquals2(1, this.deque.set(1, this.k4));
        TestUtils.assertEquals2(2, this.deque.set(2, this.k5));

        TestUtils.assertListEquals(this.deque.toArray(), 3, 4, 5);
    }

    /* */
    @Test
    public void testGetAndGrowth()
    {
        final int maxGrowth = 10;
        final int count = 500;

        this.deque = new KTypeArrayDeque<KType>(0,
                new BoundedProportionalArraySizingStrategy(5, maxGrowth, 2));

        for (int i = 0; i < count; i++) {
            this.deque.add(cast(i));
        }

        Assert.assertEquals(count, this.deque.size());

        for (int i = 0; i < count; i++) {
            TestUtils.assertEquals2(cast(i), this.deque.get(i));
        }

        Assert.assertTrue("Buffer size: 510 <= " + this.deque.buffer.length,
                this.deque.buffer.length <= count + maxGrowth);
    }

    /* */
    @Test
    public void testRemove()
    {
        this.deque.addLast(asArray(0, 1, 2, 3, 4, 5, 6, 7, 8));

        Assert.assertEquals(0, castType(this.deque.remove(0)));
        Assert.assertEquals(3, castType(this.deque.remove(2)));
        Assert.assertEquals(2, castType(this.deque.remove(1)));
        Assert.assertEquals(6, castType(this.deque.remove(3)));

        TestUtils.assertListEquals(this.deque.toArray(), 1, 4, 5, 7, 8);
    }

    /* */
    @Test
    public void testRemoveFirstOccurrence()
    {
        final int modulo = 10;
        final int count = 10000;
        this.sequence.clear();

        for (int i = 0; i < count; i++)
        {
            //add in head an in queue indifferently
            if (i % 3 == 0) {
                this.deque.addFirst(cast(i % modulo));
                this.sequence.insert(0, cast(i % modulo));
            }
            else {
                this.deque.addLast(cast(i % modulo));
                this.sequence.add(cast(i % modulo));
            }
        }

        //at that point, both IndexedContainers are the same
        TestUtils.assertListEquals(this.deque.toArray(), this.sequence.toArray());

        final Random rnd = new Random(0xdeadbeef);

        //remove values randomly
        for (int i = 0; i < 500; i++)
        {
            final KType k = cast(rnd.nextInt(modulo));

            //both ArrayList and ArrayDequeue returns the same index:
            Assert.assertEquals(" at i = " + i,
                    this.sequence.removeFirst(k),
                    this.deque.removeFirst(k));
        }

        TestUtils.assertListEquals(this.deque.toArray(), this.sequence.toArray());

        //non-existent element
        Assert.assertTrue(0 > this.deque.removeFirst(cast(modulo + 1)));

        //now existing
        this.deque.addLast(cast(modulo + 1));
        Assert.assertTrue(0 <= this.deque.removeFirst(cast(modulo + 1)));
    }

    /* */
    @Test
    public void testEquals()
    {
        final int modulo = 127;
        final int count = 15000;

        final KTypeArrayDeque<KType> deque2 = KTypeArrayDeque.newInstance();

        for (int i = 0; i < count; i++)
        {
            //add in head an in queue indifferently
            if (i % 3 == 0) {
                this.deque.addFirst(cast(i % modulo));
                deque2.addFirst(cast(i % modulo));
            }
            else {
                this.deque.addLast(cast(i % modulo));
                deque2.addLast(cast(i % modulo));
            }
        }

        //both elements by elements are equal
        TestUtils.assertListEquals(this.deque.toArray(), deque2.toArray());

        //Both dequeue are indeed equal
        Assert.assertTrue(this.deque.equals(deque2));
    }

    /* */
    @Test
    public void testIndexedContainerEquals()
    {
        final int modulo = 127;
        final int count = 10000;
        this.sequence.clear();

        for (int i = 0; i < count; i++)
        {
            //add in head an in queue indifferently
            if (i % 3 == 0) {
                this.deque.addFirst(cast(i % modulo));
                this.sequence.insert(0, cast(i % modulo));
            }
            else {
                this.deque.addLast(cast(i % modulo));
                this.sequence.add(cast(i % modulo));
            }
        }

        //both elements by elements are equal
        TestUtils.assertListEquals(this.deque.toArray(), this.sequence.toArray());

        //The array list and dequeue are indeed equal
        Assert.assertTrue(this.deque.equals(this.sequence));
    }

    /* */
    @Test
    public void testRemoveLastOccurrence()
    {
        final int modulo = 10;
        final int count = 10000;
        this.sequence.clear();

        for (int i = 0; i < count; i++)
        {
            //add in head an in queue indifferently
            if (i % 3 == 0) {
                this.deque.addFirst(cast(i % modulo));
                this.sequence.insert(0, cast(i % modulo));
            }
            else {
                this.deque.addLast(cast(i % modulo));
                this.sequence.add(cast(i % modulo));
            }
        }

        //at that point, both IndexedContainers are the same
        TestUtils.assertListEquals(this.deque.toArray(), this.sequence.toArray());

        final Random rnd = new Random(0x11223344);

        //remove values randomly
        for (int i = 0; i < 500; i++)
        {
            final KType k = cast(rnd.nextInt(modulo));

            //both ArrayList and ArrayDequeue returns the same index:
            Assert.assertEquals(" at i = " + i,
                    this.sequence.removeLast(k),
                    this.deque.removeLast(k));
        }

        TestUtils.assertListEquals(this.deque.toArray(), this.sequence.toArray());

        //non existent element
        Assert.assertTrue(0 > this.deque.removeLast(cast(modulo + 1)));

        //now existing
        this.deque.addFirst(cast(modulo + 1));
        Assert.assertTrue(0 <= this.deque.removeLast(cast(modulo + 1)));
    }

    /* */
    @Test
    public void testRemoveAllOccurrences()
    {
        this.deque.addLast(asArray(0, 1, 2, 1, 0, 3, 0));

        Assert.assertEquals(0, this.deque.removeAll(this.k4));
        Assert.assertEquals(3, this.deque.removeAll(this.k0));
        TestUtils.assertListEquals(this.deque.toArray(), 1, 2, 1, 3);
        Assert.assertEquals(1, this.deque.removeAll(this.k3));
        TestUtils.assertListEquals(this.deque.toArray(), 1, 2, 1);
        Assert.assertEquals(2, this.deque.removeAll(this.k1));
        TestUtils.assertListEquals(this.deque.toArray(), 2);
        Assert.assertEquals(1, this.deque.removeAll(this.k2));
        Assert.assertEquals(0, this.deque.size());
    }

    /* */
    @Test
    public void testRemoveAllInLookupContainer()
    {
        this.deque.addLast(asArray(0, 1, 2, 1, 0));

        final KTypeOpenHashSet<KType> set = KTypeOpenHashSet.newInstance();
        set.add(asArray(0, 2));

        Assert.assertEquals(3, this.deque.removeAll(set));
        Assert.assertEquals(0, this.deque.removeAll(set));

        TestUtils.assertListEquals(this.deque.toArray(), 1, 1);
    }

    /* */
    @Test
    public void testRemoveAllWithPredicate()
    {
        this.deque.addLast(newArray(this.k0, this.k1, this.k2, this.k1, this.k4));

        Assert.assertEquals(3, this.deque.removeAll(new KTypePredicate<KType>()
                {
            @Override
            public boolean apply(final KType v)
            {
                return v == KTypeArrayDequeTest.this.key1 || v == KTypeArrayDequeTest.this.key2;
            };
                }));

        TestUtils.assertListEquals(this.deque.toArray(), 0, 4);
    }

    /* */
    @Test
    public void testRemoveAllWithPredicateInterrupted()
    {
        this.deque.addLast(newArray(this.k0, this.k1, this.k2, this.k1, this.k4));

        final RuntimeException t = new RuntimeException();

        try
        {
            //the assert below should never be triggered because of the exception
            //so give it an invalid value in case the thing terminates  = initial size
            Assert.assertEquals(5, this.deque.removeAll(new KTypePredicate<KType>()
                    {
                @Override
                public boolean apply(final KType v)
                {
                    if (v == KTypeArrayDequeTest.this.key2) {
                        throw t;
                    }
                    return v == KTypeArrayDequeTest.this.key1;
                };
                    }));
            Assert.fail();
        }
        catch (final RuntimeException e)
        {
            // Make sure it's really our exception...
            if (e != t) {
                throw e;
            }
        }

        // And check if the deque is in consistent state.
        TestUtils.assertListEquals(this.deque.toArray(), 0, this.key2, this.key1, 4);
        Assert.assertEquals(4, this.deque.size());
    }

    /* */
    @Test
    public void testClear()
    {
        this.deque.addLast(this.k1);
        this.deque.addLast(this.k2);
        this.deque.addFirst(this.k3);
        this.deque.clear();
        Assert.assertEquals(0, this.deque.size());
        Assert.assertEquals(0, this.deque.head);
        Assert.assertEquals(0, this.deque.tail);

        this.deque.addLast(this.k1);
        TestUtils.assertListEquals(this.deque.toArray(), 1);
    }

    /* */
    @Test
    public void testRelease()
    {
        this.deque.addLast(this.k1);
        this.deque.addLast(this.k2);
        this.deque.addFirst(this.k3);
        this.deque.release();
        Assert.assertEquals(0, this.deque.size());
        Assert.assertEquals(0, this.deque.head);
        Assert.assertEquals(0, this.deque.tail);

        this.deque.addLast(this.k1);
        TestUtils.assertListEquals(this.deque.toArray(), 1);
    }

    /* */
    @Test
    public void testIterable()
    {
        this.deque.addLast(this.sequence);

        int count = 0;
        for (final KTypeCursor<KType> cursor : this.deque)
        {
            TestUtils.assertEquals2(this.sequence.buffer[count], cursor.value);
            TestUtils.assertEquals2(this.deque.buffer[cursor.index], cursor.value);
            count++;
        }
        Assert.assertEquals(count, this.deque.size());
        Assert.assertEquals(count, this.sequence.size());

        count = 0;
        this.deque.clear();
        for (@SuppressWarnings("unused")
        final KTypeCursor<KType> cursor : this.deque)
        {
            count++;
        }
        Assert.assertEquals(0, count);
    }

    /* */
    @Test
    public void testIterator()
    {
        this.deque.addLast(asArray(0, 1, 2, 3));

        final Iterator<KTypeCursor<KType>> iterator = this.deque.iterator();
        int count = 0;
        while (iterator.hasNext())
        {
            iterator.hasNext();
            iterator.hasNext();
            iterator.hasNext();
            iterator.next();
            count++;
        }
        Assert.assertEquals(count, this.deque.size());

        this.deque.clear();
        Assert.assertFalse(this.deque.iterator().hasNext());
    }

    /* */
    @Test
    public void testDescendingIterator()
    {
        this.deque.addLast(this.sequence);

        int index = this.sequence.size() - 1;
        for (final Iterator<KTypeCursor<KType>> i = this.deque.descendingIterator(); i.hasNext();)
        {
            final KTypeCursor<KType> cursor = i.next();
            TestUtils.assertEquals2(this.sequence.buffer[index], cursor.value);
            TestUtils.assertEquals2(this.deque.buffer[cursor.index], cursor.value);
            index--;
        }
        Assert.assertEquals(-1, index);

        this.deque.clear();
        Assert.assertFalse(this.deque.descendingIterator().hasNext());
    }

    /* */
    @Test
    public void testForEachWithProcedure()
    {
        this.deque.addLast(this.sequence);

        final IntHolder count = new IntHolder();
        this.deque.forEach(new KTypeProcedure<KType>() {
            int index = 0;

            @Override
            public void apply(final KType v)
            {
                TestUtils.assertEquals2(KTypeArrayDequeTest.this.sequence.buffer[this.index++], v);
                count.value++;
            }
        });
        Assert.assertEquals(count.value, this.deque.size());
    }

    /* */
    @Test
    public void testDescendingForEachWithProcedure()
    {
        this.deque.addLast(this.sequence);

        final IntHolder count = new IntHolder();
        this.deque.descendingForEach(new KTypeProcedure<KType>() {
            int index = KTypeArrayDequeTest.this.sequence.size();

            @Override
            public void apply(final KType v)
            {
                TestUtils.assertEquals2(KTypeArrayDequeTest.this.sequence.buffer[--this.index], v);
                count.value++;
            }
        });
        Assert.assertEquals(count.value, this.deque.size());
    }

    /* */
    @Test
    public void testForEachWithPredicate()
    {
        this.deque.addLast(asArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));

        final int lastValue = this.deque.forEach(new KTypePredicate<KType>() {
            int value = 0;
            int index = 0;

            @Override
            public boolean apply(final KType v)
            {
                TestUtils.assertEquals2(v, KTypeArrayDequeTest.this.deque.get(this.index));
                this.value = castType(v);

                if (this.value == 6) {

                    return false;
                }

                this.index++;

                return true;
            }
        }).value;

        Assert.assertEquals(lastValue, 6);
    }

    /* */
    @Test
    public void testForEachWithPredicateAllwaysTrue()
    {
        this.deque.addLast(asArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));

        final int lastValue = this.deque.forEach(new KTypePredicate<KType>() {
            int value = 0;
            int index = 0;

            @Override
            public boolean apply(final KType v)
            {
                TestUtils.assertEquals2(v, KTypeArrayDequeTest.this.deque.get(this.index));
                this.value = castType(v);

                this.index++;

                return true;
            }
        }).value;

        Assert.assertEquals(lastValue, 12);
    }

    /* */
    @Test
    public void testDescendingForEachWithPredicate()
    {
        this.deque.addLast(asArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));

        final int lastValue = this.deque.descendingForEach(new KTypePredicate<KType>() {
            int value = 0;
            int index = KTypeArrayDequeTest.this.deque.size() - 1;

            @Override
            public boolean apply(final KType v)
            {
                TestUtils.assertEquals2(v, KTypeArrayDequeTest.this.deque.get(this.index));
                this.value = castType(v);

                if (this.value == 9) {

                    return false;
                }

                this.index--;

                return true;
            }
        }).value;

        Assert.assertEquals(lastValue, 9);
    }

    /* */
    @Test
    public void testDescendingForEachWithPredicateAllwaysTrue()
    {
        this.deque.addLast(asArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));

        final int lastValue = this.deque.descendingForEach(new KTypePredicate<KType>() {
            int value = 0;
            int index = KTypeArrayDequeTest.this.deque.size() - 1;

            @Override
            public boolean apply(final KType v)
            {
                TestUtils.assertEquals2(v, KTypeArrayDequeTest.this.deque.get(this.index));
                this.value = castType(v);

                this.index--;

                return true;
            }
        }).value;

        Assert.assertEquals(lastValue, 1);
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testAgainstArrayDeque()
    {
        final Random rnd = new Random(78461644457454L);
        final int rounds = 10000;
        final int modulo = 100;

        final ArrayDeque<KType> ad = new ArrayDeque<KType>();

        for (int i = 0; i < rounds; i++)
        {
            final KType k = cast(rnd.nextInt(modulo));

            final int op = rnd.nextInt(8);
            if (op < 2)
            {
                this.deque.addFirst(k);
                ad.addFirst(k);
            }
            else if (op < 4)
            {
                this.deque.addLast(k);
                ad.addLast(k);
            }
            else if (op < 5 && ad.size() > 0)
            {
                this.deque.removeLast();
                ad.removeLast();
            }
            else if (op < 6 && ad.size() > 0)
            {
                this.deque.removeLast();
                ad.removeLast();
            }
            else if (op < 7)
            {
                Assert.assertEquals(
                        ad.removeFirstOccurrence(k),
                        this.deque.removeFirst(k) >= 0);
            }
            else if (op < 8)
            {
                Assert.assertEquals(
                        ad.removeLastOccurrence(k),
                        this.deque.removeLast(k) >= 0);
            }
            Assert.assertEquals(ad.size(), this.deque.size());
        }

        Assert.assertArrayEquals(ad.toArray(), this.deque.toArray());
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testAgainstArrayDequeVariousTailHeadPositions()
    {
        this.deque.clear();
        this.deque.head = this.deque.tail = 2;
        testAgainstArrayDeque();

        this.deque.clear();
        this.deque.head = this.deque.tail = this.deque.buffer.length - 2;
        testAgainstArrayDeque();

        this.deque.clear();
        this.deque.head = this.deque.tail = this.deque.buffer.length / 2;
        testAgainstArrayDeque();
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testHashCodeEquals()
    {
        final KTypeArrayDeque<KType> l0 = KTypeArrayDeque.newInstance();
        Assert.assertEquals(1, l0.hashCode());
        Assert.assertEquals(l0, KTypeArrayDeque.from());

        final KTypeArrayDeque<KType> l1 = KTypeArrayDeque.from(this.k1, this.k2, this.k3);
        final KTypeArrayDeque<KType> l2 = KTypeArrayDeque.from(this.k1, this.k2);
        l2.addLast(this.k3);

        Assert.assertEquals(l1.hashCode(), l2.hashCode());
        Assert.assertEquals(l1, l2);
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    @Test
    public void testHashCodeWithNulls()
    {
        final KTypeArrayDeque<KType> l1 = KTypeArrayDeque.from(this.k1, null, this.k3);
        final KTypeArrayDeque<KType> l2 = KTypeArrayDeque.from(this.k1, null, this.k3);
        Assert.assertEquals(l1.hashCode(), l2.hashCode());
        Assert.assertEquals(l1, l2);
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testToArrayWithClass()
    {
        final KTypeArrayDeque<Integer> l1 = KTypeArrayDeque.from(1, 2, 3);
        final Integer[] result = l1.toArray(Integer.class);
        Assert.assertArrayEquals(new Integer[] { 1, 2, 3 }, result); // dummy
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    @Test
    public void testToArray()
    {
        final KTypeArrayDeque<KType> l1 = KTypeArrayDeque.from(this.k1, this.k2, this.k3);
        final Object[] result = l1.toArray();
        Assert.assertArrayEquals(new Object[] { this.k1, this.k2, this.k3 }, result); // dummy
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testClone()
    {
        this.deque.addLast(this.key1, this.key2, this.key3);

        final KTypeArrayDeque<KType> cloned = this.deque.clone();
        cloned.removeAll(this.key1);

        TestUtils.assertSortedListEquals(this.deque.toArray(), this.key1, this.key2, this.key3);
        TestUtils.assertSortedListEquals(cloned.toArray(), this.key2, this.key3);
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testToString()
    {
        Assert.assertEquals("["
                + this.key1 + ", "
                + this.key2 + ", "
                + this.key3 + "]", KTypeArrayDeque.from(this.key1, this.key2, this.key3).toString());
    }

    @Test
    public void testPooledIteratorForEach()
    {
        // Unbroken for-each loop
        final int TEST_SIZE = 5000;
        final long TEST_ROUNDS = 100;

        final KTypeArrayDeque<KType> testContainer = createDequeWithOrderedData(TEST_SIZE);

        final long checksum = testContainer.forEach(new KTypeProcedure<KType>() {

            long count;

            @Override
            public void apply(final KType value)
            {
                this.count += castType(value);
            }
        }).count;

        long testValue = 0;
        final long initialPoolSize = testContainer.valueIteratorPool.size();

        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            //for-each in test :
            testValue = 0;
            for (final KTypeCursor<KType> cursor : testContainer)
            {
                //we consume 1 iterator for this loop
                Assert.assertEquals(initialPoolSize - 1, testContainer.valueIteratorPool.size());

                testValue += castType(cursor.value);
            }

            //check checksum the iteration
            Assert.assertEquals(checksum, testValue);

            //iterator is returned to its pool
            Assert.assertEquals(initialPoolSize, testContainer.valueIteratorPool.size());
        } //end for rounds
    }

    @Test
    public void testPooledIteratorBrokenForEach()
    {
        final int TEST_SIZE = 5000;
        final long TEST_ROUNDS = 100;

        final KTypeArrayDeque<KType> testContainer = createDequeWithOrderedData(TEST_SIZE);

        int count = 0;
        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            //for-each in test :
            final long initialPoolSize = testContainer.valueIteratorPool.size();

            count = 0;
            for (final KTypeCursor<KType> cursor : testContainer)
            {
                this.guard += castType(cursor.value);
                //we consume 1 iterator for this loop, but reallocs can happen,
                //so we can only say its != initialPoolSize
                Assert.assertTrue(initialPoolSize != testContainer.valueIteratorPool.size());

                //brutally interrupt in the middle
                if (count > TEST_SIZE / 2)
                {
                    break;
                }

                count++;
            } //end for-each

            //iterator is NOT returned to its pool, due to the break.
            //reallocation could happen, so that the only testable thing
            //is that the size is != full pool
            Assert.assertTrue(initialPoolSize != testContainer.valueIteratorPool.size());
        } //end for rounds

        //Due to policy of the Iterator pool, the intended pool never get bigger that some limit
        //despite the Iterator leak.
        Assert.assertTrue(testContainer.valueIteratorPool.capacity() < IteratorPool.getMaxPoolSize() + 1);
    }

    @Test
    public void testPooledIteratorFullIteratorLoop()
    {
        final int TEST_SIZE = 5000;
        final long TEST_ROUNDS = 100;

        final KTypeArrayDeque<KType> testContainer = createDequeWithOrderedData(TEST_SIZE);

        final long checksum = testContainer.forEach(new KTypeProcedure<KType>() {

            long count;

            @Override
            public void apply(final KType value)
            {
                this.count += castType(value);
            }
        }).count;

        long testValue = 0;
        final int startingPoolSize = testContainer.valueIteratorPool.size();
        final int startingPoolDescendingSize = testContainer.descendingValueIteratorPool.size();

        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            //A) Classical iterator loop, with manually allocated Iterator
            int initialPoolSize = testContainer.valueIteratorPool.size();

            Iterator<KTypeCursor<KType>> loopIterator = testContainer.iterator();

            Assert.assertEquals(initialPoolSize - 1, testContainer.valueIteratorPool.size());

            testValue = 0;

            while (loopIterator.hasNext())
            {
                testValue += castType(loopIterator.next().value);
            } //end IteratorLoop

            //iterator is returned automatically to its pool, by normal iteration termination
            Assert.assertEquals(initialPoolSize, testContainer.valueIteratorPool.size());

            //checksum
            Assert.assertEquals(checksum, testValue);

            //B) Descending iterator loop
            initialPoolSize = testContainer.descendingValueIteratorPool.size();

            loopIterator = testContainer.descendingIterator();

            Assert.assertEquals(initialPoolSize - 1, testContainer.descendingValueIteratorPool.size());

            testValue = 0;
            while (loopIterator.hasNext())
            {
                testValue += castType(loopIterator.next().value);
            } //end IteratorLoop

            //iterator is returned automatically to its pool, by normal iteration termination
            Assert.assertEquals(initialPoolSize, testContainer.descendingValueIteratorPool.size());

            //checksum
            Assert.assertEquals(checksum, testValue);

        } //end for rounds

        // pool initial size is untouched anyway
        Assert.assertEquals(startingPoolSize, testContainer.valueIteratorPool.size());
        Assert.assertEquals(startingPoolDescendingSize, testContainer.descendingValueIteratorPool.size());
    }

    @Test
    public void testPooledIteratorBrokenIteratorLoop()
    {
        final int TEST_SIZE = 5000;
        final long TEST_ROUNDS = 100;

        final KTypeArrayDeque<KType> testContainer = createDequeWithOrderedData(TEST_SIZE);
        final int startingPoolSize = testContainer.valueIteratorPool.size();
        final int startingPoolDescendingSize = testContainer.descendingValueIteratorPool.size();

        int count = 0;
        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            //A) Classical iterator loop, with manually allocated Iterator, forward
            long initialPoolSize = testContainer.valueIteratorPool.size();

            Iterator<KTypeCursor<KType>> loopIterator = testContainer.iterator();

            Assert.assertEquals(initialPoolSize - 1, testContainer.valueIteratorPool.size());

            count = 0;
            while (loopIterator.hasNext())
            {
                this.guard += castType(loopIterator.next().value);

                //brutally interrupt in the middle
                if (count > TEST_SIZE / 2)
                {
                    break;
                }
                count++;
            } //end IteratorLoop

            //iterator is NOT returned to its pool, due to the break.
            Assert.assertEquals(initialPoolSize - 1, testContainer.valueIteratorPool.size());

            //manual return to the pool
            ((KTypeArrayDeque<KType>.ValueIterator) loopIterator).release();

            //now the pool is restored
            Assert.assertEquals(initialPoolSize, testContainer.valueIteratorPool.size());

            //B) Descending iteration
            initialPoolSize = testContainer.descendingValueIteratorPool.size();

            loopIterator = testContainer.descendingIterator();

            Assert.assertEquals(initialPoolSize - 1, testContainer.descendingValueIteratorPool.size());

            count = 0;
            while (loopIterator.hasNext())
            {
                this.guard += castType(loopIterator.next().value);

                //brutally interrupt in the middle
                if (count > TEST_SIZE / 2)
                {
                    break;
                }
                count++;
            } //end IteratorLoop

            //iterator is NOT returned to its pool, due to the break.
            Assert.assertEquals(initialPoolSize - 1, testContainer.descendingValueIteratorPool.size());

            //manual return to the pool
            ((KTypeArrayDeque<KType>.DescendingValueIterator) loopIterator).release();

            //now the pool is restored
            Assert.assertEquals(initialPoolSize, testContainer.descendingValueIteratorPool.size());

        } //end for rounds

        // pool initial size is untouched anyway
        Assert.assertEquals(startingPoolSize, testContainer.valueIteratorPool.size());
        Assert.assertEquals(startingPoolDescendingSize, testContainer.descendingValueIteratorPool.size());
    }

    @Test
    public void testPooledIteratorExceptionIteratorLoop()
    {
        final int TEST_SIZE = 5000;
        final long TEST_ROUNDS = 100;

        final KTypeArrayDeque<KType> testContainer = createDequeWithOrderedData(TEST_SIZE);

        final long checksum = testContainer.forEach(new KTypeProcedure<KType>() {

            long count;

            @Override
            public void apply(final KType value)
            {
                this.count += castType(value);
            }
        }).count;

        final int startingPoolSize = testContainer.valueIteratorPool.size();

        int count = 0;
        Iterator<KTypeCursor<KType>> loopIterator = null;

        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            try
            {
                loopIterator = testContainer.iterator();
                Assert.assertEquals(startingPoolSize - 1, testContainer.valueIteratorPool.size());

                this.guard = 0;
                count = 0;
                while (loopIterator.hasNext())
                {
                    this.guard += castType(loopIterator.next().value);

                    //brutally interrupt in the middle some of the loops, but not all
                    if (round > TEST_ROUNDS / 2 && count > TEST_SIZE / 2)
                    {
                        throw new Exception("Oups some problem in the loop occured");
                    }
                    count++;
                } //end while

                //iterator is returned to its pool in case of normal loop termination
                Assert.assertEquals(startingPoolSize, testContainer.valueIteratorPool.size());
                Assert.assertEquals(checksum, this.guard);

            }
            catch (final Exception e)
            {
                //iterator is NOT returned to its pool because of the exception
                Assert.assertEquals(startingPoolSize - 1, testContainer.valueIteratorPool.size());

                //manual return to the pool then
                ((KTypeArrayDeque<KType>.ValueIterator) loopIterator).release();

                //now the pool is restored
                Assert.assertEquals(startingPoolSize, testContainer.valueIteratorPool.size());
            }
        } //end for rounds

        // pool initial size is untouched anyway
        Assert.assertEquals(startingPoolSize, testContainer.valueIteratorPool.size());
    }

    @Repeat(iterations = 100)
    @Test
    public void testSort()
    {
        //natural ordering comparator
        final KTypeComparator<KType> comp = new KTypeComparator<KType>() {

            @Override
            public int compare(final KType e1, final KType e2)
            {
                int res = 0;

                if (castType(e1) < castType(e2))
                {
                    res = -1;
                }
                else if (castType(e1) > castType(e2))
                {
                    res = 1;
                }

                return res;
            }
        };

        final int TEST_SIZE = (int) 1e4;

        //get a new seed for the current iteration
        final long currentSeed = RandomizedTest.randomLong();

        final int upperRange = RandomizedTest.randomInt(TEST_SIZE);
        final int lowerRange = RandomizedTest.randomInt(upperRange);

        //A) Sort a deque of random values of primitive types

        //A-1) full sort
        KTypeArrayDeque<KType> primitiveDeque = creatDequeWithRandomData(TEST_SIZE, currentSeed);
        KTypeArrayDeque<KType> primitiveDequeOriginal = creatDequeWithRandomData(TEST_SIZE, currentSeed);
        primitiveDeque.sort();
        assertOrder(primitiveDequeOriginal, primitiveDeque, 0, primitiveDequeOriginal.size());
        //A-2) Partial sort
        primitiveDeque = creatDequeWithRandomData(TEST_SIZE, currentSeed);
        primitiveDequeOriginal = creatDequeWithRandomData(TEST_SIZE, currentSeed);
        primitiveDeque.sort(lowerRange, upperRange);
        assertOrder(primitiveDequeOriginal, primitiveDeque, lowerRange, upperRange);

        //B) Sort with Comparator
        //B-1) Full sort
        KTypeArrayDeque<KType> comparatorDeque = creatDequeWithRandomData(TEST_SIZE, currentSeed);
        KTypeArrayDeque<KType> comparatorDequeOriginal = creatDequeWithRandomData(TEST_SIZE, currentSeed);
        comparatorDeque.sort(comp);
        assertOrder(comparatorDequeOriginal, comparatorDeque, 0, comparatorDequeOriginal.size());
        //B-2) Partial sort
        comparatorDeque = creatDequeWithRandomData(TEST_SIZE, currentSeed);
        comparatorDequeOriginal = creatDequeWithRandomData(TEST_SIZE, currentSeed);
        comparatorDeque.sort(lowerRange, upperRange, comp);
        assertOrder(comparatorDequeOriginal, comparatorDeque, lowerRange, upperRange);
    }

    @Repeat(iterations = 10)
    @Test
    public void testPreallocatedSize()
    {
        final Random randomVK = RandomizedTest.getRandom();
        //Test that the container do not resize if less that the initial size

        //1) Choose a random number of elements
        /*! #if ($TemplateOptions.isKType("GENERIC", "INT", "LONG", "FLOAT", "DOUBLE")) !*/
        final int PREALLOCATED_SIZE = randomVK.nextInt(10000);
        /*!
            #elseif ($TemplateOptions.isKType("SHORT", "CHAR"))
             int PREALLOCATED_SIZE = randomVK.nextInt(10000);
            #else
              int PREALLOCATED_SIZE = randomVK.nextInt(10000);
            #end !*/

        //2) Preallocate to PREALLOCATED_SIZE :
        final KTypeArrayDeque<KType> newDequeue = KTypeArrayDeque.newInstance(PREALLOCATED_SIZE);

        //computed real capacity
        final int realCapacity = newDequeue.capacity();

        //3) Add PREALLOCATED_SIZE different values. At the end, size() must be == realCapacity,
        //and internal buffer/allocated must not have changed of size
        final int contructorBufferSize = newDequeue.buffer.length;

        Assert.assertEquals(contructorBufferSize, newDequeue.buffer.length);

        for (int i = 0; i < 1.5 * realCapacity; i++) {

            newDequeue.addLast(cast(randomVK.nextInt()));

            //internal size has not changed until realCapacity
            if (newDequeue.size() <= realCapacity) {

                Assert.assertEquals(contructorBufferSize, newDequeue.buffer.length);
            }

            if (contructorBufferSize < newDequeue.buffer.length) {
                //The container as just reallocated, its actual size must be not too far from the previous capacity:
                Assert.assertTrue("Container as reallocated at size = " + newDequeue.size() + " with previous capacity = " + realCapacity,
                        (newDequeue.size() - realCapacity) <= 2);
                break;
            }
        }
    }

    @Repeat(iterations = 10)
    @Test
    public void testNoOverallocation() {

        final Random randomVK = RandomizedTest.getRandom();
        //Test that the container do not resize if less that the initial size

        //1) Choose a random number of elements
        /*! #if ($TemplateOptions.isKType("GENERIC", "INT", "LONG", "FLOAT", "DOUBLE")) !*/
        final int PREALLOCATED_SIZE = randomVK.nextInt(10000);
        /*!
            #elseif ($TemplateOptions.isKType("SHORT", "CHAR"))
             int PREALLOCATED_SIZE = randomVK.nextInt(10000);
            #else
              int PREALLOCATED_SIZE = randomVK.nextInt(10000);
            #end !*/

        //2) Preallocate to PREALLOCATED_SIZE :
        final KTypeArrayDeque<KType> refContainer = new KTypeArrayDeque<KType>(PREALLOCATED_SIZE);

        final int refCapacity = refContainer.capacity();

        //3) Fill with random values, random number of elements below preallocation
        final int nbElements = RandomizedTest.randomInt(PREALLOCATED_SIZE - 3);

        for (int i = 0; i < nbElements; i++) {

            refContainer.add(cast(randomVK.nextInt()));
        }

        final int nbRefElements = refContainer.size();

        Assert.assertEquals(refCapacity, refContainer.capacity());

        //4) Duplicate by copy-construction and/or clone
        KTypeArrayDeque<KType> clonedContainer = refContainer.clone();
        KTypeArrayDeque<KType> copiedContainer = new KTypeArrayDeque<KType>(refContainer);

        //Duplicated containers must be equal to their origin, with a capacity no bigger than the original.
        Assert.assertEquals(nbRefElements, clonedContainer.size());
        Assert.assertEquals(nbRefElements, copiedContainer.size());
        Assert.assertTrue(refCapacity >= clonedContainer.capacity());
        Assert.assertTrue(refCapacity >= copiedContainer.capacity());
        Assert.assertTrue(clonedContainer.equals(refContainer));
        Assert.assertTrue(copiedContainer.equals(refContainer));

        //Maybe we were lucky, iterate duplication over itself several times
        for (int j = 0; j < 5; j++) {

            clonedContainer = clonedContainer.clone();
            copiedContainer = new KTypeArrayDeque<KType>(copiedContainer);

            Assert.assertEquals(nbRefElements, clonedContainer.size());
            Assert.assertEquals(nbRefElements, copiedContainer.size());
            Assert.assertTrue(refCapacity >= clonedContainer.capacity());
            Assert.assertTrue(refCapacity >= copiedContainer.capacity());
            Assert.assertTrue(clonedContainer.equals(refContainer));
            Assert.assertTrue(copiedContainer.equals(refContainer));
        }
    }

    private KTypeArrayDeque<KType> createDequeWithOrderedData(final int size)
    {
        final KTypeArrayDeque<KType> newArray = KTypeArrayDeque.newInstance();

        for (int i = 0; i < size; i++)
        {
            newArray.addLast(cast(i));
        }

        return newArray;
    }

    private KTypeArrayDeque<KType> creatDequeWithRandomData(final int size, final long randomSeed)
    {
        final Random prng = new Random(randomSeed);

        final KTypeArrayDeque<KType> newDeque = KTypeArrayDeque.newInstance();

        while (newDeque.size() < size)
        {
            final KType newValueToInsert = cast(prng.nextInt());
            final boolean insertInTail = prng.nextInt() % 7 == 0;
            final boolean deleteHead = prng.nextInt() % 17 == 0;

            if (deleteHead && !newDeque.isEmpty())
            {
                newDeque.removeFirst();
            }
            else if (insertInTail)
            {
                newDeque.addLast(newValueToInsert);
            }
            else
            {
                newDeque.addFirst(newValueToInsert);
            }
        }

        return newDeque;
    }

}
