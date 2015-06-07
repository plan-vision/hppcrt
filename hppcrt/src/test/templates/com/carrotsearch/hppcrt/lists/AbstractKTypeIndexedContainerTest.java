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
import com.carrotsearch.hppcrt.sets.KTypeHashSet;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.*;

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
/**
 * Tests common for all kinds of hash sets {@link KTypeIndexedContainer}.
 */
// ${TemplateOptions.doNotGenerateKType("BOOLEAN")}
/*! ${TemplateOptions.generatedAnnotation} !*/
public abstract class AbstractKTypeIndexedContainerTest<KType> extends AbstractKTypeTest<KType>
{
    @BeforeClass
    public static void configure()
    {
        IteratorPool.configureInitialPoolSize(8);
    }

    /**
     * Customize this for concrete hash set creation
     * @param initialCapacity
     * @param loadFactor
     * @param strategy
     * @return
     */
    protected abstract KTypeIndexedContainer<KType> createNewInstance(final int initialCapacity);

    protected abstract KType[] getBuffer(KTypeIndexedContainer<KType> testList);

    protected abstract KTypeIndexedContainer<KType> getClone(KTypeIndexedContainer<KType> testList);

    protected abstract KTypeIndexedContainer<KType> getFrom(KTypeContainer<KType> container);

    protected abstract KTypeIndexedContainer<KType> getFrom(final KType... elements);

    protected abstract KTypeIndexedContainer<KType> getFromArray(KType[] keys);

    protected abstract void addFromArray(KTypeIndexedContainer<KType> testList, KType... keys);

    protected abstract KTypeIndexedContainer<KType> getCopyConstructor(KTypeIndexedContainer<KType> testList);

    abstract int getValuePoolSize(KTypeIndexedContainer<KType> testList);

    abstract int getValuePoolCapacity(KTypeIndexedContainer<KType> testList);

    /**
     * Per-test fresh initialized instance.
     */
    protected KTypeIndexedContainer<KType> list;

    @Before
    public void initialize() {

        this.list = createNewInstance();
    }

    protected KTypeIndexedContainer<KType> createNewInstance() {

        return createNewInstance(0);
    }

