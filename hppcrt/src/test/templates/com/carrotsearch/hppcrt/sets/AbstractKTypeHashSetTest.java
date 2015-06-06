package com.carrotsearch.hppcrt.sets;

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
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.*;

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
/**
 * Tests common for all kinds of hash sets {@link KTypeSet}.
 */
// ${TemplateOptions.doNotGenerateKType("BOOLEAN")}
/*! ${TemplateOptions.generatedAnnotation} !*/
public abstract class AbstractKTypeHashSetTest<KType> extends AbstractKTypeTest<KType>
{
    @BeforeClass
    public static void configure()
    {
        IteratorPool.configureInitialPoolSize(8);
    }

    private static final int STRIDE = 13;

    protected final KTypeHashingStrategy<KType> TEST_STRATEGY = new KTypeHashingStrategy<KType>() {

        @Override
        public int computeHashCode(final KType object) {

            return BitMixer.mix(cast(castType(object) + AbstractKTypeHashSetTest.STRIDE));
        }

        @Override
        public boolean equals(final KType o1, final KType o2) {

            return Intrinsics.<KType> equals(cast(castType(o1) + AbstractKTypeHashSetTest.STRIDE), cast(castType(o2)
                    + AbstractKTypeHashSetTest.STRIDE));
        }

    };

    /**
     * Customize this for concrete hash set creation
     * @param initialCapacity
     * @param loadFactor
     * @param strategy
     * @return
     */
    protected abstract KTypeSet<KType> createNewSetInstance(final int initialCapacity,
            final double loadFactor, KTypeHashingStrategy<KType> strategy);

    protected abstract KType[] getKeys(KTypeSet<KType> testSet);

    protected abstract boolean isAllocatedDefaultKey(KTypeSet<KType> testSet);

    protected abstract KTypeSet<KType> getClone(KTypeSet<KType> testSet);

    protected abstract KTypeSet<KType> getFrom(KTypeContainer<KType> container);

    protected abstract KTypeSet<KType> getFrom(final KType... elements);

    protected abstract KTypeSet<KType> getFromArray(KType[] keys);

    protected abstract void addFromArray(KTypeSet<KType> testSet, KType... keys);

    protected abstract KTypeSet<KType> getCopyConstructor(KTypeSet<KType> testSet);

    abstract int getEntryPoolSize(KTypeSet<KType> testSet);

    abstract int getEntryPoolCapacity(KTypeSet<KType> testSet);

    /**
     * Per-test fresh initialized instance.
     */
    protected KTypeSet<KType> set;

    @Before
    public void initialize() {

        this.set = createNewSetInstance();
    }

    protected KTypeSet<KType> createNewSetInstance() {

        //use the Max load factor to assure to trigger all the code paths
        return createNewSetInstance(0, HashContainers.MAX_LOAD_FACTOR, this.TEST_STRATEGY);
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

            final int mask = getKeys(this.set).length - 1;

            for (int i = 0; i < getKeys(this.set).length; i++)
            {
                if (!is_allocated(i, Intrinsics.<KType[]> cast(getKeys(this.set))))
                {
                    //if not allocated, generic version if patched to null for GC sake
                    /*! #if ($TemplateOptions.KTypeGeneric) !*/
                    TestUtils.assertEquals2(this.keyE, getKeys(this.set)[i]);
                    /*! #end !*/
                }
                else
                {
                    //try to reach the key by contains()
                    Assert.assertTrue(this.set.contains(Intrinsics.<KType> cast(getKeys(this.set)[i])));

                    occupied++;
                }
            }

            if (isAllocatedDefaultKey(this.set)) {

                //try to reach the key by contains()
                Assert.assertTrue(this.set.contains(this.keyE));

                occupied++;
            }

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

        Assert.assertTrue(this.set.add(this.keyE));
        Assert.assertFalse(this.set.add(this.keyE));

        Assert.assertEquals(2, this.set.size());

        Assert.assertTrue(this.set.add(this.key2));
        Assert.assertFalse(this.set.add(this.key2));

        Assert.assertEquals(3, this.set.size());
    }

    /* */
    @Test
    public void testAddAll()
    {
        final KTypeSet<KType> set2 = createNewSetInstance();
        addFromArray(set2, asArray(1, 2));
        addFromArray(this.set, asArray(0, 1));

        Assert.assertEquals(1, this.set.addAll(set2));
        Assert.assertEquals(0, this.set.addAll(set2));

        Assert.assertEquals(3, this.set.size());
        TestUtils.assertSortedListEquals(this.set.toArray(), 0, 1, 2);
    }

