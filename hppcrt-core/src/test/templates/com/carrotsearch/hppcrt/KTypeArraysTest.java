package com.carrotsearch.hppcrt;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.carrotsearch.hppcrt.lists.KTypeArrayList;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.annotations.Seed;

// ${TemplateOptions.doNotGenerateKType("BOOLEAN")}
/**
 * Unit tests for {@link KTypeArrays}.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeArraysTest<KType> extends AbstractKTypeTest<KType>
{
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
}
