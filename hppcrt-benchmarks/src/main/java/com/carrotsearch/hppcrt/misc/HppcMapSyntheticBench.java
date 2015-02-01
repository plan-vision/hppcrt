package com.carrotsearch.hppcrt.misc;

import java.util.Random;

import com.carrotsearch.hppcrt.BenchmarkSuiteRunner;
import com.carrotsearch.hppcrt.DistributionGenerator;
import com.carrotsearch.hppcrt.DistributionGenerator.Generator;
import com.carrotsearch.hppcrt.IntLongMap;
import com.carrotsearch.hppcrt.ObjectLongMap;
import com.carrotsearch.hppcrt.Util;
import com.carrotsearch.hppcrt.XorShiftRandom;
import com.carrotsearch.hppcrt.cursors.IntLongCursor;
import com.carrotsearch.hppcrt.lists.IntArrayList;
import com.carrotsearch.hppcrt.lists.LongArrayList;
import com.carrotsearch.hppcrt.lists.ObjectArrayList;
import com.carrotsearch.hppcrt.maps.IntLongOpenHashMap;
import com.carrotsearch.hppcrt.maps.ObjectLongOpenCustomHashMap;
import com.carrotsearch.hppcrt.maps.ObjectLongOpenHashMap;
import com.carrotsearch.hppcrt.maps.ObjectLongOpenIdentityHashMap;
import com.carrotsearch.hppcrt.procedures.LongProcedure;
import com.carrotsearch.hppcrt.strategies.ObjectHashingStrategy;

public final class HppcMapSyntheticBench
{
    public static final int COUNT = (int) 6e6;

    private static final long RANDOM_SEED = 5487911234761188L;
    private static final long RAND_SEED = 15487012316864131L;
    private static final long RAND_SEED2 = 9988713416546546L;
    private static final long RAND_SEED3 = 412316451315451545L;
    private static final long RAND_SEED4 = 2345613216796312185L;

    public static final int NB_WARMUPS = 3;

    private static final int COUNT_ITERATION = (int) 10e6;

    private static final boolean RUN_PRIMITIVES = true;

    private static final boolean RUN_INTEGERS = true;

    private static final boolean RUN_IDENTITY = true;

    public Random prng = new XorShiftRandom();

    private long totalSum;

    private final int nbWarmupsRuns;

    public enum MAP_LOOKUP_TEST
    {
        TRUE,
        MOSTLY_FALSE,
        MIXED
    }

    /**
     * Those are global distribution property, nothing to do with insertion order !
     * @author Vincent
     *
     */
    public enum Distribution
    {
        RANDOM, CONTIGUOUS, HIGHBITS;
    }

    enum HASH_QUALITY
    {
        NORMAL(0),
        BAD(6);

        public final int shift;

        private HASH_QUALITY(final int bitshift)
        {
            this.shift = bitshift;

        }
    }

    //closure like
    private final LongProcedure sumProcedureInstance = new LongProcedure() {

        @Override
        public void apply(final long value)
        {
            HppcMapSyntheticBench.this.totalSum += value;
        }
    };

    /**
     * A Int holder with variable Hash Qualities.
     * @author Vincent
     *
     */
    public static class ComparableInt implements Comparable<ComparableInt>
    {
        public int value;
        public final int bitshift;

        public ComparableInt(final int initValue, final HASH_QUALITY quality)
        {
            this.value = initValue;
            this.bitshift = quality.shift;
        }

        @Override
        public int compareTo(final ComparableInt other)
        {
            if (this.value < other.value)
            {
                return -1;
            }
            else if (this.value > other.value)
            {
                return 1;
            }

            return 0;
        }

        @Override
        public int hashCode()
        {
            return this.value << this.bitshift;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (obj instanceof ComparableInt)
            {
                return ((ComparableInt) obj).value == this.value;
            }

            return false;
        }
    }