    /* */
    @Test
    public void testRemove()
    {
        addFromArray(this.set, asArray(0, 1, 2, 3, 4));

        Assert.assertTrue(this.set.remove(this.k2));
        Assert.assertFalse(this.set.remove(this.k2));
        Assert.assertEquals(4, this.set.size());
        TestUtils.assertSortedListEquals(this.set.toArray(), 0, 1, 3, 4);
    }

    /* */
    @Test
    public void testInitialCapacityAndGrowth()
    {
        for (int i = 0; i < 256; i++)
        {
            final KTypeSet<KType> set = createNewSetInstance(i, HashContainers.MAX_LOAD_FACTOR, this.TEST_STRATEGY);

            for (int j = 0; j < i; j++)
            {
                set.add(cast(j));
            }

            Assert.assertEquals(i, set.size());
        }
    }

    /* */
    @Test
    public void testRemoveAllFromLookupContainer()
    {
        addFromArray(this.set, asArray(0, 1, 2, 3, 4));

        //test against concrete HashSet
        final KTypeHashSet<KType> set2 = new KTypeHashSet<KType>();
        set2.add(asArray(1, 3, 5));

        Assert.assertEquals(2, this.set.removeAll(set2));

        Assert.assertEquals(3, this.set.size());
        TestUtils.assertSortedListEquals(this.set.toArray(), 0, 2, 4);
    }

    /* */
    @Test
    public void testRemoveAllWithPredicate()
    {
        addFromArray(this.set, newArray(this.k0, this.k1, this.k2));

        Assert.assertEquals(1, this.set.removeAll(new KTypePredicate<KType>()
        {
            @Override
            public boolean apply(final KType v)
            {
                return v == AbstractKTypeHashSetTest.this.k1;
            };
        }));

        TestUtils.assertSortedListEquals(this.set.toArray(), this.k0, this.k2);
    }

    /* */
    @Test
    public void testRemoveAllWithPredicate2()
    {
        addFromArray(this.set, this.keyE, this.key1, this.key2, this.key4);

        Assert.assertEquals(2, this.set.removeAll(new KTypePredicate<KType>()
        {
            @Override
            public boolean apply(final KType v)
            {
                return (v == AbstractKTypeHashSetTest.this.key1) || (v == AbstractKTypeHashSetTest.this.keyE);
            };
        }));

        TestUtils.assertSortedListEquals(this.set.toArray(), this.key2, this.key4);
    }

    /* */
    @Test
    public void testRemoveAllWithPredicateInterrupted()
    {
        addFromArray(this.set, newArray(this.k0, this.k1, this.k2, this.k3, this.k4, this.k5, this.k6, this.k7, this.k8));

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
                    if (v == AbstractKTypeHashSetTest.this.key7) {
                        throw t;
                    }
                    return v == AbstractKTypeHashSetTest.this.key2 || v == AbstractKTypeHashSetTest.this.key9 || v == AbstractKTypeHashSetTest.this.key5;
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

        // And check if the set is in consistent state. We cannot predict the pattern,
        //but we know that since key7 throws an exception, key7 is still present in the set.

        Assert.assertTrue(this.set.contains(this.key7));
        checkConsistency();
    }

    /* */
    @Test
    public void testRetainAllWithPredicate()
    {
        addFromArray(this.set, newArray(this.k0, this.k1, this.k2, this.k3, this.k4, this.k5));

        Assert.assertEquals(4, this.set.retainAll(new KTypePredicate<KType>()
        {
            @Override
            public boolean apply(final KType v)
            {
                return v == AbstractKTypeHashSetTest.this.key1 || v == AbstractKTypeHashSetTest.this.key2;
            };
        }));

        TestUtils.assertSortedListEquals(this.set.toArray(), this.key1, this.key2);
    }

    /* */
    @Test
    public void testRetainAllWithPredicate2()
    {
        addFromArray(this.set, newArray(this.keyE, this.k1, this.k2, this.k3, this.k4, this.k5));

        Assert.assertEquals(4, this.set.retainAll(new KTypePredicate<KType>()
        {
            @Override
            public boolean apply(final KType v)
            {
                return v == AbstractKTypeHashSetTest.this.keyE || v == AbstractKTypeHashSetTest.this.k3;
            };
        }));

        TestUtils.assertSortedListEquals(this.set.toArray(), this.keyE, this.k3);
    }

