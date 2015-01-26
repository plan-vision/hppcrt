package com.carrotsearch.hppcrt.jmh;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

import com.carrotsearch.hppcrt.BenchmarkSuiteRunner;
import com.carrotsearch.hppcrt.DistributionGenerator;
import com.carrotsearch.hppcrt.Implementations;
import com.carrotsearch.hppcrt.MapImplementation;
import com.carrotsearch.hppcrt.XorShiftRandom;

/**
 * Benchmark putting a given number of integers into a hashmap.
 */
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class BenchmarkPut
{
    /* Prepare some test data */
    public int[] keys;

    public enum Distribution
    {
        RANDOM, LINEAR, LINEAR_DECREMENT, HIGHBITS;
    }

    @Param
    public Distribution distribution;

    @Param
    public Implementations implementation;

    public MapImplementation<?> impl;

    @Param(
            {
            "5000000"
            })
    public int size;

    /*
     * 
     */
    @Setup
    public void setUp() throws Exception
    {
        // Our tested implementation, uses preallocation
        this.impl = this.implementation.getInstance(this.size);

        final DistributionGenerator gene = new DistributionGenerator(this.size, new XorShiftRandom(0x11223344));

        switch (this.distribution)
        {
            case RANDOM:
                this.keys = gene.RANDOM.prepare(this.size);
                break;
            case LINEAR:
                this.keys = gene.LINEAR.prepare(this.size);
                break;
            case HIGHBITS:
                this.keys = gene.HIGHBITS.prepare(this.size);
                break;
            case LINEAR_DECREMENT:
                this.keys = gene.LINEAR_DECREMENT.prepare(this.size);
                break;

            default:
                throw new RuntimeException();
        }
    }

    /**
     * Time the 'put' operation.
     */
    @Benchmark
    public int timePut()
    {
        int count = 0;

        count += this.impl.putAll(this.keys, this.keys);

        return count;
    }

    public static void main(final String[] args) throws RunnerException
    {
        BenchmarkSuiteRunner.runJmhBasicBenchmarkWithCommandLine(BenchmarkPut.class, args, 500, 1000);
    }
}