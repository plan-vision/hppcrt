package com.carrotsearch.hppcrt.jmh;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

import com.carrotsearch.hppcrt.BenchmarkSuiteRunner;
import com.carrotsearch.hppcrt.cursors.ObjectCursor;
import com.carrotsearch.hppcrt.lists.ObjectArrayList;
import com.carrotsearch.hppcrt.procedures.ObjectProcedure;

/**
 * Benchmark tests for {@link ObjectArrayList}.
 */
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class ObjectArrayListBenchmark
{
    public static final int CELLS = (1024 * 1024) * 50;
    private ObjectArrayList<Object> list;

    private static final Object defValue = null;

    /* */
    @Setup
    public void before()
    {
        this.list = new ObjectArrayList<Object>();
        this.list.resize(ObjectArrayListBenchmark.CELLS);
    }

    /* */
    @Benchmark
    public int testSimpleGetLoop()
    {
        final ObjectArrayList<Object> list = this.list;
        final int max = list.size();
        int count = 0;
        for (int i = 0; i < max; i++)
        {
            if (list.get(i) != ObjectArrayListBenchmark.defValue)
                count++;
        }

        return count;
    }

    /* */
    @Benchmark
    public int testDirectBufferLoop()
    {
        final int size = this.list.size();
        final Object[] buffer = this.list.buffer;
        int count = 0;
        for (int i = 0; i < size; i++)
        {
            if (buffer[i] != ObjectArrayListBenchmark.defValue)
                count++;
        }

        return count;
    }

    /* */
    @Benchmark
    public int testIterableCursor()
    {
        int count = 0;
        for (final ObjectCursor<Object> c : this.list)
        {
            if (c.value != ObjectArrayListBenchmark.defValue)
                count++;
        }

        return count;
    }

    /* */
    @Benchmark
    public int testWithProcedureClosure()
    {
        final AtomicInteger count = new AtomicInteger();
        this.list.forEach(new ObjectProcedure<Object>() {
            @Override
            public void apply(final Object v)
            {
                if (v != ObjectArrayListBenchmark.defValue)
                    count.incrementAndGet();
            }
        });

        return count.get();
    }

    /* */
    @Benchmark
    public int testDirectBufferWithNewFor()
    {
        int count = 0;
        for (final Object c : this.list.buffer)
        {
            if (ObjectArrayListBenchmark.defValue != c)
                count++;
        }
        return count;
    }

    public static void main(final String[] args) throws RunnerException
    {
        BenchmarkSuiteRunner.runJmhBasicBenchmarkWithCommandLine(ObjectArrayListBenchmark.class, args, 1000, 2000);
    }
}
