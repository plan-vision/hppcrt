package com.carrotsearch.hppc.jub;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;

@BenchmarkHistoryChart(filePrefix = "CLASSNAME.history", maxRuns = 10)
@BenchmarkMethodChart(filePrefix = "CLASSNAME.methods")
@BenchmarkOptions(callgc = false, warmupRounds = 10, benchmarkRounds = 5)
public class HashCollisionsCornerCaseTest extends AbstractBenchmark
{
    private static int NB_ELEMENTS = (int) 10e6;
    private static float LOAD_FACTOR = 0.75f;

    private static final IntOpenHashSet testSet = new IntOpenHashSet(HashCollisionsCornerCaseTest.NB_ELEMENTS, HashCollisionsCornerCaseTest.LOAD_FACTOR);
    private static final IntIntOpenHashMap testMap = new IntIntOpenHashMap(HashCollisionsCornerCaseTest.NB_ELEMENTS, HashCollisionsCornerCaseTest.LOAD_FACTOR);

    private static final IntOpenHashSet testHashSet = new IntOpenHashSet(HashCollisionsCornerCaseTest.NB_ELEMENTS);
    private static final IntOpenHashSet testHashSetNoPerturb = IntOpenHashSet.newInstanceWithoutPerturbations(HashCollisionsCornerCaseTest.NB_ELEMENTS, IntOpenHashSet.DEFAULT_LOAD_FACTOR);

    private static final IntIntOpenHashMap testHashMap = new IntIntOpenHashMap(HashCollisionsCornerCaseTest.NB_ELEMENTS);
    private static final IntIntOpenHashMap testHashMapNoPerturb = IntIntOpenHashMap.newInstanceWithoutPerturbations(HashCollisionsCornerCaseTest.NB_ELEMENTS, IntOpenHashSet.DEFAULT_LOAD_FACTOR);

    @BeforeClass
    public static void beforeClass()
    {
        //we are to really push realNbElements to fill the sets up to their
        //max allowable load factor to really test the worst case here.
        final int realNbElements = HashCollisionsCornerCaseTest.testSet.capacity() - 1;

        for (int i = realNbElements; i-- != 0;) {

            HashCollisionsCornerCaseTest.testSet.add(i);
            HashCollisionsCornerCaseTest.testMap.put(i, 0);
        }
    }

    @Before
    public void before()
    {
        HashCollisionsCornerCaseTest.testHashSet.clear();
        HashCollisionsCornerCaseTest.testHashMap.clear();
        HashCollisionsCornerCaseTest.testHashSetNoPerturb.clear();
        HashCollisionsCornerCaseTest.testHashMapNoPerturb.clear();
    }

    /** @see "http://issues.carrot2.org/browse/HPPC-80" */
    @Test
    public void testHashSets()
    {
        HashCollisionsCornerCaseTest.testHashSet.addAll(HashCollisionsCornerCaseTest.testSet);
    }

    @Test
    public void testHashSetsNoPerturbation()
    {
        HashCollisionsCornerCaseTest.testHashSetNoPerturb.addAll(HashCollisionsCornerCaseTest.testSet);
    }

    @Test
    public void testHashSetsWithReallocation()
    {
        final IntOpenHashSet test = new IntOpenHashSet(IntOpenHashSet.DEFAULT_CAPACITY, HashCollisionsCornerCaseTest.LOAD_FACTOR);

        test.addAll(HashCollisionsCornerCaseTest.testSet);
    }

    @Test
    public void testHashSetsNoPerturbationWithReallocation()
    {
        final IntOpenHashSet test = IntOpenHashSet.newInstanceWithoutPerturbations(IntOpenHashSet.DEFAULT_CAPACITY, HashCollisionsCornerCaseTest.LOAD_FACTOR);
        test.addAll(HashCollisionsCornerCaseTest.testSet);
    }

    /** @see "http://issues.carrot2.org/browse/HPPC-80" */
    @Test
    public void testHashMaps()
    {
        HashCollisionsCornerCaseTest.testHashMap.putAll(HashCollisionsCornerCaseTest.testMap);
    }

    /** @see "http://issues.carrot2.org/browse/HPPC-80" */
    @Test
    public void testHashMapsNoPerturbation()
    {
        HashCollisionsCornerCaseTest.testHashMapNoPerturb.putAll(HashCollisionsCornerCaseTest.testMap);
    }

    /** @see "http://issues.carrot2.org/browse/HPPC-80" */
    @Test
    public void testHashMapsWithReallocation()
    {
        final IntIntOpenHashMap test = new IntIntOpenHashMap(IntIntOpenHashMap.DEFAULT_CAPACITY, HashCollisionsCornerCaseTest.LOAD_FACTOR);
        test.putAll(HashCollisionsCornerCaseTest.testMap);
    }

    /** @see "http://issues.carrot2.org/browse/HPPC-80" */
    @Test
    public void testHashMapsNoPerturbationWithReallocation()
    {
        final IntIntOpenHashMap test = IntIntOpenHashMap.newInstanceWithoutPerturbations(IntIntOpenHashMap.DEFAULT_CAPACITY, HashCollisionsCornerCaseTest.LOAD_FACTOR);
        test.putAll(HashCollisionsCornerCaseTest.testMap);
    }
}
