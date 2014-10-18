package com.carrotsearch.hppcrt.examples;

import org.junit.Test;

import com.carrotsearch.hppcrt.sets.*;
import com.carrotsearch.hppcrt.cursors.IntCursor;
import com.carrotsearch.hppcrt.procedures.IntProcedure;

public class IteratingOverSets
{
    IntOpenHashSet prepare(final int size)
    {
        final IntOpenHashSet set = new IntOpenHashSet(size);
        for (int i = 0; i < size / 2; i++)
        {
            set.add(i);
        }
        return set;
    }

    @Test
    public void testIterableCursor()
    {
        // [[[start:iteration-sets-using-iterator]]]
        // Prepare some set to iterate over
        final IntOpenHashSet set = prepare(10);

        // Sets implement the Iterable interface that returns [type]Cursor elements.
        // The cursor contains the index and value of the current element.
        for (final IntCursor c : set)
        {
            System.out.println(c.index + ": " + c.value);
        }
        // [[[end:iteration-sets-using-iterator]]]
    }

    @Test
    public void testWithProcedureClosure()
    {
        // [[[start:iteration-sets-using-procedures]]]
        final IntOpenHashSet set = prepare(10);

        // Sets also support iteration through [type]Procedure interfaces.
        // The apply() method will be called once for each element in the set.

        // Iteration from head to tail
        set.forEach(new IntProcedure()
        {
            @Override
            public void apply(final int value)
            {
                System.out.println(value);
            }
        });
        // [[[end:iteration-sets-using-procedures]]]
    }

    @Test
    public void testDirectBufferLoop() throws Exception
    {
        // [[[start:iteration-sets-using-direct-buffer-access]]]
        final IntOpenHashSet set = prepare(10);

        // For the fastest iteration, you can access the sets's data buffers directly.
        final int[] keys = set.keys;

        for (int i = 0; i < keys.length; i++)
        {
            if (keys[i] != 0) {
                System.out.println(keys[i]);
            }
        }
        // [[[end:iteration-sets-using-direct-buffer-access]]]
    }
}
