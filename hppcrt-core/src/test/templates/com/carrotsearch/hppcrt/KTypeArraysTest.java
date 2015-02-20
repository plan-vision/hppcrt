package com.carrotsearch.hppcrt;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.carrotsearch.hppcrt.lists.KTypeArrayList;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.annotations.Seed;

/**
 * Unit tests for {@link KTypeArrays}.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeArraysTest<KType> extends AbstractKTypeTest<KType>
{
    //this is a prime
    private static final int COUNT_PRIME = (int) 1020379L;

    @BeforeClass
    public static void configure()
    {
        IteratorPool.configureInitialPoolSize(8);
    }

    /* */
    @Before
    public void initialize()
    {
        // nothing for now
    }

    @After
    public void checkConsistency()
    {
        // nothing for now
    }

    /* */
    @Repeat(iterations = 100)
    @Test
    public void testRotate()
    {
        final int NB_ELEMENTS = (int) 50.0;

        final int endRotateRange = RandomizedTest.randomIntBetween(3, NB_ELEMENTS);
        final int startRotateRange = RandomizedTest.randomIntBetween(0, endRotateRange - 1);

        if (endRotateRange - startRotateRange < 3) {

            return;
        }

        final int middleRange = RandomizedTest.randomIntBetween(startRotateRange + 1, endRotateRange - 1);

        //A) re-construct the rotated ranges
        final KTypeArrayList<KType> slice1 = new KTypeArrayList<KType>(NB_ELEMENTS);
        final KTypeArrayList<KType> slice2 = new KTypeArrayList<KType>(NB_ELEMENTS);

        for (int i = startRotateRange; i < middleRange; i++) {

            slice1.add(cast(i));
        }

        for (int i = middleRange; i < endRotateRange; i++) {

            slice2.add(cast(i));
        }

        //A) Reference Array
        final KTypeArrayList<KType> refArray = new KTypeArrayList<KType>(NB_ELEMENTS);

        for (int i = 0; i < startRotateRange; i++) {

            refArray.add(cast(i));
        }

        refArray.addAll(slice2);
        refArray.addAll(slice1);

        for (int i = endRotateRange; i < NB_ELEMENTS; i++) {

            refArray.add(cast(i));
        }

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        final Object[] refArrayBuffer = refArray.toArray();
        /*! #else
        final KType[] refArrayBuffer = refArray.toArray();
        #end !*/

        //B) Compare with a test array
        final KTypeArrayList<KType> testArray = new KTypeArrayList<KType>(NB_ELEMENTS);

        for (int i = 0; i < NB_ELEMENTS; i++) {

            testArray.add(cast(i));
        }

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        final Object[] testArrayBuffer = testArray.toArray();
        /*! #else
        final KType[] testArrayBuffer = testArray.toArray();
        #end !*/

        //C) Rotate Container
        KTypeArrays.rotate(testArray, startRotateRange, middleRange, endRotateRange);

        //check
        TestUtils.assertListEquals(refArray.toArray(), testArray.toArray());

        //D) Rotate Array
        KTypeArrays.rotate(testArrayBuffer, startRotateRange, middleRange, endRotateRange);

        //check
        TestUtils.assertListEquals(refArrayBuffer, testArrayBuffer);
    }

    /* */
    @Repeat(iterations = 100)
    @Test
    public void testReverse()
    {
        final int NB_ELEMENTS = (int) 50.0;

        final int endRange = RandomizedTest.randomIntBetween(3, NB_ELEMENTS);
        final int startRange = RandomizedTest.randomIntBetween(0, endRange - 1);

        if (endRange - startRange < 3) {

            return;
        }

        //A) re-construct the reversed range
        final KTypeArrayList<KType> slice1Reversed = new KTypeArrayList<KType>(NB_ELEMENTS);

        for (int i = endRange - 1; i >= startRange; i--) {

            slice1Reversed.add(cast(i));
        }

        //A) Reference Array
        final KTypeArrayList<KType> refArray = new KTypeArrayList<KType>(NB_ELEMENTS);

        for (int i = 0; i < startRange; i++) {

            refArray.add(cast(i));
        }

        refArray.addAll(slice1Reversed);

        for (int i = endRange; i < NB_ELEMENTS; i++) {

            refArray.add(cast(i));
        }

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        final Object[] refArrayBuffer = refArray.toArray();
        /*! #else
        final KType[] refArrayBuffer = refArray.toArray();
        #end !*/

        //B) Compare with a test array
        final KTypeArrayList<KType> testArray = new KTypeArrayList<KType>(NB_ELEMENTS);

        for (int i = 0; i < NB_ELEMENTS; i++) {

            testArray.add(cast(i));
        }

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        final Object[] testArrayBuffer = testArray.toArray();
        /*! #else
        final KType[] testArrayBuffer = testArray.toArray();
        #end !*/

        //C) Reverse Container
        KTypeArrays.reverse(testArray, startRange, endRange);

        //check
        TestUtils.assertListEquals(refArray.toArray(), testArray.toArray());

        //D) Reverse Array
        KTypeArrays.reverse(testArrayBuffer, startRange, endRange);

        //check
        TestUtils.assertListEquals(refArrayBuffer, testArrayBuffer);
    }

    @Test
    public void testBlankArray()
    {
        KType[] testArray = null;

        //a) Blank all a prime
        testArray = buildArray(KTypeArraysTest.COUNT_PRIME);

        KTypeArrays.blankArray(testArray, 0, testArray.length);

        //Check
        checkArray(testArray, 0, testArray.length);

        //b) Blank all a 1024 multiple
        testArray = buildArray(1024 * 991);
        KTypeArrays.blankArray(testArray, 0, testArray.length);

        //Check
        checkArray(testArray, 0, testArray.length);

        //c) Blank a void array
        testArray = buildArray(0);
        KTypeArrays.blankArray(testArray, 0, testArray.length);

        //Check
        checkArray(testArray, 0, testArray.length);

        //c-1) Blank an array < 1024
        testArray = buildArray(991);
        KTypeArrays.blankArray(testArray, 0, testArray.length);

        //Check
        checkArray(testArray, 0, testArray.length);

        //d) Blank a null range (no change)
        testArray = buildArray(1136287);

        //d-1)
        KTypeArrays.blankArray(testArray, 0, 0);
        //Check
        checkArray(testArray, 0, 0);

        //d-2)
        KTypeArrays.blankArray(testArray, 1024, 1024);
        //Check
        checkArray(testArray, 1024, 1024);

        //d-3)
        KTypeArrays.blankArray(testArray, 2 * 1024, 2 * 1024);
        //Check
        checkArray(testArray, 2 * 1024, 2 * 1024);

        //d-4)
        KTypeArrays.blankArray(testArray, 11 * 1024, 11 * 1024);
        //Check
        checkArray(testArray, 11 * 1024, 11 * 1024);

        //d-5)
        KTypeArrays.blankArray(testArray, 1, 1);
        //Check
        checkArray(testArray, 1, 1);

        //d-6)
        KTypeArrays.blankArray(testArray, 3, 3);
        //Check
        checkArray(testArray, 3, 3);

        //d-7)
        KTypeArrays.blankArray(testArray, 2027, 2027);
        //Check
        checkArray(testArray, 2027, 2027);

        //d-8)
        KTypeArrays.blankArray(testArray, 2609, 2609);
        //Check
        checkArray(testArray, 2609, 2609);

        //d-9)
        KTypeArrays.blankArray(testArray, 99529, 99529);
        //Check
        checkArray(testArray, 99529, 99529);

        //d-10)
        KTypeArrays.blankArray(testArray, testArray.length - 1, testArray.length - 1);
        //Check
        checkArray(testArray, testArray.length - 1, testArray.length - 1);

        //d-11)
        KTypeArrays.blankArray(testArray, testArray.length - 2, testArray.length - 2);
        //Check
        checkArray(testArray, testArray.length - 2, testArray.length - 2);

        //d-12)
        KTypeArrays.blankArray(testArray, testArray.length - 3, testArray.length - 3);
        //Check
        checkArray(testArray, testArray.length - 3, testArray.length - 3);

        //e) Blank non-null subrange

        //e-1) first element
        testArray = buildArray(1284047);
        KTypeArrays.blankArray(testArray, 0, 1);
        //Check
        checkArray(testArray, 0, 1);

        //e-2) last element
        testArray = buildArray(1284047);
        KTypeArrays.blankArray(testArray, testArray.length - 1, testArray.length);
        //Check
        checkArray(testArray, testArray.length - 1, testArray.length);

        //e-3) some range starting from start element
        testArray = buildArray(1284047);
        KTypeArrays.blankArray(testArray, 0, 0 + 1282703);
        //Check
        checkArray(testArray, 0, 0 + 1282703);

        //e-4) some range ending at the last element
        testArray = buildArray(1284047);
        KTypeArrays.blankArray(testArray, 1282703, 1284047);
        //Check
        checkArray(testArray, 1282703, 1284047);

        //e-5) Some range in the middle
        testArray = buildArray(1292251);
        KTypeArrays.blankArray(testArray, 179939, 185533);
        //Check
        checkArray(testArray, 179939, 185533);

        testArray = buildArray(876257);
        KTypeArrays.blankArray(testArray, 1024, 3 * 1024);
        //Check
        checkArray(testArray, 1024, 3 * 1024);

        testArray = buildArray(434293);
        KTypeArrays.blankArray(testArray, 1024, 11 * 1024 + 17);
        //Check
        checkArray(testArray, 1024, 11 * 1024 + 17);

        testArray = buildArray(811);
        KTypeArrays.blankArray(testArray, 111, 222);
        //Check
        checkArray(testArray, 111, 222);
    }

    private KType[] buildArray(final int size) {

        final KType[] testArray = Intrinsics.<KType[]> newKTypeArray(size);

        for (int i = 0; i < size; i++) {

            //use autoboxing
            testArray[i] = cast(i);
        }

        return testArray;
    }

    private void checkArray(final KType[] arrayToTest, final int startBlankingIndex, final int endBlankingIndex) {

        for (int i = 0; i < arrayToTest.length; i++) {

            if ((i >= startBlankingIndex) && (i < endBlankingIndex)) {

                //this is blanked
                if (Intrinsics.defaultKTypeValue() != arrayToTest[i]) {
                    Assert.assertTrue("value = " + castType(arrayToTest[i]), false);
                }
            }
            else {
                //check the original pattern, double-cast for narrowing conversions
                if (castType(arrayToTest[i]) != castType(cast(i))) {
                    Assert.assertTrue("value = " + castType(arrayToTest[i]), false);
                }
            }
        }
    }
}
