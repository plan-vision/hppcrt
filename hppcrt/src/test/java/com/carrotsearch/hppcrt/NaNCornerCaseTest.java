package com.carrotsearch.hppcrt;

import java.util.Arrays;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.carrotsearch.hppcrt.lists.DoubleArrayList;
import com.carrotsearch.hppcrt.lists.FloatArrayList;
import com.carrotsearch.hppcrt.maps.DoubleObjectHashMap;
import com.carrotsearch.hppcrt.maps.FloatObjectHashMap;
import com.carrotsearch.hppcrt.maps.IntDoubleHashMap;
import com.carrotsearch.hppcrt.maps.IntFloatHashMap;
import com.carrotsearch.hppcrt.sets.DoubleHashSet;
import com.carrotsearch.hppcrt.sets.FloatHashSet;
import com.carrotsearch.hppcrt.sorting.DoubleSort;
import com.carrotsearch.hppcrt.sorting.FloatSort;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;

public class NaNCornerCaseTest extends RandomizedTest
{
    private double[] nanListD;
    private float[] nanListF;

    @Before
    public void init() {
        final double NaN1d = Double.NaN;

        final double NaN2d = Double.longBitsToDouble(0x7ff8000000000001L);
        final double NaN3d = Double.longBitsToDouble(0x7ff8000000000010L);
        final double NaN4d = Double.longBitsToDouble(0x7ff8000000000011L);
        final double NaN5d = Double.longBitsToDouble(0x7ff8000000000111L);
        final double NaN6d = Double.longBitsToDouble(0x7ff8000000001111L);
        final double NaN7d = Double.longBitsToDouble(0x7ff8000000011111L);
        final double NaN8d = Double.longBitsToDouble(0x7ff8000000111111L);
        final double NaN9d = Double.longBitsToDouble(0x7ff8000000101101L);
        final double NaN10d = Double.longBitsToDouble(0x7ff8000011111111L);

        this.nanListD = new double[] { NaN1d, NaN2d, NaN3d, NaN4d, NaN5d, NaN6d, NaN7d, NaN8d, NaN9d, NaN10d };

        final float NaN1f = Float.NaN;

        final float NaN2f = Float.intBitsToFloat(0x7fc00011);
        final float NaN3f = Float.intBitsToFloat(0x7fc00001);
        final float NaN4f = Float.intBitsToFloat(0x7fc00010);
        final float NaN5f = Float.intBitsToFloat(0x7fc00101);
        final float NaN6f = Float.intBitsToFloat(0x7fc00110);
        final float NaN7f = Float.intBitsToFloat(0x7fc00111);
        final float NaN8f = Float.intBitsToFloat(0x7fc01011);
        final float NaN9f = Float.intBitsToFloat(0x7fc01111);
        final float NaN10f = Float.intBitsToFloat(0x7fc10001);

        this.nanListF = new float[] { NaN1f, NaN2f, NaN3f, NaN4f, NaN5f, NaN6f, NaN7f, NaN8f, NaN9f, NaN10f };
    }

    /** @see "http://issues.carrot2.org/browse/HPPC-93" */
    @Test
    public void testDoubleNaNInfinitesComparison()
    {
        System.out.println(Arrays.toString(this.nanListD));

        for (int i = 0; i < this.nanListD.length; i++) {

            System.out.println(Double.doubleToLongBits(this.nanListD[i]) + " raw : " + Double.doubleToRawLongBits(this.nanListD[i]));

            if (i < this.nanListD.length - 1) {

                //All NaN are equal by doubleToLongBits
                Assert.assertTrue(Double.doubleToLongBits(this.nanListD[i]) == Double.doubleToLongBits(this.nanListD[i + 1]));

                //Even if they are indeed binary different
                Assert.assertFalse(Double.doubleToRawLongBits(this.nanListD[i]) == Double.doubleToRawLongBits(this.nanListD[i + 1]));

                //All NaNs are equals by compare
                Assert.assertTrue(Double.compare(this.nanListD[i], this.nanListD[i + 1]) == 0);
            }

            //Double NaN are considered superior to everything by compare(), so support ordering
            Assert.assertTrue(Double.compare(this.nanListD[i], Double.NEGATIVE_INFINITY) > 0);
            Assert.assertTrue(Double.compare(this.nanListD[i], 0.0) > 0);
            Assert.assertTrue(Double.compare(this.nanListD[i], 10.0) > 0);
            Assert.assertTrue(Double.compare(this.nanListD[i], 1e89) > 0);
            Assert.assertTrue(Double.compare(this.nanListD[i], Double.POSITIVE_INFINITY) > 0);
        } //end for
    }

