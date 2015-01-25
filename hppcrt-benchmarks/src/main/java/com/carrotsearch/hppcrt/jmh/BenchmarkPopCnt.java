package com.carrotsearch.hppcrt.jmh;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

import com.carrotsearch.hppcrt.BenchmarkSuiteRunner;
import com.carrotsearch.hppcrt.BroadWord;

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class BenchmarkPopCnt
{
    long[] seq;

    @Param
    public Distribution distribution;

    public static enum Distribution
    {
        ZEROS,
        FULL,
        RANDOM,
        ONEBIT
    }

    @Setup
    public void setUp() throws Exception
    {
        this.seq = new long[1000000];

        final Random rnd = new Random(0xdeadbeef);
        switch (this.distribution) {
            case ZEROS:
                break;
            case FULL:
                Arrays.fill(this.seq, -1);
                break;
            case RANDOM:
                for (int i = 0; i < this.seq.length; i++) {
                    this.seq[i] = rnd.nextLong();
                }
                break;
            case ONEBIT:
                for (int i = 0; i < this.seq.length; i++) {
                    this.seq[i] = 1L << rnd.nextInt(64);
                }
                break;
        }
    }

    @Benchmark
    public int timeLongBitCount() {
        int v = 0;

        for (int j = 0; j < this.seq.length; j++) {
            v += Long.bitCount(this.seq[j]);
        }

        return v;
    }

    @Benchmark
    public int timeHdPopCnd() {
        int v = 0;

        for (int j = 0; j < this.seq.length; j++) {
            v += BenchmarkPopCnt.hdBitCount(this.seq[j]);
        }

        return v;
    }

    @Benchmark
    public int timeRank9() {
        int v = 0;

        for (int j = 0; j < this.seq.length; j++) {
            v += BenchmarkPopCnt.rank9(this.seq[j]);
        }

        return v;
    }

    @Benchmark
    public int timeNaivePopCnt() {
        int v = 0;

        for (int j = 0; j < this.seq.length; j++) {
            v += BenchmarkPopCnt.naivePopCnt(this.seq[j]);
        }

        return v;
    }

    //

    private static int naivePopCnt(long x) {
        int cnt = 0;
        while (x != 0) {
            if (((x >>>= 1) & 1) != 0L) {
                cnt++;
            }
        }
        return cnt;
    }

    private static int hdBitCount(long i) {
        // HD, Figure 5-14
        i = i - ((i >>> 1) & 0x5555555555555555L);
        i = (i & 0x3333333333333333L) + ((i >>> 2) & 0x3333333333333333L);
        i = (i + (i >>> 4)) & 0x0f0f0f0f0f0f0f0fL;
        i = i + (i >>> 8);
        i = i + (i >>> 16);
        i = i + (i >>> 32);
        return (int) i & 0x7f;
    }

    private static int rank9(long x) {
        // Step 0 leaves in each pair of bits the number of ones originally contained in that pair:
        x = x - ((x & 0xAAAAAAAAAAAAAAAAL) >>> 1);
        // Step 1, idem for each nibble:
        x = (x & 0x3333333333333333L) + ((x >>> 2) & 0x3333333333333333L);
        // Step 2, idem for each byte:
        x = (x + (x >>> 4)) & 0x0F0F0F0F0F0F0F0FL;
        // Multiply to sum them all into the high byte, and return the high byte:
        return (int) ((x * BroadWord.L8_L) >>> 56);
    }

    public static void main(final String[] args) throws RunnerException
    {
        BenchmarkSuiteRunner.runJmhBasicBenchmarkWithCommandLine(BenchmarkPopCnt.class, args, 500, 1000);
    }
}
