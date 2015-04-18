package com.carrotsearch.hppcrt;

import static com.carrotsearch.hppcrt.TestUtils.assertEquals2;

import org.junit.Assert;
import org.junit.Test;

import com.carrotsearch.hppcrt.lists.*;
import com.carrotsearch.hppcrt.maps.*;
import com.carrotsearch.hppcrt.sets.*;
import com.carrotsearch.randomizedtesting.RandomizedTest;

/**
 * Various API expectations from generated classes.
 */
public class APIExpectationsTest extends RandomizedTest
{
    public volatile int[] t1;

    @Test
    public void testRemoveAllFromMap()
    {
        final ObjectIntOpenHashMap<Integer> list = new ObjectIntOpenHashMap<Integer>();
        list.put(1, 1);
        list.put(2, 2);
        list.put(3, 3);

        // Same type.
        final ObjectOpenHashSet<Integer> other1 = new ObjectOpenHashSet<Integer>();
        other1.add(1);
        list.removeAll(other1);

        // Supertype.
        final ObjectArrayList<Integer> other2 = new ObjectArrayList<Integer>();
        other2.add(1);
        list.removeAll(other2);

        // Object
        final ObjectArrayList<Object> other3 = new ObjectArrayList<Object>();
        other3.add(1);
        list.removeAll(other3);
    }

    @Test
    public void testRemoveAllWithLookupContainer()
    {
        final ObjectArrayList<Integer> list = new ObjectArrayList<Integer>();
        list.add(1);
        list.add(2);
        list.add(3);

        // Same type.
        final ObjectOpenHashSet<Integer> other1 = new ObjectOpenHashSet<Integer>();
        other1.add(1);
        list.removeAll(other1);

        // Supertype.
        final ObjectOpenHashSet<Number> other2 = new ObjectOpenHashSet<Number>();
        other2.add(1);
        list.removeAll(other2);

        // Object
        final ObjectOpenHashSet<Object> other3 = new ObjectOpenHashSet<Object>();
        other3.add(1);
        list.removeAll(other3);
    }

    @Test
    public void testToArrayWithClass()
    {
        final ObjectArrayDeque<Integer> l1 = ObjectArrayDeque.from(1, 2, 3);
        final Integer[] result1 = l1.toArray(Integer.class);
        Assert.assertArrayEquals(new Integer[] { 1, 2, 3 }, result1);

        final Number[] result2 = l1.toArray(Number.class);
        Assert.assertEquals(Number[].class, result2.getClass());
        Assert.assertArrayEquals(new Number[] { 1, 2, 3 }, result2);

        //To test if overriden toArray() from Stack works too:
        final ObjectStack<Integer> l2 = ObjectStack.from(1, 2, 3, 4);
        final Integer[] result3 = l2.toArray(Integer.class);
        Assert.assertArrayEquals(new Integer[] { 4, 3, 2, 1 }, result3);

        final Number[] result4 = l2.toArray(Number.class);
        Assert.assertEquals(Number[].class, result4.getClass());
        Assert.assertArrayEquals(new Number[] { 4, 3, 2, 1 }, result4);
    }

    @Test
    public void testPrimitiveToArray()
    {
        this.t1 = IntArrayList.from(1, 2, 3).toArray();
        this.t1 = IntStack.from(1, 2, 3).toArray();
        this.t1 = IntArrayDeque.from(1, 2, 3).toArray();
        this.t1 = IntOpenHashSet.from(1, 2, 3).toArray();

        this.t1 = IntObjectOpenHashMap.from(
                new int[] { 1, 2 }, new Long[] { 1L, 2L }).keys().toArray();
    }

    @Test
    public void testSizeLimitByteArrayList() {
        final ByteArrayList list = new ByteArrayList(0, new ArraySizingStrategy()
        {
            final BoundedProportionalArraySizingStrategy delegate = new BoundedProportionalArraySizingStrategy();

            @Override
            public int grow(final int currentBufferLength, final int elementsCount, final int expectedAdditions)
            {
                final int grow = this.delegate.grow(currentBufferLength, elementsCount, expectedAdditions);
                // System.out.println("Resizing to: " + Integer.toHexString(grow) + " " + grow);
                return grow;
            }
        });

        try {
            while (true) {
                list.add((byte) 0);
            }
        }
        catch (final AssertionError e) {
            Assert.assertEquals(0x7fffffff, list.size());
        }
    }

