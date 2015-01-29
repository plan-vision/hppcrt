package com.carrotsearch.hppcrt.jmh;

import org.openjdk.jmh.annotations.Benchmark;

import org.openjdk.jmh.annotations.Setup;

import org.openjdk.jmh.runner.RunnerException;

import com.carrotsearch.hppcrt.BenchmarkSuiteRunner;

/**
 * Benchmark putting a given number of integers / Objects into a hashmap.
 * also the base class for all the other Hash benchmarks.
 */
public class BenchmarkHashMapPut extends BenchmarkHashMapBase
{
    @Setup
    public void setUp() throws Exception
    {
        setUpCommon();

        //call setup of impl, only put setup is OK.
        this.impl.setup(this.pushedKeys, this.hash_quality, this.pushedKeys, this.pushedKeys);
    }

    /**
     * Time the 'put' operation.
     */
    @Benchmark
    public int timePut()
    {
        return this.impl.benchPutAll();
    }

    public static void main(final String[] args) throws RunnerException
    {
        BenchmarkSuiteRunner.runJmhBasicBenchmarkWithCommandLine(BenchmarkHashMapPut.class, args, 1000, 2000);
    }
}