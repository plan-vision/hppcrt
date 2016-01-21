package com.carrotsearch.hppcrt;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.VerboseMode;

public class BenchmarkSuiteRunner
{
    public static final int NB_MEASUREMENTS_DEFAULT = 10;
    public static final int NB_WARMUPS_DEFAULT = 15;

    public static final String USAGE = "Usage : --- [--warmup=[nb of iterations, default 10]] [--measure=[nb of measurements, default 10]] <overridenJmhParams>\n" + "with overridenJmhParams are in the form of a series of fields 'param=value1,value2...(etc)'\n" + "where param is one @Param of the benchmark, with value1,value2,... replacing the original alternatives.\n";

    public static class BenchmarkOptions
    {
        public int nbWarmups;
        public int nbMeasurements;

        public BenchmarkOptions() {

            //nothing
        }

        @Override
        public String toString() {

            return "{nbMeasurements=" + this.nbMeasurements + ", nbWarmups=" + this.nbWarmups + "}";
        }
    }

    public static class BenchmarkOptionException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;

        public BenchmarkOptionException() {
            super();

        }
    }

    public BenchmarkSuiteRunner() {
        // nothing
    }

    /**
     * Analyze originalArgs as command line arguments to extract common parameters,
     * in the format :    --- [--warmup=[nb of warmups iterations, default 10] --measure=[nb of measurements, default 10]] <other specific benchmark args>,
     * @param originalArgs
     * @param commonOptions : extracted warmup and measurement values, with default values if not present
     * @return the remaining command line args with the common options removed, for further processing by specific benchmarks.
     * @throws BenchmarkOptionException if invalid command line
     */
    public static final String[] parseCommonArguments(final String[] originalArgs, final BenchmarkOptions commonOptions) throws BenchmarkOptionException {

        //default values
        commonOptions.nbMeasurements = BenchmarkSuiteRunner.NB_MEASUREMENTS_DEFAULT;
        commonOptions.nbWarmups = BenchmarkSuiteRunner.NB_WARMUPS_DEFAULT;

        String fullArgString = "";

        //Concat for split
        for (final String strInd : originalArgs) {

            fullArgString += strInd + " ";
        }

        final String[] parsedArgs = fullArgString.split("---");

        //argument check
        if (parsedArgs.length < 2)
        {
            System.out.println(BenchmarkSuiteRunner.USAGE);
            throw new BenchmarkOptionException();
        }

        //Split again the 2nd part for warmup and measure checks:
        final String[] remainingArgs = parsedArgs[1].trim().split("\\s");

        final ArrayList<String> specialArgs = new ArrayList<String>();

        for (final String commonArg : remainingArgs) {

            if (commonArg.contains("--warmup=")) {

                try {
                    commonOptions.nbWarmups = Integer.parseInt(commonArg.split("--warmup=")[1]);
                } finally {
                    //nothing, keep previous or default value,
                }
            }
            else if (commonArg.contains("--measure=")) {

                try {
                    commonOptions.nbMeasurements = Integer.parseInt(commonArg.split("--measure=")[1]);
                } finally {
                    //nothing, keep previous or default value
                }
            }
            else {
                //copy the arg verbatim
                specialArgs.add(commonArg.trim());
            }
        } //end for common arg

        //return the remaining args, where warmup or measure args are stripped off the original list of args
        return specialArgs.toArray(new String[specialArgs.size()]);
    }

    /**
     * Run the jmhClass JMH class with generic params, automatically parsing warmup and measure parameters
     * from commandLine, then executing JMH with the provided JVM memory params.
     * @param commandLine
     * @param minHeapSizeMbytes
     * @param maxHeapSizeMbytes
     * @throws RunnerException
     */
    public static void runJmhBasicBenchmarkWithCommandLine(final Class<?> jmhClass, final String[] commandLine,
            final int minHeapSizeMbytes, final int maxHeapSizeMbytes) throws RunnerException {

        final BenchmarkSuiteRunner.BenchmarkOptions commonOpts = new BenchmarkSuiteRunner.BenchmarkOptions();

        //extract warmup and measurement args
        final String[] overridenJmhParams = BenchmarkSuiteRunner.parseCommonArguments(commandLine, commonOpts);

        BenchmarkSuiteRunner.runJmhBenchmark(jmhClass,
                commonOpts.nbWarmups, commonOpts.nbMeasurements,
                minHeapSizeMbytes, maxHeapSizeMbytes, overridenJmhParams);

    }

    /**
     * Run the jmhClass JMH class with generic params, automatically parsing warmup and measure parameters
     * from commandLine, then executing JMH with warmup and measure parameters and the provided JVM memory params.
     * @param jmhClass
     * @param nbWarmups
     * @param nbIterations
     * @param commandLine
     * @param minHeapSizeMbytes
     * @param maxHeapSizeMbytes
     * @param overridenJmhParams
     * @throws RunnerException
     */
    public static void runJmhBenchmark(final Class<?> jmhClass,
            final int nbWarmups, final int nbIterations,
            final int minHeapSizeMbytes, final int maxHeapSizeMbytes, final String[] overridenJmhParams) throws RunnerException {

        Util.printHeader("Benchmarks for '" + jmhClass.getSimpleName() + "' starting...");

        final OptionsBuilder optBuilder = new OptionsBuilder();

        optBuilder.include(jmhClass.getSimpleName())
        .forks(1)
        .mode(Mode.SingleShotTime)
        .warmupIterations(nbWarmups)
        .measurementIterations(nbIterations)
        .verbosity(VerboseMode.NORMAL)
        .jvmArgsAppend("-Xms" + minHeapSizeMbytes + "m")
        .jvmArgsAppend("-Xmx" + maxHeapSizeMbytes + "m");

        //overridenJmhParams are in the form "[param]=[value1,value2...etc]
        //where param is one @Param of the benchmark, with value1,value2,... replacing the original alternatives.
        for (final String singleParamOverride : overridenJmhParams) {

            final String[] splitParam = singleParamOverride.split("=");

            if (splitParam.length == 2) {

                System.out.println(">>>> Override benchmark @Param '" + splitParam[0] + "' values by " + Arrays.toString(splitParam[1].split(",")));
                optBuilder.param(splitParam[0], splitParam[1].split(","));
            }
        }

        final Options opt = optBuilder.build();

        //run
        new Runner(opt).run();
    }

    /**
     * For each Class in  runClasses, call its main(runArgs) method, so providing runArgs as
     * if run with command line arguments.
     */
    public static void runMain(final List<Class<?>> runClasses, final String[] runArgs) throws Exception
    {
        int i = 0;

        final int totalSize = runClasses.size();

        for (final Class<?> clz : runClasses)
        {
            Util.printHeader(clz.getSimpleName() + " (" + (++i) + "/" + totalSize + ")");

            final Method method = clz.getDeclaredMethod("main", String[].class);

            method.invoke(null, new Object[] { runArgs });
        }
    }
}
