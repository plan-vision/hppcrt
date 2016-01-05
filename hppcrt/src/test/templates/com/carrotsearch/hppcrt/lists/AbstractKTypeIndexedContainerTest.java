package com.carrotsearch.hppcrt.lists;

import java.util.*;

import org.junit.*;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.cursors.*;
import com.carrotsearch.hppcrt.predicates.*;
import com.carrotsearch.hppcrt.procedures.*;
import com.carrotsearch.hppcrt.sets.AbstractKTypeHashSetTest;
import com.carrotsearch.hppcrt.sets.KTypeHashSet;
import com.carrotsearch.randomizedtesting.RandomizedContext;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.*;

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
/**
 * Tests common for all kinds of {@link KTypeIndexedContainer}.
 */
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

    protected abstract int getValuePoolSize(KTypeIndexedContainer<KType> testList);

    protected abstract int getValuePoolCapacity(KTypeIndexedContainer<KType> testList);

    protected abstract void insertAtHead(KTypeIndexedContainer<KType> testList, KType value);

    /**
     * Per-test fresh initialized instance.
     */
    protected KTypeIndexedContainer<KType> list;

    /**
     * Some sequence values for tests.
     */
    protected ArrayList<Integer> sequence;

    @Before
    public void initialize() {

        this.list = createNewInstance();

        this.sequence = new ArrayList<Integer>();

        for (int i = 0; i < 10000; i++) {
            this.sequence.add(castType(cast(i))); //double-cast to assure downmixing for smaller types
        }
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
            int count = 0;
            //check access by get()
            for (/*! #if ($TemplateOptions.KTypeGeneric) !*/final Object
                    /*! #else
            final KType
            #end !*/
                    val : this.list.toArray()) {

                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                TestUtils.assertEquals2(val, (Object) this.list.get(count));
                /*! #else
                TestUtils.assertEquals2(val, this.list.get(count));
                #end !*/
                count++;
            }

            Assert.assertEquals(count, this.list.size());

            //check beyond validity range
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

        //insert at the end :
        this.list.insert(this.list.size(), this.k7);

        TestUtils.assertListEquals(this.list.toArray(), 2, 4, 1, 3, 7);

        //insert at the last position, shift the lest element to the right :
        this.list.insert(this.list.size() - 1, this.k5);

        TestUtils.assertListEquals(this.list.toArray(), 2, 4, 1, 3, 5, 7);
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

    /**
     * Try lots of combinations of size, range,
     * validity is 0 <= from <= to <= size()
     */
    @Test
    public void testRemoveRange()
    {
        final ArrayList<Integer> referenceDeque = new ArrayList<Integer>();

        final int TEST_SIZE = (int) 55;

        final Random prng = new Random(0xBADCAFE);

        final int NB_ITERATION = 200000;

        for (int ii = 0; ii < NB_ITERATION; ii++) {

            referenceDeque.clear();

            //create deque of this size
            final int dequeSize = prng.nextInt(TEST_SIZE);

            final KTypeIndexedContainer<KType> testQ = createIndexedContainerWithRandomData(dequeSize, 0xBADCAFE);

            //will attempt to remove this range
            final int upperRange = prng.nextInt(dequeSize + 1);

            final int lowerRange = prng.nextInt(upperRange + 1);

            //Fill the reference JCF deque
            for (int jj = 0; jj < testQ.size(); jj++) {

                if (upperRange == lowerRange) {
                    referenceDeque.add(castType(testQ.get(jj))); //no removal at all
                }
                else if (jj < lowerRange || jj >= upperRange) {
                    referenceDeque.add(castType(testQ.get(jj)));
                }
            }

            //Proceed to truncation
            testQ.removeRange(lowerRange, upperRange);

            //Check: testQ is the same as referenceDeque.

            Assert.assertEquals(String.format("Iteration = %d, lower range %d, upper range %d", ii, lowerRange, upperRange),
                    referenceDeque.size(), testQ.size());

            for (int jj = 0; jj < testQ.size(); jj++) {

                if (referenceDeque.get(jj).intValue() != castType(testQ.get(jj))) {

                    Assert.assertEquals(String.format("Iteration = %d, index %d", ii, jj),
                            referenceDeque.get(jj).intValue(), castType(testQ.get(jj)));
                }
            }
        } //end for
    }

    /**
     * forEach(procedure, slice) Try lots of combinations of size, range
     * validity is 0 <= from <= to <= size()
     */
    @Test
    public void testForEachProcedureRange()
    {
        final ArrayList<Integer> referenceList = new ArrayList<Integer>();

        final ArrayList<Integer> procedureResult = new ArrayList<Integer>();

        final int TEST_SIZE = (int) 55;

        final Random prng = new Random(0xBADCAFE);

        final int NB_ITERATION = 200000;

        for (int ii = 0; ii < NB_ITERATION; ii++) {

            referenceList.clear();
            procedureResult.clear();

            long expectedSum = 0;

            //create deque of this size
            final int dequeSize = prng.nextInt(TEST_SIZE);

            final KTypeIndexedContainer<KType> testQ = createIndexedContainerWithRandomData(dequeSize, 0xBADCAFE);

            //will attempt to remove this range
            final int upperRange = prng.nextInt(dequeSize + 1);

            final int lowerRange = prng.nextInt(upperRange + 1);

            //Fill the reference JCF deque
            for (int jj = 0; jj < testQ.size(); jj++) {

                if (lowerRange == upperRange) {
                    expectedSum = 0;
                    break;
                }
                else if (jj >= lowerRange && jj < upperRange) {
                    referenceList.add(castType(testQ.get(jj)));
                    expectedSum += castType(testQ.get(jj));
                }
            }

            //Execute the procedure: this one copies the range into procedureResult,
            //and returns the sum of the values.
            final long computedSum = testQ.forEach(new KTypeProcedure<KType>() {

                long count = 0;

                @Override
                public void apply(final KType value) {

                    procedureResult.add(castType(value));

                    this.count += castType(value);
                }

            }, lowerRange, upperRange).count;

            //Check : the sums are the same
            Assert.assertEquals(expectedSum, computedSum);

            //Check: procedureResult is the same as referenceList.
            Assert.assertEquals(String.format("Iteration = %d, lower range %d, upper range %d", ii, lowerRange, upperRange),
                    referenceList.size(), procedureResult.size());

            for (int jj = 0; jj < procedureResult.size(); jj++) {

                if (referenceList.get(jj).intValue() != procedureResult.get(jj).intValue()) {

                    Assert.assertEquals(String.format("Iteration = %d, index %d", ii, jj),
                            referenceList.get(jj).intValue(), procedureResult.get(jj).intValue());
                }
            }
        } //end for
    }

    /**
     * forEach(predicate, slice) Try lots of combinations of size, range
     * validity is 0 <= from <= to <= size()
     */
    @Seed("6EB81D6F6E68EBF")
    @Test
    public void testForEachPredicateRange()
    {
        final ArrayList<Integer> referenceList = new ArrayList<Integer>();

        final ArrayList<Integer> predicateResult = new ArrayList<Integer>();

        final int TEST_SIZE = (int) 55;

        final Random prng = RandomizedTest.getRandom();

        final int NB_ITERATION = 200000;

        for (int ii = 0; ii < NB_ITERATION; ii++) {

            referenceList.clear();
            predicateResult.clear();

            long expectedSum = 0;

            //create container
            final int containerSize = prng.nextInt(TEST_SIZE);

            final KTypeIndexedContainer<KType> testQ = createIndexedContainerWithRandomData(containerSize, 0xBADCAFE);

            //will attempt to remove this range
            final int upperRange = prng.nextInt(containerSize + 1);

            final int lowerRange = prng.nextInt(upperRange + 1);

            //stop at a random position between [lowerRange, upperRange[
            final int stopRange; //stopRange is included, i.e computations goes from [lowerRange; stopRange] !

            if (upperRange - lowerRange <= 1) {
                stopRange = lowerRange;
            } else {

                stopRange = RandomizedTest.randomIntBetween(lowerRange, upperRange - 1);
            }

            //Fill the reference JCF deque
            for (int jj = 0; jj < testQ.size(); jj++) {

                if (lowerRange == upperRange) {
                    expectedSum = 0;
                    break;
                }
                else if (jj >= lowerRange && jj <= stopRange) { //stopRange is included into the computation
                    referenceList.add(castType(testQ.get(jj)));
                    expectedSum += castType(testQ.get(jj));
                }
            }

            //Execute the predicate: this one copies the [lowerRange, stopRange] elements into procedureResult,
            //and returns the sum of the values.
            final long computedSum = testQ.forEach(new KTypePredicate<KType>() {

                long count = 0;

                int currentRange = lowerRange;

                @Override
                public boolean apply(final KType value) {

                    //execute
                    predicateResult.add(castType(value));

                    this.count += castType(value);

                    if (this.currentRange >= stopRange) { //stopRange computation is included, then iteration stops
                        return false;
                    }

                    this.currentRange++;

                    return true;
                }

            }, lowerRange, upperRange).count;

            //Check : the sums are the same
            Assert.assertEquals("Iteration = " + ii, expectedSum, computedSum);

            //Check: procedureResult is the same as referenceList.
            Assert.assertEquals(String.format("Iteration = %d, lower range %d, upper range %d", ii, lowerRange, upperRange),
                    referenceList.size(), predicateResult.size());

            for (int jj = 0; jj < predicateResult.size(); jj++) {

                if (referenceList.get(jj).intValue() != predicateResult.get(jj).intValue()) {

                    Assert.assertEquals(String.format("Iteration = %d, index %d", ii, jj),
                            referenceList.get(jj).intValue(), predicateResult.get(jj).intValue());
                }
            }
        } //end for
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
        TestUtils.assertListEquals(this.list.toArray(), this.k0, this.k2, this.k1, this.k4);
        Assert.assertEquals(4, this.list.size());
    }

    /* */
    @Test
    public void testIndexOf()
    {
        addFromArray(this.list, asArray(0, 1, 2, 1, 0, 8, 7, 4, 3, 2));

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        this.list.add((KType) null);
        Assert.assertEquals(10, this.list.indexOf(null));
        /*! #end !*/

        Assert.assertEquals(0, this.list.indexOf(this.k0));
        Assert.assertEquals(8, this.list.indexOf(this.k3));
        Assert.assertEquals(-1, this.list.indexOf(this.k9));
        Assert.assertEquals(2, this.list.indexOf(this.k2));
        Assert.assertEquals(5, this.list.indexOf(this.k8));
        Assert.assertEquals(7, this.list.indexOf(this.k4));
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

        addFromArray(this.list, asArray(0, 1, 2, 1, 0, 8, 3, 4, 8, 2));

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        this.list.add((KType) null);
        Assert.assertEquals(10, this.list.lastIndexOf(null));
        /*! #end !*/

        Assert.assertEquals(4, this.list.lastIndexOf(this.k0));
        Assert.assertEquals(6, this.list.lastIndexOf(this.k3));
        Assert.assertEquals(-1, this.list.indexOf(this.k9));
        Assert.assertEquals(9, this.list.lastIndexOf(this.k2));
        Assert.assertEquals(8, this.list.lastIndexOf(this.k8));
    }

    /* */
    @Test
    public void testIterable()
    {
        for (final int val : this.sequence) {

            this.list.add(cast(val));
        }

        int count = 0;

        final Integer[] seqBuffer = this.sequence.toArray(new Integer[this.sequence.size()]);

        for (final KTypeCursor<KType> cursor : this.list)
        {
            TestUtils.assertEquals2((int) (seqBuffer[count]), castType(cursor.value));
            //general case: index in buffer matches index of cursor
            TestUtils.assertEquals2(getBuffer(this.list)[cursor.index], cursor.value);
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

        Assert.assertFalse(l1.equals(null));
        Assert.assertFalse(l2.equals(null));

        final KTypeIndexedContainer<KType> mine = getFrom(this.key1, this.key2, this.key3, this.key4, this.key5);

        final KTypeIndexedContainer<KType> other = getFrom();

        Assert.assertNotEquals(mine, other);
        Assert.assertNotEquals(other, mine);

        other.add(this.key1);
        Assert.assertNotEquals(mine, other);
        Assert.assertNotEquals(other, mine);

        other.add(this.key2);
        other.add(this.key3);
        Assert.assertNotEquals(mine, other);
        Assert.assertNotEquals(other, mine);

        other.add(this.key4);
        other.add(this.key5);

        Assert.assertEquals(mine, other);
        Assert.assertEquals(other, mine);
        Assert.assertEquals(mine.hashCode(), other.hashCode());

        //they are the same
        //modify index 2 original this.key3
        other.set(2, this.key4);
        Assert.assertNotEquals(mine, other);
        Assert.assertNotEquals(other, mine);

        //re-establish
        other.set(2, this.key3);
        Assert.assertEquals(mine, other);
        Assert.assertEquals(other, mine);
        Assert.assertEquals(mine.hashCode(), other.hashCode());

        //modify
        insertAtHead(other, this.k8);
        Assert.assertNotEquals(mine, other);
        Assert.assertNotEquals(other, mine);

        insertAtHead(mine, this.k8);
        Assert.assertEquals(mine, other);
        Assert.assertEquals(other, mine);
        Assert.assertEquals(mine.hashCode(), other.hashCode());
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

    protected KTypeIndexedContainer<KType> createIndexedContainerWithRandomData(final int size, final long randomSeed)
    {
        final Random prng = new Random(randomSeed);

        final KTypeIndexedContainer<KType> newDeque = createNewInstance();

        while (newDeque.size() < size)
        {
            final KType newValueToInsert = cast(prng.nextInt(size));
            final boolean insertInTail = prng.nextInt() % 7 == 0;
            final boolean deleteHead = prng.nextInt() % 17 == 0;

            if (deleteHead && !newDeque.isEmpty())
            {
                newDeque.remove(0);
            }
            else if (insertInTail)
            {
                newDeque.add(newValueToInsert);
            }
            else
            {
                insertAtHead(newDeque, newValueToInsert);
            }
        }

        return newDeque;
    }
}
