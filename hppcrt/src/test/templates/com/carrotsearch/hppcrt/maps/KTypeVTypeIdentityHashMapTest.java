package com.carrotsearch.hppcrt.maps;

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
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.*;

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
/**
 * Tests for {@link KTypeVTypeIdentityHashMap}.
 */
/*! ${TemplateOptions.doNotGenerateKType("BOOLEAN", "BYTE", "CHAR", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE")} !*/
//${TemplateOptions.doNotGenerateVType("BOOLEAN")}
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeVTypeIdentityHashMapTest<KType, VType> extends AbstractKTypeVTypeTest<KType, VType>
{
    @BeforeClass
    public static void configure()
    {
        IteratorPool.configureInitialPoolSize(8);
    }

    /**
     * Per-test fresh initialized instance.
     */
    public KTypeVTypeIdentityHashMap<KType, VType> map = KTypeVTypeIdentityHashMap.newInstance();

    /**
     * The identity map is only valid for Object keys anyway
     */
    @Before
    public void initialize() {

        this.map = KTypeVTypeIdentityHashMap.newInstance();

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

            final int mask = this.map.keys.length - 1;

            for (int i = 0; i < this.map.keys.length; i++)
            {
                if (!is_allocated(i, this.map.keys))
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

                    //get() test
                    Assert.assertEquals(vcastType(this.map.values[i]), vcastType(this.map.get(this.map.keys[i])));

                    occupied++;
                }
            }

            if (this.map.allocatedDefaultKey) {

                //try to reach the key by contains()
                Assert.assertTrue(this.map.containsKey(Intrinsics.<KType> defaultKTypeValue()));

                //get() test
                TestUtils.assertEquals2(vcastType(this.map.allocatedDefaultKeyValue), vcastType(this.map.get(Intrinsics.<KType> defaultKTypeValue())));

                occupied++;
            }

            Assert.assertEquals(occupied, this.map.size());
        }
    }

    @Test
    public void testClone()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.keyE, this.value0);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value3);

        final KTypeVTypeIdentityHashMap<KType, VType> cloned = this.map.clone();
        cloned.remove(this.key1);

        TestUtils.assertSortedListEquals(this.map.keys().toArray(), this.keyE, this.key1, this.key2, this.key3);
        TestUtils.assertSortedListEquals(cloned.keys().toArray(), this.keyE, this.key2, this.key3);
    }

    /* */
    @Test
    public void testCloningConstructor()
    {
        this.map.put(this.keyE, this.value0);
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value3);

        assertSameMap(this.map, KTypeVTypeIdentityHashMap.from(this.map));
        assertSameMap(this.map, new KTypeVTypeIdentityHashMap<KType, VType>(this.map));
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

        final KTypeVTypeIdentityHashMap<KType, VType> map2 = KTypeVTypeIdentityHashMap.from(
                asArrayObjects(this.keyE, this.key1, this.key2, this.key3),
                newvArray(this.value0, this.value1, this.value2, this.value3));

        assertSameMap(this.map, map2);
    }

    /* */
    @Test
    public void testPutWithExpansions()
    {
        final int COUNT = 10000;
        final Random rnd = new Random(0xDEADBEEF);

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
    public void testMapKeySetIterator()
    {
        this.map.put(this.key1, this.value3);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value1);

        int counted = 0;
        for (final KTypeCursor<KType> cursor : this.map.keys())
        {
            if (cursor.index == this.map.keys.length) {

                TestUtils.assertEquals2(Intrinsics.defaultKTypeValue(), cursor.value);
                counted++;
                continue;
            }

            TestUtils.assertEquals2(this.map.keys[cursor.index], cursor.value);
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
            if (cursor.index == this.map.keys.length) {

                TestUtils.assertEquals2(Intrinsics.defaultKTypeValue(), cursor.value);
                counted++;
                continue;
            }

            TestUtils.assertEquals2(this.map.keys[cursor.index], cursor.value);
            counted++;
        }
        Assert.assertEquals(counted, this.map.size());
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
            if (cursor.index == this.map.keys.length) {

                TestUtils.assertEquals2(Intrinsics.defaultKTypeValue(), cursor.key);
                TestUtils.assertEquals2(this.map.allocatedDefaultKeyValue, cursor.value);
                count++;
                continue;
            }

            count++;
            Assert.assertTrue(this.map.containsKey(cursor.key));
            TestUtils.assertEquals2(cursor.value, this.map.get(cursor.key));

            TestUtils.assertEquals2(cursor.value, this.map.values[cursor.index]);
            TestUtils.assertEquals2(cursor.key, this.map.keys[cursor.index]);
        }
        Assert.assertEquals(count, this.map.size());

        this.map.clear();
        Assert.assertFalse(this.map.iterator().hasNext());

        this.map.put(this.keyE, this.value1);
        this.map.put(this.key8, this.value2);
        this.map.put(this.key2, this.value3);
        this.map.remove(this.key2);

        count = 0;
        for (final KTypeVTypeCursor<KType, VType> cursor : this.map)
        {
            if (cursor.index == this.map.keys.length) {

                TestUtils.assertEquals2(Intrinsics.defaultKTypeValue(), cursor.key);
                TestUtils.assertEquals2(this.map.allocatedDefaultKeyValue, cursor.value);
                count++;
                continue;
            }

            count++;
            Assert.assertTrue(this.map.containsKey(cursor.key));
            TestUtils.assertEquals2(cursor.value, this.map.get(cursor.key));

            TestUtils.assertEquals2(cursor.value, this.map.values[cursor.index]);
            TestUtils.assertEquals2(cursor.key, this.map.keys[cursor.index]);
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
        final KTypeVTypeIdentityHashMap<KType, VType> l0 =
                new KTypeVTypeIdentityHashMap<KType, VType>();
        Assert.assertEquals(0, l0.hashCode());
        Assert.assertEquals(l0, new KTypeVTypeIdentityHashMap<KType, VType>());

        final KTypeVTypeIdentityHashMap<KType, VType> l1 = KTypeVTypeIdentityHashMap.from(
                asArrayObjects(this.key1, this.key2, this.key3),
                newvArray(this.value1, this.value2, this.value3));

        final KTypeVTypeIdentityHashMap<KType, VType> l2 = KTypeVTypeIdentityHashMap.from(
                asArrayObjects(this.key2, this.key1, this.key3),
                newvArray(this.value2, this.value1, this.value3));

        final KTypeVTypeIdentityHashMap<KType, VType> l3 = KTypeVTypeIdentityHashMap.from(
                asArrayObjects(this.key1, this.key2),
                newvArray(this.value2, this.value1));

        Assert.assertEquals(l1.hashCode(), l2.hashCode());
        Assert.assertEquals(l1, l2);

        Assert.assertFalse(l1.equals(l3));
        Assert.assertFalse(l2.equals(l3));

        l1.put(this.keyE, this.value4);
        l2.put(this.keyE, this.value4);

        Assert.assertEquals(l1.hashCode(), l2.hashCode());
        Assert.assertEquals(l1, l2);
    }

    /*! #if ($TemplateOptions.VTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testBug_HPPC37()
    {
        final KTypeVTypeIdentityHashMap<KType, VType> l1 = KTypeVTypeIdentityHashMap.from(
                asArrayObjects(this.key1),
                newvArray(this.value1));

        final KTypeVTypeIdentityHashMap<KType, VType> l2 = KTypeVTypeIdentityHashMap.from(
                asArrayObjects(this.key2),
                newvArray(this.value1));

        Assert.assertFalse(l1.equals(l2));
        Assert.assertFalse(l2.equals(l1));
    }

    /*! #if ($TemplateOptions.isKType("int", "short", "byte", "long", "Object") &&
             $TemplateOptions.isVType("int", "short", "byte", "long", "Object")) !*/
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

    /*! #end !*/

    private KTypeVTypeIdentityHashMap<KType, VType> createMapWithRandomData(final int size, final long randomSeed)
    {

        final Random prng = new Random(randomSeed);

        final KTypeVTypeIdentityHashMap<KType, VType> newMap = KTypeVTypeIdentityHashMap.newInstance(Containers.DEFAULT_EXPECTED_ELEMENTS,
                HashContainers.DEFAULT_LOAD_FACTOR);

        for (int i = 0; i < size; i++) {

            newMap.put(cast(prng.nextInt()), vcast(prng.nextInt()));
        }

        return newMap;
    }

    @Repeat(iterations = 5)
    @Test
    public void testForEachProcedureWithException()
    {
        final Random randomVK = RandomizedTest.getRandom();

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

        final KTypeVTypeIdentityHashMap<Object, VType> newMap = KTypeVTypeIdentityHashMap.newInstance();

        //add a ordered number of key/values pairs (needed for IdentityMap here)
        //use the same value for keys and values to ease later analysis

        newMap.put(this.keyE, this.value0);

        for (int i = 1; i < NB_ELEMENTS; i++) {

            final Integer KVpair = i;

            newMap.put(KVpair, vcast(KVpair));
        }

        //List the keys in the reverse-order of the internal buffer, since forEach() is iterating in reverse also:
        final ArrayList<Object> keyList = new ArrayList<Object>();
        final ArrayList<Integer> valueList = new ArrayList<Integer>();

        if (newMap.allocatedDefaultKey) {

            keyList.add(this.keyE);
            valueList.add(0);
        }

        //Test forEach predicate and stop at each key in turn.
        final ArrayList<Object> keyListTest = new ArrayList<Object>();
        final ArrayList<Integer> valueListTest = new ArrayList<Integer>();

        for (int k = newMap.keys.length - 1; k >= 0; k--) {

            if (is_allocated(k, newMap.keys)) {

                keyList.add(newMap.keys[k]);
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

            if (newMap.allocatedDefaultKey) {

                keyList.add(this.keyE);
                valueList.add(0);
            }

            for (int k = newMap.keys.length - 1; k >= 0; k--) {

                if (is_allocated(k, newMap.keys)) {

                    keyList.add(newMap.keys[k]);
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
                    TestUtils.assertEquals2(valueList.get(j), valueListTest.get(j));
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
                    TestUtils.assertEquals2(valueList.get(j), valueListTest.get(j));
                }
            } //end finally

            //C) Run values().forEach(VType) Beware, the values are iterated in-order this time !
            keyListTest.clear();
            valueListTest.clear();
            keyList.clear();
            valueList.clear();

            if (newMap.allocatedDefaultKey) {

                keyList.add(this.keyE);
                valueList.add(0);
            }

            for (int k = 0; k < newMap.keys.length; k++) {

                if (is_allocated(k, newMap.keys)) {

                    keyList.add(newMap.keys[k]);
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
                    TestUtils.assertEquals2(valueList.get(j), valueListTest.get(j));
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
        /*! #if ($TemplateOptions.isKType("GENERIC", "int", "long", "float", "double") &&
                $TemplateOptions.isVType("GENERIC", "int", "long", "float", "double")) !*/
        final int NB_ELEMENTS = 2000;
        /*!
        #elseif($TemplateOptions.isKType("short", "char") && $TemplateOptions.isVType("short", "char"))
             final int NB_ELEMENTS = 1000;
            #else
             final int NB_ELEMENTS = 126;
        #end !*/

        final KTypeVTypeIdentityHashMap<Object, VType> newMap = KTypeVTypeIdentityHashMap.newInstance();

        ///add a ordered number of key/values pairs (needed for IdentityMap here)
        //use the same value for keys and values to ease later analysis

        newMap.put(this.keyE, this.value0);

        for (int i = 1; i < NB_ELEMENTS; i++) {

            final Integer KVpair = i;

            newMap.put(KVpair, vcast(KVpair));
        }

        //List the keys in the reverse-order of the internal buffer, since forEach() is iterating in reverse also:
        final ArrayList<Object> keyList = new ArrayList<Object>();
        final ArrayList<Integer> valueList = new ArrayList<Integer>();

        if (newMap.allocatedDefaultKey) {

            keyList.add(this.keyE);
            valueList.add(0);
        }

        for (int i = newMap.keys.length - 1; i >= 0; i--) {

            if (is_allocated(i, newMap.keys)) {

                keyList.add(newMap.keys[i]);
                valueList.add(vcastType(newMap.values[i]));
            }
        }

        //Test forEach predicate and stop at each key in turn.
        final ArrayList<Object> keyListTest = new ArrayList<Object>();
        final ArrayList<Integer> valueListTest = new ArrayList<Integer>();

        keyListTest.clear();
        valueListTest.clear();

        //A) Run forEach(KTypeVType)

        newMap.forEach(new KTypeVTypeProcedure<Object, VType>() {

            @Override
            public void apply(final Object key, final VType value)
            {
                keyListTest.add(key);
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
                keyListTest.add(key);
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

        if (newMap.allocatedDefaultKey) {

            keyList.add(this.keyE);
            valueList.add(0);
        }

        for (int k = 0; k < newMap.keys.length; k++) {

            if (is_allocated(k, newMap.keys)) {

                keyList.add(newMap.keys[k]);
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

    @Repeat(iterations = 5)
    @Test
    public void testForEachPredicate()
    {
        final Random randomVK = RandomizedTest.getRandom();

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

        final KTypeVTypeIdentityHashMap<Object, VType> newMap = KTypeVTypeIdentityHashMap.newInstance();

        //add a ordered number of key/values pairs (needed for IdentityMap here)
        //use the same value for keys and values to ease later analysis

        newMap.put(this.keyE, this.value0);

        for (int i = 1; i < NB_ELEMENTS; i++) {

            final Integer KVpair = i;

            newMap.put(KVpair, vcast(KVpair));
        }

        //List the keys in the reverse-order of the internal buffer, since forEach() is iterating in reverse also:
        final ArrayList<Object> keyList = new ArrayList<Object>();
        final ArrayList<Integer> valueList = new ArrayList<Integer>();

        if (newMap.allocatedDefaultKey) {

            keyList.add(this.keyE);
            valueList.add(0);
        }

        //Test forEach predicate and stop at each key in turn.
        final ArrayList<Object> keyListTest = new ArrayList<Object>();
        final ArrayList<Integer> valueListTest = new ArrayList<Integer>();

        for (int k = newMap.keys.length - 1; k >= 0; k--) {

            if (is_allocated(k, newMap.keys)) {

                keyList.add(newMap.keys[k]);
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

            if (newMap.allocatedDefaultKey) {

                keyList.add(this.keyE);
                valueList.add(0);
            }

            for (int k = newMap.keys.length - 1; k >= 0; k--) {

                if (is_allocated(k, newMap.keys)) {

                    keyList.add(newMap.keys[k]);
                    valueList.add(vcastType(newMap.values[k]));
                }
            }

            //A) Run forEach(KTypeVType)

            newMap.forEach(new KTypeVTypePredicate<Object, VType>() {

                @Override
                public boolean apply(final Object key, final VType value)
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
                //compare by reference !
                Assert.assertTrue(keyList.get(j) == keyListTest.get(j));
                TestUtils.assertEquals2(valueList.get(j), valueListTest.get(j));
            }

            //B) Run keys().forEach(KType)
            keyListTest.clear();
            valueListTest.clear();

            newMap.keys().forEach(new KTypePredicate<Object>() {

                @Override
                public boolean apply(final Object key)
                {
                    keyListTest.add(key);

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
                TestUtils.assertEquals2(valueList.get(j), valueListTest.get(j));
            }

            //C) Run values().forEach(VType) : Beware, values are iterated in-order !
            keyListTest.clear();
            valueListTest.clear();
            keyList.clear();
            valueList.clear();

            if (newMap.allocatedDefaultKey) {

                keyList.add(this.keyE);
                valueList.add(0);
            }

            for (int k = 0; k < newMap.keys.length; k++) {

                if (is_allocated(k, newMap.keys)) {

                    keyList.add(newMap.keys[k]);
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
                TestUtils.assertEquals2(valueList.get(j), valueListTest.get(j));
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

        final KTypeVTypeIdentityHashMap<Object, VType> newMap = KTypeVTypeIdentityHashMap.newInstance();

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

        } //end for

        //objects are all different, so size is really NB_ELEMENTS
        Assert.assertEquals(NB_ELEMENTS, newMap.size());
    }

    private KTypeVTypeIdentityHashMap<KType, VType> createMapWithOrderedData(final int size)
    {
        final KTypeVTypeIdentityHashMap<KType, VType> newMap = KTypeVTypeIdentityHashMap.newInstance(Containers.DEFAULT_EXPECTED_ELEMENTS,
                HashContainers.DEFAULT_LOAD_FACTOR);

        for (int i = 0; i < size; i++)
        {
            newMap.put(cast(i), vcast(i));
        }

        return newMap;
    }

    private boolean is_allocated(final int slot, final Object[] keys) {

        return !Intrinsics.isEmptyKey(keys[slot]);
    }
}
