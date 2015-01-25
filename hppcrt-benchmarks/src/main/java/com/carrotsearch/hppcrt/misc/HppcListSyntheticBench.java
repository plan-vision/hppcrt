package com.carrotsearch.hppcrt.misc;

import java.util.Comparator;
import java.util.Random;

import com.carrotsearch.hppcrt.BenchmarkSuiteRunner;
import com.carrotsearch.hppcrt.LongIndexedContainer;
import com.carrotsearch.hppcrt.XorShiftRandom;
import com.carrotsearch.hppcrt.cursors.LongCursor;
import com.carrotsearch.hppcrt.lists.LongArrayList;
import com.carrotsearch.hppcrt.lists.LongLinkedList;
import com.carrotsearch.hppcrt.predicates.LongPredicate;
import com.carrotsearch.hppcrt.procedures.LongProcedure;

public class HppcListSyntheticBench
{
    public static final int COUNT = (int) 5e6;

    public static final long RANDOM_SEED = 5487911234761188L;
    private static final long RAND_SEED = 15487012316864131L;
    private static final long RAND_SEED2 = 9988713416546546L;
    private static final long RAND_SEED3 = 412316451315451545L;
    private static final long RAND_SEED4 = 2345613216796312185L;

    public static final int NB_WARMUPS = 3;

    public Random prng = new XorShiftRandom();

    private final int nbWarmupsRuns;

    private long totalSum;

    //closure like
    private final LongProcedure sumProcedureInstance = new LongProcedure() {

        @Override
        public void apply(final long value)
        {
            HppcListSyntheticBench.this.totalSum += value;
        }
    };

    // inner comparator class
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
    public HppcListSyntheticBench(final int nbWarmups)
    {
        this.nbWarmupsRuns = nbWarmups;
    }

    public void runListIterationBench(final String additionalInfo, final LongIndexedContainer list,
            final int nbPreallocated, final int nbWarmupRuns)
    {

        long sumIt = 0;
        long sumForeach = 0;
        long sumDirect = 0;
        long sumGet = 0;
        long tBefore = 0;
        long tAfter = 0;

        double tExecForEachMs = 0.0;
        double tExecIteratorMs = 0.0;
        double tExecDirectMs = 0.0;
        double tExecGetMs = 0.0;

        //////////////////////////////////
        int nbWarmups = 0;

        //Iteration Procedure to sum the elements
        this.prng.setSeed(HppcListSyntheticBench.RANDOM_SEED);

        while (nbWarmups <= nbWarmupRuns)
        {
            nbWarmups++;

            //A) sum with iterator
            //fill with random values
            list.clear();

            this.prng.setSeed(HppcListSyntheticBench.RANDOM_SEED);

            for (int ii = 0; ii < nbPreallocated; ii++)
            {
                //put all, then for even key number, delete by key
                list.add(this.prng.nextLong());
            }

            tBefore = System.nanoTime();

            sumIt = 0;
            for (final LongCursor cursor : list)
            {
                sumIt += cursor.value;
            }

            tAfter = System.nanoTime();

            tExecIteratorMs = (tAfter - tBefore) / 1e6;

            //B) sum with forEach
            list.clear();

            this.prng.setSeed(HppcListSyntheticBench.RANDOM_SEED);

            for (int ii = 0; ii < nbPreallocated; ii++)
            {
                //put all, then for even key number, delete by key
                list.add(this.prng.nextLong());
            }

            tBefore = System.nanoTime();

            sumForeach = 0;
            this.totalSum = 0L;
            list.forEach(this.sumProcedureInstance);
            sumForeach = this.totalSum;

            tAfter = System.nanoTime();

            tExecForEachMs = (tAfter - tBefore) / 1e6;

            //C) sum with get() index iteration
            list.clear();

            this.prng.setSeed(HppcListSyntheticBench.RANDOM_SEED);

            for (int ii = 0; ii < nbPreallocated; ii++)
            {
                //put all, then for even key number, delete by key
                list.add(this.prng.nextLong());
            }

            tBefore = System.nanoTime();

            sumGet = 0;

            int size = list.size();

            for (int ii = 0; ii < size; ii++)
            {
                sumGet += list.get(ii);
            }

            tAfter = System.nanoTime();

            tExecGetMs = (tAfter - tBefore) / 1e6;

            //D) sum with direct iteration
            list.clear();

            this.prng.setSeed(HppcListSyntheticBench.RANDOM_SEED);

            for (int ii = 0; ii < nbPreallocated; ii++)
            {
                //put all, then for even key number, delete by key
                list.add(this.prng.nextLong());
            }

            sumDirect = 0;

            size = list.size();

            long[] buffer = null;

            tBefore = System.nanoTime();

            if (list instanceof LongArrayList)
            {
                buffer = ((LongArrayList) list).buffer;

                for (int ii = 0; ii < size; ii++)
                {
                    sumDirect += buffer[ii];
                }

            }
            else if (list instanceof LongLinkedList)
            {
                buffer = ((LongLinkedList) list).buffer;

                //in linkedList buffer, valid values starts at index 2
                for (int ii = 2; ii < size + 2; ii++)
                {
                    sumDirect += buffer[ii];
                }
            }

            tAfter = System.nanoTime();

            tExecDirectMs = (tAfter - tBefore) / 1e6;

        } //end while

        if (!(sumForeach == sumIt && sumIt == sumDirect && sumDirect == sumGet))
        {
            throw new AssertionError(String.format("ERROR, (Iterator sum = %d) != (forEachSum = %d) != (directSum = %d) != (directSum = %d)\n",
                    sumIt, sumForeach, sumGet, sumDirect));
        }

        System.out.format(
                ">>>> BENCH: HPPC List (long), Iterator vs. forEach vs. get() vs. Direct, (%s), %d elements summed, Iterator= %f ms, foreach= %f ms, Get= %f ms, Direct= %f ms (result = %d)\n",
                additionalInfo,
                nbPreallocated,
                tExecIteratorMs, tExecForEachMs, tExecGetMs, tExecDirectMs, sumForeach);

        //outputs the results to defeat optimizations
        System.out.println(sumForeach + " | " + sumIt + " | " + sumGet + " | " + sumDirect);
    }

