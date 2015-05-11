package com.carrotsearch.hppcrt.heaps;

import java.util.*;

import org.junit.*;

import static com.carrotsearch.hppcrt.TestUtils.*;
import static org.junit.Assert.*;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.hash.BitMixer;
import com.carrotsearch.hppcrt.lists.*;
import com.carrotsearch.hppcrt.TestUtils;
import com.carrotsearch.hppcrt.cursors.*;
import com.carrotsearch.hppcrt.predicates.*;
import com.carrotsearch.hppcrt.procedures.*;
import com.carrotsearch.hppcrt.sets.*;
import com.carrotsearch.hppcrt.strategies.*;
import com.carrotsearch.hppcrt.sorting.*;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.*;

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
/**
 * Unit tests for {@link KTypeHeapPriorityQueue}.
 */
//${TemplateOptions.doNotGenerateKType("BOOLEAN")}
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeIndexedHeapPriorityQueueTest<KType> extends AbstractKTypeTest<KType>
{

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    /**
     * Test class for mutable comparable type
     */
    public final class TestHolder implements Comparable<TestHolder>
    {
        public KType value;

        public TestHolder() {
            //nothing
        }

        public TestHolder(final KType value) {
            this.value = value;
        }

        @Override
        public int hashCode() {
            return BitMixer.mix(this.value);
        }

        @Override
        public boolean equals(final Object other) {
            if (other instanceof KTypeIndexedHeapPriorityQueueTest.TestHolder) {

                @SuppressWarnings("unchecked")
                final TestHolder otherTh = (TestHolder) other;

                return Intrinsics.equalsKType(this.value, otherTh.value);
            }

            return false;
        }

        @Override
        public int compareTo(final TestHolder o) {

            return Intrinsics.compareKTypeUnchecked(this.value, o.value);
        }
    }

    /*! #end !*/

    public final KTypeComparator<KType> NATURAL_COMPARATOR = new KTypeComparator<KType>() {

        @Override
        public int compare(final KType e1, final KType e2) {

            return Intrinsics.compareKTypeUnchecked(e1, e2);
        }
    };

    public final KTypeComparator<KType> INVERSE_COMPARATOR = new KTypeComparator<KType>() {

        @Override
        public int compare(final KType e1, final KType e2) {

            return -Intrinsics.compareKTypeUnchecked(e1, e2);
        }
    };

    /**
     * Per-test fresh initialized instance.
     */
    public KTypeIndexedHeapPriorityQueue<KType> prioq;

    public KTypeIndexedHeapPriorityQueue<KType> prioqNaturalComparator;
    public KTypeIndexedHeapPriorityQueue<KType> prioqInverseComparator;

    public volatile long guard;

    @BeforeClass
    public static void configure() {
        IteratorPool.configureInitialPoolSize(8);
    }

    /* */
    @Before
    public void initialize() {
        this.prioq = new KTypeIndexedHeapPriorityQueue<KType>(0);
        this.prioqNaturalComparator = new KTypeIndexedHeapPriorityQueue<KType>(this.NATURAL_COMPARATOR);
        this.prioqInverseComparator = new KTypeIndexedHeapPriorityQueue<KType>(this.INVERSE_COMPARATOR);
    }

    private void checkConsistencyAfter(final KTypeIndexedHeapPriorityQueue<KType> prio) {
        if (prio != null) {
            //scan beyond the active zone
            //1-based indexing
            for (int i = prio.elementsCount + 1; i < prio.buffer.length; i++) {
                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                Assert.assertTrue(Intrinsics.<KType> defaultKTypeValue() == prio.buffer[i]);
                /*! #end !*/
            }

            //check heap property
            Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(prio));

            //Check that we can pop() all elements
            final KType[] popedElements = Intrinsics.newKTypeArray(prio.size());

            int ii = 0;
            while (prio.size() > 0) {

                popedElements[ii++] = prio.popTop();
            }

            //final size is zero
            Assert.assertEquals(0, prio.size());

            //Check that their order respects the comparison criteria :
            assertOrder(popedElements, prio.comparator());
        }
    }

    @After
    public void checkConsistencyAfter() {
        checkConsistencyAfter(this.prioq);
        checkConsistencyAfter(this.prioqInverseComparator);
        checkConsistencyAfter(this.prioqNaturalComparator);
    }

    /* */
    @Test
    public void testInitiallyEmpty() {
        Assert.assertEquals(0, this.prioq.size());

        //Access by key with topKey()
        Assert.assertEquals(-1, this.prioq.topKey());
    }

    /* */
    @Test
    public void testPut() {
        this.prioq.put(0, this.key1);
        this.prioq.put(111, this.key2);
        assertPrioQueueEquals(this.prioq, 0, 1, 111, 2);
    }

    /* */
    @Test
    public void testPut2() {
        this.prioq.put(5000, this.key1);
        this.prioq.put(0, this.key2);
        this.prioq.put(4, this.key4);
        this.prioq.put(3, this.key3);
        assertPrioQueueEquals(this.prioq, 5000, 1, 0, 2, 4, 4, 3, 3);
    }

    /* */
    @Test
    public void testPutAll() {
        this.prioq.put(1, this.key1);
        this.prioq.put(2, this.key1);

        final KTypeIndexedHeapPriorityQueue<KType> prio2 = new KTypeIndexedHeapPriorityQueue<KType>(0);

        prio2.put(2, this.key2);
        prio2.put(3, this.key4);

        // One new key (key3).
        Assert.assertEquals(1, this.prioq.putAll(prio2));

        // Assert the value under 2 has been replaced.
        TestUtils.assertEquals2(this.key2, this.prioq.get(2));

        // And 3 has been added.
        TestUtils.assertEquals2(this.key4, this.prioq.get(3));
        Assert.assertEquals(3, this.prioq.size());
    }

    /* */
    @Test
    public void testPutIfAbsent() {
        Assert.assertTrue(this.prioq.putIfAbsent(1, this.key1));
        Assert.assertFalse(this.prioq.putIfAbsent(1, this.key2));
        TestUtils.assertEquals2(this.key1, this.prioq.get(1));
    }

    /*! #if ($TemplateOptions.KTypeNumeric)
    @Test
    public void testPutOrAdd()
    {
        assertEquals2(key1, prioq.putOrAdd(1, key1, key2));
        assertEquals2(key1 + key2, prioq.putOrAdd(1, key1, key2));
    }
    #end !*/

    /*! #if ($TemplateOptions.KTypeNumeric)
    @Test
    public void testAddTo()
    {
        assertEquals2(key1, prioq.addTo(1, key1));
        assertEquals2(key1 + key2, prioq.addTo(1, key2));
    }
    #end !*/

    /* */
    @Test
    public void testRemove() {
        this.prioq.put(0, this.k0);
        this.prioq.put(1, this.k1);
        this.prioq.put(2, this.k2);
        this.prioq.put(3, this.k3);
        this.prioq.put(4, this.k4);

        Assert.assertEquals(0, castType(this.prioq.remove(0)));
        Assert.assertFalse(this.prioq.containsKey(0));
        Assert.assertEquals(4, castType(this.prioq.remove(4)));
        Assert.assertFalse(this.prioq.containsKey(4));
        Assert.assertEquals(3, castType(this.prioq.remove(3)));
        Assert.assertFalse(this.prioq.containsKey(3));
        Assert.assertEquals(2, castType(this.prioq.remove(2)));
        Assert.assertFalse(this.prioq.containsKey(2));
        Assert.assertEquals(1, castType(this.prioq.remove(1)));
        Assert.assertFalse(this.prioq.containsKey(1));

        //try to delete a non-existent element index
        Assert.assertEquals(castType(this.prioq.getDefaultValue()), castType(this.prioq.remove(10)));
    }

    /* */
    @Test
    public void testRemoveRandom() {
        final int COUNT = (int) 1e5;
        final Random prng = new Random(791614611L);

        final int[] reference = new int[COUNT];
        Arrays.fill(reference, -1);

        int refSize = 0;

        for (int i = 0; i < COUNT; i++) {
            final int index = prng.nextInt(COUNT);
            final int value = prng.nextInt(COUNT);

            //cast twice to narrow conversion
            if (reference[index] == -1) {
                reference[index] = value;
                refSize++;

                //add in map (there is a resize going on)
                this.prioq.put(index, cast(value));
                Assert.assertTrue(this.prioq.containsKey(index));
            }

            if (i % 11587 == 0) {
                KTypeIndexedHeapPriorityQueueTest.checkConsistency(this.prioq);
                Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(this.prioq));
            }
        }

        KTypeIndexedHeapPriorityQueueTest.checkConsistency(this.prioq);
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(this.prioq));

        Assert.assertEquals(refSize, this.prioq.size());

        //B) delete some indices
        for (int i = 0; i < COUNT; i++) {
            if (i % 3 == 0) {
                reference[i] = -1;

                //remove also in the prio queue
                this.prioq.remove(i);
                Assert.assertFalse("" + i, this.prioq.containsKey(i));
                Assert.assertEquals("" + i, castType(this.prioq.getDefaultValue()), castType(this.prioq.remove(i)));
            }

            if (i % 8450 == 0) {
                KTypeIndexedHeapPriorityQueueTest.checkConsistency(this.prioq);
                Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(this.prioq));
            }
        }

        KTypeIndexedHeapPriorityQueueTest.checkConsistency(this.prioq);
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(this.prioq));

        //B-2) the remaining slots exists
        for (int index = 0; index < reference.length; index++) {
            if (reference[index] != -1) {
                Assert.assertTrue(this.prioq.containsKey(index));
                this.prioq.remove(index);
                Assert.assertFalse("" + index, this.prioq.containsKey(index));
                Assert.assertEquals("" + index, castType(this.prioq.getDefaultValue()), castType(this.prioq.remove(index)));

                if (index % 5557 == 0) {
                    KTypeIndexedHeapPriorityQueueTest.checkConsistency(this.prioq);
                    Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(this.prioq));
                }
            }
        }

        Assert.assertEquals(0, this.prioq.size());
    }

    /* */
    @Test
    public void testPutRandom() {
        final int COUNT = (int) 1e4;
        final Random prng = new Random(87842154L);

        final HashMap<Integer, Integer> reference = new HashMap<Integer, Integer>();

        for (int i = 0; i < COUNT; i++) {
            final int index = prng.nextInt(COUNT);
            int value = prng.nextInt(COUNT);
            value = castType(cast(value));

            //cast twice to narrow conversion
            if (!reference.containsKey(index)) {
                reference.put(index, value);

                //add in map (there is a resize going on)
                this.prioq.put(index, cast(value));
                final int currentSize = this.prioq.size();
                //attempt to add twice
                this.prioq.put(index, cast(value));
                //size has not changed
                Assert.assertEquals(currentSize, this.prioq.size());
            }

            if (i % 11587 == 0) {
                KTypeIndexedHeapPriorityQueueTest.checkConsistency(this.prioq);
                Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(this.prioq));
            }
        }

        KTypeIndexedHeapPriorityQueueTest.checkConsistency(this.prioq);
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(this.prioq));

        Assert.assertEquals(reference.size(), this.prioq.size());

        //iterate all the reference and check it also exists in prio queue
        for (final int index : reference.keySet()) {
            Assert.assertTrue(this.prioq.containsKey(index));
            Assert.assertEquals(reference.get(index).intValue(), castType(this.prioq.get(index)));
        }
    }

    /* */
    @Test
    public void testPopTop() {
        insertElements(this.prioq, 0, 10, /* */1, 9, /* */2, 8, /* */3, 7, /* */4, 6, /* */5, 5, /* */6, 4, /* */7, 3);
        assertPrioQueueEquals(this.prioq, 0, 10, /* */1, 9, /* */2, 8, /* */3, 7, /* */4, 6, /* */5, 5, /* */6, 4, /* */7,
                3);
        Assert.assertEquals(8, this.prioq.size());
        Assert.assertEquals(3, castType(this.prioq.top()));
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(this.prioq));

        Assert.assertEquals(3, castType(this.prioq.popTop()));
        assertPrioQueueEquals(this.prioq, 6, 4, /* */5, 5, /* */4, 6, /* */3, 7, /* */2, 8, /* */1, 9, /* */0, 10);
        Assert.assertEquals(7, this.prioq.size());
        Assert.assertEquals(4, castType(this.prioq.top()));
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(this.prioq));

        Assert.assertEquals(4, castType(this.prioq.popTop()));
        assertPrioQueueEquals(this.prioq, /* */5, 5, /* */4, 6, /* */3, 7, /* */2, 8, /* */1, 9, /* */0, 10);
        Assert.assertEquals(6, this.prioq.size());
        Assert.assertEquals(5, castType(this.prioq.top()));
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(this.prioq));

        Assert.assertEquals(5, castType(this.prioq.popTop()));
        assertPrioQueueEquals(this.prioq, /* */4, 6, /* */3, 7, /* */2, 8, /* */1, 9, /* */0, 10);
        Assert.assertEquals(5, this.prioq.size());
        Assert.assertEquals(6, castType(this.prioq.top()));
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(this.prioq));
        Assert.assertFalse(this.prioq.isEmpty());

        Assert.assertEquals(6, castType(this.prioq.popTop()));
        assertPrioQueueEquals(this.prioq, /* */3, 7, /* */2, 8, /* */1, 9, /* */0, 10);
        Assert.assertEquals(4, this.prioq.size());
        Assert.assertEquals(7, castType(this.prioq.top()));
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(this.prioq));
        Assert.assertFalse(this.prioq.isEmpty());

        Assert.assertEquals(7, castType(this.prioq.popTop()));
        assertPrioQueueEquals(this.prioq, /* */2, 8, /* */1, 9, /* */0, 10);
        Assert.assertEquals(3, this.prioq.size());
        Assert.assertEquals(8, castType(this.prioq.top()));
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(this.prioq));
        Assert.assertFalse(this.prioq.isEmpty());

        Assert.assertEquals(8, castType(this.prioq.popTop()));
        assertPrioQueueEquals(this.prioq, /* */1, 9, /* */0, 10);
        Assert.assertEquals(2, this.prioq.size());
        Assert.assertEquals(9, castType(this.prioq.top()));
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(this.prioq));
        Assert.assertFalse(this.prioq.isEmpty());

        Assert.assertEquals(9, castType(this.prioq.popTop()));
        assertPrioQueueEquals(this.prioq, /* */0, 10);
        Assert.assertEquals(1, this.prioq.size());
        Assert.assertEquals(10, castType(this.prioq.top()));
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(this.prioq));
        Assert.assertFalse(this.prioq.isEmpty());

        Assert.assertEquals(10, castType(this.prioq.popTop()));
        Assert.assertEquals(0, this.prioq.size());
        Assert.assertTrue(this.prioq.isEmpty());
    }

    /* */
    @Test
    public void testRemoveAllValues() {
        insertElements(this.prioq, 1000, 0, 1001, 1, 1002, 0, 1003, 1, 1004, 0);

        Assert.assertEquals(0, this.prioq.values().removeAll(this.k2));
        Assert.assertEquals(3, this.prioq.values().removeAll(this.k0));
        assertPrioQueueEquals(this.prioq, 1001, 1, 1003, 1);

        Assert.assertEquals(2, this.prioq.values().removeAll(this.k1));
        Assert.assertTrue(this.prioq.isEmpty());

        //
        insertElements(this.prioqNaturalComparator, 1000, 0, 1001, 1, 1002, 0, 1003, 1, 1004, 0);

        Assert.assertEquals(0, this.prioqNaturalComparator.values().removeAll(this.k2));
        Assert.assertEquals(3, this.prioqNaturalComparator.values().removeAll(this.k0));
        assertPrioQueueEquals(this.prioqNaturalComparator, 1001, 1, 1003, 1);

        Assert.assertEquals(2, this.prioqNaturalComparator.values().removeAll(this.k1));
        Assert.assertTrue(this.prioqNaturalComparator.isEmpty());

        //
        insertElements(this.prioqInverseComparator, 1000, 0, 1001, 1, 1002, 0, 1003, 1, 1004, 0);

        Assert.assertEquals(0, this.prioqInverseComparator.values().removeAll(this.k2));
        Assert.assertEquals(3, this.prioqInverseComparator.values().removeAll(this.k0));
        assertPrioQueueEquals(this.prioqInverseComparator, 1001, 1, 1003, 1);

        Assert.assertEquals(2, this.prioqInverseComparator.values().removeAll(this.k1));
        Assert.assertTrue(this.prioqInverseComparator.isEmpty());
    }

    /* */
    @Test
    public void testRemoveAllKeys() {
        insertElements(this.prioq, 1000, 0, 1001, 1, 1002, 0, 1003, 1, 1004, 0);

        Assert.assertEquals(0, this.prioq.keys().removeAll(1111));
        Assert.assertEquals(1, this.prioq.keys().removeAll(1001));
        assertPrioQueueEquals(this.prioq, 1000, 0, 1002, 0, 1003, 1, 1004, 0);

        Assert.assertEquals(1, this.prioq.keys().removeAll(1004));
        assertPrioQueueEquals(this.prioq, 1000, 0, 1002, 0, 1003, 1);
    }

    /* */
    @Test
    public void testRemoveAllFromLookupContainerValues() {
        insertElements(this.prioq, 10, 0, 11, 1, 12, 2, 13, 1, 0, 0);

        final KTypeHashSet<KType> list2 = KTypeHashSet.newInstance();
        list2.add(asArray(0, 2));

        Assert.assertEquals(3, this.prioq.values().removeAll(list2));
        Assert.assertEquals(0, this.prioq.values().removeAll(list2));

        assertPrioQueueEquals(this.prioq, 11, 1, 13, 1);
    }

    /* */
    @Test
    public void testRemoveAllFromLookupContainerKeys() {
        insertElements(this.prioq, 10, 0, 11, 1, 12, 2, 13, 1, 0, 0);

        final IntHashSet list2 = IntHashSet.newInstance();
        list2.add(0, 10, 12);

        Assert.assertEquals(3, this.prioq.keys().removeAll(list2));
        Assert.assertEquals(0, this.prioq.keys().removeAll(list2));

        assertPrioQueueEquals(this.prioq, 11, 1, 13, 1);
    }

    /* */
    @Test
    public void testRemoveAllFromContainerKeys() {
        insertElements(this.prioq, 10, 0, 11, 1, 12, 2, 13, 1, 0, 0);

        final IntArrayList list2 = IntArrayList.newInstance();
        list2.add(0, 10, 12);

        Assert.assertEquals(3, this.prioq.removeAll(list2));
        Assert.assertEquals(0, this.prioq.removeAll(list2));

        assertPrioQueueEquals(this.prioq, 11, 1, 13, 1);
    }

    /* */
    @Test
    public void testRemoveAllWithPredicateValues() {
        insertElements(this.prioq, 0, 0, 1, 1, 2, 2, 11, 1, 44, 4);

        Assert.assertEquals(3, this.prioq.values().removeAll(new KTypePredicate<KType>() {
            @Override
            public boolean apply(final KType v) {
                return v == KTypeIndexedHeapPriorityQueueTest.this.key1 || v == KTypeIndexedHeapPriorityQueueTest.this.key2;
            };
        }));

        assertPrioQueueEquals(this.prioq, 0, 0, 44, 4);
    }

    /* */
    @Test
    public void testRemoveAllWithPredicateKeys() {
        insertElements(this.prioq, 0, 0, 1, 1, 2, 2, 11, 1, 44, 4);

        Assert.assertEquals(2, this.prioq.keys().removeAll(new IntPredicate() {
            @Override
            public boolean apply(final int v) {
                return v == 1 || v == 44;
            };
        }));

        assertPrioQueueEquals(this.prioq, 0, 0, 2, 2, 11, 1);
    }

    /* */
    @Test
    public void testRemoveAllWithPredicateInterruptedValues() {
        insertElements(this.prioq, 0, 0, 1, 1, 2, 2, 3, 1, 11, 11, 44, 44, 66, 11, 77, 12);

        final RuntimeException t = new RuntimeException();
        try {
            //the assert below should never be triggered because of the exception
            //so give it an invalid value in case the thing terminates  = initial size
            Assert.assertEquals(8, this.prioq.values().removeAll(new KTypePredicate<KType>() {
                @Override
                public boolean apply(final KType v) {
                    if (v == cast(11)) {
                        throw t;
                    }
                    return v == KTypeIndexedHeapPriorityQueueTest.this.key1;
                };
            }));
            Assert.fail();
        } catch (final RuntimeException e) {
            // Make sure it's really our exception...
            if (e != t) {
                throw e;
            }
        }

        // And check if the list is in consistent state.
        assertPrioQueueEquals(this.prioq, 0, 0, 2, 2, 11, 11, 44, 44, 66, 11, 77, 12);
        Assert.assertEquals(6, this.prioq.size());
    }

    /* */
    @Test
    public void testRemoveAllWithPredicateInterruptedKeys() {
        insertElements(this.prioq, 0, 0, 1, 1, 2, 2, 3, 1, 11, 2, 44, 4, 66, 11, 77, 12);

        final RuntimeException t = new RuntimeException();
        try {
            //the assert below should never be triggered because of the exception
            //so give it an invalid value in case the thing terminates  = initial size
            //for keys, iteration is in-order.
            Assert.assertEquals(8, this.prioq.keys().removeAll(new IntPredicate() {
                @Override
                public boolean apply(final int v) {
                    if (v == 44) {
                        throw t;
                    }
                    return (v == 3 || v == 77); // 3 will be suppressed but not 77, because 44 created an exception before.
                };
            }));
            Assert.fail();
        } catch (final RuntimeException e) {
            // Make sure it's really our exception...
            if (e != t) {
                throw e;
            }
        }

        // And check if the list is in consistent state.
        assertPrioQueueEquals(this.prioq, 0, 0, 1, 1, 2, 2, 11, 2, 44, 4, 66, 11, 77, 12);
        Assert.assertEquals(7, this.prioq.size());
    }

    /* */
    @Test
    public void testRemoveAllEverythingValues() {
        insertElements(this.prioq, 0, 0, 1, 1, 2, 2, 11, 1, 44, 4);

        Assert.assertEquals(5, this.prioq.values().removeAll(new KTypePredicate<KType>() {
            @Override
            public boolean apply(final KType v) {
                return true;
            };
        }));

        Assert.assertEquals(0, this.prioq.size());
    }

    /* */
    @Test
    public void testRemoveAllEverythingKeys() {
        insertElements(this.prioq, 0, 0, 1, 1, 2, 2, 11, 1, 44, 4);

        Assert.assertEquals(5, this.prioq.keys().removeAll(new IntPredicate() {
            @Override
            public boolean apply(final int v) {
                return true;
            };
        }));

        Assert.assertEquals(0, this.prioq.size());
    }

    /* */
    @Test
    public void testRetainAllWithPredicateValues() {
        insertElements(this.prioq, 10, 0, 11, 1, 12, 2, 13, 1, 14, 0);

        Assert.assertEquals(2, this.prioq.values().retainAll(new KTypePredicate<KType>() {
            @Override
            public boolean apply(final KType v) {
                return v == KTypeIndexedHeapPriorityQueueTest.this.key1 || v == KTypeIndexedHeapPriorityQueueTest.this.key2;
            };
        }));

        assertPrioQueueEquals(this.prioq, 11, 1, 13, 1, 12, 2);
    }

    /* */
    @Test
    public void testRetainAllWithPredicateKeys() {
        insertElements(this.prioq, 10, 0, 11, 1, 12, 2, 13, 1, 14, 8);

        Assert.assertEquals(3, this.prioq.keys().retainAll(new IntPredicate() {
            @Override
            public boolean apply(final int v) {
                return v == 10 || v == 14;
            };
        }));

        assertPrioQueueEquals(this.prioq, 10, 0, 14, 8);
    }

    /* */
    @Test
    public void testRemoveAllOccurencesValues() {
        this.prioq.put(1, this.key1); //del
        this.prioq.put(2, this.key2);
        this.prioq.put(3, this.key1); //del
        this.prioq.put(4, this.key3);
        this.prioq.put(5, this.key7);
        this.prioq.put(6, this.key5);
        this.prioq.put(7, this.key1); //del
        this.prioq.put(8, this.key8);
        this.prioq.put(9, this.key2);

        Assert.assertEquals(9, this.prioq.size());

        final int nbRemoved = this.prioq.values().removeAll(this.key1);

        Assert.assertEquals(3, nbRemoved);
        Assert.assertEquals(6, this.prioq.size());
        Assert.assertTrue(this.prioq.containsKey(2));
        Assert.assertTrue(this.prioq.containsKey(4));
        Assert.assertTrue(this.prioq.containsKey(5));
        Assert.assertTrue(this.prioq.containsKey(6));
        Assert.assertTrue(this.prioq.containsKey(8));
        Assert.assertTrue(this.prioq.containsKey(9));
    }

    /* */
    @Test
    public void testRemoveAllOccurencesValuesNaturalComparator() {
        this.prioqNaturalComparator.put(1, this.key1); //del
        this.prioqNaturalComparator.put(2, this.key2);
        this.prioqNaturalComparator.put(3, this.key1); //del
        this.prioqNaturalComparator.put(4, this.key3);
        this.prioqNaturalComparator.put(5, this.key7);
        this.prioqNaturalComparator.put(6, this.key5);
        this.prioqNaturalComparator.put(7, this.key1); //del
        this.prioqNaturalComparator.put(8, this.key8);
        this.prioqNaturalComparator.put(9, this.key2);

        Assert.assertEquals(9, this.prioqNaturalComparator.size());

        final int nbRemoved = this.prioqNaturalComparator.values().removeAll(this.key1);

        Assert.assertEquals(3, nbRemoved);
        Assert.assertEquals(6, this.prioqNaturalComparator.size());
        Assert.assertTrue(this.prioqNaturalComparator.containsKey(2));
        Assert.assertTrue(this.prioqNaturalComparator.containsKey(4));
        Assert.assertTrue(this.prioqNaturalComparator.containsKey(5));
        Assert.assertTrue(this.prioqNaturalComparator.containsKey(6));
        Assert.assertTrue(this.prioqNaturalComparator.containsKey(8));
        Assert.assertTrue(this.prioqNaturalComparator.containsKey(9));
    }

    /* */
    @Test
    public void testRemoveAllOccurencesValuesInverseComparatorValues() {
        this.prioqInverseComparator.put(1, this.key1); //del
        this.prioqInverseComparator.put(2, this.key2);
        this.prioqInverseComparator.put(3, this.key1); //del
        this.prioqInverseComparator.put(4, this.key3);
        this.prioqInverseComparator.put(5, this.key7);
        this.prioqInverseComparator.put(6, this.key5);
        this.prioqInverseComparator.put(7, this.key1); //del
        this.prioqInverseComparator.put(8, this.key8);
        this.prioqInverseComparator.put(9, this.key2);

        Assert.assertEquals(9, this.prioqInverseComparator.size());

        final int nbRemoved = this.prioqInverseComparator.values().removeAll(this.key1);

        Assert.assertEquals(3, nbRemoved);
        Assert.assertEquals(6, this.prioqInverseComparator.size());
        Assert.assertTrue(this.prioqInverseComparator.containsKey(2));
        Assert.assertTrue(this.prioqInverseComparator.containsKey(4));
        Assert.assertTrue(this.prioqInverseComparator.containsKey(5));
        Assert.assertTrue(this.prioqInverseComparator.containsKey(6));
        Assert.assertTrue(this.prioqInverseComparator.containsKey(8));
        Assert.assertTrue(this.prioqInverseComparator.containsKey(9));
    }

    /* */
    @Test
    public void testContainsValues() {
        this.prioq.put(1, this.key1);
        this.prioq.put(2, this.key2);
        this.prioq.put(3, this.key1);
        this.prioq.put(4, this.key3);
        this.prioq.put(5, this.key7);
        this.prioq.put(6, this.key5);
        this.prioq.put(7, this.key1);
        this.prioq.put(8, this.key8);
        this.prioq.put(9, this.key2);

        Assert.assertEquals(9, this.prioq.size());

        Assert.assertTrue(this.prioq.values().contains(this.key1));
        Assert.assertTrue(this.prioq.values().contains(this.key2));
        Assert.assertTrue(this.prioq.values().contains(this.key3));
        Assert.assertFalse(this.prioq.values().contains(this.key4)); //not in heap
        Assert.assertTrue(this.prioq.values().contains(this.key5));
        Assert.assertFalse(this.prioq.values().contains(this.key6)); //not in heap
        Assert.assertTrue(this.prioq.values().contains(this.key7));
        Assert.assertTrue(this.prioq.values().contains(this.key8));
    }

    /* */
    @Test
    public void testContainsValuesNaturalComparator() {
        this.prioqNaturalComparator.put(1, this.key1);
        this.prioqNaturalComparator.put(2, this.key2);
        this.prioqNaturalComparator.put(3, this.key1);
        this.prioqNaturalComparator.put(4, this.key3);
        this.prioqNaturalComparator.put(5, this.key7);
        this.prioqNaturalComparator.put(6, this.key5);
        this.prioqNaturalComparator.put(7, this.key1);
        this.prioqNaturalComparator.put(8, this.key8);
        this.prioqNaturalComparator.put(9, this.key2);

        Assert.assertEquals(9, this.prioqNaturalComparator.size());

        Assert.assertTrue(this.prioqNaturalComparator.values().contains(this.key1));
        Assert.assertTrue(this.prioqNaturalComparator.values().contains(this.key2));
        Assert.assertTrue(this.prioqNaturalComparator.values().contains(this.key3));
        Assert.assertFalse(this.prioqNaturalComparator.values().contains(this.key4)); //not in heap
        Assert.assertTrue(this.prioqNaturalComparator.values().contains(this.key5));
        Assert.assertFalse(this.prioqNaturalComparator.values().contains(this.key6)); //not in heap
        Assert.assertTrue(this.prioqNaturalComparator.values().contains(this.key7));
        Assert.assertTrue(this.prioqNaturalComparator.values().contains(this.key8));
    }

    /* */
    @Test
    public void testContainsValuesInverseComparator() {
        this.prioqInverseComparator.put(1, this.key1);
        this.prioqInverseComparator.put(2, this.key2);
        this.prioqInverseComparator.put(3, this.key1);
        this.prioqInverseComparator.put(4, this.key3);
        this.prioqInverseComparator.put(5, this.key7);
        this.prioqInverseComparator.put(6, this.key5);
        this.prioqInverseComparator.put(7, this.key1);
        this.prioqInverseComparator.put(8, this.key8);
        this.prioqInverseComparator.put(9, this.key2);

        Assert.assertEquals(9, this.prioqInverseComparator.size());

        Assert.assertTrue(this.prioqInverseComparator.values().contains(this.key1));
        Assert.assertTrue(this.prioqInverseComparator.values().contains(this.key2));
        Assert.assertTrue(this.prioqInverseComparator.values().contains(this.key3));
        Assert.assertFalse(this.prioqInverseComparator.values().contains(this.key4)); //not in heap
        Assert.assertTrue(this.prioqInverseComparator.values().contains(this.key5));
        Assert.assertFalse(this.prioqInverseComparator.values().contains(this.key6)); //not in heap
        Assert.assertTrue(this.prioqInverseComparator.values().contains(this.key7));
        Assert.assertTrue(this.prioqInverseComparator.values().contains(this.key8));
    }

    /* */
    @Test
    public void testIterable() {
        final int COUNT = (int) 5e4;
        final Random prng = new Random(57894132145L);

        this.prioq = new KTypeIndexedHeapPriorityQueue<KType>((int) 1e5);

        final HashMap<Integer, Integer> reference = new HashMap<Integer, Integer>();

        for (int i = 0; i < COUNT; i++) {
            final int index = prng.nextInt(COUNT);
            final int value = prng.nextInt(COUNT);

            if (!reference.containsKey(index)) {
                //cast twice to narrow conversion
                reference.put(index, castType(cast(value)));

                //add in map (there is a resize going on)
                this.prioq.put(index, cast(value));
            }
        }

        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.checkConsistency(this.prioq));
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(this.prioq));

        Assert.assertEquals(reference.size(), this.prioq.size());

        //iterate the prio queue to see if it fits
        int count = 0;
        for (final IntKTypeCursor<KType> cursor : this.prioq) {
            Assert.assertTrue(reference.containsKey(cursor.key));
            Assert.assertEquals(reference.get(cursor.key).intValue(), castType(cursor.value));
            Assert.assertEquals(castType(this.prioq.buffer[cursor.index]), castType(cursor.value));
            count++;
        }

        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.checkConsistency(this.prioq));
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(this.prioq));

        Assert.assertEquals(reference.size(), count);

        //iterate keys()
        count = 0;
        for (final IntCursor cursor : this.prioq.keys()) {
            Assert.assertTrue("" + cursor.value, reference.containsKey(cursor.value));
            Assert.assertEquals("" + cursor.value, reference.get(cursor.value).intValue(),
                    castType(this.prioq.buffer[cursor.index]));
            count++;
        }

        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.checkConsistency(this.prioq));
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(this.prioq));

        Assert.assertEquals(reference.size(), count);

        //iterate values()
        count = 0;
        for (final KTypeCursor<KType> cursor : this.prioq.values()) {
            Assert.assertEquals(castType(this.prioq.buffer[cursor.index]), castType(cursor.value));
            count++;
        }

        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.checkConsistency(this.prioq));
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(this.prioq));

        Assert.assertEquals(reference.size(), count);

        //try to iterate a void Prio queue
        count = 0;
        this.prioq.clear();
        Assert.assertEquals(0, this.prioq.size());
        Assert.assertTrue(this.prioq.isEmpty());

        //Access by key with topKey()
        Assert.assertEquals(-1, this.prioq.topKey());

        count = 0;
        for (final IntKTypeCursor<KType> cursor : this.prioq) {
            count++;
        }
        Assert.assertEquals(0, count);

        count = 0;
        for (final IntCursor cursor : this.prioq.keys()) {
            count++;
        }
        Assert.assertEquals(0, count);

        count = 0;
        for (final KTypeCursor<KType> cursor : this.prioq.values()) {
            count++;
        }
        Assert.assertEquals(0, count);
    }

    /* */
    @Test
    public void testIterator() {
        insertElements(this.prioq, 0, 0, 1, 1, 2, 2, 3, 3);

        final Iterator<IntKTypeCursor<KType>> iterator = this.prioq.iterator();

        int count = 0;

        while (iterator.hasNext()) {
            iterator.hasNext();
            iterator.hasNext();
            iterator.hasNext();
            iterator.next();
            count++;
        }
        Assert.assertEquals(count, this.prioq.size());

        this.prioq.clear();
        Assert.assertFalse(this.prioq.iterator().hasNext());

        //iterate keys()
        insertElements(this.prioq, 0, 0, 1, 1, 2, 2, 3, 3);
        final KTypeIndexedHeapPriorityQueue<KType>.KeysIterator iteratorKeys = this.prioq.keys().iterator();

        count = 0;

        while (iteratorKeys.hasNext()) {
            iteratorKeys.hasNext();
            iteratorKeys.hasNext();
            iteratorKeys.hasNext();
            iteratorKeys.next();
            count++;
        }
        Assert.assertEquals(count, this.prioq.size());

        this.prioq.clear();
        Assert.assertFalse(this.prioq.keys().iterator().hasNext());

        //iterate values()
        insertElements(this.prioq, 0, 0, 1, 1, 2, 2, 3, 3);
        final KTypeIndexedHeapPriorityQueue<KType>.ValuesIterator iteratorValues = this.prioq.values().iterator();

        count = 0;

        while (iteratorValues.hasNext()) {
            iteratorValues.hasNext();
            iteratorValues.hasNext();
            iteratorValues.hasNext();
            iteratorValues.next();
            count++;
        }
        Assert.assertEquals(count, this.prioq.size());

        this.prioq.clear();
        Assert.assertFalse(this.prioq.values().iterator().hasNext());
    }

    /* */
    @Test
    public void testForEachWithProcedure() {
        //A) Fill
        final int COUNT = (int) 1e4;
        final Random prng = new Random(154447431644L);

        final HashMap<Integer, Integer> reference = new HashMap<Integer, Integer>();

        long checksumValue = 0;
        long checksumKeys = 0;

        for (int i = 0; i < COUNT; i++) {
            final int index = prng.nextInt(COUNT);
            int value = prng.nextInt(COUNT);
            value = castType(cast(value));

            if (!reference.containsKey(index)) {
                //cast twice to narrow conversion
                reference.put(index, value);

                //add in map (there is a resize going on)
                this.prioq.put(index, cast(value));

                checksumValue += value;
                checksumKeys += index;
            }
        }

        Assert.assertEquals(reference.size(), this.prioq.size());

        //B) values() iterate and check that each value is taken
        final IntHolder val = new IntHolder();
        final IntHolder key = new IntHolder();
        final IntHolder count = new IntHolder();

        this.prioq.values().forEach(new KTypeProcedure<KType>() {

            @Override
            public void apply(final KType v) {
                val.value += castType(v);
                count.value++;
            }
        });

        Assert.assertEquals(reference.size(), count.value);

        Assert.assertEquals(checksumValue, val.value);

        //C) full forEach, test that each value is passed on to apply
        count.value = 0;
        val.value = 0;
        key.value = 0;

        this.prioq.forEach(new IntKTypeProcedure<KType>() {

            @Override
            public void apply(final int index, final KType value) {
                Assert.assertTrue(reference.containsKey(index));
                Assert.assertEquals(reference.get(index).intValue(), castType(value));
                val.value += castType(value);
                key.value += index;

                count.value++;
            }
        });

        Assert.assertEquals(reference.size(), count.value);

        Assert.assertEquals(checksumValue, val.value);
        Assert.assertEquals(checksumKeys, key.value);

        //D) forEach() on keys, test that each value is passed on to apply
        count.value = 0;
        val.value = 0;
        key.value = 0;

        this.prioq.keys().forEach(new IntProcedure() {

            @Override
            public void apply(final int index) {
                Assert.assertTrue(reference.containsKey(index));
                key.value += index;

                count.value++;
            }
        });

        Assert.assertEquals(reference.size(), count.value);

        Assert.assertEquals(checksumKeys, key.value);
    }

    /* */
    @Test
    public void testClear() {
        insertElements(this.prioq, 1, 2, 3, 4, 5, 6, 7, 8);
        this.prioq.clear();

        //Access by key with topKey()
        Assert.assertEquals(-1, this.prioq.topKey());
    }

    /* */
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testHashCodeEquals() {
        //A) Fill
        final int COUNT = (int) 1e4;
        final Random prng = new Random(1513164L);

        final HashMap<Integer, Integer> reference = new HashMap<Integer, Integer>();

        for (int i = 0; i < COUNT; i++) {
            final int index = prng.nextInt(COUNT);
            int value = prng.nextInt(COUNT);
            value = castType(cast(value));

            if (!reference.containsKey(index)) {
                //cast twice to narrow conversion
                reference.put(index, value);

                //add in map (there is a resize going on)
                this.prioq.put(index, cast(value));
            }
        }

        //create another
        final KTypeIndexedHeapPriorityQueue<KType> prioq2 = new KTypeIndexedHeapPriorityQueue<KType>(0);

        for (final int index : reference.keySet()) {
            prioq2.put(index, cast(reference.get(index)));
        }

        Assert.assertEquals(this.prioq.hashCode(), prioq2.hashCode());
    }

    /* */
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testClone() {
        insertElements(this.prioq, 1, 1, 2, 2, 3, 3, 4, 3);

        final KTypeIndexedHeapPriorityQueue<KType> cloned = this.prioq.clone();
        cloned.values().removeAll(this.key3);

        assertPrioQueueEquals(this.prioq, 1, 1, 2, 2, 3, 3, 4, 3);
        assertPrioQueueEquals(cloned, 1, 1, 2, 2);

        checkConsistencyAfter(cloned);
    }

    @Repeat(iterations = 10)
    @Test
    public void testSyntheticComparable() {
        //A) Fill
        final int COUNT = (int) 1e4;
        final Random prng = RandomizedTest.getRandom();

        final KTypeIndexedHeapPriorityQueue<KType> testPQ = new KTypeIndexedHeapPriorityQueue<KType>(10);
        final HashMap<Integer, Integer> reference = new HashMap<Integer, Integer>();

        for (int i = 0; i < COUNT; i++) {
            final int index = prng.nextInt(COUNT);
            int value = prng.nextInt(COUNT);
            value = castType(cast(value));

            if (!reference.containsKey(index)) {
                //cast twice to narrow conversion
                reference.put(index, value);

                //add in map (there is a resize going on)
                testPQ.put(index, cast(value));
            }

            if (i % 4001 == 0) {
                KTypeIndexedHeapPriorityQueueTest.checkConsistency(this.prioq);
                Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(this.prioq));
            }
        }

        KTypeIndexedHeapPriorityQueueTest.checkConsistency(this.prioq);
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(this.prioq));

        //fill reference array
        final int[] referenceArray = new int[reference.size()];

        int ii = 0;
        for (final int value : reference.values()) {

            referenceArray[ii] = value;
            ii++;
        }

        //B) popTop elements one by one
        //they are supposed to come in natural order
        Arrays.sort(referenceArray);

        int currentSize = referenceArray.length;

        for (int i = 0; i < referenceArray.length - 1; i++) {
            final int expected = referenceArray[i];
            final int expected_next = referenceArray[i + 1];

            Assert.assertEquals(expected, castType(testPQ.top()));

            //size doesn't change
            Assert.assertEquals(currentSize, testPQ.size());
            Assert.assertEquals(expected, castType(testPQ.top()));

            //Access by key with topKey()
            Assert.assertTrue(testPQ.topKey() != -1);
            Assert.assertEquals(expected, castType(testPQ.get(testPQ.topKey())));

            Assert.assertEquals(expected, castType(testPQ.popTop()));
            //size is smaller by one element
            Assert.assertEquals(currentSize - 1, testPQ.size());
            //top() point now to the next smallest element
            Assert.assertEquals(expected_next, castType(testPQ.top()));

            //Access by key with topKey()
            Assert.assertTrue(testPQ.topKey() != -1);
            Assert.assertEquals(expected_next, castType(testPQ.get(testPQ.topKey())));

            if (i % 30711 == 0) {
                KTypeIndexedHeapPriorityQueueTest.checkConsistency(this.prioq);
                Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(this.prioq));
            }

            currentSize--;
        }

        Assert.assertEquals(1, testPQ.size());
        testPQ.clear();
        Assert.assertTrue(testPQ.isEmpty());

        //Access by key with topKey()
        Assert.assertEquals(-1, testPQ.topKey());
    }

    @Test
    public void testEqualsComparable() {
        final Random prng = new Random(87541321464L);

        final int COUNT = (int) 100e3;

        //A) fill COUNT random values in prio-queue
        final KTypeIndexedHeapPriorityQueue<KType> testPQ = new KTypeIndexedHeapPriorityQueue<KType>(10);
        final KTypeIndexedHeapPriorityQueue<KType> testPQ2 = new KTypeIndexedHeapPriorityQueue<KType>((int) 1e6);

        for (int i = 0; i < COUNT; i++) {
            //use a Random number on a small enough range so that it can be exactly represented
            //in Float or Double with no conversion error back and forth.
            final int randomInt = prng.nextInt(1000 * 1000);

            testPQ.put(i, cast(randomInt));
            testPQ2.put(i, cast(randomInt));
        } //end for

        Assert.assertEquals(COUNT, testPQ.size());
        Assert.assertEquals(COUNT, testPQ2.size());

        //B) compare both
        Assert.assertTrue(testPQ.equals(testPQ2));
    }

    @Test
    public void testEqualsComparator() {
        //Inverse natural ordering comparator
        final KTypeComparator<KType> compIndepinstance = new KTypeComparator<KType>() {

            @Override
            public int compare(final KType e1, final KType e2) {
                int res = 0;

                if (castType(e1) < castType(e2)) {
                    res = 1;
                } else if (castType(e1) > castType(e2)) {
                    res = -1;
                }

                return res;
            }
        };

        //Inverse natural ordering comparator
        final KTypeComparator<KType> compIndepinstance2 = new KTypeComparator<KType>() {

            @Override
            public int compare(final KType e1, final KType e2) {
                int res = 0;

                if (castType(e1) < castType(e2)) {
                    res = 1;
                } else if (castType(e1) > castType(e2)) {
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
            public int compare(final KType e1, final KType e2) {
                int res = 0;

                if (castType(e1) < castType(e2)) {
                    res = 1;
                } else if (castType(e1) > castType(e2)) {
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
            public int compare(final KType e1, final KType e2) {
                int res = 0;

                if (castType(e1) < castType(e2)) {
                    res = 1;
                } else if (castType(e1) > castType(e2)) {
                    res = -1;
                }

                return res;
            }
        };

        final Random prng = new Random(74743233156464L);

        final int COUNT = (int) 100e3;

        //A) fill COUNT random values in prio-queue
        final KTypeIndexedHeapPriorityQueue<KType> testPQ = new KTypeIndexedHeapPriorityQueue<KType>(compIndepinstance, 10);
        final KTypeIndexedHeapPriorityQueue<KType> testPQSameInstance = new KTypeIndexedHeapPriorityQueue<KType>(
                compIndepinstance, 25);
        final KTypeIndexedHeapPriorityQueue<KType> testPQOtherInstance = new KTypeIndexedHeapPriorityQueue<KType>(
                compIndepinstance2, 1000);
        final KTypeIndexedHeapPriorityQueue<KType> testPQSame = new KTypeIndexedHeapPriorityQueue<KType>(compAlwaysSame,
                15000);
        final KTypeIndexedHeapPriorityQueue<KType> testPQSame2 = new KTypeIndexedHeapPriorityQueue<KType>(compAlwaysSame2,
                745415);

        for (int i = 0; i < COUNT; i++) {
            //use a Random number on a small enough range so that it can be exactly represented
            //in Float or Double with no conversion error back and forth.
            final int randomInt = prng.nextInt(1000 * 1000);

            testPQ.put(i, cast(randomInt));
            testPQSameInstance.put(i, cast(randomInt));
            testPQOtherInstance.put(i, cast(randomInt));
            testPQSame.put(i, cast(randomInt));
            testPQSame2.put(i, cast(randomInt));

        } //end for

        //B) compare both

        //same instance , by reference
        Assert.assertTrue(testPQ.equals(testPQSameInstance));

        //different instances, diff by default
        Assert.assertFalse(testPQ.equals(testPQOtherInstance));

        //different instances, but properly compared because instances are equals() = true
        Assert.assertTrue(testPQSame.equals(testPQSame2));
    }

    @Repeat(iterations = 10)
    @Test
    public void testUpdatePrioritiesComparable() {
        /*! #if ($TemplateOptions.isKType("GENERIC", "int", "long", "float", "double")) !*/
        final int COUNT = (int) 1e4;
        /*! #elseif($TemplateOptions.isKType("short", "char"))
         final int COUNT = (int) 1e3;
        #else
          final int COUNT = (int) (126 * 0.5);
        #end !*/

        //A) fill COUNT random values in prio-queue
        final KTypeIndexedHeapPriorityQueue<KType> testPQ = new KTypeIndexedHeapPriorityQueue<KType>(10);

        for (int i = 0; i < COUNT; i++) {
            //use unique values so that swapping values always disturb the heap property
            //values are between [COUNT, 2* COUNT]
            testPQ.put(i, cast(2 * COUNT - i));
        }

        Assert.assertEquals(COUNT, testPQ.size());
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(testPQ));
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.checkConsistency(testPQ));

        //B) Directly change elements of the buffer, so that the heap property is not respected anymore:
        final int NB_CHANGES = RandomizedTest.randomIntBetween(2, (int) (0.5 * COUNT));

        for (int i = 0; i < NB_CHANGES; i++) {
            //start at index, 2 because exclude the index of min (the top()), which change would not
            //destroy the heap property !!
            final int changedPosition = RandomizedTest.randomIntBetween(2, COUNT - 2);

            final int newValue = RandomizedTest.randomIntBetween(0, COUNT - 2);

            testPQ.buffer[changedPosition] = (KType) cast(newValue);
        }

        //no longer a heap
        Assert.assertEquals(COUNT, testPQ.size());
        Assert.assertFalse(KTypeIndexedHeapPriorityQueueTest.isMinHeap(testPQ));

        //but still consistent !
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.checkConsistency(testPQ));

        //C) Reestablish
        testPQ.updatePriorities();

        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(testPQ));
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.checkConsistency(testPQ));
    }

    @Test
    public void testUpdateTopPriorityComparable() {
        /*! #if ($TemplateOptions.isKType("GENERIC", "int", "long", "float", "double")) !*/
        final int COUNT = (int) 1e4;
        /*! #elseif($TemplateOptions.isKType("short", "char"))
         final int COUNT = (int) 1e3;
        #else
          final int COUNT = (int) (126 * 0.5);
        #end !*/

        //A) fill COUNT random values in prio-queue
        final KTypeIndexedHeapPriorityQueue<KType> testPQ = new KTypeIndexedHeapPriorityQueue<KType>(10);

        for (int i = 0; i < COUNT; i++) {
            //use unique values so that swapping values always disturb the heap property
            //values are between [0, COUNT]
            testPQ.put(i, cast(i));
        }

        Assert.assertEquals(COUNT, testPQ.size());
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(testPQ));
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.checkConsistency(testPQ));

        //B) Directly change elements of the buffer, so that the heap property is not respected anymore:
        final int NB_CHANGES = (int) (COUNT * 0.45);

        for (int i = 0; i < NB_CHANGES; i++) {
            // Change the top() with a value bigger than any previous value, garanteeed to destroy the heap
            final int newValue = COUNT + i;
            testPQ.buffer[1] = (KType) cast(newValue);
        }

        //no longer a heap
        Assert.assertEquals(COUNT, testPQ.size());
        Assert.assertFalse(KTypeIndexedHeapPriorityQueueTest.isMinHeap(testPQ));

        //but still consistent !
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.checkConsistency(testPQ));

        //C) Reestablish
        testPQ.updateTopPriority();

        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(testPQ));
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.checkConsistency(testPQ));
    }

    @Repeat(iterations = 10)
    @Test
    public void testUpdatePriorityComparable() {
        /*! #if ($TemplateOptions.isKType("GENERIC", "int", "long", "float", "double")) !*/
        final int COUNT = (int) 1e4;
        /*! #elseif($TemplateOptions.isKType("short", "char"))
         final int COUNT = (int) 1e3;
        #else
          final int COUNT = (int) (126 * 0.5);
        #end !*/

        //A) fill COUNT random values in prio-queue
        final KTypeIndexedHeapPriorityQueue<KType> testPQ = new KTypeIndexedHeapPriorityQueue<KType>(10);

        for (int i = 0; i < COUNT; i++) {
            //use unique values so that swapping values always disturb the heap property
            //values are between [COUNT, 2* COUNT]
            testPQ.put(i, cast(2 * COUNT - i));
        }

        Assert.assertEquals(COUNT, testPQ.size());
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(testPQ));
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.checkConsistency(testPQ));

        //B) Directly change elements of the buffer, so that the heap property is not respected anymore:
        final int NB_CHANGES = RandomizedTest.randomIntBetween(2, (int) (0.5 * COUNT));

        int minValue = COUNT - 2;

        // Reestablish on the flow
        for (int i = 0; i < NB_CHANGES; i++) {
            //start at index, 2 because exclude the index of min (the top()), which change would not
            //destroy the heap property !!
            final int changedPosition = RandomizedTest.randomIntBetween(2, COUNT - 2);

            final int changedIndex = testPQ.qp[changedPosition];

            //values are between [COUNT, 2* COUNT], so change with a value < actual min value !, garanteed to destroy the heap property.

            final int newValue = RandomizedTest.randomIntBetween(Math.min(0, minValue), Math.max(0, minValue));

            if (newValue >= minValue) {

                //the new min value is not < the previous one, so the test cannot continue.
                break;
            }

            testPQ.buffer[changedPosition] = (KType) cast(newValue);

            //no longer a heap
            Assert.assertFalse(KTypeIndexedHeapPriorityQueueTest.isMinHeap(testPQ));

            //but still consistent !
            Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.checkConsistency(testPQ));

            //access back by index and reestablish the heap property
            testPQ.updatePriority(changedIndex);

            //this is again a heap
            Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(testPQ));
            Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.checkConsistency(testPQ));

            //Ah yes but we have a new min !
            minValue = newValue;
        }
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/

    @Repeat(iterations = 10)
    @Test
    public void testUpdatePrioritiesComparator() {
        //INVERSE natural ordering comparator, so it will create a MAX priority queue !
        final Comparator<? super TestHolder> comp = new Comparator<TestHolder>() {

            @Override
            public int compare(final TestHolder e1, final TestHolder e2) {
                return (-1) * Intrinsics.compareKTypeUnchecked(e1.value, e2.value);
            }
        };

        /*! #if ($TemplateOptions.isKType("GENERIC", "int", "long", "float", "double")) !*/
        final int COUNT = (int) 1e4;
        /*! #elseif($TemplateOptions.isKType("short", "char"))
         final int COUNT = (int) 1e3;
        #else
          final int COUNT = (int) (126 * 0.5);
        #end !*/

        //A) fill COUNT random values in prio-queue
        final KTypeIndexedHeapPriorityQueue<TestHolder> testPQ = new KTypeIndexedHeapPriorityQueue<TestHolder>(comp, 10);

        for (int i = 0; i < COUNT; i++) {
            //values are between [0, COUNT]
            testPQ.put(i, new TestHolder(cast(COUNT - i)));
        }

        Assert.assertEquals(COUNT, testPQ.size());
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(testPQ));
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.checkConsistency(testPQ));

        //B) Directly change elements of the buffer, so that the heap property is not respected anymore:
        final int NB_CHANGES = RandomizedTest.randomIntBetween(2, (int) (0.5 * COUNT));

        for (int i = 0; i < NB_CHANGES; i++) {
            //start at index, 2 because exclude the index of max (the top()), which change would not
            //destroy the heap property !!
            final int changedPosition = RandomizedTest.randomIntBetween(2, COUNT - 2);

            final int changedIndex = testPQ.qp[changedPosition];

            //values are between [0, COUNT], so change with a value >  max, garanteed to destroy the heap property.
            final KType newValue = cast(RandomizedTest.randomIntBetween(COUNT, 2 * COUNT));

            testPQ.get(changedIndex).value = newValue;
        }

        //no longer a heap
        Assert.assertEquals(COUNT, testPQ.size());
        Assert.assertFalse(KTypeIndexedHeapPriorityQueueTest.isMinHeap(testPQ));

        //but still consistent !
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.checkConsistency(testPQ));

        //C) Reestablish
        testPQ.updatePriorities();

        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(testPQ));
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.checkConsistency(testPQ));
    }

    @Repeat(iterations = 10)
    @Test
    public void testUpdatePriorityComparator() {
        //INVERSE natural ordering comparator, so it will create a MAX priority queue !
        final Comparator<? super TestHolder> comp = new Comparator<TestHolder>() {

            @Override
            public int compare(final TestHolder e1, final TestHolder e2) {
                return (-1) * Intrinsics.compareKTypeUnchecked(e1.value, e2.value);
            }
        };

        /*! #if ($TemplateOptions.isKType("GENERIC", "int", "long", "float", "double")) !*/
        final int COUNT = (int) 1e4;
        /*! #elseif($TemplateOptions.isKType("short", "char"))
         final int COUNT = (int) 1e3;
        #else
          final int COUNT = (int) (126 * 0.5);
        #end !*/

        //A) fill COUNT random values in prio-queue
        final KTypeIndexedHeapPriorityQueue<TestHolder> testPQ = new KTypeIndexedHeapPriorityQueue<TestHolder>(comp, 10);

        for (int i = 0; i < COUNT; i++) {
            //use unique values so that changing values always disturb the heap property
            //values are between [0,  COUNT]
            testPQ.put(i, new TestHolder(cast(COUNT - i)));
        }

        Assert.assertEquals(COUNT, testPQ.size());
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(testPQ));
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.checkConsistency(testPQ));

        final int NB_CHANGES = RandomizedTest.randomIntBetween(2, (int) (0.5 * COUNT));

        int maxValue = COUNT + 2;

        //Reestablish on the flow
        for (int i = 0; i < NB_CHANGES; i++) {
            //start at index, 2 because exclude the index of max (the top()), which change would not
            //destroy the heap property !!
            final int changedPosition = RandomizedTest.randomIntBetween(2, COUNT - 2);

            final int changedIndex = testPQ.qp[changedPosition];

            //values are between [0,  COUNT], so change with a value > actual max !, garanteed to destroy the heap property.

            final KType newValue = cast(RandomizedTest.randomIntBetween(Math.min(maxValue, 2 * COUNT),
                    Math.max(maxValue, 2 * COUNT)));

            if (castType(newValue) <= maxValue) {

                //the new max value is not > the previous one, so the test cannot continue.
                break;
            }

            testPQ.get(changedIndex).value = newValue;

            //no longer a heap
            Assert.assertFalse("index of change: " + i, KTypeIndexedHeapPriorityQueueTest.isMinHeap(testPQ));

            //but still consistent !
            Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.checkConsistency(testPQ));

            //access back by index and reestablish the heap property
            testPQ.updatePriority(changedIndex);

            //this is again a heap
            Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(testPQ));
            Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.checkConsistency(testPQ));

            //Ah yes but we have a new max !
            maxValue = castType(newValue);
        }
    }

    @Test
    public void testUpdateTopPriorityComparator() {
        //INVERSE natural ordering comparator, so it will create a MAX priority queue !
        final Comparator<? super TestHolder> comp = new Comparator<TestHolder>() {

            @Override
            public int compare(final TestHolder e1, final TestHolder e2) {
                return (-1) * Intrinsics.compareKTypeUnchecked(e1.value, e2.value);
            }
        };

        /*! #if ($TemplateOptions.isKType("GENERIC", "int", "long", "float", "double")) !*/
        final int COUNT = (int) 1e4;
        /*! #elseif($TemplateOptions.isKType("short", "char"))
         final int COUNT = (int) 1e3;
        #else
          final int COUNT = (int) (126 * 0.5);
        #end !*/

        //A) fill COUNT random values in prio-queue
        final KTypeIndexedHeapPriorityQueue<TestHolder> testPQ = new KTypeIndexedHeapPriorityQueue<TestHolder>(comp, 10);

        for (int i = 0; i < COUNT; i++) {
            //use unique values so that changing values always disturb the heap property
            //values are between [COUNT,  2* COUNT]
            testPQ.put(i, new TestHolder(cast(2 * COUNT - i)));
        }

        Assert.assertEquals(COUNT, testPQ.size());
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(testPQ));
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.checkConsistency(testPQ));

        final int NB_CHANGES = (int) (0.45 * COUNT);

        //Reestablish on the flow
        for (int i = 0; i < NB_CHANGES; i++) {
            //change value at position 1 = top()

            //values are between [COUNT,  2* COUNT], so change with a value < COUNT, garanteed to destroy the heap property.
            final KType newValue = cast(COUNT - i - 1);

            testPQ.get(testPQ.topKey()).value = newValue;

            //no longer a heap
            Assert.assertFalse("index of change: " + i, KTypeIndexedHeapPriorityQueueTest.isMinHeap(testPQ));

            //but still consistent !
            Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.checkConsistency(testPQ));

            //update the top()
            testPQ.updateTopPriority();

            //this is again a heap
            Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(testPQ));
            Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.checkConsistency(testPQ));
        }
    }

    /*! #end !*/

    @Repeat(iterations = 10)
    @Test
    public void testSyntheticComparator() {
        //INVERSE natural ordering comparator
        final KTypeComparator<KType> comp = new KTypeComparator<KType>() {

            @Override
            public int compare(final KType e1, final KType e2) {
                int res = 0;

                if (castType(e1) < castType(e2)) {
                    res = 1;
                } else if (castType(e1) > castType(e2)) {
                    res = -1;
                }

                return res;
            }
        };

        final Random prng = RandomizedTest.getRandom();

        final int COUNT = (int) 1e4;

        //A) fill COUNT random values in prio-queue
        final KTypeIndexedHeapPriorityQueue<KType> testPQ = new KTypeIndexedHeapPriorityQueue<KType>(comp, 10);
        final HashMap<Integer, Integer> reference = new HashMap<Integer, Integer>();

        for (int i = 0; i < COUNT; i++) {
            final int index = prng.nextInt(COUNT);
            int value = prng.nextInt(COUNT);
            value = castType(cast(value));

            if (!reference.containsKey(index)) {
                //cast twice to narrow conversion
                reference.put(index, value);

                //add in map (there is a resize going on)
                testPQ.put(index, cast(value));
            }

            if (i % 27001 == 0) {
                KTypeIndexedHeapPriorityQueueTest.checkConsistency(this.prioq);
                Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(this.prioq));
            }
        }

        KTypeIndexedHeapPriorityQueueTest.checkConsistency(this.prioq);
        Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(this.prioq));

        //fill reference array
        final KType[] referenceArray = Intrinsics.newKTypeArray(reference.size());

        int ii = 0;
        for (final int value : reference.values()) {
            referenceArray[ii] = cast(value);
            ii++;
        }

        //B) popTop elements one by one
        //they are supposed to come in inverse-natural order
        KTypeSort.quicksort(referenceArray, comp);

        int currentSize = referenceArray.length;

        for (int i = 0; i < referenceArray.length - 1; i++) {
            final KType expected = referenceArray[i];
            final KType expected_next = referenceArray[i + 1];

            //assure it is indeed in inverse natural order : bigger first
            Assert.assertTrue(castType(expected) >= castType(expected_next));

            Assert.assertEquals(castType(expected), castType(testPQ.top()));
            //size doesn't change
            Assert.assertEquals(currentSize, testPQ.size());
            Assert.assertEquals(castType(expected), castType(testPQ.top()));

            //Access by key with topKey()
            Assert.assertTrue(testPQ.topKey() != -1);
            Assert.assertEquals(castType(expected), castType(testPQ.get(testPQ.topKey())));

            Assert.assertEquals(castType(expected), castType(testPQ.popTop()));
            //size is smaller by one element
            Assert.assertEquals(currentSize - 1, testPQ.size());
            //top() point now to the next smallest element
            Assert.assertEquals(castType(expected_next), castType(testPQ.top()));

            //Access by key with topKey()
            Assert.assertTrue(testPQ.topKey() != -1);
            Assert.assertEquals(castType(expected_next), castType(testPQ.get(testPQ.topKey())));

            if (i % 31117 == 0) {
                KTypeIndexedHeapPriorityQueueTest.checkConsistency(this.prioq);
                Assert.assertTrue(KTypeIndexedHeapPriorityQueueTest.isMinHeap(this.prioq));
            }

            currentSize--;
        }

        Assert.assertEquals(1, testPQ.size());
        testPQ.clear();
        Assert.assertTrue(testPQ.isEmpty());

        //Access by key with topKey()
        Assert.assertEquals(-1, testPQ.topKey());
    }

    @Test
    public void testPooledIteratorForEach() {
        // Unbroken for-each loop
        final int TEST_SIZE = 5000;
        final long TEST_ROUNDS = 100;

        final KTypeIndexedHeapPriorityQueue<KType> testContainer = new KTypeIndexedHeapPriorityQueue<KType>(10);

        //fill pq
        final Random prng = new Random(215649612148461L);

        for (int i = 0; i < TEST_SIZE; i++) {
            testContainer.put(prng.nextInt(TEST_SIZE), cast(prng.nextInt()));
        }

        final long checksum = testContainer.forEach(new IntKTypeProcedure<KType>() {

            long count;

            @Override
            public void apply(final int key, final KType value) {
                this.count += castType(value) + key;
            }
        }).count;

        long testValue = 0;
        final long initialPoolSize = testContainer.entryIteratorPool.size();

        for (int round = 0; round < TEST_ROUNDS; round++) {
            //for-each in test :
            testValue = 0;
            for (final IntKTypeCursor<KType> cursor : testContainer) {
                //we consume 1 iterator for this loop
                Assert.assertEquals(initialPoolSize - 1, testContainer.entryIteratorPool.size());

                testValue += castType(cursor.value) + cursor.key;
            }

            //check checksum the iteration
            Assert.assertEquals(checksum, testValue);

            //iterator is returned to its pool
            Assert.assertEquals(initialPoolSize, testContainer.entryIteratorPool.size());
        } //end for rounds
    }

    @Test
    public void testPooledIteratorBrokenForEach() {
        // for-each loop interrupted

        final int TEST_SIZE = 5000;
        final long TEST_ROUNDS = 100;

        final KTypeIndexedHeapPriorityQueue<KType> testContainer = new KTypeIndexedHeapPriorityQueue<KType>(10);

        //fill pq
        final Random prng = new Random(484163441L);

        for (int i = 0; i < TEST_SIZE; i++) {

            testContainer.put(prng.nextInt(TEST_SIZE), cast(prng.nextInt()));
        }

        int count = 0;
        for (int round = 0; round < TEST_ROUNDS; round++) {
            //for-each in test :
            final long initialPoolSize = testContainer.entryIteratorPool.size();

            count = 0;
            for (final IntKTypeCursor<KType> cursor : testContainer) {
                this.guard += castType(cursor.value) + cursor.key;
                //we consume 1 iterator for this loop, but reallocs can happen,
                //so we can only say its != initialPoolSize
                Assert.assertTrue(initialPoolSize != testContainer.entryIteratorPool.size());

                //brutally interrupt in the middle
                if (count > TEST_SIZE / 2) {
                    break;
                }

                count++;
            } //end for-each

            //iterator is NOT returned to its pool, due to the break.
            //reallocation could happen, so that the only testable thing
            //is that the size is != full pool
            Assert.assertTrue(initialPoolSize != testContainer.entryIteratorPool.size());
        } //end for rounds

        //Due to policy of the Iterator pool, the intended pool never get bigger that some limit
        //despite the Iterator leak.
        Assert.assertTrue(testContainer.entryIteratorPool.capacity() < IteratorPool.getMaxPoolSize() + 1);
    }

    @Test
    public void testPooledIteratorFullIteratorLoop() {
        // for-each loop interrupted

        final int TEST_SIZE = 5000;
        final long TEST_ROUNDS = 100;

        final KTypeIndexedHeapPriorityQueue<KType> testContainer = new KTypeIndexedHeapPriorityQueue<KType>(10);

        //fill pq
        final Random prng = new Random(89874156187414L);

        for (int i = 0; i < TEST_SIZE; i++) {
            testContainer.put(prng.nextInt(TEST_SIZE), cast(prng.nextInt()));
        }

        final long checksum = testContainer.forEach(new IntKTypeProcedure<KType>() {

            long count;

            @Override
            public void apply(final int key, final KType value) {
                this.count += castType(value) + key;
            }
        }).count;

        long testValue = 0;
        final int startingPoolSize = testContainer.entryIteratorPool.size();

        for (int round = 0; round < TEST_ROUNDS; round++) {
            //Classical iterator loop, with manually allocated Iterator
            final int initialPoolSize = testContainer.entryIteratorPool.size();

            final KTypeIndexedHeapPriorityQueue<KType>.EntryIterator loopIterator = testContainer.iterator();

            Assert.assertEquals(initialPoolSize - 1, testContainer.entryIteratorPool.size());

            testValue = 0;
            while (loopIterator.hasNext()) {
                final IntKTypeCursor<KType> c = loopIterator.next();
                testValue += castType(c.value) + c.key;
            } //end IteratorLoop

            //iterator is returned automatically to its pool, by normal iteration termination
            Assert.assertEquals(initialPoolSize, testContainer.entryIteratorPool.size());

            //checksum
            Assert.assertEquals(checksum, testValue);
        } //end for rounds

        // pool initial size is untouched anyway
        Assert.assertEquals(startingPoolSize, testContainer.entryIteratorPool.size());
    }

    @Test
    public void testPooledIteratorBrokenIteratorLoop() {
        // for-each loop interrupted

        final int TEST_SIZE = 5000;
        final long TEST_ROUNDS = 100;

        final KTypeIndexedHeapPriorityQueue<KType> testContainer = new KTypeIndexedHeapPriorityQueue<KType>(10);

        //fill pq
        final Random prng = new Random(994610788L);

        for (int i = 0; i < TEST_SIZE; i++) {
            testContainer.put(prng.nextInt(TEST_SIZE), cast(prng.nextInt()));
        }

        final int startingPoolSize = testContainer.entryIteratorPool.size();

        int count = 0;
        for (int round = 0; round < TEST_ROUNDS; round++) {
            //Classical iterator loop, with manually allocated Iterator
            final long initialPoolSize = testContainer.entryIteratorPool.size();

            final KTypeIndexedHeapPriorityQueue<KType>.EntryIterator loopIterator = testContainer.iterator();

            Assert.assertEquals(initialPoolSize - 1, testContainer.entryIteratorPool.size());

            count = 0;
            while (loopIterator.hasNext()) {
                final IntKTypeCursor<KType> c = loopIterator.next();
                this.guard += castType(c.value) + c.index;

                //brutally interrupt in the middle
                if (count > TEST_SIZE / 2) {
                    break;
                }
                count++;
            } //end IteratorLoop

            //iterator is NOT returned to its pool, due to the break.
            Assert.assertEquals(initialPoolSize - 1, testContainer.entryIteratorPool.size());

            //manual return to the pool
            loopIterator.release();

            //now the pool is restored
            Assert.assertEquals(initialPoolSize, testContainer.entryIteratorPool.size());

        } //end for rounds

        // pool initial size is untouched anyway
        Assert.assertEquals(startingPoolSize, testContainer.entryIteratorPool.size());
    }

    @Test
    public void testPooledIteratorExceptionIteratorLoop() {
        final int TEST_SIZE = 5000;
        final long TEST_ROUNDS = 100;

        final KTypeIndexedHeapPriorityQueue<KType> testContainer = new KTypeIndexedHeapPriorityQueue<KType>(10);

        //fill pq
        final Random prng = new Random(78411114444L);

        for (int i = 0; i < TEST_SIZE; i++) {
            testContainer.put(prng.nextInt(TEST_SIZE), cast(prng.nextInt()));
        }

        final long checksum = testContainer.forEach(new IntKTypeProcedure<KType>() {

            long count;

            @Override
            public void apply(final int key, final KType value) {
                this.count += castType(value) + key;
            }
        }).count;

        final int startingPoolSize = testContainer.entryIteratorPool.size();

        int count = 0;
        KTypeIndexedHeapPriorityQueue<KType>.EntryIterator loopIterator = null;

        for (int round = 0; round < TEST_ROUNDS; round++) {
            try {
                loopIterator = testContainer.iterator();
                Assert.assertEquals(startingPoolSize - 1, testContainer.entryIteratorPool.size());

                this.guard = 0;
                count = 0;
                while (loopIterator.hasNext()) {
                    final IntKTypeCursor<KType> c = loopIterator.next();
                    this.guard += castType(c.value) + c.key;

                    //brutally interrupt in the middle some of the loops, but not all
                    if (round > TEST_ROUNDS / 2 && count > TEST_SIZE / 2) {
                        throw new Exception("Oups some problem in the loop occured");
                    }
                    count++;
                } //end while

                //iterator is returned to its pool in case of normal loop termination
                Assert.assertEquals(startingPoolSize, testContainer.entryIteratorPool.size());
                Assert.assertEquals(checksum, this.guard);

            } catch (final Exception e) {
                //iterator is NOT returned to its pool because of the exception
                Assert.assertEquals(startingPoolSize - 1, testContainer.entryIteratorPool.size());

                //manual return to the pool then
                loopIterator.release();

                //now the pool is restored
                Assert.assertEquals(startingPoolSize, testContainer.entryIteratorPool.size());
            }
        } //end for rounds

        // pool initial size is untouched anyway
        Assert.assertEquals(startingPoolSize, testContainer.entryIteratorPool.size());
    }

    @Test
    public void testPooledIteratorExceptionSafe() {
        final int TEST_SIZE = 15171;
        final long TEST_ROUNDS = 15;

        final KTypeIndexedHeapPriorityQueue<KType> testContainer = new KTypeIndexedHeapPriorityQueue<KType>(10000);

        //fill pq
        final Random prng = new Random(9774442154544L);

        for (int i = 0; i < TEST_SIZE; i++) {
            testContainer.put(prng.nextInt(TEST_SIZE), cast(prng.nextInt()));
        }

        final long checksum = testContainer.forEach(new IntKTypeProcedure<KType>() {

            long count;

            @Override
            public void apply(final int key, final KType value) {
                this.count += castType(value) + key;
            }
        }).count;

        final int initialPoolSize = testContainer.entryIteratorPool.size();

        //start with a non full pool, remove 3 elements
        final KTypeIndexedHeapPriorityQueue<KType>.EntryIterator loopIteratorFake = testContainer.iterator();
        final KTypeIndexedHeapPriorityQueue<KType>.EntryIterator loopIteratorFake2 = testContainer.iterator();
        final KTypeIndexedHeapPriorityQueue<KType>.EntryIterator loopIteratorFake3 = testContainer.iterator();

        final int startingTestPoolSize = testContainer.entryIteratorPool.size();

        Assert.assertEquals(initialPoolSize - 3, startingTestPoolSize);

        int count = 0;
        KTypeIndexedHeapPriorityQueue<KType>.EntryIterator loopIterator = null;

        for (int round = 0; round < TEST_ROUNDS; round++) {
            try {
                loopIterator = testContainer.iterator();
                Assert.assertEquals(startingTestPoolSize - 1, testContainer.entryIteratorPool.size());

                this.guard = 0;
                count = 0;

                while (loopIterator.hasNext()) {
                    final IntKTypeCursor<KType> c = loopIterator.next();
                    this.guard += castType(c.value) + c.key;

                    //brutally interrupt in the middle some of the loops, but not all
                    if (round > TEST_ROUNDS / 2 && count > TEST_SIZE / 2) {
                        throw new Exception("Oups some problem in the loop occured");
                    }
                    count++;
                } //end while

                //iterator is returned to its pool in case of normal loop termination
                Assert.assertEquals(startingTestPoolSize, testContainer.entryIteratorPool.size());
                Assert.assertEquals(checksum, this.guard);

                //still, try to return it ....
                loopIterator.release();

                //nothing has changed
                Assert.assertEquals(startingTestPoolSize, testContainer.entryIteratorPool.size());
            } catch (final Exception e) {
                //iterator is NOT returned to its pool because of the exception
                Assert.assertEquals(startingTestPoolSize - 1, testContainer.entryIteratorPool.size());

                //manual return to the pool then
                loopIterator.release();

                //now the pool is restored
                Assert.assertEquals(startingTestPoolSize, testContainer.entryIteratorPool.size());

                //continue to try to release...
                loopIterator.release();

                //nothing has changed
                Assert.assertEquals(startingTestPoolSize, testContainer.entryIteratorPool.size());
            }
        } //end for rounds

        // pool initial size is untouched anyway
        Assert.assertEquals(startingTestPoolSize, testContainer.entryIteratorPool.size());

        //finally return the fake ones, several times
        loopIteratorFake.release();
        loopIteratorFake.release();
        loopIteratorFake.release();
        loopIteratorFake2.release();
        loopIteratorFake2.release();
        loopIteratorFake2.release();
        loopIteratorFake3.release();
        loopIteratorFake3.release();

        Assert.assertEquals(initialPoolSize, testContainer.entryIteratorPool.size());
    }

    @Repeat(iterations = 10)
    @Test
    public void testPreallocatedSize() {
        final Random randomVK = RandomizedTest.getRandom();
        //Test that the container do not resize if less that the initial size

        //1) Choose a random number of elements
        /*! #if ($TemplateOptions.isKType("GENERIC", "INT", "LONG", "FLOAT", "DOUBLE")) !*/
        final int PREALLOCATED_SIZE = randomVK.nextInt(10000);
        /*!
            #elseif ($TemplateOptions.isKType("SHORT", "CHAR"))
             int PREALLOCATED_SIZE = randomVK.nextInt(1500);
            #else
              int PREALLOCATED_SIZE = randomVK.nextInt(126);
            #end !*/

        //2) Preallocate to PREALLOCATED_SIZE :
        final KTypeIndexedHeapPriorityQueue<KType> newHeap = new KTypeIndexedHeapPriorityQueue<KType>(PREALLOCATED_SIZE);

        //computed real capacity
        final int realCapacity = newHeap.capacity();

        //3) Add PREALLOCATED_SIZE different values. At the end, size() must be == realCapacity,
        //and internal buffer/allocated must not have changed of size
        final int contructorBufferSize = newHeap.buffer.length;

        Assert.assertEquals(contructorBufferSize, newHeap.buffer.length);

        for (int i = 0; i < 1.5 * realCapacity; i++) {

            newHeap.put(i, cast(randomVK.nextInt()));

            //internal size has not changed until realCapacity
            if (newHeap.size() <= realCapacity) {

                Assert.assertEquals(contructorBufferSize, newHeap.buffer.length);
            }

            if (contructorBufferSize < newHeap.buffer.length) {
                //The container as just reallocated, its actual size must be not too far from the previous capacity:
                Assert.assertTrue("Container as reallocated at size = " + newHeap.size() + " with previous capacity = "
                        + realCapacity, (newHeap.size() - realCapacity) <= 2);
                break;
            }
        }
    }

    @Test
    public void testForEachProcedureWithException() {
        final Random randomVK = new Random(9521455645L);

        //Test that the container do not resize if less that the initial size

        //1) Choose a map to build
        /*! #if ($TemplateOptions.isKType("GENERIC", "INT", "LONG", "FLOAT", "DOUBLE")) !*/
        final int NB_ELEMENTS = 2000;
        /*!
            #elseif ($TemplateOptions.isKType("SHORT", "CHAR"))
             final int NB_ELEMENTS = 1000;
            #else
               final int NB_ELEMENTS = 126;
            #end !*/

        final KTypeIndexedHeapPriorityQueue<KType> newMap = new KTypeIndexedHeapPriorityQueue<KType>(0);

        //add a randomized number of key/values pairs
        //use the same value for keys and values to ease later analysis
        for (int i = 0; i < NB_ELEMENTS; i++) {

            final int KVpair = randomVK.nextInt((int) (0.7 * NB_ELEMENTS));

            newMap.put(KVpair, cast(KVpair));
        }

        //List the keys in the reverse-order of the internal buffer, since forEach() is iterating in reverse also:
        final ArrayList<Integer> keyList = new ArrayList<Integer>();
        final ArrayList<Integer> valueList = new ArrayList<Integer>();

        //Test forEach predicate and stop at each key in turn.
        final ArrayList<Integer> keyListTest = new ArrayList<Integer>();
        final ArrayList<Integer> valueListTest = new ArrayList<Integer>();

        for (int k = 1; k < newMap.size() + 1; k++) {

            //keys are int keys
            keyList.add(newMap.qp[k]);

            valueList.add(castType(newMap.buffer[k]));

            //check that it matches the get()
            Assert.assertEquals(castType(newMap.buffer[k]), castType(newMap.get(newMap.qp[k])));
        }

        final int size = keyList.size();

        for (int i = 0; i < size; i++) {
            final int currentPairIndexSizeToIterate = i + 1;

            keyListTest.clear();
            valueListTest.clear();

            //A) Run forEach(int KType)
            try {
                newMap.forEach(new IntKTypeProcedure<KType>() {

                    @Override
                    public void apply(final int key, final KType value) {
                        keyListTest.add(key);
                        valueListTest.add(castType(value));

                        //when the stopping key/value pair is encountered, add to list and stop iteration
                        if (key == keyList.get(currentPairIndexSizeToIterate - 1)) {
                            //interrupt iteration by an exception
                            throw new RuntimeException("Interrupted treatment by test");
                        }
                    }
                });
            } catch (final RuntimeException e) {
                if (!e.getMessage().equals("Interrupted treatment by test")) {
                    throw e;
                }
            } finally {
                //despite the exception, the procedure terminates cleanly

                //check that keyList/keyListTest and valueList/valueListTest are identical for the first
                //currentPairIndexToIterate + 1 elements
                Assert.assertEquals(currentPairIndexSizeToIterate, keyListTest.size());
                Assert.assertEquals(currentPairIndexSizeToIterate, valueListTest.size());

                for (int j = 0; j < currentPairIndexSizeToIterate; j++) {
                    Assert.assertEquals(keyList.get(j), keyListTest.get(j));
                    Assert.assertEquals(valueList.get(j), valueListTest.get(j));
                }
            } //end finally

            //B) Run keys().forEach(int)
            keyListTest.clear();
            valueListTest.clear();
            try {
                newMap.keys().forEach(new IntProcedure() {

                    @Override
                    public void apply(final int key) {
                        keyListTest.add(key);

                        //retreive value by get() on the map
                        valueListTest.add(castType(newMap.get(key)));

                        //when the stopping key/value pair is encountered, add to list and stop iteration
                        if (key == keyList.get(currentPairIndexSizeToIterate - 1)) {
                            //interrupt iteration by an exception
                            throw new RuntimeException("Interrupted treatment by test");
                        }
                    }
                });
            } catch (final RuntimeException e) {
                if (!e.getMessage().equals("Interrupted treatment by test")) {
                    throw e;
                }
            } finally {
                //despite the exception, the procedure terminates cleanly

                //check that keyList/keyListTest and valueList/valueListTest are identical for the first
                //currentPairIndexToIterate + 1 elements
                Assert.assertEquals(currentPairIndexSizeToIterate, keyListTest.size());
                Assert.assertEquals(currentPairIndexSizeToIterate, valueListTest.size());

                for (int j = 0; j < currentPairIndexSizeToIterate; j++) {
                    Assert.assertEquals(keyList.get(j), keyListTest.get(j));
                    Assert.assertEquals(valueList.get(j), valueListTest.get(j));
                }
            } //end finally

            //C) Run values().forEach(KType)
            keyListTest.clear();
            valueListTest.clear();

            try {
                newMap.values().forEach(new KTypeProcedure<KType>() {

                    @Override
                    public void apply(final KType value) {
                        valueListTest.add(castType(value));

                        //when the stopping key/value pair is encountered, add to list and stop iteration
                        if (castType(value) == valueList.get(currentPairIndexSizeToIterate - 1)) {
                            //interrupt iteration by an exception
                            throw new RuntimeException("Interrupted treatment by test");
                        }
                    }
                });
            } catch (final RuntimeException e) {
                if (!e.getMessage().equals("Interrupted treatment by test")) {
                    throw e;
                }
            } finally {
                //despite the exception, the procedure terminates cleanly

                //check that  valueList/valueListTest are identical for the first
                //currentPairIndexToIterate + 1 elements
                Assert.assertEquals(currentPairIndexSizeToIterate, valueListTest.size());

                for (int j = 0; j < currentPairIndexSizeToIterate; j++) {
                    Assert.assertEquals(valueList.get(j), valueListTest.get(j));
                }
            } //end finally

        } //end for each index
    }

    @Test
    public void testForEachProcedure() {
        final Random randomVK = new Random(9521455645L);

        //Test that the container do not resize if less that the initial size

        //1) Choose a map to build
        /*! #if ($TemplateOptions.isKType("GENERIC", "INT", "LONG", "FLOAT", "DOUBLE")) !*/
        final int NB_ELEMENTS = 2000;
        /*!
            #elseif ($TemplateOptions.isKType("SHORT", "CHAR"))
             final int NB_ELEMENTS = 1000;
            #else
               final int NB_ELEMENTS = 126;
            #end !*/

        final KTypeIndexedHeapPriorityQueue<KType> newMap = new KTypeIndexedHeapPriorityQueue<KType>(0);

        //add a randomized number of key/values pairs
        //use the same value for keys and values to ease later analysis
        for (int i = 0; i < NB_ELEMENTS; i++) {

            final int KVpair = randomVK.nextInt((int) (0.7 * NB_ELEMENTS));

            newMap.put(KVpair, cast(KVpair));
        }

        //List the keys in the reverse-order of the internal buffer, since forEach() is iterating in reverse also:
        final ArrayList<Integer> keyList = new ArrayList<Integer>();
        final ArrayList<Integer> valueList = new ArrayList<Integer>();

        for (int k = 1; k < newMap.size() + 1; k++) {

            //keys are int keys
            keyList.add(newMap.qp[k]);

            valueList.add(castType(newMap.buffer[k]));

            //check that it matches the get()
            Assert.assertEquals(castType(newMap.buffer[k]), castType(newMap.get(newMap.qp[k])));
        }

        //Test forEach predicate and stop at each key in turn.
        final ArrayList<Integer> keyListTest = new ArrayList<Integer>();
        final ArrayList<Integer> valueListTest = new ArrayList<Integer>();

        keyListTest.clear();
        valueListTest.clear();

        //A) Run forEach(int , VType)

        newMap.forEach(new IntKTypeProcedure<KType>() {

            @Override
            public void apply(final int key, final KType value) {
                keyListTest.add(key);
                valueListTest.add(castType(value));
            }
        });

        //check that keyList/keyListTest and valueList/valueListTest are identical.
        Assert.assertEquals(keyList, keyListTest);
        Assert.assertEquals(valueList, valueListTest);

        //B) Run keys().forEach(int)
        keyListTest.clear();
        valueListTest.clear();

        newMap.keys().forEach(new IntProcedure() {

            @Override
            public void apply(final int key) {
                keyListTest.add(key);
                //retreive value by get() on the map
                valueListTest.add(castType(newMap.get(key)));
            }
        });

        //check that keyList/keyListTest and valueList/valueListTest are identical .
        Assert.assertEquals(keyList, keyListTest);
        Assert.assertEquals(valueList, valueListTest);

        //C) Run values().forEach(KType)
        keyListTest.clear();
        valueListTest.clear();

        newMap.values().forEach(new KTypeProcedure<KType>() {

            @Override
            public void apply(final KType value) {
                valueListTest.add(castType(value));
            }
        });

        //check that  valueList/valueListTest are identical .
        Assert.assertEquals(valueList, valueListTest);
    }

    @Test
    public void testForEachPredicate() {
        final Random randomVK = new Random(9521455645L);

        //Test that the container do not resize if less that the initial size

        //1) Choose a map to build
        /*! #if ($TemplateOptions.isKType("GENERIC", "INT", "LONG", "FLOAT", "DOUBLE")) !*/
        final int NB_ELEMENTS = 2000;
        /*!
            #elseif ($TemplateOptions.isKType("SHORT", "CHAR"))
             final int NB_ELEMENTS = 1000;
            #else
               final int NB_ELEMENTS = 126;
            #end !*/

        final KTypeIndexedHeapPriorityQueue<KType> newMap = new KTypeIndexedHeapPriorityQueue<KType>(0);

        //add a randomized number of key/values pairs
        //use the same value for keys and values to ease later analysis
        for (int i = 0; i < NB_ELEMENTS; i++) {

            final int KVpair = randomVK.nextInt((int) (0.7 * NB_ELEMENTS));

            newMap.put(KVpair, cast(KVpair));
        }

        //List the keys in the reverse-order of the internal buffer, since forEach() is iterating in reverse also:
        final ArrayList<Integer> keyList = new ArrayList<Integer>();
        final ArrayList<Integer> valueList = new ArrayList<Integer>();

        //Test forEach predicate and stop at each key in turn.
        final ArrayList<Integer> keyListTest = new ArrayList<Integer>();
        final ArrayList<Integer> valueListTest = new ArrayList<Integer>();

        for (int k = 1; k < newMap.size() + 1; k++) {

            //keys are int keys
            keyList.add(newMap.qp[k]);

            valueList.add(castType(newMap.buffer[k]));

            //check that it matches the get()
            Assert.assertEquals(castType(newMap.buffer[k]), castType(newMap.get(newMap.qp[k])));
        }

        final int size = keyList.size();

        for (int i = 0; i < size; i++) {
            final int currentPairIndexSizeToIterate = i + 1;

            keyListTest.clear();
            valueListTest.clear();

            //A) Run forEach(int, KType)

            newMap.forEach(new IntKTypePredicate<KType>() {

                @Override
                public boolean apply(final int key, final KType value) {
                    keyListTest.add(key);
                    valueListTest.add(castType(value));

                    //when the stopping key/value pair is encountered, add to list and stop iteration
                    if (key == keyList.get(currentPairIndexSizeToIterate - 1)) {
                        //interrupt iteration by an exception
                        return false;
                    }

                    return true;
                }
            });

            //despite the interruption, the procedure terminates cleanly

            //check that keyList/keyListTest and valueList/valueListTest are identical for the first
            //currentPairIndexToIterate + 1 elements
            Assert.assertEquals(currentPairIndexSizeToIterate, keyListTest.size());
            Assert.assertEquals(currentPairIndexSizeToIterate, valueListTest.size());

            for (int j = 0; j < currentPairIndexSizeToIterate; j++) {
                Assert.assertEquals(keyList.get(j), keyListTest.get(j));
                Assert.assertEquals(valueList.get(j), valueListTest.get(j));
            }

            //B) Run keys().forEach(int)
            keyListTest.clear();
            valueListTest.clear();

            newMap.keys().forEach(new IntPredicate() {

                @Override
                public boolean apply(final int key) {
                    keyListTest.add(key);

                    //retreive value by get() on the map
                    valueListTest.add(castType(newMap.get(key)));

                    //when the stopping key/value pair is encountered, add to list and stop iteration
                    if (key == keyList.get(currentPairIndexSizeToIterate - 1)) {
                        //interrupt iteration
                        return false;
                    }

                    return true;
                }
            });

            //despite the interruption, the procedure terminates cleanly

            //check that keyList/keyListTest and valueList/valueListTest are identical for the first
            //currentPairIndexToIterate + 1 elements
            Assert.assertEquals(currentPairIndexSizeToIterate, keyListTest.size());
            Assert.assertEquals(currentPairIndexSizeToIterate, valueListTest.size());

            for (int j = 0; j < currentPairIndexSizeToIterate; j++) {
                Assert.assertEquals(keyList.get(j), keyListTest.get(j));
                Assert.assertEquals(valueList.get(j), valueListTest.get(j));
            }

            //C) Run values().forEach(KType)
            keyListTest.clear();
            valueListTest.clear();

            newMap.values().forEach(new KTypePredicate<KType>() {

                @Override
                public boolean apply(final KType value) {
                    valueListTest.add(castType(value));

                    //when the stopping key/value pair is encountered, add to list and stop iteration
                    if (castType(value) == valueList.get(currentPairIndexSizeToIterate - 1)) {
                        //interrupt iteration
                        return false;
                    }

                    return true;
                }
            });

            //despite the interruption, the procedure terminates cleanly

            //check that  valueList/valueListTest are identical for the first
            //currentPairIndexToIterate + 1 elements
            Assert.assertEquals(currentPairIndexSizeToIterate, valueListTest.size());

            for (int j = 0; j < currentPairIndexSizeToIterate; j++) {
                Assert.assertEquals(valueList.get(j), valueListTest.get(j));
            }
        } //end for each index
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
             int PREALLOCATED_SIZE = randomVK.nextInt(126);
            #else
              int PREALLOCATED_SIZE = randomVK.nextInt(10000);
            #end !*/

        //2) Preallocate to PREALLOCATED_SIZE :
        final KTypeIndexedHeapPriorityQueue<KType> refContainer = new KTypeIndexedHeapPriorityQueue<KType>(
                PREALLOCATED_SIZE);

        final int refCapacity = refContainer.capacity();

        //3) Fill with random values, random number of elements below preallocation
        final int nbElements = RandomizedTest.randomInt(PREALLOCATED_SIZE);

        for (int i = 0; i < nbElements; i++) {

            refContainer.put(randomVK.nextInt(PREALLOCATED_SIZE), cast(randomVK.nextInt()));
        }

        final int nbRefElements = refContainer.size();

        Assert.assertEquals(refCapacity, refContainer.capacity());

        //4) Duplicate by copy-construction and/or clone
        KTypeIndexedHeapPriorityQueue<KType> clonedContainer = refContainer.clone();
        KTypeIndexedHeapPriorityQueue<KType> copiedContainer = new KTypeIndexedHeapPriorityQueue<KType>(refContainer);

        final int copiedCapacity = copiedContainer.capacity();

        Assert.assertEquals(nbRefElements, clonedContainer.size());
        Assert.assertEquals(nbRefElements, copiedContainer.size());
        Assert.assertEquals(refCapacity, clonedContainer.capacity()); //clone is supposed to be cloned, so exact match !
        Assert.assertTrue(refCapacity >= copiedCapacity);
        Assert.assertTrue(clonedContainer.equals(refContainer));
        Assert.assertTrue(copiedContainer.equals(refContainer));

        //Maybe we were lucky, iterate duplication over itself several times
        for (int j = 0; j < 10; j++) {

            clonedContainer = clonedContainer.clone();
            copiedContainer = new KTypeIndexedHeapPriorityQueue<KType>(copiedContainer);

            //when copied over itself, of course every characteristic must be constant, else something is wrong.
            Assert.assertEquals(nbRefElements, clonedContainer.size());
            Assert.assertEquals(nbRefElements, copiedContainer.size());
            Assert.assertEquals(refCapacity, clonedContainer.capacity());
            Assert.assertEquals(copiedCapacity, copiedContainer.capacity());
            Assert.assertTrue(clonedContainer.equals(refContainer));
            Assert.assertTrue(copiedContainer.equals(refContainer));
        }
    }

    /**
     * Check if the indexed, prio queue content is identical to int...elemenents
     * made of (indexes, values) alternated
     */
    private void assertPrioQueueEquals(final KTypeIndexedHeapPriorityQueue<KType> obj, final int... elements) {
        Assert.assertEquals(obj.toString(), elements.length / 2, obj.size());

        final KType[] valuesArrayExport = (KType[]) obj.values().toArray();
        final int[] keysArrayExport = obj.keys().toArray();

        Assert.assertEquals(obj.toString(), obj.size(), valuesArrayExport.length);
        Assert.assertEquals(obj.toString(), obj.size(), keysArrayExport.length);

        //A) test with containsKey() / get()
        int i = 0;
        while (i < elements.length) {
            Assert.assertTrue(obj.toString(), obj.containsKey(elements[i]));
            Assert.assertEquals(obj.toString(), elements[i + 1], castType(obj.get(elements[i])));
            i++;
            i++;
        }

        //B) Test with toArray[] on obj
        for (int ii = 0; ii < keysArrayExport.length; ii++) {

            final int currentValue = castType(obj.get(keysArrayExport[ii]));

            boolean valueExist = false;

            //show that the matching value exists in valuesArrayExport
            for (int jj = 0; jj < valuesArrayExport.length; jj++) {

                if (currentValue == castType(valuesArrayExport[jj])) {
                    valueExist = true;
                    break;
                }
            }

            Assert.assertTrue(valueExist);
        } //end for each key
    }

    public void insertElements(final KTypeIndexedHeapPriorityQueue<KType> pq, final int... elements) {
        int i = 0;
        while (i < elements.length) {

            pq.put(elements[i], cast(elements[i + 1]));
            i++;
            i++;
        }

        Assert.assertEquals(elements.length / 2, pq.size());
    }

    private static <KType> boolean checkConsistency(final KTypeIndexedHeapPriorityQueue<KType> prio) {
        if (prio.elementsCount > 0) {
            //A) For each valid index, (in pq), there is match in position in qp
            for (int index = 0; index < prio.pq.length; index++) {
                if (prio.pq[index] > 0) {
                    if (index != prio.qp[prio.pq[index]]) {
                        assert false : String.format(
                                "Inconsistent Index: index=%d, size=%d , pq[index] = %d, ==> qp[pq[index]] = %d", index, prio.size(),
                                prio.pq[index], prio.qp[prio.pq[index]]);
                    }
                }
            }

            //B) Reverse check : for each element of position pos in buffer, there is a match in pq
            for (int pos = 1; pos <= prio.elementsCount; pos++) {

                if (pos != prio.pq[prio.qp[pos]]) {
                    assert false : String.format("Inconsistent position: pos=%d, size=%d , qp[pos] = %d, ==> pq[qp[pos]] = %d",
                            pos, prio.size(), prio.qp[pos], prio.pq[prio.qp[pos]]);

                }
            }
        }

        return true;
    }

    /**
     * method to test heap invariant in assert expressions
     */
    // is buffer[1..N] a min heap?
    private static <KType> boolean isMinHeap(final KTypeIndexedHeapPriorityQueue<KType> prio) {
        if (prio.comparator == null) {
            return KTypeIndexedHeapPriorityQueueTest.isMinHeapComparable(prio, 1);
        }

        return KTypeIndexedHeapPriorityQueueTest.isMinHeapComparator(prio, 1);
    }

    // is subtree of pq[1..N] rooted at k a min heap?
    private static <KType> boolean isMinHeapComparable(final KTypeIndexedHeapPriorityQueue<KType> prio, final int k) {
        final int N = prio.elementsCount;
        final KType[] buffer = prio.buffer;

        if (k > N) {
            return true;
        }
        final int left = 2 * k, right = 2 * k + 1;

        if (left <= N && Intrinsics.isCompSupKTypeUnchecked(buffer[k], buffer[left])) {
            return false;
        }
        if (right <= N && Intrinsics.isCompSupKTypeUnchecked(buffer[k], buffer[right])) {
            return false;
        }
        //recursively test
        return KTypeIndexedHeapPriorityQueueTest.isMinHeapComparable(prio, left)
                && KTypeIndexedHeapPriorityQueueTest.isMinHeapComparable(prio, right);
    }

    // is subtree of pq[1..N] rooted at k a min heap?
    private static <KType> boolean isMinHeapComparator(final KTypeIndexedHeapPriorityQueue<KType> prio, final int k) {
        final int N = prio.elementsCount;
        final KType[] buffer = prio.buffer;

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        final Comparator<? super KType> comp = prio.comparator;
        /*! #else
        KTypeComparator<? super KType> comp = prio.comparator;
        #end !*/

        if (k > N) {
            return true;
        }
        final int left = 2 * k, right = 2 * k + 1;

        if (left <= N && comp.compare(buffer[k], buffer[left]) > 0) {
            return false;
        }
        if (right <= N && comp.compare(buffer[k], buffer[right]) > 0) {
            return false;
        }
        //recursively test
        return KTypeIndexedHeapPriorityQueueTest.isMinHeapComparator(prio, left)
                && KTypeIndexedHeapPriorityQueueTest.isMinHeapComparator(prio, right);
    }
}
