package com.carrotsearch.hppcrt.sorting;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.carrotsearch.hppcrt.XorShiftRandom;
import com.carrotsearch.hppcrt.strategies.*;

/**
 * Test cases for {@link IndirectSort}.
 */
public class IndirectSortTest
{
    static final int DATA_LENGTH = 1000000;

    /**
     * Implies the same order as the order of indices.
     */
    private static class OrderedInputComparator implements IndirectComparator
    {
        @Override
        public int compare(final int a, final int b)
        {
            if (a < b)
                return -1;
            if (a > b)
                return 1;
            return 0;
        }
    }

    /**
     * Implies reverse order of indices.
     */
    private static class ReverseOrderedInputComparator extends OrderedInputComparator
    {
        @Override
        public int compare(final int a, final int b)
        {
            return -super.compare(a, b);
        }
    }

    enum DataDistribution
    {
        ORDERED, SAWTOOTH, RANDOM, STAGGER, PLATEAU, SHUFFLE
    }

    enum Algorithm
    {
        MERGESORT, MERGESORT_RT, QUICKSORT
    }

    /**
     * Test "certification" program as in Bentley and McIlroy's paper.
     */
    @Test
    public void testSortCertificationMergeSort()
    {
        IndirectSortTest.sortCertification(Algorithm.MERGESORT);
    }

    @Test
    public void testSortCertificationMergeSortRT()
    {
        IndirectSortTest.sortCertification(Algorithm.MERGESORT_RT);
    }

    @Test
    public void testSortCertificationQuicksort()
    {
        IndirectSortTest.sortCertification(Algorithm.QUICKSORT);
    }

    /**
     * Run a "sort certification" test.
     */
    private static void sortCertification(final Algorithm algorithm)
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
                    final int[] x = IndirectSortTest.generate(dist, n, m);

