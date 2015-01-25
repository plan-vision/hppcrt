package com.carrotsearch.hppcrt.jmh;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.mahout.math.Arrays;
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
import com.carrotsearch.hppcrt.Util;

/**
 * Create a large map of int keys, remove a fraction of the keys and query with half/half keys
 * and a some random values.
 */
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class BenchmarkContainsWithRemoved
{
    /* Prepare some test data */
    public int[] keys;
    public int[] queryKeys;

    @Param(
    {
                "0", "0.25", "0.5", "0.75", "1"
    })
    public double removedKeys;

    @Param
    public Implementations implementation;

    public MapImplementation<?> impl;

    @Param(
    {
                "5000000"
    })
    public int size;

    @Setup
    public void setUp() throws Exception
    {
        final Random rnd = new Random(0x11223344);

        final DistributionGenerator gene = new DistributionGenerator(this.size, rnd);

        // Our tested implementation.
        this.impl = this.implementation.getInstance(this.size);

        // Random keys
        this.keys = gene.RANDOM.prepare(this.size);

        // Half keys, half random. Shuffle order.
        this.queryKeys = Arrays.copyOf(this.keys, this.keys.length);
        for (int i = 0; i < this.queryKeys.length / 2; i++)
        {
            this.queryKeys[i] = rnd.nextInt();
        }

        Util.shuffle(this.queryKeys, rnd);

        // Fill with random keys.
        for (int i = 0; i < this.keys.length; i++)
        {
            this.impl.put(this.keys[i], 0);
        }

        // Shuffle keys and remove a fraction of them.
        final int[] randomized = Util.shuffle(Arrays.copyOf(this.keys, this.keys.length), rnd);
        int removeKeys = (int) (this.removedKeys * this.keys.length);
        for (int i = 0; removeKeys > 0; removeKeys--, i++)
        {
            this.impl.remove(randomized[i]);
        }
    }

    @Benchmark
    public int timeContains()
    {
        int count = 0;

        count += this.impl.containKeys(this.keys);

        return count;
    }

    public static void main(final String[] args) throws RunnerException
    {
        BenchmarkSuiteRunner.runJmhBasicBenchmarkWithCommandLine(BenchmarkContainsWithRemoved.class, args, 500, 1000);
    }
}