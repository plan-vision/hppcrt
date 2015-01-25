package com.carrotsearch.hppcrt.misc;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import com.carrotsearch.hppcrt.BenchmarkSuiteRunner;
import com.carrotsearch.hppcrt.Util;
import com.carrotsearch.hppcrt.XorShiftRandom;
import com.carrotsearch.hppcrt.sorting.DoubleSort;
import com.carrotsearch.hppcrt.sorting.LongSort;
import com.carrotsearch.hppcrt.sorting.ObjectSort;

public class HppcSortSyntheticBench
{
    public static final int COUNT = (int) 5e6;

    public static final long RANDOM_SEED = 5487911234761188L;
    private static final long RAND_SEED = 15487012316864131L;
    private static final long RAND_SEED2 = 9988713416546546L;
    private static final long RAND_SEED3 = 412316451315451545L;
    private static final long RAND_SEED4 = 2345613216796312185L;

    public static final int NB_WARMUPS = 3;

    private final int nbWarmupsRuns;

    /**
     *  inner comparator class
     */
    public final class InverseComparator implements Comparator<ComparableLong>
    {
        @Override
        public int compare(final ComparableLong o1, final ComparableLong o2)
        {

            int res = 0;

            if (o1.value > o2.value)
            {
                res = -1;
            }
            else if (o1.value < o2.value)
            {
                res = 1;
            }

            return res;
        }
    }

    public final class NaturalComparator implements Comparator<ComparableLong>
    {
        @Override
        public int compare(final ComparableLong o1, final ComparableLong o2)
        {
            int res = 0;

            if (o1.value < o2.value)
            {
                res = -1;
            }
            else if (o1.value > o2.value)
            {
                res = 1;
            }
            return res;
        }
    }

    // create comparable type
    public final class ComparableLong implements Comparable<ComparableLong>
    {
        public long value;

        public ComparableLong(final long initValue)
        {
            this.value = initValue;
        }

        @Override
        public int compareTo(final ComparableLong other)
        {
            int res = 0;

            if (this.value < other.value)
            {
                res = -1;
            }

            else if (this.value > other.value)
            {
                res = 1;
            }

            return res;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException
        {
            return new ComparableLong(this.value);
        }

        @Override
        public boolean equals(final Object obj)
        {
            return this.value == ((ComparableLong) obj).value;
        }

        @Override
        public int hashCode()
        {
            return (int) this.value;
        }
    }

    /**
     * Constructor
     */
    public HppcSortSyntheticBench(final int nbWarmups)
    {
        this.nbWarmupsRuns = nbWarmups;
    }

    private void runBenchTypeComparator(final String benchType, final int nbwarmupRuns, final ComparableLong[] inputArray)
    {
        // a shallow copy is enough
        final ComparableLong[] arrayToSort = inputArray.clone();

        long tBefore = 0;
        long tAfter = 0;
        double stdSortRunMS = 0.0;
        double quicksortSortRunMS = 0.0;
        long dummyValue = 0;

        for (int ii = 0; ii < nbwarmupRuns + 1; ii++)
        {
            // A) Random sort bench : copy random ordered references
            System.arraycopy(inputArray, 0, arrayToSort, 0, inputArray.length);

            tBefore = System.nanoTime();
            Arrays.sort(arrayToSort, new NaturalComparator());
            tAfter = System.nanoTime();

            stdSortRunMS = (tAfter - tBefore) / 1e6;
            //this is used to defeat optimizations. That way, arrayToSort is forced to be used.
            dummyValue += arrayToSort[arrayToSort.length % 7].value;

            System.arraycopy(inputArray, 0, arrayToSort, 0, inputArray.length);

            tBefore = System.nanoTime();
            ObjectSort.quicksort(arrayToSort, new NaturalComparator());
            tAfter = System.nanoTime();

            quicksortSortRunMS = (tAfter - tBefore) / 1e6;
            //this is used to defeat optimizations. That way, arrayToSort is forced to be used.
            dummyValue += arrayToSort[arrayToSort.length % 11].value;
        }

        System.out.println(String.format(">>>> BENCH: Sorting %d elements of kind (%s), java.util.Arrays: %f ms, HPPC: %f ms, (dummy = %d)",
                arrayToSort.length, "external Comparator<> Long - " + benchType, stdSortRunMS, quicksortSortRunMS, dummyValue));
    }