                    final String testName = dist + "-" + n + "-" + m;
                    IndirectSortTest.testOn(algorithm, x, testName + "-normal");
                    IndirectSortTest.testOn(algorithm, IndirectSortTest.reverse(x, 0, n), testName + "-reversed");
                    IndirectSortTest.testOn(algorithm, IndirectSortTest.reverse(x, 0, n / 2), testName + "-reversed_front");
                    IndirectSortTest.testOn(algorithm, IndirectSortTest.reverse(x, n / 2, n), testName + "-reversed_back");
                    IndirectSortTest.testOn(algorithm, IndirectSortTest.sort(x), testName + "-sorted");
                    IndirectSortTest.testOn(algorithm, IndirectSortTest.dither(x), testName + "-dither");
                }
            }
        }
    }

    /**
     * Generate <code>n</code>-length data set distributed according to <code>dist</code>.
     * 
     * @param m Step for sawtooth, stagger, plateau and shuffle.
     */
    private static int[] generate(final DataDistribution dist, final int n, final int m)
    {
        // Start from a constant seed (repeatable tests).
        final Random rand = new Random(0x11223344);
        final int[] x = new int[n];
        for (int i = 0, j = 0, k = 1; i < n; i++)
        {
            switch (dist)
            {
                case ORDERED:
                    x[i] = i;
                    break;
                case SAWTOOTH:
                    x[i] = i % m;
                    break;
                case RANDOM:
                    x[i] = rand.nextInt() % m;
                    break;
                case STAGGER:
                    x[i] = (i * m + i) % n;
                    break;
                case PLATEAU:
                    x[i] = Math.min(i, m);
                    break;
                case SHUFFLE:
                    x[i] = (rand.nextInt() % m) != 0 ? (j += 2) : (k += 2);
                    break;
                default:
                    throw new RuntimeException();
            }
        }

        return x;
    }

    private static int[] sort(int[] x)
    {
        x = IndirectSortTest.copy(x);
        Arrays.sort(x);
        return x;
    }

    private static int[] dither(int[] x)
    {
        x = IndirectSortTest.copy(x);
        for (int i = 0; i < x.length; i++)
            x[i] += i % 5;
        return x;
    }

    private static int[] reverse(int[] x, final int start, final int end)
    {
        x = IndirectSortTest.copy(x);
        for (int i = start, j = end - 1; i < j; i++, j--)
        {
            final int v = x[i];
            x[i] = x[j];
            x[j] = v;
        }
        return x;
    }

    private static int[] copy(final int[] x)
    {
        return x.clone();
    }

    /*
     * 
     */
    private static void testOn(final Algorithm algo, final int[] x, final String testName)
    {
        final IndirectComparator c = new IndirectComparator.AscendingIntComparator(x);

        final int[] order;
        switch (algo)
        {
            case MERGESORT:
                order = IndirectSort.mergesort(0, x.length, c);
                break;
            case MERGESORT_RT:
                order = new int[x.length];
                final int[] tmp = new int[x.length];
                IndirectSort.mergesort(0, x.length, c, tmp, order);
                break;
            case QUICKSORT:
                order = new int[x.length];
                IndirectSort.quicksort(0, x.length, c, order);
                break;
            default:
                Assert.fail();
                throw new RuntimeException();
        }

        IndirectSortTest.assertOrder(order, x.length, c);
    }

    /**
     * Empty and single-item input.
     */
    @Test
    public void testEmptyAndSingle()
    {
        final IndirectComparator comparator = new OrderedInputComparator();
        int[] mSortOrder = IndirectSort.mergesort(0, 0, comparator);
        Assert.assertEquals(mSortOrder.length, 0);

        for (int i = 0; i < 1000; i++)
        {
            mSortOrder = IndirectSort.mergesort(0, i, comparator);
            Assert.assertEquals(mSortOrder.length, i);
        }
    }

    /**
     * Large ordered input.
     */
    @Test
    public void testOrderedMergeSort()
    {
        final IndirectComparator comparator = new OrderedInputComparator();
        final int[] order = IndirectSort.mergesort(0, IndirectSortTest.DATA_LENGTH, comparator);
        IndirectSortTest.assertOrder(order, IndirectSortTest.DATA_LENGTH, comparator);
    }

    /**
     * Large reversed input.
     */
    @Test
    public void testReversedMergeSort()
    {
        final IndirectComparator comparator = new ReverseOrderedInputComparator();
        final int[] order = IndirectSort.mergesort(0, IndirectSortTest.DATA_LENGTH, comparator);
        IndirectSortTest.assertOrder(order, IndirectSortTest.DATA_LENGTH, comparator);
    }

    /*
     * 
     */
    private static void assertOrder(final int[] order, final int length,
            final IndirectComparator comparator)
    {
        for (int i = 1; i < length; i++)
        {
            Assert.assertTrue(comparator.compare(order[i - 1], order[i]) <= 0);
        }
    }

    /**
     * Randomized input, ascending int comparator.
     */
    @Test
    public void testAscInt()
    {
        final int maxSize = 500;
        final int rounds = 1000;
        final int vocabulary = 10;
        final Random rnd = new Random(0x11223344);

        for (int round = 0; round < rounds; round++)
        {
            final int[] input = generateRandom(maxSize, vocabulary, rnd);

            final IndirectComparator comparator = new IndirectComparator.AscendingIntComparator(
                    input);

            final int start = rnd.nextInt(input.length - 1);
            final int length = (input.length - start);

            final int[] order = IndirectSort.mergesort(start, length, comparator);
            IndirectSortTest.assertOrder(order, length, comparator);
        }
    }

    /**
     * Randomized input, descending int comparator.
     */
    @Test
    public void testDescInt()
    {
        final int maxSize = 500;
        final int rounds = 1000;
        final int vocabulary = 10;
        final Random rnd = new Random(0x11223344);

        for (int round = 0; round < rounds; round++)
        {
            final int[] input = generateRandom(maxSize, vocabulary, rnd);

            final IndirectComparator comparator = new IndirectComparator.DescendingIntComparator(
                    input);

            final int start = rnd.nextInt(input.length - 1);
            final int length = (input.length - start);

            final int[] order = IndirectSort.mergesort(start, length, comparator);
            IndirectSortTest.assertOrder(order, length, comparator);
        }
    }

    /**
     * Randomized input, ascending double comparator.
     */
    @Test
    public void testAscDouble()
    {
        final int maxSize = 1000;
        final int rounds = 1000;
        final Random rnd = new Random(0x11223344);

        for (int round = 0; round < rounds; round++)
        {
            final double[] input = generateRandom(maxSize, rnd);

            final IndirectComparator comparator = new IndirectComparator.AscendingDoubleComparator(
                    input);

            final int start = rnd.nextInt(input.length - 1);
            final int length = (input.length - start);

            final int[] order = IndirectSort.mergesort(start, length, comparator);
            IndirectSortTest.assertOrder(order, length, comparator);
        }
    }

    /**
     * Sort random integers from the range 0..0xff based on their 4 upper bits. The relative
     * order of 0xf0-masked integers should be preserved from the input.
     */
    @Test
    public void testMergeSortIsStable()
    {
        final Random rnd = new XorShiftRandom(0xdeadbeef);
        final int[] data = new int[10000];

        for (int i = 0; i < data.length; i++)
            data[i] = rnd.nextInt(0x100);

        final int[] order = IndirectSort.mergesort(0, data.length, new IndirectComparator()
        {
            @Override
            public int compare(final int indexA, final int indexB)
            {
                return (data[indexA] & 0xf0) - (data[indexB] & 0xf0);
            }
        });

        for (int i = 1; i < order.length; i++)
        {
            if ((data[order[i - 1]] & 0xf0) == (data[order[i]] & 0xf0))
            {
                Assert.assertTrue(order[i - 1] < order[i]);
            }
        }
    }

    /*
     * 
     */
    private int[] generateRandom(final int maxSize, final int vocabulary,
            final Random rnd)
    {
        final int[] input = new int[2 + rnd.nextInt(maxSize)];
        for (int i = 0; i < input.length; i++)
        {
            input[i] = vocabulary / 2 - rnd.nextInt(vocabulary);
        }
        return input;
    }

    /*
     * 
     */
    private double[] generateRandom(final int maxSize, final Random rnd)
    {
        final double[] input = new double[2 + rnd.nextInt(maxSize)];
        for (int i = 0; i < input.length; i++)
        {
            input[i] = rnd.nextGaussian();
        }
        return input;
    }
}
