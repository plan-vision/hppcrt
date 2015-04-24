package com.carrotsearch.hppcrt.misc;

import com.carrotsearch.hppcrt.maps.*;
import com.carrotsearch.hppcrt.sets.*;

/**
 * Requires a lot of heap!
 */
public class HppcMaxHashMapSize
{
    public static void main(final String[] args)
            throws Exception
    {
        final IntHashSet set = new IntHashSet(0x40000000, 0.75f);

        for (int i = 0;; i++) {
            try {
                set.add(i);
            }
            catch (final RuntimeException e) {
                System.out.println("Max capacity: " + set.size());
            }
            catch (final OutOfMemoryError e) {
                System.out.println("OOM hit at size: " + set.size() + " (0x" + Integer.toHexString(set.size()));
            }
        }
    }
}
