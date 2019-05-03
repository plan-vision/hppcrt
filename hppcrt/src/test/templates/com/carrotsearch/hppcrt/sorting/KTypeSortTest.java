package com.carrotsearch.hppcrt.sorting;

import java.util.Arrays;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.strategies.*;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;

/**
 * Unit tests for {@link KTypeSort}.
 */
public class KTypeSortTest<KType> extends AbstractKTypeTest<KType>
{
    enum DataDistribution
    {
        ORDERED, SAWTOOTH, RANDOM, STAGGER, PLATEAU, SHUFFLE
    }

    enum Algorithm
    {
        QUICKSORT, QUICKSORT_COMPARATOR
    }

    public KTypeSortTest()
    {
        //nothing
    }

    @Test
    public void testQuicksort()
    {
        sortCertification(Algorithm.QUICKSORT);
    }

    @Test
    public void testQuicksortComparator()
    {
        sortCertification(Algorithm.QUICKSORT_COMPARATOR);
    }

    @Test
    public void testRandomizedSort()
    {
        //natural ordering comparator
        final KTypeComparator<KType> comp = new KTypeComparator<KType>() {

            @Override
            public int compare(final KType e1, final KType e2)
            {
                int res = 0;

                if (castType(e1) < castType(e2))
                {
                    res = -1;
                }
                else if (castType(e1) > castType(e2))
                {
                    res = 1;
                }

                return res;
            }
        };

        final int TEST_SIZE = 50;
        final int NB_ITERATIONS = (int) 1e6;

        //get a new seed for the current iteration
        final long currentSeed = RandomizedTest.randomLong();

        for (int ii = 0; ii < NB_ITERATIONS; ii++) {

            final int upperRange = RandomizedTest.between(0, TEST_SIZE);
            final int lowerRange = RandomizedTest.between(0, upperRange);

            //A) Sort an array of random values of primitive types

            //A-1) full sort
            KType[] primitiveList = createArrayWithComparableRandomData(TEST_SIZE, currentSeed);
            KType[] primitiveListOriginal = createArrayWithComparableRandomData(TEST_SIZE, currentSeed);
            KTypeSort.quicksort(primitiveList);
            assertOrder(primitiveListOriginal, primitiveList, 0, primitiveList.length);
            //A-2) Partial sort
            primitiveList = createArrayWithComparableRandomData(TEST_SIZE, currentSeed);
            primitiveListOriginal = createArrayWithComparableRandomData(TEST_SIZE, currentSeed);
            KTypeSort.quicksort(primitiveList, lowerRange, upperRange);
            assertOrder(primitiveListOriginal, primitiveList, lowerRange, upperRange);

            //B) Sort with Comparator
            //B-1) Full sort
            KType[] comparatorList = createArrayWithComparableRandomData(TEST_SIZE, currentSeed);
            KType[] comparatorListOriginal = createArrayWithComparableRandomData(TEST_SIZE, currentSeed);
            KTypeSort.quicksort(comparatorList, comp);
            assertOrder(comparatorListOriginal, comparatorList, 0, comparatorList.length);
            //B-2) Partial sort
            comparatorList = createArrayWithComparableRandomData(TEST_SIZE, currentSeed);
            comparatorListOriginal = createArrayWithComparableRandomData(TEST_SIZE, currentSeed);
            KTypeSort.quicksort(comparatorList, lowerRange, upperRange, comp);
            assertOrder(comparatorListOriginal, comparatorList, lowerRange, upperRange);
        }
    }

    ///////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////

    /**
     * Run a "sort certification" test.
     */
    private void sortCertification(final Algorithm algorithm)
    {
        final int[] n_values =
            {
                    100, 1023, 1024, 1025, 1024 * 32
            };

        for (final int n : n_values)
        {
            for (int m = 1; m < 2 * n; m *= 2)
            {
                for (final DataDistribution dist : DataDistribution.values())
                {
                    final KType[] x = generate(dist, n, m);

                    final String testName = dist + "-" + n + "-" + m;
                    testOn(algorithm, x, testName + "-normal");
                    testOn(algorithm, reverse(x, 0, n), testName + "-reversed");
                    testOn(algorithm, reverse(x, 0, n / 2), testName + "-reversed_front");
                    testOn(algorithm, reverse(x, n / 2, n), testName + "-reversed_back");
                    testOn(algorithm, sort(x), testName + "-sorted");
                    testOn(algorithm, dither(x), testName + "-dither");
                }
            }
        }
    }

