package com.carrotsearch.hppcrt.misc;

import java.util.Arrays;
import com.carrotsearch.hppcrt.BenchmarkSuiteRunner;

/**
 * Runs the suite of benchmarks about hash containers
 */
public class SyntheticBenchmarkSuite
{
    /**
     * List of synthetic benchmarks
     */
    private final static Class<?>[] ALL_BENCHMARKS = new Class[] {

        HppcArraysBench.class,
        HppcSortSyntheticBench.class,
        HppcListSyntheticBench.class,
        HppcHeapsSyntheticBench.class,
        HppcMapSyntheticBench.class
    };

    public static void main(final String[] args) throws Exception
    {
        BenchmarkSuiteRunner.runMain(Arrays.asList(SyntheticBenchmarkSuite.ALL_BENCHMARKS), args);
    }
}