    private void runBenchTypeComparable(final String benchType, final int nbwarmupRuns, final ComparableLong[] inputArray)
    {
        // a shallow copy is enough
        final ComparableLong[] arrayToSort = inputArray.clone();

        long tBefore = 0;
        long tAfter = 0;
        double stdSortRunMS = 0.0;
        double quicksortSortRunMS = 0.0;
        long dummyValue = 0;

        for (int ii = 0; ii < nbwarmupRuns + 1; ii++)
        {
            // A) Random sort bench : copy random ordered references
            System.arraycopy(inputArray, 0, arrayToSort, 0, inputArray.length);

            tBefore = System.nanoTime();
            Arrays.sort(arrayToSort);
            tAfter = System.nanoTime();

            stdSortRunMS = (tAfter - tBefore) / 1e6;
            //this is used to defeat optimizations. That way, arrayToSort is forced to be used.
            dummyValue += arrayToSort[arrayToSort.length % 7].value;

            System.arraycopy(inputArray, 0, arrayToSort, 0, inputArray.length);

            tBefore = System.nanoTime();
            ObjectSort.quicksort(arrayToSort);
            tAfter = System.nanoTime();

            quicksortSortRunMS = (tAfter - tBefore) / 1e6;
            //this is used to defeat optimizations. That way, arrayToSort is forced to be used.
            dummyValue += arrayToSort[arrayToSort.length % 11].value;

        }

        System.out.println(String.format(">>>> BENCH: Sorting %d elements of kind (%s), java.util.Arrays: %f ms, HPPC: %f ms, (dummy = %d)",
                arrayToSort.length, "Comparable<> Long-" + benchType, stdSortRunMS, quicksortSortRunMS, dummyValue));
    }

    private void runBenchTypeLong(final String benchType, final int nbwarmupRuns, final ComparableLong[] inputArray)
    {
        // a shallow copy is enough
        final long[] arrayToSortReference = new long[inputArray.length];

        for (int ii = 0; ii < inputArray.length; ii++)
        {
            arrayToSortReference[ii] = inputArray[ii].value;
        }

        final long[] arrayToSort = arrayToSortReference.clone();

        long tBefore = 0;
        long tAfter = 0;
        double stdSortRunMS = 0.0;
        double quicksortSortRunMS = 0.0;

        long dummyValue = 0;

        for (int ii = 0; ii < nbwarmupRuns + 1; ii++)
        {
            //reinit the sort array
            System.arraycopy(arrayToSortReference, 0, arrayToSort, 0, arrayToSortReference.length);

            // A) Random sort bench

            tBefore = System.nanoTime();
            Arrays.sort(arrayToSort);
            tAfter = System.nanoTime();

            stdSortRunMS = (tAfter - tBefore) / 1e6;

            //this is used to defeat optimizations. That way, arrayToSort is forced to be used.
            dummyValue += arrayToSort[arrayToSort.length % 7];

            //reinit the sort array
            System.arraycopy(arrayToSortReference, 0, arrayToSort, 0, arrayToSortReference.length);

            tBefore = System.nanoTime();
            LongSort.quicksort(arrayToSort);
            tAfter = System.nanoTime();

            quicksortSortRunMS = (tAfter - tBefore) / 1e6;

            //this is used to defeat optimizations. That way, arrayToSort is forced to be used.
            dummyValue += arrayToSort[arrayToSort.length % 11];
        }

        System.out.println(String.format(">>>> BENCH: Sorting %d elements of kind (%s), java.util.Arrays: %f ms, HPPC: %f ms, (dummy = %d)",
                arrayToSort.length, "long-" + benchType, stdSortRunMS, quicksortSortRunMS, dummyValue));
    }

    private void runBenchTypeDouble(final String benchType, final int nbwarmupRuns, final ComparableLong[] inputArray)
    {
        // a shallow copy is enough
        final double[] arrayToSortReference = new double[inputArray.length];

        for (int ii = 0; ii < inputArray.length; ii++)
        {
            arrayToSortReference[ii] = inputArray[ii].value;
        }

        final double[] arrayToSort = arrayToSortReference.clone();

        long tBefore = 0;
        long tAfter = 0;
        double stdSortRunMS = 0.0;
        double quicksortSortRunMS = 0.0;

        long dummyValue = 0;

        for (int ii = 0; ii < nbwarmupRuns + 1; ii++)
        {
            //reinit the sort array
            System.arraycopy(arrayToSortReference, 0, arrayToSort, 0, arrayToSortReference.length);

            // A) Random sort bench

            tBefore = System.nanoTime();
            Arrays.sort(arrayToSort);
            tAfter = System.nanoTime();

            stdSortRunMS = (tAfter - tBefore) / 1e6;

            //this is used to defeat optimizations. That way, arrayToSort is forced to be used.
            dummyValue += arrayToSort[arrayToSort.length % 7];

            //reinit the sort array
            System.arraycopy(arrayToSortReference, 0, arrayToSort, 0, arrayToSortReference.length);

            tBefore = System.nanoTime();
            DoubleSort.quicksort(arrayToSort);
            tAfter = System.nanoTime();

            quicksortSortRunMS = (tAfter - tBefore) / 1e6;

            //this is used to defeat optimizations. That way, arrayToSort is forced to be used.
            dummyValue += arrayToSort[arrayToSort.length % 11];
        }

        System.out.println(String.format(">>>> BENCH: Sorting %d elements of kind (%s), java.util.Arrays: %f ms, HPPC: %f ms, (dummy = %d)",
                arrayToSort.length, "double-" + benchType, stdSortRunMS, quicksortSortRunMS, dummyValue));
    }

