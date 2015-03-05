package com.carrotsearch.hppcrt.jmh;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.runner.RunnerException;

import com.carrotsearch.hppcrt.BenchmarkSuiteRunner;
import com.carrotsearch.hppcrt.DistributionGenerator;
import com.carrotsearch.hppcrt.XorShiftRandom;
import com.carrotsearch.hppcrt.maps.IntIntOpenHashMap;
import com.carrotsearch.hppcrt.procedures.IntProcedure;
import com.carrotsearch.hppcrt.sets.IntOpenHashSet;

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class BenchmarkHashCollisions
{

    /* Prepare some test data */
    public IntOpenHashSet testSet;
    public IntIntOpenHashMap testMap;

    public IntOpenHashSet currentUnderTestSet;
    public IntIntOpenHashMap currentUnderTestMap;

    public IntOpenHashSet currentUnderTestSet2;

    public enum Distribution
    {
        RANDOM, LINEAR_DECREASING, LINEAR, HIGHBITS;
    }

    public enum Allocation
    {
        DEFAULT_SIZE, PREALLOCATED;
    }

    @Param
    public Allocation allocation;

    @Param
    public Distribution distribution;

    @Param("10000000")
    public int size;

    public BenchmarkHashCollisions() {
        // nothing
    }

    @Setup
    public void setUp() throws Exception
    {
        //Instead of this.size, fill up
        //prepare testSet to be filled up to their specified load factor.

        this.testSet = new IntOpenHashSet(this.size);
        this.testMap = new IntIntOpenHashMap(this.size);

        final DistributionGenerator gene = new DistributionGenerator(-this.size, 3 * this.size, new XorShiftRandom(87955214455L));

        int nextValue = -1;

        int count = 0;

        while (this.testSet.size() < this.size) {

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

                nextValue = this.size - count;
            }

            this.testSet.add(nextValue);
            this.testMap.put(nextValue, 0);
            count++;
        }

        //Preallocate of not the tested hash containers
        if (this.allocation == Allocation.DEFAULT_SIZE) {

            this.currentUnderTestSet = IntOpenHashSet.newInstance();
            this.currentUnderTestSet2 = IntOpenHashSet.newInstance();
            this.currentUnderTestMap = IntIntOpenHashMap.newInstance();

        }
        else if (this.allocation == Allocation.PREALLOCATED) {

            this.currentUnderTestSet = IntOpenHashSet.newInstanceWithCapacity(this.size, IntOpenHashSet.DEFAULT_LOAD_FACTOR);
            this.currentUnderTestSet2 = IntOpenHashSet.newInstanceWithCapacity(this.size, IntOpenHashSet.DEFAULT_LOAD_FACTOR);
            this.currentUnderTestMap = IntIntOpenHashMap.newInstance(this.size, IntOpenHashSet.DEFAULT_LOAD_FACTOR);
        }
    }

    //Tests
    /**
     * Time the 'addAll' operation.
     */
    @Benchmark
    public int timeSet_AddAll()
    {
        int count = 0;

        count += this.currentUnderTestSet.addAll(this.testSet);

        return count;
    }

    @Benchmark
    public int timeMap_PutAll()
    {
        int count = 0;

        count += this.currentUnderTestMap.putAll(this.testMap);

        return count;
    }

    @Benchmark
    public int timeSet_AddAll_Successive()
    {
        int count = 0;
        count += this.currentUnderTestSet.addAll(this.testSet);
        count += this.currentUnderTestSet2.addAll(this.currentUnderTestSet);

        return count;
    }

    @Benchmark
    public int timeSet_Direct_iteration_add_all()
    {
        int count = 0;

        final int[] testSetKeys = this.testSet.keys;

        for (int j = 0; j < testSetKeys.length; j++) {

            if (testSetKeys[j] != 0) {

                this.currentUnderTestSet.add(testSetKeys[j]);
            }
        }

        count += this.currentUnderTestSet.size();

        return count;
    }

    @Benchmark
    public int timeSet_Direct_iteration_reversed_add_all()
    {
        int count = 0;

        final int[] testSetKeys = this.testSet.keys;

        for (int j = testSetKeys.length - 1; j >= 0; j--) {

            if (testSetKeys[j] != 0) {

                this.currentUnderTestSet.add(testSetKeys[j]);
            }
        }

        count += this.currentUnderTestSet.size();

        return count;
    }

    @Benchmark
    public int timeSet_ForEach_add_all()
    {
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
        BenchmarkSuiteRunner.runJmhBasicBenchmarkWithCommandLine(BenchmarkHashCollisions.class, args, 1000, 1500);
    }
}
