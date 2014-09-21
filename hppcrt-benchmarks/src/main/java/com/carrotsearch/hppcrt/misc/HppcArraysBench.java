package com.carrotsearch.hppcrt.misc;

import java.util.Comparator;
import java.util.Random;

import com.carrotsearch.hppcrt.LongArrays;
import com.carrotsearch.hppcrt.XorShiftRandom;

public class HppcArraysBench
{
    public static final int COUNT = (int) 1e6;

    public static final long RANDOM_SEED = 5487911234761188L;
    private static final long RAND_SEED = 15487012316864131L;
    private static final long RAND_SEED2 = 9988713416546546L;
    private static final long RAND_SEED3 = 412316451315451545L;
    private static final long RAND_SEED4 = 2345613216796312185L;

    public static final int NB_WARMUPS = 3;

    private final int nbWarmupsRuns;

    public Random prng = new XorShiftRandom();

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
    public HppcArraysBench(final int nbWarmups)
    {
        this.nbWarmupsRuns = nbWarmups;
    }

    private void runBenchLongsRotateRandom(final int nbRotations, final int nbwarmupRuns, final ComparableLong[] inputArray)
    {
        long tBefore = 0;
        long tAfter = 0;
        double stdRotateRunMS = 0.0;
        long dummyValue = 0;

        final long[] arrayToTest1 = new long[inputArray.length];

        for (int ii = 0; ii < inputArray.length; ii++)
        {
            arrayToTest1[ii] = inputArray[ii].value;
        }

        this.prng.setSeed(HppcArraysBench.RAND_SEED);

        //Retry any number of warmups
        for (int ii = 0; ii < nbwarmupRuns + 1; ii++)
        {
            // A) Try a series of different rotations
            tBefore = System.nanoTime();

            for (int j = 0; j < nbRotations; j++) {

                final int endRotateRange = randomInt(3, arrayToTest1.length, this.prng);
                final int startRotateRange = randomInt(0, endRotateRange - 1, this.prng);

                if (endRotateRange - startRotateRange < 3) {

                    j--;
                    continue;
                }

                final int middleRange = randomInt(startRotateRange + 1, endRotateRange - 1, this.prng);

                //Rotate !
                LongArrays.rotate(arrayToTest1, startRotateRange, middleRange, endRotateRange);

                dummyValue += arrayToTest1[startRotateRange];
            }

            tAfter = System.nanoTime();

            stdRotateRunMS = (tAfter - tBefore) * 1e-6;

        }

        System.out.println(String.format(">>>> BENCH: Rotate %d random subsets of %d elements: LongArrays.rotate(): %f ms (dummy = %d)",
                nbRotations, arrayToTest1.length, stdRotateRunMS, dummyValue));
    }

    private int randomInt(final int min, final int max, final Random rand) {

        return (rand.nextInt(max - min) + min);
    }

    /**
     * Main bench method
     * @param warmupRuns
     */
    public void runBenchArrays(final int nbElements)
    {
        final Random randGenerator = new XorShiftRandom(HppcArraysBench.RAND_SEED);

        // A) Start with a random sample
        final ComparableLong[] referenceArray = new ComparableLong[nbElements];

        for (int ii = 0; ii < nbElements; ii++)
        {
            final long randValue = randGenerator.nextLong();

            referenceArray[ii] = new ComparableLong(randValue);
        }

        // A) Test Rotation
        runBenchLongsRotateRandom(10000, this.nbWarmupsRuns, referenceArray);
        System.gc();
    }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * main
     */
    public static void main(final String[] args)
    {
        if (args.length != 1 || !args[0].contains("--warmup="))
        {
            System.out.println("Usage : " + HppcArraysBench.class.getName() + " --warmup=[nb warmup runs]");
        }
        else
        {
            final int nbWarmup = new Integer(args[0].split("--warmup=")[1]);

            final HppcArraysBench testClass = new HppcArraysBench(nbWarmup);

            System.out.println(String.format(">>>>>>>>>>>>>>>>>>>> HPPC ARRAYS BENCH with %d warmup runs ... <<<<<<<<<<<<<<<<<<<<\n", nbWarmup));

            testClass.runBenchArrays(HppcArraysBench.COUNT);
        }
    }
}