    @Test
    public void testSizeLimitByteQueue() {
        final ByteArrayDeque queue = new ByteArrayDeque(1, new ArraySizingStrategy()
        {
            final BoundedProportionalArraySizingStrategy delegate = new BoundedProportionalArraySizingStrategy();

            @Override
            public int grow(final int currentBufferLength, final int elementsCount, final int expectedAdditions)
            {
                final int grow = this.delegate.grow(currentBufferLength, elementsCount, expectedAdditions);
                // System.out.println("Resizing to: " + Integer.toHexString(grow) + " " + grow);
                return grow;
            }
        });

        try {
            while (true) {
                queue.addLast((byte) 0);
            }
        }
        catch (final AssertionError e) {
            Assert.assertEquals(0x7fffffff /* Account for an empty slot. */- 1, queue.size());
        }
    }

    @Test
    @SuppressWarnings("unused")
    public void testNewInstance()
    {
        final IntArrayList v1 = IntArrayList.newInstance();
        final ObjectArrayList<Integer> v2 = ObjectArrayList.newInstance();
        final ObjectArrayList<Long> v3 = ObjectArrayList.newInstance();

        final IntStack v4 = IntStack.newInstance();
        final ObjectStack<Integer> v5 = ObjectStack.newInstance();
        final ObjectStack<Long> v6 = ObjectStack.newInstance();

        final IntOpenHashSet v7 = IntOpenHashSet.newInstance();
        final ObjectOpenHashSet<Integer> v8 = ObjectOpenHashSet.newInstance();
        final ObjectOpenHashSet<Long> v9 = ObjectOpenHashSet.newInstance();

        final IntArrayDeque v10 = IntArrayDeque.newInstance();
        final ObjectArrayDeque<Integer> v11 = ObjectArrayDeque.newInstance();
        final ObjectArrayDeque<Long> v12 = ObjectArrayDeque.newInstance();

        final IntIntOpenHashMap v13 = IntIntOpenHashMap.newInstance();
        final ObjectIntOpenHashMap<Integer> v14 = ObjectIntOpenHashMap.newInstance();
        final IntObjectOpenHashMap<Integer> v15 = IntObjectOpenHashMap.newInstance();
    }

    @Test
    public void testObjectToArray()
    {
        isObjectArray(ObjectArrayList.from(1, 2, 3).toArray());
        isObjectArray(ObjectStack.from(1, 2, 3).toArray());
        isObjectArray(ObjectArrayDeque.from(1, 2, 3).toArray());
        isObjectArray(ObjectOpenHashSet.from(1, 2, 3).toArray());

        isObjectArray(ObjectObjectOpenHashMap.from(
                new Integer[] { 1, 2 }, new Long[] { 1L, 2L }).keys().toArray());
    }

    @Test
    public void testWithClassToArray()
    {
        isIntegerArray(ObjectArrayList.from(1, 2, 3).toArray(Integer.class));
        isIntegerArray(ObjectStack.from(1, 2, 3).toArray(Integer.class));
        isIntegerArray(ObjectArrayDeque.from(1, 2, 3).toArray(Integer.class));
        isIntegerArray(ObjectOpenHashSet.from(1, 2, 3).toArray(Integer.class));

        isIntegerArray(ObjectObjectOpenHashMap.from(
                new Integer[] { 1, 2 }, new Long[] { 1L, 2L }).keys().toArray(Integer.class));
    }

    @Test
    public void testWildcards()
    {
        ObjectArrayList<? extends Number> t = ObjectArrayList.from(1, 2, 3);
        isTypeArray(Number.class, t.toArray(Number.class));

        t = ObjectArrayList.from(1L, 2L, 3L);
        isTypeArray(Number.class, t.toArray(Number.class));
    }

    @Test
    public void testPutOrAddOnEqualKeys()
    {
        final ObjectIntOpenHashMap<Integer> map = ObjectIntOpenHashMap.newInstance();

        final Integer k1 = 1;
        final Integer k1b = new Integer(k1.intValue());

        Assert.assertTrue(k1 != k1b);
        TestUtils.assertEquals2(1, map.putOrAdd(k1, 1, 2));
        Assert.assertTrue(map.containsKey(k1b));
        TestUtils.assertEquals2(3, map.putOrAdd(k1b, 1, 2));
    }

    /**
     * Check if the array is indeed of Object component type.
     */
    private void isObjectArray(final Object[] array)
    {
        isTypeArray(Object.class, array);
    }

    /**
     * 
     */
    private void isTypeArray(final Class<?> clazz, final Object[] array)
    {
        Assert.assertEquals(clazz, array.getClass().getComponentType());
    }

    /**
     * Check if the array is indeed of Integer component type.
     */
    private void isIntegerArray(final Integer[] array)
    {
        isTypeArray(Integer.class, array);
    }
}
