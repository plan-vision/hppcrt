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
/**
 * Unit tests for {@link KTypeLinkedList}.
 */
//${TemplateOptions.doNotGenerateKType("BOOLEAN")}
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeLinkedListTest<KType> extends AbstractKTypeTest<KType>
{
    /**
     * Per-test fresh initialized instance.
     */
    public KTypeLinkedList<KType> list;

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
        //create a vary small list to force reallocs.
        this.list = KTypeLinkedList.newInstance(2);

        this.sequence = KTypeArrayList.newInstance();

        for (int i = 0; i < 10000; i++) {
            this.sequence.add(cast(i));
        }
    }

    @After
    public void checkConsistency()
    {
        if (this.list != null)
        {
            for (int i = this.list.elementsCount; i < this.list.buffer.length; i++) {
                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                Assert.assertTrue(Intrinsics.<KType> defaultKTypeValue() == this.list.buffer[i]);
                /*! #end !*/
            }
        }
    }

    /* */
    @Test
    public void testInitiallyEmpty()
    {
        Assert.assertEquals(0, this.list.size());
    }

    /* */
    @Test
    public void testAdd()
    {
        this.list.add(this.key1, this.key2);
        TestUtils.assertListEquals(this.list.toArray(), 1, 2);
    }

    /* */
    @Test
    public void testAddTwoArgs()
    {
        this.list.add(this.key1, this.key2);
        this.list.add(this.key3, this.key4);
        TestUtils.assertListEquals(this.list.toArray(), 1, 2, 3, 4);
    }

    /* */
    @Test
    public void testAddArray()
    {
        this.list.add(asArray(0, 1, 2, 3), 1, 2);
        TestUtils.assertListEquals(this.list.toArray(), 1, 2);
    }

    /* */
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testAddVarArg()
    {
        this.list.add(asArray(0, 1, 2, 3));
        this.list.add(this.key4, this.key5, this.key6, this.key7);
        TestUtils.assertListEquals(this.list.toArray(), 0, 1, 2, 3, 4, 5, 6, 7);
    }

    /* */
    @Test
    public void testAddAll()
    {
        final KTypeLinkedList<KType> list2 = KTypeLinkedList.newInstance();
        list2.add(asArray(0, 1, 2));

        this.list.addAll(list2);
        this.list.addAll(list2);

        TestUtils.assertListEquals(this.list.toArray(), 0, 1, 2, 0, 1, 2);
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testAddAll_subclass()
    {
        class A
        {
        }

        class B extends A
        {
        }

        final KTypeLinkedList<B> list2 = new KTypeLinkedList<B>();
        list2.add(new B());

        final KTypeLinkedList<A> list3 = new KTypeLinkedList<A>();
        list3.add(new B());
        list3.add(new A());
        list3.addAll(list2);
        Assert.assertEquals(3, list3.size());
    }

    /*! #end !*/

    /* */
    @Test
    public void testInsert()
    {
        this.list.insert(0, this.k1);
        this.list.insert(0, this.k2);
        this.list.insert(2, this.k3);
        this.list.insert(1, this.k4);

        TestUtils.assertListEquals(this.list.toArray(), 2, 4, 1, 3);
    }

    /* */
    @Test
    public void testSet()
    {
        this.list.add(asArray(0, 1, 2));

        TestUtils.assertEquals2(0, this.list.set(0, this.k3));
        TestUtils.assertEquals2(1, this.list.set(1, this.k4));
        TestUtils.assertEquals2(2, this.list.set(2, this.k5));

        TestUtils.assertListEquals(this.list.toArray(), 3, 4, 5);
    }

    /* */
    @Test
    public void testRemove()
    {
        this.list.add(asArray(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));

        this.list.remove(0);
        this.list.remove(2);
        this.list.remove(1);
        this.list.remove(4);

        TestUtils.assertListEquals(this.list.toArray(), 1, 4, 5, 6, 8, 9);
    }

    /* */
    @Test
    public void testRemoveRange()
    {
        this.list.add(asArray(0, 1, 2, 3, 4));

        this.list.removeRange(0, 2);
        TestUtils.assertListEquals(this.list.toArray(), 2, 3, 4);

        this.list.removeRange(2, 3);
        TestUtils.assertListEquals(this.list.toArray(), 2, 3);

        this.list.removeRange(1, 1);
        TestUtils.assertListEquals(this.list.toArray(), 2, 3);

        this.list.removeRange(0, 1);
        TestUtils.assertListEquals(this.list.toArray(), 3);
    }

    /* */
    @Test
    public void testGotoIndex()
    {
        //fill with distinct values
        final int COUNT = (int) 1e4;

        for (int i = 0; i < COUNT; i++)
        {
            this.list.add(cast(i));
        }

        //check that we reach the good element, by index
        for (int i = 0; i < COUNT; i++)
        {
            Assert.assertEquals(castType(cast(i)), castType(this.list.buffer[this.list.gotoIndex(i)]));
        }
    }

    /* */
    @Test
    public void testRemoveFirstLast()
    {
        this.list.add(asArray(0, 1, 2, 1, 0));

        Assert.assertEquals(-1, this.list.removeFirst(this.k5));
        Assert.assertEquals(-1, this.list.removeLast(this.k5));
        TestUtils.assertListEquals(this.list.toArray(), 0, 1, 2, 1, 0);

        Assert.assertEquals(1, this.list.removeFirst(this.k1));
        TestUtils.assertListEquals(this.list.toArray(), 0, 2, 1, 0);
        Assert.assertEquals(3, this.list.removeLast(this.k0));
        TestUtils.assertListEquals(this.list.toArray(), 0, 2, 1);
        Assert.assertEquals(0, this.list.removeLast(this.k0));
        TestUtils.assertListEquals(this.list.toArray(), 2, 1);
        Assert.assertEquals(-1, this.list.removeLast(this.k0));

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        this.list.clear();
        this.list.add(newArray(this.k0, null, this.k2, null, this.k0));
        Assert.assertEquals(1, this.list.removeFirst(null));
        Assert.assertEquals(2, this.list.removeLast(null));
        TestUtils.assertListEquals(this.list.toArray(), 0, 2, 0);
        /*! #end !*/
    }

    /* */
    @Test
    public void testRemoveAll()
    {
        this.list.add(asArray(0, 1, 0, 1, 0));

        Assert.assertEquals(0, this.list.removeAll(this.k2));
        Assert.assertEquals(3, this.list.removeAll(this.k0));
        TestUtils.assertListEquals(this.list.toArray(), 1, 1);

        Assert.assertEquals(2, this.list.removeAll(this.k1));
        Assert.assertTrue(this.list.isEmpty());

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        this.list.clear();
        this.list.add(newArray(this.k0, null, this.k2, null, this.k0));
        Assert.assertEquals(2, this.list.removeAll((KType) null));
        Assert.assertEquals(0, this.list.removeAll((KType) null));
        TestUtils.assertListEquals(this.list.toArray(), 0, 2, 0);
        /*! #end !*/
    }

    /* */
    @Test
    public void testRemoveAllEverything()
    {
        this.list.add(asArray(1, 1, 1, 1, 1));

        Assert.assertEquals(5, this.list.removeAll(this.k1));
        Assert.assertEquals(0, this.list.size());

        //remove all a void
        Assert.assertEquals(0, this.list.removeAll(this.k1));
        Assert.assertEquals(0, this.list.size());
    }

    /* */
    @Test
    public void testRemoveAllFromLookupContainer()
    {
        this.list.add(asArray(0, 1, 2, 1, 0));

        final KTypeOpenHashSet<KType> list2 = KTypeOpenHashSet.newInstance();
        list2.add(asArray(0, 2));

        Assert.assertEquals(3, this.list.removeAll(list2));
        Assert.assertEquals(0, this.list.removeAll(list2));

        TestUtils.assertListEquals(this.list.toArray(), 1, 1);
    }

    /* */
    @Test
    public void testRemoveAllFromLookupContainerEverything()
    {
        this.list.add(asArray(0, 1, 2, 1, 0));

        final KTypeOpenHashSet<KType> list2 = KTypeOpenHashSet.newInstance();
        list2.add(asArray(0, 1, 2));

        Assert.assertEquals(5, this.list.removeAll(list2));
        Assert.assertEquals(0, this.list.size());
        Assert.assertEquals(0, this.list.removeAll(list2));
    }

    /* */
    @Test
    public void testRemoveAllWithPredicate()
    {
        this.list.add(newArray(this.k0, this.k1, this.k2, this.k1, this.k4));

        Assert.assertEquals(3, this.list.removeAll(new KTypePredicate<KType>()
                {
            @Override
            public boolean apply(final KType v)
            {
                return v == KTypeLinkedListTest.this.key1 || v == KTypeLinkedListTest.this.key2;
            };
                }));

        TestUtils.assertListEquals(this.list.toArray(), 0, 4);
    }

    /* */
    @Test
    public void testRemoveAllWithPredicateEverything()
    {
        this.list.add(newArray(this.k0, this.k1, this.k2, this.k1, this.k4));

        Assert.assertEquals(5, this.list.removeAll(new KTypePredicate<KType>()
                {
            @Override
            public boolean apply(final KType v)
            {
                return true;
            };
                }));

        Assert.assertEquals(0, this.list.size());

        //try again
        Assert.assertEquals(0, this.list.removeAll(new KTypePredicate<KType>()
                {
            @Override
            public boolean apply(final KType v)
            {
                return true;
            };
                }));

        Assert.assertEquals(0, this.list.size());
    }

    /* */
    @Test
    public void testRetainAllWithPredicate()
    {
        this.list.add(newArray(this.k0, this.k1, this.k2, this.k1, this.k0));

        Assert.assertEquals(2, this.list.retainAll(new KTypePredicate<KType>()
                {
            @Override
            public boolean apply(final KType v)
            {
                return v == KTypeLinkedListTest.this.key1 || v == KTypeLinkedListTest.this.key2;
            };
                }));

        TestUtils.assertListEquals(this.list.toArray(), 1, 2, 1);
    }

    /* */
    @Test
    public void testRemoveAllWithPredicateInterrupted()
    {
        this.list.add(newArray(this.k0, this.k1, this.k2, this.k1, this.k4));

        final RuntimeException t = new RuntimeException();
        try
        {
            //the assert below should never be triggered because of the exception
            //so give it an invalid value in case the thing terminates  = initial size
            Assert.assertEquals(5, this.list.removeAll(new KTypePredicate<KType>()
                    {
                @Override
                public boolean apply(final KType v)
                {
                    if (v == KTypeLinkedListTest.this.key2) {
                        throw t;
                    }
                    return v == KTypeLinkedListTest.this.key1;
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
        TestUtils.assertListEquals(this.list.toArray(), 0, this.key2, this.key1, 4);
        Assert.assertEquals(4, this.list.size());
    }

    /* */
    @Test
    public void testIndexOf()
    {
        this.list.add(asArray(0, 1, 2, 1, 0));

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        this.list.add((KType) null);
        Assert.assertEquals(5, this.list.indexOf(null));
        /*! #end !*/

        Assert.assertEquals(0, this.list.indexOf(this.k0));
        Assert.assertEquals(-1, this.list.indexOf(this.k3));
        Assert.assertEquals(2, this.list.indexOf(this.k2));
    }

    /* */
    @Test
    public void testContains()
    {
        this.list.add(asArray(0, 1, 2, 7, 4, 3));

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        this.list.add((KType) null);
        Assert.assertTrue(this.list.contains(null));
        /*! #end !*/

        Assert.assertTrue(this.list.contains(this.k0));
        Assert.assertTrue(this.list.contains(this.k3));
        Assert.assertTrue(this.list.contains(this.k2));

        Assert.assertFalse(this.list.contains(this.k5));
        Assert.assertFalse(this.list.contains(this.k6));
        Assert.assertFalse(this.list.contains(this.k8));
    }

    /* */
    @Test
    public void testLastIndexOf()
    {
        this.list.add(asArray(0, 1, 2, 1, 0));

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        this.list.add((KType) null);
        Assert.assertEquals(5, this.list.lastIndexOf(null));
        /*! #end !*/

        TestUtils.assertEquals2(4, this.list.lastIndexOf(this.k0));
        TestUtils.assertEquals2(-1, this.list.lastIndexOf(this.k3));
        TestUtils.assertEquals2(2, this.list.lastIndexOf(this.k2));
    }

    /* */
    @Test
    public void testEnsureCapacity()
    {
        this.list.ensureCapacity(100);
        Assert.assertTrue(this.list.buffer.length >= 100);

        this.list.ensureCapacity(1000);
        this.list.ensureCapacity(1000);
        Assert.assertTrue(this.list.buffer.length >= 1000);
    }

    /* */
    @Test
    public void testForEachWithProcedure()
    {
        this.list.add(asArray(1, 2, 3));
        final IntHolder holder = new IntHolder();
        this.list.forEach(new KTypeProcedure<KType>() {
            int index = 0;

            @Override
            public void apply(final KType v)
            {
                TestUtils.assertEquals2(v, KTypeLinkedListTest.this.list.get(this.index++));
                holder.value = this.index;
            }
        });
        Assert.assertEquals(holder.value, this.list.size());
    }

    /* */
    @Test
    public void testForEachWithPredicate()
    {
        this.list.add(asArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));

        final int lastValue = this.list.forEach(new KTypePredicate<KType>() {
            int value = 0;
            int index = 0;

            @Override
            public boolean apply(final KType v)
            {
                TestUtils.assertEquals2(v, KTypeLinkedListTest.this.list.get(this.index));
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
        this.list.add(asArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));

        final int lastValue = this.list.forEach(new KTypePredicate<KType>() {
            int value = 0;
            int index = 0;

            @Override
            public boolean apply(final KType v)
            {
                TestUtils.assertEquals2(v, KTypeLinkedListTest.this.list.get(this.index));
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
        this.list.add(asArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));

        final int lastValue = this.list.descendingForEach(new KTypePredicate<KType>() {
            int value = 0;
            int index = KTypeLinkedListTest.this.list.size() - 1;

            @Override
            public boolean apply(final KType v)
            {
                TestUtils.assertEquals2(v, KTypeLinkedListTest.this.list.get(this.index));
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
        this.list.add(asArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));

        final int lastValue = this.list.descendingForEach(new KTypePredicate<KType>() {
            int value = 0;
            int index = KTypeLinkedListTest.this.list.size() - 1;

            @Override
            public boolean apply(final KType v)
            {
                TestUtils.assertEquals2(v, KTypeLinkedListTest.this.list.get(this.index));
                this.value = castType(v);

                this.index--;

                return true;
            }
        }).value;

        Assert.assertEquals(lastValue, 1);
    }

    /* */
    @Test
    public void testForEachReturnValueFromAnonymousClass()
    {
        this.list.add(asArray(1, 2, 3));
        final int result = this.list.forEach(new KTypeProcedure<KType>() {
            int index = 0;

            @Override
            public void apply(final KType v)
            {
                TestUtils.assertEquals2(v, KTypeLinkedListTest.this.list.get(this.index++));
            }
        }).index;
        Assert.assertEquals(result, this.list.size());
    }

    /* */
    @Test
    public void testClear()
    {
        this.list.add(asArray(1, 2, 3));
        this.list.clear();
        checkConsistency();
    }

    /* */
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testFrom()
    {
        final KTypeLinkedList<KType> variable = KTypeLinkedList.from(this.k1, this.k2, this.k3);
        Assert.assertEquals(3, variable.size());
        TestUtils.assertListEquals(variable.toArray(), 1, 2, 3);
    }

    /* */
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testHashCodeEquals()
    {
        final ObjectArrayList<Integer> l0 = ObjectArrayList.from();
        Assert.assertEquals(1, l0.hashCode());
        Assert.assertEquals(l0, ObjectArrayList.from());

        final KTypeLinkedList<KType> l1 = KTypeLinkedList.from(this.k1, this.k2, this.k3);
        final KTypeLinkedList<KType> l2 = KTypeLinkedList.from(this.k1, this.k2);
        l2.add(this.k3);

        Assert.assertEquals(l1.hashCode(), l2.hashCode());
        Assert.assertEquals(l1, l2);
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    @SuppressWarnings("unchecked")
    public void testHashCodeWithNulls()
    {
        final KTypeLinkedList<KType> l1 = KTypeLinkedList.from(this.k1, null, this.k3);
        final KTypeLinkedList<KType> l2 = KTypeLinkedList.from(this.k1, null, this.k3);

        Assert.assertEquals(l1.hashCode(), l2.hashCode());
        Assert.assertEquals(l1, l2);
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testToArrayWithClass()
    {
        final KTypeLinkedList<Integer> l1 = KTypeLinkedList.from(1, 2, 3);
        final Integer[] result = l1.toArray(Integer.class);
        Assert.assertArrayEquals(new Integer[] { 1, 2, 3 }, result); // dummy
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    @Test
    public void testToArray()
    {
        final KTypeLinkedList<KType> l1 = KTypeLinkedList.from(this.k1, this.k2, this.k3, this.k4, this.k5, this.k7);
        final Object[] result = l1.toArray();
        Assert.assertArrayEquals(new Object[] { this.k1, this.k2, this.k3, this.k4, this.k5, this.k7 }, result);
    }

    /*! #end !*/

    /* */
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testClone()
    {
        this.list.add(this.k1, this.k2, this.k3);

        final KTypeLinkedList<KType> cloned = this.list.clone();
        cloned.removeAll(this.key1);

        TestUtils.assertSortedListEquals(this.list.toArray(), this.key1, this.key2, this.key3);
        TestUtils.assertSortedListEquals(cloned.toArray(), this.key2, this.key3);
    }

    /* */
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testToString()
    {
        Assert.assertEquals("["
                + this.key1 + ", "
                + this.key2 + ", "
                + this.key3 + ", "
                + this.key1 + ", "
                + this.key7 + ", "
                + this.key4 + ", "
                + this.key3 + "]", KTypeLinkedList.from(this.k1, this.k2, this.k3, this.k1, this.k7, this.k4, this.k3).toString());
    }

    @Test
    public void testPooledIteratorForEach()
    {
        // Unbroken for-each loop
        final int TEST_SIZE = 5000;
        final long TEST_ROUNDS = 100;

        final KTypeLinkedList<KType> testContainer = createArrayWithOrderedData(TEST_SIZE);

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

        final int TEST_SIZE = 5000;
        final long TEST_ROUNDS = 100;

        final KTypeLinkedList<KType> testContainer = createArrayWithOrderedData(TEST_SIZE);

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

        final KTypeLinkedList<KType> testContainer = createArrayWithOrderedData(TEST_SIZE);

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

            final KTypeLinkedList<KType>.ValueIterator loopIterator = testContainer.iterator();

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

        final int TEST_SIZE = 5000;
        final long TEST_ROUNDS = 100;

        final KTypeLinkedList<KType> testContainer = createArrayWithOrderedData(TEST_SIZE);
        final int startingPoolSize = testContainer.valueIteratorPool.size();

        int count = 0;
        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            //Classical iterator loop, with manually allocated Iterator
            final long initialPoolSize = testContainer.valueIteratorPool.size();

            final KTypeLinkedList<KType>.ValueIterator loopIterator = testContainer.iterator();

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
        final int TEST_SIZE = 5000;
        final long TEST_ROUNDS = 100;

        final KTypeLinkedList<KType> testContainer = createArrayWithOrderedData(TEST_SIZE);

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
        KTypeLinkedList<KType>.ValueIterator loopIterator = null;

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
        final int TEST_SIZE = 88171;
        final long TEST_ROUNDS = 15;

        final KTypeLinkedList<KType> testContainer = createArrayWithOrderedData(TEST_SIZE);

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
        KTypeLinkedList<KType>.ValueIterator loopIterator = null;

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

    /**
     * The beast is slow, don't do too much
     */
    @Repeat(iterations = 20)
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

        final int TEST_SIZE = (int) 1e3;

        //get a new seed for the current iteration
        final long currentSeed = RandomizedTest.randomLong();

        final int upperRange = RandomizedTest.randomInt(TEST_SIZE);
        final int lowerRange = RandomizedTest.randomInt(upperRange);

        //A) Sort an array of random values of primitive types

        //A-1) full sort
        KTypeLinkedList<KType> primitiveList = createLinkedListWithRandomData(TEST_SIZE, currentSeed);
        KTypeLinkedList<KType> primitiveListOriginal = createLinkedListWithRandomData(TEST_SIZE, currentSeed);
        primitiveList.sort();
        assertOrder(primitiveListOriginal, primitiveList, 0, primitiveListOriginal.size());
        //A-2) Partial sort
        primitiveList = createLinkedListWithRandomData(TEST_SIZE, currentSeed);
        primitiveListOriginal = createLinkedListWithRandomData(TEST_SIZE, currentSeed);
        primitiveList.sort(lowerRange, upperRange);
        assertOrder(primitiveListOriginal, primitiveList, lowerRange, upperRange);

        //B) Sort with Comparator
        //B-1) Full sort
        KTypeLinkedList<KType> comparatorList = createLinkedListWithRandomData(TEST_SIZE, currentSeed);
        KTypeLinkedList<KType> comparatorListOriginal = createLinkedListWithRandomData(TEST_SIZE, currentSeed);
        comparatorList.sort(comp);
        assertOrder(comparatorListOriginal, comparatorList, 0, comparatorListOriginal.size());
        //B-2) Partial sort
        comparatorList = createLinkedListWithRandomData(TEST_SIZE, currentSeed);
        comparatorListOriginal = createLinkedListWithRandomData(TEST_SIZE, currentSeed);
        comparatorList.sort(lowerRange, upperRange, comp);
        assertOrder(comparatorListOriginal, comparatorList, lowerRange, upperRange);
    }

    private KTypeLinkedList<KType> createArrayWithOrderedData(final int size)
    {
        final KTypeLinkedList<KType> newArray = KTypeLinkedList.newInstance(KTypeLinkedList.DEFAULT_CAPACITY);

        for (int i = 0; i < size; i++)
        {
            newArray.add(cast(i));
        }

        return newArray;
    }

    private KTypeLinkedList<KType> createLinkedListWithRandomData(final int size, final long randomSeed)
    {
        final Random prng = new Random(randomSeed);

        final KTypeLinkedList<KType> newArray = KTypeLinkedList.newInstance(KTypeLinkedList.DEFAULT_CAPACITY);

        for (int i = 0; i < size; i++)
        {
            newArray.add(cast(prng.nextInt()));
        }

        return newArray;
    }

    ////////////////////////////////// Dequeue-like tests //////////////////////////////////////////////////////////////////

    /* */
    @Test
    public void testAddFirst()
    {
        this.list.addFirst(this.k1);
        this.list.addFirst(this.k2);
        this.list.addFirst(this.k3);
        this.list.addFirst(this.k7);
        this.list.addFirst(this.k1);
        this.list.addFirst(this.k4);
        this.list.addFirst(this.k5);
        TestUtils.assertListEquals(this.list.toArray(), 5, 4, 1, 7, 3, 2, 1);
        Assert.assertEquals(7, this.list.size());
    }

    /* */
    @Test
    public void testAddLast()
    {
        this.list.addLast(this.k1);
        this.list.addLast(this.k2);
        this.list.addLast(this.k3);
        this.list.addLast(this.k7);
        this.list.addLast(this.k1);
        this.list.addLast(this.k4);
        this.list.addLast(this.k5);
        TestUtils.assertListEquals(this.list.toArray(), 1, 2, 3, 7, 1, 4, 5);
        Assert.assertEquals(7, this.list.size());
    }

    /* */
    @Test
    public void testAddWithGrowth()
    {
        for (int i = 0; i < this.sequence.size(); i++)
        {
            this.list.addFirst(this.sequence.buffer[i]);
        }

        TestUtils.assertListEquals(TestUtils.reverse(this.sequence.toArray()), this.list.toArray());
    }

    /* */
    @Test
    public void testAddLastWithGrowth()
    {
        for (int i = 0; i < this.sequence.size(); i++) {
            this.list.addLast(this.sequence.buffer[i]);
        }

        TestUtils.assertListEquals(this.sequence.toArray(), this.list.toArray());
    }

    /* */
    @Test
    public void testAddAllFirst()
    {
        final KTypeArrayList<KType> list2 = KTypeArrayList.newInstance();
        list2.add(asArray(0, 1, 2));

        this.list.addFirst(list2);
        TestUtils.assertListEquals(this.list.toArray(), 2, 1, 0);
        this.list.addFirst(list2);
        TestUtils.assertListEquals(this.list.toArray(), 2, 1, 0, 2, 1, 0);

        this.list.clear();
        final KTypeArrayDeque<KType> deque2 = new KTypeArrayDeque<KType>();
        deque2.addLast(asArray(0, 1, 2));
        this.list.addFirst(deque2);
        TestUtils.assertListEquals(this.list.toArray(), 2, 1, 0);
    }

    /* */
    @Test
    public void testAddAllLast()
    {
        final KTypeArrayList<KType> list2 = KTypeArrayList.newInstance();
        list2.add(asArray(0, 1, 2));

        this.list.addLast(list2);
        TestUtils.assertListEquals(this.list.toArray(), 0, 1, 2);
        this.list.addLast(list2);
        TestUtils.assertListEquals(this.list.toArray(), 0, 1, 2, 0, 1, 2);

        this.list.clear();
        final KTypeArrayDeque<KType> deque2 = new KTypeArrayDeque<KType>();
        deque2.addLast(asArray(0, 1, 2));
        this.list.addLast(deque2);
        TestUtils.assertListEquals(this.list.toArray(), 0, 1, 2);
    }

    /* */
    @Test
    public void testRemoveFirst()
    {
        this.list.addLast(this.k1);
        this.list.addLast(this.k2);
        this.list.addLast(this.k3);

        this.list.removeFirst();
        TestUtils.assertListEquals(this.list.toArray(), 2, 3);
        Assert.assertEquals(2, this.list.size());

        this.list.addFirst(this.k4);
        TestUtils.assertListEquals(this.list.toArray(), 4, 2, 3);
        Assert.assertEquals(3, this.list.size());

        this.list.removeFirst();
        this.list.removeFirst();
        this.list.removeFirst();
        Assert.assertEquals(0, this.list.toArray().length);
        Assert.assertEquals(0, this.list.size());
    }

    /* */
    @Test(expected = AssertionError.class)
    public void testRemoveFirstEmpty()
    {
        this.list.removeFirst();
    }

    /* */
    @Test
    public void testRemoveLast()
    {
        this.list.addLast(this.k1);
        this.list.addLast(this.k2);
        this.list.addLast(this.k3);

        this.list.removeLast();
        TestUtils.assertListEquals(this.list.toArray(), 1, 2);
        Assert.assertEquals(2, this.list.size());

        this.list.addLast(this.k4);
        TestUtils.assertListEquals(this.list.toArray(), 1, 2, 4);
        Assert.assertEquals(3, this.list.size());

        this.list.removeLast();
        this.list.removeLast();
        this.list.removeLast();
        Assert.assertEquals(0, this.list.toArray().length);
        Assert.assertEquals(0, this.list.size());
    }

    /* */
    @Test(expected = AssertionError.class)
    public void testRemoveLastEmpty()
    {
        this.list.removeLast();
    }

    /* */
    @Test
    public void testGetFirst()
    {
        this.list.addLast(this.k1);
        this.list.addLast(this.k2);
        TestUtils.assertEquals2(this.k1, this.list.getFirst());
        this.list.addFirst(this.k3);
        TestUtils.assertEquals2(this.k3, this.list.getFirst());
    }

    /* */
    @Test(expected = AssertionError.class)
    public void testGetFirstEmpty()
    {
        this.list.getFirst();
    }

    /* */
    @Test
    public void testGetLast()
    {
        this.list.addLast(this.k1);
        this.list.addLast(this.k2);
        TestUtils.assertEquals2(this.k2, this.list.getLast());
        this.list.addLast(this.k3);
        TestUtils.assertEquals2(this.k3, this.list.getLast());
    }

    /* */
    @Test(expected = AssertionError.class)
    public void testGetLastEmpty()
    {
        this.list.getLast();
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
            this.list.addLast(cast(i % modulo));
            this.sequence.add(cast(i % modulo));
        }

        TestUtils.assertListEquals(this.list.toArray(), this.sequence.toArray());

        final Random rnd = new Random(0xdeadbeef);
        for (int i = 0; i < 500; i++)
        {
            final KType k = cast(rnd.nextInt(modulo));
            Assert.assertEquals(
                    this.list.removeFirst(k) >= 0,
                    this.sequence.removeFirst(k) >= 0);
        }

        TestUtils.assertListEquals(this.list.toArray(), this.sequence.toArray());

        Assert.assertTrue(0 > this.list.removeFirst(cast(modulo + 1)));
        this.list.addLast(cast(modulo + 1));
        Assert.assertTrue(0 <= this.list.removeFirst(cast(modulo + 1)));
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
            this.list.addLast(cast(i % modulo));
            this.sequence.add(cast(i % modulo));
        }

        TestUtils.assertListEquals(this.list.toArray(), this.sequence.toArray());

        final Random rnd = new Random(0x11223344);

        for (int i = 0; i < 500; i++)
        {
            final KType k = cast(rnd.nextInt(modulo));
            Assert.assertEquals(
                    this.list.removeLast(k) >= 0,
                    this.sequence.removeLast(k) >= 0);
        }

        TestUtils.assertListEquals(this.list.toArray(), this.sequence.toArray());

        int removedIndex = this.list.removeLast(cast(modulo + 1));
        Assert.assertTrue("removeLastOccurrence(cast(modulo + 1) = " + removedIndex, removedIndex < 0);

        this.list.addFirst(cast(modulo + 1));

        removedIndex = this.list.removeLast(cast(modulo + 1));
        Assert.assertTrue("removeLastOccurrence(cast(modulo + 1) = " + removedIndex, removedIndex == 0);
    }

    /* */
    @Test
    public void testRemoveAllOccurrences()
    {
        this.list.add(asArray(0, 1, 2, 1, 0, 3, 0));

        Assert.assertEquals(0, this.list.removeAll(this.k4));
        Assert.assertEquals(3, this.list.removeAll(this.k0));
        TestUtils.assertListEquals(this.list.toArray(), 1, 2, 1, 3);
        Assert.assertEquals(1, this.list.removeAll(this.k3));
        TestUtils.assertListEquals(this.list.toArray(), 1, 2, 1);
        Assert.assertEquals(2, this.list.removeAll(this.k1));
        TestUtils.assertListEquals(this.list.toArray(), 2);
        Assert.assertEquals(1, this.list.removeAll(this.k2));
        Assert.assertEquals(0, this.list.size());
    }

    /* */
    @Test
    public void testRemoveAllInLookupContainer()
    {
        this.list.add(asArray(0, 1, 2, 1, 0));

        final KTypeOpenHashSet<KType> set = KTypeOpenHashSet.newInstance();
        set.add(asArray(0, 2));

        Assert.assertEquals(3, this.list.removeAll(set));
        Assert.assertEquals(0, this.list.removeAll(set));

        TestUtils.assertListEquals(this.list.toArray(), 1, 1);
    }

    /* */
    @Test
    public void testClear2()
    {
        this.list.addLast(this.k1);
        this.list.addLast(this.k2);
        this.list.addFirst(this.k3);
        this.list.clear();
        Assert.assertEquals(0, this.list.size());

        this.list.addLast(this.k1);
        TestUtils.assertListEquals(this.list.toArray(), 1);
    }

    /* */
    @Test
    public void testIterable()
    {
        this.list.addLast(this.sequence);

        int count = 0;
        for (final KTypeCursor<KType> cursor : this.list)
        {
            TestUtils.assertEquals2(this.sequence.buffer[count], cursor.value);
            TestUtils.assertEquals2(count, cursor.index);
            count++;
        }
        Assert.assertEquals(count, this.list.size());
        Assert.assertEquals(count, this.sequence.size());

        count = 0;
        this.list.clear();
        for (@SuppressWarnings("unused")
        final KTypeCursor<KType> cursor : this.list)
        {
            count++;
        }
        Assert.assertEquals(0, count);
    }

    /* */
    @Test
    public void testIterator()
    {
        this.list.addLast(asArray(0, 1, 2, 3));

        final Iterator<KTypeCursor<KType>> iterator = this.list.iterator();
        int count = 0;
        while (iterator.hasNext())
        {
            iterator.hasNext();
            iterator.hasNext();
            iterator.hasNext();
            iterator.next();
            count++;
        }
        Assert.assertEquals(count, this.list.size());

        this.list.clear();
        Assert.assertFalse(this.list.iterator().hasNext());
    }

    /* */
    @Test
    public void testDescendingIterator()
    {
        this.list.addLast(this.sequence);

        int index = this.sequence.size() - 1;
        for (final Iterator<KTypeCursor<KType>> i = this.list.descendingIterator(); i.hasNext();)
        {
            final KTypeCursor<KType> cursor = i.next();
            TestUtils.assertEquals2(this.sequence.buffer[index], cursor.value);
            TestUtils.assertEquals2(index, cursor.index);
            index--;
        }
        Assert.assertEquals(-1, index);

        this.list.clear();
        Assert.assertFalse(this.list.descendingIterator().hasNext());
    }

    /* */
    @Test
    public void testDescendingForEachWithProcedure()
    {
        this.list.addLast(this.sequence);

        final IntHolder count = new IntHolder();
        this.list.descendingForEach(new KTypeProcedure<KType>() {
            int index = KTypeLinkedListTest.this.sequence.size();

            @Override
            public void apply(final KType v)
            {
                TestUtils.assertEquals2(KTypeLinkedListTest.this.sequence.buffer[--this.index], v);
                count.value++;
            }
        });
        Assert.assertEquals(count.value, this.list.size());
    }

    ///////////////////////////////// iteration special methods ////////////////////////////////////

    /* */
    @Test
    public void testIterationHeadTail()
    {
        this.list.add(asArray(0, 11, 22, 33, 44, 55));

        final KTypeLinkedList<KType>.ValueIterator it = this.list.iterator();

        Assert.assertEquals(-1, it.cursor.index);
        Assert.assertTrue(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain next
        Assert.assertEquals(null, it.getPrevious());
        Assert.assertEquals(0, castType(it.getNext().value));
        Assert.assertEquals(0, it.getNext().index);

        //Try to move backwards, we stay in head
        it.gotoPrevious();
        Assert.assertEquals(-1, it.cursor.index);
        Assert.assertTrue(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain next
        Assert.assertEquals(null, it.getPrevious());
        Assert.assertEquals(0, castType(it.getNext().value));
        Assert.assertEquals(0, it.getNext().index);

        //iteration 0
        it.gotoNext();
        Assert.assertEquals(0, castType(it.cursor.value));
        Assert.assertEquals(0, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertTrue(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain previous / next
        Assert.assertEquals(null, it.getPrevious());
        Assert.assertEquals(11, castType(it.getNext().value));
        Assert.assertEquals(1, it.getNext().index);

        //iteration 1
        it.gotoNext();
        Assert.assertEquals(11, castType(it.cursor.value));
        Assert.assertEquals(1, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain previous / next
        Assert.assertEquals(0, castType(it.getPrevious().value));
        Assert.assertEquals(0, it.getPrevious().index);
        Assert.assertEquals(22, castType(it.getNext().value));
        Assert.assertEquals(2, it.getNext().index);

        //iteration 2
        it.gotoNext();
        Assert.assertEquals(22, castType(it.cursor.value));
        Assert.assertEquals(2, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain previous / next
        Assert.assertEquals(11, castType(it.getPrevious().value));
        Assert.assertEquals(1, it.getPrevious().index);
        Assert.assertEquals(33, castType(it.getNext().value));
        Assert.assertEquals(3, it.getNext().index);

        //iteration 3
        it.gotoNext();
        Assert.assertEquals(33, castType(it.cursor.value));
        Assert.assertEquals(3, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain previous / next
        Assert.assertEquals(22, castType(it.getPrevious().value));
        Assert.assertEquals(2, it.getPrevious().index);
        Assert.assertEquals(44, castType(it.getNext().value));
        Assert.assertEquals(4, it.getNext().index);

        //iteration 4
        it.gotoNext();
        Assert.assertEquals(44, castType(it.cursor.value));
        Assert.assertEquals(4, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain previous / next
        Assert.assertEquals(33, castType(it.getPrevious().value));
        Assert.assertEquals(3, it.getPrevious().index);
        Assert.assertEquals(55, castType(it.getNext().value));
        Assert.assertEquals(5, it.getNext().index);

        //iteration 5
        it.gotoNext();
        Assert.assertEquals(55, castType(it.cursor.value));
        Assert.assertEquals(5, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertTrue(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain previous / next
        Assert.assertEquals(44, castType(it.getPrevious().value));
        Assert.assertEquals(4, it.getPrevious().index);
        Assert.assertEquals(null, it.getNext());

        //iteration 6
        it.gotoNext();
        Assert.assertEquals(this.list.size(), it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertTrue(it.isTail());
        //obtain previous / next
        Assert.assertEquals(55, castType(it.getPrevious().value));
        Assert.assertEquals(5, it.getPrevious().index);
        Assert.assertEquals(null, it.getNext());

        //iteration 7 : we are already at tail, we don't move further
        it.gotoNext();
        Assert.assertEquals(this.list.size(), it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertTrue(it.isTail());
        //obtain previous / next
        Assert.assertEquals(55, castType(it.getPrevious().value));
        Assert.assertEquals(5, it.getPrevious().index);
        Assert.assertEquals(null, it.getNext());

        //Goes back to head
        it.gotoHead();
        Assert.assertEquals(-1, it.cursor.index);
        Assert.assertTrue(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain next
        Assert.assertEquals(null, it.getPrevious());
        Assert.assertEquals(0, castType(it.getNext().value));
        Assert.assertEquals(0, it.getNext().index);

        //Goes again to tail:
        it.gotoTail();
        Assert.assertEquals(this.list.size(), it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertTrue(it.isTail());
        //obtain previous / next
        Assert.assertEquals(55, castType(it.getPrevious().value));
        Assert.assertEquals(5, it.getPrevious().index);
        Assert.assertEquals(null, it.getNext());

    }

    /* */
    @Test
    public void testIterationHeadTailReversed()
    {
        this.list.add(asArray(0, 11, 22, 33, 44, 55));

        //this is a reversed iteration
        final KTypeLinkedList<KType>.DescendingValueIterator it = this.list.descendingIterator();

        Assert.assertEquals(this.list.size(), it.cursor.index);
        Assert.assertTrue(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain next
        Assert.assertEquals(null, it.getPrevious());
        Assert.assertEquals(55, castType(it.getNext().value));
        Assert.assertEquals(5, it.getNext().index);

        //Try to move backwards, we stay in head
        it.gotoPrevious();
        Assert.assertEquals(this.list.size(), it.cursor.index);
        Assert.assertTrue(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain next
        Assert.assertEquals(null, it.getPrevious());
        Assert.assertEquals(55, castType(it.getNext().value));
        Assert.assertEquals(5, it.getNext().index);

        //iteration 0
        it.gotoNext();
        Assert.assertEquals(55, castType(it.cursor.value));
        Assert.assertEquals(5, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertTrue(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain previous / next
        Assert.assertEquals(null, it.getPrevious());
        Assert.assertEquals(44, castType(it.getNext().value));
        Assert.assertEquals(4, it.getNext().index);

        //iteration 1
        it.gotoNext();
        Assert.assertEquals(44, castType(it.cursor.value));
        Assert.assertEquals(4, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain previous / next
        Assert.assertEquals(55, castType(it.getPrevious().value));
        Assert.assertEquals(5, it.getPrevious().index);
        Assert.assertEquals(33, castType(it.getNext().value));
        Assert.assertEquals(3, it.getNext().index);

        //iteration 2
        it.gotoNext();
        Assert.assertEquals(33, castType(it.cursor.value));
        Assert.assertEquals(3, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain previous / next
        Assert.assertEquals(44, castType(it.getPrevious().value));
        Assert.assertEquals(4, it.getPrevious().index);
        Assert.assertEquals(22, castType(it.getNext().value));
        Assert.assertEquals(2, it.getNext().index);

        //iteration 3
        it.gotoNext();
        Assert.assertEquals(22, castType(it.cursor.value));
        Assert.assertEquals(2, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain previous / next
        Assert.assertEquals(33, castType(it.getPrevious().value));
        Assert.assertEquals(3, it.getPrevious().index);
        Assert.assertEquals(11, castType(it.getNext().value));
        Assert.assertEquals(1, it.getNext().index);

        //iteration 4
        it.gotoNext();
        Assert.assertEquals(11, castType(it.cursor.value));
        Assert.assertEquals(1, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain previous / next
        Assert.assertEquals(22, castType(it.getPrevious().value));
        Assert.assertEquals(2, it.getPrevious().index);
        Assert.assertEquals(0, castType(it.getNext().value));
        Assert.assertEquals(0, it.getNext().index);

        //iteration 5
        it.gotoNext();
        Assert.assertEquals(0, castType(it.cursor.value));
        Assert.assertEquals(0, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertTrue(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain previous / next
        Assert.assertEquals(11, castType(it.getPrevious().value));
        Assert.assertEquals(1, it.getPrevious().index);
        Assert.assertEquals(null, it.getNext());

        //iteration 6
        it.gotoNext();
        Assert.assertEquals(-1, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertTrue(it.isTail());
        //obtain previous / next
        Assert.assertEquals(0, castType(it.getPrevious().value));
        Assert.assertEquals(0, it.getPrevious().index);
        Assert.assertEquals(null, it.getNext());

        //iteration 7 : we are already at tail, we don't move further
        it.gotoNext();
        Assert.assertEquals(-1, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertTrue(it.isTail());
        //obtain previous / next
        Assert.assertEquals(0, castType(it.getPrevious().value));
        Assert.assertEquals(0, it.getPrevious().index);
        Assert.assertEquals(null, it.getNext());

        //Goes back to head
        it.gotoHead();
        Assert.assertEquals(this.list.size(), it.cursor.index);
        Assert.assertTrue(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain next
        Assert.assertEquals(null, it.getPrevious());
        Assert.assertEquals(55, castType(it.getNext().value));
        Assert.assertEquals(5, it.getNext().index);

        //Goes again to tail:
        it.gotoTail();
        Assert.assertEquals(-1, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertTrue(it.isTail());
        //obtain previous / next
        Assert.assertEquals(0, castType(it.getPrevious().value));
        Assert.assertEquals(0, it.getPrevious().index);
        Assert.assertEquals(null, it.getNext());
    }

    /* */
    @Test
    public void testIterationBackAndForth()
    {
        this.list.add(asArray(0, 11, 22, 33, 44, 55));

        final KTypeLinkedList<KType>.ValueIterator it = this.list.iterator();

        //goes 3 steps ==> , 1 step <==
        it.gotoNext().gotoNext().gotoNext().gotoPrevious();
        Assert.assertEquals(11, castType(it.cursor.value));
        Assert.assertEquals(1, it.cursor.index);

        //goes 2 steps ==> , 2 step <==
        it.gotoNext().gotoNext().gotoPrevious().gotoPrevious();
        Assert.assertEquals(11, castType(it.cursor.value));
        Assert.assertEquals(1, it.cursor.index);

        //goes 3 steps <== , 5 step ==> (back is clamped at head, in fact)
        it.gotoPrevious().gotoPrevious().gotoPrevious();
        it.gotoNext().gotoNext().gotoNext().gotoNext().gotoNext();
        Assert.assertEquals(44, castType(it.cursor.value));
        Assert.assertEquals(4, it.cursor.index);
    }

    /* */
    @Test
    public void testIterationBackAndForthReversed()
    {
        this.list.add(asArray(0, 11, 22, 33, 44, 55));

        final KTypeLinkedList<KType>.DescendingValueIterator it = this.list.descendingIterator();

        //goes 3 steps ==> , 1 step <==
        it.gotoNext().gotoNext().gotoNext().gotoPrevious();
        Assert.assertEquals(44, castType(it.cursor.value));
        Assert.assertEquals(4, it.cursor.index);

        //goes 2 steps ==> , 2 step <==
        it.gotoNext().gotoNext().gotoPrevious().gotoPrevious();
        Assert.assertEquals(44, castType(it.cursor.value));
        Assert.assertEquals(4, it.cursor.index);

        //goes 3 steps <== , 5 step ==> (back is clamped at head, in fact)
        it.gotoPrevious().gotoPrevious().gotoPrevious();
        it.gotoNext().gotoNext().gotoNext().gotoNext().gotoNext();
        Assert.assertEquals(11, castType(it.cursor.value));
        Assert.assertEquals(1, it.cursor.index);
    }

    /* */
    @Test
    public void testIterationWithInsertionRemoveSetDelete()
    {
        this.list.add(asArray(0, 11, 22, 33, 44, 55));

        final KTypeLinkedList<KType>.ValueIterator it = this.list.iterator();

        //goes to 11
        it.gotoNext().gotoNext();
        //list.add(asArray(0, 11, 22, 33, 44, 55));
        it.insertAfter(cast(111));
        it.insertAfter(cast(100));
        it.insertAfter(cast(99));
        //==> list.add(asArray(0, 11, 99, 100, 111, 22, 33, 44, 55));
        TestUtils.assertListEquals(this.list.toArray(), 0, 11, 99, 100, 111, 22, 33, 44, 55);
        it.insertBefore(cast(3));
        it.insertBefore(cast(33));
        it.insertBefore(cast(20));
        //==> list.add(asArray(0, 3,33,20, 11, 99, 100, 111, 22, 33, 44, 55));
        TestUtils.assertListEquals(this.list.toArray(), 0, 3, 33, 20, 11, 99, 100, 111, 22, 33, 44, 55);
        it.gotoPrevious();
        Assert.assertEquals(20, castType(it.cursor.value));

        //insert at head
        it.gotoHead();
        it.insertAfter(cast(111));
        it.insertAfter(cast(112));
        // ==> list.add(asArray(112,111,0, 3,33,20, 11, 99, 100, 111, 22, 33, 44, 55));
        TestUtils.assertListEquals(this.list.toArray(), 112, 111, 0, 3, 33, 20, 11, 99, 100, 111, 22, 33, 44, 55);

        //insert at tail
        it.gotoTail();
        it.insertBefore(cast(7));
        it.insertBefore(cast(9));
        // ==> list.add(asArray(112,111,0, 3,33,20, 11, 99, 100, 111, 22, 33, 44, 55,7, 9));
        TestUtils.assertListEquals(this.list.toArray(), 112, 111, 0, 3, 33, 20, 11, 99, 100, 111, 22, 33, 44, 55, 7, 9);

        //////// remove / set / delete /////////////////////

        //rewind to first
        it.gotoHead().gotoNext();
        //set -88 at 0 value
        it.gotoNext().gotoNext();
        Assert.assertEquals(0, castType(it.set(cast(88))));
        // ==> list.add(asArray(112,111,88, 3,33,20, 11, 99, 100, 111, 22, 33, 44, 55,7, 9));
        TestUtils.assertListEquals(this.list.toArray(), 112, 111, 88, 3, 33, 20, 11, 99, 100, 111, 22, 33, 44, 55, 7, 9);

        //move forward by 2 elements : 33
        it.gotoNext().gotoNext();
        //remove next = 20
        KTypeCursor<KType> removed = it.removeNext();
        Assert.assertEquals(5, removed.index);
        Assert.assertEquals(20, castType(removed.value));
        // ==> list.add(asArray(112, 111,88, 3, 33, 11, 99, 100, 111, 22, 33, 44, 55,7, 9));
        //it still points to 33
        TestUtils.assertListEquals(this.list.toArray(), 112, 111, 88, 3, 33, /*20 */11, 99, 100, 111, 22, 33, 44, 55, 7, 9);
        Assert.assertEquals(33, castType(it.cursor.value));
        Assert.assertEquals(4, it.cursor.index);

        //move again of 3 : 100
        it.gotoNext().gotoNext().gotoNext();
        //remove the previous = 99
        removed = it.removePrevious();
        Assert.assertEquals(6, removed.index);
        Assert.assertEquals(99, castType(removed.value));

        TestUtils.assertListEquals(this.list.toArray(), 112, 111, 88, 3, 33, /*20 */11, /*99 */100, 111, 22, 33, 44, 55, 7, 9);
        //the iterator still points to 100
        Assert.assertEquals(100, castType(it.cursor.value));
        Assert.assertEquals(6, it.cursor.index);

        //move again of 4 : 44
        it.gotoNext().gotoNext().gotoNext().gotoNext();
        //remove itself
        it.delete();
        //the iterator now points to 55
        TestUtils.assertListEquals(this.list.toArray(), 112, 111, 88, 3, 33, /*20 */11, /*99 */100, 111, 22, 33, /* 44 */55, 7, 9);
        Assert.assertEquals(55, castType(it.cursor.value));
        Assert.assertEquals(10, it.cursor.index);
    }

    /* */
    @Test
    public void testIterationLoopWithDelete()
    {
        this.list.add(asArray(112, 111, 88, 3, 33, 20, 11, 99, 100, 111, 22, 33, 44, 55, 7, 9));

        final int poolSize = this.list.valueIteratorPool.size();

        KTypeLinkedList<KType>.ValueIterator it = null;
        try
        {
            for (it = this.list.iterator().gotoNext(); !it.isTail(); it.gotoNext())
            {
                if (it.getNext() != null && castType(it.getNext().value) == 88 ||
                        it.getNext() != null && castType(it.getNext().value) == 99 ||
                        it.getNext() != null && castType(it.getNext().value) == 55)
                {

                    it.removeNext();
                }
            }

            TestUtils.assertListEquals(this.list.toArray(), 112, 111, 3, 33, 20, 11, 100, 111, 22, 33, 44, 7, 9);
            it.release();
            //empty all
            for (it = this.list.iterator().gotoNext(); !it.isTail(); it.gotoNext())
            {
                it.delete();
                //the for advanced itself, so go back one position
                it.gotoPrevious();
            }

            Assert.assertEquals(0, this.list.size());
            it.release();
            //try to iterate an empty list
            for (it = this.list.iterator().gotoNext(); !it.isTail(); it.gotoNext())
            {
                it.delete();
                //the for advanced itself, so go back one position
                it.gotoPrevious();
            }

            Assert.assertEquals(0, this.list.size());
        }
        finally
        {
            it.release();
        }

        Assert.assertEquals(poolSize, this.list.valueIteratorPool.size());

    }

    /* */
    @Test
    public void testIterationLoopWithDeleteReversed()
    {
        this.list.add(asArray(112, 111, 88, 3, 33, 20, 11, 99, 100, 111, 22, 33, 44, 55, 7, 9));

        final int poolSize = this.list.valueIteratorPool.size();

        KTypeLinkedList<KType>.DescendingValueIterator it = null;
        try
        {
            for (it = this.list.descendingIterator().gotoNext(); !it.isTail(); it.gotoNext())
            {
                if (it.getNext() != null && castType(it.getNext().value) == 88 ||
                        it.getNext() != null && castType(it.getNext().value) == 99 ||
                        it.getNext() != null && castType(it.getNext().value) == 55)
                {

                    it.removeNext();
                }
            }

            TestUtils.assertListEquals(this.list.toArray(), 112, 111, 3, 33, 20, 11, 100, 111, 22, 33, 44, 7, 9);
            it.release();
            //empty all
            for (it = this.list.descendingIterator().gotoNext(); !it.isTail(); it.gotoNext())
            {
                it.delete();
                //the for advanced itself, so go back one position
                it.gotoPrevious();
            }

            Assert.assertEquals(0, this.list.size());
            it.release();
            //try to iterate an empty list
            for (it = this.list.descendingIterator().gotoNext(); !it.isTail(); it.gotoNext())
            {
                it.delete();
                //the for advanced itself, so go back one position
                it.gotoPrevious();
            }

            Assert.assertEquals(0, this.list.size());
        }
        finally
        {
            it.release();
        }

        Assert.assertEquals(poolSize, this.list.valueIteratorPool.size());
    }

    /* */
    @Test
    public void testIterationWithInsertionRemoveSetDeleteReversed()
    {
        this.list.add(asArray(0, 11, 22, 33, 44, 55));

        final KTypeLinkedList<KType>.DescendingValueIterator it = this.list.descendingIterator();

        //goes to 44
        it.gotoNext().gotoNext();
        //list.add(asArray(0, 11, 22, 33, 44, 55));
        it.insertAfter(cast(111));
        it.insertAfter(cast(100));
        it.insertAfter(cast(99));
        //==> list.add(asArray(0, 11, 22, 33, 111, 100, 99, 44, 55));
        TestUtils.assertListEquals(this.list.toArray(), 0, 11, 22, 33, 111, 100, 99, 44, 55);
        it.insertBefore(cast(3));
        it.insertBefore(cast(33));
        it.insertBefore(cast(20));
        //==> list.add(asArray(0, 11, 22, 33, 111, 100, 99, 44, 20, 33,3,55));
        TestUtils.assertListEquals(this.list.toArray(), 0, 11, 22, 33, 111, 100, 99, 44, 20, 33, 3, 55);
        it.gotoPrevious();
        Assert.assertEquals(20, castType(it.cursor.value));

        //insert at head
        it.gotoHead();
        it.insertAfter(cast(111));
        it.insertAfter(cast(112));
        // ==> list.add(asArray(0, 11, 22, 33, 111, 100, 99, 44, 20, 33,3,55,111,112));
        TestUtils.assertListEquals(this.list.toArray(), 0, 11, 22, 33, 111, 100, 99, 44, 20, 33, 3, 55, 111, 112);

        //insert at tail
        it.gotoTail();
        it.insertBefore(cast(7));
        it.insertBefore(cast(9));
        // ==> list.add(asArray(9,7, 0, 11, 22, 33, 111, 100, 99, 44, 20, 33,3,55,111,112));
        TestUtils.assertListEquals(this.list.toArray(), 9, 7, 0, 11, 22, 33, 111, 100, 99, 44, 20, 33, 3, 55, 111, 112);

        //////// remove / set / delete /////////////////////

        //rewind to first
        it.gotoHead().gotoNext();
        //set 88 at 55 value
        it.gotoNext().gotoNext();
        Assert.assertEquals(55, castType(it.set(cast(88))));
        // ==> list.add(asArray( 9, 7, 0, 11, 22, 33, 111, 100, 99, 44, 20, 33,3,-88,111,112));
        TestUtils.assertListEquals(this.list.toArray(), 9, 7, 0, 11, 22, 33, 111, 100, 99, 44, 20, 33, 3, 88, 111, 112);

        //move forward by 2 elements : 33
        it.gotoNext().gotoNext();
        //remove next = 20
        KTypeCursor<KType> removed = it.removeNext();
        Assert.assertEquals(10, removed.index);
        Assert.assertEquals(20, castType(removed.value));
        // ==> list.add(asArray(9, 7, 0, 11, 22, 33, 111, 100, 99, 44, 20, 33, 3,88,111,112));
        //it still points to 33
        TestUtils.assertListEquals(this.list.toArray(), 9, 7, 0, 11, 22, 33, 111, 100, 99, 44 /*20 */, 33, 3, 88, 111, 112);
        Assert.assertEquals(33, castType(it.cursor.value));
        Assert.assertEquals(10, it.cursor.index);

        //move again of 3 : 100
        it.gotoNext().gotoNext().gotoNext();
        //remove the previous = 99
        removed = it.removePrevious();
        Assert.assertEquals(8, removed.index);
        Assert.assertEquals(99, castType(removed.value));

        TestUtils.assertListEquals(this.list.toArray(), 9, 7, 0, 11, 22, 33, 111, 100, /*99*/44 /*20 */, 33, 3, 88, 111, 112);
        //the iterator still points to 100
        Assert.assertEquals(100, castType(it.cursor.value));
        Assert.assertEquals(7, it.cursor.index);

        //move again of 4 : 11
        it.gotoNext().gotoNext().gotoNext().gotoNext();
        //remove itself
        it.delete();
        //the iterator now points to 0
        TestUtils.assertListEquals(this.list.toArray(), 9, 7, 0 /*11 */, 22, 33, 111, 100, /*99*/44 /*20 */, 33, 3, 88, 111, 112);
        Assert.assertEquals(0, castType(it.cursor.value));
        Assert.assertEquals(2, it.cursor.index);
    }

    ////////////////////////////////// END iteration special methods  ////////////////////////////////////

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testAgainstArrayDeque()
    {
        final Random rnd = new Random();
        final int rounds = 10000;
        final int modulo = 100;

        final ArrayDeque<KType> ad = new ArrayDeque<KType>();
        for (int i = 0; i < rounds; i++)
        {
            final KType k = cast(rnd.nextInt(modulo));

            final int op = rnd.nextInt(8);
            if (op < 2)
            {
                this.list.addFirst(k);
                ad.addFirst(k);
            }
            else if (op < 4)
            {
                this.list.addLast(k);
                ad.addLast(k);
            }
            else if (op < 5 && ad.size() > 0)
            {
                this.list.removeLast();
                ad.removeLast();
            }
            else if (op < 6 && ad.size() > 0)
            {
                this.list.removeLast();
                ad.removeLast();
            }
            else if (op < 7)
            {
                Assert.assertEquals(
                        ad.removeFirstOccurrence(k),
                        this.list.removeFirst(k) >= 0);
            }
            else if (op < 8)
            {
                Assert.assertEquals(
                        ad.removeLastOccurrence(k),
                        this.list.removeLast(k) >= 0);
            }
            Assert.assertEquals(ad.size(), this.list.size());
        }

        Assert.assertArrayEquals(ad.toArray(), this.list.toArray());
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testHashCodeEquals2()
    {
        final KTypeLinkedList<KType> l0 = KTypeLinkedList.newInstance();
        Assert.assertEquals(1, l0.hashCode());
        Assert.assertEquals(l0, KTypeLinkedList.from());

        final KTypeLinkedList<KType> l1 = KTypeLinkedList.from(this.k1, this.k2, this.k3);
        final KTypeLinkedList<KType> l2 = KTypeLinkedList.from(this.k1, this.k2);
        l2.addLast(this.k3);

        Assert.assertEquals(l1.hashCode(), l2.hashCode());
        Assert.assertEquals(l1, l2);
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
        final KTypeLinkedList<KType> newList = KTypeLinkedList.newInstance(PREALLOCATED_SIZE);

        //computed real capacity
        final int realCapacity = newList.capacity();

        //3) Add PREALLOCATED_SIZE different values. At the end, size() must be == realCapacity,
        //and internal buffer/allocated must not have changed of size
        final int contructorBufferSize = newList.buffer.length;

        Assert.assertEquals(contructorBufferSize, newList.buffer.length);

        for (int i = 0; i < 1.5 * realCapacity; i++) {

            newList.add(cast(randomVK.nextInt()));

            //internal size has not changed until realCapacity
            if (newList.size() <= realCapacity) {

                Assert.assertEquals(contructorBufferSize, newList.buffer.length);
            }

            if (contructorBufferSize < newList.buffer.length) {
                //The container as just reallocated, its actual size must be not too far from the previous capacity:
                Assert.assertTrue("Container as reallocated at size = " + newList.size() + " with previous capacity = " + realCapacity,
                        (newList.size() - realCapacity) <= 2);
                break;
            }
        }
    }

    /* */
    @Test
    public void testEquals()
    {
        final int modulo = 127;
        final int count = 15000;

        final KTypeLinkedList<KType> list2 = KTypeLinkedList.newInstance();

        for (int i = 0; i < count; i++)
        {
            //add in head an in queue indifferently
            if (i % 3 == 0) {
                this.list.addFirst(cast(i % modulo));
                list2.addFirst(cast(i % modulo));
            }
            else {
                this.list.addLast(cast(i % modulo));
                list2.addLast(cast(i % modulo));
            }
        }

        //both elements by elements are equal
        TestUtils.assertListEquals(this.list.toArray(), list2.toArray());

        //Both dequeue are indeed equal
        Assert.assertTrue(this.list.equals(list2));
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
                this.list.addFirst(cast(i % modulo));
                this.sequence.insert(0, cast(i % modulo));
            }
            else {
                this.list.addLast(cast(i % modulo));
                this.sequence.add(cast(i % modulo));
            }
        }

        //both elements by elements are equal
        TestUtils.assertListEquals(this.list.toArray(), this.sequence.toArray());

        //The array list and dequeue are indeed equal
        Assert.assertTrue(this.list.equals(this.sequence));
    }

    /* */
    @Test
    public void testDequeContainerEquals()
    {
        final int modulo = 127;
        final int count = 15000;

        final KTypeArrayDeque<KType> deque2 = KTypeArrayDeque.newInstance();

        for (int i = 0; i < count; i++)
        {
            //add in head an in queue indifferently
            if (i % 3 == 0) {
                this.list.addFirst(cast(i % modulo));
                deque2.addFirst(cast(i % modulo));
            }
            else {
                this.list.addLast(cast(i % modulo));
                deque2.addLast(cast(i % modulo));
            }
        }

        //both elements by elements are equal
        TestUtils.assertListEquals(this.list.toArray(), deque2.toArray());

        //Both Dequeues are indeed equal: explicitely cast it in KTypeDeque
        Assert.assertTrue(this.list.equals((KTypeDeque<KType>) deque2));
    }
}
