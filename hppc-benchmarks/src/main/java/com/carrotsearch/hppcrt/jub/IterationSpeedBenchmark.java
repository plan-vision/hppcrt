package com.carrotsearch.hppcrt.jub;

import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.carrotsearch.hppcrt.lists.IntArrayList;
import com.carrotsearch.hppcrt.cursors.IntCursor;
import com.carrotsearch.hppcrt.mutables.IntHolder;
import com.carrotsearch.hppcrt.procedures.IntProcedure;
import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;

/**
 * Various iteration approaches on an integer list.
 */
@BenchmarkHistoryChart(filePrefix = "CLASSNAME.history", maxRuns = 50)
@BenchmarkMethodChart(filePrefix = "CLASSNAME.methods")
@BenchmarkOptions(callgc = false, warmupRounds = 10, benchmarkRounds = 10)
public class IterationSpeedBenchmark extends AbstractBenchmark
{
    public static final int CELLS = (1024 * 1024) * 200;
    private static IntArrayList list;
    public volatile int guard;

    /* */
    @BeforeClass
    public static void before()
    {
        IterationSpeedBenchmark.list = new IntArrayList();
        IterationSpeedBenchmark.list.resize(IterationSpeedBenchmark.CELLS);

        //fill with random values
        final Random prng = new Random(210654641246431L);

        for (int i = 0; i < IterationSpeedBenchmark.list.size(); i++) {

            IterationSpeedBenchmark.list.set(i, prng.nextInt());
        }
    }

    @AfterClass
    public static void after()
    {
        IterationSpeedBenchmark.list = null;

    }

    /* */
    @Test
    public void testSimpleGetLoop() throws Exception
    {
        int count = 0;
        for (int i = 0; i < IterationSpeedBenchmark.list.size(); i++)
        {
            count += IterationSpeedBenchmark.list.get(i);
        }

        guard = count;
    }

    /* */
    @Test
    public void testDirectBufferLoop() throws Exception
    {
        final int size = IterationSpeedBenchmark.list.size();
        final int[] buffer = IterationSpeedBenchmark.list.buffer;
        int count = 0;
        for (int i = 0; i < size; i++)
        {
            count += buffer[i];
        }
        guard = count;
    }

    /* */
    @Test
    public void testIterableCursor() throws Exception
    {
        int count = 0;
        for (final IntCursor c : IterationSpeedBenchmark.list)
        {
            count += c.value;
        }
        guard = count;
    }

    /* */
    @Test
    public void testWithProcedureClosure()
    {
        final IntHolder holder = new IntHolder();
        IterationSpeedBenchmark.list.forEach(new IntProcedure() {
            @Override
            public void apply(final int v)
            {
                holder.value += v;
            }
        });
        guard = holder.value;
    }

    /* */
    @Test
    public void testDirectBufferWithNewFor() throws Exception
    {
        int count = 0;
        for (final int c : IterationSpeedBenchmark.list.buffer)
        {
            count += c;
        }
        guard = count;
    }
}