    /**
     * Generate <code>n</code>-length data set distributed according to <code>dist</code>.
     * 
     * @param m Step for sawtooth, stagger, plateau and shuffle.
     */
    private KType[] generate(final DataDistribution dist, final int n, final int m)
    {
        // Start from a constant seed (repeatable tests).
        final Random rand = new Random(0xBADCAFE);

        //generate an array of KType
        final KType[] x = asArray(new int[n]);

        for (int i = 0, j = 0, k = 1; i < n; i++)
        {
            switch (dist)
            {
            case ORDERED:
                x[i] = cast(i);
                break;
            case SAWTOOTH:
                x[i] = cast(i % m);
                break;
            case RANDOM:
                x[i] = cast(rand.nextInt() % m);
                break;
            case STAGGER:
                x[i] = cast((i * m + i) % n);
                break;
            case PLATEAU:
                x[i] = cast(Math.min(i, m));
                break;
            case SHUFFLE:
                x[i] = cast((rand.nextInt() % m) != 0 ? (j += 2) : (k += 2));
                break;
            default:
                throw new RuntimeException();
            }
        }

        return x;
    }

    private KType[] sort(KType[] x)
    {
        x = copy(x);

        /*! #if (! $TemplateOptions.KTypeBoolean) !*/
        Arrays.sort(x);
        /*! #else
        x = specialBooleanSort(x);
        #end !*/

        return x;
    }

    private boolean[] specialBooleanSort(final boolean[] inputBoolean)
    {
        //sort as is :
        // a) count the number of false : nbFalse
        // b) count the number of true : nbTrue
        //then the sorted result is made of nbFalse "false" elements,
        //followed by nbTrue "true" elements.

        int nbFalse = 0;
        int nbTrue = 0;

        for (int ii = 0; ii < inputBoolean.length; ii++)
        {
            if (inputBoolean[ii])
            {
                nbTrue++;
            }
            else
            {
                nbFalse++;
            }
        }

        //sorted
        final boolean[] out = new boolean[inputBoolean.length];

        for (int ii = 0; ii < nbFalse; ii++)
        {
            out[ii] = false;
        }

        for (int ii = nbFalse; ii < nbFalse + nbTrue; ii++)
        {
            out[ii] = true;
        }

        return out;
    }

    private KType[] dither(KType[] x)
    {
        x = copy(x);
        for (int i = 0; i < x.length; i++) {
            x[i] = cast(castType(x[i]) + i % 5);
        }

        return x;
    }

    private KType[] reverse(KType[] x, final int start, final int end)
    {
        x = copy(x);
        for (int i = start, j = end - 1; i < j; i++, j--)
        {
            final KType v = x[i];
            x[i] = x[j];
            x[j] = v;
        }
        return x;
    }

    @SuppressWarnings("unchecked")
    private void testOn(final Algorithm algo, final KType[] order, final String testName)
    {
        //natural ordering comparator
        final KTypeComparator<KType> comp = new KTypeComparator<KType>() {

            @Override
            public int compare(final KType e1, final KType e2)
            {
                int res = 0;

                if (castType(e1) < castType(e2))
                {
                    res = -1;
                }
                else if (castType(e1) > castType(e2))
                {
                    res = 1;
                }

                return res;
            }
        };

        switch (algo)
        {
        case QUICKSORT:
            //the supplied KType[] are also Numbers in generics, so are
            //Comparable
            final KType[] orderComparable = newArray(order);

            KTypeSort.quicksort(orderComparable);
            assertOrder(orderComparable);
            break;

        case QUICKSORT_COMPARATOR:
            KTypeSort.quicksort(order, comp);
            assertOrder(order);
            break;
        default:
            Assert.fail();
            throw new RuntimeException();
        }
    }
}
