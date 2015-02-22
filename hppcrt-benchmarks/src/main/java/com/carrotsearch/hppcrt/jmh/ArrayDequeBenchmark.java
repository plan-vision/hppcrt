package com.carrotsearch.hppcrt.jmh;

import java.util.ArrayDeque;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

import com.carrotsearch.hppcrt.BenchmarkSuiteRunner;
import com.carrotsearch.hppcrt.lists.ObjectStack;

/**
 * A micro-benchmark test case for comparing {@link ArrayDeque} (used as a {@link Stack}
 * against {@link ObjectStack}.
 */
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class ArrayDequeBenchmark
{
    private ArrayDeque<Integer> jre;

    private static final int COUNT = (int) 1e7;

    /* */
    @Setup
    public void before()
    {
        this.jre = new ArrayDeque<Integer>();
    }

    /**
     * Test sequential push and pops from the stack (first a lot of pushes, then a lot of
     * pops).
     */
    @Benchmark
    public int testPushPops(final Blackhole b)
    {
        int count = 0;

        for (int i = 0; i < ArrayDequeBenchmark.COUNT; i++)
            this.jre.push(i);

        count += this.jre.size();

        while (this.jre.size() > 0)
            this.jre.pop();

        count += this.jre.size();

        //just to be sure
        b.consume(count);

        return count;
    }

    public static void main(final String[] args) throws RunnerException
    {
        BenchmarkSuiteRunner.runJmhBasicBenchmarkWithCommandLine(ArrayDequeBenchmark.class, args, 500, 1000);
    }
}
