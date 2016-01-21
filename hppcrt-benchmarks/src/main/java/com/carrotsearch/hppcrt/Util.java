package com.carrotsearch.hppcrt;

import java.util.Random;


public final class Util
{

    /**
     * shuffle array contents
     * @param array
     * @param rnd
     * @return
     */
    public static int[] shuffle(final int[] array, final Random rnd)
    {
        for (int i = array.length - 1; i > 0; i--)
        {
            final int pos = rnd.nextInt(i + 1);
            final int t = array[pos];
            array[pos] = array[i];
            array[i] = t;
        }
        return array;
    }

    /**
     * shuffle object array contents
     * @param array
     * @param rnd
     * @return
     */
    public static <T> T[] shuffle(final T[] array, final Random rnd)
    {
        for (int i = array.length - 1; i > 0; i--)
        {
            final int pos = rnd.nextInt(i + 1);
            final T t = array[pos];
            array[pos] = array[i];
            array[i] = t;
        }
        return array;
    }

    /**
     * shuffle ObjectIndexedContainer contents
     * @param array
     * @param rnd
     * @return
     */
    public static <T> ObjectIndexedContainer<T> shuffle(final ObjectIndexedContainer<T> array, final Random rnd)
    {
        for (int i = array.size() - 1; i > 0; i--)
        {
            final int pos = rnd.nextInt(i + 1);
            final T t = array.get(pos);
            array.set(pos, array.get(i));
            array.set(i, t);
        }
        return array;
    }

    /**
     * shuffle IntIndexedContainer contents
     * @param array
     * @param rnd
     * @return
     */
    public static IntIndexedContainer shuffle(final IntIndexedContainer array, final Random rnd)
    {
        for (int i = array.size() - 1; i > 0; i--)
        {
            final int pos = rnd.nextInt(i + 1);
            final int t = array.get(pos);
            array.set(pos, array.get(i));
            array.set(i, t);
        }
        return array;
    }

    public static void printHeader(final String msg)
    {
        System.out.println();
        System.out.println("================================================================================");
        System.out.println(msg);
        System.out.println("================================================================================");
        System.out.flush();
    }
}
