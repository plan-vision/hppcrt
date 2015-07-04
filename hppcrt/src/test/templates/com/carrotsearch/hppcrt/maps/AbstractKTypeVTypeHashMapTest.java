package com.carrotsearch.hppcrt.maps;

import java.util.*;

import org.junit.*;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.lists.*;
import com.carrotsearch.hppcrt.TestUtils;
import com.carrotsearch.hppcrt.cursors.*;
import com.carrotsearch.hppcrt.predicates.*;
import com.carrotsearch.hppcrt.procedures.*;
import com.carrotsearch.hppcrt.sets.*;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.*;

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
/**
 * Tests common for all kinds of hash maps {@link KTypeVTypeMap}.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public abstract class AbstractKTypeVTypeHashMapTest<KType, VType> extends AbstractKTypeVTypeTest<KType, VType>
{
    @BeforeClass
    public static void configure()
    {
        IteratorPool.configureInitialPoolSize(8);
    }

    /**
     * Customize this for concrete hash map creation
     * @param initialCapacity
     * @param loadFactor
     * @param strategy
     * @return
     */
    protected abstract KTypeVTypeMap<KType, VType> createNewMapInstance(final int initialCapacity,
            final double loadFactor);

    protected abstract KType[] getKeys(KTypeVTypeMap<KType, VType> testMap);

    protected abstract VType[] getValues(KTypeVTypeMap<KType, VType> testMap);

    protected abstract boolean isAllocatedDefaultKey(KTypeVTypeMap<KType, VType> testMap);

    protected abstract VType getAllocatedDefaultKeyValue(KTypeVTypeMap<KType, VType> testMap);

    protected abstract KTypeVTypeMap<KType, VType> getClone(KTypeVTypeMap<KType, VType> testMap);

    protected abstract KTypeVTypeMap<KType, VType> getFrom(KTypeVTypeMap<KType, VType> testMap);

    protected abstract KTypeVTypeMap<KType, VType> getFromArrays(KType[] keys, VType[] values);

    protected abstract KTypeVTypeMap<KType, VType> getCopyConstructor(KTypeVTypeMap<KType, VType> testMap);

    abstract int getEntryPoolSize(KTypeVTypeMap<KType, VType> testMap);

    abstract int getKeysPoolSize(KTypeCollection<KType> testMap);

    abstract int getValuesPoolSize(KTypeCollection<VType> values);

    abstract int getEntryPoolCapacity(KTypeVTypeMap<KType, VType> testMap);

    abstract int getKeysPoolCapacity(KTypeCollection<KType> keys);

    abstract int getValuesPoolCapacity(KTypeCollection<VType> values);

    /**
     * Per-test fresh initialized instance.
     */
    protected KTypeVTypeMap<KType, VType> map;

    @Before
    public void initialize() {

        this.map = createNewMapInstance();
    }

    protected KTypeVTypeMap<KType, VType> createNewMapInstance() {

        //use the Max load factor to assure to trigger all the code paths
        return createNewMapInstance(0, HashContainers.MAX_LOAD_FACTOR);
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

            final int mask = this.getKeys(this.map).length - 1;

            for (int i = 0; i < this.getKeys(this.map).length; i++)
            {
                if (!is_allocated(i, Intrinsics.<KType[]> cast(this.getKeys(this.map))))
                {
                    //if not allocated, generic version if patched to null for GC sake

                    /*! #if ($TemplateOptions.KTypeGeneric ) !*/
                    TestUtils.assertEquals2(this.keyE, this.getKeys(this.map)[i]);
                    /*! #end !*/
                    /*! #if ($TemplateOptions.VTypeGeneric) !*/
                    TestUtils.assertEquals2(Intrinsics.<VType> empty(), this.getValues(this.map)[i]);
                    /*! #end !*/
                }
                else
                {
                    //try to reach the key by contains()
                    Assert.assertTrue(this.map.containsKey(Intrinsics.<KType> cast(this.getKeys(this.map)[i])));

                    //get() test
                    Assert.assertEquals(vcastType(Intrinsics.<VType> cast(this.getValues(this.map)[i])),
                            vcastType(this.map.get(Intrinsics.<KType> cast(this.getKeys(this.map)[i]))));

                    occupied++;
                }
            }

            if (this.isAllocatedDefaultKey(this.map)) {

                //try to reach the key by contains()
                Assert.assertTrue(this.map.containsKey(this.keyE));

                //get() test
                Assert.assertEquals(vcastType(this.getAllocatedDefaultKeyValue(this.map)), vcastType(this.map.get(this.keyE)));

                occupied++;
            }

            Assert.assertEquals(occupied, this.map.size());
        }
    }

    /* */
    @Test
    public void testCloningConstructor()
    {
        this.map.put(this.keyE, this.value0);
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value3);

        assertSameMap(this.map, getFrom(this.map));
        assertSameMap(this.map, getCopyConstructor(this.map));

        this.map.put(Intrinsics.<KType> empty(), this.value7);
        assertSameMap(this.map, getFrom(this.map));
        assertSameMap(this.map, getCopyConstructor(this.map));
    }

    /* */
    /*! #if ($TemplateOptions.VTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testFromArrays()
    {
        this.map.put(this.keyE, this.value0);
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value3);

        final KTypeVTypeMap<KType, VType> map2 = getFromArrays(
                newArray(this.keyE, this.key1, this.key2, this.key3),
                newvArray(this.value0, this.value1, this.value2, this.value3));

        assertSameMap(this.map, map2);
    }

    /* */
    @Test
    public void testPut()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.keyE, this.value0);

        Assert.assertTrue(this.map.containsKey(this.key1));

        TestUtils.assertEquals2(this.value1, this.map.get(this.key1));

        Assert.assertTrue(this.map.containsKey(this.keyE));

        TestUtils.assertEquals2(this.value0, this.map.get(this.keyE));

    }

    /* */
    @Test
    public void testPutOverExistingKey()
    {
        this.map.put(this.key1, this.value1);
        TestUtils.assertEquals2(this.value1, this.map.put(this.key1, this.value3));
        TestUtils.assertEquals2(this.value3, this.map.get(this.key1));

        this.map.put(this.keyE, this.value0);
        TestUtils.assertEquals2(this.value0, this.map.put(this.keyE, this.value4));
        TestUtils.assertEquals2(this.value4, this.map.get(this.keyE));
    }

    /* */
    @Test
    public void testPutWithExpansions()
    {
        final int COUNT = 10000;
        final Random rnd = new Random(0xDEADBEEF);
        final HashSet<Object> values = new HashSet<Object>();

        for (int i = 0; i < COUNT; i++)
        {
            final int v = rnd.nextInt();
            values.add(cast(v));

            final boolean hadKey = values.contains(cast(v));
            Assert.assertTrue(hadKey);

            this.map.put(cast(v), vcast(v));
            Assert.assertEquals(hadKey, this.map.containsKey(cast(v)));

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
        this.map.put(this.keyE, this.value0);

        final KTypeVTypeMap<KType, VType> map2 =
                createNewMapInstance(0, 0.75);

        map2.put(this.key2, this.value2);
        map2.put(this.key3, this.value1);

        // One new key (key3).
        Assert.assertEquals(1, this.map.putAll(map2));

        // Assert the value under key2 has been replaced.
        TestUtils.assertEquals2(this.value2, this.map.get(this.key2));

        // And key3 has been added.
        TestUtils.assertEquals2(this.value1, this.map.get(this.key3));
        Assert.assertEquals(4, this.map.size());
    }

    /* */
    @Test
    public void testPutIfAbsent()
    {
        Assert.assertTrue(this.map.putIfAbsent(this.key1, this.value1));
        Assert.assertFalse(this.map.putIfAbsent(this.key1, this.value2));
        TestUtils.assertEquals2(this.value1, this.map.get(this.key1));
        Assert.assertTrue(this.map.putIfAbsent(this.keyE, this.value0));
        Assert.assertFalse(this.map.putIfAbsent(this.keyE, this.value3));
        TestUtils.assertEquals2(this.value0, this.map.get(this.keyE));

    }

    /*! #if ($TemplateOptions.VTypePrimitive) !*/
    @Test
    public void testPutOrAdd()
    {
        TestUtils.assertEquals2(this.value3, this.map.putOrAdd(this.key1, this.value3, this.value2));
        TestUtils.assertEquals2(vcast(3 + 2), this.map.putOrAdd(this.key1, this.value7, this.value2));

        TestUtils.assertEquals2(this.value4, this.map.putOrAdd(this.keyE, this.value4, this.value2));
        TestUtils.assertEquals2(vcast(4 + 2), this.map.putOrAdd(this.keyE, this.value5, this.value2));

        //trigger reallocs
        for (int i = 2; i < 126; i++) {

            final KType keyRef = cast(i);

            //force to reference the same key because of identity tests
            TestUtils.assertEquals2("i = " + i, this.value3, this.map.putOrAdd(keyRef, this.value3, this.value6));
            TestUtils.assertEquals2("i = " + i, vcast(3 + 5), this.map.putOrAdd(keyRef, this.value7, this.value5));
        }
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.VTypePrimitive) !*/
    @Test
    public void testAddTo()
    {
        TestUtils.assertEquals2(this.value3, this.map.addTo(this.key1, this.value3));
        TestUtils.assertEquals2(vcast(3 + 2), this.map.addTo(this.key1, this.value2));

        TestUtils.assertEquals2(this.value3, this.map.addTo(this.keyE, this.value3));
        TestUtils.assertEquals2(vcast(3 + 2), this.map.addTo(this.keyE, this.value2));
    }

    /*! #end !*/

    /* */
    @Test
    public void testRemove()
    {
        this.map.put(this.keyE, this.value7);
        this.map.put(this.key1, this.value1);
        TestUtils.assertEquals2(this.value1, this.map.remove(this.key1));
        TestUtils.assertEquals2(this.map.getDefaultValue(), this.map.remove(this.key1));
        Assert.assertEquals(1, this.map.size());

        TestUtils.assertEquals2(this.value7, this.map.remove(this.keyE));
        TestUtils.assertEquals2(this.map.getDefaultValue(), this.map.remove(this.keyE));
        Assert.assertEquals(0, this.map.size());
    }

    /* */
    @Test
    public void testRemoveAllWithContainer()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value1);
        this.map.put(this.key3, this.value1);
        this.map.put(this.keyE, this.value0);

        final KTypeArrayList<KType> list2 = KTypeArrayList.newInstance();
        list2.add(newArray(this.key2, this.key3, this.key4));

        this.map.removeAll(list2);
        Assert.assertEquals(2, this.map.size());
        Assert.assertTrue(this.map.containsKey(this.key1));
        Assert.assertTrue(this.map.containsKey(this.keyE));
    }

    /* */
    @Test
    public void testRemoveAllWithLookupContainer()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value1);
        this.map.put(this.key3, this.value1);
        this.map.put(this.keyE, this.value0);

        final KTypeHashSet<KType> set = KTypeHashSet.newInstance();
        set.add(this.key2, this.key3, this.key4, this.key5, this.key6);

        //to cover the Lookup code branch, set must be bigger than map !
        Assert.assertTrue(set.size() > this.map.size());

        this.map.removeAll(set);
        Assert.assertEquals(2, this.map.size());
        Assert.assertTrue(this.map.containsKey(this.key1));
        Assert.assertTrue(this.map.containsKey(this.keyE));
    }

    /* */
    @Test
    public void testRemoveAllWithContainer2()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value1);
        this.map.put(this.key3, this.value1);
        this.map.put(this.keyE, this.value0);

        final KTypeArrayList<KType> list2 = KTypeArrayList.newInstance();
        list2.add(newArray(this.keyE, this.key3, this.key4));

        this.map.removeAll(list2);
        Assert.assertEquals(2, this.map.size());
        Assert.assertTrue(this.map.containsKey(this.key1));
        Assert.assertTrue(this.map.containsKey(this.key2));
    }

    /* */
    @Test
    public void testRemoveAllWithLookupContainer2()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value1);
        this.map.put(this.key3, this.value1);
        this.map.put(this.keyE, this.value0);

        final KTypeHashSet<KType> set = KTypeHashSet.newInstance();
        set.add(newArray(this.keyE, this.key3, this.key4, this.key5, this.key6));

        //to cover the Lookup code branch, set must be bigger than map !
        Assert.assertTrue(set.size() > this.map.size());

        this.map.removeAll(set);
        Assert.assertEquals(2, this.map.size());
        Assert.assertTrue(this.map.containsKey(this.key1));
        Assert.assertTrue(this.map.containsKey(this.key2));
    }

    /* */
    @Test
    public void testRemoveAllWithPredicate()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value1);
        this.map.put(this.key3, this.value1);
        this.map.put(this.keyE, this.value0);

        this.map.removeAll(new KTypePredicate<KType>()
        {
            @Override
            public boolean apply(final KType value)
            {
                return value == AbstractKTypeVTypeHashMapTest.this.key2 || value == AbstractKTypeVTypeHashMapTest.this.key3;
            }
        });
        Assert.assertEquals(2, this.map.size());
        Assert.assertTrue(this.map.containsKey(this.key1));
        Assert.assertTrue(this.map.containsKey(this.keyE));
    }

    /* */
    @Test
    public void testRemoveAllWithPredicate2()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value1);
        this.map.put(this.key3, this.value1);
        this.map.put(this.keyE, this.value0);

        this.map.removeAll(new KTypePredicate<KType>()
        {
            @Override
            public boolean apply(final KType value)
            {
                return value == AbstractKTypeVTypeHashMapTest.this.keyE || value == AbstractKTypeVTypeHashMapTest.this.key1;
            }
        });
        Assert.assertEquals(2, this.map.size());
        Assert.assertTrue(this.map.containsKey(this.key2));
        Assert.assertTrue(this.map.containsKey(this.key3));
    }

    /* */
    @Test
    public void testRemoveAllWithPredicateInterrupted()
    {
        this.map.put(this.keyE, this.value1);
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
                    if (key == AbstractKTypeVTypeHashMapTest.this.key7) {
                        throw t;
                    }
                    return key == AbstractKTypeVTypeHashMapTest.this.key2 || key == AbstractKTypeVTypeHashMapTest.this.key9 || key == AbstractKTypeVTypeHashMapTest.this.key5;
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

        Assert.assertTrue(this.map.containsKey(this.key7));
    }

    /* */
    @Test
    public void testRemoveAllWithKeyValuesPredicate()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value2); //removed by key
        this.map.put(this.key3, this.value3); //removed by key
        this.map.put(this.keyE, this.value0);
        this.map.put(this.key4, this.value4);
        this.map.put(this.key5, this.value5); //removed by value
        this.map.put(this.key6, this.value6);

        this.map.removeAll(new KTypeVTypePredicate<KType, VType>()
        {
            @Override
            public boolean apply(final KType key, final VType value)
            {
                return key == AbstractKTypeVTypeHashMapTest.this.key2 || key == AbstractKTypeVTypeHashMapTest.this.key3 ||
                        value == AbstractKTypeVTypeHashMapTest.this.value5;
            }
        });

        Assert.assertEquals(4, this.map.size());
        Assert.assertTrue(this.map.containsKey(this.key1));
        Assert.assertTrue(this.map.containsKey(this.keyE));
        Assert.assertTrue(this.map.containsKey(this.key4));
        Assert.assertTrue(this.map.containsKey(this.key6));
    }

    /* */
    @Test
    public void testRemoveAllWithKeyValuesPredicate2()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value2); //removed by key
        this.map.put(this.key3, this.value3);
        this.map.put(this.keyE, this.value0); //removed by key
        this.map.put(this.key4, this.value4);
        this.map.put(this.key5, this.value5);
        this.map.put(this.key6, this.value6);//removed by value

        this.map.removeAll(new KTypeVTypePredicate<KType, VType>()
        {
            @Override
            public boolean apply(final KType key, final VType value)
            {
                return key == AbstractKTypeVTypeHashMapTest.this.key2 || key == AbstractKTypeVTypeHashMapTest.this.keyE ||
                        value == AbstractKTypeVTypeHashMapTest.this.value6;
            }
        });

        Assert.assertEquals(4, this.map.size());
        Assert.assertTrue(this.map.containsKey(this.key1));
        Assert.assertTrue(this.map.containsKey(this.key3));
        Assert.assertTrue(this.map.containsKey(this.key4));
        Assert.assertTrue(this.map.containsKey(this.key5));
    }

    /* */
    @Test
    public void testRemoveAllViaKeySetView()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value1);
        this.map.put(this.key3, this.value1);
        this.map.put(this.keyE, this.value0);

        this.map.keys().removeAll(new KTypePredicate<KType>()
        {
            @Override
            public boolean apply(final KType value)
            {
                return value == AbstractKTypeVTypeHashMapTest.this.key2 || value == AbstractKTypeVTypeHashMapTest.this.key3;
            }
        });
        Assert.assertEquals(2, this.map.size());
        Assert.assertTrue(this.map.containsKey(this.key1));
        Assert.assertTrue(this.map.containsKey(this.keyE));
    }

    /* */
    @Test
    public void testRemoveAllViaKeySetView2()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value1);
        this.map.put(this.key3, this.value1);
        this.map.put(this.keyE, this.value0);

        this.map.keys().removeAll(new KTypePredicate<KType>()
        {
            @Override
            public boolean apply(final KType value)
            {
                return value == AbstractKTypeVTypeHashMapTest.this.keyE || value == AbstractKTypeVTypeHashMapTest.this.key1;
            }
        });
        Assert.assertEquals(2, this.map.size());
        Assert.assertTrue(this.map.containsKey(this.key2));
        Assert.assertTrue(this.map.containsKey(this.key3));
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
        this.map.put(this.keyE, this.value0);

        final int nbRemoved = this.map.values().removeAll(new KTypePredicate<VType>()
        {
            @Override
            public boolean apply(final VType value)
            {
                return value == AbstractKTypeVTypeHashMapTest.this.value1 || value == AbstractKTypeVTypeHashMapTest.this.value2;
            }
        });

        Assert.assertEquals(5, nbRemoved);
        Assert.assertEquals(5, this.map.size());
        Assert.assertTrue(this.map.containsKey(this.key4));
        Assert.assertTrue(this.map.containsKey(this.key5));
        Assert.assertTrue(this.map.containsKey(this.key6));
        Assert.assertTrue(this.map.containsKey(this.key8));
        Assert.assertTrue(this.map.containsKey(this.keyE));
    }

    /* */
    @Test
    public void testRemoveAllViaValueSetView2()
    {
        this.map.put(this.keyE, this.value1); //del
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
                return value == AbstractKTypeVTypeHashMapTest.this.value1 || value == AbstractKTypeVTypeHashMapTest.this.value2;
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
        this.map.put(this.key5, this.value7);
        this.map.put(this.key6, this.value5);
        this.map.put(this.key7, this.value1); //del
        this.map.put(this.key8, this.value8);
        this.map.put(this.key9, this.value2);
        this.map.put(this.keyE, this.value0);

        final int nbRemoved = this.map.values().removeAll(this.value1);

        Assert.assertEquals(3, nbRemoved);
        Assert.assertEquals(7, this.map.size());
        Assert.assertTrue(this.map.containsKey(this.key2));
        Assert.assertTrue(this.map.containsKey(this.key4));
        Assert.assertTrue(this.map.containsKey(this.key5));
        Assert.assertTrue(this.map.containsKey(this.key6));
        Assert.assertTrue(this.map.containsKey(this.key8));
        Assert.assertTrue(this.map.containsKey(this.key9));
        Assert.assertTrue(this.map.containsKey(this.keyE));
    }

    /* */
    @Test
    public void testRemoveAllOccurencesViaValueSetView2()
    {
        this.map.put(this.keyE, this.value1); //del
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value1); //del
        this.map.put(this.key4, this.value3);
        this.map.put(this.key5, this.value7);
        this.map.put(this.key6, this.value5);
        this.map.put(this.key7, this.value1); //del
        this.map.put(this.key8, this.value8);
        this.map.put(this.key9, this.value2);

        final int nbRemoved = this.map.values().removeAll(this.value1);

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
    public void testContainsValueViaValueSetView()
    {
        this.map.put(this.keyE, this.value1);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value1);
        this.map.put(this.key4, this.value3);
        this.map.put(this.key5, this.value7);
        this.map.put(this.key6, this.value5);
        this.map.put(this.key7, this.value1);
        this.map.put(this.key8, this.value8);
        this.map.put(this.key9, this.value2);

        Assert.assertTrue(this.map.values().contains(this.value1));
        Assert.assertTrue(this.map.values().contains(this.value2));
        Assert.assertTrue(this.map.values().contains(this.value3));
        Assert.assertFalse(this.map.values().contains(this.value4));
        Assert.assertTrue(this.map.values().contains(this.value5));
        Assert.assertFalse(this.map.values().contains(this.value6));
        Assert.assertTrue(this.map.values().contains(this.value7));
        Assert.assertTrue(this.map.values().contains(this.value8));
        Assert.assertFalse(this.map.values().contains(this.value9));
    }

    /* */
    @Test
    public void testMapsIntersection()
    {
        final KTypeVTypeMap<KType, VType> map2 =
                createNewMapInstance();

        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value1);
        this.map.put(this.key3, this.value1);

        map2.put(this.key2, this.value1);
        map2.put(this.keyE, this.value0);
        map2.put(this.key4, this.value1);

        //Indeed, keys are also KTypeLookupContainer<KType>

        Assert.assertEquals(2, this.map.keys().retainAll((KTypeLookupContainer<KType>) map2.keys()));

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

        TestUtils.assertSortedListEquals(this.map.keys().toArray(), this.key1, this.key2, this.key3);

        this.map.clear();

        this.map.put(this.keyE, this.value0);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value1);

        TestUtils.assertSortedListEquals(this.map.keys().toArray(), this.keyE, this.key2, this.key3);
    }

    /* */
    @Test
    public void testMapKeySetIterator()
    {
        this.map.put(this.key1, this.value3);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value1);

        int counted = 0;
        for (final KTypeCursor<KType> cursor : this.map.keys())
        {
            if (cursor.index == this.getKeys(this.map).length) {

                TestUtils.assertEquals2(this.keyE, cursor.value);
                counted++;
                continue;
            }

            TestUtils.assertEquals2(this.getKeys(this.map)[cursor.index], cursor.value);
            counted++;
        }
        Assert.assertEquals(counted, this.map.size());

        this.map.clear();

        this.map.put(this.key1, this.value3);
        this.map.put(this.keyE, this.value2);
        this.map.put(this.key3, this.value1);

        counted = 0;
        for (final KTypeCursor<KType> cursor : this.map.keys())
        {
            if (cursor.index == this.getKeys(this.map).length) {

                TestUtils.assertEquals2(this.keyE, cursor.value);
                counted++;
                continue;
            }

            TestUtils.assertEquals2(this.getKeys(this.map)[cursor.index], cursor.value);
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

        // Check if the map behaves properly upon subsequent use.
        testPutWithExpansions();

        this.map.put(this.keyE, this.value0);
        this.map.put(this.key2, this.value1);
        this.map.clear();
        Assert.assertEquals(0, this.map.size());

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

        // Check if the map behaves properly upon subsequent use.
        testPutWithExpansions();
    }

    /* */
    @Test
    public void testIterable()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value3);
        this.map.put(this.key5, this.value5);
        this.map.put(this.key6, this.value7);
        this.map.put(this.key7, this.value9);
        this.map.remove(this.key2);

        int count = 0;
        for (final KTypeVTypeCursor<KType, VType> cursor : this.map)
        {
            if (cursor.index == this.getKeys(this.map).length) {

                Assert.assertTrue(this.isAllocatedDefaultKey(this.map));
                TestUtils.assertEquals2(this.keyE, cursor.key);
                TestUtils.assertEquals2(this.getAllocatedDefaultKeyValue(this.map), cursor.value);
                Assert.assertTrue(this.map.containsKey(cursor.key));
                TestUtils.assertEquals2(cursor.value, this.map.get(cursor.key));
                count++;
                continue;
            }

            Assert.assertTrue(this.map.containsKey(cursor.key));
            TestUtils.assertEquals2(cursor.value, this.map.get(cursor.key));

            TestUtils.assertEquals2(cursor.value, this.getValues(this.map)[cursor.index]);
            TestUtils.assertEquals2(cursor.key, this.getKeys(this.map)[cursor.index]);
            count++;
        }
        Assert.assertEquals(count, this.map.size());

        //Another try !
        this.map.clear();
        Assert.assertFalse(this.map.iterator().hasNext());

        this.map.put(this.key8, this.value2);
        this.map.put(this.key2, this.value3);
        this.map.put(this.key5, this.value2);
        this.map.put(this.keyE, this.value1);
        this.map.put(this.key4, this.value3);
        this.map.put(this.key3, this.value2);
        this.map.put(this.key2, this.value9);
        this.map.remove(this.key2);

        count = 0;
        for (final KTypeVTypeCursor<KType, VType> cursor : this.map)
        {
            if (cursor.index == this.getKeys(this.map).length) {

                Assert.assertTrue(this.isAllocatedDefaultKey(this.map));
                TestUtils.assertEquals2(this.keyE, cursor.key);
                TestUtils.assertEquals2(this.getAllocatedDefaultKeyValue(this.map), cursor.value);
                Assert.assertTrue(this.map.containsKey(cursor.key));
                TestUtils.assertEquals2(cursor.value, this.map.get(cursor.key));
                count++;
                continue;
            }

            Assert.assertTrue(this.map.containsKey(cursor.key));
            TestUtils.assertEquals2(cursor.value, this.map.get(cursor.key));

            TestUtils.assertEquals2(cursor.value, this.getValues(this.map)[cursor.index]);
            TestUtils.assertEquals2(cursor.key, this.getKeys(this.map)[cursor.index]);
            count++;
        }
        Assert.assertEquals(count, this.map.size());

        this.map.clear();
        Assert.assertFalse(this.map.iterator().hasNext());
    }

    /* */
    @Test
    public void testIterableByKeysView()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value3);
        this.map.put(this.key5, this.value5);
        this.map.put(this.key6, this.value7);
        this.map.put(this.key7, this.value9);
        this.map.remove(this.key2);

        int count = 0;
        for (final KTypeCursor<KType> cursor : this.map.keys())
        {
            if (cursor.index == this.getKeys(this.map).length) {

                Assert.assertTrue(this.isAllocatedDefaultKey(this.map));
                TestUtils.assertEquals2(this.keyE, cursor.value);
                Assert.assertTrue(this.isAllocatedDefaultKey(this.map));
                Assert.assertTrue(this.map.containsKey(cursor.value));

                count++;
                continue;
            }

            Assert.assertTrue(this.map.containsKey(cursor.value));

            TestUtils.assertEquals2(cursor.value, this.getKeys(this.map)[cursor.index]);
            count++;
        }
        Assert.assertEquals(count, this.map.size());

        //Another try !
        this.map.clear();
        Assert.assertFalse(this.map.keys().iterator().hasNext());

        this.map.put(this.key8, this.value2);
        this.map.put(this.key2, this.value3);
        this.map.put(this.key5, this.value2);
        this.map.put(this.keyE, this.value1);
        this.map.put(this.key4, this.value3);
        this.map.put(this.key3, this.value2);
        this.map.put(this.key2, this.value9);
        this.map.remove(this.key2);

        count = 0;
        for (final KTypeCursor<KType> cursor : this.map.keys())
        {
            if (cursor.index == this.getKeys(this.map).length) {

                Assert.assertTrue(this.isAllocatedDefaultKey(this.map));
                TestUtils.assertEquals2(this.keyE, cursor.value);
                Assert.assertTrue(this.isAllocatedDefaultKey(this.map));
                Assert.assertTrue(this.map.containsKey(cursor.value));

                count++;
                continue;
            }

            Assert.assertTrue(this.map.containsKey(cursor.value));

            TestUtils.assertEquals2(cursor.value, this.getKeys(this.map)[cursor.index]);
            count++;
        }
        Assert.assertEquals(count, this.map.size());

        this.map.clear();
        Assert.assertFalse(this.map.keys().iterator().hasNext());
    }

    /* */
    @Test
    public void testIterableByValuesView()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value3);
        this.map.put(this.key5, this.value5);
        this.map.put(this.key6, this.value7);
        this.map.put(this.key7, this.value9);
        this.map.remove(this.key2);

        int count = 0;
        for (final KTypeCursor<VType> cursor : this.map.values())
        {
            if (cursor.index == this.getKeys(this.map).length) {

                Assert.assertTrue(this.isAllocatedDefaultKey(this.map));
                TestUtils.assertEquals2(this.getAllocatedDefaultKeyValue(this.map), cursor.value);

                count++;
                continue;
            }

            TestUtils.assertEquals2(cursor.value, this.getValues(this.map)[cursor.index]);
            count++;
        }
        Assert.assertEquals(count, this.map.size());

        //Another try !
        this.map.clear();
        Assert.assertFalse(this.map.iterator().hasNext());

        this.map.put(this.key8, this.value2);
        this.map.put(this.key2, this.value3);
        this.map.put(this.key5, this.value2);
        this.map.put(this.keyE, this.value1);
        this.map.put(this.key4, this.value3);
        this.map.put(this.key3, this.value2);
        this.map.put(this.key2, this.value9);
        this.map.remove(this.key2);

        count = 0;
        for (final KTypeCursor<VType> cursor : this.map.values())
        {
            if (cursor.index == this.getKeys(this.map).length) {

                Assert.assertTrue(this.isAllocatedDefaultKey(this.map));
                TestUtils.assertEquals2(this.getAllocatedDefaultKeyValue(this.map), cursor.value);

                count++;
                continue;
            }

            TestUtils.assertEquals2(cursor.value, this.getValues(this.map)[cursor.index]);

            count++;
        }
        Assert.assertEquals(count, this.map.size());

        this.map.clear();
        Assert.assertFalse(this.map.iterator().hasNext());
    }

    /*! #if ($TemplateOptions.VTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testHashCodeEquals()
    {
        final KTypeVTypeMap<KType, VType> l0 =
                createNewMapInstance();
        Assert.assertEquals(0, l0.hashCode());
        Assert.assertEquals(l0, createNewMapInstance());

        final KTypeVTypeMap<KType, VType> l1 = getFromArrays(
                newArray(this.key1, this.key2, this.key3),
                newvArray(this.value1, this.value2, this.value3));

        final KTypeVTypeMap<KType, VType> l2 = getFromArrays(
                newArray(this.key2, this.key1, this.key3),
                newvArray(this.value2, this.value1, this.value3));

        final KTypeVTypeMap<KType, VType> l3 = getFromArrays(
                newArray(this.key1, this.key2),
                newvArray(this.value2, this.value1));

        Assert.assertEquals(l1.hashCode(), l2.hashCode());
        Assert.assertEquals(l1, l2);

        Assert.assertFalse(l1.equals(l3));
        Assert.assertFalse(l2.equals(l3));

        l1.put(this.keyE, this.value3);
        l2.put(this.keyE, this.value3);

        Assert.assertEquals(l1.hashCode(), l2.hashCode());
        Assert.assertEquals(l1, l2);
    }

    /*! #if ($TemplateOptions.VTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testBug_HPPC37()
    {
        final KTypeVTypeMap<KType, VType> l1 = getFromArrays(
                newArray(this.key1),
                newvArray(this.value1));

        final KTypeVTypeMap<KType, VType> l2 = getFromArrays(
                newArray(this.key2),
                newvArray(this.value1));

        Assert.assertFalse(l1.equals(l2));
        Assert.assertFalse(l2.equals(l1));
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testNullKey()
    {
        TestUtils.assertEquals2(this.valueE, this.map.get(null));
        TestUtils.assertEquals2(this.valueE, this.map.put(null, vcast(10)));

        TestUtils.assertEquals2(vcast(10), this.map.get(null));
        Assert.assertTrue(this.map.containsKey(null));

        this.map.remove(null);
        Assert.assertEquals(0, this.map.size());
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.VTypeGeneric) !*/
    @Test
    public void testNullValue()
    {
        this.map.put(this.key1, null);
        Assert.assertEquals(this.valueE, this.map.get(this.key1));
        Assert.assertTrue(this.map.containsKey(this.key1));
        this.map.remove(this.key1);
        Assert.assertFalse(this.map.containsKey(this.key1));
        Assert.assertEquals(0, this.map.size());
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.AllGeneric) !*/
    /**
     * Run some random insertions/ deletions and compare the results
     * against <code>java.util.HashMap</code>.
     */
    @Test
    public void testAgainstHashMap()
    {
        final Random rnd = new Random(0xBADCAFE);
        final java.util.HashMap<KType, VType> other =
                new java.util.HashMap<KType, VType>();

        for (int size = 1000; size < 20000; size += 4000)
        {
            other.clear();
            this.map.clear();

            for (int round = 0; round < size * 20; round++)
            {
                final KType key = cast(rnd.nextInt(size));
                final VType value = vcast(rnd.nextInt());

                if (rnd.nextBoolean())
                {
                    this.map.put(key, value);
                    other.put(key, value);

                    Assert.assertEquals(vcastType(value), vcastType(this.map.get(key)));
                    Assert.assertTrue(this.map.containsKey(key));
                }
                else
                {
                    Assert.assertEquals("size= " + size + ", round = " + round,
                            vcastType(other.remove(key)), vcastType(this.map.remove(key)));
                }

                Assert.assertEquals(other.size(), this.map.size());
            }
        }
    }

    /*! #end !*/

    /*
     * 
     */
    @Test
    public void testClone()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.keyE, this.value0);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value3);

        final KTypeVTypeMap<KType, VType> cloned = this.getClone(this.map);
        cloned.remove(this.key1);

        TestUtils.assertSortedListEquals(this.map.keys().toArray(), this.keyE, this.key1, this.key2, this.key3);
        TestUtils.assertSortedListEquals(cloned.keys().toArray(), this.keyE, this.key2, this.key3);
    }

    /*! #if ($TemplateOptions.isKType("int", "short", "byte", "long", "Object") &&
             $TemplateOptions.isVType("int", "short", "byte", "long", "Object")) !*/
    @Test
    public void testToString()
    {

        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value2);

        String asString = this.map.toString();
        asString = asString.replaceAll("[^0-9]", "");
        final char[] asCharArray = asString.toCharArray();
        Arrays.sort(asCharArray);
        Assert.assertEquals("1122", new String(asCharArray));
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.isKType("int", "long", "Object")) !*/
    @Test
    public void testAddRemoveSameHashCollision()
    {

        final IntArrayList hashChain = TestUtils.generateMurmurHash3CollisionChain(0x1fff, 0x7e, 0x1fff / 3);

        /*
         * Add all of the conflicting keys to a map.
         */
        for (final IntCursor c : hashChain) {
            this.map.put(cast(c.value), this.value1);
        }

        Assert.assertEquals(hashChain.size(), this.map.size());

        /*
         * Add some more keys (random).
         */
        final Random rnd = new Random(0xbabebeef);
        final IntSet chainKeys = IntHashSet.from(hashChain);
        final IntSet differentKeys = new IntHashSet();
        while (differentKeys.size() < 500)
        {
            final int k = rnd.nextInt();
            if (!chainKeys.contains(k) && !differentKeys.contains(k)) {
                differentKeys.add(k);
            }
        }

        for (final IntCursor c : differentKeys) {
            this.map.put(cast(c.value), this.value2);
        }

        Assert.assertEquals(hashChain.size() + differentKeys.size(), this.map.size());

        /*
         * Verify the map contains all of the conflicting keys.
         */
        for (final IntCursor c : hashChain) {
            Assert.assertEquals(vcastType(this.value1), vcastType(this.map.get(cast(c.value))));
        }

        /*
         * Verify the map contains all the other keys.
         */
        for (final IntCursor c : differentKeys) {
            TestUtils.assertEquals2(this.value2, this.map.get(cast(c.value)));
        }

        /*
         * Iteratively remove the keys, from first to last.
         */
        for (final IntCursor c : hashChain) {
            TestUtils.assertEquals2(this.value1, this.map.remove(cast(c.value)));
        }

        Assert.assertEquals(differentKeys.size(), this.map.size());

        /*
         * Verify the map contains all the other keys.
         */
        for (final IntCursor c : differentKeys) {
            TestUtils.assertEquals2(this.value2, this.map.get(cast(c.value)));
        }
    }

    /*! #end !*/

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
        this.map.put(this.keyE, this.value0);
        TestUtils.assertSortedListEquals(this.map.values().toArray(), this.value0, this.value1, this.value2, this.value2);
    }

    /* */
    @Test
    public void testMapValuesIterator()
    {
        this.map.put(this.key1, this.value3);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value1);

        int counted = 0;
        for (final KTypeCursor<VType> cursor : this.map.values())
        {
            if (cursor.index == this.getKeys(this.map).length) {

                TestUtils.assertEquals2(this.getAllocatedDefaultKeyValue(this.map), cursor.value);
                counted++;
                continue;
            }

            TestUtils.assertEquals2(this.getValues(this.map)[cursor.index], cursor.value);
            counted++;
        }
        Assert.assertEquals(counted, this.map.size());

        this.map.clear();

        this.map.put(this.key1, this.value3);
        this.map.put(this.keyE, this.value2);
        this.map.put(this.key2, this.value1);

        counted = 0;
        for (final KTypeCursor<VType> cursor : this.map.values())
        {
            if (cursor.index == this.getKeys(this.map).length) {

                TestUtils.assertEquals2(this.getAllocatedDefaultKeyValue(this.map), cursor.value);
                counted++;
                continue;
            }

            TestUtils.assertEquals2(this.getValues(this.map)[cursor.index], cursor.value);
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
        this.map.put(this.keyE, this.value0);

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
        TestUtils.assertSortedListEquals(this.map.values().toArray(), this.value0, this.value1, this.value2, this.value2);

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
        TestUtils.assertSortedListEquals(this.map.values().toArray(), this.value0, this.value1, this.value2, this.value2);
    }

    @Test
    public void testPooledIteratorForEach()
    {
        //A) Unbroken for-each loop
        //must accommodate even the smallest primitive type
        //so that the iteration do not break before it should...
        final int TEST_SIZE = 126;
        final long TEST_ROUNDS = 5000;

        final KTypeVTypeMap<KType, VType> testContainer = createMapWithOrderedData(TEST_SIZE);
        final KTypeCollection<KType> keyset = testContainer.keys();
        final KTypeCollection<VType> valueset = testContainer.values();

        final long checksum = testContainer.forEach(new KTypeVTypeProcedure<KType, VType>() {

            long count;

            @Override
            public void apply(final KType key, final VType value)
            {
                this.count += vcastType(value);
            }
        }).count;

        long testValue = 0;
        long initialPoolSize = this.getEntryPoolSize(testContainer);

        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            //for-each in test :

            //A) Loop on entries
            testValue = 0;
            for (final KTypeVTypeCursor<KType, VType> cursor : testContainer)
            {
                //we consume 1 iterator for this loop
                Assert.assertEquals(initialPoolSize - 1, this.getEntryPoolSize(testContainer));

                testValue += vcastType(cursor.value);
            }

            //check checksum the iteration
            Assert.assertEquals(checksum, testValue);

            //iterator is returned to its pool
            Assert.assertEquals(initialPoolSize, this.getEntryPoolSize(testContainer));

            //B) Loop on keys == same value as values
            testValue = 0;
            initialPoolSize = this.getKeysPoolSize(keyset);

            for (final KTypeCursor<KType> cursor : keyset)
            {
                //we consume 1 iterator for this loop
                Assert.assertEquals(initialPoolSize - 1, this.getKeysPoolSize(keyset));

                testValue += castType(cursor.value);
            }

            //check checksum the iteration
            Assert.assertEquals(checksum, testValue);

            //iterator is returned to its pool
            Assert.assertEquals(initialPoolSize, this.getKeysPoolSize(keyset));

            //C) Loop on values
            testValue = 0;
            initialPoolSize = this.getValuesPoolSize(valueset);

            for (final KTypeCursor<VType> cursor : valueset)
            {
                //we consume 1 iterator for this loop
                Assert.assertEquals(initialPoolSize - 1, this.getValuesPoolSize(valueset));

                testValue += vcastType(cursor.value);
            }

            //check checksum the iteration
            Assert.assertEquals(checksum, testValue);

            //iterator is returned to its pool
            Assert.assertEquals(initialPoolSize, this.getValuesPoolSize(valueset));

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

        final KTypeVTypeMap<KType, VType> testContainer = createMapWithOrderedData(TEST_SIZE);
        final KTypeCollection<KType> keyset = testContainer.keys();
        final KTypeCollection<VType> valueset = testContainer.values();

        int count = 0;
        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            //A) for-each in test :
            long initialPoolSize = this.getEntryPoolSize(testContainer);
            count = 0;
            for (final KTypeVTypeCursor<KType, VType> cursor : testContainer)
            {
                this.guard += vcastType(cursor.value);
                //we consume 1 iterator for this loop, but reallocs can happen,
                //so we can only say its != initialPoolSize
                Assert.assertTrue(initialPoolSize != this.getEntryPoolSize(testContainer));

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
            Assert.assertTrue(initialPoolSize != this.getEntryPoolSize(testContainer));

            //B) Loop on keys
            initialPoolSize = this.getKeysPoolSize(keyset);
            count = 0;
            for (final KTypeCursor<KType> cursor : keyset)
            {
                this.guard += castType(cursor.value);
                //we consume 1 iterator for this loop, but reallocs can happen,
                //so we can only say its != initialPoolSize
                Assert.assertTrue(initialPoolSize != this.getKeysPoolSize(keyset));

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
            Assert.assertTrue(initialPoolSize != this.getKeysPoolSize(keyset));

            //C) Loop on values
            initialPoolSize = this.getValuesPoolSize(valueset);
            count = 0;
            for (final KTypeCursor<VType> cursor : valueset)
            {
                this.guard += vcastType(cursor.value);
                //we consume 1 iterator for this loop, but reallocs can happen,
                //so we can only say its != initialPoolSize
                Assert.assertTrue(initialPoolSize != this.getValuesPoolSize(valueset));

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
            Assert.assertTrue(initialPoolSize != this.getValuesPoolSize(valueset));
        } //end for rounds

        //Due to policy of the Iterator pool, the intended pool never get bigger that some limit
        //despite the Iterator leak.
        Assert.assertTrue(this.getEntryPoolCapacity(testContainer) < IteratorPool.getMaxPoolSize() + 1);
        Assert.assertTrue(this.getKeysPoolCapacity(keyset) < IteratorPool.getMaxPoolSize() + 1);
        Assert.assertTrue(this.getValuesPoolCapacity(valueset) < IteratorPool.getMaxPoolSize() + 1);
    }

    @Test
    public void testPooledIteratorFullIteratorLoop()
    {
        // for-each loop interrupted

        //must accommodate even the smallest primitive type
        //so that the iteration do not break before it should...
        final int TEST_SIZE = 126;
        final long TEST_ROUNDS = 5000;

        final KTypeVTypeMap<KType, VType> testContainer = createMapWithOrderedData(TEST_SIZE);
        final KTypeCollection<KType> keyset = testContainer.keys();
        final KTypeCollection<VType> valueset = testContainer.values();

        final long checksum = testContainer.forEach(new KTypeVTypeProcedure<KType, VType>() {

            long count;

            @Override
            public void apply(final KType key, final VType value)
            {
                this.count += vcastType(value);
            }
        }).count;

        long testValue = 0;
        final int startingPoolSize = this.getEntryPoolSize(testContainer);

        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            //A) Classical iterator loop, with manually allocated Iterator
            int initialPoolSize = this.getEntryPoolSize(testContainer);

            final AbstractIterator<KTypeVTypeCursor<KType, VType>> loopIterator = (AbstractIterator<KTypeVTypeCursor<KType, VType>>) testContainer.iterator();

            Assert.assertEquals(initialPoolSize - 1, this.getEntryPoolSize(testContainer));

            testValue = 0;
            while (loopIterator.hasNext())
            {
                testValue += vcastType(loopIterator.next().value);
            } //end IteratorLoop

            //iterator is returned automatically to its pool, by normal iteration termination
            Assert.assertEquals(initialPoolSize, this.getEntryPoolSize(testContainer));

            //checksum
            Assert.assertEquals(checksum, testValue);

            //B) Loop on keys
            initialPoolSize = this.getKeysPoolSize(keyset);

            final AbstractIterator<KTypeCursor<KType>> keyLoopIterator = (AbstractIterator<KTypeCursor<KType>>) keyset.iterator();

            Assert.assertEquals(initialPoolSize - 1, this.getKeysPoolSize(keyset));

            testValue = 0;
            while (keyLoopIterator.hasNext())
            {
                testValue += castType(keyLoopIterator.next().value);
            } //end IteratorLoop

            //iterator is returned automatically to its pool, by normal iteration termination
            Assert.assertEquals(initialPoolSize, this.getKeysPoolSize(keyset));

            //checksum
            Assert.assertEquals(checksum, testValue);

            //C) Loop on values
            initialPoolSize = this.getValuesPoolSize(valueset);

            final AbstractIterator<KTypeCursor<VType>> valueLoopIterator = (AbstractIterator<KTypeCursor<VType>>) valueset.iterator();

            Assert.assertEquals(initialPoolSize - 1, this.getValuesPoolSize(valueset));

            testValue = 0;
            while (valueLoopIterator.hasNext())
            {
                testValue += vcastType(valueLoopIterator.next().value);
            } //end IteratorLoop

            //iterator is returned automatically to its pool, by normal iteration termination
            Assert.assertEquals(initialPoolSize, this.getValuesPoolSize(valueset));

            //checksum
            Assert.assertEquals(checksum, testValue);

        } //end for rounds

        // pool initial size is untouched anyway
        Assert.assertEquals(startingPoolSize, this.getEntryPoolSize(testContainer));
        Assert.assertEquals(startingPoolSize, this.getKeysPoolSize(keyset));
        Assert.assertEquals(startingPoolSize, this.getValuesPoolSize(valueset));
    }

    @Test
    public void testPooledIteratorBrokenIteratorLoop()
    {
        //for-each loop interrupted

        //must accommodate even the smallest primitive type
        //so that the iteration do not break before it should...
        final int TEST_SIZE = 126;
        final long TEST_ROUNDS = 5000;

        final KTypeVTypeMap<KType, VType> testContainer = createMapWithOrderedData(TEST_SIZE);
        final KTypeCollection<KType> keyset = testContainer.keys();
        final KTypeCollection<VType> valueset = testContainer.values();

        final int startingPoolSize = this.getEntryPoolSize(testContainer);

        int count = 0;
        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            //A) Classical iterator loop, with manually allocated Iterator
            long initialPoolSize = this.getEntryPoolSize(testContainer);

            final AbstractIterator<KTypeVTypeCursor<KType, VType>> loopIterator = (AbstractIterator<KTypeVTypeCursor<KType, VType>>) testContainer.iterator();

            Assert.assertEquals(initialPoolSize - 1, this.getEntryPoolSize(testContainer));

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
            Assert.assertEquals(initialPoolSize - 1, this.getEntryPoolSize(testContainer));

            //manual return to the pool
            loopIterator.release();

            //now the pool is restored
            Assert.assertEquals(initialPoolSize, this.getEntryPoolSize(testContainer));

            //B) Iterate on keys
            initialPoolSize = this.getKeysPoolSize(keyset);

            final AbstractIterator<KTypeCursor<KType>> keyLoopIterator = (AbstractIterator<KTypeCursor<KType>>) keyset.iterator();

            Assert.assertEquals(initialPoolSize - 1, this.getKeysPoolSize(keyset));

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
            Assert.assertEquals(initialPoolSize - 1, this.getKeysPoolSize(keyset));

            //manual return to the pool
            keyLoopIterator.release();

            //now the pool is restored
            Assert.assertEquals(initialPoolSize, this.getKeysPoolSize(keyset));

            //C) Iterate on values
            initialPoolSize = this.getValuesPoolSize(valueset);

            final AbstractIterator<KTypeCursor<VType>> valueLoopIterator = (AbstractIterator<KTypeCursor<VType>>) valueset.iterator();

            Assert.assertEquals(initialPoolSize - 1, this.getValuesPoolSize(valueset));

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
            Assert.assertEquals(initialPoolSize - 1, this.getValuesPoolSize(valueset));

            //manual return to the pool
            valueLoopIterator.release();

            //now the pool is restored
            Assert.assertEquals(initialPoolSize, this.getValuesPoolSize(valueset));

        } //end for rounds

        // pool initial size is untouched anyway
        Assert.assertEquals(startingPoolSize, this.getEntryPoolSize(testContainer));
        Assert.assertEquals(startingPoolSize, this.getKeysPoolSize(keyset));
        Assert.assertEquals(startingPoolSize, this.getValuesPoolSize(valueset));
    }

    @Test
    public void testPooledIteratorExceptionIteratorLoop()
    {
        //must accommodate even the smallest primitive type
        //so that the iteration do not break before it should...
        final int TEST_SIZE = 126;
        final long TEST_ROUNDS = 5000;

        final KTypeVTypeMap<KType, VType> testContainer = createMapWithOrderedData(TEST_SIZE);

        final long checksum = testContainer.forEach(new KTypeVTypeProcedure<KType, VType>() {

            long count;

            @Override
            public void apply(final KType key, final VType value)
            {
                this.count += vcastType(value);
            }
        }).count;

        final int startingPoolSize = this.getEntryPoolSize(testContainer);

        int count = 0;
        AbstractIterator<KTypeVTypeCursor<KType, VType>> loopIterator = null;

        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            try
            {
                loopIterator = (AbstractIterator<KTypeVTypeCursor<KType, VType>>) testContainer.iterator();

                Assert.assertEquals(startingPoolSize - 1, this.getEntryPoolSize(testContainer));

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
                Assert.assertEquals(startingPoolSize, this.getEntryPoolSize(testContainer));
                Assert.assertEquals(checksum, this.guard);

            } catch (final Exception e)
            {
                //iterator is NOT returned to its pool because of the exception
                Assert.assertEquals(startingPoolSize - 1, this.getEntryPoolSize(testContainer));

                //manual return to the pool then
                loopIterator.release();

                //now the pool is restored
                Assert.assertEquals(startingPoolSize, this.getEntryPoolSize(testContainer));
            }
        } //end for rounds

        // pool initial size is untouched anyway
        Assert.assertEquals(startingPoolSize, this.getEntryPoolSize(testContainer));
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
        final KTypeVTypeMap<KType, VType> newMap = createNewMapInstance(PREALLOCATED_SIZE,
                HashContainers.DEFAULT_LOAD_FACTOR);

        //computed real capacity
        final int realCapacity = newMap.capacity();

        //3) Add PREALLOCATED_SIZE different values. At the end, size() must be == PREALLOCATED_SIZE,
        //and internal buffer/allocated must not have changed of size
        final int contructorBufferSize = getKeys(newMap).length;

        Assert.assertEquals(contructorBufferSize, getKeys(newMap).length);
        Assert.assertEquals(contructorBufferSize, getValues(newMap).length);

        for (int i = 0; i < 1.5 * realCapacity; i++) {

            newMap.put(cast(i), vcast(randomVK.nextInt()));

            //internal size has not changed until realCapacity
            if (newMap.size() <= realCapacity) {

                Assert.assertEquals(contructorBufferSize, getKeys(newMap).length);
            }

            if (contructorBufferSize < getKeys(newMap).length) {
                //The container as just reallocated, its actual size must be not too far from the previous capacity:
                Assert.assertTrue("Container as reallocated at size = " + newMap.size() + " with previous capacity = " + realCapacity,
                        (newMap.size() - realCapacity) <= 3);
                break;
            }
        }

    }

    @Test
    public void testForEachProcedureWithException()
    {
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

        final KTypeVTypeMap<KType, VType> newMap = createNewMapInstance(NB_ELEMENTS,
                HashContainers.DEFAULT_LOAD_FACTOR);

        newMap.put(this.keyE, vcast(NB_ELEMENTS));

        //add a increasing number of unique key/values pairs
        //use the same value for keys and values to ease later analysis
        for (int i = 0; i < NB_ELEMENTS; i++) {

            final int KVpair = i;

            newMap.put(cast(KVpair), vcast(KVpair));
        }

        //List the keys in the reverse-order of the internal buffer, since forEach() is iterating in reverse also:
        final KTypeArrayList<KType> keyList = new KTypeArrayList<KType>();
        final ArrayList<Integer> valueList = new ArrayList<Integer>();

        keyList.add(this.keyE);
        valueList.add(vcastType(getAllocatedDefaultKeyValue(newMap)));

        //Test forEach predicate and stop at each key in turn.
        final KTypeArrayList<KType> keyListTest = new KTypeArrayList<KType>();
        final ArrayList<Integer> valueListTest = new ArrayList<Integer>();

        for (int k = getKeys(newMap).length - 1; k >= 0; k--) {

            if (is_allocated(k, Intrinsics.<KType[]> cast(getKeys(newMap)))) {

                keyList.add(Intrinsics.<KType> cast(getKeys(newMap)[k]));
                valueList.add(vcastType(Intrinsics.<VType> cast(getValues(newMap)[k])));
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

            keyList.add(this.keyE);
            valueList.add(vcastType(getAllocatedDefaultKeyValue(newMap)));

            for (int k = getKeys(newMap).length - 1; k >= 0; k--) {

                if (is_allocated(k, Intrinsics.<KType[]> cast(getKeys(newMap)))) {

                    keyList.add(Intrinsics.<KType> cast(getKeys(newMap)[k]));
                    valueList.add(vcastType(Intrinsics.<VType> cast(getValues(newMap)[k])));
                }
            }

            //A) Run forEach(KTypeVType)
            try
            {
                newMap.forEach(new KTypeVTypeProcedure<KType, VType>() {

                    @Override
                    public void apply(final KType key, final VType value)
                    {
                        keyListTest.add(key);
                        valueListTest.add(vcastType(value));

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
                Assert.assertEquals(currentPairIndexSizeToIterate, keyListTest.size());
                Assert.assertEquals(currentPairIndexSizeToIterate, valueListTest.size());

                for (int j = 0; j < currentPairIndexSizeToIterate; j++)
                {
                    TestUtils.assertEquals2(keyList.get(j), keyListTest.get(j));
                    TestUtils.assertEquals2(valueList.get(j), valueListTest.get(j));
                }
            } //end finally

            //B) Run keys().forEach(KType)
            keyListTest.clear();
            valueListTest.clear();
            try
            {
                newMap.keys().forEach(new KTypeProcedure<KType>() {

                    @Override
                    public void apply(final KType key)
                    {
                        keyListTest.add(key);

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
                Assert.assertEquals(currentPairIndexSizeToIterate, keyListTest.size());
                Assert.assertEquals(currentPairIndexSizeToIterate, valueListTest.size());

                for (int j = 0; j < currentPairIndexSizeToIterate; j++)
                {
                    TestUtils.assertEquals2(keyList.get(j), keyListTest.get(j));
                    TestUtils.assertEquals2(valueList.get(j), valueListTest.get(j));
                }
            } //end finally

            //C) Run values().forEach(VType) Beware, the values are iterated in-order this time !
            keyListTest.clear();
            valueListTest.clear();
            keyList.clear();
            valueList.clear();

            keyList.add(this.keyE);
            valueList.add(vcastType(getAllocatedDefaultKeyValue(newMap)));

            for (int k = 0; k < getKeys(newMap).length; k++) {

                if (is_allocated(k, Intrinsics.<KType[]> cast(getKeys(newMap)))) {

                    keyList.add(Intrinsics.<KType> cast(getKeys(newMap)[k]));
                    valueList.add(vcastType(Intrinsics.<VType> cast(getValues(newMap)[k])));
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
            } catch (final RuntimeException e)
            {
                if (!e.getMessage().equals("Interrupted treatment by test"))
                {
                    throw e;
                }
            } finally
            {
                //despite the exception, the procedure terminates cleanly

                //check that  valueList/valueListTest are identical for the first
                //currentPairIndexToIterate + 1 elements
                Assert.assertEquals("i = " + i + "(size=" + size + ")", currentPairIndexSizeToIterate, valueListTest.size());

                for (int j = 0; j < currentPairIndexSizeToIterate; j++)
                {
                    TestUtils.assertEquals2("i = " + i + ", j = " + j, valueList.get(j), valueListTest.get(j));
                }
            } //end finally

        } //end for each index
    }

    @Test
    public void testForEachProcedure()
    {
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

        final KTypeVTypeMap<KType, VType> newMap = createNewMapInstance(NB_ELEMENTS,
                HashContainers.DEFAULT_LOAD_FACTOR);

        newMap.put(this.keyE, vcast(NB_ELEMENTS));

        //add a increasing number of unique key/values pairs
        //use the same value for keys and values to ease later analysis
        for (int i = 0; i < NB_ELEMENTS; i++) {

            final int KVpair = i;

            newMap.put(cast(KVpair), vcast(KVpair));
        }

        //List the keys in the reverse-order of the internal buffer, since forEach() is iterating in reverse also:
        final KTypeArrayList<KType> keyList = new KTypeArrayList<KType>();
        final ArrayList<Integer> valueList = new ArrayList<Integer>();

        keyList.add(this.keyE);
        valueList.add(vcastType(getAllocatedDefaultKeyValue(newMap)));

        for (int i = getKeys(newMap).length - 1; i >= 0; i--) {

            if (is_allocated(i, Intrinsics.<KType[]> cast(getKeys(newMap)))) {

                keyList.add(Intrinsics.<KType> cast(getKeys(newMap)[i]));
                valueList.add(vcastType(Intrinsics.<VType> cast(getValues(newMap)[i])));
            }
        }

        //Test forEach predicate and stop at each key in turn.
        final KTypeArrayList<KType> keyListTest = new KTypeArrayList<KType>();
        final ArrayList<Integer> valueListTest = new ArrayList<Integer>();

        keyListTest.clear();
        valueListTest.clear();

        //A) Run forEach(KTypeVType)

        newMap.forEach(new KTypeVTypeProcedure<KType, VType>() {

            @Override
            public void apply(final KType key, final VType value)
            {
                keyListTest.add(key);
                valueListTest.add(vcastType(value));
            }
        });

        //check that keyList/keyListTest and valueList/valueListTest are identical.
        Assert.assertEquals(keyList, keyListTest);
        Assert.assertEquals(valueList, valueListTest);

        //B) Run keys().forEach(KType)
        keyListTest.clear();
        valueListTest.clear();

        newMap.keys().forEach(new KTypeProcedure<KType>() {

            @Override
            public void apply(final KType key)
            {
                keyListTest.add(key);
                //retreive value by get() on the map
                valueListTest.add(vcastType(newMap.get(key)));
            }
        });

        //check that keyList/keyListTest and valueList/valueListTest are identical .
        Assert.assertEquals(keyList, keyListTest);
        Assert.assertEquals(valueList, valueListTest);

        //C) Run values().forEach(VType) : Beware, they are iterated in-order !
        keyListTest.clear();
        valueListTest.clear();

        keyList.clear();
        valueList.clear();

        keyList.add(this.keyE);
        valueList.add(vcastType(getAllocatedDefaultKeyValue(newMap)));

        for (int k = 0; k < getKeys(newMap).length; k++) {

            if (is_allocated(k, Intrinsics.<KType[]> cast(getKeys(newMap)))) {

                keyList.add(Intrinsics.<KType> cast(getKeys(newMap)[k]));
                valueList.add(vcastType(Intrinsics.<VType> cast(getValues(newMap)[k])));
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

        final KTypeVTypeMap<KType, VType> newMap = createNewMapInstance(NB_ELEMENTS,
                HashContainers.DEFAULT_LOAD_FACTOR);

        newMap.put(this.keyE, vcast(NB_ELEMENTS));

        //add a increasing number of unique key/values pairs
        //use the same value for keys and values to ease later analysis
        for (int i = 0; i < NB_ELEMENTS; i++) {

            final int KVpair = i;

            newMap.put(cast(KVpair), vcast(KVpair));
        }

        //List the keys in the reverse-order of the internal buffer, since forEach() is iterating in reverse also:
        final KTypeArrayList<KType> keyList = new KTypeArrayList<KType>();
        final ArrayList<Integer> valueList = new ArrayList<Integer>();

        keyList.add(this.keyE);
        valueList.add(vcastType(getAllocatedDefaultKeyValue(newMap)));

        //Test forEach predicate and stop at each key in turn.
        final KTypeArrayList<KType> keyListTest = new KTypeArrayList<KType>();
        final ArrayList<Integer> valueListTest = new ArrayList<Integer>();

        for (int k = getKeys(newMap).length - 1; k >= 0; k--) {

            if (is_allocated(k, Intrinsics.<KType[]> cast(getKeys(newMap)))) {

                keyList.add(Intrinsics.<KType> cast(getKeys(newMap)[k]));
                valueList.add(vcastType(Intrinsics.<VType> cast(getValues(newMap)[k])));
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

            keyList.add(this.keyE);
            valueList.add(vcastType(getAllocatedDefaultKeyValue(newMap)));

            for (int k = getKeys(newMap).length - 1; k >= 0; k--) {

                if (is_allocated(k, Intrinsics.<KType[]> cast(getKeys(newMap)))) {

                    keyList.add(Intrinsics.<KType> cast(getKeys(newMap)[k]));
                    valueList.add(vcastType(Intrinsics.<VType> cast(getValues(newMap)[k])));
                }
            }

            //A) Run forEach(KTypeVType)

            newMap.forEach(new KTypeVTypePredicate<KType, VType>() {

                @Override
                public boolean apply(final KType key, final VType value)
                {
                    keyListTest.add(key);
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
                TestUtils.assertEquals2(keyList.get(j), keyListTest.get(j));
                TestUtils.assertEquals2(valueList.get(j), valueListTest.get(j));
            }

            //B) Run keys().forEach(KType)
            keyListTest.clear();
            valueListTest.clear();

            newMap.keys().forEach(new KTypePredicate<KType>() {

                @Override
                public boolean apply(final KType key)
                {
                    keyListTest.add(key);

                    //retreive value by get() on the map
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
                TestUtils.assertEquals2(keyList.get(j), keyListTest.get(j));
                TestUtils.assertEquals2(valueList.get(j), valueListTest.get(j));
            }

            //C) Run values().forEach(VType) : Beware, values are iterated in-order !
            keyListTest.clear();
            valueListTest.clear();
            keyList.clear();
            valueList.clear();

            keyList.add(this.keyE);
            valueList.add(vcastType(getAllocatedDefaultKeyValue(newMap)));

            for (int k = 0; k < getKeys(newMap).length; k++) {

                if (is_allocated(k, Intrinsics.<KType[]> cast(getKeys(newMap)))) {

                    keyList.add(Intrinsics.<KType> cast(getKeys(newMap)[k]));
                    valueList.add(vcastType(Intrinsics.<VType> cast(getValues(newMap)[k])));
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
            Assert.assertEquals("i = " + i + "(size=" + size + ")", currentPairIndexSizeToIterate, valueListTest.size());

            for (int j = 0; j < currentPairIndexSizeToIterate; j++)
            {
                TestUtils.assertEquals2("i = " + i + ", j = " + j, valueList.get(j), valueListTest.get(j));
            }
        } //end for each index
    }

    @Seed("88DC7A1093FD66C5")
    @Repeat(iterations = 25)
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

        //2) Preallocate to PREALLOCATED_SIZE : use default factor because copy-constructor use this.
        final KTypeVTypeMap<KType, VType> refContainer = createNewMapInstance(PREALLOCATED_SIZE,
                HashContainers.DEFAULT_LOAD_FACTOR);

        final int refCapacity = refContainer.capacity();

        //3) Fill with random values, random number of elements below preallocation
        final int nbElements = RandomizedTest.randomInt(PREALLOCATED_SIZE);

        for (int i = 0; i < nbElements; i++) {

            refContainer.put(cast(i), vcast(randomVK.nextInt()));
        }

        final int nbRefElements = refContainer.size();

        //Capacity must have not changed, i.e no reallocation must have occured.
        Assert.assertEquals(refCapacity, refContainer.capacity());

        //4) Duplicate by copy-construction and/or clone
        KTypeVTypeMap<KType, VType> clonedContainer = getClone(refContainer);
        KTypeVTypeMap<KType, VType> copiedContainer = getFrom(refContainer);

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

    @Test
    public void testDefaultValues() {

        final VType[] EVEN_DEFAULT_VALUES = newvArray(this.value2, this.value4, this.value6, this.value8);
        final VType[] ODD_DEFAULT_VALUES = newvArray(this.value1, this.value3, this.value5, this.value7, this.value9);

        checkAgainstDefaultValues(EVEN_DEFAULT_VALUES);

        checkAgainstDefaultValues(ODD_DEFAULT_VALUES);
    }

    protected void checkAgainstDefaultValues(final VType[] defaultValuesForTest) {

        int currentDefaultValueIndex = 0;

        //recreate from scratch
        this.map = createNewMapInstance();

        final int NB_INSERTED_ELEMENTS = 126;

        // Insert values "not in default set":
        //- Check: get() on  existing key, NEVER returns the default value,
        //- Check : get() on non-existing key returns the current default value
        //- Check : on empty map returns the default value.

        // by default, o/null
        TestUtils.assertEquals2(this.valueE, this.map.getDefaultValue());

        Assert.assertFalse(this.map.containsKey(this.keyE));

        //try to get ii, it is not in the map, return default value
        TestUtils.assertEquals2(this.map.getDefaultValue(), this.map.get(this.keyE));

        //try to remove ii, which do not exist in map, so current value is returned
        TestUtils.assertEquals2(this.map.getDefaultValue(), this.map.remove(this.keyE));

        //insert "not-defaulted" value, return the default value because the key wasn't there before:
        TestUtils.assertEquals2(this.map.getDefaultValue(), this.map.put(this.keyE, vcast(NB_INSERTED_ELEMENTS)));

        for (int ii = 11; ii < NB_INSERTED_ELEMENTS; ii++) {

            if (!isInVArray(defaultValuesForTest, vcast(ii))) {

                Assert.assertFalse(this.map.containsKey(cast(ii)));

                //try to get ii, it is not in the map, return default value
                TestUtils.assertEquals2(this.map.getDefaultValue(), this.map.get(cast(ii)));

                //try to remove ii, which do not exist in map, so current value is returned
                TestUtils.assertEquals2(this.map.getDefaultValue(), this.map.remove(cast(ii)));

                //insert "not-defaulted" value, return the default value because the key wasn't there before:
                TestUtils.assertEquals2(this.map.getDefaultValue(), this.map.put(cast(ii), vcast(ii)));

                //insert another value, the previous value is now returned
                TestUtils.assertEquals2(vcast(ii), this.map.put(cast(ii), vcast(ii + 1)));

                //Remove ii, previous value ii + 1 is returned
                TestUtils.assertEquals2(vcast(ii + 1), this.map.remove(cast(ii)));

                //try to remove again, which do not exist in map, so default value is returned
                TestUtils.assertEquals2(this.map.getDefaultValue(), this.map.remove(cast(ii)));

                //add again
                TestUtils.assertEquals2(this.map.getDefaultValue(), this.map.put(cast(ii), vcast(ii + 1)));

                //every 5 inserted elements, change the default value
                if (ii % 5 == 0) {
                    this.map.setDefaultValue(defaultValuesForTest[currentDefaultValueIndex]);
                    TestUtils.assertEquals2(defaultValuesForTest[currentDefaultValueIndex], this.map.getDefaultValue());

                    //try to remove keyE, returns the previous value
                    TestUtils.assertEquals2(vcast(NB_INSERTED_ELEMENTS), this.map.remove(this.keyE));

                    //try to get ii, it is not in the map, return default value
                    TestUtils.assertEquals2(this.map.getDefaultValue(), this.map.get(this.keyE));

                    //try to remove again, which do not exist in map, so default value is returned
                    TestUtils.assertEquals2(this.map.getDefaultValue(), this.map.remove(this.keyE));

                    //insert "not-defaulted" value, return the default value because the key wasn't there before:
                    TestUtils.assertEquals2(this.map.getDefaultValue(), this.map.put(this.keyE, vcast(NB_INSERTED_ELEMENTS)));

                    currentDefaultValueIndex = (currentDefaultValueIndex + 1) % defaultValuesForTest.length;
                }
            }
        } //end for NB_INSERTED_ELEMENTS
    }

    protected KTypeVTypeMap<KType, VType> createMapWithOrderedData(final int size)
    {
        final KTypeVTypeMap<KType, VType> newMap = createNewMapInstance(Containers.DEFAULT_EXPECTED_ELEMENTS,
                HashContainers.DEFAULT_LOAD_FACTOR);

        for (int i = 0; i < size; i++)
        {
            newMap.put(cast(i), vcast(i));
        }

        return newMap;
    }

    private boolean is_allocated(final int slot, final KType[] keys) {

        return !Intrinsics.<KType> isEmpty(keys[slot]);
    }
}