    /**
     * Bench for insert / clear() for primitive longs in array lists
     */
    public void runListPrimitiveLong(final String additionalInfo, final LongIndexedContainer list,
            final int nbPreallocated, final int nbWarmupRuns)
    {

        final long sum = 0;

        long tBefore = 0;
        long tAfter = 0;
        long tBeforeClear = 0;
        long tAfterClear = 0;
        //////////////////////////////////
        int nbWarmups = 0;

        this.prng.setSeed(HppcListSyntheticBench.RANDOM_SEED);

        while (nbWarmups <= nbWarmupRuns)
        {
            list.clear();

            nbWarmups++;

            //A) Fill the array
            tBefore = System.nanoTime();

            for (int ii = 0; ii < nbPreallocated; ii++)
            {
                list.add(this.prng.nextLong());
            }
            tAfter = System.nanoTime();

            //B) clear() on all
            tBeforeClear = System.nanoTime();

            list.clear();

            tAfterClear = System.nanoTime();

        } //end while

        System.out.format(">>>> BENCH: HPPC List (long), (%s), %d elements added in %f ms, then clear() in %f ms\n",
                additionalInfo,
                nbPreallocated,
                (tAfter - tBefore) / 1e6,
                (tAfterClear - tBeforeClear) / 1e6);

        //outputs the results to defeat optimizations
        System.out.println(list.size());
    }

    /**
     * Bench filter predicates longs in array lists
     */
    public void runListPrimitiveLongFilterPredicate(final String additionalInfo, final LongIndexedContainer list,
            final int nbPreallocated, final int nbWarmupRuns)
    {

        //predicate to remove all odd values
        final LongPredicate oddPredicate = new LongPredicate() {

            @Override
            public boolean apply(final long value)
            {
                return (value & 1) == 0;
            }
        };

        long sum = 0;

        long tBefore = 0;
        long tAfter = 0;

        //////////////////////////////////
        int nbWarmups = 0;

        this.prng.setSeed(HppcListSyntheticBench.RANDOM_SEED);

        while (nbWarmups <= nbWarmupRuns)
        {
            list.clear();

            nbWarmups++;

            //A) Fill the array

            for (int ii = 0; ii < nbPreallocated; ii++)
            {

                list.add(this.prng.nextLong());
            }

            //B) removeAll() on all
            tBefore = System.nanoTime();

            sum = list.removeAll(oddPredicate);

            tAfter = System.nanoTime();

        } //end while

        System.out.format(">>>> BENCH: HPPC List (long), (%s), %d elements added, then removeAll(odd) in %f ms, %d elements removed\n",
                additionalInfo,
                nbPreallocated,
                (tAfter - tBefore) / 1e6,
                sum);
    }

