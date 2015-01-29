package com.carrotsearch.hppcrt;

import com.carrotsearch.hppcrt.maps.*;

import gnu.trove.map.hash.TIntIntHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.io.IOException;

import org.apache.commons.io.IOUtils;

public class BigramCountingBase
{
    /* Prepare some test data */
    public static char[] DATA;

    public static void prepareData() throws IOException
    {
        final byte[] dta = IOUtils.toByteArray(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("books-polish.txt"));
        BigramCountingBase.DATA = new String(dta, "UTF-8").toCharArray();
    }

    public int hppc()
    {
        // [[[start:bigram-counting]]]
        // Some character data
        final char[] CHARS = BigramCountingBase.DATA;

        // We'll use a int -> int map for counting. A bigram can be encoded
        // as an int by shifting one of the bigram's characters by 16 bits
        // and then ORing the other character to form a 32-bit int.
        final IntIntOpenHashMap map = new IntIntOpenHashMap(
                IntIntOpenHashMap.DEFAULT_CAPACITY,
                IntIntOpenHashMap.DEFAULT_LOAD_FACTOR);

        int count = 0;

        for (int i = 0; i < CHARS.length - 1; i++)
        {
            final int bigram = CHARS[i] << 16 | CHARS[i + 1];
            count += map.putOrAdd(bigram, 1, 1);
        }
        // [[[end:bigram-counting]]]

        return count;
    }

    public int trove()
    {
        final char[] CHARS = BigramCountingBase.DATA;
        final TIntIntHashMap map = new TIntIntHashMap();

        int count = 0;

        for (int i = 0; i < CHARS.length - 1; i++)
        {
            final int bigram = CHARS[i] << 16 | CHARS[i + 1];
            count += map.adjustOrPutValue(bigram, 1, 1);
        }

        return count;
    }

    public int mahoutCollections()
    {
        final char[] CHARS = BigramCountingBase.DATA;
        final org.apache.mahout.math.map.OpenIntIntHashMap map =
                new org.apache.mahout.math.map.OpenIntIntHashMap();

        int count = 0;

        for (int i = 0; i < CHARS.length - 1; i++)
        {
            final int bigram = CHARS[i] << 16 | CHARS[i + 1];
            count += map.adjustOrPutValue(bigram, 1, 1);
        }

        return count;
    }

    public int fastutilOpenHashMap()
    {
        final char[] CHARS = BigramCountingBase.DATA;
        final Int2IntOpenHashMap map = new Int2IntOpenHashMap();
        map.defaultReturnValue(0);

        int count = 0;

        for (int i = 0; i < CHARS.length - 1; i++)
        {
            final int bigram = CHARS[i] << 16 | CHARS[i + 1];
            count += map.addTo(bigram, 1);
        }

        return count;
    }

    public int fastutilLinkedOpenHashMap()
    {
        final char[] CHARS = BigramCountingBase.DATA;
        final Int2IntLinkedOpenHashMap map = new Int2IntLinkedOpenHashMap();
        map.defaultReturnValue(0);

        int count = 0;

        for (int i = 0; i < CHARS.length - 1; i++)
        {
            final int bigram = CHARS[i] << 16 | CHARS[i + 1];
            count += map.addTo(bigram, 1);
        }

        return count;
    }
}