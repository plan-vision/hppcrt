package com.carrotsearch.hppcrt.lists;

import java.util.*;

import org.junit.*;

import static com.carrotsearch.hppcrt.TestUtils.*;
import static org.junit.Assert.*;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.cursors.*;
import com.carrotsearch.hppcrt.mutables.*;
import com.carrotsearch.hppcrt.predicates.*;
import com.carrotsearch.hppcrt.procedures.*;
import com.carrotsearch.hppcrt.sets.*;
import com.carrotsearch.hppcrt.sorting.*;
import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;

// ${TemplateOptions.doNotGenerateKType("BOOLEAN")}
/**
 * Unit tests for {@link KTypeArrayList}.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeArrayListTest<KType> extends AbstractKTypeTest<KType>
{
    /**
     * Per-test fresh initialized instance.
     */
    public KTypeArrayList<KType> list;

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
        this.list = KTypeArrayList.newInstance();
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
        final KTypeArrayList<KType> list2 = KTypeArrayList.newInstance();
        list2.add(asArray(0, 1, 2));

        this.list.addAll(list2);
        this.list.addAll(list2);

        TestUtils.assertListEquals(this.list.toArray(), 0, 1, 2, 0, 1, 2);
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testAddAll_subclass()
    {
        class A {
        }

        class B extends A {
        }

        final KTypeArrayList<B> list2 = new KTypeArrayList<B>();
        list2.add(new B());

        final KTypeArrayList<A> list3 = new KTypeArrayList<A>();
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
        this.list.add(asArray(0, 1, 2, 3, 4, 5, 6, 7, 8));

        Assert.assertEquals(0, castType(this.list.remove(0)));
        Assert.assertEquals(3, castType(this.list.remove(2)));
        Assert.assertEquals(2, castType(this.list.remove(1)));
        Assert.assertEquals(6, castType(this.list.remove(3)));

        TestUtils.assertListEquals(this.list.toArray(), 1, 4, 5, 7, 8);
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
    public void testRemoveFirstLast()
    {
        this.list.add(asArray(0, 1, 2, 1, 0));

        Assert.assertEquals(-1, this.list.removeFirstOccurrence(this.k5));
        Assert.assertEquals(-1, this.list.removeLastOccurrence(this.k5));
        TestUtils.assertListEquals(this.list.toArray(), 0, 1, 2, 1, 0);

        Assert.assertEquals(1, this.list.removeFirstOccurrence(this.k1));
        TestUtils.assertListEquals(this.list.toArray(), 0, 2, 1, 0);
        Assert.assertEquals(3, this.list.removeLastOccurrence(this.k0));
        TestUtils.assertListEquals(this.list.toArray(), 0, 2, 1);
        Assert.assertEquals(0, this.list.removeLastOccurrence(this.k0));
        TestUtils.assertListEquals(this.list.toArray(), 2, 1);
        Assert.assertEquals(-1, this.list.removeLastOccurrence(this.k0));

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        this.list.clear();
        this.list.add(newArray(this.k0, null, this.k2, null, this.k0));
        Assert.assertEquals(1, this.list.removeFirstOccurrence(null));
        Assert.assertEquals(2, this.list.removeLastOccurrence(null));
        TestUtils.assertListEquals(this.list.toArray(), 0, 2, 0);
        /*! #end !*/
    }

    /* */
    @Test
    public void testRemoveAll()
    {
        this.list.add(asArray(0, 1, 0, 1, 0));

        Assert.assertEquals(0, this.list.removeAllOccurrences(this.k2));
        Assert.assertEquals(3, this.list.removeAllOccurrences(this.k0));
        TestUtils.assertListEquals(this.list.toArray(), 1, 1);

        Assert.assertEquals(2, this.list.removeAllOccurrences(this.k1));
        Assert.assertTrue(this.list.isEmpty());

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        this.list.clear();
        this.list.add(newArray(this.k0, null, this.k2, null, this.k0));
        Assert.assertEquals(2, this.list.removeAllOccurrences(null));
        Assert.assertEquals(0, this.list.removeAllOccurrences(null));
        TestUtils.assertListEquals(this.list.toArray(), 0, 2, 0);
        /*! #end !*/
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
    public void testRemoveAllWithPredicate()
    {
        this.list.add(newArray(this.k0, this.k1, this.k2, this.k1, this.k4));

        Assert.assertEquals(3, this.list.removeAll(new KTypePredicate<KType>()
                {
            @Override
            public boolean apply(final KType v)
            {
                return v == KTypeArrayListTest.this.key1 || v == KTypeArrayListTest.this.key2;
            };
                }));

        TestUtils.assertListEquals(this.list.toArray(), 0, 4);
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
                return v == KTypeArrayListTest.this.key1 || v == KTypeArrayListTest.this.key2;
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
                    if (v == KTypeArrayListTest.this.key2) {
                        throw t;
                    }
                    return v == KTypeArrayListTest.this.key1;
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

    @Test
    public void testResizeAndCleanBuffer()
    {
        this.list.ensureCapacity(20);
        Arrays.fill(this.list.buffer, this.k1);

        this.list.resize(10);
        Assert.assertEquals(10, this.list.size());

        for (int i = 0; i < this.list.size(); i++) {

            TestUtils.assertEquals2(Intrinsics.<KType> defaultKTypeValue(), this.list.get(i));

        }

        Arrays.fill(this.list.buffer, Intrinsics.<KType> defaultKTypeValue());

        for (int i = 5; i < this.list.size(); i++) {
            this.list.set(i, this.k1);
        }

        this.list.resize(5);
        Assert.assertEquals(5, this.list.size());

        for (int i = this.list.size(); i < this.list.buffer.length; i++) {
            //only objects get cleared for GC sake.
            /*! #if ($TemplateOptions.KTypeGeneric) !*/
            TestUtils.assertEquals2(Intrinsics.<KType> defaultKTypeValue(), this.list.buffer[i]);
            /*! #end !*/
        }

    }

    /* */
    @Test
    public void testTrimToSize()
    {
        this.list.add(asArray(1, 2));
        this.list.trimToSize();
        Assert.assertEquals(2, this.list.buffer.length);
    }

    /* */
    @Test
    public void testRelease()
    {
        this.list.add(asArray(1, 2));
        this.list.release();
        Assert.assertEquals(0, this.list.size());
        this.list.add(asArray(1, 2));
        Assert.assertEquals(2, this.list.size());
    }

    /* */
    @Test
    public void testGrowth()
    {
        final int maxGrowth = 10;
        final int count = 500;

        this.list = new KTypeArrayList<KType>(0,
                new BoundedProportionalArraySizingStrategy(5, maxGrowth, 2));

        for (int i = 0; i < count; i++) {
            this.list.add(cast(i));
        }

        Assert.assertEquals(count, this.list.size());

        for (int i = 0; i < count; i++) {
            TestUtils.assertEquals2(cast(i), this.list.get(i));
        }

        Assert.assertTrue("Buffer size: 510 <= " + this.list.buffer.length,
                this.list.buffer.length <= count + maxGrowth);
    }

    /* */
    @Test
    public void testIterable()
    {
        this.list.add(asArray(0, 1, 2, 3));
        int count = 0;
        for (final KTypeCursor<KType> cursor : this.list)
        {
            count++;
            TestUtils.assertEquals2(this.list.get(cursor.index), cursor.value);
            TestUtils.assertEquals2(this.list.buffer[cursor.index], cursor.value);
        }
        Assert.assertEquals(count, this.list.size());

        count = 0;
        this.list.resize(0);
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
        this.list.add(asArray(0, 1, 2, 3));
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

        this.list.resize(0);
        Assert.assertFalse(this.list.iterator().hasNext());
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
                TestUtils.assertEquals2(v, KTypeArrayListTest.this.list.get(this.index++));
                holder.value = this.index;
            }
        });
        Assert.assertEquals(holder.value, this.list.size());
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
                TestUtils.assertEquals2(v, KTypeArrayListTest.this.list.get(this.index++));
            }
        }).index;
        Assert.assertEquals(result, this.list.size());
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
                TestUtils.assertEquals2(v, KTypeArrayListTest.this.list.get(this.index));
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
                TestUtils.assertEquals2(v, KTypeArrayListTest.this.list.get(this.index));
                this.value = castType(v);

                this.index++;

                return true;
            }
        }).value;

        Assert.assertEquals(lastValue, 12);
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
        final KTypeArrayList<KType> variable = KTypeArrayList.from(this.k1, this.k2, this.k3);
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

        final KTypeArrayList<KType> l1 = KTypeArrayList.from(this.k1, this.k2, this.k3);
        final KTypeArrayList<KType> l2 = KTypeArrayList.from(this.k1, this.k2);
        l2.add(this.k3);

        Assert.assertEquals(l1.hashCode(), l2.hashCode());
        Assert.assertEquals(l1, l2);
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    @SuppressWarnings("unchecked")
    public void testHashCodeWithNulls()
    {
        final KTypeArrayList<KType> l1 = KTypeArrayList.from(this.k1, null, this.k3);
        final KTypeArrayList<KType> l2 = KTypeArrayList.from(this.k1, null, this.k3);

        Assert.assertEquals(l1.hashCode(), l2.hashCode());
        Assert.assertEquals(l1, l2);
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testToArrayWithClass()
    {
        final KTypeArrayList<Integer> l1 = KTypeArrayList.from(1, 2, 3);
        final Integer[] result = l1.toArray(Integer.class);
        Assert.assertArrayEquals(new Integer[] { 1, 2, 3 }, result); // dummy
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    @Test
    public void testToArray()
    {
        final KTypeArrayList<KType> l1 = KTypeArrayList.from(this.k1, this.k2, this.k3);
        final Object[] result = l1.toArray();
        Assert.assertArrayEquals(new Object[] { this.k1, this.k2, this.k3 }, result);
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

        final KTypeArrayList<KType> cloned = this.list.clone();
        cloned.removeAllOccurrences(this.key1);

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
                + this.key3 + "]", KTypeArrayList.from(this.k1, this.k2, this.k3).toString());
    }

    @Test
    public void testPooledIteratorForEach()
    {
        // Unbroken for-each loop
        final int TEST_SIZE = 10000;
        final long TEST_ROUNDS = 100;

        final KTypeArrayList<KType> testContainer = createArrayWithOrderedData(TEST_SIZE);

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

        final KTypeArrayList<KType> testContainer = createArrayWithOrderedData(TEST_SIZE);

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

        final KTypeArrayList<KType> testContainer = createArrayWithOrderedData(TEST_SIZE);

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

            final KTypeArrayList<KType>.ValueIterator loopIterator = testContainer.iterator();

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

        final KTypeArrayList<KType> testContainer = createArrayWithOrderedData(TEST_SIZE);
        final int startingPoolSize = testContainer.valueIteratorPool.size();

        int count = 0;
        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            //Classical iterator loop, with manually allocated Iterator
            final long initialPoolSize = testContainer.valueIteratorPool.size();

            final KTypeArrayList<KType>.ValueIterator loopIterator = testContainer.iterator();

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

        final KTypeArrayList<KType> testContainer = createArrayWithOrderedData(TEST_SIZE);

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
        KTypeArrayList<KType>.ValueIterator loopIterator = null;

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
        final int TEST_SIZE = 104171;
        final long TEST_ROUNDS = 15;

        final KTypeArrayList<KType> testContainer = createArrayWithOrderedData(TEST_SIZE);

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
        KTypeArrayList<KType>.ValueIterator loopIterator = null;

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

        //A) Sort an array of random values of primitive types

/*! #if ($TemplateOptions.KTypePrimitive)
        //A-1) full sort
        KTypeArrayList<KType> primitiveList = createArrayWithRandomData(TEST_SIZE, currentSeed);
         KTypeArrayList<KType> primitiveListOriginal = createArrayWithRandomData(TEST_SIZE, currentSeed);
        primitiveList.sort();
        assertOrder(primitiveListOriginal, primitiveList, 0, primitiveList.size());
        //A-2) Partial sort
        primitiveList = createArrayWithRandomData(TEST_SIZE, currentSeed);
        primitiveListOriginal = createArrayWithRandomData(TEST_SIZE, currentSeed);
        primitiveList.sort(lowerRange, upperRange);
        assertOrder(primitiveListOriginal, primitiveList, lowerRange, upperRange);
#end !*/

        //B) Sort with Comparator
        //B-1) Full sort
        KTypeArrayList<KType> comparatorList = createArrayWithRandomData(TEST_SIZE, currentSeed);
        KTypeArrayList<KType> comparatorListOriginal = createArrayWithRandomData(TEST_SIZE, currentSeed);
        comparatorList.sort(comp);
        assertOrder(comparatorListOriginal, comparatorList, 0, comparatorList.size());
        //B-2) Partial sort
        comparatorList = createArrayWithRandomData(TEST_SIZE, currentSeed);
        comparatorListOriginal = createArrayWithRandomData(TEST_SIZE, currentSeed);
        comparatorList.sort(lowerRange, upperRange, comp);
        assertOrder(comparatorListOriginal, comparatorList, lowerRange, upperRange);
    }

    @Repeat(iterations = 50)
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
        final KTypeArrayList<KType> newList = KTypeArrayList.newInstanceWithCapacity(PREALLOCATED_SIZE);

        //3) Add PREALLOCATED_SIZE different values. At the end, size() must be == PREALLOCATED_SIZE,
        //and internal buffer/allocated must not have changed of size
        final int contructorBufferSize = newList.buffer.length;

        for (int i = 0; i < PREALLOCATED_SIZE; i++)
        {
            newList.add(cast(randomVK.nextInt()));

            //internal size has not changed.
            Assert.assertEquals(contructorBufferSize, newList.buffer.length);
        }

        Assert.assertEquals(PREALLOCATED_SIZE, newList.size());
    }

    /**
     * Test natural ordering between [startIndex; endIndex[, starting from original
     * @param expected
     * @param actual
     * @param length
     */
    private void assertOrder(final KTypeArrayList<KType> original, final KTypeArrayList<KType> order, final int startIndex, final int endIndex)
    {

        Assert.assertEquals(original.size(), order.size());

        //A) check that the required range is ordered
        for (int i = startIndex + 1; i < endIndex; i++)
        {
            if (castType(order.get(i - 1)) > castType(order.get(i)))
            {
                Assert.assertTrue(String.format("Not ordered: (previous, next) = (%d, %d) at index %d",
                        castType(order.get(i - 1)), castType(order.get(i)), i), false);
            }
        }

        //B) Check that the rest is untouched also
        for (int i = 0; i < startIndex; i++)
        {
            if (castType(original.get(i)) != castType(order.get(i)))
            {
                Assert.assertTrue(String.format("This index has been touched: (original, erroneously modified) = (%d, %d) at index %d",
                        castType(original.get(i)), castType(order.get(i)), i), false);
            }
        }

        for (int i = endIndex; i < original.size(); i++)
        {
            if (castType(original.get(i)) != castType(order.get(i)))
            {
                Assert.assertTrue(String.format("This index has been touched: (original, erroneously modified) = (%d, %d) at index %d",
                        castType(original.get(i)), castType(order.get(i)), i), false);
            }
        }
    }

    private KTypeArrayList<KType> createArrayWithOrderedData(final int size)
    {
        final KTypeArrayList<KType> newArray = KTypeArrayList.newInstanceWithCapacity(KTypeArrayList.DEFAULT_CAPACITY);

        for (int i = 0; i < size; i++)
        {
            newArray.add(cast(i));
        }

        return newArray;
    }

    private KTypeArrayList<KType> createArrayWithRandomData(final int size, final long currentSeed)
    {
        final Random prng = new Random(currentSeed);

        final KTypeArrayList<KType> newArray = KTypeArrayList.newInstanceWithCapacity(KTypeArrayList.DEFAULT_CAPACITY);

        for (int i = 0; i < size; i++)
        {
            newArray.add(cast(prng.nextInt()));
        }

        return newArray;
    }

}
