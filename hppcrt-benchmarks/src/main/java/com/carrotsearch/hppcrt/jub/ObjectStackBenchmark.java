package com.carrotsearch.hppcrt.jub;

import org.junit.Before;
import org.junit.Test;

import com.carrotsearch.hppcrt.lists.ObjectStack;
import com.carrotsearch.junitbenchmarks.AbstractBenchmark;

/**
 * A micro-benchmark test case for {@link ObjectStack}.
 */
public class ObjectStackBenchmark extends AbstractBenchmark
{
    public static final int COUNT = 5 * 1000000;

    private ObjectStack<Integer> hppc;

    /* */
    @Before
    public void before()
    {
        hppc = new ObjectStack<Integer>();
    }

    /**
     * Test sequential push and pops from the stack (first a lot of pushes, then a lot of
     * pops).
     */
    @Test
    public void testPushPops() throws Exception
    {
        for (int i = 0; i < ObjectStackBenchmark.COUNT; i++)
            hppc.push(/* intrinsic:ktypecast */i);

        while (hppc.size() > 0)
            hppc.pop();
    }
}
