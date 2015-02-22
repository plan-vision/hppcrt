package com.carrotsearch.hppcrt.jmh;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

import com.carrotsearch.hppcrt.BenchmarkSuiteRunner;
import com.carrotsearch.hppcrt.BitSet;
import com.carrotsearch.hppcrt.BitSetIterator;
import com.carrotsearch.hppcrt.cursors.IntCursor;

/**
 * Simple benchmarks against <code>java.util.BitSet</code>.
 */
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class BitSetBenchmark
{
    private BitSet hppc;
    private java.util.BitSet jre;

    /** Pseudo-random with initial seed (repeatability). */
    private final Random rnd = new Random(0x11223344);

    /* */
    @Setup
    public void before()
    {
        final int MB = 1024 * 1024;
        final int bits = 128 * MB * 4;

        this.hppc = new BitSet(bits);
        this.jre = new java.util.BitSet(bits);

        // Randomly fill every bits (this is fairly dense distribution).
        for (int i = 0; i < bits; i += 1 + this.rnd.nextInt(10))
        {
            if (this.rnd.nextBoolean())
            {
                this.hppc.set(i);
                this.jre.set(i);
            }
        }
    }

    @Benchmark
    public long testCardinalityHPPC()
    {
        return this.hppc.cardinality();
    }

    @Benchmark
    public int testCardinalityJRE()
    {
        return this.jre.cardinality();
    }

    @Benchmark
    public int testBitSetIteratorHPPC()
    {
        final BitSetIterator bi = this.hppc.iterator();
        int sum = 0;

        for (int i = bi.nextSetBit(); i >= 0; i = bi.nextSetBit())
        {
            sum += i;
        }

        return sum;
    }

    @Benchmark
    public int testIntCursorIteratorHPPC()
    {
        int sum = 0;
        for (final IntCursor c : this.hppc.asIntLookupContainer())
        {
            sum += c.value;
        }

        return sum;
    }

    @Benchmark
    public int testIteratorHPPC()
    {
        int sum = 0;
        for (int i = this.hppc.nextSetBit(0); i >= 0; i = this.hppc.nextSetBit(i + 1))
        {
            sum += i;
        }

        return sum;
    }

    @Benchmark
    public int testIteratorJRE()
    {
        int sum = 0;
        for (int i = this.jre.nextSetBit(0); i >= 0; i = this.jre.nextSetBit(i + 1))
        {
            sum += i;
        }
        return sum;
    }

    public static void main(final String[] args) throws RunnerException
    {
        BenchmarkSuiteRunner.runJmhBasicBenchmarkWithCommandLine(BitSetBenchmark.class, args, 500, 1000);
    }
}
