package com.carrotsearch.hppcrt;

import com.carrotsearch.hppcrt.maps.*;

import gnu.trove.map.hash.TIntIntHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class BigramCountingBase
{
    /* Prepare some test data */
    public static char[] DATA;

    /* Prevent dead code removal. */
    public volatile int guard;

    @BeforeClass
    public static void prepareData() throws IOException
    {
        final byte[] dta = IOUtils.toByteArray(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("books-polish.txt"));
        BigramCountingBase.DATA = new String(dta, "UTF-8").toCharArray();
    }

    @Test
    public void hppc()
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

        for (int i = 0; i < CHARS.length - 1; i++)
        {
            final int bigram = CHARS[i] << 16 | CHARS[i + 1];
            map.putOrAdd(bigram, 1, 1);
        }
        // [[[end:bigram-counting]]]

        guard = map.size();
    }

    @Test
    public void trove()
    {
        final char[] CHARS = BigramCountingBase.DATA;
        final TIntIntHashMap map = new TIntIntHashMap();

        for (int i = 0; i < CHARS.length - 1; i++)
        {
            final int bigram = CHARS[i] << 16 | CHARS[i + 1];
            map.adjustOrPutValue(bigram, 1, 1);
        }

        guard = map.size();
    }

    @Test
    public void mahoutCollections()
    {
        final char[] CHARS = BigramCountingBase.DATA;
        final org.apache.mahout.math.map.OpenIntIntHashMap map =
                new org.apache.mahout.math.map.OpenIntIntHashMap();
        for (int i = 0; i < CHARS.length - 1; i++)
        {
            final int bigram = CHARS[i] << 16 | CHARS[i + 1];
            map.adjustOrPutValue(bigram, 1, 1);
        }

        guard = map.size();
    }

    @Test
    public void fastutilOpenHashMap()
    {
        final char[] CHARS = BigramCountingBase.DATA;
        final Int2IntOpenHashMap map = new Int2IntOpenHashMap();
        map.defaultReturnValue(0);

        for (int i = 0; i < CHARS.length - 1; i++)
        {
            final int bigram = CHARS[i] << 16 | CHARS[i + 1];
            map.addTo(bigram, 1);
        }

        guard = map.size();
    }

    @Test
    public void fastutilLinkedOpenHashMap()
    {
        final char[] CHARS = BigramCountingBase.DATA;
        final Int2IntLinkedOpenHashMap map = new Int2IntLinkedOpenHashMap();
        map.defaultReturnValue(0);

        for (int i = 0; i < CHARS.length - 1; i++)
        {
            final int bigram = CHARS[i] << 16 | CHARS[i + 1];
            map.addTo(bigram, 1);
        }

        guard = map.size();
    }
}