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

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
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

            final int mask = this.set.keys.length - 1;

            for (int i = 0; i < this.set.keys.length; i++)
            {
                if (!is_allocated(i, this.set.keys))
                {
                    //if not allocated, generic version if patched to null for GC sake
                    /*! #if (($TemplateOptions.KTypeGeneric)  || $SA  ) !*/
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

            if (this.set.allocatedDefaultKey) {

                //try to reach the key by contains()
                Assert.assertTrue(this.set.contains(Intrinsics.<KType> defaultKTypeValue()));

                //check slot
                Assert.assertEquals(-2, this.set.lslot());

                //Retrieve again by lkey() :
                TestUtils.assertEquals2(Intrinsics.defaultKTypeValue(), this.set.lkey());
                occupied++;
            }

            Assert.assertEquals(occupied, this.set.size());

        }
    }

    /* */
    @Test
    public void testAddVarArgs()
    {
        this.set.add(asArrayObjects(this.key0, this.key1, this.key2, this.key1, this.key0));
        Assert.assertEquals(3, this.set.size());
        TestUtils.assertSortedListEqualsByReference(this.set.toArray(), this.key0, this.key1, this.key2);
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
    public void testFullLoadFactor()
    {
        this.set = new KTypeOpenIdentityHashSet<KType>(1, 1f);

        // Fit in the byte key range.
        final int capacity = 0x80;
        final int max = capacity - 2;
        for (int i = 2; i <= max; i++)
        {
            this.set.add(cast(i));
        }

        this.set.add(this.key1);

        // Still not expanded.
        Assert.assertEquals(max, this.set.size());
        Assert.assertEquals(capacity, this.set.keys.length);
        // Won't expand (existing key).
        this.set.add(this.key1);
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
        for (int i = 1; i < max; i++)
        {
            this.set.add(cast(i));
        }

        this.set.add(this.key0);

        Assert.assertEquals(max, this.set.size());
        Assert.assertEquals(capacity, this.set.keys.length);

        // Non-existent key.
        this.set.remove(cast(max + 1));
        Assert.assertFalse(this.set.contains(cast(max + 1)));

        // Should not expand because we're replacing an existing element.
        Assert.assertFalse(this.set.add(this.key0));
        Assert.assertEquals(max, this.set.size());
        Assert.assertEquals(capacity, this.set.keys.length);

        // Remove from a full set.
        this.set.remove(this.key0);
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
    public void testRemoveAllWithPredicate2()
    {
        this.set.add(asArrayObjects(this.key0, this.k1, this.k2, this.key4));

        Assert.assertEquals(2, this.set.removeAll(new KTypePredicate<KType>()
                {
            @Override
            public boolean apply(final KType v)
            {
                return (v == KTypeOpenIdentityHashSetTest.this.k1) || (v == KTypeOpenIdentityHashSetTest.this.key0);
            };
                }));

        TestUtils.assertSortedListEqualsByReference(this.set.toArray(), this.key2, this.key4);
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

        TestUtils.assertSortedListEqualsByReference(this.set.toArray(), this.key1, this.key2);
    }

    /* */
    @Test
    public void testRetainAllWithPredicate2()
    {
        this.set.add(asArrayObjects(this.key0, this.k1, this.k2, this.k3, this.k4, this.k5));

        Assert.assertEquals(4, this.set.retainAll(new KTypePredicate<KType>()
                {
            @Override
            public boolean apply(final KType v)
            {
                return v == KTypeOpenIdentityHashSetTest.this.key0 || v == KTypeOpenIdentityHashSetTest.this.k3;
            };
                }));

        TestUtils.assertSortedListEqualsByReference(this.set.toArray(), this.key0, this.k3);
    }

    /* */
    @Test
    public void testClear()
    {
        this.set.add(asArrayObjects(this.k1, this.k2, this.k3));
        this.set.clear();
        checkConsistency();
        Assert.assertEquals(0, this.set.size());

        this.set.add(asArrayObjects(this.k0, this.k2, this.k8));
        this.set.clear();
        checkConsistency();
        Assert.assertEquals(0, this.set.size());
    }

    /* */
    @Test
    public void testIterable()
    {
        this.set.add(asArrayObjects(this.k1, this.k2, this.k2, this.k3, this.k4));
        this.set.remove(this.k2);
        Assert.assertEquals(3, this.set.size());

        int counted = 0;
        for (final KTypeCursor<KType> cursor : this.set)
        {
            if (cursor.index == this.set.keys.length) {

                TestUtils.assertEquals2(Intrinsics.defaultKTypeValue(), cursor.value);
                counted++;
                continue;
            }

            counted++;
            Assert.assertTrue(this.set.contains(cursor.value));

            TestUtils.assertEquals2(cursor.value, this.set.lkey());
        }
        Assert.assertEquals(counted, this.set.size());

        this.set.clear();
        Assert.assertFalse(this.set.iterator().hasNext());
    }

    /* */
    @Test
    public void testIterable2()
    {
        this.set.add(asArrayObjects(this.key0, this.k2, this.k2, this.k3, this.k4));
        this.set.remove(this.k2);
        Assert.assertEquals(3, this.set.size());

        int counted = 0;
        for (final KTypeCursor<KType> cursor : this.set)
        {
            if (cursor.index == this.set.keys.length) {

                TestUtils.assertEquals2(Intrinsics.defaultKTypeValue(), cursor.value);
                counted++;
                continue;
            }

            counted++;
            Assert.assertTrue(this.set.contains(cursor.value));

            TestUtils.assertEquals2(cursor.value, this.set.lkey());
        }
        Assert.assertEquals(counted, this.set.size());

        this.set.clear();
        Assert.assertFalse(this.set.iterator().hasNext());
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testClone()
    {
        this.set.add(this.key1, this.key2, this.key3, this.key0);

        final KTypeOpenIdentityHashSet<KType> cloned = this.set.clone();
        cloned.removeAllOccurrences(this.key1);

        TestUtils.assertSortedListEqualsByReference(this.set.toArray(), this.key0, this.key1, this.key2, this.key3);
        TestUtils.assertSortedListEqualsByReference(cloned.toArray(), this.key0, this.key2, this.key3);
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

    @Repeat(iterations = 5)
    @Test
    public void testForEachProcedureWithException()
    {
        final Random randomVK = RandomizedTest.getRandom();

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

        newSet.add(this.key0);

        for (int i = 0; i < NB_ELEMENTS; i++) {

            final Integer KVpair = randomVK.nextInt((int) (0.7 * NB_ELEMENTS));

            newSet.add(KVpair);
        }

        //List the keys in the reverse-order of the internal buffer, since forEach() is iterating in reverse also:
        final ArrayList<Object> keyList = new ArrayList<Object>();

        if (newSet.allocatedDefaultKey) {

            keyList.add(Intrinsics.defaultKTypeValue());
        }

        //Test forEach predicate and stop at each key in turn.
        final ArrayList<Object> keyListTest = new ArrayList<Object>();

        for (int k = newSet.keys.length - 1; k >= 0; k--) {

            if (is_allocated(k, newSet.keys)) {

                keyList.add((newSet.keys[k]));
            }
        }

        final int size = keyList.size();

        for (int i = 0; i < size; i++)
        {
            final int currentPairIndexSizeToIterate = i + 1;

            keyListTest.clear();

            keyList.clear();

            if (newSet.allocatedDefaultKey) {

                keyList.add(Intrinsics.defaultKTypeValue());
            }

            for (int k = newSet.keys.length - 1; k >= 0; k--) {

                if (is_allocated(k, newSet.keys)) {

                    keyList.add(newSet.keys[k]);
                }
            }

            //A) Run forEach(KType)
            try
            {
                newSet.forEach(new KTypeProcedure<Object>() {

                    @Override
                    public void apply(final Object key)
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

    @Repeat(iterations = 5)
    @Test
    public void testForEachProcedure()
    {
        final Random randomVK = RandomizedTest.getRandom();

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

        newSet.add(this.key0);

        for (int i = 0; i < NB_ELEMENTS; i++) {

            final Integer KVpair = randomVK.nextInt((int) (0.7 * NB_ELEMENTS));

            newSet.add(KVpair);
        }

        //List the keys in the reverse-order of the internal buffer, since forEach() is iterating in reverse also:
        final ArrayList<Object> keyList = new ArrayList<Object>();

        if (newSet.allocatedDefaultKey) {

            keyList.add(Intrinsics.defaultKTypeValue());
        }

        for (int i = newSet.keys.length - 1; i >= 0; i--) {

            if (is_allocated(i, newSet.keys)) {

                keyList.add(newSet.keys[i]);
            }
        }

        //Test forEach predicate and stop at each key in turn.
        final ArrayList<Object> keyListTest = new ArrayList<Object>();

        keyListTest.clear();

        //A) Run forEach(KType)

        newSet.forEach(new KTypeProcedure<Object>() {

            @Override
            public void apply(final Object key)
            {
                keyListTest.add(key);
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

    @Repeat(iterations = 5)
    @Test
    public void testForEachPredicate()
    {
        final Random randomVK = RandomizedTest.getRandom();

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

        newSet.add(this.key0);

        for (int i = 0; i < NB_ELEMENTS; i++) {

            final Integer KVpair = randomVK.nextInt((int) (0.7 * NB_ELEMENTS));

            newSet.add(KVpair);
        }

        //List the keys in the reverse-order of the internal buffer, since forEach() is iterating in reverse also:
        final ArrayList<Object> keyList = new ArrayList<Object>();

        /*! #if ($SA)
        if (newSet.allocatedDefaultKey) {

            keyList.add(Intrinsics.defaultKTypeValue());
        }
        #end !*/

        //Test forEach predicate and stop at each key in turn.
        final ArrayList<Object> keyListTest = new ArrayList<Object>();

        for (int k = newSet.keys.length - 1; k >= 0; k--) {

            if (is_allocated(k, newSet.keys)) {

                keyList.add(newSet.keys[k]);
            }
        }

        final int size = keyList.size();

        for (int i = 0; i < size; i++)
        {
            final int currentPairIndexSizeToIterate = i + 1;

            keyListTest.clear();
            keyList.clear();

            if (newSet.allocatedDefaultKey) {

                keyList.add(Intrinsics.defaultKTypeValue());
            }

            for (int k = newSet.keys.length - 1; k >= 0; k--) {

                if (is_allocated(k, newSet.keys)) {

                    keyList.add(newSet.keys[k]);
                }
            }

            //A) Run forEach(KType)

            newSet.forEach(new KTypePredicate<Object>() {

                @Override
                public boolean apply(final Object key)
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

    private boolean is_allocated(final int slot, final Object[] keys) {

        return keys[slot] != Intrinsics.defaultKTypeValue();
    }
}
