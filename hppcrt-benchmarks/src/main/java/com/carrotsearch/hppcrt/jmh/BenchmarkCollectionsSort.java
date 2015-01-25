package com.carrotsearch.hppcrt.jmh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

import com.carrotsearch.hppcrt.BenchmarkSuiteRunner;
import com.carrotsearch.hppcrt.sorting.IndirectComparator;
import com.carrotsearch.hppcrt.sorting.IndirectSort;
import com.google.common.collect.Lists;

/**
 * Benchmark {@link Collections#sort(java.util.List)}, in particular for differences
 * in the new JDK 1.7 (TimSort). The point of this benchmark is to get the order
 * of elements (their indexes), not an array or collection of sorted elements (!).
 */
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class BenchmarkCollectionsSort
{
    @Param("1000000")
    public int size;

    private ArrayList<String> data;

    //represent the initial index of data
    private int[] indexes;
    private int[] indexesClone;
    private Integer[] indexesInteger;

    @Setup
    public void setUp() throws Exception
    {

        this.data = Lists.newArrayList();
        this.indexes = new int[this.size];
        this.indexesClone = new int[this.size];
        this.indexesInteger = new Integer[this.size];

        for (int i = 0; i < this.size; i++)
        {
            this.data.add(Integer.toString(i));
            this.indexes[i] = i;
            this.indexesInteger[i] = i;
        }

        Collections.shuffle(this.data);
    }

    /*
     * 
     */
    @Benchmark
    public int timeIndirectMergeSort()
    {
        int count = 0;

        final Object[] input = this.data.toArray();
        final int[] ordered = IndirectSort.mergesort(input, 0, input.length, new Comparator<Object>()
        {
            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public int compare(final Object o1, final Object o2)
            {
                return ((Comparable) o1).compareTo(o2);
            }
        });

        count += ordered[0];

        return count;
    }

    /*
     * 
     */
    @Benchmark
    public int timeLegacySort()
    {
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        return timeNewSort();
    }

    /*
     * 
     */
    @Benchmark
    public int timeNewSort()
    {
        final Integer[] indexesClone = this.indexesInteger.clone();

        int count = 0;

        Arrays.sort(indexesClone, new Comparator<Integer>()
        {
            final ArrayList<String> dta = BenchmarkCollectionsSort.this.data;

            @Override
            public int compare(final Integer o1, final Integer o2)
            {
                return this.dta.get(o1.intValue()).compareTo(
                        this.dta.get(o2.intValue()));
            }
        });
        count += indexesClone[0];

        return count;
    }

    /*
     * 
     */
    @Benchmark
    public int timeIndirectQuickSort()
    {
        int count = 0;

        //overwrite the same array, since it is modified in-place
        System.arraycopy(this.indexes, 0, this.indexesClone, 0, this.indexes.length);

        IndirectSort.quicksort(0, this.indexesClone.length, new IndirectComparator() {

            final ArrayList<String> dta = BenchmarkCollectionsSort.this.data;

            @Override
            public int compare(final int o1, final int o2)
            {
                return this.dta.get(o1).compareTo(this.dta.get(o2));
            }
        }, this.indexes);

        count += this.indexesClone[0];

        return count;
    }

    public static void main(final String[] args) throws RunnerException
    {
        BenchmarkSuiteRunner.runJmhBasicBenchmarkWithCommandLine(BenchmarkCollectionsSort.class, args, 500, 1000);
    }
}
