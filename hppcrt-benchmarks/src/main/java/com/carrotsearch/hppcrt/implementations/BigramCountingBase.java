package com.carrotsearch.hppcrt.implementations;

import gnu.trove.map.hash.TIntIntHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.carrotsearch.hppcrt.maps.IntIntOpenHashMap;
import com.carrotsearch.hppcrt.mutables.IntHolder;

public class BigramCountingBase
{
    /* Prepare some test data */
    public char[] data;

    public void prepareData() throws IOException
    {
        final byte[] dta = IOUtils.toByteArray(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("books-polish.txt"));
        this.data = new String(dta, "UTF-8").toCharArray();
    }

    public int hppc()
    {
        // [[[start:bigram-counting]]]
        // Some character data
        final char[] CHARS = this.data;

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
        final char[] CHARS = this.data;
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
        final char[] CHARS = this.data;
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
        final char[] CHARS = this.data;
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
        final char[] CHARS = this.data;
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

    public int jcfSmarter()
    {
        int benchCount = 0;

        final char[] CHARS = this.data;
        final Map<Integer, IntHolder> counts = new HashMap<Integer, IntHolder>();
        for (int i = 0; i < CHARS.length - 1; i++)
        {
            final int bigram = CHARS[i] << 16 | CHARS[i + 1];
            final IntHolder currentCount = counts.get(bigram);
            if (currentCount == null)
            {
                benchCount += (counts.put(bigram, new IntHolder(1)) != null) ? 1 : 0;
            }
            else
            {
                currentCount.value++;
                benchCount++;
            }
        }

        return benchCount;
    }

    public int jcfNaive()
    {
        int benchCount = 0;

        final char[] CHARS = this.data;

        final Map<Integer, Integer> counts = new HashMap<Integer, Integer>();

        for (int i = 0; i < CHARS.length - 1; i++)
        {
            final int bigram = CHARS[i] << 16 | CHARS[i + 1];
            final Integer currentCount = counts.get(bigram);
            if (currentCount == null)
            {
                benchCount += (counts.put(bigram, 1) != null) ? 1 : 0;
            }
            else
            {
                benchCount += (counts.put(bigram, currentCount + 1) != null) ? 1 : 0;
            }
        }

        return benchCount;
    }
}