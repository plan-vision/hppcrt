package com.carrotsearch.hppcrt.sets;

import java.util.*;

import org.junit.*;

import static com.carrotsearch.hppcrt.TestUtils.*;
import static org.junit.Assert.*;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.lists.*;
import com.carrotsearch.hppcrt.TestUtils;
import com.carrotsearch.hppcrt.cursors.*;
import com.carrotsearch.hppcrt.maps.*;
import com.carrotsearch.hppcrt.mutables.*;
import com.carrotsearch.hppcrt.predicates.*;
import com.carrotsearch.hppcrt.procedures.*;
import com.carrotsearch.hppcrt.mutables.*;
import com.carrotsearch.hppcrt.sets.*;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.*;

/**
 * Unit tests for {@link KTypeOpenIdentityHashSetTest}.
 */
/*! ${TemplateOptions.doNotGenerateKType("BOOLEAN", "BYTE", "CHAR", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE")} !*/
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeOpenIdentityHashSetTest<KType> extends AbstractKTypeTest<KType>
{
    /**
     * Per-test fresh initialized instance.
     */
    public KTypeOpenIdentityHashSet<KType> set;

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
        this.set = KTypeOpenIdentityHashSet.newInstance();

        //The identity set is only valid for Object keys anyway
        Assume.assumeTrue(Object[].class.isInstance(this.set.keys));
    }

    /**
     * Check that the set is consistent, i.e all allocated slots are reachable by get(),
     * and all not-allocated contains nulls if Generic
     * @param set
     */
    @After
    public void checkConsistency()
    {
        if (this.set != null)
        {
            int occupied = 0;

            final int mask = this.set.allocated.length - 1;

            for (int i = 0; i < this.set.keys.length; i++)
            {
                if (!this.set.allocated[i])
                {
                    //if not allocated, generic version if patched to null for GC sake
                    /*! #if ($TemplateOptions.KTypeGeneric) !*/
                    TestUtils.assertEquals2(Intrinsics.defaultKTypeValue(), this.set.keys[i]);
                    /*! #end !*/
                }
                else
                {
                    //try to reach the key by contains()
                    Assert.assertTrue(this.set.contains(this.set.keys[i]));

                    //check slot
                    Assert.assertEquals(i, this.set.lslot());

                    //Retrieve again by lkey(), compare by reference !
                    Assert.assertSame(this.set.keys[i], this.set.lkey());

                    occupied++;
                }
            }

            Assert.assertEquals(occupied, this.set.assigned);
            Assert.assertEquals(occupied, this.set.size());

        }
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
        this.set.add(asArrayObjects(this.k0, this.k1, this.k2, this.k1, this.k0));
        Assert.assertEquals(3, this.set.size());
        TestUtils.assertSortedListEqualsByReference(this.set.toArray(), this.k0, this.k1, this.k2);
    }

    /* */
    @Test
    public void testAddAll()
    {
        final KTypeOpenIdentityHashSet<KType> set2 = new KTypeOpenIdentityHashSet<KType>();
        set2.add(asArrayObjects(this.k1, this.k2));
        this.set.add(asArrayObjects(this.k0, this.k1));

        Assert.assertEquals(1, this.set.addAll(set2));
        Assert.assertEquals(0, this.set.addAll(set2));

        Assert.assertEquals(3, this.set.size());
        TestUtils.assertSortedListEqualsByReference(this.set.toArray(), this.k0, this.k1, this.k2);
    }

    /* */
    @Test
    public void testRemove()
    {
        this.set.add(asArrayObjects(this.k0, this.k1, this.k2, this.k3, this.k4));

        Assert.assertTrue(this.set.remove(this.k2));
        Assert.assertFalse(this.set.remove(this.k2));
        Assert.assertEquals(4, this.set.size());
        TestUtils.assertSortedListEqualsByReference(this.set.toArray(), this.k0, this.k1, this.k3, this.k4);
    }

    /* */
    @Test
    public void testInitialCapacityAndGrowth()
    {
        for (int i = 0; i < 256; i++)
        {
            final KTypeOpenIdentityHashSet<KType> set = new KTypeOpenIdentityHashSet<KType>(i);

            for (int j = 0; j < i; j++)
            {
                set.add(cast(j));
            }

            Assert.assertEquals(i, set.size());
        }
    }

    /* */
    @Test
    public void testFullLoadFactor()
    {
        this.set = new KTypeOpenIdentityHashSet<KType>(1, 1f);

        // Fit in the byte key range.
        final int capacity = 0x80;
        final int max = capacity - 2;
        for (int i = 0; i < max; i++)
        {
            this.set.add(cast(i));
        }

        // Still not expanded.
        Assert.assertEquals(max, this.set.size());
        Assert.assertEquals(capacity, this.set.keys.length);
        // Won't expand (existing key).
        this.set.add(cast(0));
        Assert.assertEquals(capacity, this.set.keys.length);
        // Expanded.
        this.set.add(cast(0xff));
        Assert.assertEquals(2 * capacity, this.set.keys.length);
    }

    /* */
    @Test
    public void testBug_HPPC73_FullCapacityGet()
    {
        this.set = new KTypeOpenIdentityHashSet<KType>(1, 1f);
        final int capacity = 0x80;
        final int max = capacity - 2;
        for (int i = 0; i < max; i++)
        {
            this.set.add(cast(i));
        }

        Assert.assertEquals(max, this.set.size());
        Assert.assertEquals(capacity, this.set.keys.length);

        // Non-existent key.
        this.set.remove(cast(max + 1));
        Assert.assertFalse(this.set.contains(cast(max + 1)));

        // Should not expand because we're replacing an existing element.
        Assert.assertFalse(this.set.add(cast(0)));
        Assert.assertEquals(max, this.set.size());
        Assert.assertEquals(capacity, this.set.keys.length);

        // Remove from a full set.
        this.set.remove(cast(0));
        Assert.assertEquals(max - 1, this.set.size());
        Assert.assertEquals(capacity, this.set.keys.length);
    }

    /* */
    @Test
    public void testRemoveAllFromLookupContainer()
    {
        this.set.add(asArrayObjects(this.k0, this.k1, this.k2, this.k3, this.k4));

        final KTypeOpenIdentityHashSet<KType> list2 = new KTypeOpenIdentityHashSet<KType>();
        list2.add(asArrayObjects(this.k1, this.k3, this.k5));

        Assert.assertEquals(2, this.set.removeAll(list2));
        Assert.assertEquals(3, this.set.size());
        TestUtils.assertSortedListEquals(this.set.toArray(), this.k0, this.k2, this.k4);
    }

    /* */
    @Test
    public void testRemoveAllWithPredicate()
    {
        this.set.add(asArrayObjects(this.k0, this.k1, this.k2));

        Assert.assertEquals(1, this.set.removeAll(new KTypePredicate<KType>()
                {
            @Override
            public boolean apply(final KType v)
            {
                return v == KTypeOpenIdentityHashSetTest.this.key1;
            };
                }));

        TestUtils.assertSortedListEqualsByReference(this.set.toArray(), this.k0, this.key2);
    }

    /* */
    @Test
    public void testRemoveAllWithPredicateInterrupted()
    {
        this.set.add(asArrayObjects(this.k0, this.k1, this.k2, this.k3, this.k4, this.k5, this.k6, this.k7, this.k8));

        final RuntimeException t = new RuntimeException();
        try
        {
            //the assert below should never be triggered because of the exception
            //so give it an invalid value in case the thing terminates  = initial size + 1
            Assert.assertEquals(10, this.set.removeAll(new KTypePredicate<KType>()
                    {
                @Override
                public boolean apply(final KType v)
                {
                    if (v == KTypeOpenIdentityHashSetTest.this.key7) {
                        throw t;
                    }
                    return v == KTypeOpenIdentityHashSetTest.this.key2 || v == KTypeOpenIdentityHashSetTest.this.key9 || v == KTypeOpenIdentityHashSetTest.this.key5;
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

        // And check if the set is in consistent state. We cannot predict the pattern,
        //but we know that since key7 throws an exception, key7 is still present in the set.

        Assert.assertTrue(this.set.contains(this.key7));
        checkConsistency();
    }

    /* */
    @Test
    public void testRetainAllWithPredicate()
    {
        this.set.add(asArrayObjects(this.k0, this.k1, this.k2, this.k3, this.k4, this.k5));

        Assert.assertEquals(4, this.set.retainAll(new KTypePredicate<KType>()
                {
            @Override
            public boolean apply(final KType v)
            {
                return v == KTypeOpenIdentityHashSetTest.this.key1 || v == KTypeOpenIdentityHashSetTest.this.key2;
            };
                }));

        TestUtils.assertSortedListEquals(this.set.toArray(), this.key1, this.key2);
    }

    /* */
    @Test
    public void testClear()
    {
        this.set.add(asArrayObjects(this.k1, this.k2, this.k3));
        this.set.clear();

        Assert.assertEquals(0, this.set.size());
    }

    /* */
    @Test
    public void testIterable()
    {
        this.set.add(asArrayObjects(this.k1, this.k2, this.k2, this.k3, this.k4));
        this.set.remove(this.k2);
        Assert.assertEquals(3, this.set.size());

        int count = 0;
        for (final KTypeCursor<KType> cursor : this.set)
        {
            count++;
            Assert.assertTrue(this.set.contains(cursor.value));

            TestUtils.assertEquals2(cursor.value, this.set.lkey());
        }
        Assert.assertEquals(count, this.set.size());

        this.set.clear();
        Assert.assertFalse(this.set.iterator().hasNext());
    }

    @Test
    public void testLkey()
    {
        this.set.add(this.key1);
        this.set.add(this.key8);
        this.set.add(this.key3);
        this.set.add(this.key9);
        this.set.add(this.key2);
        this.set.add(this.key5);

        Assert.assertTrue(this.set.contains(this.key1));

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        Assert.assertSame(this.key1, this.set.lkey());
        /*! #end !*/

    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testNullKey()
    {
        this.set.add((KType) null);
        Assert.assertEquals(1, this.set.size());
        Assert.assertTrue(this.set.contains(null));
        Assert.assertTrue(this.set.remove(null));
        Assert.assertEquals(0, this.set.size());
        Assert.assertFalse(this.set.contains(null));
    }

    /*! #end !*/

    /* */
    @Test
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    public void testHashCodeEquals()
    {
        final KTypeOpenIdentityHashSet<Integer> l0 = KTypeOpenIdentityHashSet.from();
        Assert.assertEquals(0, l0.hashCode());
        Assert.assertEquals(l0, KTypeOpenIdentityHashSet.newInstance());

        final KTypeOpenIdentityHashSet<KType> l1 = KTypeOpenIdentityHashSet.from(this.k1, this.k2, this.k3);
        final KTypeOpenIdentityHashSet<KType> l2 = KTypeOpenIdentityHashSet.from(this.k1, this.k2);
        l2.add(this.k3);

        Assert.assertEquals(l1.hashCode(), l2.hashCode());
        Assert.assertEquals(l1, l2);
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    @Test
    public void testHashCodeWithNulls()
    {
        final KTypeOpenIdentityHashSet<KType> l1 = KTypeOpenIdentityHashSet.from(this.k1, null, this.k3);
        final KTypeOpenIdentityHashSet<KType> l2 = KTypeOpenIdentityHashSet.from(this.k1, null);
        l2.add(this.k3);

        Assert.assertEquals(l1.hashCode(), l2.hashCode());
        Assert.assertEquals(l1, l2);
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testClone()
    {
        this.set.add(this.key1, this.key2, this.key3);

        final KTypeOpenIdentityHashSet<KType> cloned = this.set.clone();
        cloned.removeAllOccurrences(this.key1);

        TestUtils.assertSortedListEqualsByReference(this.set.toArray(), this.key1, this.key2, this.key3);
        TestUtils.assertSortedListEqualsByReference(cloned.toArray(), this.key2, this.key3);
    }

    /*
     * 
     */
    @Test
    public void testToString()
    {
        Assume.assumeTrue(Object[].class.isInstance(this.set.keys));

        this.set.add(this.key1, this.key2);
        String asString = this.set.toString();
        asString = asString.replaceAll("[\\[\\],\\ ]", "");
        final char[] asCharArray = asString.toCharArray();
        Arrays.sort(asCharArray);
        Assert.assertEquals("12", new String(asCharArray));
    }

    @Test
    public void testPooledIteratorForEach()
    {
        //A) Unbroken for-each loop
        //must accommodate even the smallest primitive type
        //so that the iteration do not break before it should...
        final int TEST_SIZE = 126;
        final long TEST_ROUNDS = 500;

        final KTypeOpenIdentityHashSet<KType> testContainer = createSetWithOrderedData(TEST_SIZE);

        final long checksum = testContainer.forEach(new KTypeProcedure<KType>() {

            long count;

            @Override
            public void apply(final KType value) {

                this.count += castType(value);
            }
        }).count;

        long testValue = 0;
        final long initialPoolSize = testContainer.entryIteratorPool.size();

        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            //for-each in test :
            testValue = 0;
            for (final KTypeCursor<KType> cursor : testContainer)
            {
                //we consume 1 iterator for this loop
                Assert.assertEquals(initialPoolSize - 1, testContainer.entryIteratorPool.size());

                testValue += castType(cursor.value);
            }

            //check checksum the iteration
            Assert.assertEquals(checksum, testValue);

            //iterator is returned to its pool
            Assert.assertEquals(initialPoolSize, testContainer.entryIteratorPool.size());
        } //end for rounds
    }

    @Test
    public void testPooledIteratorBrokenForEach()
    {
        //A) for-each loop interrupted

        //must accommodate even the smallest primitive type
        //so that the iteration do not break before it should...
        final int TEST_SIZE = 126;
        final long TEST_ROUNDS = 5000;

        final KTypeOpenIdentityHashSet<KType> testContainer = createSetWithOrderedData(TEST_SIZE);

        int count = 0;
        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            //for-each in test :
            final long initialPoolSize = testContainer.entryIteratorPool.size();

            count = 0;
            for (final KTypeCursor<KType> cursor : testContainer)
            {
                this.guard += castType(cursor.value);
                //we consume 1 iterator for this loop, but reallocs can happen,
                //so we can only say its != initialPoolSize
                Assert.assertTrue(initialPoolSize != testContainer.entryIteratorPool.size());

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
            Assert.assertTrue(initialPoolSize != testContainer.entryIteratorPool.size());
        } //end for rounds

        //Due to policy of the Iterator pool, the intended pool never get bigger that some limit
        //despite the Iterator leak.
        Assert.assertTrue(testContainer.entryIteratorPool.capacity() < IteratorPool.getMaxPoolSize() + 1);
    }

    @Test
    public void testPooledIteratorFullIteratorLoop()
    {
        //A) for-each loop interrupted

        //must accommodate even the smallest primitive type
        //so that the iteration do not break before it should...
        final int TEST_SIZE = 126;
        final long TEST_ROUNDS = 5000;

        final KTypeOpenIdentityHashSet<KType> testContainer = createSetWithOrderedData(TEST_SIZE);

        final long checksum = testContainer.forEach(new KTypeProcedure<KType>() {

            long count;

            @Override
            public void apply(final KType value)
            {
                this.count += castType(value);
            }
        }).count;

        long testValue = 0;
        final int startingPoolSize = testContainer.entryIteratorPool.size();

        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            //Classical iterator loop, with manually allocated Iterator
            final int initialPoolSize = testContainer.entryIteratorPool.size();

            final KTypeOpenIdentityHashSet<KType>.EntryIterator loopIterator = testContainer.iterator();

            Assert.assertEquals(initialPoolSize - 1, testContainer.entryIteratorPool.size());

            testValue = 0;
            while (loopIterator.hasNext())
            {
                testValue += castType(loopIterator.next().value);
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
    public void testPooledIteratorBrokenIteratorLoop()
    {
        //A) for-each loop interrupted

        //must accommodate even the smallest primitive type
        //so that the iteration do not break before it should...
        final int TEST_SIZE = 126;
        final long TEST_ROUNDS = 5000;

        final KTypeOpenIdentityHashSet<KType> testContainer = createSetWithOrderedData(TEST_SIZE);
        final int startingPoolSize = testContainer.entryIteratorPool.size();

        int count = 0;
        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            //Classical iterator loop, with manually allocated Iterator
            final long initialPoolSize = testContainer.entryIteratorPool.size();

            final KTypeOpenIdentityHashSet<KType>.EntryIterator loopIterator = testContainer.iterator();

            Assert.assertEquals(initialPoolSize - 1, testContainer.entryIteratorPool.size());

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
    public void testPooledIteratorExceptionIteratorLoop()
    {
        //must accommodate even the smallest primitive type
        //so that the iteration do not break before it should...
        final int TEST_SIZE = 126;
        final long TEST_ROUNDS = 5000;

        final KTypeOpenIdentityHashSet<KType> testContainer = createSetWithOrderedData(TEST_SIZE);

        final long checksum = testContainer.forEach(new KTypeProcedure<KType>() {

            long count;

            @Override
            public void apply(final KType value)
            {
                this.count += castType(value);
            }
        }).count;

        final int startingPoolSize = testContainer.entryIteratorPool.size();

        int count = 0;
        KTypeOpenIdentityHashSet<KType>.EntryIterator loopIterator = null;

        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            try
            {
                loopIterator = testContainer.iterator();

                Assert.assertEquals(startingPoolSize - 1, testContainer.entryIteratorPool.size());

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
                Assert.assertEquals(startingPoolSize, testContainer.entryIteratorPool.size());
                Assert.assertEquals(checksum, this.guard);

            }
            catch (final Exception e)
            {
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
             int PREALLOCATED_SIZE = randomVK.nextInt(1500);
            #else
              int PREALLOCATED_SIZE = randomVK.nextInt(126);
            #end !*/

        //2) Preallocate to PREALLOCATED_SIZE :
        final KTypeOpenIdentityHashSet<KType> newSet = KTypeOpenIdentityHashSet.newInstanceWithCapacity(PREALLOCATED_SIZE,
                KTypeOpenIdentityHashSet.DEFAULT_LOAD_FACTOR);

        //3) Add PREALLOCATED_SIZE different values. At the end, size() must be == PREALLOCATED_SIZE,
        //and internal buffer/allocated must not have changed of size
        final int contructorBufferSize = newSet.keys.length;

        Assert.assertEquals(contructorBufferSize, newSet.allocated.length);

        for (int i = 0; i < PREALLOCATED_SIZE; i++) {

            newSet.add(cast(i));

            //internal size has not changed.
            Assert.assertEquals(contructorBufferSize, newSet.keys.length);
            Assert.assertEquals(contructorBufferSize, newSet.allocated.length);
        }

        Assert.assertEquals(PREALLOCATED_SIZE, newSet.size());

    }

    @Test
    public void testForEachProcedureWithException()
    {
        final Random randomVK = new Random(9521455645L);

        //Test that the container do not resize if less that the initial size

        //1) Choose a map to build
        /*! #if ($TemplateOptions.isKType("GENERIC", "int", "long", "float", "double")) !*/
        final int NB_ELEMENTS = 2000;
        /*!
            #elseif ($TemplateOptions.isKType("short", "char"))
             int NB_ELEMENTS = 1000;
            #else
              int NB_ELEMENTS = 126;
            #end !*/

        final KTypeOpenIdentityHashSet<Object> newSet = KTypeOpenIdentityHashSet.newInstance();

        //add a randomized number of key
        for (int i = 0; i < NB_ELEMENTS; i++) {

            final Integer KVpair = randomVK.nextInt((int) (0.7 * NB_ELEMENTS));

            newSet.add(KVpair);
        }

        //List the keys in the reverse-order of the internal buffer, since forEach() is iterating in reverse also:
        final ArrayList<Integer> keyList = new ArrayList<Integer>();

        //Test forEach predicate and stop at each key in turn.
        final ArrayList<Integer> keyListTest = new ArrayList<Integer>();

        for (int k = newSet.allocated.length - 1; k >= 0; k--) {

            if (newSet.allocated[k]) {

                keyList.add((Integer) (newSet.keys[k]));
            }
        }

        final int size = keyList.size();

        for (int i = 0; i < size; i++)
        {
            final int currentPairIndexSizeToIterate = i + 1;

            keyListTest.clear();

            keyList.clear();

            for (int k = newSet.allocated.length - 1; k >= 0; k--) {

                if (newSet.allocated[k]) {

                    keyList.add((Integer) newSet.keys[k]);
                }
            }

            //A) Run forEach(KType)
            try
            {
                newSet.forEach(new KTypeProcedure<Object>() {

                    @Override
                    public void apply(final Object key)
                    {
                        keyListTest.add((Integer) key);

                        //when the stopping key/value pair is encountered, add to list and stop iteration
                        if (key == keyList.get(currentPairIndexSizeToIterate - 1))
                        {
                            //interrupt iteration by an exception
                            throw new RuntimeException("Interrupted treatment by test");
                        }
                    }
                });
            }
            catch (final RuntimeException e)
            {
                if (!e.getMessage().equals("Interrupted treatment by test"))
                {
                    throw e;
                }
            }
            finally
            {
                //despite the exception, the procedure terminates cleanly

                //check that keyList/keyListTest are identical for the first
                //currentPairIndexToIterate + 1 elements
                Assert.assertEquals(currentPairIndexSizeToIterate, keyListTest.size());

                for (int j = 0; j < currentPairIndexSizeToIterate; j++)
                {
                    //Compare by reference !
                    Assert.assertTrue(keyList.get(j) == keyListTest.get(j));
                }
            } //end finally
        } //end for each index
    }

    @Test
    public void testForEachProcedure()
    {
        final Random randomVK = new Random(9521455645L);

        //Test that the container do not resize if less that the initial size

        //1) Choose a map to build
        /*! #if ($TemplateOptions.isKType("GENERIC", "int", "long", "float", "double")) !*/
        final int NB_ELEMENTS = 2000;
        /*!
            #elseif ($TemplateOptions.isKType("short", "char"))
             int NB_ELEMENTS = 1000;
            #else
              int NB_ELEMENTS = 126;
            #end !*/

        final KTypeOpenIdentityHashSet<Object> newSet = KTypeOpenIdentityHashSet.newInstance();

        //add a randomized number of key
        //use the same value for keys and values to ease later analysis
        for (int i = 0; i < NB_ELEMENTS; i++) {

            final Integer KVpair = randomVK.nextInt((int) (0.7 * NB_ELEMENTS));

            newSet.add(KVpair);
        }

        //List the keys in the reverse-order of the internal buffer, since forEach() is iterating in reverse also:
        final ArrayList<Integer> keyList = new ArrayList<Integer>();

        for (int i = newSet.allocated.length - 1; i >= 0; i--) {

            if (newSet.allocated[i]) {

                keyList.add((Integer) newSet.keys[i]);
            }
        }

        //Test forEach predicate and stop at each key in turn.
        final ArrayList<Integer> keyListTest = new ArrayList<Integer>();

        keyListTest.clear();

        //A) Run forEach(KType)

        newSet.forEach(new KTypeProcedure<Object>() {

            @Override
            public void apply(final Object key)
            {
                keyListTest.add((Integer) key);
            }
        });

        //check that keyList/keyListTest  are identical.
        Assert.assertEquals(keyList.size(), keyListTest.size());

        for (int j = 0; j < keyList.size(); j++)
        {
            //Compare by reference !
            Assert.assertTrue(keyList.get(j) == keyListTest.get(j));
        }
    }

    @Test
    public void testForEachPredicate()
    {
        final Random randomVK = new Random(9521455645L);

        //Test that the container do not resize if less that the initial size

        //1) Choose a map to build
        /*! #if ($TemplateOptions.isKType("GENERIC", "int", "long", "float", "double")) !*/
        final int NB_ELEMENTS = 2000;
        /*!
            #elseif ($TemplateOptions.isKType("short", "char"))
             int NB_ELEMENTS = 1000;
            #else
              int NB_ELEMENTS = 126;
            #end !*/

        final KTypeOpenIdentityHashSet<Object> newSet = KTypeOpenIdentityHashSet.newInstance();

        //add a randomized number of key
        //use the same value for keys and values to ease later analysis
        for (int i = 0; i < NB_ELEMENTS; i++) {

            final Integer KVpair = randomVK.nextInt((int) (0.7 * NB_ELEMENTS));

            newSet.add(KVpair);
        }

        //List the keys in the reverse-order of the internal buffer, since forEach() is iterating in reverse also:
        final ArrayList<Integer> keyList = new ArrayList<Integer>();

        //Test forEach predicate and stop at each key in turn.
        final ArrayList<Integer> keyListTest = new ArrayList<Integer>();

        for (int k = newSet.allocated.length - 1; k >= 0; k--) {

            if (newSet.allocated[k]) {

                keyList.add((Integer) newSet.keys[k]);
            }
        }

        final int size = keyList.size();

        for (int i = 0; i < size; i++)
        {
            final int currentPairIndexSizeToIterate = i + 1;

            keyListTest.clear();
            keyList.clear();

            for (int k = newSet.allocated.length - 1; k >= 0; k--) {

                if (newSet.allocated[k]) {

                    keyList.add((Integer) newSet.keys[k]);
                }
            }

            //A) Run forEach(KType)

            newSet.forEach(new KTypePredicate<Object>() {

                @Override
                public boolean apply(final Object key)
                {
                    keyListTest.add((Integer) key);

                    //when the stopping key/value pair is encountered, add to list and stop iteration
                    if (key == keyList.get(currentPairIndexSizeToIterate - 1))
                    {
                        //interrupt iteration by an exception
                        return false;
                    }

                    return true;
                }
            });

            //despite the interruption, the procedure terminates cleanly

            //check that keyList/keyListTest are identical for the first
            //currentPairIndexToIterate + 1 elements
            Assert.assertEquals(currentPairIndexSizeToIterate, keyListTest.size());

            for (int j = 0; j < currentPairIndexSizeToIterate; j++)
            {
                //Compare by reference !
                Assert.assertTrue(keyList.get(j) == keyListTest.get(j));
            }
        } //end for each index
    }

    @Test
    public void testNotEqualsButIdentical()
    {
        //the goal of this test is to demonstrate that keys are really treated with "identity",
        //not using the equals() / hashCode().
        //So attempt to fill the container with lots of "equals" instances, which are by definition different objects.

        /*! #if ($TemplateOptions.isVType("GENERIC", "int", "long", "float", "double")) !*/
        final int NB_ELEMENTS = 2000;
        /*!
            #elseif ($TemplateOptions.isVType("short", "char"))
             int NB_ELEMENTS = 1000;
            #else
              int NB_ELEMENTS = 126;
            #end !*/

        final KTypeOpenIdentityHashSet<Object> newSet = KTypeOpenIdentityHashSet.newInstance();

        Assert.assertEquals(0, newSet.size());

        //A) fill
        for (int i = 0; i < NB_ELEMENTS; i++) {

            final Object newObject = new IntHolder(0xAF);

            Assert.assertTrue(newSet.add(newObject));

            //Equals key, but not the same object
            Assert.assertFalse(newSet.contains(new IntHolder(0xAF)));

            //Really the same object
            Assert.assertTrue(newSet.contains(newObject));

            //lkey() really return the previously stored object
            Assert.assertSame(newObject, newSet.lkey());
        } //end for

        //objects are all different, so size is really NB_ELEMENTS
        Assert.assertEquals(NB_ELEMENTS, newSet.size());
    }

    private KTypeOpenIdentityHashSet<KType> createSetWithOrderedData(final int size)
    {
        final KTypeOpenIdentityHashSet<KType> newSet = KTypeOpenIdentityHashSet.newInstanceWithCapacity(KTypeOpenIdentityHashSet.DEFAULT_CAPACITY,
                KTypeOpenIdentityHashSet.DEFAULT_LOAD_FACTOR);

        for (int i = 0; i < size; i++) {

            newSet.add(cast(i));
        }

        return newSet;
    }

}