    @Repeat(iterations = 20)
    @Test
    public void testDoubleNaNSortingArray()
    {
        //Try to sort
        final int NB_SORT_ELEMENTS = (int) 1e6;

        final DoubleArrayList referenceList = new DoubleArrayList(NB_SORT_ELEMENTS);

        final Random prng = RandomizedTest.getRandom();

        while (true) {

            final boolean AddNaN = (prng.nextInt(13) % 5 == 0);

            if (!AddNaN) {

                referenceList.add(prng.nextDouble() * NB_SORT_ELEMENTS);

            }
            else {

                final int chunkSize = RandomizedTest.randomIntBetween(1, 5);

                for (int j = 0; j < chunkSize; j++) {

                    referenceList.add(this.nanListD[j]);
                }
            }

            if (referenceList.size() > NB_SORT_ELEMENTS) {

                break;
            }
        } //end while

        //duplicate
        final double[] testArray = referenceList.toArray();
        final double[] referenceArray = referenceList.toArray();

        //Compare the sorts JDK vs. HPPC
        Arrays.sort(referenceArray);
        DoubleSort.quicksort(testArray);

        for (int i = 0; i < referenceArray.length; i++) {

            Assert.assertEquals(Double.doubleToLongBits(referenceArray[i]), Double.doubleToLongBits(testArray[i]));
            Assert.assertTrue(Double.compare(referenceArray[i], testArray[i]) == 0);

            if (!Double.isNaN(referenceArray[i])) {

                Assert.assertTrue(referenceArray[i] == testArray[i]);
            }
        }
    }

    @Repeat(iterations = 20)
    @Test
    public void testFloatNaNSortingArray()
    {
        //Try to sort
        final int NB_SORT_ELEMENTS = (int) 1e6;

        final FloatArrayList referenceList = new FloatArrayList(NB_SORT_ELEMENTS);

        final Random prng = RandomizedTest.getRandom();

        while (true) {

            final boolean AddNaN = (prng.nextInt(13) % 5 == 0);

            if (!AddNaN) {

                referenceList.add(prng.nextFloat() * NB_SORT_ELEMENTS);
            }
            else {

                final int chunkSize = RandomizedTest.randomIntBetween(1, 5);

                for (int j = 0; j < chunkSize; j++) {

                    referenceList.add(this.nanListF[j]);
                }
            }

            if (referenceList.size() > NB_SORT_ELEMENTS) {

                break;
            }
        } //end while

        //duplicate
        final float[] testArray = referenceList.toArray();
        final float[] referenceArray = referenceList.toArray();

        //Compare the sorts JDK vs. HPPC
        Arrays.sort(referenceArray);
        FloatSort.quicksort(testArray);

        for (int i = 0; i < NB_SORT_ELEMENTS; i++) {

            Assert.assertEquals(Float.floatToIntBits(referenceArray[i]), Float.floatToIntBits(testArray[i]));
            Assert.assertTrue(Float.compare(referenceArray[i], testArray[i]) == 0);

            if (!Float.isNaN(referenceArray[i])) {

                Assert.assertTrue(referenceArray[i] == testArray[i]);
            }
        }
    }

    /** @see "http://issues.carrot2.org/browse/HPPC-93" */
    @Test
    public void testFloatNaNInfinitesComparison()
    {
        System.out.println(Arrays.toString(this.nanListF));

        for (int i = 0; i < this.nanListF.length; i++) {

            System.out.println(Float.floatToIntBits(this.nanListF[i]) + " raw : " + Float.floatToRawIntBits(this.nanListF[i]));

            if (i < this.nanListF.length - 1) {

                //All NaN are equal by floatToIntBits
                Assert.assertTrue(Float.floatToIntBits(this.nanListF[i]) == Float.floatToIntBits(this.nanListF[i + 1]));

                //Even if they are indeed binary different
                Assert.assertFalse(Float.floatToRawIntBits(this.nanListF[i]) == Float.floatToRawIntBits(this.nanListF[i + 1]));

                //All NaNs are equals by compare
                Assert.assertTrue(Float.compare(this.nanListF[i], this.nanListF[i + 1]) == 0);
            }

            //Float NaN are considered superior to everything by compare(), so support ordering
            Assert.assertTrue(Float.compare(this.nanListF[i], Float.NEGATIVE_INFINITY) > 0);
            Assert.assertTrue(Float.compare(this.nanListF[i], 0f) > 0);
            Assert.assertTrue(Float.compare(this.nanListF[i], 10f) > 0);
            Assert.assertTrue(Float.compare(this.nanListF[i], 44887774f) > 0);
            Assert.assertTrue(Float.compare(this.nanListF[i], Float.POSITIVE_INFINITY) > 0);
        } //end for
    }

