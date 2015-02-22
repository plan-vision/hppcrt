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
import com.carrotsearch.hppcrt.implementations.BigramCountingBase;

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class BenchmarkBigramCounting
{
    private BigramCountingBase bc;

    @Param
    public Library library;

    public static enum Library
    {
        HPPC,
        TROVE,
        FASTUTIL_OPEN,
        FASTUTIL_LINKED,
        MAHOUT,
        JAVA_NAIVE,
        JAVA_SMART
    }

    @Setup
    public void setUp() throws Exception
    {
        this.bc = new BigramCountingBase();
        this.bc.prepareData();
    }

    @Benchmark
    public int timeLibrary()
    {
        int count = 0;

        switch (this.library)
        {
            case HPPC:
                count += this.bc.hppc();
                break;
            case TROVE:
                count += this.bc.trove();
                break;
            case FASTUTIL_LINKED:
                this.bc.fastutilLinkedOpenHashMap();
                break;
            case FASTUTIL_OPEN:
                count += this.bc.fastutilOpenHashMap();
                break;
            case MAHOUT:
                count += this.bc.mahoutCollections();
                break;
            case JAVA_NAIVE:
                count += this.bc.jcfNaive();
                break;
            case JAVA_SMART:
                count += this.bc.jcfSmarter();
                break;
            default:
                break;
        }

        return count;
    }

    public static void main(final String[] args) throws RunnerException
    {
        BenchmarkSuiteRunner.runJmhBasicBenchmarkWithCommandLine(BenchmarkBigramCounting.class, args, 500, 1000);
    }
}