    /**
     * Bench filter predicates longs in array lists
     */
    public void runListPrimitiveLongKeepPredicate(final String additionalInfo, final LongIndexedContainer list,
            final int nbPreallocated, final int nbWarmupRuns)
    {
        //predicate to remove all odd values
        final LongPredicate oddPredicate = new LongPredicate() {

            @Override
            public boolean apply(final long value)
            {
                return (value & 1) == 0;
            }
        };

        long sum = 0;

        long tBefore = 0;
        long tAfter = 0;

        //////////////////////////////////
        int nbWarmups = 0;

        this.prng.setSeed(HppcListSyntheticBench.RANDOM_SEED);

        while (nbWarmups <= nbWarmupRuns)
        {
            list.clear();

            nbWarmups++;

            //A) Fill the array

            for (int ii = 0; ii < nbPreallocated; ii++)
            {
                list.add(this.prng.nextLong());
            }

            //B) removeAll() on all
            tBefore = System.nanoTime();

            sum = list.retainAll(oddPredicate);

            tAfter = System.nanoTime();

        } //end while

        System.out.format(">>>> BENCH: HPPC List (long), (%s), %d elements added, then retainAll(odd) in %f ms, %d elements removed\n",
                additionalInfo,
                nbPreallocated,
                (tAfter - tBefore) / 1e6,
                sum);

    }

    /**
     * Delete by remove(), in order
     */
    public void runListPrimitiveLongDeleteInOrder(final String additionalInfo, final LongIndexedContainer list,
            final int nbPreallocated, final int nbWarmupRuns)
    {
        long sum = 0;

        long tBefore = 0;
        long tAfter = 0;

        //////////////////////////////////
        int nbWarmups = 0;

        this.prng.setSeed(HppcListSyntheticBench.RANDOM_SEED);

        while (nbWarmups <= nbWarmupRuns)
        {
            list.clear();

            nbWarmups++;

            //A) Fill the array

            for (int ii = 0; ii < nbPreallocated; ii++)
            {
                list.add(this.prng.nextLong());
            }

            //B) remove opearion on all
            tBefore = System.nanoTime();

            if (list instanceof LongArrayList)
            {
                //remove by index, the only available
                for (int ii = 0; ii < nbPreallocated; ii++)
                {
                    list.remove(ii);
                }

                sum = list.size();
            }
            else if (list instanceof LongLinkedList)
            {
                final LongLinkedList linkedlist = (LongLinkedList) list;

                final LongLinkedList.ValueIterator it = linkedlist.iterator().gotoNext();

                //remove by iterator
                try
                {
                    while (!it.isTail())
                    {
                        it.delete();
                    }

                    sum = list.size();
                }
                finally
                {
                    it.release();
                }

                sum = linkedlist.size();

            }

            tAfter = System.nanoTime();

        } //end while

        System.out.format(">>>> BENCH: HPPC List (long), (%s), %d elements added, then remove in order, in %f ms, (final size = %d)\n",
                additionalInfo,
                nbPreallocated,
                (tAfter - tBefore) / 1e6, sum);

    }