    /**
     * Check that the set is consistent, i.e all allocated slots are reachable by get(),
     * and all not-allocated contains nulls if Generic
     * @param set
     */
    @After 
    public void checkConsistency()
    {
        if (this.list != null)
        {
            for (int i = this.list.size(); i < getBuffer(this.list).length; i++) {
                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                Assert.assertTrue(Intrinsics.<KType> empty() == getBuffer(this.list)[i]);
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
        this.list.add(this.key1);
        this.list.add(this.key2);
        this.list.add(this.key3);
        this.list.add(this.key5);
        TestUtils.assertListEquals(this.list.toArray(), 1, 2, 3, 5);
    }

    /* */
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testAddVarArg()
    {
        addFromArray(this.list, asArray(0, 1, 2, 3));
        addFromArray(this.list, this.key4, this.key5, this.key6, this.key7);
        TestUtils.assertListEquals(this.list.toArray(), 0, 1, 2, 3, 4, 5, 6, 7);
    }

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
        addFromArray(this.list, asArray(0, 1, 2));

        TestUtils.assertEquals2(0, this.list.set(0, this.k3));
        TestUtils.assertEquals2(1, this.list.set(1, this.k4));
        TestUtils.assertEquals2(2, this.list.set(2, this.k5));

        TestUtils.assertListEquals(this.list.toArray(), 3, 4, 5);
    }

    /* */
    @Test
    public void testRemove()
    {
        addFromArray(this.list, asArray(0, 1, 2, 3, 4, 5, 6, 7, 8));

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
        addFromArray(this.list, asArray(0, 1, 2, 3, 4));

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
        addFromArray(this.list, asArray(0, 1, 2, 1, 0));

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
        addFromArray(this.list, newArray(this.k0, null, this.k2, null, this.k0));
        Assert.assertEquals(1, this.list.removeFirst(null));
        Assert.assertEquals(2, this.list.removeLast(null));
        TestUtils.assertListEquals(this.list.toArray(), 0, 2, 0);
        /*! #end !*/
    }

    /* */
    @Test
    public void testRemoveAll()
    {
        addFromArray(this.list, asArray(0, 1, 0, 1, 0));

        Assert.assertEquals(0, this.list.removeAll(this.k2));
        Assert.assertEquals(3, this.list.removeAll(this.k0));
        TestUtils.assertListEquals(this.list.toArray(), 1, 1);

        Assert.assertEquals(2, this.list.removeAll(this.k1));
        Assert.assertTrue(this.list.isEmpty());

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        this.list.clear();
        addFromArray(this.list, newArray(this.k0, null, this.k2, null, this.k0));
        Assert.assertEquals(2, this.list.removeAll((KType) null));
        Assert.assertEquals(0, this.list.removeAll((KType) null));
        TestUtils.assertListEquals(this.list.toArray(), 0, 2, 0);
        /*! #end !*/
    }

    /* */
    @Test
    public void testRemoveAllEverything()
    {
        addFromArray(this.list, asArray(1, 1, 1, 1, 1));

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
        addFromArray(this.list, asArray(0, 1, 2, 1, 0));

        final KTypeHashSet<KType> list2 = KTypeHashSet.newInstance();
        list2.add(asArray(0, 2));

        Assert.assertEquals(3, this.list.removeAll(list2));
        Assert.assertEquals(0, this.list.removeAll(list2));

        TestUtils.assertListEquals(this.list.toArray(), 1, 1);
    }

    /* */
    @Test
    public void testRemoveAllOccurrences()
    {
        addFromArray(this.list, asArray(0, 1, 2, 1, 0, 3, 0));

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
    public void testRemoveAllFromLookupContainerEverything()
    {
        addFromArray(this.list, asArray(0, 1, 2, 1, 0));

        final KTypeHashSet<KType> list2 = KTypeHashSet.newInstance();
        list2.add(asArray(0, 1, 2));

        Assert.assertEquals(5, this.list.removeAll(list2));
        Assert.assertEquals(0, this.list.size());
        Assert.assertEquals(0, this.list.removeAll(list2));
    }

    /* */
    @Test
    public void testRemoveAllWithPredicate()
    {
        addFromArray(this.list, newArray(this.k0, this.k1, this.k2, this.k1, this.k4));

        Assert.assertEquals(3, this.list.removeAll(new KTypePredicate<KType>()
        {
            @Override
            public boolean apply(final KType v)
            {
                return v == AbstractKTypeIndexedContainerTest.this.key1 || v == AbstractKTypeIndexedContainerTest.this.key2;
            };
        }));

        TestUtils.assertListEquals(this.list.toArray(), 0, 4);
    }

    /* */
    @Test
    public void testRemoveAllWithPredicateEverything()
    {
        addFromArray(this.list, newArray(this.k0, this.k1, this.k2, this.k1, this.k4));

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
        addFromArray(this.list, newArray(this.k0, this.k1, this.k2, this.k1, this.k0));

        Assert.assertEquals(2, this.list.retainAll(new KTypePredicate<KType>()
        {
            @Override
            public boolean apply(final KType v)
            {
                return v == AbstractKTypeIndexedContainerTest.this.key1 || v == AbstractKTypeIndexedContainerTest.this.key2;
            };
        }));

        TestUtils.assertListEquals(this.list.toArray(), 1, 2, 1);
    }

    /* */
    @Test
    public void testRemoveAllWithPredicateInterrupted()
    {
        addFromArray(this.list, newArray(this.k0, this.k1, this.k2, this.k1, this.k4));

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
                    if (v == AbstractKTypeIndexedContainerTest.this.key2) {
                        throw t;
                    }
                    return v == AbstractKTypeIndexedContainerTest.this.key1;
                };
            }));
            Assert.fail();
        } catch (final RuntimeException e)
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
        addFromArray(this.list, asArray(0, 1, 2, 1, 0));

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
        addFromArray(this.list, asArray(0, 1, 2, 7, 4, 3));

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
        addFromArray(this.list, asArray(0, 1, 2, 1, 0));

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
    public void testIterable()
    {
        addFromArray(this.list, asArray(0, 1, 2, 3, 4, 5, 6, 7));
        int count = 0;

        for (final KTypeCursor<KType> cursor : this.list)
        {
            count++;
            TestUtils.assertEquals2(this.list.get(cursor.index), cursor.value);
        }
        Assert.assertEquals(count, this.list.size());

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
        addFromArray(this.list, asArray(0, 1, 2, 3));
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
    public void testForEachWithProcedure()
    {
        addFromArray(this.list, asArray(1, 2, 3));
        final IntHolder holder = new IntHolder();
        this.list.forEach(new KTypeProcedure<KType>() {
            int index = 0;

            @Override
            public void apply(final KType v)
            {
                TestUtils.assertEquals2(v, AbstractKTypeIndexedContainerTest.this.list.get(this.index++));
                holder.value = this.index;
            }
        });
        Assert.assertEquals(holder.value, this.list.size());
    }

    /* */
    @Test
    public void testForEachReturnValueFromAnonymousClass()
    {
        addFromArray(this.list, asArray(1, 2, 3));
        final int result = this.list.forEach(new KTypeProcedure<KType>() {
            int index = 0;

            @Override
            public void apply(final KType v)
            {
                TestUtils.assertEquals2(v, AbstractKTypeIndexedContainerTest.this.list.get(this.index++));
            }
        }).index;
        Assert.assertEquals(result, this.list.size());
    }

    /* */
    @Test
    public void testForEachWithPredicate()
    {
        addFromArray(this.list, asArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));

        final int lastValue = this.list.forEach(new KTypePredicate<KType>() {
            int value = 0;
            int index = 0;

            @Override
            public boolean apply(final KType v)
            {
                TestUtils.assertEquals2(v, AbstractKTypeIndexedContainerTest.this.list.get(this.index));
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
        addFromArray(this.list, asArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));

        final int lastValue = this.list.forEach(new KTypePredicate<KType>() {
            int value = 0;
            int index = 0;

            @Override
            public boolean apply(final KType v)
            {
                TestUtils.assertEquals2(v, AbstractKTypeIndexedContainerTest.this.list.get(this.index));
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
        addFromArray(this.list, asArray(1, 2, 3));
        this.list.clear();
    }

    /* */
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testFrom()
    {
        final KTypeIndexedContainer<KType> variable = getFrom(this.k1, this.k2, this.k3);
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
        final KTypeIndexedContainer<KType> l0 = getFrom();
        Assert.assertEquals(1, l0.hashCode());
        Assert.assertEquals(l0, getFrom());

        final KTypeIndexedContainer<KType> l1 = getFrom(this.k1, this.k2, this.k3);
        final KTypeIndexedContainer<KType> l2 = getFrom(this.k1, this.k2);
        l2.add(this.k3);

        Assert.assertEquals(l1.hashCode(), l2.hashCode());
        Assert.assertEquals(l1, l2);
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    @SuppressWarnings("unchecked")
    public void testHashCodeWithNulls()
    {
        final KTypeIndexedContainer<KType> l1 = getFrom(this.k1, this.keyE, this.k3);
        final KTypeIndexedContainer<KType> l2 = getFrom(this.k1, this.keyE, this.k3);

        Assert.assertEquals(l1.hashCode(), l2.hashCode());
        Assert.assertEquals(l1, l2);
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testToArrayWithClass()
    {
        final KTypeIndexedContainer<KType> l1 = getFrom(this.k1, this.k2, this.k3);
        final Integer[] result = l1.toArray(Integer.class);
        Assert.assertArrayEquals(new Integer[] { 1, 2, 3 }, result); // dummy
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    @Test
    public void testToArray()
    {
        final KTypeIndexedContainer<KType> l1 = getFrom(this.k1, this.k2, this.k3);
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
        addFromArray(this.list, this.k1, this.k2, this.k3);

        final KTypeIndexedContainer<KType> cloned = getClone(this.list);
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
                + this.key3 + "]", getFrom(this.k1, this.k2, this.k3, this.k1, this.k7, this.k4, this.k3).toString());
    }

    @Test
    public void testPooledIteratorForEach()
    {
        // Unbroken for-each loop
        final int TEST_SIZE = 10000;
        final long TEST_ROUNDS = 100;

        final KTypeIndexedContainer<KType> testContainer = createArrayListWithOrderedData(TEST_SIZE);

        final long checksum = testContainer.forEach(new KTypeProcedure<KType>() {

            long count;

            @Override
            public void apply(final KType value)
            {
                this.count += castType(value);
            }
        }).count;

        long testValue = 0;
        final long initialPoolSize = getValuePoolSize(testContainer);

        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            //for-each in test :
            testValue = 0;
            for (final KTypeCursor<KType> cursor : testContainer)
            {
                //we consume 1 iterator for this loop
                Assert.assertEquals(initialPoolSize - 1, getValuePoolSize(testContainer));

                testValue += castType(cursor.value);
            }

            //check checksum the iteration
            Assert.assertEquals(checksum, testValue);

            //iterator is returned to its pool
            Assert.assertEquals(initialPoolSize, getValuePoolSize(testContainer));
        } //end for rounds
    }

    @Test
    public void testPooledIteratorBrokenForEach()
    {
        // for-each loop interrupted

        final int TEST_SIZE = 5000;
        final long TEST_ROUNDS = 100;

        final KTypeIndexedContainer<KType> testContainer = createArrayListWithOrderedData(TEST_SIZE);

        int count = 0;
        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            //for-each in test :
            final long initialPoolSize = getValuePoolSize(testContainer);

            count = 0;
            long guard = 0;
            for (final KTypeCursor<KType> cursor : testContainer)
            {
                guard += castType(cursor.value);
                //we consume 1 iterator for this loop, but reallocs can happen,
                //so we can only say its != initialPoolSize
                Assert.assertTrue(initialPoolSize != getValuePoolSize(testContainer));

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
            Assert.assertTrue(initialPoolSize != getValuePoolSize(testContainer));
        } //end for rounds

        //Due to policy of the Iterator pool, the intended pool never get bigger that some limit
        //despite the Iterator leak.
        Assert.assertTrue(getValuePoolCapacity(testContainer) < IteratorPool.getMaxPoolSize() + 1);
    }

    @Test
    public void testPooledIteratorFullIteratorLoop()
    {
        // for-each loop interrupted

        final int TEST_SIZE = 5000;
        final long TEST_ROUNDS = 100;

        final KTypeIndexedContainer<KType> testContainer = createArrayListWithOrderedData(TEST_SIZE);

        final long checksum = testContainer.forEach(new KTypeProcedure<KType>() {

            long count;

            @Override
            public void apply(final KType value)
            {
                this.count += castType(value);
            }
        }).count;

        long testValue = 0;
        final int startingPoolSize = getValuePoolSize(testContainer);

        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            //Classical iterator loop, with manually allocated Iterator
            final int initialPoolSize = getValuePoolSize(testContainer);

            final AbstractIterator<KTypeCursor<KType>> loopIterator = (AbstractIterator<KTypeCursor<KType>>) testContainer.iterator();

            Assert.assertEquals(initialPoolSize - 1, getValuePoolSize(testContainer));

            testValue = 0;
            while (loopIterator.hasNext())
            {
                testValue += castType(loopIterator.next().value);
            } //end IteratorLoop

            //iterator is returned automatically to its pool, by normal iteration termination
            Assert.assertEquals(initialPoolSize, getValuePoolSize(testContainer));

            //checksum
            Assert.assertEquals(checksum, testValue);
        } //end for rounds

        // pool initial size is untouched anyway
        Assert.assertEquals(startingPoolSize, getValuePoolSize(testContainer));
    }

    @Test
    public void testPooledIteratorBrokenIteratorLoop()
    {
        // for-each loop interrupted

        final int TEST_SIZE = 5000;
        final long TEST_ROUNDS = 100;

        final KTypeIndexedContainer<KType> testContainer = createArrayListWithOrderedData(TEST_SIZE);
        final int startingPoolSize = getValuePoolSize(testContainer);

        int count = 0;
        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            //Classical iterator loop, with manually allocated Iterator
            final long initialPoolSize = getValuePoolSize(testContainer);

            final AbstractIterator<KTypeCursor<KType>> loopIterator = (AbstractIterator<KTypeCursor<KType>>) testContainer.iterator();

            Assert.assertEquals(initialPoolSize - 1, getValuePoolSize(testContainer));

            count = 0;
            long guard = 0;
            while (loopIterator.hasNext())
            {
                guard += castType(loopIterator.next().value);

                //brutally interrupt in the middle
                if (count > TEST_SIZE / 2)
                {
                    break;
                }
                count++;
            } //end IteratorLoop

            //iterator is NOT returned to its pool, due to the break.
            Assert.assertEquals(initialPoolSize - 1, getValuePoolSize(testContainer));

            //manual return to the pool
            loopIterator.release();

            //now the pool is restored
            Assert.assertEquals(initialPoolSize, getValuePoolSize(testContainer));

        } //end for rounds

        // pool initial size is untouched anyway
        Assert.assertEquals(startingPoolSize, getValuePoolSize(testContainer));
    }

    @Test
    public void testPooledIteratorExceptionIteratorLoop()
    {
        final int TEST_SIZE = 5000;
        final long TEST_ROUNDS = 100;

        final KTypeIndexedContainer<KType> testContainer = createArrayListWithOrderedData(TEST_SIZE);

        final long checksum = testContainer.forEach(new KTypeProcedure<KType>() {

            long count;

            @Override
            public void apply(final KType value)
            {
                this.count += castType(value);
            }
        }).count;

        final int startingPoolSize = getValuePoolSize(testContainer);

        int count = 0;
        AbstractIterator<KTypeCursor<KType>> loopIterator = null;

        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            try
            {
                loopIterator = (AbstractIterator<KTypeCursor<KType>>) testContainer.iterator();

                Assert.assertEquals(startingPoolSize - 1, getValuePoolSize(testContainer));

                long guard = 0;
                count = 0;
                while (loopIterator.hasNext())
                {
                    guard += castType(loopIterator.next().value);

                    //brutally interrupt in the middle some of the loops, but not all
                    if (round > TEST_ROUNDS / 2 && count > TEST_SIZE / 2)
                    {
                        throw new Exception("Oups some problem in the loop occured");
                    }
                    count++;
                } //end while

                //iterator is returned to its pool in case of normal loop termination
                Assert.assertEquals(startingPoolSize, getValuePoolSize(testContainer));
                Assert.assertEquals(checksum, guard);

            } catch (final Exception e)
            {
                //iterator is NOT returned to its pool because of the exception
                Assert.assertEquals(startingPoolSize - 1, getValuePoolSize(testContainer));

                //manual return to the pool then
                loopIterator.release();

                //now the pool is restored
                Assert.assertEquals(startingPoolSize, getValuePoolSize(testContainer));
            }
        } //end for rounds

        // pool initial size is untouched anyway
        Assert.assertEquals(startingPoolSize, getValuePoolSize(testContainer));
    }

    @Test
    public void testPooledIteratorExceptionSafe()
    {
        final int TEST_SIZE = 104171;
        final long TEST_ROUNDS = 15;

        final KTypeIndexedContainer<KType> testContainer = createArrayListWithOrderedData(TEST_SIZE);

        final long checksum = testContainer.forEach(new KTypeProcedure<KType>() {

            long count;

            @Override
            public void apply(final KType value)
            {
                this.count += castType(value);
            }
        }).count;

        final int initialPoolSize = getValuePoolSize(testContainer);

        //start with a non full pool, remove 3 elements
        final AbstractIterator<KTypeCursor<KType>> loopIteratorFake = (AbstractIterator<KTypeCursor<KType>>) testContainer.iterator();
        final AbstractIterator<KTypeCursor<KType>> loopIteratorFake2 = (AbstractIterator<KTypeCursor<KType>>) testContainer.iterator();
        final AbstractIterator<KTypeCursor<KType>> loopIteratorFake3 = (AbstractIterator<KTypeCursor<KType>>) testContainer.iterator();

        final int startingTestPoolSize = getValuePoolSize(testContainer);

        Assert.assertEquals(initialPoolSize - 3, startingTestPoolSize);

        int count = 0;
        AbstractIterator<KTypeCursor<KType>> loopIterator = null;

        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            try
            {
                loopIterator = (AbstractIterator<KTypeCursor<KType>>) testContainer.iterator();

                Assert.assertEquals(startingTestPoolSize - 1, getValuePoolSize(testContainer));

                long guard = 0;
                count = 0;

                while (loopIterator.hasNext())
                {
                    guard += castType(loopIterator.next().value);

                    //brutally interrupt in the middle some of the loops, but not all
                    if (round > TEST_ROUNDS / 2 && count > TEST_SIZE / 2)
                    {
                        throw new Exception("Oups some problem in the loop occured");
                    }
                    count++;
                } //end while

                //iterator is returned to its pool in case of normal loop termination
                Assert.assertEquals(startingTestPoolSize, getValuePoolSize(testContainer));
                Assert.assertEquals(checksum, guard);

                //still, try to return it ....
                loopIterator.release();

                //nothing has changed
                Assert.assertEquals(startingTestPoolSize, getValuePoolSize(testContainer));
            } catch (final Exception e)
            {
                //iterator is NOT returned to its pool because of the exception
                Assert.assertEquals(startingTestPoolSize - 1, getValuePoolSize(testContainer));

                //manual return to the pool then
                loopIterator.release();

                //now the pool is restored
                Assert.assertEquals(startingTestPoolSize, getValuePoolSize(testContainer));

                //continue to try to release...
                loopIterator.release();

                //nothing has changed
                Assert.assertEquals(startingTestPoolSize, getValuePoolSize(testContainer));
            }
        } //end for rounds

        // pool initial size is untouched anyway
        Assert.assertEquals(startingTestPoolSize, getValuePoolSize(testContainer));

        //finally return the fake ones, several times
        loopIteratorFake.release();
        loopIteratorFake.release();
        loopIteratorFake.release();
        loopIteratorFake2.release();
        loopIteratorFake2.release();
        loopIteratorFake2.release();
        loopIteratorFake3.release();
        loopIteratorFake3.release();

        Assert.assertEquals(initialPoolSize, getValuePoolSize(testContainer));
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
        final KTypeIndexedContainer<KType> newList = createNewInstance(PREALLOCATED_SIZE);

        //computed real capacity
        final int realCapacity = newList.capacity();

        //3) Add PREALLOCATED_SIZE different values. At the end, size() must be == realCapacity,
        //and internal buffer/allocated must not have changed of size
        final int contructorBufferSize = getBuffer(newList).length;

        Assert.assertEquals(contructorBufferSize, getBuffer(newList).length);

        for (int i = 0; i < 1.5 * realCapacity; i++) {

            newList.add(cast(randomVK.nextInt()));

            //internal size has not changed until realCapacity
            if (newList.size() <= realCapacity) {

                Assert.assertEquals(contructorBufferSize, getBuffer(newList).length);
            }

            if (contructorBufferSize < getBuffer(newList).length) {
                //The container as just reallocated, its actual size must be not too far from the previous capacity:
                Assert.assertTrue("Container as reallocated at size = " + newList.size() + " with previous capacity = " + realCapacity,
                        (newList.size() - realCapacity) <= 2);
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
        final KTypeIndexedContainer<KType> refContainer = createNewInstance(PREALLOCATED_SIZE);

        final int refCapacity = refContainer.capacity();

        //3) Fill with random values, random number of elements below preallocation
        final int nbElements = RandomizedTest.randomInt(PREALLOCATED_SIZE);

        for (int i = 0; i < nbElements; i++) {

            refContainer.add(cast(randomVK.nextInt()));
        }

        final int nbRefElements = refContainer.size();

        //Capacity must have not changed, i.e no reallocation must have occured.
        Assert.assertEquals(refCapacity, refContainer.capacity());

        //4) Duplicate by copy-construction and/or clone
        KTypeIndexedContainer<KType> clonedContainer = getClone(refContainer);
        KTypeIndexedContainer<KType> copiedContainer = getCopyConstructor(refContainer);

        //Duplicated containers must be equal to their origin, with a capacity no bigger than the original.

        final int copiedCapacity = copiedContainer.capacity();

        Assert.assertEquals(nbRefElements, clonedContainer.size());
        Assert.assertEquals(nbRefElements, copiedContainer.size());
        Assert.assertEquals(refCapacity, clonedContainer.capacity()); //clone is supposed to be cloned, so exact match !
        Assert.assertTrue(refCapacity >= copiedCapacity);
        Assert.assertTrue(clonedContainer.equals(refContainer));
        Assert.assertTrue(copiedContainer.equals(refContainer));

        //Maybe we were lucky, iterate duplication over itself several times
        for (int j = 0; j < 10; j++) {

            clonedContainer = getClone(clonedContainer);
            copiedContainer = getCopyConstructor(copiedContainer);

            //when copied over itself, of course every characteristic must be constant, else something is wrong.
            Assert.assertEquals(nbRefElements, clonedContainer.size());
            Assert.assertEquals(nbRefElements, copiedContainer.size());
            Assert.assertEquals(refCapacity, clonedContainer.capacity());
            Assert.assertEquals(copiedCapacity, copiedContainer.capacity());
            Assert.assertTrue(clonedContainer.equals(refContainer));
            Assert.assertTrue(copiedContainer.equals(refContainer));
        }
    }

    protected KTypeIndexedContainer<KType> createArrayListWithOrderedData(final int size)
    {
        final KTypeIndexedContainer<KType> newArray = createNewInstance();

        for (int i = 0; i < size; i++)
        {
            newArray.add(cast(i));
        }

        return newArray;
    }
}
