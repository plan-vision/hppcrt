package com.carrotsearch.hppcrt.maps;

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
import com.carrotsearch.hppcrt.mutables.*;
import com.carrotsearch.hppcrt.sets.*;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.*;

/**
 * Tests for {@link KTypeVTypeOpenIdentityHashMap}.
 */
/*! ${TemplateOptions.doNotGenerateKType("BOOLEAN", "BYTE", "CHAR", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE")} !*/
//${TemplateOptions.doNotGenerateVType("BOOLEAN")}
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeVTypeOpenIdentityHashMapTest<KType, VType> extends AbstractKTypeVTypeTest<KType, VType>
{

    @BeforeClass
    public static void configure()
    {
        IteratorPool.configureInitialPoolSize(8);
    }

    /**
     * Per-test fresh initialized instance.
     */
    public KTypeVTypeOpenIdentityHashMap<KType, VType> map = KTypeVTypeOpenIdentityHashMap.newInstance();


    /**
     * The identity map is only valid for Object keys anyway
     */
    @Before
    public void initialize() {

        this.map = KTypeVTypeOpenIdentityHashMap.newInstance();

        Assume.assumeTrue(Object[].class.isInstance(this.map.keys));
    }

    /**
     * Check that the set is consistent, i.e all allocated slots are reachable by get(),
     * and all not-allocated contains nulls if Generic
     * @param set
     */
    @After
    public void checkConsistency()
    {
        if (this.map != null)
        {
            int occupied = 0;

            final int mask = this.map.allocated.length - 1;

            for (int i = 0; i < this.map.keys.length; i++)
            {
                if (!this.map.allocated[i])
                {
                    //if not allocated, generic version if patched to null for GC sake

                    /*! #if ($TemplateOptions.KTypeGeneric) !*/
                    TestUtils.assertEquals2(Intrinsics.defaultKTypeValue(), this.map.keys[i]);
                    /*! #end !*/
                    /*! #if ($TemplateOptions.VTypeGeneric) !*/
                    TestUtils.assertEquals2(Intrinsics.defaultVTypeValue(), this.map.values[i]);
                    /*! #end !*/
                }
                else
                {
                    //try to reach the key by contains()
                    Assert.assertTrue(this.map.containsKey(this.map.keys[i]));

                    //check slot
                    Assert.assertEquals(i, this.map.lslot());

                    //get() test
                    Assert.assertEquals(vcastType(this.map.values[i]), vcastType(this.map.get(this.map.keys[i])));

                    //retreive again by lkey(), lget() :

                    //test key by reference !
                    Assert.assertSame(this.map.keys[i], this.map.lkey());
                    Assert.assertEquals(vcastType(this.map.values[i]), vcastType(this.map.lget()));

                    occupied++;
                }
            }
            Assert.assertEquals(occupied, this.map.assigned);
        }
    }

    /* */
    @Test
    public void testCloningConstructor()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value3);

        assertSameMap(this.map, KTypeVTypeOpenIdentityHashMap.from(this.map));
        assertSameMap(this.map, new KTypeVTypeOpenIdentityHashMap<KType, VType>(this.map));
    }

    /* */
    /*! #if ($TemplateOptions.VTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testFromArrays()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value3);

        final KTypeVTypeOpenIdentityHashMap<KType, VType> map2 = KTypeVTypeOpenIdentityHashMap.from(
                asArrayObjects(this.key1, this.key2, this.key3),
                newvArray(this.value1, this.value2, this.value3));

        assertSameMap(this.map, map2);
    }

    /* */
    @Test
    public void testPut()
    {
        this.map.put(this.key1, this.value1);

        Assert.assertTrue(this.map.containsKey(this.key1));
        TestUtils.assertEquals2(this.value1, this.map.lget());
        TestUtils.assertEquals2(this.value1, this.map.get(this.key1));
    }

    /* */
    @Test
    public void testLPut()
    {
        this.map.put(this.key1, this.value2);
        if (this.map.containsKey(this.key1)) {
            this.map.lset(this.value3);
        }

        Assert.assertTrue(this.map.containsKey(this.key1));
        TestUtils.assertEquals2(this.value3, this.map.lget());
        TestUtils.assertEquals2(this.value3, this.map.get(this.key1));
    }

    /* */
    @Test
    public void testPutOverExistingKey()
    {
        this.map.put(this.key1, this.value1);
        TestUtils.assertEquals2(this.value1, this.map.put(this.key1, this.value3));
        TestUtils.assertEquals2(this.value3, this.map.get(this.key1));
    }

    /* */
    @Test
    public void testPutWithExpansions()
    {
        final int COUNT = 10000;
        final Random rnd = new Random();

        //We must use an identity Hash map to compare !!!!
        final IdentityHashMap<Object, Object> values = new IdentityHashMap<Object, Object>();

        for (int i = 0; i < COUNT; i++)
        {
            final KType v = cast(rnd.nextInt());
            values.put(v, vcast(i));

            final boolean hadKey = values.containsKey(v);
            Assert.assertTrue(hadKey);

            this.map.put(v, vcast(i));
            Assert.assertEquals(hadKey, this.map.containsKey(v));

            Assert.assertEquals(values.size(), this.map.size());
        }

        Assert.assertEquals(values.size(), this.map.size());
    }

    /* */
    @Test
    public void testPutAll()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value1);

        final KTypeVTypeOpenIdentityHashMap<KType, VType> map2 =
                new KTypeVTypeOpenIdentityHashMap<KType, VType>();

        map2.put(this.key2, this.value2);
        map2.put(this.key3, this.value1);

        // One new key (key3).
        Assert.assertEquals(1, this.map.putAll(map2));

        // Assert the value under key2 has been replaced.
        TestUtils.assertEquals2(this.value2, this.map.get(this.key2));

        // And key3 has been added.
        TestUtils.assertEquals2(this.value1, this.map.get(this.key3));
        Assert.assertEquals(3, this.map.size());
    }

    /* */
    @Test
    public void testPutIfAbsent()
    {
        Assert.assertTrue(this.map.putIfAbsent(this.key1, this.value1));
        Assert.assertFalse(this.map.putIfAbsent(this.key1, this.value2));
        TestUtils.assertEquals2(this.value1, this.map.get(this.key1));
    }

    /*! #if ($TemplateOptions.VTypeNumeric)
    @Test
    public void testPutOrAdd()
    {
        assertEquals2(value1, map.putOrAdd(key1, value1, value2));
        assertEquals2(value1 + value2, map.putOrAdd(key1, value1, value2));
    }
    #end !*/

    /*! #if ($TemplateOptions.VTypeNumeric)
    @Test
    public void testAddTo()
    {
        assertEquals2(value1, map.addTo(key1, value1));
        assertEquals2(value1 + value2, map.addTo(key1, value2));
    }
    #end !*/

    /* */
    @Test
    public void testRemove()
    {
        this.map.put(this.key1, this.value1);
        TestUtils.assertEquals2(this.value1, this.map.remove(this.key1));
        TestUtils.assertEquals2(Intrinsics.defaultVTypeValue(), this.map.remove(this.key1));
        Assert.assertEquals(0, this.map.size());

        // These are internals, but perhaps worth asserting too.
        Assert.assertEquals(0, this.map.assigned);
    }

    /* */
    @Test
    public void testRemoveAllWithContainer()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value1);
        this.map.put(this.key3, this.value1);

        final KTypeArrayList<KType> list2 = KTypeArrayList.newInstance();
        list2.add(asArrayObjects(this.key2, this.key3, this.key4));

        this.map.removeAll(list2);
        Assert.assertEquals(1, this.map.size());
        Assert.assertTrue(this.map.containsKey(this.key1));
    }

    /* */
    @Test
    public void testRemoveAllWithPredicate()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value1);
        this.map.put(this.key3, this.value1);

        this.map.removeAll(new KTypePredicate<KType>()
                {
            @Override
            public boolean apply(final KType value)
            {
                return value == KTypeVTypeOpenIdentityHashMapTest.this.key2 || value == KTypeVTypeOpenIdentityHashMapTest.this.key3;
            }
                });
        Assert.assertEquals(1, this.map.size());
        Assert.assertTrue(this.map.containsKey(this.key1));
    }

    /* */
    @Test
    public void testRemoveAllWithPredicateInterrupted()
    {
        this.map.put(this.key0, this.value1);
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value1);
        this.map.put(this.key3, this.value1);
        this.map.put(this.key4, this.value1);
        this.map.put(this.key5, this.value1);
        this.map.put(this.key6, this.value1);
        this.map.put(this.key7, this.value1);
        this.map.put(this.key8, this.value1);

        final RuntimeException t = new RuntimeException();
        try
        {
            //the assert below should never be triggered because of the exception
            //so give it an invalid value in case the thing terminates  = initial size + 1
            Assert.assertEquals(10, this.map.removeAll(new KTypePredicate<KType>()
                    {
                @Override
                public boolean apply(final KType key)
                {
                    if (key == KTypeVTypeOpenIdentityHashMapTest.this.key7) {
                        throw t;
                    }
                    return key == KTypeVTypeOpenIdentityHashMapTest.this.key2 || key == KTypeVTypeOpenIdentityHashMapTest.this.key9 || key == KTypeVTypeOpenIdentityHashMapTest.this.key5;
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

        Assert.assertTrue(this.map.containsKey(this.key7));
        checkConsistency();
    }

    /* */
    @Test
    public void testRemoveAllViaKeySetView()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value1);
        this.map.put(this.key3, this.value1);

        this.map.keys().removeAll(new KTypePredicate<KType>()
                {
            @Override
            public boolean apply(final KType value)
            {
                return value == KTypeVTypeOpenIdentityHashMapTest.this.key2 || value == KTypeVTypeOpenIdentityHashMapTest.this.key3;
            }
                });
        Assert.assertEquals(1, this.map.size());
        Assert.assertTrue(this.map.containsKey(this.key1));
    }

    /* */
    @Test
    public void testRemoveAllViaValueSetView()
    {
        this.map.put(this.key1, this.value1); //del
        this.map.put(this.key2, this.value2); //del
        this.map.put(this.key3, this.value1); //del
        this.map.put(this.key4, this.value3);
        this.map.put(this.key5, this.value5);
        this.map.put(this.key6, this.value5);
        this.map.put(this.key7, this.value1); //del
        this.map.put(this.key8, this.value8);
        this.map.put(this.key9, this.value2); //del

        final int nbRemoved = this.map.values().removeAll(new KTypePredicate<VType>()
                {
            @Override
            public boolean apply(final VType value)
            {
                return value == KTypeVTypeOpenIdentityHashMapTest.this.value1 || value == KTypeVTypeOpenIdentityHashMapTest.this.value2;
            }
                });

        Assert.assertEquals(5, nbRemoved);
        Assert.assertEquals(4, this.map.size());
        Assert.assertTrue(this.map.containsKey(this.key4));
        Assert.assertTrue(this.map.containsKey(this.key5));
        Assert.assertTrue(this.map.containsKey(this.key6));
        Assert.assertTrue(this.map.containsKey(this.key8));
    }

    /* */
    @Test
    public void testRemoveAllOccurencesViaValueSetView()
    {
        this.map.put(this.key1, this.value1); //del
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value1); //del
        this.map.put(this.key4, this.value3);
        this.map.put(this.key5, this.value2);
        this.map.put(this.key6, this.value5);
        this.map.put(this.key7, this.value1); //del
        this.map.put(this.key8, this.value8);
        this.map.put(this.key9, this.value2);

        final int nbRemoved = this.map.values().removeAllOccurrences(this.value1);

        Assert.assertEquals(3, nbRemoved);
        Assert.assertEquals(6, this.map.size());
        Assert.assertTrue(this.map.containsKey(this.key2));
        Assert.assertTrue(this.map.containsKey(this.key4));
        Assert.assertTrue(this.map.containsKey(this.key5));
        Assert.assertTrue(this.map.containsKey(this.key6));
        Assert.assertTrue(this.map.containsKey(this.key8));
        Assert.assertTrue(this.map.containsKey(this.key9));
    }

    /* */
    @Test
    public void testMapsIntersection()
    {
        final KTypeVTypeOpenIdentityHashMap<KType, VType> map2 =
                KTypeVTypeOpenIdentityHashMap.newInstance();

        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value1);
        this.map.put(this.key3, this.value1);

        map2.put(this.key2, this.value1);
        map2.put(this.key4, this.value1);

        Assert.assertEquals(2, this.map.keys().retainAll(map2.keys()));

        Assert.assertEquals(1, this.map.size());
        Assert.assertTrue(this.map.containsKey(this.key2));
    }

    /* */
    @Test
    public void testMapKeySet()
    {
        this.map.put(this.key1, this.value3);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value1);

        TestUtils.assertSortedListEqualsByReference(this.map.keys().toArray(), this.key1, this.key2, this.key3);
    }

    /* */
    @Test
    public void testMapKeySetIterator()
    {
        this.map.put(this.key1, this.value3);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value1);

        int counted = 0;
        for (final KTypeCursor<KType> c : this.map.keys())
        {
            Assert.assertSame(this.map.keys[c.index], c.value);
            counted++;
        }
        Assert.assertEquals(counted, this.map.size());
    }

    /* */
    @Test
    public void testClear()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value1);
        this.map.clear();
        Assert.assertEquals(0, this.map.size());

        // These are internals, but perhaps worth asserting too.
        Assert.assertEquals(0, this.map.assigned);

        // Check if the map behaves properly upon subsequent use.
        testPutWithExpansions();
    }

    /* */
    @Test
    public void testClearViaKeySetView()
    {
        this.map.put(this.key3, this.value2);
        this.map.put(this.key4, this.value4);
        this.map.keys().clear();
        Assert.assertEquals(0, this.map.keys().size());

        // These are internals, but perhaps worth asserting too.
        Assert.assertEquals(0, this.map.assigned);

        // Check if the map behaves properly upon subsequent use.
        testPutWithExpansions();
    }

    /* */
    @Test
    public void testClearViaValueSetView()
    {
        this.map.put(this.key5, this.value4);
        this.map.put(this.key6, this.value1);
        this.map.values().clear();
        Assert.assertEquals(0, this.map.values().size());

        // These are internals, but perhaps worth asserting too.
        Assert.assertEquals(0, this.map.assigned);

        // Check if the map behaves properly upon subsequent use.
        testPutWithExpansions();
    }

    /* */
    @Test
    public void testRoundCapacity()
    {
        Assert.assertEquals(0x40000000, HashContainerUtils.roundCapacity(Integer.MAX_VALUE));
        Assert.assertEquals(0x40000000, HashContainerUtils.roundCapacity(Integer.MAX_VALUE / 2 + 1));
        Assert.assertEquals(0x40000000, HashContainerUtils.roundCapacity(Integer.MAX_VALUE / 2));
        Assert.assertEquals(KTypeVTypeOpenIdentityHashMap.MIN_CAPACITY, HashContainerUtils.roundCapacity(0));
        Assert.assertEquals(Math.max(4, KTypeVTypeOpenIdentityHashMap.MIN_CAPACITY), HashContainerUtils.roundCapacity(3));
    }

    /* */
    @Test
    public void testIterable()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value3);
        this.map.remove(this.key2);

        int count = 0;
        for (final KTypeVTypeCursor<KType, VType> cursor : this.map)
        {
            count++;
            Assert.assertTrue(this.map.containsKey(cursor.key));
            TestUtils.assertEquals2(cursor.value, this.map.get(cursor.key));

            TestUtils.assertEquals2(cursor.value, this.map.values[cursor.index]);
            Assert.assertSame(cursor.key, this.map.keys[cursor.index]);

            Assert.assertTrue(this.map.allocated[cursor.index]);

        }
        Assert.assertEquals(count, this.map.size());

        this.map.clear();
        Assert.assertFalse(this.map.iterator().hasNext());
    }

    /* */
    @Test
    public void testFullLoadFactor()
    {
        this.map = new KTypeVTypeOpenIdentityHashMap<KType, VType>(1, 1f);

        // Fit in the byte key range.
        final int capacity = 0x80;
        final int max = capacity - 2;
        for (int i = 0; i < max; i++)
        {
            this.map.put(cast(i), this.value1);
        }

        // Still not expanded.
        Assert.assertEquals(max, this.map.size());
        Assert.assertEquals(capacity, this.map.keys.length);
        // Won't expand (existing key).
        this.map.put(cast(0), this.value2);
        Assert.assertEquals(capacity, this.map.keys.length);
        // Expanded.
        this.map.put(cast(0xff), this.value2);
        Assert.assertEquals(2 * capacity, this.map.keys.length);
    }

    /* */
    @Test
    public void testBug_HPPC73_FullCapacityGet()
    {
        this.map = new KTypeVTypeOpenIdentityHashMap<KType, VType>(1, 1f);
        final int capacity = 0x80;
        final int max = capacity - 2;
        for (int i = 0; i < max; i++)
        {
            this.map.put(cast(i), this.value1);
        }
        Assert.assertEquals(max, this.map.size());
        Assert.assertEquals(capacity, this.map.keys.length);

        // Non-existent key.
        this.map.remove(cast(max + 1));
        Assert.assertFalse(this.map.containsKey(cast(max + 1)));
        TestUtils.assertEquals2(Intrinsics.defaultVTypeValue(), this.map.get(cast(max + 1)));

        // Should not expand because we're replacing an existing element.
        this.map.put(cast(0), this.value2);
        Assert.assertEquals(max, this.map.size());
        Assert.assertEquals(capacity, this.map.keys.length);

        this.map.putIfAbsent(cast(0), this.value3);
        Assert.assertEquals(max, this.map.size());
        Assert.assertEquals(capacity, this.map.keys.length);

        // Remove from a full map.
        this.map.remove(cast(0));
        Assert.assertEquals(max - 1, this.map.size());
        Assert.assertEquals(capacity, this.map.keys.length);
    }

    /* */
    @Test
    public void testHalfLoadFactor()
    {
        this.map = new KTypeVTypeOpenIdentityHashMap<KType, VType>(1, 0.5f);

        final int capacity = 0x80;
        final int max = capacity - 2;
        for (int i = 0; i < max; i++)
        {
            this.map.put(cast(i), this.value1);
        }

        Assert.assertEquals(max, this.map.size());
        // Still not expanded.
        Assert.assertEquals(2 * capacity, this.map.keys.length);
        // Won't expand (existing key);
        this.map.put(cast(0), this.value2);
        Assert.assertEquals(2 * capacity, this.map.keys.length);
        // Expanded.
        this.map.put(cast(0xff), this.value1);
        Assert.assertEquals(4 * capacity, this.map.keys.length);
    }

    /*! #if ($TemplateOptions.VTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testHashCodeEquals()
    {
        final KTypeVTypeOpenIdentityHashMap<KType, VType> l0 =
                new KTypeVTypeOpenIdentityHashMap<KType, VType>();
        Assert.assertEquals(0, l0.hashCode());
        Assert.assertEquals(l0, new KTypeVTypeOpenIdentityHashMap<KType, VType>());

        final KTypeVTypeOpenIdentityHashMap<KType, VType> l1 = KTypeVTypeOpenIdentityHashMap.from(
                asArrayObjects(this.key1, this.key2, this.key3),
                newvArray(this.value1, this.value2, this.value3));

        final KTypeVTypeOpenIdentityHashMap<KType, VType> l2 = KTypeVTypeOpenIdentityHashMap.from(
                asArrayObjects(this.key2, this.key1, this.key3),
                newvArray(this.value2, this.value1, this.value3));

        final KTypeVTypeOpenIdentityHashMap<KType, VType> l3 = KTypeVTypeOpenIdentityHashMap.from(
                asArrayObjects(this.key1, this.key2),
                newvArray(this.value2, this.value1));

        Assert.assertEquals(l1.hashCode(), l2.hashCode());
        Assert.assertEquals(l1, l2);

        Assert.assertFalse(l1.equals(l3));
        Assert.assertFalse(l2.equals(l3));
    }

    /*! #if ($TemplateOptions.VTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testBug_HPPC37()
    {
        final KTypeVTypeOpenIdentityHashMap<KType, VType> l1 = KTypeVTypeOpenIdentityHashMap.from(
                asArrayObjects(this.key1),
                newvArray(this.value1));

        final KTypeVTypeOpenIdentityHashMap<KType, VType> l2 = KTypeVTypeOpenIdentityHashMap.from(
                asArrayObjects(this.key2),
                newvArray(this.value1));

        Assert.assertFalse(l1.equals(l2));
        Assert.assertFalse(l2.equals(l1));
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testNullKey()
    {
        this.map.put(null, vcast(10));
        TestUtils.assertEquals2(vcast(10), this.map.get(null));
        Assert.assertTrue(this.map.containsKey(null));
        TestUtils.assertEquals2(vcast(10), this.map.lget());
        TestUtils.assertEquals2(null, this.map.lkey());
        this.map.remove(null);
        Assert.assertEquals(0, this.map.size());
    }

    /*! #end !*/

    @Test
    public void testLkey()
    {
        this.map.put(this.key1, vcast(10));
        this.map.put(this.key8, vcast(5));
        this.map.put(this.key5, vcast(5));
        this.map.put(this.key7, vcast(6));
        this.map.put(this.key2, vcast(5));
        this.map.put(this.key4, vcast(32));
        this.map.put(this.key9, vcast(25));

        Assert.assertTrue(this.map.containsKey(this.key1));

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        Assert.assertSame(this.key1, this.map.lkey());
        /*! #end !*/
    }

    /*! #if ($TemplateOptions.VTypeGeneric) !*/
    @Test
    public void testNullValue()
    {
        this.map.put(this.key1, null);
        Assert.assertEquals(null, this.map.get(this.key1));
        Assert.assertTrue(this.map.containsKey(this.key1));
        this.map.remove(this.key1);
        Assert.assertFalse(this.map.containsKey(this.key1));
        Assert.assertEquals(0, this.map.size());
    }

    /*! #end !*/

    @Test
    public void testClone()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value3);

        final KTypeVTypeOpenIdentityHashMap<KType, VType> cloned = this.map.clone();
        cloned.remove(this.key1);

        TestUtils.assertSortedListEqualsByReference(this.map.keys().toArray(), this.key1, this.key2, this.key3);
        TestUtils.assertSortedListEqualsByReference(cloned.keys().toArray(), this.key2, this.key3);
    }

    /*
     * 
     */
    @Test
    public void testToString()
    {
        Assume.assumeTrue(Object[].class.isInstance(this.map.keys) &&
                (!float[].class.isInstance(this.map.values) &&
                        !double[].class.isInstance(this.map.values) &&
                        !char[].class.isInstance(this.map.values)));

        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value2);

        String asString = this.map.toString();
        asString = asString.replaceAll("[^0-9]", "");
        final char[] asCharArray = asString.toCharArray();
        Arrays.sort(asCharArray);
        Assert.assertEquals("1122", new String(asCharArray));
    }


    /* */
    @Test
    public void testMapValues()
    {
        this.map.put(this.key1, this.value3);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value1);
        TestUtils.assertSortedListEquals(this.map.values().toArray(), this.value1, this.value2, this.value3);

        this.map.clear();
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value2);
        TestUtils.assertSortedListEquals(this.map.values().toArray(), this.value1, this.value2, this.value2);
    }

    /* */
    @Test
    public void testMapValuesIterator()
    {
        this.map.put(this.key1, this.value3);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value1);

        int counted = 0;
        for (final KTypeCursor<VType> c : this.map.values())
        {
            TestUtils.assertEquals2(this.map.values[c.index], c.value);
            counted++;
        }
        Assert.assertEquals(counted, this.map.size());
    }

    /* */
    @Test
    public void testMapValuesContainer()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value2);

        // contains()
        for (final KTypeVTypeCursor<KType, VType> c : this.map) {
            Assert.assertTrue(this.map.values().contains(c.value));
        }
        Assert.assertFalse(this.map.values().contains(this.value3));

        Assert.assertEquals(this.map.isEmpty(), this.map.values().isEmpty());
        Assert.assertEquals(this.map.size(), this.map.values().size());

        final KTypeArrayList<VType> values = new KTypeArrayList<VType>();
        this.map.values().forEach(new KTypeProcedure<VType>()
                {
            @Override
            public void apply(final VType value)
            {
                values.add(value);
            }
                });
        TestUtils.assertSortedListEquals(this.map.values().toArray(), this.value1, this.value2, this.value2);

        values.clear();
        this.map.values().forEach(new KTypePredicate<VType>()
                {
            @Override
            public boolean apply(final VType value)
            {
                values.add(value);
                return true;
            }
                });
        TestUtils.assertSortedListEquals(this.map.values().toArray(), this.value1, this.value2, this.value2);
    }


    private KTypeVTypeOpenIdentityHashMap<KType, VType> createMapWithRandomData(final int size, final long randomSeed)
    {

        final Random prng = new Random(randomSeed);

        final KTypeVTypeOpenIdentityHashMap<KType, VType> newMap = KTypeVTypeOpenIdentityHashMap.newInstance(KTypeVTypeOpenIdentityHashMap.DEFAULT_CAPACITY,
                KTypeVTypeOpenIdentityHashMap.DEFAULT_LOAD_FACTOR);

        for (int i = 0; i < size; i++) {

            newMap.put(cast(prng.nextInt()), vcast(prng.nextInt()));
        }

        return newMap;
    }

    @Test
    public void testPooledIteratorForEach()
    {
        //A) Unbroken for-each loop
        //must accommodate even the smallest primitive type
        //so that the iteration do not break before it should...
        final int TEST_SIZE = 126;
        final long TEST_ROUNDS = 5000;

        final KTypeVTypeOpenIdentityHashMap<KType, VType> testContainer = createMapWithOrderedData(TEST_SIZE);
        final KTypeVTypeOpenIdentityHashMap<KType, VType>.KeysContainer keyset = testContainer.keys();
        final KTypeVTypeOpenIdentityHashMap<KType, VType>.ValuesContainer valueset = testContainer.values();

        final long checksum = testContainer.forEach(new KTypeVTypeProcedure<KType, VType>() {

            long count;

            @Override
            public void apply(final KType key, final VType value)
            {

                this.count += vcastType(value);
            }
        }).count;

        long testValue = 0;
        long initialPoolSize = testContainer.entryIteratorPool.size();

        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            //for-each in test :

            //A) Loop on entries
            testValue = 0;
            for (final KTypeVTypeCursor<KType, VType> cursor : testContainer)
            {
                //we consume 1 iterator for this loop
                Assert.assertEquals(initialPoolSize - 1, testContainer.entryIteratorPool.size());

                testValue += vcastType(cursor.value);
            }

            //check checksum the iteration
            Assert.assertEquals(checksum, testValue);

            //iterator is returned to its pool
            Assert.assertEquals(initialPoolSize, testContainer.entryIteratorPool.size());

            //B) Loop on keys == same value as values
            testValue = 0;
            initialPoolSize = keyset.keyIteratorPool.size();

            for (final KTypeCursor<KType> cursor : keyset)
            {
                //we consume 1 iterator for this loop
                Assert.assertEquals(initialPoolSize - 1, keyset.keyIteratorPool.size());

                testValue += castType(cursor.value);
            }

            //check checksum the iteration
            Assert.assertEquals(checksum, testValue);

            //iterator is returned to its pool
            Assert.assertEquals(initialPoolSize, keyset.keyIteratorPool.size());

            //C) Loop on values
            testValue = 0;
            initialPoolSize = valueset.valuesIteratorPool.size();

            for (final KTypeCursor<VType> cursor : valueset)
            {
                //we consume 1 iterator for this loop
                Assert.assertEquals(initialPoolSize - 1, valueset.valuesIteratorPool.size());

                testValue += vcastType(cursor.value);
            }

            //check checksum the iteration
            Assert.assertEquals(checksum, testValue);

            //iterator is returned to its pool
            Assert.assertEquals(initialPoolSize, valueset.valuesIteratorPool.size());

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

        final KTypeVTypeOpenIdentityHashMap<KType, VType> testContainer = createMapWithOrderedData(TEST_SIZE);
        final KTypeVTypeOpenIdentityHashMap<KType, VType>.KeysContainer keyset = testContainer.keys();
        final KTypeVTypeOpenIdentityHashMap<KType, VType>.ValuesContainer valueset = testContainer.values();

        int count = 0;
        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            //A) for-each in test :
            long initialPoolSize = testContainer.entryIteratorPool.size();
            count = 0;
            for (final KTypeVTypeCursor<KType, VType> cursor : testContainer)
            {
                this.guard += vcastType(cursor.value);
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

            //B) Loop on keys
            initialPoolSize = keyset.keyIteratorPool.size();
            count = 0;
            for (final KTypeCursor<KType> cursor : keyset)
            {
                this.guard += castType(cursor.value);
                //we consume 1 iterator for this loop, but reallocs can happen,
                //so we can only say its != initialPoolSize
                Assert.assertTrue(initialPoolSize != keyset.keyIteratorPool.size());

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
            Assert.assertTrue(initialPoolSize != keyset.keyIteratorPool.size());

            //C) Loop on values
            initialPoolSize = valueset.valuesIteratorPool.size();
            count = 0;
            for (final KTypeCursor<VType> cursor : valueset)
            {
                this.guard += vcastType(cursor.value);
                //we consume 1 iterator for this loop, but reallocs can happen,
                //so we can only say its != initialPoolSize
                Assert.assertTrue(initialPoolSize != valueset.valuesIteratorPool.size());

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
            Assert.assertTrue(initialPoolSize != valueset.valuesIteratorPool.size());
        } //end for rounds

        //Due to policy of the Iterator pool, the intended pool never get bigger that some limit
        //despite the Iterator leak.
        Assert.assertTrue(testContainer.entryIteratorPool.capacity() < IteratorPool.getMaxPoolSize() + 1);
        Assert.assertTrue(keyset.keyIteratorPool.capacity() < IteratorPool.getMaxPoolSize() + 1);
        Assert.assertTrue(valueset.valuesIteratorPool.capacity() < IteratorPool.getMaxPoolSize() + 1);
    }

    @Test
    public void testPooledIteratorFullIteratorLoop()
    {
        // for-each loop interrupted

        //must accommodate even the smallest primitive type
        //so that the iteration do not break before it should...
        final int TEST_SIZE = 126;
        final long TEST_ROUNDS = 5000;

        final KTypeVTypeOpenIdentityHashMap<KType, VType> testContainer = createMapWithOrderedData(TEST_SIZE);
        final KTypeVTypeOpenIdentityHashMap<KType, VType>.KeysContainer keyset = testContainer.keys();
        final KTypeVTypeOpenIdentityHashMap<KType, VType>.ValuesContainer valueset = testContainer.values();

        final long checksum = testContainer.forEach(new KTypeVTypeProcedure<KType, VType>() {

            long count;

            @Override
            public void apply(final KType key, final VType value)
            {
                this.count += vcastType(value);
            }
        }).count;

        long testValue = 0;
        final int startingPoolSize = testContainer.entryIteratorPool.size();

        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            //A) Classical iterator loop, with manually allocated Iterator
            int initialPoolSize = testContainer.entryIteratorPool.size();

            final KTypeVTypeOpenIdentityHashMap<KType, VType>.EntryIterator loopIterator = testContainer.iterator();

            Assert.assertEquals(initialPoolSize - 1, testContainer.entryIteratorPool.size());

            testValue = 0;
            while (loopIterator.hasNext())
            {
                testValue += vcastType(loopIterator.next().value);
            } //end IteratorLoop

            //iterator is returned automatically to its pool, by normal iteration termination
            Assert.assertEquals(initialPoolSize, testContainer.entryIteratorPool.size());

            //checksum
            Assert.assertEquals(checksum, testValue);

            //B) Loop on keys
            initialPoolSize = keyset.keyIteratorPool.size();

            final KTypeVTypeOpenIdentityHashMap<KType, VType>.KeysIterator keyLoopIterator = keyset.iterator();

            Assert.assertEquals(initialPoolSize - 1, keyset.keyIteratorPool.size());

            testValue = 0;
            while (keyLoopIterator.hasNext())
            {
                testValue += castType(keyLoopIterator.next().value);
            } //end IteratorLoop

            //iterator is returned automatically to its pool, by normal iteration termination
            Assert.assertEquals(initialPoolSize, keyset.keyIteratorPool.size());

            //checksum
            Assert.assertEquals(checksum, testValue);

            //C) Loop on values
            initialPoolSize = valueset.valuesIteratorPool.size();

            final KTypeVTypeOpenIdentityHashMap<KType, VType>.ValuesIterator valueLoopIterator = valueset.iterator();

            Assert.assertEquals(initialPoolSize - 1, valueset.valuesIteratorPool.size());

            testValue = 0;
            while (valueLoopIterator.hasNext())
            {
                testValue += vcastType(valueLoopIterator.next().value);
            } //end IteratorLoop

            //iterator is returned automatically to its pool, by normal iteration termination
            Assert.assertEquals(initialPoolSize, valueset.valuesIteratorPool.size());

            //checksum
            Assert.assertEquals(checksum, testValue);

        } //end for rounds

        // pool initial size is untouched anyway
        Assert.assertEquals(startingPoolSize, testContainer.entryIteratorPool.size());
        Assert.assertEquals(startingPoolSize, keyset.keyIteratorPool.size());
        Assert.assertEquals(startingPoolSize, valueset.valuesIteratorPool.size());
    }

    @Test
    public void testPooledIteratorBrokenIteratorLoop()
    {
        //for-each loop interrupted

        //must accommodate even the smallest primitive type
        //so that the iteration do not break before it should...
        final int TEST_SIZE = 126;
        final long TEST_ROUNDS = 5000;

        final KTypeVTypeOpenIdentityHashMap<KType, VType> testContainer = createMapWithOrderedData(TEST_SIZE);
        final KTypeVTypeOpenIdentityHashMap<KType, VType>.KeysContainer keyset = testContainer.keys();
        final KTypeVTypeOpenIdentityHashMap<KType, VType>.ValuesContainer valueset = testContainer.values();

        final int startingPoolSize = testContainer.entryIteratorPool.size();

        int count = 0;
        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            //A) Classical iterator loop, with manually allocated Iterator
            long initialPoolSize = testContainer.entryIteratorPool.size();

            final KTypeVTypeOpenIdentityHashMap<KType, VType>.EntryIterator loopIterator = testContainer.iterator();

            Assert.assertEquals(initialPoolSize - 1, testContainer.entryIteratorPool.size());

            count = 0;
            while (loopIterator.hasNext())
            {
                this.guard += vcastType(loopIterator.next().value);

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

            //B) Iterate on keys
            initialPoolSize = keyset.keyIteratorPool.size();

            final KTypeVTypeOpenIdentityHashMap<KType, VType>.KeysIterator keyLoopIterator = keyset.iterator();

            Assert.assertEquals(initialPoolSize - 1, keyset.keyIteratorPool.size());

            count = 0;
            while (keyLoopIterator.hasNext())
            {
                this.guard += castType(keyLoopIterator.next().value);

                //brutally interrupt in the middle
                if (count > TEST_SIZE / 2)
                {
                    break;
                }
                count++;
            } //end IteratorLoop

            //iterator is NOT returned to its pool, due to the break.
            Assert.assertEquals(initialPoolSize - 1, keyset.keyIteratorPool.size());

            //manual return to the pool
            keyLoopIterator.release();

            //now the pool is restored
            Assert.assertEquals(initialPoolSize, keyset.keyIteratorPool.size());

            //C) Iterate on values
            initialPoolSize = valueset.valuesIteratorPool.size();

            final KTypeVTypeOpenIdentityHashMap<KType, VType>.ValuesIterator valueLoopIterator = valueset.iterator();

            Assert.assertEquals(initialPoolSize - 1, valueset.valuesIteratorPool.size());

            count = 0;
            while (valueLoopIterator.hasNext())
            {
                this.guard += vcastType(valueLoopIterator.next().value);

                //brutally interrupt in the middle
                if (count > TEST_SIZE / 2)
                {
                    break;
                }
                count++;
            } //end IteratorLoop

            //iterator is NOT returned to its pool, due to the break.
            Assert.assertEquals(initialPoolSize - 1, valueset.valuesIteratorPool.size());

            //manual return to the pool
            valueLoopIterator.release();

            //now the pool is restored
            Assert.assertEquals(initialPoolSize, valueset.valuesIteratorPool.size());

        } //end for rounds

        // pool initial size is untouched anyway
        Assert.assertEquals(startingPoolSize, testContainer.entryIteratorPool.size());
        Assert.assertEquals(startingPoolSize, keyset.keyIteratorPool.size());
        Assert.assertEquals(startingPoolSize, valueset.valuesIteratorPool.size());
    }

    @Test
    public void testPooledIteratorExceptionIteratorLoop()
    {
        //must accommodate even the smallest primitive type
        //so that the iteration do not break before it should...
        final int TEST_SIZE = 126;
        final long TEST_ROUNDS = 5000;

        final KTypeVTypeOpenIdentityHashMap<KType, VType> testContainer = createMapWithOrderedData(TEST_SIZE);

        final long checksum = testContainer.forEach(new KTypeVTypeProcedure<KType, VType>() {

            long count;

            @Override
            public void apply(final KType key, final VType value)
            {
                this.count += vcastType(value);
            }
        }).count;

        final int startingPoolSize = testContainer.entryIteratorPool.size();

        int count = 0;
        KTypeVTypeOpenIdentityHashMap<KType, VType>.EntryIterator loopIterator = null;

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
                    this.guard += vcastType(loopIterator.next().value);

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

    @Repeat(iterations = 20)
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
        final KTypeVTypeOpenIdentityHashMap<KType, VType> newMap = KTypeVTypeOpenIdentityHashMap.newInstance(PREALLOCATED_SIZE,
                KTypeVTypeOpenIdentityHashMap.DEFAULT_LOAD_FACTOR);

        //3) Add PREALLOCATED_SIZE different values. At the end, size() must be == PREALLOCATED_SIZE,
        //and internal buffer/allocated must not have changed of size
        final int contructorBufferSize = newMap.keys.length;

        Assert.assertEquals(contructorBufferSize, newMap.allocated.length);
        Assert.assertEquals(contructorBufferSize, newMap.values.length);

        for (int i = 0; i < PREALLOCATED_SIZE; i++)
        {
            newMap.put(cast(i), vcast(randomVK.nextInt()));

            //internal size has not changed.
            Assert.assertEquals(contructorBufferSize, newMap.keys.length);
            Assert.assertEquals(contructorBufferSize, newMap.allocated.length);
            Assert.assertEquals(contructorBufferSize, newMap.values.length);
        }

        Assert.assertEquals(PREALLOCATED_SIZE, newMap.size());

    }

    @Test
    public void testForEachProcedureWithException()
    {
        final Random randomVK = new Random(9521455645L);

        //Test that the container do not resize if less that the initial size

        //1) Choose a map to build
        /*! #if ($TemplateOptions.isKType("GENERIC", "int", "long", "float", "double") &&
                $TemplateOptions.isVType("GENERIC", "int", "long", "float", "double")) !*/
        final int NB_ELEMENTS = 2000;
        /*!
        #elseif($TemplateOptions.isKType("short", "char") && $TemplateOptions.isVType("short", "char"))
             final int NB_ELEMENTS = 1000;
            #else
             final int NB_ELEMENTS = 126;
        #end !*/

        final KTypeVTypeOpenIdentityHashMap<Object, VType> newMap = KTypeVTypeOpenIdentityHashMap.newInstance();

        //add a ordered number of key/values pairs (needed for IdentityMap here)
        //use the same value for keys and values to ease later analysis
        for (int i = 0; i < NB_ELEMENTS; i++) {

            final Integer KVpair = i;

            newMap.put(KVpair, vcast(KVpair));
        }

        //List the keys in the reverse-order of the internal buffer, since forEach() is iterating in reverse also:
        final ArrayList<Integer> keyList = new ArrayList<Integer>();
        final ArrayList<Integer> valueList = new ArrayList<Integer>();

        //Test forEach predicate and stop at each key in turn.
        final ArrayList<Integer> keyListTest = new ArrayList<Integer>();
        final ArrayList<Integer> valueListTest = new ArrayList<Integer>();

        for (int k = newMap.allocated.length - 1; k >= 0; k--) {

            if (newMap.allocated[k]) {

                keyList.add((Integer) newMap.keys[k]);
                valueList.add(vcastType(newMap.values[k]));
            }
        }

        final int size = keyList.size();

        for (int i = 0; i < size; i++)
        {
            final int currentPairIndexSizeToIterate = i + 1;

            keyListTest.clear();
            valueListTest.clear();
            keyList.clear();
            valueList.clear();

            for (int k = newMap.allocated.length - 1; k >= 0; k--) {

                if (newMap.allocated[k]) {

                    keyList.add((Integer) newMap.keys[k]);
                    valueList.add(vcastType(newMap.values[k]));
                }
            }

            //A) Run forEach(KTypeVType)
            try
            {
                newMap.forEach(new KTypeVTypeProcedure<Object, VType>() {

                    @Override
                    public void apply(final Object key, final VType value)
                    {
                        keyListTest.add((Integer) key);
                        valueListTest.add(vcastType(value));

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

                //check that keyList/keyListTest and valueList/valueListTest are identical for the first
                //currentPairIndexToIterate + 1 elements
                Assert.assertEquals(currentPairIndexSizeToIterate, keyListTest.size());
                Assert.assertEquals(currentPairIndexSizeToIterate, valueListTest.size());

                for (int j = 0; j < currentPairIndexSizeToIterate; j++)
                {
                    //compare by reference !
                    Assert.assertTrue(keyList.get(j) == keyListTest.get(j));
                    Assert.assertEquals(valueList.get(j), valueListTest.get(j));
                }
            } //end finally

            //B) Run keys().forEach(KType)
            keyListTest.clear();
            valueListTest.clear();
            try
            {
                newMap.keys().forEach(new KTypeProcedure<Object>() {

                    @Override
                    public void apply(final Object key)
                    {
                        keyListTest.add((Integer) key);

                        //retreive value by get() on the map
                        valueListTest.add(vcastType(newMap.get(key)));

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

                //check that keyList/keyListTest and valueList/valueListTest are identical for the first
                //currentPairIndexToIterate + 1 elements
                Assert.assertEquals(currentPairIndexSizeToIterate, keyListTest.size());
                Assert.assertEquals(currentPairIndexSizeToIterate, valueListTest.size());

                for (int j = 0; j < currentPairIndexSizeToIterate; j++)
                {
                    //compare by reference !
                    Assert.assertTrue(keyList.get(j) == keyListTest.get(j));
                    Assert.assertEquals(valueList.get(j), valueListTest.get(j));
                }
            } //end finally

            //C) Run values().forEach(VType) Beware, the values are iterated in-order this time !
            keyListTest.clear();
            valueListTest.clear();
            keyList.clear();
            valueList.clear();

            for (int k = 0; k < newMap.allocated.length; k++) {

                if (newMap.allocated[k]) {

                    keyList.add((Integer) newMap.keys[k]);
                    valueList.add(vcastType(newMap.values[k]));
                }
            }

            try
            {
                newMap.values().forEach(new KTypeProcedure<VType>() {

                    @Override
                    public void apply(final VType value)
                    {
                        valueListTest.add(vcastType(value));

                        //when the stopping key/value pair is encountered, add to list and stop iteration
                        if (vcastType(value) == valueList.get(currentPairIndexSizeToIterate - 1))
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

                //check that  valueList/valueListTest are identical for the first
                //currentPairIndexToIterate + 1 elements
                Assert.assertEquals(currentPairIndexSizeToIterate, valueListTest.size());

                for (int j = 0; j < currentPairIndexSizeToIterate; j++)
                {
                    Assert.assertEquals(valueList.get(j), valueListTest.get(j));
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
        /*! #if ($TemplateOptions.isKType("GENERIC", "int", "long", "float", "double") &&
                $TemplateOptions.isVType("GENERIC", "int", "long", "float", "double")) !*/
        final int NB_ELEMENTS = 2000;
        /*!
        #elseif($TemplateOptions.isKType("short", "char") && $TemplateOptions.isVType("short", "char"))
             final int NB_ELEMENTS = 1000;
            #else
             final int NB_ELEMENTS = 126;
        #end !*/

        final KTypeVTypeOpenIdentityHashMap<Object, VType> newMap = KTypeVTypeOpenIdentityHashMap.newInstance();

        ///add a ordered number of key/values pairs (needed for IdentityMap here)
        //use the same value for keys and values to ease later analysis
        for (int i = 0; i < NB_ELEMENTS; i++) {

            final Integer KVpair = i;

            newMap.put(KVpair, vcast(KVpair));
        }

        //List the keys in the reverse-order of the internal buffer, since forEach() is iterating in reverse also:
        final ArrayList<Integer> keyList = new ArrayList<Integer>();
        final ArrayList<Integer> valueList = new ArrayList<Integer>();

        for (int i = newMap.allocated.length - 1; i >= 0; i--) {

            if (newMap.allocated[i]) {

                keyList.add((Integer) newMap.keys[i]);
                valueList.add(vcastType(newMap.values[i]));
            }
        }

        //Test forEach predicate and stop at each key in turn.
        final ArrayList<Integer> keyListTest = new ArrayList<Integer>();
        final ArrayList<Integer> valueListTest = new ArrayList<Integer>();

        keyListTest.clear();
        valueListTest.clear();

        //A) Run forEach(KTypeVType)

        newMap.forEach(new KTypeVTypeProcedure<Object, VType>() {

            @Override
            public void apply(final Object key, final VType value)
            {
                keyListTest.add((Integer) key);
                valueListTest.add(vcastType(value));
            }
        });

        //check that keyList/keyListTest and valueList/valueListTest are identical.
        Assert.assertEquals(keyList.size(), keyListTest.size());

        for (int j = 0; j < keyList.size(); j++)
        {
            //compare by reference !
            Assert.assertTrue(keyList.get(j) == keyListTest.get(j));
        }

        Assert.assertEquals(valueList, valueListTest);

        //B) Run keys().forEach(KType)
        keyListTest.clear();
        valueListTest.clear();

        newMap.keys().forEach(new KTypeProcedure<Object>() {

            @Override
            public void apply(final Object key)
            {
                keyListTest.add((Integer) key);
                //retreive value by get() on the map
                valueListTest.add(vcastType(newMap.get(key)));
            }
        });

        //check that keyList/keyListTest and valueList/valueListTest are identical .
        Assert.assertEquals(keyList.size(), keyListTest.size());

        for (int j = 0; j < keyList.size(); j++)
        {
            //Compare by reference !
            Assert.assertTrue(keyList.get(j) == keyListTest.get(j));
        }

        Assert.assertEquals(valueList, valueListTest);

        //C) Run values().forEach(VType) : Beware, they are iterated in-order !
        keyListTest.clear();
        valueListTest.clear();

        keyList.clear();
        valueList.clear();

        for (int k = 0; k < newMap.allocated.length; k++) {

            if (newMap.allocated[k]) {

                keyList.add((Integer) newMap.keys[k]);
                valueList.add(vcastType(newMap.values[k]));
            }
        }

        newMap.values().forEach(new KTypeProcedure<VType>() {

            @Override
            public void apply(final VType value)
            {
                valueListTest.add(vcastType(value));
            }
        });

        //check that  valueList/valueListTest are identical .
        Assert.assertEquals(valueList, valueListTest);
    }

    @Test
    public void testForEachPredicate()
    {
        final Random randomVK = new Random(9521455645L);

        //Test that the container do not resize if less that the initial size

        //1) Choose a map to build
        /*! #if ($TemplateOptions.isKType("GENERIC", "int", "long", "float", "double") &&
                $TemplateOptions.isVType("GENERIC", "int", "long", "float", "double")) !*/
        final int NB_ELEMENTS = 2000;
        /*!
        #elseif($TemplateOptions.isKType("short", "char") && $TemplateOptions.isVType("short", "char"))
             final int NB_ELEMENTS = 1000;
            #else
             final int NB_ELEMENTS = 126;
        #end !*/

        final KTypeVTypeOpenIdentityHashMap<Object, VType> newMap = KTypeVTypeOpenIdentityHashMap.newInstance();

        //add a ordered number of key/values pairs (needed for IdentityMap here)
        //use the same value for keys and values to ease later analysis
        for (int i = 0; i < NB_ELEMENTS; i++) {

            final Integer KVpair = i;

            newMap.put(KVpair, vcast(KVpair));
        }

        //List the keys in the reverse-order of the internal buffer, since forEach() is iterating in reverse also:
        final ArrayList<Integer> keyList = new ArrayList<Integer>();
        final ArrayList<Integer> valueList = new ArrayList<Integer>();

        //Test forEach predicate and stop at each key in turn.
        final ArrayList<Integer> keyListTest = new ArrayList<Integer>();
        final ArrayList<Integer> valueListTest = new ArrayList<Integer>();

        for (int k = newMap.allocated.length - 1; k >= 0; k--) {

            if (newMap.allocated[k]) {

                keyList.add((Integer) newMap.keys[k]);
                valueList.add(vcastType(newMap.values[k]));
            }
        }

        final int size = keyList.size();

        for (int i = 0; i < size; i++)
        {
            final int currentPairIndexSizeToIterate = i + 1;

            keyListTest.clear();
            valueListTest.clear();
            keyList.clear();
            valueList.clear();

            for (int k = newMap.allocated.length - 1; k >= 0; k--) {

                if (newMap.allocated[k]) {

                    keyList.add((Integer) newMap.keys[k]);
                    valueList.add(vcastType(newMap.values[k]));
                }
            }

            //A) Run forEach(KTypeVType)

            newMap.forEach(new KTypeVTypePredicate<Object, VType>() {

                @Override
                public boolean apply(final Object key, final VType value)
                {
                    keyListTest.add((Integer) key);
                    valueListTest.add(vcastType(value));

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
            Assert.assertEquals(currentPairIndexSizeToIterate, valueListTest.size());

            for (int j = 0; j < currentPairIndexSizeToIterate; j++)
            {
                //compare by reference !
                Assert.assertTrue(keyList.get(j) == keyListTest.get(j));
                Assert.assertEquals(valueList.get(j), valueListTest.get(j));
            }

            //B) Run keys().forEach(KType)
            keyListTest.clear();
            valueListTest.clear();

            newMap.keys().forEach(new KTypePredicate<Object>() {

                @Override
                public boolean apply(final Object key)
                {
                    keyListTest.add((Integer) key);

                    //Retrieve value by get() on the map
                    valueListTest.add(vcastType(newMap.get(key)));

                    //when the stopping key/value pair is encountered, add to list and stop iteration
                    if (key == keyList.get(currentPairIndexSizeToIterate - 1))
                    {
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

            for (int j = 0; j < currentPairIndexSizeToIterate; j++)
            {
                //compare by reference
                Assert.assertTrue(keyList.get(j) == keyListTest.get(j));
                Assert.assertEquals(valueList.get(j), valueListTest.get(j));
            }

            //C) Run values().forEach(VType) : Beware, values are iterated in-order !
            keyListTest.clear();
            valueListTest.clear();
            keyList.clear();
            valueList.clear();

            for (int k = 0; k < newMap.allocated.length; k++) {

                if (newMap.allocated[k]) {

                    keyList.add((Integer) newMap.keys[k]);
                    valueList.add(vcastType(newMap.values[k]));
                }
            }

            newMap.values().forEach(new KTypePredicate<VType>() {

                @Override
                public boolean apply(final VType value)
                {
                    valueListTest.add(vcastType(value));

                    //when the stopping key/value pair is encountered, add to list and stop iteration
                    if (vcastType(value) == valueList.get(currentPairIndexSizeToIterate - 1))
                    {
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

            for (int j = 0; j < currentPairIndexSizeToIterate; j++)
            {
                Assert.assertEquals(valueList.get(j), valueListTest.get(j));
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

        final KTypeVTypeOpenIdentityHashMap<Object, VType> newMap = KTypeVTypeOpenIdentityHashMap.newInstance();

        Assert.assertEquals(0, newMap.size());

        //A) fill
        for (int i = 0; i < NB_ELEMENTS; i++) {

            final Object newObject = new IntHolder(0xAF);

            Assert.assertTrue(Intrinsics.<VType> defaultVTypeValue() == newMap.put(newObject, vcast(i)));

            //Equals key, but not the same object
            Assert.assertFalse(newMap.containsKey(new IntHolder(0xAF)));
            Assert.assertTrue(newMap.get(new IntHolder(0xAF)) == Intrinsics.<VType> defaultVTypeValue());

            //Really the same object
            Assert.assertEquals(i, vcastType(newMap.get(newObject)));

            Assert.assertTrue(newMap.containsKey(newObject));

            //lkey() really return the previously stored object
            Assert.assertSame(newObject, newMap.lkey());

            Assert.assertEquals(i, vcastType(newMap.lget()));
        } //end for

        //objects are all different, so size is really NB_ELEMENTS
        Assert.assertEquals(NB_ELEMENTS, newMap.size());
    }

    private KTypeVTypeOpenIdentityHashMap<KType, VType> createMapWithOrderedData(final int size)
    {
        final KTypeVTypeOpenIdentityHashMap<KType, VType> newMap = KTypeVTypeOpenIdentityHashMap.newInstance(KTypeVTypeOpenIdentityHashMap.DEFAULT_CAPACITY,
                KTypeVTypeOpenIdentityHashMap.DEFAULT_LOAD_FACTOR);

        for (int i = 0; i < size; i++)
        {
            newMap.put(cast(i), vcast(i));
        }

        return newMap;
    }
}