    /**
     * Delete by remove(), in order
     */
    public void runListPrimitiveLongDeleteReversed(final String additionalInfo, final LongIndexedContainer list,
            final int nbPreallocated, final int nbWarmupRuns)
    {
        //predicate to remove all odd values
        final LongPredicate oddPredicate = new LongPredicate() {

            @Override
            public boolean apply(final long value)
            {

                return (value & 1) == 0;
            }
        };

        long sum = 0;

        long tBefore = 0;
        long tAfter = 0;

        //////////////////////////////////
        int nbWarmups = 0;

        this.prng.setSeed(HppcListSyntheticBench.RANDOM_SEED);

        while (nbWarmups <= nbWarmupRuns)
        {
            list.clear();

            nbWarmups++;

            //A) Fill the array
            for (int ii = 0; ii < nbPreallocated; ii++)
            {

                list.add(this.prng.nextLong());
            }

            //B) remove() on all
            tBefore = System.nanoTime();

            if (list instanceof LongArrayList)
            {
                //remove by index, the only available
                for (int ii = 0; ii < nbPreallocated; ii++)
                {

                    list.remove(nbPreallocated - ii - 1);
                }

                sum = list.size();

            }
            else if (list instanceof LongLinkedList)
            {
                final LongLinkedList linkedlist = (LongLinkedList) list;

                //remove by reversed iterator
                final LongLinkedList.DescendingValueIterator it = linkedlist.descendingIterator().gotoNext();

                //remove by iterator
                try
                {
                    while (!it.isTail())
                    {
                        it.delete();
                    }

                    sum = list.size();
                }
                finally
                {
                    it.release();
                }
            }

            tAfter = System.nanoTime();

        } //end while

        System.out.format(">>>> BENCH: HPPC List (long), (%s), %d elements added, then remove in reverse, in %f ms, (final size = %d)\n",
                additionalInfo,
                nbPreallocated,
                (tAfter - tBefore) / 1e6, sum);

    }

    public void runBenchList(final int nbElements)
    {
        runListIterationBench("ArrayList", new LongArrayList(nbElements), nbElements, this.nbWarmupsRuns);
        System.gc();
        runListPrimitiveLong("ArrayList", new LongArrayList(nbElements), nbElements, this.nbWarmupsRuns);
        System.gc();
        runListPrimitiveLongFilterPredicate("ArrayList", new LongArrayList(nbElements), nbElements, this.nbWarmupsRuns);
        System.gc();
        runListPrimitiveLongKeepPredicate("ArrayList", new LongArrayList(nbElements), nbElements, this.nbWarmupsRuns);
        System.gc();
        runListPrimitiveLongDeleteInOrder("ArrayList, with remove(index), slow !!!", new LongArrayList((int) (nbElements * 1e-2)),
                (int) (nbElements * 1e-2), this.nbWarmupsRuns);
        System.gc();
        runListPrimitiveLongDeleteReversed("ArrayList, with remove(index)", new LongArrayList(nbElements), nbElements, this.nbWarmupsRuns);

        //Linked-list :
        System.gc();
        runListIterationBench("LinkedList, get() is O(N), direct iteration is out of order !", new LongLinkedList((int) (HppcListSyntheticBench.COUNT * 1e-2)),
                (int) (HppcListSyntheticBench.COUNT * 1e-2),
                this.nbWarmupsRuns);
        System.gc();
        runListPrimitiveLong("LinkedList", new LongLinkedList(nbElements), nbElements, this.nbWarmupsRuns);
        System.gc();
        runListPrimitiveLongFilterPredicate("LinkedList", new LongLinkedList(nbElements), nbElements, this.nbWarmupsRuns);
        System.gc();
        runListPrimitiveLongKeepPredicate("LinkedList", new LongLinkedList(nbElements), nbElements, this.nbWarmupsRuns);
        System.gc();
        runListPrimitiveLongDeleteInOrder("LinkedList, with iterator detete() method", new LongLinkedList(nbElements), nbElements,
                this.nbWarmupsRuns);
        System.gc();
        runListPrimitiveLongDeleteReversed("LinkedList, with descending iterator detete() method", new LongLinkedList(nbElements), nbElements,
                this.nbWarmupsRuns);

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

        final HppcListSyntheticBench testClass = new HppcListSyntheticBench(nbWarmup);

        System.out.println(String.format(">>>>>>>>>>>>>>>>>>>> HPPC LISTS SYNTHETIC BENCH with %d warmup runs ... <<<<<<<<<<<<<<<<<<<<\n", nbWarmup));

        testClass.runBenchList(HppcListSyntheticBench.COUNT);

    }

}
