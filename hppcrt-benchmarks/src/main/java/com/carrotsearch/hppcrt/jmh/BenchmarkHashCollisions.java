package com.carrotsearch.hppcrt.jmh;

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
import com.carrotsearch.hppcrt.BitUtil;
import com.carrotsearch.hppcrt.DistributionGenerator;
import com.carrotsearch.hppcrt.XorShiftRandom;
import com.carrotsearch.hppcrt.maps.IntIntOpenHashMap;
import com.carrotsearch.hppcrt.procedures.IntProcedure;
import com.carrotsearch.hppcrt.sets.IntOpenHashSet;
import com.carrotsearch.hppcrt.sets.ObjectOpenHashSet;

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class BenchmarkHashCollisions
{
    /* Prepare some test data */
    public IntOpenHashSet testSet;
    public ObjectOpenHashSet<Integer> testSetInteger;
    public IntIntOpenHashMap testMap;

    public IntOpenHashSet currentUnderTestSet;
    public ObjectOpenHashSet<Integer> currentUnderTestSetInteger;
    public IntIntOpenHashMap currentUnderTestMap;

    public IntOpenHashSet currentUnderTestSet2;

    /**
     * Consider that @Benchmark executing in more than TIMEOUT_EXEC_IN_S s
     * must be aborted. Convenient way to test hanging benchmarks.
     */
    public static final int TIMEOUT_EXEC_IN_S = 15;

    public enum Distribution
    {
        RANDOM, LINEAR_DECREASING, LINEAR, HIGHBITS;
    }

    public enum Allocation
    {
        DEFAULT_SIZE, PREALLOCATED;
    }

    public static final float HPPC_LOAD_FACTOR_ABSOLUTE_ERROR = 0.05f;

    @Param
    public Allocation allocation;

    @Param
    public Distribution distribution;

    @Param("6000000")
    public int targetSize;

    @Param({
            "0.75"
    })
    public float loadFactor;

    public BenchmarkHashCollisions() {
        // nothing
    }

    //This part is only done once
    @Setup
    public void setUpCommon() throws Exception
    {
        //For this test, this is especially important to push up to the target load factor.
        //suppose our target load factor is this.loadFactor
        //compute the final size to allocate to reach knowing that the table is indeed sized to a power of 2.
        final int finalArraySize = BitUtil.nextHighestPowerOfTwo((int) (this.targetSize / this.loadFactor));

        //to be sure to NOT reallocate, and have the correct load factor, take a margin !
        final int nbElementsToPush = (int) ((finalArraySize * this.loadFactor) - 32);

        this.testSet = new IntOpenHashSet(nbElementsToPush);
        this.testMap = new IntIntOpenHashMap(nbElementsToPush);
        this.testSetInteger = new ObjectOpenHashSet<Integer>(nbElementsToPush);

        final DistributionGenerator gene = new DistributionGenerator(-nbElementsToPush, 3 * nbElementsToPush, new XorShiftRandom(87955214455L));

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
            this.testSetInteger.add(new Integer(nextValue));
            this.testMap.put(nextValue, 0);
            count++;
        }

        //In PREALLOCATED we only need to create once, and clear the containers under test before each test.
        if (this.allocation == Allocation.PREALLOCATED) {

            this.currentUnderTestSet = IntOpenHashSet.newInstanceWithCapacity(nbElementsToPush, this.loadFactor);
            this.currentUnderTestSetInteger = ObjectOpenHashSet.<Integer> newInstanceWithCapacity(nbElementsToPush, this.loadFactor);
            this.currentUnderTestSet2 = IntOpenHashSet.newInstanceWithCapacity(nbElementsToPush, this.loadFactor);
            this.currentUnderTestMap = IntIntOpenHashMap.newInstance(nbElementsToPush, this.loadFactor);
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

            this.currentUnderTestSet = IntOpenHashSet.newInstance();
            this.currentUnderTestSetInteger = ObjectOpenHashSet.<Integer> newInstance();
            this.currentUnderTestSet2 = IntOpenHashSet.newInstance();
            this.currentUnderTestMap = IntIntOpenHashMap.newInstance();
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

        checkFinalLoadFactor(this.currentUnderTestSet);

        return count;
    }

    @Timeout(time = BenchmarkHashCollisions.TIMEOUT_EXEC_IN_S, timeUnit = TimeUnit.SECONDS)
    @Benchmark
    public int timeSet_AddAllObject()
    {
        int count = 0;

        this.currentUnderTestSetInteger.clear();
        count += this.currentUnderTestSetInteger.addAll(this.testSetInteger);

        checkFinalLoadFactor(this.currentUnderTestSetInteger);

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

        checkFinalLoadFactor(this.currentUnderTestSet);
        checkFinalLoadFactor(this.currentUnderTestSet2);

        return count;
    }

    @Timeout(time = BenchmarkHashCollisions.TIMEOUT_EXEC_IN_S, timeUnit = TimeUnit.SECONDS)
    @Benchmark
    public int timeMap_PutAll()
    {
        int count = 0;
        this.currentUnderTestMap.clear();
        count += this.currentUnderTestMap.putAll(this.testMap);

        checkFinalLoadFactor(this.currentUnderTestMap);

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

        checkFinalLoadFactor(this.currentUnderTestSet);

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

        checkFinalLoadFactor(this.currentUnderTestSet);

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

        checkFinalLoadFactor(this.currentUnderTestSet);

        return count;
    }

    private void checkFinalLoadFactor(final IntOpenHashSet tested) {

        if (this.allocation != Allocation.PREALLOCATED) {
            return;
        }

        final double effectiveLoadFactor = tested.size() / (double) tested.keys.length;

        final double loadFactorError = Math.abs(this.loadFactor - effectiveLoadFactor);

        //Check
        if (loadFactorError > BenchmarkHashCollisions.HPPC_LOAD_FACTOR_ABSOLUTE_ERROR) {

            throw new RuntimeException("Wrong target fill factor reached = " + effectiveLoadFactor + " != " + this.loadFactor);
        }
    }

    private void checkFinalLoadFactor(final ObjectOpenHashSet<Integer> tested) {

        if (this.allocation != Allocation.PREALLOCATED) {
            return;
        }

        final double effectiveLoadFactor = tested.size() / (double) ((Object[]) tested.keys).length;

        final double loadFactorError = Math.abs(this.loadFactor - effectiveLoadFactor);

        //Check
        if (loadFactorError > BenchmarkHashCollisions.HPPC_LOAD_FACTOR_ABSOLUTE_ERROR) {

            throw new RuntimeException("Wrong target fill factor reached = " + effectiveLoadFactor + " != " + this.loadFactor);
        }
    }

    private void checkFinalLoadFactor(final IntIntOpenHashMap tested) {

        if (this.allocation != Allocation.PREALLOCATED) {
            return;
        }

        final double effectiveLoadFactor = tested.size() / (double) tested.keys.length;

        final double loadFactorError = Math.abs(this.loadFactor - effectiveLoadFactor);

        //Check
        if (loadFactorError > BenchmarkHashCollisions.HPPC_LOAD_FACTOR_ABSOLUTE_ERROR) {

            throw new RuntimeException("Wrong target fill factor reached = " + effectiveLoadFactor + " != " + this.loadFactor);
        }
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