    /* */
    @Test
    public void testClear()
    {
        addFromArray(this.set, asArray(1, 2, 3));
        this.set.clear();
        checkConsistency();
        Assert.assertEquals(0, this.set.size());

        addFromArray(this.set, asArray(0, 2, 8));
        this.set.clear();
        checkConsistency();
        Assert.assertEquals(0, this.set.size());
    }

    /* */
    @Test
    public void testIterable()
    {
        addFromArray(this.set, asArray(1, 2, 2, 3, 4));
        this.set.remove(this.k2);
        Assert.assertEquals(3, this.set.size());

        int counted = 0;
        for (final KTypeCursor<KType> cursor : this.set)
        {
            if (cursor.index == getKeys(this.set).length) {

                TestUtils.assertEquals2(this.keyE, cursor.value);
                counted++;
                continue;
            }

            counted++;
            Assert.assertTrue(this.set.contains(cursor.value));

        }
        Assert.assertEquals(counted, this.set.size());

        this.set.clear();
        Assert.assertFalse(this.set.iterator().hasNext());
    }

    /* */
    @Test
    public void testIterable2()
    {
        addFromArray(this.set, this.keyE, this.key1, this.key2, this.key2, this.key3, this.key4);
        this.set.remove(this.k2);
        Assert.assertEquals(4, this.set.size());

        int counted = 0;
        for (final KTypeCursor<KType> cursor : this.set)
        {

            if (cursor.index == getKeys(this.set).length) {

                TestUtils.assertEquals2(this.keyE, cursor.value);
                counted++;
                continue;
            }

            counted++;
            Assert.assertTrue(this.set.contains(cursor.value));

        }
        Assert.assertEquals(counted, this.set.size());

        this.set.clear();
        Assert.assertFalse(this.set.iterator().hasNext());
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

    /**
     * Run some random insertions/ deletions and compare the results
     * against <code>java.util.HashSet</code>.
     */
    @Test
    public void testAgainstHashMap()
    {
        final java.util.Random rnd = new Random(0xBADCAFE);
        final java.util.HashSet<KType> other = new java.util.HashSet<KType>();

        for (int size = 1000; size < 20000; size += 4000)
        {
            other.clear();
            this.set.clear();

            for (int round = 0; round < size * 20; round++)
            {
                final KType key = cast(rnd.nextInt(size));

                if (rnd.nextBoolean())
                {
                    other.add(key);
                    this.set.add(key);

                    Assert.assertTrue(this.set.contains(key));
                }
                else
                {
                    Assert.assertTrue("size= " + size + ", round = " + round,
                            other.remove(key) == this.set.remove(key));
                }

                Assert.assertEquals(other.size(), this.set.size());
            }
        }
    }

    /*! #end !*/

    /* */
    @Test
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    public void testHashCodeEquals()
    {
        final KTypeSet<KType> l0 = getFrom();
        Assert.assertEquals(0, l0.hashCode());
        Assert.assertEquals(l0, createNewSetInstance());

        final KTypeSet<KType> l1 = getFrom(this.k1, this.k2, this.k3);
        final KTypeSet<KType> l2 = getFrom(this.k1, this.k2);
        l2.add(this.k3);

        Assert.assertEquals(l1.hashCode(), l2.hashCode());
        Assert.assertEquals(l1, l2);

        l1.add(this.keyE);
        l2.add(this.keyE);

        Assert.assertEquals(l1.hashCode(), l2.hashCode());
        Assert.assertEquals(l1, l2);

    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    @Test
    public void testHashCodeWithNulls()
    {
        final KTypeSet<KType> l1 = getFrom(this.k1, null, this.k3);
        final KTypeSet<KType> l2 = getFrom(this.k1, null);
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
        addFromArray(this.set, this.key1, this.key2, this.key3, this.keyE);

        final KTypeSet<KType> cloned = getClone(this.set);
        cloned.removeAll(this.key1);

        TestUtils.assertSortedListEquals(this.set.toArray(), this.keyE, this.key1, this.key2, this.key3);
        TestUtils.assertSortedListEquals(cloned.toArray(), this.keyE, this.key2, this.key3);
    }

    /*! #if ($TemplateOptions.isKType("int", "short", "byte", "long", "Object")) !*/
    @Test
    public void testToString()
    {
        Assume.assumeTrue(
                int[].class.isInstance(getKeys(this.set)) ||
                        short[].class.isInstance(getKeys(this.set)) ||
                        byte[].class.isInstance(getKeys(this.set)) ||
                        long[].class.isInstance(getKeys(this.set)) ||
                        Object[].class.isInstance(getKeys(this.set)));

        addFromArray(this.set, this.key1, this.key2);
        String asString = this.set.toString();
        asString = asString.replaceAll("[\\[\\],\\ ]", "");
        final char[] asCharArray = asString.toCharArray();
        Arrays.sort(asCharArray);
        Assert.assertEquals("12", new String(asCharArray));
    }

    /*! #end !*/

    @Test
    public void testPooledIteratorForEach()
    {
        //A) Unbroken for-each loop
        //must accommodate even the smallest primitive type
        //so that the iteration do not break before it should...
        final int TEST_SIZE = 126;
        final long TEST_ROUNDS = 5000;

        final KTypeSet<KType> testContainer = createSetWithOrderedData(TEST_SIZE);

        final long checksum = testContainer.forEach(new KTypeProcedure<KType>() {

            long count;

            @Override
            public void apply(final KType value) {

                this.count += castType(value);
            }
        }).count;

        long testValue = 0;
        final long initialPoolSize = getEntryPoolSize(testContainer);

        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            //for-each in test :
            testValue = 0;
            for (final KTypeCursor<KType> cursor : testContainer)
            {
                //we consume 1 iterator for this loop
                Assert.assertEquals(initialPoolSize - 1, getEntryPoolSize(testContainer));

                testValue += castType(cursor.value);
            }

            //check checksum the iteration
            Assert.assertEquals(checksum, testValue);

            //iterator is returned to its pool
            Assert.assertEquals(initialPoolSize, getEntryPoolSize(testContainer));
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

        final KTypeSet<KType> testContainer = createSetWithOrderedData(TEST_SIZE);

        int count = 0;
        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            //for-each in test :
            final long initialPoolSize = getEntryPoolSize(testContainer);

            count = 0;
            int guard = 0;
            for (final KTypeCursor<KType> cursor : testContainer)
            {
                guard += castType(cursor.value);
                //we consume 1 iterator for this loop, but reallocs can happen,
                //so we can only say its != initialPoolSize
                Assert.assertTrue(initialPoolSize != getEntryPoolSize(testContainer));

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
            Assert.assertTrue(initialPoolSize != getEntryPoolSize(testContainer));
        } //end for rounds

        //Due to policy of the Iterator pool, the intended pool never get bigger that some limit
        //despite the Iterator leak.
        Assert.assertTrue(getEntryPoolCapacity(testContainer) < IteratorPool.getMaxPoolSize() + 1);
    }

    @Test
    public void testPooledIteratorFullIteratorLoop()
    {
        //A) for-each loop interrupted

        //must accommodate even the smallest primitive type
        //so that the iteration do not break before it should...
        final int TEST_SIZE = 126;
        final long TEST_ROUNDS = 5000;

        final KTypeSet<KType> testContainer = createSetWithOrderedData(TEST_SIZE);

        final long checksum = testContainer.forEach(new KTypeProcedure<KType>() {

            long count;

            @Override
            public void apply(final KType value)
            {
                this.count += castType(value);
            }
        }).count;

        long testValue = 0;
        final int startingPoolSize = getEntryPoolSize(testContainer);

        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            //Classical iterator loop, with manually allocated Iterator
            final int initialPoolSize = getEntryPoolSize(testContainer);

            final AbstractIterator<KTypeCursor<KType>> loopIterator = (AbstractIterator<KTypeCursor<KType>>) testContainer.iterator();

            Assert.assertEquals(initialPoolSize - 1, getEntryPoolSize(testContainer));

            testValue = 0;
            while (loopIterator.hasNext())
            {
                testValue += castType(loopIterator.next().value);
            } //end IteratorLoop

            //iterator is returned automatically to its pool, by normal iteration termination
            Assert.assertEquals(initialPoolSize, getEntryPoolSize(testContainer));

            //checksum
            Assert.assertEquals(checksum, testValue);
        } //end for rounds

        // pool initial size is untouched anyway
        Assert.assertEquals(startingPoolSize, getEntryPoolSize(testContainer));
    }

    @Test
    public void testPooledIteratorBrokenIteratorLoop()
    {
        //A) for-each loop interrupted

        //must accommodate even the smallest primitive type
        //so that the iteration do not break before it should...
        final int TEST_SIZE = 126;
        final long TEST_ROUNDS = 5000;

        final KTypeSet<KType> testContainer = createSetWithOrderedData(TEST_SIZE);
        final int startingPoolSize = getEntryPoolSize(testContainer);

        int count = 0;
        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            //Classical iterator loop, with manually allocated Iterator
            final long initialPoolSize = getEntryPoolSize(testContainer);

            final AbstractIterator<KTypeCursor<KType>> loopIterator = (AbstractIterator<KTypeCursor<KType>>) testContainer.iterator();

            Assert.assertEquals(initialPoolSize - 1, getEntryPoolSize(testContainer));

            count = 0;
            int guard = 0;
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
            Assert.assertEquals(initialPoolSize - 1, getEntryPoolSize(testContainer));

            //manual return to the pool
            loopIterator.release();

            //now the pool is restored
            Assert.assertEquals(initialPoolSize, getEntryPoolSize(testContainer));

        } //end for rounds

        // pool initial size is untouched anyway
        Assert.assertEquals(startingPoolSize, getEntryPoolSize(testContainer));
    }

