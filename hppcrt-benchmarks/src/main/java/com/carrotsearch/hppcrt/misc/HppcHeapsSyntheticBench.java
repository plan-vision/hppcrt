package com.carrotsearch.hppcrt.misc;

import java.util.Comparator;
import java.util.Random;

import com.carrotsearch.hppcrt.Util;
import com.carrotsearch.hppcrt.XorShiftRandom;
import com.carrotsearch.hppcrt.heaps.LongHeapPriorityQueue;
import com.carrotsearch.hppcrt.heaps.LongIndexedHeapPriorityQueue;
import com.carrotsearch.hppcrt.heaps.ObjectHeapPriorityQueue;
import com.carrotsearch.hppcrt.heaps.ObjectIndexedHeapPriorityQueue;
import com.carrotsearch.hppcrt.predicates.LongPredicate;
import com.carrotsearch.hppcrt.procedures.LongProcedure;

public class HppcHeapsSyntheticBench
{
    public static final int COUNT = (int) 2e6;

    public static final long RANDOM_SEED = 5487911234761188L;

    public static final int NB_WARMUPS = 3;

    public Random prng = new XorShiftRandom();

    private final int nbWarmupsRuns;

    private long totalSum;

    //closure like
    private final LongProcedure sumProcedureInstance = new LongProcedure() {

        @Override
        public void apply(final long value)
        {
            HppcHeapsSyntheticBench.this.totalSum += value;
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
    public HppcHeapsSyntheticBench(final int nbWarmups)
    {
        this.nbWarmupsRuns = nbWarmups;
    }

    /**
     * Bench for insert / popTop() Primitive longs
     */
    public void runIndexedPrioQueuePrimitiveLong(final String additionalInfo, final LongIndexedHeapPriorityQueue pq,
            final int nbPreallocated)
    {

        long sum = 0;
        long tBefore = 0;
        long tAfter = 0;
        long tBeforeClear = 0;
        long tAfterClear = 0;
        int size = 0;

        //////////////////////////////////
        int nbWarmups = 0;
        this.prng.setSeed(HppcHeapsSyntheticBench.RANDOM_SEED);

        while (nbWarmups <= this.nbWarmupsRuns)
        {
            pq.clear();

            nbWarmups++;

            //A) Random insert bench
            tBefore = System.nanoTime();

            for (int ii = 0; ii < nbPreallocated; ii++)
            {
                pq.put(this.prng.nextInt(nbPreallocated), this.prng.nextLong());
            }

            tAfter = System.nanoTime();
            ///////////////////////////////////////

            //B) popTop() on all
            tBeforeClear = System.nanoTime();

            size = pq.size();

            for (int ii = 0; ii < size; ii++)
            {
                sum += pq.popTop();
            }

            tAfterClear = System.nanoTime();

        } //end while

        System.out.format(">>>> BENCH: HPPC Indexed Prio queue (long), (%s), %d elements inserted, time = %f ms, then popTop() on all in %f ms (dummy = %d)\n",
                additionalInfo,
                size,
                (tAfter - tBefore) / 1e6,
                (tAfterClear - tBeforeClear) / 1e6,
                sum);
    }

    /**
     * Bench for insert / popTop() for primitive longs
     */
    public void runIndexedPrioQueueClearPrimitiveLong(final String additionalInfo, final LongIndexedHeapPriorityQueue pq,
            final int nbPreallocated)
    {

        final long sum = 0;

        long tBeforeClear = 0;
        long tAfterClear = 0;
        //////////////////////////////////
        int nbWarmups = 0;
        int size = 0;

        this.prng.setSeed(HppcHeapsSyntheticBench.RANDOM_SEED);

        while (nbWarmups <= this.nbWarmupsRuns)
        {
            pq.clear();

            nbWarmups++;

            for (int ii = 0; ii < nbPreallocated; ii++)
            {
                pq.put(this.prng.nextInt(nbPreallocated), this.prng.nextLong());
            }

            size = pq.size();

            //A) clear() on all
            tBeforeClear = System.nanoTime();

            pq.clear();

            tAfterClear = System.nanoTime();

        } //end while

        System.out.format(">>>> BENCH: HPPC Indexed Prio queue (long), (%s), %d elements inserted, clear() in %f ms\n",
                additionalInfo,
                size,
                (tAfterClear - tBeforeClear) / 1e6);
    }

    /**
     * Bench for insert / popTop() of Comparable Longs
     */
    public void runIndexedPrioQueueLongObjects(final String additionalInfo, final ObjectIndexedHeapPriorityQueue<ComparableLong> pq,
            final int nbPreallocated)
    {

        long sum = 0;
        long tBefore = 0;
        long tAfter = 0;
        long tBeforeClear = 0;
        long tAfterClear = 0;
        //////////////////////////////////
        int nbWarmups = 0;
        int size = 0;

        // A) Start with a random sample
        final ComparableLong[] referenceArray = new ComparableLong[nbPreallocated];

        this.prng.setSeed(HppcHeapsSyntheticBench.RANDOM_SEED);

        for (int ii = 0; ii < nbPreallocated; ii++)
        {
            referenceArray[ii] = new ComparableLong(this.prng.nextLong());
        }

        //don't make things too easy, shuffle it so the bench do some pointer chasing in memory...
        Util.shuffle(referenceArray, this.prng);


        while (nbWarmups <= this.nbWarmupsRuns)
        {
            pq.clear();

            nbWarmups++;

            //A) Random insert bench
            tBefore = System.nanoTime();

            for (int ii = 0; ii < nbPreallocated; ii++)
            {
                pq.put(this.prng.nextInt(nbPreallocated), referenceArray[ii]);
            }

            tAfter = System.nanoTime();
            ///////////////////////////////////////

            //B) popTop() on all
            size = pq.size();

            tBeforeClear = System.nanoTime();

            for (int ii = 0; ii < size; ii++)
            {
                sum += pq.popTop().value;
            }

            tAfterClear = System.nanoTime();

        } //end while

        System.out.format(">>>> BENCH: HPPC Indexed Prio queue (Comparable Long objects), (%s), %d elements inserted, time = %f ms, then popTop() on all in %f ms (dummy = %d)\n",
                additionalInfo,
                size,
                (tAfter - tBefore) / 1e6,
                (tAfterClear - tBeforeClear) / 1e6,
                sum);
    }

    /**
     * Bench for clear() of Comparable Longs
     */
    public void runIndexedPrioQueueClearLongObjects(final String additionalInfo, final ObjectIndexedHeapPriorityQueue<ComparableLong> pq,
            final int nbPreallocated)
    {
        final long sum = 0;
        long tBeforeClear = 0;
        long tAfterClear = 0;
        //////////////////////////////////
        int nbWarmups = 0;
        int size = 0;

        // A) Start with a random sample
        final ComparableLong[] referenceArray = new ComparableLong[nbPreallocated];

        this.prng.setSeed(HppcHeapsSyntheticBench.RANDOM_SEED);

        for (int ii = 0; ii < nbPreallocated; ii++)
        {
            referenceArray[ii] = new ComparableLong(this.prng.nextLong());
        }

        //don't make things too easy, shuffle it so the bench do some pointer chasing in memory...
        Util.shuffle(referenceArray, this.prng);

        while (nbWarmups <= this.nbWarmupsRuns)
        {
            pq.clear();

            nbWarmups++;

            for (int ii = 0; ii < nbPreallocated; ii++)
            {
                pq.put(this.prng.nextInt(nbPreallocated), referenceArray[ii]);
            }

            size = pq.size();

            //A) clear()
            tBeforeClear = System.nanoTime();

            pq.clear();

            tAfterClear = System.nanoTime();

        } //end while

        System.out.format(">>>> BENCH: HPPC Indexed Prio queue (Comparable Long objects), (%s), %d elements inserted,  then clear() in %f ms (dummy = %d)\n",
                additionalInfo,
                size,
                (tAfterClear - tBeforeClear) / 1e6,
                sum);
    }

//////////////////////////////////////////////////////////////////
    /**
     * Bench for insert / popTop() Primitive longs
     */
    public void runPrioQueuePrimitiveLong(final String additionalInfo, final LongHeapPriorityQueue pq,
            final int nbPreallocated)
    {

        long sum = 0;
        long tBefore = 0;
        long tAfter = 0;
        long tBeforeClear = 0;
        long tAfterClear = 0;
        //////////////////////////////////
        int nbWarmups = 0;

        int size = 0;

        this.prng.setSeed(HppcHeapsSyntheticBench.RANDOM_SEED);


        while (nbWarmups <= this.nbWarmupsRuns)
        {
            pq.clear();

            nbWarmups++;

            //A) Random insert bench
            tBefore = System.nanoTime();

            for (int ii = 0; ii < nbPreallocated; ii++)
            {
                pq.add(this.prng.nextLong());
            }

            size = pq.size();

            tAfter = System.nanoTime();
            ///////////////////////////////////////

            //B) popTop() on all
            tBeforeClear = System.nanoTime();

            for (int ii = 0; ii < nbPreallocated; ii++)
            {
                sum += pq.popTop();
            }

            tAfterClear = System.nanoTime();

        } //end while

        System.out.format(">>>> BENCH: HPPC Prio queue (long), (%s), %d elements inserted, time = %f ms, then popTop() on all in %f ms (dummy = %d)\n",
                additionalInfo,
                size,
                (tAfter - tBefore) / 1e6,
                (tAfterClear - tBeforeClear) / 1e6,
                sum);
    }

    /**
     * Bench for Filtering odd values out by removeAll(Predicate)
     */
    public void runPrioQueuesPrimitiveLongFilterPredicate(final String additionalInfo,
            final int nbInserted)
    {
        long sum = 0;
        long tBeforeHeap = 0, tBeforeIndexedHeap = 0;
        long tAfterHeap = 0, tAfterIndexedHeap = 0;

        final LongIndexedHeapPriorityQueue indexedPq = new LongIndexedHeapPriorityQueue(nbInserted);
        final LongHeapPriorityQueue pq = new LongHeapPriorityQueue(nbInserted);

        //predicate to remove all odd values
        final LongPredicate oddPredicateHeap = new LongPredicate() {

            @Override
            public boolean apply(final long value)
            {
                return (value & 1) == 0;
            }
        };

        final LongPredicate oddPredicateIndexedHeap = new LongPredicate() {

            @Override
            public boolean apply(final long value)
            {

                return (value & 1) == 0;
            }
        };

        this.prng.setSeed(HppcHeapsSyntheticBench.RANDOM_SEED);

        //////////////////////////////////
        int nbWarmups = 0;
        int nbRemovedHeap = 0;
        int nbRemovedIndexedHeap = 0;
        int initialSize = 0;

        while (nbWarmups <= this.nbWarmupsRuns)
        {
            nbWarmups++;

            pq.clear();
            indexedPq.clear();

            //Random insert: loop until inserting nbInserted values with random index / long value
            while (indexedPq.size() < nbInserted)
            {
                indexedPq.put(this.prng.nextInt((int) (nbInserted * 1.3)), this.prng.nextLong());
            }

            //load pq with the same long values
            pq.addAll(indexedPq.values());

            initialSize = pq.size();

            //A)  Iterate over all elements and remove
            tBeforeHeap = System.nanoTime();

            nbRemovedHeap = pq.removeAll(oddPredicateHeap);
            sum += nbRemovedHeap;

            tAfterHeap = System.nanoTime();

            tBeforeIndexedHeap = System.nanoTime();

            nbRemovedIndexedHeap = indexedPq.values().removeAll(oddPredicateIndexedHeap);
            sum += nbRemovedIndexedHeap;

            tAfterIndexedHeap = System.nanoTime();

        } //end while

        System.out
        .format(">>>> BENCH: HPPC Prio queues (long), (%s), %d elements inserted, with filtered out odd values in (heap = %d removed values in %f ms / IndexedHeap = %d removed values in %f ms) (dummy: %d)\n",
                additionalInfo,
                initialSize,
                nbRemovedHeap, (tAfterHeap - tBeforeHeap) / 1e6,
                nbRemovedIndexedHeap, (tAfterIndexedHeap - tBeforeIndexedHeap) / 1e6,
                sum);
    }

    /**
     * Bench for Filtering odd values out by retainAll(Predicate)
     */
    public void runPrioQueuesPrimitiveLongRetainPredicate(final String additionalInfo,
            final int nbInserted)
    {
        long sum = 0;
        long tBeforeHeap = 0, tBeforeIndexedHeap = 0;
        long tAfterHeap = 0, tAfterIndexedHeap = 0;

        final LongIndexedHeapPriorityQueue indexedPq = new LongIndexedHeapPriorityQueue(nbInserted);
        final LongHeapPriorityQueue pq = new LongHeapPriorityQueue(nbInserted);

        //predicate to remove all odd values
        final LongPredicate oddPredicateHeap = new LongPredicate() {

            @Override
            public boolean apply(final long value)
            {
                return (value & 1) == 0;
            }
        };

        final LongPredicate oddPredicateIndexedHeap = new LongPredicate() {

            @Override
            public boolean apply(final long value)
            {
                return (value & 1) == 0;
            }
        };

        this.prng.setSeed(HppcHeapsSyntheticBench.RANDOM_SEED);

        //////////////////////////////////
        int nbWarmups = 0;
        int nbKeptHeap = 0;
        int nbKeptIndexedHeap = 0;
        int initialSize = 0;

        while (nbWarmups <= this.nbWarmupsRuns)
        {
            nbWarmups++;

            pq.clear();
            indexedPq.clear();

            //Random insert: loop until inserting nbInserted values with random index / long value
            while (indexedPq.size() < nbInserted)
            {
                indexedPq.put(this.prng.nextInt((int) (nbInserted * 1.3)), this.prng.nextLong());
            }

            //load pq with the same long values
            pq.addAll(indexedPq.values());

            initialSize = pq.size();

            //A)  Iterate over all elements and remove
            tBeforeHeap = System.nanoTime();

            nbKeptHeap = pq.retainAll(oddPredicateHeap);
            sum += nbKeptHeap;

            tAfterHeap = System.nanoTime();

            tBeforeIndexedHeap = System.nanoTime();

            nbKeptIndexedHeap = indexedPq.values().retainAll(oddPredicateIndexedHeap);
            sum += nbKeptIndexedHeap;

            tAfterIndexedHeap = System.nanoTime();

        } //end while

        System.out
        .format(">>>> BENCH: HPPC Prio queues (long), (%s), %d elements inserted, with keeping odd values in (heap = %d kept values in %f ms / IndexedHeap = %d kept values in %f ms) (dummy: %d)\n",
                additionalInfo,
                initialSize,
                nbKeptHeap, (tAfterHeap - tBeforeHeap) / 1e6,
                nbKeptIndexedHeap, (tAfterIndexedHeap - tBeforeIndexedHeap) / 1e6,
                sum);
    }

    /**
     * Bench for insert / popTop() for primitive longs
     */
    public void runPrioQueueClearPrimitiveLong(final String additionalInfo, final LongHeapPriorityQueue pq,
            final int nbPreallocated)
    {

        final long sum = 0;

        long tBeforeClear = 0;
        long tAfterClear = 0;
        //////////////////////////////////
        int nbWarmups = 0;
        int size = 0;

        this.prng.setSeed(HppcHeapsSyntheticBench.RANDOM_SEED);

        while (nbWarmups <= this.nbWarmupsRuns)
        {

            pq.clear();

            nbWarmups++;

            for (int ii = 0; ii < nbPreallocated; ii++)
            {
                pq.add(this.prng.nextLong());
            }

            size = pq.size();

            //A) clear() on all
            tBeforeClear = System.nanoTime();

            pq.clear();

            tAfterClear = System.nanoTime();

        } //end while

        System.out.format(">>>> BENCH: HPPC Prio queue (long), (%s), %d elements inserted, clear() in %f ms\n",
                additionalInfo,
                size,
                (tAfterClear - tBeforeClear) / 1e6);
    }

    /**
     * Bench for insert / popTop() of Comparable Longs
     */
    public void runPrioQueueLongObjects(final String additionalInfo, final ObjectHeapPriorityQueue<ComparableLong> pq,
            final int nbPreallocated)
    {

        long sum = 0;
        long tBefore = 0;
        long tAfter = 0;
        long tBeforeClear = 0;
        long tAfterClear = 0;
        //////////////////////////////////
        int nbWarmups = 0;

        this.prng.setSeed(HppcHeapsSyntheticBench.RANDOM_SEED);

        // A) Start with a random sample
        final ComparableLong[] referenceArray = new ComparableLong[nbPreallocated];

        for (int ii = 0; ii < nbPreallocated; ii++)
        {
            referenceArray[ii] = new ComparableLong(this.prng.nextLong());
        }

        //don't make things too easy, shuffle it so the bench do some pointer chasing in memory...
        Util.shuffle(referenceArray, this.prng);


        while (nbWarmups <= this.nbWarmupsRuns)
        {
            pq.clear();

            nbWarmups++;

            //A) Random insert bench
            tBefore = System.nanoTime();

            for (int ii = 0; ii < nbPreallocated; ii++)
            {
                pq.add(referenceArray[ii]);
            }

            tAfter = System.nanoTime();
            ///////////////////////////////////////

            //B) popTop() on all
            final int size = pq.size();
            tBeforeClear = System.nanoTime();

            for (int ii = 0; ii < size; ii++)
            {
                sum += pq.popTop().value;
            }

            tAfterClear = System.nanoTime();

        } //end while

        System.out.format(">>>> BENCH: HPPC Prio queue (Comparable Long objects), (%s),  %d elements inserted, time = %f ms, then popTop() on all in %f ms (dummy = %d)\n",
                additionalInfo,
                nbPreallocated,
                (tAfter - tBefore) / 1e6,
                (tAfterClear - tBeforeClear) / 1e6, sum);
    }

    /**
     * Bench for clear() of Comparable Longs
     */
    public void runPrioQueueClearLongObjects(final String additionalInfo, final ObjectHeapPriorityQueue<ComparableLong> pq,
            final int nbPreallocated)
    {
        final long sum = 0;
        long tBeforeClear = 0;
        long tAfterClear = 0;
        //////////////////////////////////
        int nbWarmups = 0;
        int size = 0;

        this.prng.setSeed(HppcHeapsSyntheticBench.RANDOM_SEED);

        // A) Start with a random sample
        final ComparableLong[] referenceArray = new ComparableLong[nbPreallocated];

        for (int ii = 0; ii < nbPreallocated; ii++)
        {
            referenceArray[ii] = new ComparableLong(this.prng.nextLong());
        }

        //don't make things too easy, shuffle it so the bench do some pointer chasing in memory...
        Util.shuffle(referenceArray, this.prng);

        while (nbWarmups <= this.nbWarmupsRuns)
        {

            pq.clear();

            nbWarmups++;

            for (int ii = 0; ii < nbPreallocated; ii++)
            {
                pq.add(referenceArray[ii]);
            }

            size = pq.size();

            //A) clear()
            tBeforeClear = System.nanoTime();

            pq.clear();

            tAfterClear = System.nanoTime();

        } //end while

        System.out.format(">>>> BENCH: HPPC Prio queue (Comparable Long objects), (%s), %d elements inserted, then clear() in %f ms\n",
                additionalInfo,
                size,
                (tAfterClear - tBeforeClear) / 1e6);
    }

    public void runBenchHeaps(final int nbElements)
    {

        System.gc();
        runPrioQueuePrimitiveLong("", new LongHeapPriorityQueue(nbElements), nbElements);
        System.gc();
        runIndexedPrioQueuePrimitiveLong("", new LongIndexedHeapPriorityQueue(nbElements), nbElements);
        System.gc();
        runPrioQueueClearPrimitiveLong("", new LongHeapPriorityQueue(nbElements), nbElements);
        System.gc();
        runIndexedPrioQueueClearPrimitiveLong("", new LongIndexedHeapPriorityQueue(nbElements), nbElements);
        System.gc();

        runPrioQueuesPrimitiveLongFilterPredicate("removeAll", nbElements);
        System.gc();
        runPrioQueuesPrimitiveLongRetainPredicate("retainAll", nbElements);

        //Comparable prio queues
        runPrioQueueLongObjects("Comparable Long", new ObjectHeapPriorityQueue<ComparableLong>(nbElements), nbElements);
        System.gc();
        runPrioQueueClearLongObjects("Comparable Long", new ObjectHeapPriorityQueue<ComparableLong>(nbElements), nbElements);
        System.gc();

        //Comparator prio queues
        runPrioQueueLongObjects("Comparable Long, with inverse ordering Comparator", new ObjectHeapPriorityQueue<ComparableLong>(new Comparator<ComparableLong>() {

            @Override
            public int compare(final ComparableLong o1, final ComparableLong o2)
            {
                if (o1.value > o2.value)
                {
                    return -1;
                }
                if (o1.value < o2.value)
                {
                    return 1;
                }
                return 0;
            }

        }, nbElements), nbElements);

        //
        System.gc();
        runPrioQueueClearLongObjects("Comparable Long, with inverse ordering Comparator", new ObjectHeapPriorityQueue<ComparableLong>(new Comparator<ComparableLong>() {

            @Override
            public int compare(final ComparableLong o1, final ComparableLong o2)
            {
                if (o1.value > o2.value)
                {
                    return -1;
                }
                if (o1.value < o2.value)
                {
                    return 1;
                }
                return 0;
            }

        }, nbElements), nbElements);

        //Comparable prio queues
        runIndexedPrioQueueLongObjects("Comparable Long", new ObjectIndexedHeapPriorityQueue<ComparableLong>(nbElements), nbElements);
        System.gc();
        runIndexedPrioQueueClearLongObjects("Comparable Long", new ObjectIndexedHeapPriorityQueue<ComparableLong>(nbElements), nbElements);
        System.gc();

        //Comparator prio queues
        runIndexedPrioQueueLongObjects("Comparable Long, with inverse ordering Comparator", new ObjectIndexedHeapPriorityQueue<ComparableLong>(new Comparator<ComparableLong>() {

            @Override
            public int compare(final ComparableLong o1, final ComparableLong o2)
            {

                if (o1.value > o2.value)
                {
                    return -1;
                }
                if (o1.value < o2.value)
                {
                    return 1;
                }
                return 0;
            }

        }, nbElements), nbElements);

        //
        System.gc();
        runIndexedPrioQueueClearLongObjects("Comparable Long, with inverse ordering Comparator", new ObjectIndexedHeapPriorityQueue<ComparableLong>(new Comparator<ComparableLong>() {

            @Override
            public int compare(final ComparableLong o1, final ComparableLong o2)
            {

                if (o1.value > o2.value)
                {
                    return -1;
                }
                if (o1.value < o2.value)
                {
                    return 1;
                }
                return 0;
            }

        }, nbElements), nbElements);
    }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * main
     */
    public static void main(final String[] args)
    {
        if (args.length != 1 || !args[0].contains("--warmup="))
        {
            System.out.println("Usage : " + HppcHeapsSyntheticBench.class.getName() + " --warmup=[nb warmup runs]");
        }
        else
        {
            final int nbWarmup = new Integer(args[0].split("--warmup=")[1]);

            final HppcHeapsSyntheticBench testClass = new HppcHeapsSyntheticBench(nbWarmup);

            System.out.println(String.format(">>>>>>>>>>>>>>>>>>>> HPPC HEAPS SYNTHETIC BENCH with %d warmup runs ... <<<<<<<<<<<<<<<<<<<<\n", nbWarmup));

            testClass.runBenchHeaps(HppcHeapsSyntheticBench.COUNT);
        }
    }
}
