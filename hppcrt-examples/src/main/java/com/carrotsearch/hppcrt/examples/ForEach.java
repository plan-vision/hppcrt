package com.carrotsearch.hppcrt.examples;

import com.carrotsearch.hppcrt.maps.*;
import com.carrotsearch.hppcrt.procedures.IntIntProcedure;

public class ForEach
{
    public void accessAnonymousClassFieldInForEach()
    {
        // [[[start:foreach-counting]]]
        // Create a map.
        final IntIntOpenHashMap map = new IntIntOpenHashMap();
        map.put(1, 2);
        map.put(2, 5);
        map.put(3, 10);

        final int count = map.forEach(new IntIntProcedure()
        {
            int count;

            @Override
            public void apply(final int key, final int value)
            {
                if (value >= 5)
                    count++;
            }
        }).count;
        System.out.println("There are " + count + " values >= 5");
        // [[[end:foreach-counting]]]
    }
}
