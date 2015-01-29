package com.carrotsearch.hppcrt.jmh;

import java.util.Arrays;

import org.openjdk.jmh.annotations.Benchmark;

import org.openjdk.jmh.annotations.Param;

import org.openjdk.jmh.annotations.Setup;

import org.openjdk.jmh.runner.RunnerException;

import com.carrotsearch.hppcrt.BenchmarkSuiteRunner;
import com.carrotsearch.hppcrt.Util;

/**
 * Benchmark putting a given number of integers / Objects into a hashmap.
 * also the base class for all the other Hash benchmarks.
 */
public class BenchmarkHashMapContains extends BenchmarkHashMapBase
{
    public enum MAP_LOOKUP_TEST
    {
        MOSTLY_TRUE,
        MOSTLY_FALSE,
        MIXED
    }

    @Param
    MAP_LOOKUP_TEST lookupSuccessKind;

    private int[] containsKeys;

    public BenchmarkHashMapContains() {
        super();
    }

    /**
     * 
     */
    @Setup
    public void setUp() throws Exception
    {
        setUpCommon();

        //Generate a series of containsKeys // B) Process by get/contains
        this.containsKeys = Arrays.copyOf(this.pushedKeys, this.pushedKeys.length);

        //Shuffle order, so the contains test order is different from
        //the insertion order.
        Util.shuffle(this.containsKeys, this.prng);

        for (int ii = 0; ii < this.pushedKeys.length; ii++)
        {
            final boolean isMixedLookupSucceded = (this.lookupSuccessKind == MAP_LOOKUP_TEST.MIXED) && this.prng.nextBoolean();

            if (this.lookupSuccessKind == MAP_LOOKUP_TEST.MOSTLY_TRUE || isMixedLookupSucceded)
            {
                //do nothing, this.containsKeys[ii] will succeed.
            }
            else if (this.lookupSuccessKind == MAP_LOOKUP_TEST.MOSTLY_FALSE || !isMixedLookupSucceded)
            {
                //this element may not be in the set: patch the place with a random value,
                //so that it is very unlikely for the key to be in the map.
                this.containsKeys[ii] = this.prng.nextInt();
            }
        }

        //call setup of impl
        this.impl.setup(this.pushedKeys, this.hash_quality, this.containsKeys, this.containsKeys);

        //Fill the map, using the putAll
        this.impl.benchPutAll();
    }

    /**
     * Time the 'contains' operation.
     */
    @Benchmark
    public int timeContains()
    {
        return this.impl.benchContainKeys();
    }

    public static void main(final String[] args) throws RunnerException
    {
        BenchmarkSuiteRunner.runJmhBasicBenchmarkWithCommandLine(BenchmarkHashMapContains.class, args, 1000, 2000);
    }
}