    @Test
    public void testPooledIteratorExceptionIteratorLoop()
    {
        //must accommodate even the smallest primitive type
        //so that the iteration do not break before it should...
        final int TEST_SIZE = 126;
        final long TEST_ROUNDS = 5000;

        final KTypeSet<KType> testContainer = createSetWithOrderedData(TEST_SIZE);

        final long checksum = testContainer.forEach(new KTypeProcedure<KType>() {

            long count;

            @Override
            public void apply(final KType value)
            {
                this.count += castType(value);
            }
        }).count;

        final int startingPoolSize = getEntryPoolSize(testContainer);

        int count = 0;
        AbstractIterator<KTypeCursor<KType>> loopIterator = null;

        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            try
            {
                loopIterator = (AbstractIterator<KTypeCursor<KType>>) testContainer.iterator();

                Assert.assertEquals(startingPoolSize - 1, getEntryPoolSize(testContainer));

                int guard = 0;
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
                Assert.assertEquals(startingPoolSize, getEntryPoolSize(testContainer));
                Assert.assertEquals(checksum, guard);

            } catch (final Exception e)
            {
                //iterator is NOT returned to its pool because of the exception
                Assert.assertEquals(startingPoolSize - 1, getEntryPoolSize(testContainer));

                //manual return to the pool then
                loopIterator.release();

                //now the pool is restored
                Assert.assertEquals(startingPoolSize, getEntryPoolSize(testContainer));
            }
        } //end for rounds

