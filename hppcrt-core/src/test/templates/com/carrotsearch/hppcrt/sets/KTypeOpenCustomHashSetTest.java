package com.carrotsearch.hppcrt.sets;

import java.util.*;

import org.junit.*;

import static com.carrotsearch.hppcrt.TestUtils.*;
import static org.junit.Assert.*;


import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.lists.*;
import com.carrotsearch.hppcrt.TestUtils;
import com.carrotsearch.hppcrt.cursors.*;
import com.carrotsearch.hppcrt.mutables.*;
import com.carrotsearch.hppcrt.predicates.*;
import com.carrotsearch.hppcrt.procedures.*;
import com.carrotsearch.hppcrt.sets.*;
import com.carrotsearch.hppcrt.sorting.*;
import com.carrotsearch.hppcrt.strategies.*;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.*;

/**
 * Unit tests for {@link KTypeOpenCustomHashSetTest}.
 */
/*! ${TemplateOptions.doNotGenerateKType("BOOLEAN")} !*/
/*! #set( $ROBIN_HOOD_FOR_PRIMITIVES = true) !*/
/*! #set( $ROBIN_HOOD_FOR_GENERICS = true) !*/
// If RH is defined, RobinHood Hashing is in effect :
/*! #set( $RH = (($TemplateOptions.KTypeGeneric && $ROBIN_HOOD_FOR_GENERICS) || ($TemplateOptions.KTypeNumeric && $ROBIN_HOOD_FOR_PRIMITIVES)) ) !*/
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeOpenCustomHashSetTest<KType> extends AbstractKTypeTest<KType>
{
    /**
     * Per-test fresh initialized instance.
     */
    public KTypeOpenCustomHashSet<KType> set;

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
        this.set = KTypeOpenCustomHashSet.newInstance(new KTypeStandardHash<KType>());
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
                if (/*! #if ($RH) !*/
                        this.set.allocated[i] == -1
                        /*!#else
                !set.allocated[i]
                #end !*/)
                {
                    //if not allocated, generic version if patched to null for GC sake
                    /*! #if ($TemplateOptions.KTypeGeneric) !*/
                    TestUtils.assertEquals2(Intrinsics.defaultKTypeValue(), this.set.keys[i]);
                    /*! #end !*/
                }
                else
                {
                    /*! #if ($RH) !*/
                    //check hash cache consistency
                    Assert.assertEquals(Internals.rehash(this.set.strategy().computeHashCode(this.set.keys[i])) & mask, this.set.allocated[i]);
                    /*! #end !*/

                    //try to reach the key by contains()
                    Assert.assertTrue(this.set.contains(this.set.keys[i]));

                    //check slot
                    Assert.assertEquals(i, this.set.lslot());

                    //Retrieve again by lkey()
                    Assert.assertEquals(castType(this.set.keys[i]), castType(this.set.lkey()));

                    occupied++;
                }
            }

            Assert.assertEquals(occupied, this.set.assigned);
            Assert.assertEquals(occupied, this.set.size());

        }
    }

    @Test
    public void testAddRemoveSameHashCollision()
    {
        // This test is only applicable to selected key types.
        Assume.assumeTrue(
                int[].class.isInstance(this.set.keys) ||
                long[].class.isInstance(this.set.keys) ||
                Object[].class.isInstance(this.set.keys));

        final IntArrayList hashChain = TestUtils.generateMurmurHash3CollisionChain(0x1fff, 0x7e, 0x1fff / 3);

        /*
         * Add all of the conflicting keys to a map.
         */
        for (final IntCursor c : hashChain) {
            this.set.add(cast(c.value));
        }

        Assert.assertEquals(hashChain.size(), this.set.size());

        /*
         * Add some more keys (random).
         */
        final Random rnd = new Random(0xbabebeef);
        final IntSet chainKeys = IntOpenHashSet.from(hashChain);
        final IntSet differentKeys = new IntOpenHashSet();
        while (differentKeys.size() < 500)
        {
            final int k = rnd.nextInt();
            if (!chainKeys.contains(k) && !differentKeys.contains(k)) {
                differentKeys.add(k);
            }
        }

        for (final IntCursor c : differentKeys) {
            this.set.add(cast(c.value));
        }

        Assert.assertEquals(hashChain.size() + differentKeys.size(), this.set.size());

        /*
         * Verify the map contains all of the conflicting keys.
         */
        for (final IntCursor c : hashChain) {
            Assert.assertTrue(this.set.contains(cast(c.value)));
        }

        /*
         * Verify the map contains all the other keys.
         */
        for (final IntCursor c : differentKeys) {
            Assert.assertTrue(this.set.contains(cast(c.value)));
        }

        /*
         * Iteratively remove the keys, from first to last.
         */
        for (final IntCursor c : hashChain) {
            Assert.assertTrue(this.set.remove(cast(c.value)));
        }

        Assert.assertEquals(differentKeys.size(), this.set.size());

        /*
         * Verify the map contains all the other keys.
         */
        for (final IntCursor c : differentKeys) {
            Assert.assertTrue(this.set.contains(cast(c.value)));
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
        this.set.add(asArray(0, 1, 2, 1, 0));
        Assert.assertEquals(3, this.set.size());
        TestUtils.assertSortedListEquals(this.set.toArray(), 0, 1, 2);
    }

    /* */
    @Test
    public void testAddAll()
    {
        final KTypeOpenCustomHashSet<KType> set2 = new KTypeOpenCustomHashSet<KType>(new KTypeStandardHash<KType>());
        set2.add(asArray(1, 2));
        this.set.add(asArray(0, 1));

        Assert.assertEquals(1, this.set.addAll(set2));
        Assert.assertEquals(0, this.set.addAll(set2));

        Assert.assertEquals(3, this.set.size());
        TestUtils.assertSortedListEquals(this.set.toArray(), 0, 1, 2);
    }

    /* */
    @Test
    public void testRemove()
    {
        this.set.add(asArray(0, 1, 2, 3, 4));

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
            final KTypeOpenCustomHashSet<KType> set = new KTypeOpenCustomHashSet<KType>(i, new KTypeStandardHash<KType>());

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
        this.set = new KTypeOpenCustomHashSet<KType>(1, 1f, new KTypeStandardHash<KType>());

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
        this.set = new KTypeOpenCustomHashSet<KType>(1, 1f, new KTypeStandardHash<KType>());
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
        this.set.add(asArray(0, 1, 2, 3, 4));

        final KTypeOpenCustomHashSet<KType> list2 = new KTypeOpenCustomHashSet<KType>(new KTypeStandardHash<KType>());
        list2.add(asArray(1, 3, 5));

        Assert.assertEquals(2, this.set.removeAll(list2));
        Assert.assertEquals(3, this.set.size());
        TestUtils.assertSortedListEquals(this.set.toArray(), 0, 2, 4);
    }

    /* */
    @Test
    public void testRemoveAllWithPredicate()
    {
        this.set.add(newArray(this.k0, this.k1, this.k2));

        Assert.assertEquals(1, this.set.removeAll(new KTypePredicate<KType>()
                {
            @Override
            public boolean apply(final KType v)
            {
                return v == KTypeOpenCustomHashSetTest.this.key1;
            };
                }));

        TestUtils.assertSortedListEquals(this.set.toArray(), 0, this.key2);
    }

    /* */
    @Test
    public void testRemoveAllWithPredicateInterrupted()
    {
        this.set.add(newArray(this.k0, this.k1, this.k2, this.k3, this.k4, this.k5, this.k6, this.k7, this.k8));

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
                    if (v == KTypeOpenCustomHashSetTest.this.key7) {
                        throw t;
                    }
                    return v == KTypeOpenCustomHashSetTest.this.key2 || v == KTypeOpenCustomHashSetTest.this.key9 || v == KTypeOpenCustomHashSetTest.this.key5;
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
        this.set.add(newArray(this.k0, this.k1, this.k2, this.k3, this.k4, this.k5));

        Assert.assertEquals(4, this.set.retainAll(new KTypePredicate<KType>()
                {
            @Override
            public boolean apply(final KType v)
            {
                return v == KTypeOpenCustomHashSetTest.this.key1 || v == KTypeOpenCustomHashSetTest.this.key2;
            };
                }));

        TestUtils.assertSortedListEquals(this.set.toArray(), this.key1, this.key2);
    }

    /* */
    @Test
    public void testClear()
    {
        this.set.add(asArray(1, 2, 3));
        this.set.clear();
        checkConsistency();
        Assert.assertEquals(0, this.set.size());
    }

    /* */
    @Test
    public void testIterable()
    {
        this.set.add(asArray(1, 2, 2, 3, 4));
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

        KType key1_ = cast(1);

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        key1_ = (KType) new Integer(1);
        Assert.assertNotSame(this.key1, key1_);
        /*! #end !*/

        Assert.assertEquals(castType(this.key1), castType(key1_));

        Assert.assertTrue(this.set.contains(key1_));

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        Assert.assertSame(this.key1, this.set.lkey());
        /*! #end !*/

        Assert.assertEquals(castType(key1_), castType(this.set.lkey()));
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

    /**
     * Run some random insertions/ deletions and compare the results
     * against <code>java.util.HashSet</code>.
     */
    @Test
    public void testAgainstHashMap()
    {
        final java.util.Random rnd = new java.util.Random();
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
                    Assert.assertEquals(castType(key), castType(this.set.lkey()));
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

    /* */
    @Test
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    public void testHashCodeEquals()
    {
        final KTypeOpenCustomHashSet<Integer> l0 = KTypeOpenCustomHashSet.from(new KTypeStandardHash<Integer>());
        Assert.assertEquals(0, l0.hashCode());
        Assert.assertEquals(l0, KTypeOpenCustomHashSet.newInstance(new KTypeStandardHash<KType>()));

        final KTypeOpenCustomHashSet<KType> l1 = KTypeOpenCustomHashSet.from(new KTypeStandardHash<KType>(), this.k1, this.k2, this.k3);
        final KTypeOpenCustomHashSet<KType> l2 = KTypeOpenCustomHashSet.from(new KTypeStandardHash<KType>(), this.k1, this.k2);
        l2.add(this.k3);

        Assert.assertEquals(l1.hashCode(), l2.hashCode());
        Assert.assertEquals(l1, l2);
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    @Test
    public void testHashCodeWithNulls()
    {
        final KTypeOpenCustomHashSet<KType> l1 = KTypeOpenCustomHashSet.from(new KTypeStandardHash<KType>(), this.k1, null, this.k3);
        final KTypeOpenCustomHashSet<KType> l2 = KTypeOpenCustomHashSet.from(new KTypeStandardHash<KType>(), this.k1, null);
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

        final KTypeOpenCustomHashSet<KType> cloned = this.set.clone();
        cloned.removeAllOccurrences(this.key1);

        TestUtils.assertSortedListEquals(this.set.toArray(), this.key1, this.key2, this.key3);
        TestUtils.assertSortedListEquals(cloned.toArray(), this.key2, this.key3);
    }

    /*
     * 
     */
    @Test
    public void testToString()
    {
        Assume.assumeTrue(
                int[].class.isInstance(this.set.keys) ||
                short[].class.isInstance(this.set.keys) ||
                byte[].class.isInstance(this.set.keys) ||
                long[].class.isInstance(this.set.keys) ||
                Object[].class.isInstance(this.set.keys));

        this.set.add(this.key1, this.key2);
        String asString = this.set.toString();
        asString = asString.replaceAll("[\\[\\],\\ ]", "");
        final char[] asCharArray = asString.toCharArray();
        Arrays.sort(asCharArray);
        Assert.assertEquals("12", new String(asCharArray));
    }

    //only applicable to generic types keys
    @Test
    public void testHashingStrategyCloneEquals()
    {
        //a) Check that 2 different sets filled the same way with same values and strategies = null
        //are indeed equal.
        final long TEST_SEED = 23167132166456L;
        final int TEST_SIZE = (int) 100e3;
        final KTypeOpenCustomHashSet<KType> refSet = createSetWithRandomData(TEST_SIZE, new KTypeStandardHash<KType>(), TEST_SEED);
        KTypeOpenCustomHashSet<KType> refSet2 = createSetWithRandomData(TEST_SIZE, new KTypeStandardHash<KType>(), TEST_SEED);

        Assert.assertEquals(refSet, refSet2);

        //b) Clone the above. All sets are now identical.
        KTypeOpenCustomHashSet<KType> refSetclone = refSet.clone();
        KTypeOpenCustomHashSet<KType> refSet2clone = refSet2.clone();

        //all strategies are null
        Assert.assertEquals(refSet.strategy(), refSet2.strategy());
        Assert.assertEquals(refSet2.strategy(), refSetclone.strategy());
        Assert.assertEquals(refSetclone.strategy(), refSet2clone.strategy());
        Assert.assertEquals(refSet2clone.strategy(), new KTypeStandardHash<KType>());

        Assert.assertEquals(refSet, refSetclone);
        Assert.assertEquals(refSetclone, refSet2);
        Assert.assertEquals(refSet2, refSet2clone);
        Assert.assertEquals(refSet2clone, refSet);

        //cleanup
        refSetclone = null;
        refSet2 = null;
        refSet2clone = null;
        System.gc();

        //c) Create a set nb 3 with same integer content, but with a strategy mapping on equals.
        final KTypeOpenCustomHashSet<KType> refSet3 = createSetWithRandomData(TEST_SIZE,
                new KTypeHashingStrategy<KType>() {

            @Override
            public int computeHashCode(final KType object) {

                return Internals.rehash(object);
            }

            @Override
            public boolean equals(final KType o1, final KType o2) {

                return Intrinsics.equalsKType(o1, o2);
            }
        }, TEST_SEED);

        //because they do the same thing as above, but with semantically different strategies, ref3 is != ref
        Assert.assertFalse(refSet.equals(refSet3));

        //However, if we cloned refSet3
        final KTypeOpenCustomHashSet<KType> refSet3clone = refSet3.clone();
        Assert.assertEquals(refSet3, refSet3clone);

        //strategies are copied by reference only
        Assert.assertTrue(refSet3.strategy() == refSet3clone.strategy());

        //d) Create identical set with same different strategy instances, but which consider themselves equals()
        KTypeOpenCustomHashSet<KType> refSet4 = createSetWithRandomData(TEST_SIZE,
                new KTypeHashingStrategy<KType>() {

            @Override
            public boolean equals(final Object obj) {

                return true;
            }

            @Override
            public int computeHashCode(final KType object) {

                return Internals.rehash(object);
            }

            @Override
            public boolean equals(final KType o1, final KType o2) {

                return Intrinsics.equalsKType(o1, o2);
            }
        }, TEST_SEED);

        KTypeOpenCustomHashSet<KType> refSet4Image = createSetWithRandomData(TEST_SIZE,
                new KTypeHashingStrategy<KType>() {

            @Override
            public boolean equals(final Object obj) {

                return true;
            }

            @Override
            public int computeHashCode(final KType object) {

                return Internals.rehash(object);
            }

            @Override
            public boolean equals(final KType o1, final KType o2) {

                return Intrinsics.equalsKType(o1, o2);
            }
        }, TEST_SEED);

        Assert.assertEquals(refSet4, refSet4Image);
        //but strategy instances are indeed 2 different objects
        Assert.assertFalse(refSet4.strategy() == refSet4Image.strategy());

        //cleanup
        refSet4 = null;
        refSet4Image = null;
        System.gc();

        //e) Do contrary to 4), hashStrategies always != from each other by equals.
        final KTypeHashingStrategy<KType> alwaysDifferentStrategy = new KTypeHashingStrategy<KType>() {

            @Override
            public boolean equals(final Object obj) {

                //never equal !!!
                return false;
            }

            @Override
            public int computeHashCode(final KType object) {

                return Internals.rehash(object);
            }

            @Override
            public boolean equals(final KType o1, final KType o2) {

                return Intrinsics.equalsKType(o1, o2);
            }
        };

        final KTypeOpenCustomHashSet<KType> refSet5 = createSetWithRandomData(TEST_SIZE, alwaysDifferentStrategy, TEST_SEED);
        final KTypeOpenCustomHashSet<KType> refSet5alwaysDifferent = createSetWithRandomData(TEST_SIZE, alwaysDifferentStrategy, TEST_SEED);

        //both sets are NOT equal because their strategies said they are different
        Assert.assertFalse(refSet5.equals(refSet5alwaysDifferent));
    }

    @Test
    public void testHashingStrategyAddContainsRemove()
    {
        final long TEST_SEED = 749741621030146103L;
        final int TEST_SIZE = (int) 500e3;

        //those following 3  sets behave indeed the same in the test context:
        final KTypeOpenCustomHashSet<KType> refSet = KTypeOpenCustomHashSet.newInstance(new KTypeStandardHash<KType>());

        final KTypeOpenCustomHashSet<KType> refSetIdenticalStrategy = KTypeOpenCustomHashSet.newInstanceWithCapacity(
                KTypeOpenCustomHashSet.DEFAULT_CAPACITY,
                KTypeOpenCustomHashSet.DEFAULT_LOAD_FACTOR,
                new KTypeHashingStrategy<KType>() {

                    @Override
                    public boolean equals(final Object obj) {

                        //always
                        return true;
                    }

                    @Override
                    public int computeHashCode(final KType object) {

                        return Internals.rehash(object);
                    }

                    @Override
                    public boolean equals(final KType o1, final KType o2) {

                        return Intrinsics.equalsKType(o1, o2);
                    }
                });

        //compute the iterations doing multiple operations
        final Random prng = new Random(TEST_SEED);

        for (int i = 0; i < TEST_SIZE; i++)
        {
            //a) generate a value to put
            int putValue = prng.nextInt();

            refSet.add(cast(putValue));
            refSetIdenticalStrategy.add(cast(putValue));

            Assert.assertEquals(refSet.contains(cast(putValue)), refSetIdenticalStrategy.contains(cast(putValue)));

            final boolean isToBeRemoved = (prng.nextInt() % 3 == 0);
            putValue = prng.nextInt();

            if (isToBeRemoved)
            {
                refSet.remove(cast(putValue));
                refSetIdenticalStrategy.remove(cast(putValue));

                Assert.assertFalse(refSet.contains(cast(putValue)));
                Assert.assertFalse(refSetIdenticalStrategy.contains(cast(putValue)));
            }

            Assert.assertEquals(refSet.contains(cast(putValue)), refSetIdenticalStrategy.contains(cast(putValue)));

            //test size
            Assert.assertEquals(refSet.size(), refSetIdenticalStrategy.size());
        }
    }

    private KTypeOpenCustomHashSet<KType> createSetWithRandomData(final int size, final KTypeHashingStrategy<? super KType> strategy, final long randomSeed)
    {
        final Random prng = new Random(randomSeed);

        final KTypeOpenCustomHashSet<KType> newSet = KTypeOpenCustomHashSet.newInstanceWithCapacity(KTypeOpenCustomHashSet.DEFAULT_CAPACITY,
                KTypeOpenCustomHashSet.DEFAULT_LOAD_FACTOR, strategy);

        for (int i = 0; i < size; i++)
        {
            newSet.add(cast(prng.nextInt()));
        }

        return newSet;
    }

    @Test
    public void testPooledIteratorForEach()
    {
        //A) Unbroken for-each loop
        //must accommodate even the smallest primitive type
        //so that the iteration do not break before it should...
        final int TEST_SIZE = 126;
        final long TEST_ROUNDS = 5000;

        final KTypeOpenCustomHashSet<KType> testContainer = createSetWithOrderedData(TEST_SIZE);

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

        final KTypeOpenCustomHashSet<KType> testContainer = createSetWithOrderedData(TEST_SIZE);

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

        final KTypeOpenCustomHashSet<KType> testContainer = createSetWithOrderedData(TEST_SIZE);

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

            final KTypeOpenCustomHashSet<KType>.EntryIterator loopIterator = testContainer.iterator();

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

        final KTypeOpenCustomHashSet<KType> testContainer = createSetWithOrderedData(TEST_SIZE);
        final int startingPoolSize = testContainer.entryIteratorPool.size();

        int count = 0;
        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            //Classical iterator loop, with manually allocated Iterator
            final long initialPoolSize = testContainer.entryIteratorPool.size();

            final KTypeOpenCustomHashSet<KType>.EntryIterator loopIterator = testContainer.iterator();

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

        final KTypeOpenCustomHashSet<KType> testContainer = createSetWithOrderedData(TEST_SIZE);

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
        KTypeOpenCustomHashSet<KType>.EntryIterator loopIterator = null;

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
             int PREALLOCATED_SIZE = randomVK.nextInt(1500);
            #else
              int PREALLOCATED_SIZE = randomVK.nextInt(126);
            #end !*/

        //2) Preallocate to PREALLOCATED_SIZE :
        final KTypeOpenCustomHashSet<KType> newSet = KTypeOpenCustomHashSet.newInstanceWithCapacity(PREALLOCATED_SIZE,
                KTypeOpenCustomHashSet.DEFAULT_LOAD_FACTOR, new KTypeStandardHash<KType>());

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

        final KTypeOpenCustomHashSet<KType> newSet = KTypeOpenCustomHashSet.newInstance(new KTypeStandardHash<KType>());

        //add a randomized number of key
        for (int i = 0; i < NB_ELEMENTS; i++) {

            final int KVpair = randomVK.nextInt((int) (0.7 * NB_ELEMENTS));

            newSet.add(cast(KVpair));
        }

        //List the keys in the reverse-order of the internal buffer, since forEach() is iterating in reverse also:
        final ArrayList<Integer> keyList = new ArrayList<Integer>();

        //Test forEach predicate and stop at each key in turn.
        final ArrayList<Integer> keyListTest = new ArrayList<Integer>();

        for (int k = newSet.allocated.length - 1; k >= 0; k--) {

            if (newSet.allocated[k] /*! #if ($RH) !*/!= -1 /*! #end !*/) {

                keyList.add(castType(newSet.keys[k]));
            }
        }

        final int size = keyList.size();

        for (int i = 0; i < size; i++)
        {
            final int currentPairIndexSizeToIterate = i + 1;

            keyListTest.clear();

            keyList.clear();

            for (int k = newSet.allocated.length - 1; k >= 0; k--) {

                if (newSet.allocated[k] /*! #if ($RH) !*/!= -1 /*! #end !*/) {

                    keyList.add(castType(newSet.keys[k]));
                }
            }

            //A) Run forEach(KType)
            try
            {
                newSet.forEach(new KTypeProcedure<KType>() {

                    @Override
                    public void apply(final KType key)
                    {
                        keyListTest.add(castType(key));

                        //when the stopping key/value pair is encountered, add to list and stop iteration
                        if (castType(key) == keyList.get(currentPairIndexSizeToIterate - 1))
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

                //check that keyList/keyListTest and valueList/valueListTest are identical for the first
                //currentPairIndexToIterate + 1 elements
                Assert.assertEquals(currentPairIndexSizeToIterate, keyListTest.size());

                for (int j = 0; j < currentPairIndexSizeToIterate; j++)
                {
                    Assert.assertEquals(keyList.get(j), keyListTest.get(j));
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

        final KTypeOpenCustomHashSet<KType> newSet = KTypeOpenCustomHashSet.newInstance(new KTypeStandardHash<KType>());

        //add a randomized number of key
        //use the same value for keys and values to ease later analysis
        for (int i = 0; i < NB_ELEMENTS; i++) {

            final int KVpair = randomVK.nextInt((int) (0.7 * NB_ELEMENTS));

            newSet.add(cast(KVpair));
        }

        //List the keys in the reverse-order of the internal buffer, since forEach() is iterating in reverse also:
        final ArrayList<Integer> keyList = new ArrayList<Integer>();

        for (int i = newSet.allocated.length - 1; i >= 0; i--) {

            if (newSet.allocated[i] /*! #if ($RH) !*/!= -1 /*! #end !*/) {

                keyList.add(castType(newSet.keys[i]));
            }
        }

        //Test forEach predicate and stop at each key in turn.
        final ArrayList<Integer> keyListTest = new ArrayList<Integer>();

        keyListTest.clear();

        //A) Run forEach(KType)

        newSet.forEach(new KTypeProcedure<KType>() {

            @Override
            public void apply(final KType key)
            {
                keyListTest.add(castType(key));
            }
        });

        //check that keyList/keyListTest and valueList/valueListTest are identical.
        Assert.assertEquals(keyList, keyListTest);
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

        final KTypeOpenCustomHashSet<KType> newSet = KTypeOpenCustomHashSet.newInstance(new KTypeStandardHash<KType>());

        //add a randomized number of key
        //use the same value for keys and values to ease later analysis
        for (int i = 0; i < NB_ELEMENTS; i++) {

            final int KVpair = randomVK.nextInt((int) (0.7 * NB_ELEMENTS));

            newSet.add(cast(KVpair));
        }

        //List the keys in the reverse-order of the internal buffer, since forEach() is iterating in reverse also:
        final ArrayList<Integer> keyList = new ArrayList<Integer>();

        //Test forEach predicate and stop at each key in turn.
        final ArrayList<Integer> keyListTest = new ArrayList<Integer>();

        for (int k = newSet.allocated.length - 1; k >= 0; k--) {

            if (newSet.allocated[k] /*! #if ($RH) !*/!= -1 /*! #end !*/) {

                keyList.add(castType(newSet.keys[k]));
            }
        }

        final int size = keyList.size();

        for (int i = 0; i < size; i++)
        {
            final int currentPairIndexSizeToIterate = i + 1;

            keyListTest.clear();
            keyList.clear();

            for (int k = newSet.allocated.length - 1; k >= 0; k--) {

                if (newSet.allocated[k] /*! #if ($RH) !*/!= -1 /*! #end !*/) {

                    keyList.add(castType(newSet.keys[k]));
                }
            }

            //A) Run forEach(KType)

            newSet.forEach(new KTypePredicate<KType>() {

                @Override
                public boolean apply(final KType key)
                {
                    keyListTest.add(castType(key));

                    //when the stopping key/value pair is encountered, add to list and stop iteration
                    if (castType(key) == keyList.get(currentPairIndexSizeToIterate - 1))
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
                Assert.assertEquals(keyList.get(j), keyListTest.get(j));
            }
        } //end for each index
    }

    private KTypeOpenCustomHashSet<KType> createSetWithOrderedData(final int size)
    {

        final KTypeOpenCustomHashSet<KType> newSet = KTypeOpenCustomHashSet.newInstanceWithCapacity(KTypeOpenCustomHashSet.DEFAULT_CAPACITY,
                KTypeOpenCustomHashSet.DEFAULT_LOAD_FACTOR, new KTypeStandardHash<KType>());

        for (int i = 0; i < size; i++) {

            newSet.add(cast(i));
        }

        return newSet;
    }

}
