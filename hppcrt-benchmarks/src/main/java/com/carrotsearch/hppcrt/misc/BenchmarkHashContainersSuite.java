package com.carrotsearch.hppcrt.misc;

import java.util.Arrays;

import com.carrotsearch.hppcrt.BenchmarkSuiteRunner;
import com.carrotsearch.hppcrt.jmh.BenchmarkHashCollisions;
import com.carrotsearch.hppcrt.jmh.BenchmarkHashMapContains;
import com.carrotsearch.hppcrt.jmh.BenchmarkHashMapPut;
import com.carrotsearch.hppcrt.jmh.BenchmarkHashMapRemove;

/**
 * Runs the suite of benchmarks about hash containers
 */
public class BenchmarkHashContainersSuite
{
    private final static Class<?>[] ALL_BENCHMARKS = new Class[]
    {
            BenchmarkHashCollisions.class,
            BenchmarkHashMapPut.class,
        BenchmarkHashMapContains.class,
        BenchmarkHashMapRemove.class,

    };

    public static void main(final String[] args) throws Exception
    {
        BenchmarkSuiteRunner.runMain(Arrays.asList(BenchmarkHashContainersSuite.ALL_BENCHMARKS), args);
    }
}