        // pool initial size is untouched anyway
        Assert.assertEquals(startingPoolSize, getEntryPoolSize(testContainer));
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

        final KTypeSet<KType> newSet = createNewSetInstance(PREALLOCATED_SIZE,
                HashContainers.DEFAULT_LOAD_FACTOR, this.TEST_STRATEGY);

        //computed real capacity
        final int realCapacity = newSet.capacity();

        //3) Add PREALLOCATED_SIZE different values. At the end, size() must be == realCapacity,
        //and internal buffer/allocated must not have changed of size
        final int contructorBufferSize = getKeys(newSet).length;

        Assert.assertEquals(contructorBufferSize, getKeys(newSet).length);

        for (int i = 0; i < 1.5 * realCapacity; i++) {

            newSet.add(cast(i));

            //internal size has not changed until realCapacity
            if (newSet.size() <= realCapacity) {

                Assert.assertEquals(contructorBufferSize, getKeys(newSet).length);
            }

            if (contructorBufferSize < getKeys(newSet).length) {
                //The container as just reallocated, its actual size must be not too far from the previous capacity:
                Assert.assertTrue("Container as reallocated at size = " + newSet.size() + " with previous capacity = " + realCapacity,
                        (newSet.size() - realCapacity) <= 3);
                break;
            }
        }
    }

    @Test
    public void testForEachProcedureWithException()
    {

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

        final KTypeSet<KType> newSet = createNewSetInstance();

        newSet.add(this.keyE);

        //add a increasing number of key
        for (int i = 0; i < NB_ELEMENTS; i++) {

            final int KVpair = i;

            newSet.add(cast(KVpair));
        }

        //List the keys in the reverse-order of the internal buffer, since forEach() is iterating in reverse also:
        final KTypeArrayList<KType> keyList = new KTypeArrayList<KType>();

        keyList.add(this.keyE);

        //Test forEach predicate and stop at each key in turn.
        final KTypeArrayList<KType> keyListTest = new KTypeArrayList<KType>();

        for (int k = getKeys(newSet).length - 1; k >= 0; k--) {

            if (is_allocated(k, Intrinsics.<KType[]> cast(getKeys(newSet)))) {

                keyList.add(Intrinsics.<KType> cast(getKeys(newSet)[k]));
            }
        }

        final int size = keyList.size();

        for (int i = 0; i < size; i++)
        {
            final int currentPairIndexSizeToIterate = i + 1;

            keyListTest.clear();

            keyList.clear();

            keyList.add(this.keyE);

            for (int k = getKeys(newSet).length - 1; k >= 0; k--) {

                if (is_allocated(k, Intrinsics.<KType[]> cast(getKeys(newSet)))) {

                    keyList.add(Intrinsics.<KType> cast(getKeys(newSet)[k]));
                }
            }

            //A) Run forEach(KType)
            try
            {
                newSet.forEach(new KTypeProcedure<KType>() {

                    @Override
                    public void apply(final KType key)
                    {
                        keyListTest.add(key);

                        //when the stopping key/value pair is encountered, add to list and stop iteration
                        if (key == keyList.get(currentPairIndexSizeToIterate - 1))
                        {
                            //interrupt iteration by an exception
                            throw new RuntimeException("Interrupted treatment by test");
                        }
                    }
                });
            } catch (final RuntimeException e)
            {
                if (!e.getMessage().equals("Interrupted treatment by test"))
                {
                    throw e;
                }
            } finally
            {
                //despite the exception, the procedure terminates cleanly

                //check that keyList/keyListTest and valueList/valueListTest are identical for the first
                //currentPairIndexToIterate + 1 elements
                Assert.assertEquals("i = " + i, currentPairIndexSizeToIterate, keyListTest.size());

                for (int j = 0; j < currentPairIndexSizeToIterate; j++)
                {
                    TestUtils.assertEquals2("j = " + j, keyList.get(j), keyListTest.get(j));
                }
            } //end finally
        } //end for each index
    }

    @Test
    public void testForEachProcedure()
    {
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

        final KTypeSet<KType> newSet = createNewSetInstance();

        newSet.add(this.keyE);

        //add a increasing number of key
        for (int i = 0; i < NB_ELEMENTS; i++) {

            final int KVpair = i;

            newSet.add(cast(KVpair));
        }

        //List the keys in the reverse-order of the internal buffer, since forEach() is iterating in reverse also:
        final KTypeArrayList<KType> keyList = new KTypeArrayList<KType>();

        keyList.add(this.keyE);

        for (int i = getKeys(newSet).length - 1; i >= 0; i--) {

            if (is_allocated(i, Intrinsics.<KType[]> cast(getKeys(newSet)))) {

                keyList.add(Intrinsics.<KType> cast(getKeys(newSet)[i]));
            }
        }

        //Test forEach predicate and stop at each key in turn.
        final KTypeArrayList<KType> keyListTest = new KTypeArrayList<KType>();

        keyListTest.clear();

        //A) Run forEach(KType)

        newSet.forEach(new KTypeProcedure<KType>() {

            @Override
            public void apply(final KType key)
            {
                keyListTest.add(key);
            }
        });

        //check that keyList/keyListTest and valueList/valueListTest are identical.
        Assert.assertEquals(keyList, keyListTest);
    }

    @Test
    public void testForEachPredicate()
    {
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

        final KTypeSet<KType> newSet = createNewSetInstance();

        newSet.add(this.keyE);

        //add a increasing number of key

        for (int i = 0; i < NB_ELEMENTS; i++) {

            final int KVpair = i;

            newSet.add(cast(KVpair));
        }

        //List the keys in the reverse-order of the internal buffer, since forEach() is iterating in reverse also:
        final KTypeArrayList<KType> keyList = new KTypeArrayList<KType>();

        keyList.add(this.keyE);

        //Test forEach predicate and stop at each key in turn.
        final KTypeArrayList<KType> keyListTest = new KTypeArrayList<KType>();

        for (int k = getKeys(newSet).length - 1; k >= 0; k--) {

            if (is_allocated(k, Intrinsics.<KType[]> cast(getKeys(newSet)))) {

                keyList.add(Intrinsics.<KType> cast(getKeys(newSet)[k]));
            }
        }

        final int size = keyList.size();

        for (int i = 0; i < size; i++)
        {
            final int currentPairIndexSizeToIterate = i + 1;

            keyListTest.clear();
            keyList.clear();

            keyList.add(this.keyE);

            for (int k = getKeys(newSet).length - 1; k >= 0; k--) {

                if (is_allocated(k, Intrinsics.<KType[]> cast(getKeys(newSet)))) {

                    keyList.add(Intrinsics.<KType> cast(getKeys(newSet)[k]));
                }
            }

            //A) Run forEach(KType)

            newSet.forEach(new KTypePredicate<KType>() {

                @Override
                public boolean apply(final KType key)
                {
                    keyListTest.add(key);

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

            //check that keyList/keyListTest and valueList/valueListTest are identical for the first
            //currentPairIndexToIterate + 1 elements
            Assert.assertEquals(currentPairIndexSizeToIterate, keyListTest.size());

            for (int j = 0; j < currentPairIndexSizeToIterate; j++)
            {
                TestUtils.assertEquals2(keyList.get(j), keyListTest.get(j));
            }
        } //end for each index
    }

    @Repeat(iterations = 25)
    @Seed("88DC7A1093FD66C5")
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

        //2) Preallocate to PREALLOCATED_SIZE, use default factor because copy-constructor use this.
        final KTypeSet<KType> refContainer = createNewSetInstance(PREALLOCATED_SIZE, HashContainers.DEFAULT_LOAD_FACTOR, this.TEST_STRATEGY);

        final int refCapacity = refContainer.capacity();

        //3) Fill with random values, random number of elements below preallocation
        final int nbElements = RandomizedTest.randomInt(PREALLOCATED_SIZE);

        for (int i = 0; i < nbElements; i++) {

            refContainer.add(cast(i));
        }

        //Capacity must have not changed, i.e no reallocation must have occured.
        Assert.assertEquals(refCapacity, refContainer.capacity());

        final int nbRefElements = refContainer.size();

        Assert.assertEquals(refCapacity, refContainer.capacity());

        //4) Duplicate by copy-construction and/or clone
        KTypeSet<KType> clonedContainer = getClone(refContainer);
        KTypeSet<KType> copiedContainer = getCopyConstructor(refContainer);

        final int copiedCapacity = copiedContainer.capacity();
        final int clonedCapacity = copiedContainer.capacity();

        Assert.assertEquals(nbRefElements, clonedContainer.size());
        Assert.assertEquals(nbRefElements, copiedContainer.size());
        // Due to different pre-sizings, clones and copy constructed may or may not have the same capacity as the refContainer.
        Assert.assertTrue(clonedContainer.equals(refContainer));
        Assert.assertTrue(copiedContainer.equals(refContainer));

        //Maybe we were lucky, iterate duplication over itself several times
        for (int j = 0; j < 10; j++) {

            clonedContainer = getClone(clonedContainer);
            copiedContainer = getFrom(copiedContainer);

            //when copied over itself, of course every characteristic must be constant, else something is wrong.
            Assert.assertEquals("j = " + j, nbRefElements, clonedContainer.size());
            Assert.assertEquals("j = " + j, nbRefElements, copiedContainer.size());
            Assert.assertEquals("j = " + j, clonedCapacity, clonedContainer.capacity());
            Assert.assertEquals("j = " + j, copiedCapacity, copiedContainer.capacity());
            Assert.assertTrue("j = " + j, clonedContainer.equals(refContainer));
            Assert.assertTrue("j = " + j, copiedContainer.equals(refContainer));
        }
    }

    private KTypeSet<KType> createSetWithOrderedData(final int size)
    {
        final KTypeSet<KType> newSet = createNewSetInstance();

        for (int i = 0; i < size; i++) {

            newSet.add(cast(i));
        }

        return newSet;
    }

    private boolean is_allocated(final int slot, final KType[] keys) {

        return !Intrinsics.<KType> isEmpty(keys[slot]);
    }
}