    /**
     * Testing for strategies
     */
    private final ObjectHashingStrategy<ComparableInt> INTHOLDER_TRIVIAL_STRATEGY = new ObjectHashingStrategy<ComparableInt>() {

        @Override
        public int computeHashCode(final ComparableInt o)
        {
            return o.value;
        }

        @Override
        public boolean equals(final ComparableInt o1, final ComparableInt o2)
        {
            return o1.value == o2.value;
        }
    };

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Constructor
     */
    public HppcMapSyntheticBench(final int nbWarmups)
    {
        this.nbWarmupsRuns = nbWarmups;
        this.prng.setSeed(HppcMapSyntheticBench.RANDOM_SEED);
    }

    /**
     * Map HPPC (int ==> long) iteration bench
     * @param testMap
     * @param additionalInfo
     * @param nbWarmupRuns
     */
    public void runMapIterationBench(final String additionalInfo, final IntLongMap testMap,
            final int minPushedElements, final float loadFactor)
    {
        long tBefore = 0;
        long tAfter = 0;

        double tExecDirectMs = 0;
        double tExecIteratorMs = 0;
        double tExecForEachMs = 0;

        //////////////////////////////////
        int nbWarmups = 0;

        long sumForeach = 0L;
        long sumIt = 0L;
        long sumDirect = 0;

        final int initialCapacity = testMap.capacity();

        //Do a dry run for enumerating the values that are to be put in map :
        final IntArrayList Klist = new IntArrayList(initialCapacity);
        final LongArrayList Vlist = new LongArrayList(initialCapacity);

        this.prng.setSeed(HppcMapSyntheticBench.RAND_SEED);

        while (testMap.size() < minPushedElements || testMap.size() < testMap.capacity())
        {

            final int K = this.prng.nextInt();
            final long V = this.prng.nextLong();
            Klist.add(K);
            Vlist.add(V);

            testMap.put(K, V);
        }

        //we need the testMap to be not-empty !!
        System.gc();

        //Iteration Procedure to sum the elements

        while (nbWarmups <= this.nbWarmupsRuns)
        {

            nbWarmups++;

            //A) sum with iterator
            tBefore = System.nanoTime();

            sumIt = 0;
            for (final IntLongCursor cursor : testMap)
            {

                sumIt += cursor.value;
            }

            tAfter = System.nanoTime();

            tExecIteratorMs = (tAfter - tBefore) / 1e6;

            //B) sum with forEacch

            tBefore = System.nanoTime();

            sumForeach = 0;
            this.totalSum = 0L;

            testMap.values().forEach(this.sumProcedureInstance);

            sumForeach = this.totalSum;

            tAfter = System.nanoTime();

            tExecForEachMs = (tAfter - tBefore) / 1e6;

            //C) sum with direct iteration
            sumDirect = 0;
            tBefore = System.nanoTime();

            if (testMap instanceof IntLongOpenHashMap)
            {

                final IntLongOpenHashMap m = (IntLongOpenHashMap) testMap;

                for (int ii = 0; ii < m.keys.length; ii++)
                {
                    if (m.keys[ii] != 0)
                    {
                        sumDirect += m.values[ii];
                    }
                }
            }

            tAfter = System.nanoTime();

            tExecDirectMs = (tAfter - tBefore) / 1e6;

        } //end while

        if (!(sumForeach == sumIt && sumIt == sumDirect))
        {
            throw new AssertionError(String.format("ERROR, (Iterator sum = %d) != (forEachSum = %d) != (directSum = %d)", sumIt, sumForeach, sumDirect));
        }

        System.out.format(">>>> BENCH: HPPC Map (int, long), (%s) Iteration test, capacity = %d, load factor = %f, %d elements summed,\n"
                + " Iterator= %f ms, foreach= %f ms, Direct= %f ms (result = %d)\n\n",
                additionalInfo,
                testMap.capacity(),
                loadFactor,
                testMap.size(),
                tExecIteratorMs, tExecForEachMs, tExecDirectMs,
                sumForeach); //outputs the results to defeat optimizations
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Bench for Primitive int ==> long, fill the container up to its load factor:
     */
    public void runMapPrimitiveInt(final String additionalInfo, final IntLongMap testMap,
            final int minPushedElements, final float loadFactor, final MAP_LOOKUP_TEST getKind, final Distribution dis)
    {
        long sum = 0;
        long tBeforePut = 0;
        long tAfterPut = 0;
        long tBeforeGet = 0;
        long tAfterGet = 0;
        long tBeforeClear = 0;
        long tAfterClear = 0;

        long tBeforeRemove = 0;
        long tAfterRemove = 0;
        final int finalSize = 0;
        //////////////////////////////////
        int nbWarmups = 0;
        int putSize = 0;

        final int initialCapacity = testMap.capacity();

        //Do a dry run for enumerating the values that are to be put in map :
        final IntArrayList Klist = new IntArrayList(initialCapacity);
        final LongArrayList Vlist = new LongArrayList(initialCapacity);

        this.prng.setSeed(HppcMapSyntheticBench.RANDOM_SEED);

        final Generator gene = getGenerator(dis, minPushedElements);

        while (testMap.size() < minPushedElements || testMap.size() < testMap.capacity())
        {

            final int K = gene.getNext();
            final long V = this.prng.nextLong();
            Klist.add(K);
            Vlist.add(V);

            testMap.put(K, V);
        }

        //don't make things too easy, shuffle it so the bench do some pointer chasing in memory...
        //except that it should have no effect because of primitives, so there is no pointer to chase... :)
        Util.shuffle(Klist, this.prng);

        testMap.clear();
        System.gc();

        while (nbWarmups <= this.nbWarmupsRuns)
        {
            testMap.clear();

            nbWarmups++;

            tBeforePut = System.nanoTime();

            //A) fill until the capacity, and at least minPushedElements
            for (int ii = 0; ii < Klist.size(); ii++)
            {
                testMap.put(Klist.get(ii), Vlist.get(ii));
            }

            tAfterPut = System.nanoTime();

            putSize = testMap.size();

            tBeforeGet = System.nanoTime();

            // B) Process by get/contains
            for (int ii = 0; ii < Klist.size(); ii++)
            {

                if ((getKind == MAP_LOOKUP_TEST.TRUE || getKind == MAP_LOOKUP_TEST.MIXED) && this.prng.nextBoolean())
                {

                    sum += testMap.get(Klist.get(ii));
                }
                else if (getKind == MAP_LOOKUP_TEST.MOSTLY_FALSE || getKind == MAP_LOOKUP_TEST.MIXED)
                {
                    //this element may or may not be in the set
                    sum += testMap.get(this.prng.nextInt());
                }
            }

            tAfterGet = System.nanoTime();
            ///////////////////////////////////////

            //C) Remove Op
            tBeforeRemove = System.nanoTime();

            for (int ii = 0; ii < Klist.size(); ii++)
            {

                if ((getKind == MAP_LOOKUP_TEST.TRUE || getKind == MAP_LOOKUP_TEST.MIXED) && this.prng.nextBoolean())
                {

                    sum += testMap.remove(Klist.get(ii));
                }
                else if (getKind == MAP_LOOKUP_TEST.MOSTLY_FALSE || getKind == MAP_LOOKUP_TEST.MIXED)
                {
                    //this element may or may not be in the set
                    sum += testMap.remove(this.prng.nextInt());
                }
            }

            tAfterRemove = System.nanoTime();

            //clear op
            tBeforeClear = System.nanoTime();

            testMap.clear();

            tAfterClear = System.nanoTime();

        } //end while

        System.out.format(">>>> BENCH: HPPC Map (int of dis %s, long), (%s) capacity = %d, load factor = %f, %d elements pushed\n"
                + " Put = %f ms, Get (%s) = %f ms,  Remove (%s) = %f ms,  Clear =  %f ms (dummy = %d)\n\n",
                dis,
                additionalInfo,
                testMap.capacity(),
                loadFactor,
                putSize,
                (tAfterPut - tBeforePut) / 1e6,
                getKind,
                (tAfterGet - tBeforeGet) / 1e6,
                getKind,
                (tAfterRemove - tBeforeRemove) / 1e6,
                (tAfterClear - tBeforeClear) / 1e6,
                sum); //outputs the results to defeat optimizations
    }

    /**
     * Bench for HPPC (ComparableInt Object ==> long).
     *
     */
    public void runMapIntegerObjectLong(final String additionalInfo, final ObjectLongMap<ComparableInt> testMap,
            final int minPushedElements, final float loadFactor, final MAP_LOOKUP_TEST getKind, final HASH_QUALITY quality, final Distribution dis)
    {

        long sum = 0;
        long tBeforePut = 0;
        long tAfterPut = 0;
        long tBeforeGet = 0;
        long tAfterGet = 0;
        long tBeforeClear = 0;
        long tAfterClear = 0;

        long tBeforeRemove = 0;
        long tAfterRemove = 0;

        //////////////////////////////////
        int nbWarmups = 0;

        int putSize = 0;

        final int initialCapacity = testMap.capacity();

        //Do a dry run for pre-boxing the values that are to be put in map :
        final ObjectArrayList<ComparableInt> Klist = new ObjectArrayList<ComparableInt>();
        final LongArrayList Vlist = new LongArrayList();

        this.prng.setSeed(HppcMapSyntheticBench.RAND_SEED);

        final Generator gene = getGenerator(dis, minPushedElements);

        while (testMap.size() < minPushedElements || testMap.size() < testMap.capacity())
        {

            //put all, then for even key number, delete by key
            final ComparableInt K = new ComparableInt(gene.getNext(), quality);
            final long V = this.prng.nextLong();
            Klist.add(K);
            Vlist.add(V);

            testMap.put(K, V);
        }

        //don't make things too easy, shuffle it so the bench do some pointer chasing in memory...
        Util.shuffle(Klist, this.prng);

        testMap.clear();
        System.gc();

        //The main measuring loop
        while (nbWarmups <= this.nbWarmupsRuns)
        {
            testMap.clear();

            nbWarmups++;

            tBeforePut = System.nanoTime();

            //A) fill until the capacity, and at least minPushedElements
            for (int ii = 0; ii < Klist.size(); ii++)
            {

                //put all, then for even key number, delete by key
                testMap.put(Klist.get(ii), Vlist.get(ii));
            }

            tAfterPut = System.nanoTime();

            putSize = testMap.size();

            tBeforeGet = System.nanoTime();

            // B) Process by get/contains
            final ComparableInt tmpAbsentIntHolder = new ComparableInt(0, quality);

            //Rerun to compute a containsKey lget() pattern
            for (int ii = 0; ii < Klist.size(); ii++)
            {

                if ((getKind == MAP_LOOKUP_TEST.TRUE || getKind == MAP_LOOKUP_TEST.MIXED) && this.prng.nextBoolean())
                {

                    sum += testMap.get(Klist.get(ii));
                }
                else if (getKind == MAP_LOOKUP_TEST.MOSTLY_FALSE || getKind == MAP_LOOKUP_TEST.MIXED)
                {

                    //this element may or may not be in the set
                    tmpAbsentIntHolder.value = this.prng.nextInt();
                    sum += testMap.get(tmpAbsentIntHolder);
                }
            }

            tAfterGet = System.nanoTime();
            ///////////////////////////////////////

            //C) Remove Op
            tBeforeRemove = System.nanoTime();

            for (int ii = 0; ii < Klist.size(); ii++)
            {

                if ((getKind == MAP_LOOKUP_TEST.TRUE || getKind == MAP_LOOKUP_TEST.MIXED) && this.prng.nextBoolean())
                {
                    sum += testMap.remove(Klist.get(ii));
                }
                else if (getKind == MAP_LOOKUP_TEST.MOSTLY_FALSE || getKind == MAP_LOOKUP_TEST.MIXED)
                {
                    //this element may or may not be in the set
                    tmpAbsentIntHolder.value = this.prng.nextInt();
                    sum += testMap.remove(tmpAbsentIntHolder);
                }
            }

            tAfterRemove = System.nanoTime();

            //clear op
            tBeforeClear = System.nanoTime();

            testMap.clear();

            tAfterClear = System.nanoTime();

        } //end while

        System.out.format(">>>> BENCH: HPPC Map (ComparableInt object of dis %s, hash %s, long), (%s), capacity = %d, load factor = %f, %d elements pushed\n"
                + " Put = %f ms, Get (%s) = %f ms, Remove (%s) = %f ms,  Clear =  %f ms (dummy = %d)\n\n",
                dis,
                quality,
                additionalInfo,
                testMap.capacity(),
                loadFactor,
                putSize,
                (tAfterPut - tBeforePut) / 1e6,
                getKind,
                (tAfterGet - tBeforeGet) / 1e6,
                getKind,
                (tAfterRemove - tBeforeRemove) / 1e6,
                (tAfterClear - tBeforeClear) / 1e6,
                sum); //outputs the results to defeat optimizations
    }

    /**
     * Bench for HPPC Identity map (ComparableInt) ==> long).
     *
     */
    public void runMapIdentityObjectLong(final String additionalInfo, final ObjectLongMap<ComparableInt> testMap,
            final int minPushedElements, final float loadFactor, final MAP_LOOKUP_TEST getKind)
    {

        long sum = 0;
        long tBeforePut = 0;
        long tAfterPut = 0;
        long tBeforeGet = 0;
        long tAfterGet = 0;
        long tBeforeClear = 0;
        long tAfterClear = 0;

        long tBeforeRemove = 0;
        long tAfterRemove = 0;

        //////////////////////////////////
        int nbWarmups = 0;

        int putSize = 0;

        final int initialCapacity = testMap.capacity();

        //Do a dry run for pre-boxing the values that are to be put in map :
        final ObjectArrayList<ComparableInt> Klist = new ObjectArrayList<ComparableInt>();
        final LongArrayList Vlist = new LongArrayList();

        this.prng.setSeed(HppcMapSyntheticBench.RAND_SEED);

        final Generator gene = getGenerator(Distribution.RANDOM, minPushedElements);

        while (testMap.size() < minPushedElements || testMap.size() < testMap.capacity())
        {
            //put all, then for even key number, delete by key
            final ComparableInt K = new ComparableInt(gene.getNext(), HASH_QUALITY.NORMAL);
            final long V = this.prng.nextLong();
            Klist.add(K);
            Vlist.add(V);

            testMap.put(K, V);
        }

        //don't make things too easy, shuffle it so the bench do some pointer chasing in memory...
        Util.shuffle(Klist, this.prng);

        testMap.clear();
        System.gc();

        //The main measuring loop
        while (nbWarmups <= this.nbWarmupsRuns)
        {
            testMap.clear();

            nbWarmups++;

            tBeforePut = System.nanoTime();

            //A) fill until the capacity, and at least minPushedElements
            for (int ii = 0; ii < Klist.size(); ii++)
            {
                //put all, then for even key number, delete by key
                testMap.put(Klist.get(ii), Vlist.get(ii));
            }

            tAfterPut = System.nanoTime();

            putSize = testMap.size();

            tBeforeGet = System.nanoTime();

            // B) Process by get/contains
            final ComparableInt tmpAbsentIntHolder = new ComparableInt(0, HASH_QUALITY.NORMAL);

            //Rerun to compute a containsKey lget() pattern
            for (int ii = 0; ii < Klist.size(); ii++)
            {
                if ((getKind == MAP_LOOKUP_TEST.TRUE || getKind == MAP_LOOKUP_TEST.MIXED) && this.prng.nextBoolean())
                {
                    sum += testMap.get(Klist.get(ii));
                }
                else if (getKind == MAP_LOOKUP_TEST.MOSTLY_FALSE || getKind == MAP_LOOKUP_TEST.MIXED)
                {
                    //this element may or may not be in the set
                    tmpAbsentIntHolder.value = this.prng.nextInt();
                    sum += testMap.get(tmpAbsentIntHolder);
                }
            }

            tAfterGet = System.nanoTime();
            ///////////////////////////////////////

            //C) Remove Op
            tBeforeRemove = System.nanoTime();

            for (int ii = 0; ii < Klist.size(); ii++)
            {

                if ((getKind == MAP_LOOKUP_TEST.TRUE || getKind == MAP_LOOKUP_TEST.MIXED) && this.prng.nextBoolean())
                {
                    sum += testMap.remove(Klist.get(ii));
                }
                else if (getKind == MAP_LOOKUP_TEST.MOSTLY_FALSE || getKind == MAP_LOOKUP_TEST.MIXED)
                {
                    //this element may or may not be in the set
                    tmpAbsentIntHolder.value = this.prng.nextInt();
                    sum += testMap.remove(tmpAbsentIntHolder);
                }
            }

            tAfterRemove = System.nanoTime();

            //clear op
            tBeforeClear = System.nanoTime();

            testMap.clear();

            tAfterClear = System.nanoTime();

        } //end while

        System.out.format(">>>> BENCH: HPPC Identity Map (ComparableInt) object, long), (%s), capacity = %d, load factor = %f, %d elements pushed\n"
                + " Put = %f ms, Get (%s) = %f ms, Remove (%s) = %f ms,  Clear =  %f ms (dummy = %d)\n\n",
                additionalInfo,
                testMap.capacity(),
                loadFactor,
                putSize,
                (tAfterPut - tBeforePut) / 1e6,
                getKind,
                (tAfterGet - tBeforeGet) / 1e6,
                getKind,
                (tAfterRemove - tBeforeRemove) / 1e6,
                (tAfterClear - tBeforeClear) / 1e6,
                sum); //outputs the results to defeat optimizations
    }

    //public methods

    public void runMapSyntheticBenchPrimitives(final MAP_LOOKUP_TEST getKind)
    {
        for (final Distribution dis : Distribution.values())
        {
            // Preallocate at maximum size
            runMapPrimitiveInt("IntLongOpenHashMap",
                    IntLongOpenHashMap.newInstance(HppcMapSyntheticBench.COUNT, IntLongOpenHashMap.DEFAULT_LOAD_FACTOR),
                    HppcMapSyntheticBench.COUNT, IntLongOpenHashMap.DEFAULT_LOAD_FACTOR,
                    getKind, dis);
            System.gc();
        }
    }

    public void runMapSyntheticBenchComparableInt(final MAP_LOOKUP_TEST getKind)
    {
        for (final Distribution dis : Distribution.values())
        {
            runMapIntegerObjectLong("ObjectLongOpenHashMap",
                    ObjectLongOpenHashMap.<ComparableInt> newInstance(HppcMapSyntheticBench.COUNT, ObjectLongOpenHashMap.DEFAULT_LOAD_FACTOR),
                    HppcMapSyntheticBench.COUNT, ObjectLongOpenHashMap.DEFAULT_LOAD_FACTOR,
                    getKind, HASH_QUALITY.NORMAL, dis);
            System.gc();

            runMapIntegerObjectLong("ObjectLongOpenHashMap",
                    ObjectLongOpenHashMap.<ComparableInt> newInstance(HppcMapSyntheticBench.COUNT, ObjectLongOpenHashMap.DEFAULT_LOAD_FACTOR),
                    HppcMapSyntheticBench.COUNT, ObjectLongOpenHashMap.DEFAULT_LOAD_FACTOR,
                    getKind, HASH_QUALITY.BAD, dis);
            System.gc();
        }

        // use specialized strategy to overcome the bad hash behaviour above.
        runMapIntegerObjectLong("ObjectLongOpenCustomHashMap with strategy",
                ObjectLongOpenCustomHashMap.<ComparableInt> newInstance(
                        HppcMapSyntheticBench.COUNT, IntLongOpenHashMap.DEFAULT_LOAD_FACTOR,
                        this.INTHOLDER_TRIVIAL_STRATEGY),
                HppcMapSyntheticBench.COUNT, ObjectLongOpenHashMap.DEFAULT_LOAD_FACTOR, getKind, HASH_QUALITY.BAD, Distribution.HIGHBITS);
        System.gc();
    }

    public void runMapIterationBench()
    {
        runMapIterationBench("IntLongOpenHashMap", new IntLongOpenHashMap(HppcMapSyntheticBench.COUNT_ITERATION), HppcMapSyntheticBench.COUNT_ITERATION, IntLongOpenHashMap.DEFAULT_LOAD_FACTOR);
        System.gc();
    }

    private Generator getGenerator(final Distribution disKind, final int pushedElements)
    {
        Generator generator = null;

        final DistributionGenerator disGene = new DistributionGenerator(-pushedElements, 2 * pushedElements, this.prng);

        switch (disKind)
        {
            case RANDOM:
                generator = disGene.RANDOM;
                break;

            case CONTIGUOUS:
                generator = disGene.LINEAR;
                break;

            case HIGHBITS:
                generator = disGene.HIGHBITS;
                break;

            default:
                throw new RuntimeException();
        }

        return generator;
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

        final HppcMapSyntheticBench testClass = new HppcMapSyntheticBench(nbWarmup);

        //Map synthetic bench
        System.out.println(String.format(">>>>>>>>>>>>>>>>>>>> HPPC HASH MAPS SYNTHETIC BENCH with %d warmup runs ... <<<<<<<<<<<<<<<<<<<<\n", nbWarmup));

        //map iteration benchs
        testClass.runMapIterationBench();
        System.gc();

        if (HppcMapSyntheticBench.RUN_PRIMITIVES) {
            System.out.println("\n");
            testClass.runMapSyntheticBenchPrimitives(MAP_LOOKUP_TEST.TRUE);
            System.gc();
        }

        if (HppcMapSyntheticBench.RUN_IDENTITY) {
            System.out.println("\n");

            testClass.runMapIdentityObjectLong("ObjectLongOpenIdentityHashMap",
                    ObjectLongOpenIdentityHashMap.<ComparableInt> newInstance(HppcMapSyntheticBench.COUNT, ObjectLongOpenHashMap.DEFAULT_LOAD_FACTOR),
                    HppcMapSyntheticBench.COUNT, ObjectLongOpenHashMap.DEFAULT_LOAD_FACTOR, MAP_LOOKUP_TEST.TRUE);
            System.gc();
        }

        if (HppcMapSyntheticBench.RUN_INTEGERS) {
            System.out.println("\n");
            testClass.runMapSyntheticBenchComparableInt(MAP_LOOKUP_TEST.TRUE);
            System.gc();
        }

        if (HppcMapSyntheticBench.RUN_PRIMITIVES) {
            System.out.println("\n\n");
            testClass.runMapSyntheticBenchPrimitives(MAP_LOOKUP_TEST.MIXED);
            System.gc();
        }

        if (HppcMapSyntheticBench.RUN_IDENTITY) {
            System.out.println("\n");

            testClass.runMapIdentityObjectLong("ObjectLongOpenIdentityHashMap",
                    ObjectLongOpenIdentityHashMap.<ComparableInt> newInstance(HppcMapSyntheticBench.COUNT, ObjectLongOpenHashMap.DEFAULT_LOAD_FACTOR),
                    HppcMapSyntheticBench.COUNT, ObjectLongOpenHashMap.DEFAULT_LOAD_FACTOR, MAP_LOOKUP_TEST.MIXED);
            System.gc();
        }

        if (HppcMapSyntheticBench.RUN_INTEGERS) {
            System.out.println("\n");
            testClass.runMapSyntheticBenchComparableInt(MAP_LOOKUP_TEST.MIXED);
            System.gc();
        }

        if (HppcMapSyntheticBench.RUN_PRIMITIVES) {
            System.out.println("\n\n");
            testClass.runMapSyntheticBenchPrimitives(MAP_LOOKUP_TEST.MOSTLY_FALSE);
            System.gc();
        }

        if (HppcMapSyntheticBench.RUN_IDENTITY) {
            System.out.println("\n");

            testClass.runMapIdentityObjectLong("ObjectLongOpenIdentityHashMap",
                    ObjectLongOpenIdentityHashMap.<ComparableInt> newInstance(HppcMapSyntheticBench.COUNT, ObjectLongOpenHashMap.DEFAULT_LOAD_FACTOR),
                    HppcMapSyntheticBench.COUNT, ObjectLongOpenHashMap.DEFAULT_LOAD_FACTOR, MAP_LOOKUP_TEST.MOSTLY_FALSE);
            System.gc();
        }

        if (HppcMapSyntheticBench.RUN_INTEGERS) {
            System.out.println("\n");
            testClass.runMapSyntheticBenchComparableInt(MAP_LOOKUP_TEST.MOSTLY_FALSE);
            System.gc();
        }

    }
}
