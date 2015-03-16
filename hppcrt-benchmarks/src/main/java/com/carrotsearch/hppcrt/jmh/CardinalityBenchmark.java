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
import com.carrotsearch.hppcrt.sets.DoubleLinkedIntSet;
import com.carrotsearch.hppcrt.sets.IntOpenHashSet;

/**
 * Repeated cardinality calculation, very sparse data sets.
 */
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class CardinalityBenchmark
{
    private static final int WND = 100;
    private final int[] numbers = new int[50000];

    private final BitSet bitset = new BitSet();
    private final DoubleLinkedIntSet dlinked = new DoubleLinkedIntSet();
    private final IntOpenHashSet hset = new IntOpenHashSet();

    /* */
    @Setup
    public void prepare()
    {
        final int MAX_RANGE = 0xfffff;
        final Random rnd = new Random(0x11223344);

        for (int i = 0; i < this.numbers.length; i++)
            this.numbers[i] = Math.abs(rnd.nextInt()) & MAX_RANGE;
    }

    /**
     * Simple cardinality calculations, double-linked set (very sparse data).
     */
    @Benchmark
    public int testCardinality_dlinked()
    {
        int card = 0;

        for (int i = 0; i < this.numbers.length - CardinalityBenchmark.WND; i++)
        {
            this.dlinked.clear();

            for (int j = 0; j < CardinalityBenchmark.WND; j++)
            {
                this.dlinked.add(this.numbers[i + j]);
            }
            card += this.dlinked.size();
        }

        return card;
    }

    /**
     * Simple cardinality calculations, hash set (very sparse data).
     * @return
     */
    @Benchmark
    public int testCardinality_hset()
    {
        int card = 0;
        for (int i = 0; i < this.numbers.length - CardinalityBenchmark.WND; i++)
        {
            this.hset.clear();
            for (int j = 0; j < CardinalityBenchmark.WND; j++)
            {
                this.hset.add(this.numbers[i + j]);
            }
            card += this.hset.size();
        }
        return card;
    }

    /**
     * Simple cardinality calculations, bitset (very sparse data).
     */
    @Benchmark
    public int testCardinality_bset()
    {
        int card = 0;
        for (int i = 0; i < this.numbers.length - CardinalityBenchmark.WND; i++)
        {
            this.bitset.clear();
            for (int j = 0; j < CardinalityBenchmark.WND; j++)
            {
                this.bitset.set(this.numbers[i + j]);
            }
            card += this.bitset.cardinality();
        }
        return card;
    }

    public static void main(final String[] args) throws RunnerException
    {
        BenchmarkSuiteRunner.runJmhBasicBenchmarkWithCommandLine(CardinalityBenchmark.class, args, 500, 1000);
    }
}
