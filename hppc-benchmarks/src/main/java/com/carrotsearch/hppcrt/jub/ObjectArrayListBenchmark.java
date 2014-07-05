package com.carrotsearch.hppcrt.jub;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.carrotsearch.hppcrt.lists.ObjectArrayList;
import com.carrotsearch.hppcrt.cursors.ObjectCursor;
import com.carrotsearch.hppcrt.mutables.IntHolder;
import com.carrotsearch.hppcrt.procedures.ObjectProcedure;
import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.annotation.*;

/**
 * Benchmark tests for {@link ObjectArrayList}.
 */
@BenchmarkHistoryChart(filePrefix = "CLASSNAME.history", maxRuns = 50)
@BenchmarkMethodChart(filePrefix = "CLASSNAME.methods")
@BenchmarkOptions(callgc = false, warmupRounds = 5, benchmarkRounds = 10)
public class ObjectArrayListBenchmark extends AbstractBenchmark
{
    public static final int CELLS = (1024 * 1024) * 50;
    private static ObjectArrayList<Object> list;

    private static final Object defValue = null;

    /* */
    @BeforeClass
    public static void before()
    {
        ObjectArrayListBenchmark.list = new ObjectArrayList<Object>();
        ObjectArrayListBenchmark.list.resize(ObjectArrayListBenchmark.CELLS);
    }

    @AfterClass
    public static void cleanup()
    {
        ObjectArrayListBenchmark.list = null;
    }

    /* */
    @Test
    public void testSimpleGetLoop() throws Exception
    {
        final ObjectArrayList<Object> list = ObjectArrayListBenchmark.list;
        final int max = list.size();
        int count = 0;
        for (int i = 0; i < max; i++)
        {
            if (list.get(i) != ObjectArrayListBenchmark.defValue)
                count++;
        }
        Assert.assertEquals(0, count);
    }

    /* */
    @Test
    public void testDirectBufferLoop() throws Exception
    {
        final int size = ObjectArrayListBenchmark.list.size();
        final Object[] buffer = ObjectArrayListBenchmark.list.buffer;
        int count = 0;
        for (int i = 0; i < size; i++)
        {
            if (buffer[i] != ObjectArrayListBenchmark.defValue)
                count++;
        }
        Assert.assertEquals(0, count);
    }

    /* */
    @Test
    public void testIterableCursor() throws Exception
    {
        int count = 0;
        for (final ObjectCursor<Object> c : ObjectArrayListBenchmark.list)
        {
            if (c.value != ObjectArrayListBenchmark.defValue)
                count++;
        }
        Assert.assertEquals(0, count);
    }

    /* */
    @Test
    public void testWithProcedureClosure()
    {
        final IntHolder count = new IntHolder();
        ObjectArrayListBenchmark.list.forEach(new ObjectProcedure<Object>() {
            @Override
            public void apply(final Object v)
            {
                if (v != ObjectArrayListBenchmark.defValue)
                    count.value++;
            }
        });
        Assert.assertEquals(0, count.value);
    }

    /* */
    @Test
    public void testDirectBufferWithNewFor() throws Exception
    {
        int count = 0;
        for (final Object c : ObjectArrayListBenchmark.list.buffer)
        {
            if (ObjectArrayListBenchmark.defValue != c)
                count++;
        }
        Assert.assertEquals(0, count);
    }
}
