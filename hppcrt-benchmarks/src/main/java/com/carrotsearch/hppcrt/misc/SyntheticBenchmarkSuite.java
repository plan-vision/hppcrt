package com.carrotsearch.hppcrt.misc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.carrotsearch.hppcrt.Util;

/**
 * Runs the suite of benchmarks about hash containers
 */
public class SyntheticBenchmarkSuite
{
    /**
     * List of synthetic benchmarks
     */
    private final static Class<?>[] ALL_BENCHMARKS = new Class[] {

        HppcSortSyntheticBench.class,
        HppcListSyntheticBench.class,
        HppcHeapsSyntheticBench.class,
        HppcMapSyntheticBench.class
    };

    public static void main(final String[] args) throws Exception
    {
        String fullArgString = "";

        for (final String strInd : args) {

            fullArgString += strInd + " ";
        }

        final String[] parsedArgs = fullArgString.split("---");

        //argument check
        if (parsedArgs.length != 2)
        {
            System.out.println("Args: --- <benchmark args>");
            System.out.println("Known benchmark classes: ");

            for (final Class<?> clz : SyntheticBenchmarkSuite.ALL_BENCHMARKS)
            {
                System.out.println("\t" + clz.getName());
            }
            return;
        }

        final ArrayList<String> argsListOther = new ArrayList<String>();

        for (final String arg : parsedArgs[args.length - 1].trim().split("\\s")) {

            argsListOther.add(arg.trim());
        }

        final List<Class<?>> classesOther = new ArrayList<Class<?>>();

        //enumerate
        for (final Class<?> clz : SyntheticBenchmarkSuite.ALL_BENCHMARKS) {

            classesOther.add(clz);

        } //end for

        Util.printSystemInfo("Synthetic Benchmark Suite for HPPC-RT starting.");
        SyntheticBenchmarkSuite.runBenchmarks(classesOther, argsListOther.toArray(new String[argsListOther.size()]));
    }

    /**
     * 
     */
    private static void runBenchmarks(final List<Class<?>> otherClasses, final String[] argsOther) throws Exception
    {
        int i = 0;

        final int totalSize = otherClasses.size();

        for (final Class<?> clz : otherClasses)
        {
            Util.printHeader(clz.getSimpleName() + " (" + (++i) + "/" + totalSize + ")");

            final Method method = clz.getDeclaredMethod("main", String[].class);

            method.invoke(null, new Object[] { argsOther });
        }
    }
}