    /**
     * Main bench method
     * @param warmupRuns
     */
    public void runBenchSort(final int nbElements)
    {
        final Random randGenerator = new XorShiftRandom(HppcSortSyntheticBench.RAND_SEED);

        // A) Start with a random sample
        final ComparableLong[] referenceArray = new ComparableLong[nbElements];

        for (int ii = 0; ii < nbElements; ii++)
        {
            final long randValue = randGenerator.nextLong();

            referenceArray[ii] = new ComparableLong(randValue);
        }

        //don't make things too easy, shuffle it so the bench do some pointer chasing in memory...
        Util.shuffle(referenceArray, randGenerator);

        // A) Rendom sample
        runBenchTypeLong("uniform random", this.nbWarmupsRuns, referenceArray);
        System.gc();
        runBenchTypeDouble("uniform random", this.nbWarmupsRuns, referenceArray);
        System.gc();
        runBenchTypeComparable("uniform random", this.nbWarmupsRuns, referenceArray);
        System.gc();
        runBenchTypeComparator("uniform random", this.nbWarmupsRuns, referenceArray);

        // B) Already ordered
        Arrays.sort(referenceArray, new NaturalComparator());

        System.gc();
        runBenchTypeLong("already sorted", this.nbWarmupsRuns, referenceArray);
        System.gc();
        runBenchTypeDouble("already sorted", this.nbWarmupsRuns, referenceArray);
        System.gc();
        runBenchTypeComparable("already sorted", this.nbWarmupsRuns, referenceArray);
        System.gc();
        runBenchTypeComparator("already sorted", this.nbWarmupsRuns, referenceArray);

        // C) ordered in reverse
        Arrays.sort(referenceArray, new InverseComparator());

        System.gc();
        runBenchTypeLong("reversed order", this.nbWarmupsRuns, referenceArray);
        System.gc();
        runBenchTypeDouble("reversed order", this.nbWarmupsRuns, referenceArray);
        System.gc();
        runBenchTypeComparable("reversed order", this.nbWarmupsRuns, referenceArray);
        System.gc();
        runBenchTypeComparator("reversed order", this.nbWarmupsRuns, referenceArray);

        // D) Dither
        for (int ii = 0; ii < nbElements; ii++)
        {
            referenceArray[ii] = new ComparableLong(ii % 5);
        }

        System.gc();
        runBenchTypeLong("dither", this.nbWarmupsRuns, referenceArray);
        System.gc();
        runBenchTypeDouble("dither", this.nbWarmupsRuns, referenceArray);
        System.gc();
        runBenchTypeComparable("dither", this.nbWarmupsRuns, referenceArray);
        System.gc();
        runBenchTypeComparator("dither", this.nbWarmupsRuns, referenceArray);

        // E) Sorted with noise
        for (int ii = 0; ii < nbElements; ii++)
        {
            // ordered
            referenceArray[ii] = new ComparableLong(ii);

            final long addNoise = randGenerator.nextLong() % 11;

            if (randGenerator.nextInt() % 7 == 0)
            {
                referenceArray[ii].value += addNoise;
            }
        }

        System.gc();
        runBenchTypeLong("almost ordered, with noise", this.nbWarmupsRuns, referenceArray);
        System.gc();
        runBenchTypeDouble("almost ordered, with noise", this.nbWarmupsRuns, referenceArray);
        System.gc();
        runBenchTypeComparable("almost ordered, with noise", this.nbWarmupsRuns, referenceArray);
        System.gc();
        runBenchTypeComparator("almost ordered, with noise", this.nbWarmupsRuns, referenceArray);

        // E) Inverse sorted with noise
        for (int ii = 0; ii < nbElements; ii++)
        {
            // ordered
            referenceArray[ii] = new ComparableLong(nbElements - ii);

            final long addNoise = randGenerator.nextLong() % 13;

            if (randGenerator.nextInt() % 7 == 0)
            {
                referenceArray[ii].value += addNoise;
            }
        }

        System.gc();
        runBenchTypeLong("almost inverse-ordered, with noise", this.nbWarmupsRuns, referenceArray);
        System.gc();
        runBenchTypeDouble("almost inverse-ordered, with noise", this.nbWarmupsRuns, referenceArray);
        System.gc();
        runBenchTypeComparable("almost inverse-ordered, with noise", this.nbWarmupsRuns, referenceArray);
        System.gc();
        runBenchTypeComparator("almost inverse-ordered, with noise", this.nbWarmupsRuns, referenceArray);
    }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * main
     */
    public static void main(final String[] args)
    {
        final BenchmarkSuiteRunner.BenchmarkOptions opts = new BenchmarkSuiteRunner.BenchmarkOptions();

        BenchmarkSuiteRunner.parseCommonArguments(args, opts);

        final int nbWarmup = opts.nbWarmups;

        final HppcSortSyntheticBench testClass = new HppcSortSyntheticBench(nbWarmup);

        System.out.println(String.format(">>>>>>>>>>>>>>>>>>>> HPPC SORTING SYNTHETIC BENCH with %d warmup runs ... <<<<<<<<<<<<<<<<<<<<\n", nbWarmup));

        testClass.runBenchSort(HppcSortSyntheticBench.COUNT);

    }
}
