package com.carrotsearch.hppcrt.jmh;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Timeout;
import org.openjdk.jmh.runner.RunnerException;

import com.carrotsearch.hppcrt.BenchmarkSuiteRunner;
import com.carrotsearch.hppcrt.DistributionGenerator;
import com.carrotsearch.hppcrt.XorShift128P;
import com.carrotsearch.hppcrt.procedures.IntProcedure;
import com.carrotsearch.hppcrt.sets.IntHashSet;

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class BenchmarkHashCollisions
{
    /* Prepare some test data */
    public IntHashSet testSet;

    public IntHashSet currentUnderTestSet;

    public IntHashSet currentUnderTestSet2;

    /**
     * Consider that @Benchmark executing in more than TIMEOUT_EXEC_IN_S s
     * must be aborted. Convenient way to test hanging benchmarks.
     */
    public static final int TIMEOUT_EXEC_IN_S = 15;

    public enum Distribution
    {
        RANDOM, LINEAR_DECREASING, LINEAR, HIGHBITS;
    }

    /**
     * Hash collision is very tedious, some cases work
     * even without perturbations :)
     * @author Vincent
     *
     */
    public enum TestAllocationSizes
    {
        HANGING(6000000),
        OK(6291424); //close to 0.75 load factor

        public final int testSize;

        private TestAllocationSizes(final int tSize) {

            this.testSize = tSize;
        }
    }

    public enum Allocation
    {
        DEFAULT_SIZE, PREALLOCATED;
    }

    @Param
    public Allocation allocation;

    @Param
    public Distribution distribution;

    @Param({
        "0.75"
    })
    public float loadFactor;

    @Param
    public TestAllocationSizes targetSize;

    public BenchmarkHashCollisions() {
        // nothing
    }

    //This part is only done once
    @Setup
    public void setUpCommon() throws Exception
    {
        final int nbElementsToPush = this.targetSize.testSize;

        this.testSet = new IntHashSet(nbElementsToPush);

        final DistributionGenerator gene = new DistributionGenerator(-nbElementsToPush, 3 * nbElementsToPush, new XorShift128P(87955214455L));

        int nextValue = -1;

        int count = 0;

        while (this.testSet.size() < nbElementsToPush) {

            if (this.distribution == Distribution.RANDOM) {

                nextValue = gene.RANDOM.getNext();
            }
            else if (this.distribution == Distribution.LINEAR) {

                nextValue = gene.LINEAR.getNext();
            }
            else if (this.distribution == Distribution.HIGHBITS) {

                nextValue = gene.HIGHBITS.getNext();
            }
            else if (this.distribution == Distribution.LINEAR_DECREASING) {

                nextValue = nbElementsToPush - count;
            }

            this.testSet.add(nextValue);

            count++;
        }

        //In PREALLOCATED we only need to create once, and clear the containers under test before each test.
        if (this.allocation == Allocation.PREALLOCATED) {

            this.currentUnderTestSet = IntHashSet.newInstance(nbElementsToPush, this.loadFactor);
            this.currentUnderTestSet2 = IntHashSet.newInstance(nbElementsToPush, this.loadFactor);
        }

        System.out.println("Initialized to test size = " + nbElementsToPush);
    }

    //Each @Benchmark iteration execution, we recreate everything
    //to be able to see reallocations effects.
    @Setup(Level.Invocation)
    public void setUpIncocation() throws Exception
    {
        //In DEFAULT_SIZE we really have to recreate all containers under test
        //each time, to be able to see reallocation effects.

        if (this.allocation == Allocation.DEFAULT_SIZE) {

            this.currentUnderTestSet = IntHashSet.newInstance();
            this.currentUnderTestSet2 = IntHashSet.newInstance();
        }

        // PREALLOCATED is created once and simply cleared. Since the clear
        //operation is very fast, we do it in each @Benchmark.
    }

    //Tests
    /**
     * Time the 'addAll' operation.
     */
    @Timeout(time = BenchmarkHashCollisions.TIMEOUT_EXEC_IN_S, timeUnit = TimeUnit.SECONDS)
    @Benchmark
    public int timeSet_AddAll()
    {
        int count = 0;

        this.currentUnderTestSet.clear();
        count += this.currentUnderTestSet.addAll(this.testSet);

        return count;
    }

    @Timeout(time = BenchmarkHashCollisions.TIMEOUT_EXEC_IN_S, timeUnit = TimeUnit.SECONDS)
    @Benchmark
    public int timeSet_AddAll_Successive()
    {
        int count = 0;
        this.currentUnderTestSet.clear();
        this.currentUnderTestSet2.clear();
        count += this.currentUnderTestSet.addAll(this.testSet);
        count += this.currentUnderTestSet2.addAll(this.currentUnderTestSet);

        return count;
    }

    @Timeout(time = BenchmarkHashCollisions.TIMEOUT_EXEC_IN_S, timeUnit = TimeUnit.SECONDS)
    @Benchmark
    public int timeSet_Direct_iteration_add_all()
    {
        int count = 0;
        this.currentUnderTestSet.clear();

        final int[] testSetKeys = this.testSet.keys;

        for (int j = 0; j < testSetKeys.length; j++) {

            if (testSetKeys[j] != 0) {

                this.currentUnderTestSet.add(testSetKeys[j]);
            }
        }

        count += this.currentUnderTestSet.size();

        return count;
    }

    @Timeout(time = BenchmarkHashCollisions.TIMEOUT_EXEC_IN_S, timeUnit = TimeUnit.SECONDS)
    @Benchmark
    public int timeSet_Direct_iteration_reversed_add_all()
    {
        int count = 0;
        this.currentUnderTestSet.clear();

        final int[] testSetKeys = this.testSet.keys;

        for (int j = testSetKeys.length - 1; j >= 0; j--) {

            if (testSetKeys[j] != 0) {

                this.currentUnderTestSet.add(testSetKeys[j]);
            }
        }

        count += this.currentUnderTestSet.size();

        return count;
    }

    @Timeout(time = BenchmarkHashCollisions.TIMEOUT_EXEC_IN_S, timeUnit = TimeUnit.SECONDS)
    @Benchmark
    public int timeSet_ForEach_add_all()
    {
        this.currentUnderTestSet.clear();

        final IntProcedure addAllProcedure = new IntProcedure() {

            @Override
            public final void apply(final int value) {

                BenchmarkHashCollisions.this.currentUnderTestSet.add(value);
            }
        };

        int count = 0;

        this.testSet.forEach(addAllProcedure);

        count += this.currentUnderTestSet.size();

        return count;
    }

    /**
     * Running main
     * @param args
     * @throws RunnerException
     */
    public static void main(final String[] args) throws RunnerException
    {
        BenchmarkSuiteRunner.runJmhBasicBenchmarkWithCommandLine(BenchmarkHashCollisions.class, args, 1000, 2000);
    }
}
