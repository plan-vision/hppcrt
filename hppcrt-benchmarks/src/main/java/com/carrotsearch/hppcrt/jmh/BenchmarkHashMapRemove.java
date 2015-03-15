package com.carrotsearch.hppcrt.jmh;

import java.util.Arrays;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.runner.RunnerException;

import com.carrotsearch.hppcrt.BenchmarkSuiteRunner;
import com.carrotsearch.hppcrt.Util;
import com.carrotsearch.hppcrt.implementations.MapImplementation;

/**
 * Benchmark putting a given number of integers / Objects into a hashmap.
 * also the base class for all the other Hash benchmarks.
 */
public class BenchmarkHashMapRemove extends BenchmarkHashMapBase
{

    public enum MAP_LOOKUP_TEST
    {
        TRUE,
        MOSTLY_FALSE,
        MIXED
    }

    @Param
    MAP_LOOKUP_TEST lookupSuccessKind;

    private int[] removedKeys;

    public BenchmarkHashMapRemove() {
        super();
    }

    // This Setup part is only done once
    @Setup
    public void initialSetUp() throws Exception
    {
        setUpCommon();

        //Generate a series of containsKeys // B) Process by get/contains
        this.removedKeys = Arrays.copyOf(this.pushedKeys, this.pushedKeys.length);

        //Shuffle order, so the contains test order is different from
        //the insertion order.
        Util.shuffle(this.removedKeys, this.prng);

        for (int ii = 0; ii < this.pushedKeys.length; ii++)
        {
            final boolean isMixedLookupSucceded = (this.lookupSuccessKind == MAP_LOOKUP_TEST.MIXED) && this.prng.nextBoolean();

            if (this.lookupSuccessKind == MAP_LOOKUP_TEST.TRUE || isMixedLookupSucceded)
            {
                //do nothing, this.removedKeys[ii] will succeed.
            }
            else if (this.lookupSuccessKind == MAP_LOOKUP_TEST.MOSTLY_FALSE || !isMixedLookupSucceded)
            {
                //this element may not be in the set: patch the place with a random value,
                //so that it is very unlikely for the key to be in the map.
                this.removedKeys[ii] = this.prng.nextInt();
            }
        }

        //call setup of impl, this is the reference implementation, that will stay inchanged.
        this.impl.setup(this.pushedKeys, this.hash_quality, this.removedKeys, this.removedKeys);

        this.impl2.setup(this.pushedKeys, this.hash_quality, this.removedKeys, this.removedKeys);

        //Fill it using PutAll
        this.impl.benchPutAll();
    }

    //Per-invocation setup here, because we must re-fill the map
    //at each iteration so better exclude it from measurement.
    @Setup(Level.Invocation)
    public void setUp() throws Exception
    {
        this.impl2.setCopyOfInstance(this.impl);
    }

    /**
     * Time the 'remove' operation.
     */
    @Benchmark
    public int timeRemove()
    {
        return this.impl2.benchRemoveKeys();
    }

    public static void main(final String[] args) throws RunnerException
    {
        BenchmarkSuiteRunner.runJmhBasicBenchmarkWithCommandLine(BenchmarkHashMapRemove.class, args, 2000, 3000);
    }
}