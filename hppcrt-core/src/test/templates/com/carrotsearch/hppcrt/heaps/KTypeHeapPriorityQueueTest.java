package com.carrotsearch.hppcrt.heaps;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.carrotsearch.hppcrt.TestUtils.*;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.cursors.KTypeCursor;
import com.carrotsearch.hppcrt.heaps.KTypeHeapPriorityQueue.ValueIterator;
import com.carrotsearch.hppcrt.lists.KTypeArrayList;
import com.carrotsearch.hppcrt.mutables.IntHolder;
import com.carrotsearch.hppcrt.mutables.LongHolder;
import com.carrotsearch.hppcrt.predicates.KTypePredicate;
import com.carrotsearch.hppcrt.procedures.KTypeProcedure;
import com.carrotsearch.hppcrt.sets.KTypeOpenHashSet;
import com.carrotsearch.hppcrt.sorting.KTypeComparator;
import com.carrotsearch.hppcrt.sorting.KTypeSort;

/**
 * Unit tests for {@link KTypeHeapPriorityQueue}.
 */

//${TemplateOptions.doNotGenerateKType("BOOLEAN")}
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeHeapPriorityQueueTest<KType> extends AbstractKTypeTest<KType>
{
    /**
     * Per-test fresh initialized instance.
     */
    public KTypeHeapPriorityQueue<KType> prioq;

    public volatile long guard;

    @BeforeClass
    public static void configure()
    {
        IteratorPool.configureInitialPoolSize(8);
    }

    /* */
    @Before
    public void initialize()
    {
        this.prioq = new KTypeHeapPriorityQueue<KType>(null);
    }

    @After
    public void checkConsistency()
    {
        if (this.prioq != null)
        {
            //scan beyond the active zone
            //1-based indexing
            for (int i = this.prioq.elementsCount + 1; i < this.prioq.buffer.length; i++)
            {
                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                Assert.assertTrue(Intrinsics.<KType> defaultKTypeValue() == this.prioq.buffer[i]);
                /*! #end !*/
            }
        }

        //check heap property
        Assert.assertTrue(isMinHeap(this.prioq));
    }

    /* */
    @Test
    public void testInitiallyEmpty()
    {
        Assert.assertEquals(0, this.prioq.size());
    }

    /* */
    @Test
    public void testInsert()
    {
        this.prioq.add(this.key1);
        this.prioq.add(this.key2);
        assertPrioQueueEquals(this.prioq, 1, 2);
    }

    /* */
    @Test
    public void testInsert2()
    {
        this.prioq.add(this.key1);
        this.prioq.add(this.key2);
        this.prioq.add(this.key4);
        this.prioq.add(this.key3);
        assertPrioQueueEquals(this.prioq, 1, 2, 3, 4);
    }

    /* */
    @Test
    public void testAddAll()
    {
        final KTypeArrayList<KType> list2 = KTypeArrayList.newInstance();
        list2.add(asArray(0, 1, 2));

        final KTypeArrayList<KType> list3 = KTypeArrayList.newInstance();
        list3.add(asArray(9, 8, 7));

        this.prioq.addAll(list2);
        this.prioq.addAll(list2);
        this.prioq.addAll(list3);

        assertPrioQueueEquals(this.prioq, 0, 0, 1, 1, 2, 2, 7, 8, 9);

    }

    /* */
    @Test
    public void testPopTop()
    {
        insertElements(this.prioq, 10, 9, 8, 7, 6, 5, 4, 3);
        assertPrioQueueEquals(this.prioq, 3, 4, 5, 6, 7, 8, 9, 10);
        Assert.assertEquals(8, this.prioq.size());
        Assert.assertTrue(isMinHeap(this.prioq));
        Assert.assertFalse(this.prioq.isEmpty());

        Assert.assertEquals(3, castType(this.prioq.popTop()));
        assertPrioQueueEquals(this.prioq, 4, 5, 6, 7, 8, 9, 10);
        Assert.assertEquals(7, this.prioq.size());
        Assert.assertEquals(4, castType(this.prioq.top()));
        Assert.assertTrue(isMinHeap(this.prioq));
        Assert.assertFalse(this.prioq.isEmpty());

        Assert.assertEquals(4, castType(this.prioq.popTop()));
        assertPrioQueueEquals(this.prioq, 5, 6, 7, 8, 9, 10);
        Assert.assertEquals(6, this.prioq.size());
        Assert.assertEquals(5, castType(this.prioq.top()));
        Assert.assertTrue(isMinHeap(this.prioq));
        Assert.assertFalse(this.prioq.isEmpty());

        Assert.assertEquals(5, castType(this.prioq.popTop()));
        assertPrioQueueEquals(this.prioq, 6, 7, 8, 9, 10);
        Assert.assertEquals(5, this.prioq.size());
        Assert.assertEquals(6, castType(this.prioq.top()));
        Assert.assertTrue(isMinHeap(this.prioq));
        Assert.assertFalse(this.prioq.isEmpty());

        Assert.assertEquals(6, castType(this.prioq.popTop()));
        assertPrioQueueEquals(this.prioq, 7, 8, 9, 10);
        Assert.assertEquals(4, this.prioq.size());
        Assert.assertEquals(7, castType(this.prioq.top()));
        Assert.assertTrue(isMinHeap(this.prioq));
        Assert.assertFalse(this.prioq.isEmpty());

        Assert.assertEquals(7, castType(this.prioq.popTop()));
        assertPrioQueueEquals(this.prioq, 8, 9, 10);
        Assert.assertEquals(3, this.prioq.size());
        Assert.assertEquals(8, castType(this.prioq.top()));
        Assert.assertTrue(isMinHeap(this.prioq));
        Assert.assertFalse(this.prioq.isEmpty());

        Assert.assertEquals(8, castType(this.prioq.popTop()));
        assertPrioQueueEquals(this.prioq, 9, 10);
        Assert.assertEquals(2, this.prioq.size());
        Assert.assertEquals(9, castType(this.prioq.top()));
        Assert.assertTrue(isMinHeap(this.prioq));
        Assert.assertFalse(this.prioq.isEmpty());

        Assert.assertEquals(9, castType(this.prioq.popTop()));
        assertPrioQueueEquals(this.prioq, 10);
        Assert.assertEquals(1, this.prioq.size());
        Assert.assertEquals(10, castType(this.prioq.top()));
        Assert.assertTrue(isMinHeap(this.prioq));
        Assert.assertFalse(this.prioq.isEmpty());

        Assert.assertEquals(10, castType(this.prioq.popTop()));
        Assert.assertEquals(0, this.prioq.size());
        Assert.assertTrue(this.prioq.isEmpty());

    }

    /* */
    @Test
    public void testRemoveAll()
    {
        insertElements(this.prioq, 0, 1, 0, 1, 0);

        Assert.assertEquals(0, this.prioq.removeAllOccurrences(this.k2));
        Assert.assertEquals(3, this.prioq.removeAllOccurrences(this.k0));
        assertPrioQueueEquals(this.prioq, 1, 1);

        Assert.assertEquals(2, this.prioq.removeAllOccurrences(this.k1));
        Assert.assertTrue(this.prioq.isEmpty());
    }

    /* */
    @Test
    public void testRemoveAllFromLookupContainer()
    {
        insertElements(this.prioq, 0, 1, 2, 1, 0);

        final KTypeOpenHashSet<KType> list2 = KTypeOpenHashSet.newInstance();
        list2.add(asArray(0, 2));

        Assert.assertEquals(3, this.prioq.removeAll(list2));
        Assert.assertEquals(0, this.prioq.removeAll(list2));

        assertPrioQueueEquals(this.prioq, 1, 1);
    }

    /* */
    @Test
    public void testRemoveAllWithPredicate()
    {
        insertElements(this.prioq, 0, 1, 2, 1, 4);

        Assert.assertEquals(3, this.prioq.removeAll(new KTypePredicate<KType>()
                {
            @Override
            public boolean apply(final KType v)
            {
                return v == KTypeHeapPriorityQueueTest.this.key1 || v == KTypeHeapPriorityQueueTest.this.key2;
            };
                }));

        assertPrioQueueEquals(this.prioq, 0, 4);
    }

    /* */
    @Test
    public void testRetainAllWithPredicate()
    {
        insertElements(this.prioq, 0, 1, 2, 1, 0);

        Assert.assertEquals(2, this.prioq.retainAll(new KTypePredicate<KType>()
                {
            @Override
            public boolean apply(final KType v)
            {
                return v == KTypeHeapPriorityQueueTest.this.key1 || v == KTypeHeapPriorityQueueTest.this.key2;
            };
                }));

        assertPrioQueueEquals(this.prioq, 1, 1, 2);
    }

    /* */
    @Test
    public void testRemoveAllWithPredicateInterrupted()
    {
        insertElements(this.prioq, 0, 1, 2, 1, 4);

        final RuntimeException t = new RuntimeException();
        try
        {
            //the assert below should never be triggered because of the exception
            //so give it an invalid value in case the thing terminates  = initial size
            Assert.assertEquals(5, this.prioq.removeAll(new KTypePredicate<KType>()
                    {
                @Override
                public boolean apply(final KType v)
                {
                    if (v == KTypeHeapPriorityQueueTest.this.key2) {
                        throw t;
                    }
                    return v == KTypeHeapPriorityQueueTest.this.key1;
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

        // And check if the list is in consistent state.
        assertPrioQueueEquals(this.prioq, 0, 1, 2, 4);
        Assert.assertEquals(4, this.prioq.size());
    }

    /* */
    @Test
    public void testGrowth()
    {
        final int count = 500;

        this.prioq = new KTypeHeapPriorityQueue<KType>(10);

        for (int i = 0; i < count; i++)
        {
            this.prioq.add(cast(i));
        }

        Assert.assertEquals(count, this.prioq.size());

    }

    /* */
    @Test
    public void testRemoveAllOccurences()
    {
        this.prioq.add(this.key1); //del
        this.prioq.add(this.key2);
        this.prioq.add(this.key1); //del
        this.prioq.add(this.key3);
        this.prioq.add(this.key7);
        this.prioq.add(this.key5);
        this.prioq.add(this.key1); //del
        this.prioq.add(this.key8);
        this.prioq.add(this.key2);

        Assert.assertEquals(9, this.prioq.size());

        final int nbRemoved = this.prioq.removeAllOccurrences(this.key1);

        Assert.assertEquals(3, nbRemoved);
        Assert.assertEquals(6, this.prioq.size());
        Assert.assertTrue(this.prioq.contains(this.key2));
        Assert.assertTrue(this.prioq.contains(this.key3));
        Assert.assertTrue(this.prioq.contains(this.key5));
        Assert.assertTrue(this.prioq.contains(this.key7));
        Assert.assertTrue(this.prioq.contains(this.key8));
    }

    /* */
    @Test
    public void testContains()
    {
        this.prioq.add(this.key1); //del
        this.prioq.add(this.key2);
        this.prioq.add(this.key1); //del
        this.prioq.add(this.key3);
        this.prioq.add(this.key7);
        this.prioq.add(this.key5);
        this.prioq.add(this.key1); //del
        this.prioq.add(this.key8);
        this.prioq.add(this.key2);

        Assert.assertEquals(9, this.prioq.size());

        Assert.assertTrue(this.prioq.contains(this.key1));
        Assert.assertTrue(this.prioq.contains(this.key2));
        Assert.assertTrue(this.prioq.contains(this.key3));
        Assert.assertFalse(this.prioq.contains(this.key4)); //not in heap
        Assert.assertTrue(this.prioq.contains(this.key5));
        Assert.assertFalse(this.prioq.contains(this.key6)); //not in heap
        Assert.assertTrue(this.prioq.contains(this.key7));
        Assert.assertTrue(this.prioq.contains(this.key8));
    }

    /* */
    @Test
    public void testIterable()
    {
        int count = (int) 1e5;

        //A) fill the prio queue
        this.prioq = new KTypeHeapPriorityQueue<KType>(10);

        for (int i = 0; i < count; i++)
        {
            this.prioq.add(cast(i));
        }

        Assert.assertEquals(count, this.prioq.size());

        //B) Iterate and check internal buffer
        final int[] testArray = new int[this.prioq.size()];

        count = 0;
        for (final KTypeCursor<KType> cursor : this.prioq)
        {
            testArray[count] = castType(cursor.value);
            count++;
        }
        Assert.assertEquals(count, this.prioq.size());

        //compare test Array
        Arrays.sort(testArray);
        assertPrioQueueEquals(this.prioq, testArray);

        //C) try to iterate a void Prio queue
        count = 0;
        this.prioq.clear();
        Assert.assertEquals(0, this.prioq.size());
        Assert.assertTrue(this.prioq.isEmpty());

        for (@SuppressWarnings("unused")
        final KTypeCursor<KType> cursor : this.prioq)
        {
            count++;
        }
        Assert.assertEquals(0, count);

    }

    /* */
    @Test
    public void testIterator()
    {
        insertElements(this.prioq, 0, 1, 2, 3);

        final KTypeHeapPriorityQueue<KType>.ValueIterator iterator = this.prioq.iterator();

        int count = 0;
        while (iterator.hasNext())
        {

            iterator.hasNext();
            iterator.hasNext();
            iterator.hasNext();
            iterator.next();
            count++;
        }
        Assert.assertEquals(count, this.prioq.size());

        this.prioq.clear();
        Assert.assertFalse(this.prioq.iterator().hasNext());
    }

    /* */
    @Test
    public void testForEachWithProcedure()
    {
        //A) Fill
        final int count = 12587;

        this.prioq = new KTypeHeapPriorityQueue<KType>(10);

        long checksum = 0;

        for (int i = 0; i < count; i++)
        {
            this.prioq.add(cast(i));
            checksum += castType(cast(i));
        }

        final LongHolder holder = new LongHolder();
        holder.value = 0;

        this.prioq.forEach(new KTypeProcedure<KType>() {

            @Override
            public void apply(final KType v)
            {
                holder.value += castType(v);
            }
        });

        Assert.assertEquals(checksum, holder.value);
    }

    /* */
    @Test
    public void testForEachReturnValueFromAnonymousClass()
    {
        //A) Fill
        final int count = 8741;

        this.prioq = new KTypeHeapPriorityQueue<KType>(10);

        long checksum = 0L;

        for (int i = 0; i < count; i++)
        {
            this.prioq.add(cast(i));
            checksum += castType(cast(i));
        }

        final IntHolder holder = new IntHolder();
        holder.value = 0;

        final long sumResult = this.prioq.forEach(new KTypeProcedure<KType>() {
            long sum = 0L;

            @Override
            public void apply(final KType v)
            {
                this.sum += castType(v);
            }
        }).sum;

        Assert.assertEquals(checksum, sumResult);
    }

    /* */
    @Test
    public void testClear()
    {
        insertElements(this.prioq, 1, 2, 3, 4, 5, 6, 7);
        this.prioq.clear();
        checkConsistency();
    }

    /* */
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testHashCodeEquals()
    {
        //A) Fill
        final int count = 12784;

        this.prioq = new KTypeHeapPriorityQueue<KType>(10);
        final KTypeHeapPriorityQueue<KType> list2 = new KTypeHeapPriorityQueue<KType>(10);

        for (int i = 0; i < count; i++)
        {
            this.prioq.add(cast(i));
            list2.add(cast(i));
        }

        Assert.assertEquals(this.prioq.hashCode(), list2.hashCode());

    }

    /* */
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testClone()
    {
        insertElements(this.prioq, 1, 2, 3);

        final KTypeHeapPriorityQueue<KType> cloned = this.prioq.clone();
        cloned.removeAllOccurrences(this.key1);

        assertPrioQueueEquals(this.prioq, 1, 2, 3);
        assertPrioQueueEquals(cloned, 2, 3);
    }

    /* */
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testToString()
    {
        insertElements(this.prioq, 1, 2, 3);
        Assert.assertEquals("["
                + this.key1 + ", "
                + this.key2 + ", "
                + this.key3 + "]", this.prioq.toString());
    }

    @Test
    public void testEqualsComparable()
    {
        final Random prng = new Random(45872243156464L);

        final int COUNT = (int) 50e3;

        //A) fill COUNT random values in prio-queue
        final KTypeHeapPriorityQueue<KType> testPQ = new KTypeHeapPriorityQueue<KType>(74);
        final KTypeHeapPriorityQueue<KType> testPQ2 = new KTypeHeapPriorityQueue<KType>((int) 1e6);

        for (int i = 0; i < COUNT; i++)
        {
            //use a Random number on a small enough range so that it can be exactly represented
            //in Float or Double with no conversion error back and forth.
            final int randomInt = prng.nextInt(1000 * 1000);

            testPQ.add(cast(randomInt));
            testPQ2.add(cast(randomInt));
        } //end for

        Assert.assertEquals(COUNT, testPQ.size());
        Assert.assertEquals(COUNT, testPQ2.size());

        //B) compare both
        Assert.assertTrue(testPQ.equals(testPQ2));

    }

    @Test
    public void testEqualsComparator()
    {
        //Inverse natural ordering comparator
        final KTypeComparator<KType> compIndepinstance = new KTypeComparator<KType>() {

            @Override
            public int compare(final KType e1, final KType e2)
            {
                int res = 0;

                if (castType(e1) < castType(e2))
                {
                    res = 1;
                }
                else if (castType(e1) > castType(e2))
                {
                    res = -1;
                }

                return res;
            }
        };

        //Inverse natural ordering comparator
        final KTypeComparator<KType> compIndepinstance2 = new KTypeComparator<KType>() {

            @Override
            public int compare(final KType e1, final KType e2)
            {
                int res = 0;

                if (castType(e1) < castType(e2))
                {
                    res = 1;
                }
                else if (castType(e1) > castType(e2))
                {
                    res = -1;
                }

                return res;
            }
        };

        //Inverse natural ordering comparator
        final KTypeComparator<KType> compAlwaysSame = new KTypeComparator<KType>() {

            @Override
            public boolean equals(final Object obj) {

                return true;
            }

            @Override
            public int compare(final KType e1, final KType e2)
            {
                int res = 0;

                if (castType(e1) < castType(e2))
                {
                    res = 1;
                }
                else if (castType(e1) > castType(e2))
                {
                    res = -1;
                }

                return res;
            }
        };

        //Inverse natural ordering comparator
        final KTypeComparator<KType> compAlwaysSame2 = new KTypeComparator<KType>() {

            @Override
            public boolean equals(final Object obj) {

                return true;
            }

            @Override
            public int compare(final KType e1, final KType e2)
            {
                int res = 0;

                if (castType(e1) < castType(e2))
                {
                    res = 1;
                }
                else if (castType(e1) > castType(e2))
                {
                    res = -1;
                }

                return res;
            }
        };

        final Random prng = new Random(74172243156464L);

        final int COUNT = (int) 50e3;

        //A) fill COUNT random values in prio-queue
        final KTypeHeapPriorityQueue<KType> testPQ = new KTypeHeapPriorityQueue<KType>(compIndepinstance, 471);
        final KTypeHeapPriorityQueue<KType> testPQSameInstance = new KTypeHeapPriorityQueue<KType>(compIndepinstance, 92365);
        final KTypeHeapPriorityQueue<KType> testPQOtherInstance = new KTypeHeapPriorityQueue<KType>(compIndepinstance2, 1545);
        final KTypeHeapPriorityQueue<KType> testPQSame = new KTypeHeapPriorityQueue<KType>(compAlwaysSame, 10);
        final KTypeHeapPriorityQueue<KType> testPQSame2 = new KTypeHeapPriorityQueue<KType>(compAlwaysSame2, 874122);

        for (int i = 0; i < COUNT; i++)
        {
            //use a Random number on a small enough range so that it can be exactly represented
            //in Float or Double with no conversion error back and forth.
            final int randomInt = prng.nextInt(1000 * 1000);

            testPQ.add(cast(randomInt));
            testPQSameInstance.add(cast(randomInt));
            testPQOtherInstance.add(cast(randomInt));
            testPQSame.add(cast(randomInt));
            testPQSame2.add(cast(randomInt));

        } //end for

        //B) compare both

        //same instance , by reference
        Assert.assertTrue(testPQ.equals(testPQSameInstance));

        //different instances, diff by default
        Assert.assertFalse(testPQ.equals(testPQOtherInstance));

        //different instances, but properly compared because instances are equals() = true
        Assert.assertTrue(testPQSame.equals(testPQSame2));
    }

    @Test
    public void testSyntheticComparable()
    {
        final Random prng = new Random(45874131463156464L);

        final int COUNT = (int) 1e4;

        //A) fill COUNT random values in prio-queue
        final KTypeHeapPriorityQueue<KType> testPQ = new KTypeHeapPriorityQueue<KType>(10);
        final int[] referenceArray = new int[COUNT];

        for (int i = 0; i < COUNT; i++)
        {
            //use a Random number on a small enough range so that it can be exactly represented
            //in Float or Double with no conversion error back and forth.
            final int randomInt = prng.nextInt(1000 * 1000);

            final int size = testPQ.size();
            testPQ.add(cast(randomInt));
            Assert.assertEquals(size + 1, testPQ.size());
            referenceArray[i] = castType(cast(randomInt));

            if (i % 3348 == 0)
            {
                Assert.assertTrue(isMinHeap(testPQ));
            }
        }

        Assert.assertEquals(COUNT, testPQ.size());
        Assert.assertTrue(isMinHeap(testPQ));

        //B) popTop elements one by one
        //they are supposed to come in natural order
        Arrays.sort(referenceArray);
        int currentSize = COUNT;

        for (int i = 0; i < COUNT - 1; i++)
        {
            final int expected = referenceArray[i];
            final int expected_next = referenceArray[i + 1];

            Assert.assertEquals(expected, castType(testPQ.top()));
            //size doesn't change
            Assert.assertEquals(currentSize, testPQ.size());
            Assert.assertEquals(expected, castType(testPQ.top()));

            Assert.assertEquals(expected, castType(testPQ.popTop()));
            //size is smaller by one element
            Assert.assertEquals(currentSize - 1, testPQ.size());
            //top() point now to the next smallest element
            Assert.assertEquals(expected_next, castType(testPQ.top()));

            if (i % 1111 == 0)
            {
                Assert.assertTrue(isMinHeap(testPQ));
            }

            currentSize--;
        }

        Assert.assertEquals(1, testPQ.size());
        testPQ.clear();
        Assert.assertTrue(testPQ.isEmpty());
    }

    @Test
    public void testSyntheticComparator()
    {
        //Inverse natural ordering comparator
        final KTypeComparator<KType> comp = new KTypeComparator<KType>() {

            @Override
            public int compare(final KType e1, final KType e2)
            {
                int res = 0;

                if (castType(e1) < castType(e2))
                {
                    res = 1;
                }
                else if (castType(e1) > castType(e2))
                {
                    res = -1;
                }

                return res;
            }
        };

        final Random prng = new Random(98754131654131L);

        final int COUNT = (int) 1e4;

        //A) fill COUNT random values in prio-queue
        final KTypeHeapPriorityQueue<KType> testPQ = new KTypeHeapPriorityQueue<KType>(comp, 10);
        final KType[] referenceArray = Intrinsics.newKTypeArray(COUNT);

        for (int i = 0; i < COUNT; i++)
        {
            //use a Random number on a small enough range so that it can be exactly represented
            //in Float of Double with no conversion error back and forth.
            final int randomInt = prng.nextInt(1000 * 1000);

            final int size = testPQ.size();
            testPQ.add(cast(randomInt));
            Assert.assertEquals(size + 1, testPQ.size());
            referenceArray[i] = cast(randomInt);

            if (i % 5548 == 0)
            {
                Assert.assertTrue(isMinHeap(testPQ));
            }
        }

        Assert.assertEquals(COUNT, testPQ.size());
        Assert.assertTrue(isMinHeap(testPQ));

        //B) popTop elements one by one
        //they are supposed to come in inverse-natural order
        KTypeSort.quicksort(referenceArray, comp);

        int currentSize = COUNT;

        for (int i = 0; i < COUNT - 1; i++)
        {
            final KType expected = referenceArray[i];
            final KType expected_next = referenceArray[i + 1];

            //assure it is indeed in inverse natural order : bigger first
            Assert.assertTrue(castType(expected) >= castType(expected_next));

            Assert.assertEquals(castType(expected), castType(testPQ.top()));
            //size doesn't change
            Assert.assertEquals(currentSize, testPQ.size());
            Assert.assertEquals(castType(expected), castType(testPQ.top()));

            Assert.assertEquals(castType(expected), castType(testPQ.popTop()));
            //size is smaller by one element
            Assert.assertEquals(currentSize - 1, testPQ.size());
            //top() point now to the next smallest element
            Assert.assertEquals(castType(expected_next), castType(testPQ.top()));

            if (i % 2548 == 0)
            {
                Assert.assertTrue(isMinHeap(testPQ));
            }

            currentSize--;
        }

        Assert.assertEquals(1, testPQ.size());
        testPQ.clear();
        Assert.assertTrue(testPQ.isEmpty());
    }

    @Test
    public void testPooledIteratorForEach()
    {
        // Unbroken for-each loop
        final int TEST_SIZE = 5000;
        final long TEST_ROUNDS = 100;

        final KTypeHeapPriorityQueue<KType> testContainer = new KTypeHeapPriorityQueue<KType>(10);

        //fill pq
        final Random prng = new Random(215649612148461L);

        for (int i = 0; i < TEST_SIZE; i++)
        {

            testContainer.add(cast(prng.nextInt()));
        }

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
        // for-each loop interrupted

        final int TEST_SIZE = 10000;
        final long TEST_ROUNDS = 100;

        final KTypeHeapPriorityQueue<KType> testContainer = new KTypeHeapPriorityQueue<KType>(10);

        //fill pq
        final Random prng = new Random(484163441L);

        for (int i = 0; i < TEST_SIZE; i++)
        {

            testContainer.add(cast(prng.nextInt()));
        }

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
        // for-each loop interrupted

        final int TEST_SIZE = 5000;
        final long TEST_ROUNDS = 100;

        final KTypeHeapPriorityQueue<KType> testContainer = new KTypeHeapPriorityQueue<KType>(10);

        //fill pq
        final Random prng = new Random(89874156187414L);

        for (int i = 0; i < TEST_SIZE; i++)
        {
            testContainer.add(cast(prng.nextInt()));
        }

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

        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            //Classical iterator loop, with manually allocated Iterator
            final int initialPoolSize = testContainer.valueIteratorPool.size();

            final KTypeHeapPriorityQueue<KType>.ValueIterator loopIterator = testContainer.iterator();

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
        } //end for rounds

        // pool initial size is untouched anyway
        Assert.assertEquals(startingPoolSize, testContainer.valueIteratorPool.size());
    }

    @Test
    public void testPooledIteratorBrokenIteratorLoop()
    {
        // for-each loop interrupted

        final int TEST_SIZE = 10000;
        final long TEST_ROUNDS = 100;

        final KTypeHeapPriorityQueue<KType> testContainer = new KTypeHeapPriorityQueue<KType>(10);

        //fill pq
        final Random prng = new Random(994610788L);

        for (int i = 0; i < TEST_SIZE; i++)
        {
            testContainer.add(cast(prng.nextInt()));
        }

        final int startingPoolSize = testContainer.valueIteratorPool.size();

        int count = 0;
        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            //Classical iterator loop, with manually allocated Iterator
            final long initialPoolSize = testContainer.valueIteratorPool.size();

            final KTypeHeapPriorityQueue<KType>.ValueIterator loopIterator = testContainer.iterator();

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
            loopIterator.release();

            //now the pool is restored
            Assert.assertEquals(initialPoolSize, testContainer.valueIteratorPool.size());

        } //end for rounds

        // pool initial size is untouched anyway
        Assert.assertEquals(startingPoolSize, testContainer.valueIteratorPool.size());
    }

    @Test
    public void testPooledIteratorExceptionIteratorLoop()
    {
        final int TEST_SIZE = 10000;
        final long TEST_ROUNDS = 100;

        final KTypeHeapPriorityQueue<KType> testContainer = new KTypeHeapPriorityQueue<KType>(10);

        //fill pq
        final Random prng = new Random(78411114444L);

        for (int i = 0; i < TEST_SIZE; i++)
        {
            testContainer.add(cast(prng.nextInt()));
        }

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
        KTypeHeapPriorityQueue<KType>.ValueIterator loopIterator = null;

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
                loopIterator.release();

                //now the pool is restored
                Assert.assertEquals(startingPoolSize, testContainer.valueIteratorPool.size());
            }
        } //end for rounds

        // pool initial size is untouched anyway
        Assert.assertEquals(startingPoolSize, testContainer.valueIteratorPool.size());
    }

    @Test
    public void testPooledIteratorExceptionSafe()
    {
        final int TEST_SIZE = 110171;
        final long TEST_ROUNDS = 15;

        final KTypeHeapPriorityQueue<KType> testContainer = new KTypeHeapPriorityQueue<KType>(10);

        //fill pq
        final Random prng = new Random(9774442154544L);

        for (int i = 0; i < TEST_SIZE; i++)
        {
            testContainer.add(cast(prng.nextInt()));
        }

        final long checksum = testContainer.forEach(new KTypeProcedure<KType>() {

            long count;

            @Override
            public void apply(final KType value)
            {
                this.count += castType(value);
            }
        }).count;

        final int initialPoolSize = testContainer.valueIteratorPool.size();

        //start with a non full pool, remove 3 elements
        final AbstractIterator<KTypeCursor<KType>> loopIteratorFake = testContainer.iterator();
        final AbstractIterator<KTypeCursor<KType>> loopIteratorFake2 = testContainer.iterator();
        final AbstractIterator<KTypeCursor<KType>> loopIteratorFake3 = testContainer.iterator();

        final int startingTestPoolSize = testContainer.valueIteratorPool.size();

        Assert.assertEquals(initialPoolSize - 3, startingTestPoolSize);

        int count = 0;
        KTypeHeapPriorityQueue<KType>.ValueIterator loopIterator = null;

        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            try
            {
                loopIterator = testContainer.iterator();
                Assert.assertEquals(startingTestPoolSize - 1, testContainer.valueIteratorPool.size());

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
                Assert.assertEquals(startingTestPoolSize, testContainer.valueIteratorPool.size());
                Assert.assertEquals(checksum, this.guard);

                //still, try to return it ....
                loopIterator.release();

                //nothing has changed
                Assert.assertEquals(startingTestPoolSize, testContainer.valueIteratorPool.size());
            }
            catch (final Exception e)
            {
                //iterator is NOT returned to its pool because of the exception
                Assert.assertEquals(startingTestPoolSize - 1, testContainer.valueIteratorPool.size());

                //manual return to the pool then
                loopIterator.release();

                //now the pool is restored
                Assert.assertEquals(startingTestPoolSize, testContainer.valueIteratorPool.size());

                //continue to try to release...
                loopIterator.release();

                //nothing has changed
                Assert.assertEquals(startingTestPoolSize, testContainer.valueIteratorPool.size());
            }
        } //end for rounds

        // pool initial size is untouched anyway
        Assert.assertEquals(startingTestPoolSize, testContainer.valueIteratorPool.size());

        //finally return the fake ones, several times
        loopIteratorFake.release();
        loopIteratorFake.release();
        loopIteratorFake.release();
        loopIteratorFake2.release();
        loopIteratorFake2.release();
        loopIteratorFake2.release();
        loopIteratorFake3.release();
        loopIteratorFake3.release();

        Assert.assertEquals(initialPoolSize, testContainer.valueIteratorPool.size());
    }

    @Test
    public void testPreallocatedSize()
    {
        final Random randomVK = new Random(234654984616L);
        //Test that the container do not resize if less that the initial size

        final int NB_TEST_RUNS = 50;

        for (int run = 0; run < NB_TEST_RUNS; run++)
        {
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
            final KTypeHeapPriorityQueue<KType> newHeap = new KTypeHeapPriorityQueue<KType>(PREALLOCATED_SIZE);

            //3) Add PREALLOCATED_SIZE different values. At the end, size() must be == PREALLOCATED_SIZE,
            //and internal buffer/allocated must not have changed of size
            final int contructorBufferSize = newHeap.buffer.length;

            for (int i = 0; i < PREALLOCATED_SIZE; i++)
            {
                newHeap.add(cast(randomVK.nextInt()));

                //internal size has not changed.
                Assert.assertEquals(contructorBufferSize, newHeap.buffer.length);
            }

            Assert.assertEquals(PREALLOCATED_SIZE, newHeap.size());
        } //end for test runs
    }

    /**
     * Check if the prio queue content is identical to a given ordered sequence of elements.
     */
    private void assertPrioQueueEquals(final KTypeHeapPriorityQueue<KType> obj, final int... elements)
    {
        Assert.assertEquals(elements.length, obj.size());

        final KType[] arrayExport = (KType[]) obj.toArray();

        Arrays.sort(arrayExport);

        for (int ii = 0; ii < arrayExport.length; ii++)
        {
            Assert.assertEquals(elements[ii], castType(arrayExport[ii]));
        }
    }

    private void insertElements(final KTypeHeapPriorityQueue<KType> pq, final int... elements)
    {
        for (int i = 0; i < elements.length; i++)
        {
            pq.add(cast(elements[i]));
        }
    }

    /**
     * method to test invariant in assert
     */
// is pq[1..N] a min heap?
    private boolean isMinHeap(final KTypeHeapPriorityQueue<KType> q)
    {
        if (q.comparator == null)
        {
            return isMinHeapComparable(q, 1);
        }

        return isMinHeapComparator(q, 1);
    }

// is subtree of pq[1..N] rooted at k a min heap?
    private boolean isMinHeapComparable(final KTypeHeapPriorityQueue<KType> q, final int k)
    {
        final int N = q.elementsCount;

        if (k > N) {
            return true;
        }
        final int left = 2 * k, right = 2 * k + 1;

        if (left <= N && Intrinsics.isCompSupKTypeUnchecked(q.buffer[k], q.buffer[left])) {
            return false;
        }
        if (right <= N && Intrinsics.isCompSupKTypeUnchecked(q.buffer[k], q.buffer[right])) {
            return false;
        }
        //recursively test
        return isMinHeapComparable(q, left) && isMinHeapComparable(q, right);
    }

// is subtree of pq[1..N] rooted at k a min heap?
    private boolean isMinHeapComparator(final KTypeHeapPriorityQueue<KType> q, final int k)
    {
        final int N = q.elementsCount;

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        final Comparator<? super KType> comp = q.comparator;
        /*! #else
        KTypeComparator<? super KType> comp = q.comparator;
        #end !*/

        if (k > N) {
            return true;
        }
        final int left = 2 * k, right = 2 * k + 1;

        if (left <= N && comp.compare(q.buffer[k], q.buffer[left]) > 0) {
            return false;
        }
        if (right <= N && comp.compare(q.buffer[k], q.buffer[right]) > 0) {
            return false;
        }
        //recursively test
        return isMinHeapComparator(q, left) && isMinHeapComparator(q, right);
    }
}
