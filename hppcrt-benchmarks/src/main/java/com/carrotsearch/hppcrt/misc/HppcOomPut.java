package com.carrotsearch.hppcrt.misc;

import java.lang.reflect.Field;

import com.carrotsearch.hppcrt.maps.*;

public class HppcOomPut
{
    public static void main(final String[] args)
            throws Exception
    {
        final IntIntOpenHashMap map = new IntIntOpenHashMap(100, 1f);
        final Field f = map.getClass().getDeclaredField("keys");
        f.setAccessible(true);

        boolean hitOOM = false;
        for (int i = 0;; i++) {
            try {
                if (hitOOM) {
                    System.out.println("put(" + i + ")");
                }
                map.put(i, i);
            }
            catch (final OutOfMemoryError e) {
                hitOOM = true;
                System.out.println("OOM, map: " + map.size() + " " + ((int[]) f.get(map)).length);
            }
        }
    }
}
