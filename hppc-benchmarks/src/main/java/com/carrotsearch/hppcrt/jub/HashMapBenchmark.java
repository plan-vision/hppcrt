package com.carrotsearch.hppcrt.jub;

import java.util.HashMap;
import java.util.Random;

import org.junit.*;

import com.carrotsearch.hppcrt.maps.ObjectObjectOpenHashMap;
import com.carrotsearch.junitbenchmarks.AbstractBenchmark;

/**
 * A micro-benchmark test case for comparing {@link HashMap} against
 * {@link ObjectObjectOpenHashMap}.
 */
public class HashMapBenchmark extends AbstractBenchmark
{
    private static HashMap<Integer, Integer> jre = new HashMap<Integer, Integer>();

    private static int COUNT = 1000000;
    private static Integer[] numbers = new Integer[HashMapBenchmark.COUNT];

    /* */
    @BeforeClass
    public static void createTestSequence()
    {
        final Random rnd = new Random(0x11223344);
        for (int i = 0; i < HashMapBenchmark.COUNT; i++)
            HashMapBenchmark.numbers[i] = rnd.nextInt();
    }

    /* */
    @Before
    public void before()
    {
        HashMapBenchmark.jre.clear();
    }

    /* */
    @Test
    public void testMultipleOperations() throws Exception
    {
        for (int r = 0; r < 2; r++)
        {
            for (int i = 0; i < HashMapBenchmark.numbers.length - r; i++)
            {
                if ((HashMapBenchmark.numbers[i].intValue() & 0x1) == 0)
                    HashMapBenchmark.jre.remove(HashMapBenchmark.numbers[i + r]);
                else
                    HashMapBenchmark.jre.put(HashMapBenchmark.numbers[i], HashMapBenchmark.numbers[i]);
            }
        }
        HashMapBenchmark.jre.clear();
    }
}
