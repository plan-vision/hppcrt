package com.carrotsearch.hppcrt.caliper;

import com.carrotsearch.hppcrt.DistributionGenerator;
import com.carrotsearch.hppcrt.XorShiftRandom;
import com.carrotsearch.hppcrt.predicates.IntPredicate;
import com.carrotsearch.hppcrt.procedures.IntProcedure;
import com.carrotsearch.hppcrt.sets.IntOpenHashSet;
import com.google.caliper.Param;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;

/**
 * Benchmark putting All of a hashed container in another.
 */
public class BenchmarkHashCollisionsAddAllSuccessive extends SimpleBenchmark
{
    /* Prepare some test data */
    public IntOpenHashSet testSet;

    public IntOpenHashSet currentUnderTestSet;

    public IntOpenHashSet currentUnderTestSet2;

    public enum Distribution
    {
        RANDOM, LINEAR, LINEAR_DECREMENT, HIGHBITS;
    }

    public enum Allocation
    {
        DEFAULT_SIZE, PREALLOCATED;
    }

    @Param
    public Allocation allocation;

    @Param
    public Distribution distribution;

    @Param(
    {
                "5000000"
    })
    public int size;

    /*
     * 
     */
    @Override
    protected void setUp() throws Exception
    {
        //Instead of this.size, fill up
        //prepare testSet to be filled up to their specified load factor.

        this.testSet = new IntOpenHashSet(this.size);

        final DistributionGenerator gene = new DistributionGenerator(this.size, new XorShiftRandom(87955214455L));

        int nextValue = -1;

        while (this.testSet.size() < this.testSet.capacity()) {

            if (this.distribution == Distribution.RANDOM) {

                nextValue = gene.RANDOM.getNext();
            }
            else if (this.distribution == Distribution.LINEAR) {

                nextValue = gene.LINEAR.getNext();
            }
            else if (this.distribution == Distribution.HIGHBITS) {

                nextValue = gene.HIGHBITS.getNext();
            }
            else if (this.distribution == Distribution.LINEAR_DECREMENT) {

                nextValue = gene.LINEAR_DECREMENT.getNext();
            }

            this.testSet.add(nextValue);
        }

        //Preallocate of not the tested hash containers
        if (this.allocation == Allocation.DEFAULT_SIZE) {

            this.currentUnderTestSet = IntOpenHashSet.newInstance();
            this.currentUnderTestSet2 = IntOpenHashSet.newInstance();
        }
        else if (this.allocation == Allocation.PREALLOCATED) {

            this.currentUnderTestSet = IntOpenHashSet.newInstanceWithCapacity(this.size, IntOpenHashSet.DEFAULT_LOAD_FACTOR);
            this.currentUnderTestSet2 = IntOpenHashSet.newInstanceWithCapacity(this.size, IntOpenHashSet.DEFAULT_LOAD_FACTOR);
        }
    }

    @Override
    protected void tearDown() throws Exception
    {
        this.testSet = null;
    }

    /**
     * Time the 'addAll' operation in successive ways
     */
    public int timeSet_AddAll_Successive(final int reps)
    {
        int count = 0;
        for (int i = 0; i < reps; i++)
        {
            this.currentUnderTestSet.clear();
            this.currentUnderTestSet2.clear();
            count += this.currentUnderTestSet.addAll(this.testSet);
            count += this.currentUnderTestSet2.addAll(this.currentUnderTestSet);
        }
        return count;
    }

    /**
     * Running main
     * @param args
     */
    public static void main(final String[] args)
    {
        Runner.main(BenchmarkHashCollisionsAddAllSuccessive.class, args);
    }
}
