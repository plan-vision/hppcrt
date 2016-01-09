package com.carrotsearch.hppcrt.jmh;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.runner.RunnerException;

import com.carrotsearch.hppcrt.BenchmarkSuiteRunner;
import com.carrotsearch.hppcrt.Util;

/**
 * Benchmark putting a given number of integers / Objects into a hashmap,
 * in the case of the keys already exist in the map : this bench try to measure
 * the search-and-replace-value performance.
 */

public class BenchmarkHashMapExistingKeysPut extends BenchmarkHashMapBase
{
    public BenchmarkHashMapExistingKeysPut() {
        super();
    }

    //Setup - once here.
    @Setup
    public void setUp() throws Exception {
        setUpCommon();

        //call setup of impl
        this.impl.setup(this.pushedKeys, this.hash_quality, this.pushedKeys, this.pushedKeys);

        //Fill it using an initial PutAll
        this.impl.benchPutAll();
    }

    //Per-invocation setup here, because we must re-shuffle the map keys
    //at each iteration so better exclude it from measurement.
    @Setup(Level.Invocation)
    public void setUpPerInvocation() throws Exception {

        //Shuffle order, so the put() (replace value only) test order is different from
        //the previous put() order
        this.impl.reshuffleInsertedKeys(this.prng);
        this.impl.reshuffleInsertedValues(this.prng);
    }


    /**
     * Time the 'put' operation, of existing keys, reshuffle each time.
     */
    @Benchmark
    public int timeExistingKeysPut() {

        return this.impl.benchPutAll();
    }

    public static void main(final String[] args) throws RunnerException {
        BenchmarkSuiteRunner.runJmhBasicBenchmarkWithCommandLine(BenchmarkHashMapExistingKeysPut.class, args, 2000, 3000);
    }
}