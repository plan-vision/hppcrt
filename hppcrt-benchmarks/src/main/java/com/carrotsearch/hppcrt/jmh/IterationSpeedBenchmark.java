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
import com.carrotsearch.hppcrt.cursors.IntCursor;
import com.carrotsearch.hppcrt.lists.IntArrayList;
import com.carrotsearch.hppcrt.mutables.IntHolder;
import com.carrotsearch.hppcrt.procedures.IntProcedure;

/**
 * Various iteration approaches on an integer list.
 */
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class IterationSpeedBenchmark
{
    public static final int CELLS = (1024 * 1024) * 50;
    private IntArrayList list;

    /* */
    @Setup
    public void before()
    {
        this.list = new IntArrayList();
        this.list.resize(IterationSpeedBenchmark.CELLS);

        //fill with random values
        final Random prng = new Random(210654641246431L);

        for (int i = 0; i < this.list.size(); i++) {

            this.list.set(i, prng.nextInt());
        }
    }

    /* */
    @Benchmark
    public int testSimpleGetLoop() throws Exception
    {
        int count = 0;
        for (int i = 0; i < this.list.size(); i++)
        {
            count += this.list.get(i);
        }

        return count;
    }

    /* */
    @Benchmark
    public int testDirectBufferLoop() throws Exception
    {
        final int size = this.list.size();
        final int[] buffer = this.list.buffer;
        int count = 0;
        for (int i = 0; i < size; i++)
        {
            count += buffer[i];
        }
        return count;
    }

    /* */
    @Benchmark
    public int testIterableCursor() throws Exception
    {
        int count = 0;
        for (final IntCursor c : this.list)
        {
            count += c.value;
        }
        return count;
    }

    /* */
    @Benchmark
    public int testWithProcedureClosure()
    {
        final IntHolder holder = new IntHolder();
        this.list.forEach(new IntProcedure() {
            @Override
            public void apply(final int v)
            {
                holder.value += v;
            }
        });
        return holder.value;
    }

    /* */
    @Benchmark
    public int testDirectBufferWithNewFor() throws Exception
    {
        int count = 0;
        for (final int c : this.list.buffer)
        {
            count += c;
        }
        return count;
    }

    public static void main(final String[] args) throws RunnerException
    {
        BenchmarkSuiteRunner.runJmhBasicBenchmarkWithCommandLine(IterationSpeedBenchmark.class, args, 1000, 2000);
    }
}