    /** @see "http://issues.carrot2.org/browse/HPPC-93" */
    @Test
    public void testNaNAsDoubleKey()
    {
        final DoubleObjectMap<String> map = DoubleObjectHashMap.newInstance();
        map.put(Double.NaN, "a");
        map.put(Double.NaN, "b");

        Assert.assertEquals(1, map.size());
        Assert.assertEquals("b", map.get(Double.NaN));

        map.put(Double.longBitsToDouble(0xfff8000000000000L), "c");
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("c", map.get(Double.NaN));
        Assert.assertEquals(
                (Double) map.keys().iterator().next().value,
                (Double) Double.NaN);

        final DoubleHashSet set = DoubleHashSet.newInstance();
        set.add(Double.NaN);
        set.add(Double.NaN);
        set.add(Double.longBitsToDouble(0xfff8000000000000L));
        Assert.assertEquals(1, set.size());
        Assert.assertEquals(
                (Double) set.iterator().next().value,
                (Double) Double.NaN);
    }

    /** @see "http://issues.carrot2.org/browse/HPPC-93" */
    @Test
    public void testNaNAsFloatKey()
    {
        final FloatObjectMap<String> map = FloatObjectHashMap.newInstance();
        map.put(Float.NaN, "a");
        map.put(Float.NaN, "b");

        Assert.assertEquals(1, map.size());
        Assert.assertEquals("b", map.get(Float.NaN));

        map.put(Float.intBitsToFloat(0xfff80000), "c");
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("c", map.get(Float.NaN));
        Assert.assertEquals(
                (Float) map.keys().iterator().next().value,
                (Float) Float.NaN);

        final FloatHashSet set = FloatHashSet.newInstance();
        set.add(Float.NaN);
        set.add(Float.NaN);
        set.add(Float.intBitsToFloat(0xfff80000));
        Assert.assertEquals(1, set.size());
        Assert.assertEquals(
                (Float) set.iterator().next().value,
                (Float) Float.NaN);
    }

    /** @see "http://issues.carrot2.org/browse/HPPC-93" */
    @Test
    public void testNaNAsValue()
    {
        {
            final IntDoubleMap m1 = IntDoubleHashMap.newInstance();
            m1.put(1, Double.NaN);
            final IntDoubleMap m2 = IntDoubleHashMap.newInstance();
            m2.put(1, Double.NaN);
            Assert.assertEquals(m1, m2);
        }

        {
            final IntFloatMap m1 = IntFloatHashMap.newInstance();
            m1.put(1, Float.NaN);
            final IntFloatMap m2 = IntFloatHashMap.newInstance();
            m2.put(1, Float.NaN);
            Assert.assertEquals(m1, m2);
        }

        {
            final FloatArrayList list = FloatArrayList.newInstance();
            Assert.assertFalse(list.contains(Float.NaN));
            list.add(0, Float.NaN, 1);
            Assert.assertTrue(list.contains(Float.NaN));
        }

        {
            final DoubleArrayList list = DoubleArrayList.newInstance();
            Assert.assertFalse(list.contains(Double.NaN));
            list.add(0, Double.NaN, 1);
            Assert.assertTrue(list.contains(Double.NaN));
        }

        {
            final DoubleArrayList l1 = DoubleArrayList.newInstance();
            l1.add(0, Double.NaN, 1);
            final DoubleArrayList l2 = DoubleArrayList.newInstance();
            l2.add(0, Double.NaN, 1);
            Assert.assertEquals(l1, l2);
        }
    }
}
