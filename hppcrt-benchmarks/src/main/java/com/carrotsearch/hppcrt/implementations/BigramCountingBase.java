package com.carrotsearch.hppcrt.implementations;

import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.carrotsearch.hppcrt.maps.IntIntHashMap;

public class BigramCountingBase
{
    /* Prepare some test data */
    public char[] data;

    public void prepareData() throws IOException, URISyntaxException
    {
        final URI resTXT = ClassLoader.getSystemResource("books-polish.txt").toURI();

        this.data = new String(Files.readAllBytes(Paths.get(resTXT)), StandardCharsets.UTF_8).toCharArray();
    }

    public int hppc()
    {
        // [[[start:bigram-counting]]]
        // Some character data
        final char[] CHARS = this.data;

        // We'll use a int -> int map for counting. A bigram can be encoded
        // as an int by shifting one of the bigram's characters by 16 bits
        // and then ORing the other character to form a 32-bit int.
        final IntIntHashMap map = new IntIntHashMap();

        int count = 0;

        for (int i = 0; i < CHARS.length - 1; i++)
        {
            final int bigram = CHARS[i] << 16 | CHARS[i + 1];
            count += map.putOrAdd(bigram, 1, 1);
        }
        // [[[end:bigram-counting]]]

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
        final Map<Integer, AtomicInteger> counts = new HashMap<Integer, AtomicInteger>();
        for (int i = 0; i < CHARS.length - 1; i++)
        {
            final int bigram = CHARS[i] << 16 | CHARS[i + 1];
            final AtomicInteger currentCount = counts.get(bigram);
            if (currentCount == null)
            {
                benchCount += (counts.put(bigram, new AtomicInteger(1)) != null) ? 1 : 0;
            }
            else
            {
                currentCount.incrementAndGet();
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

    /**
     * Teest loading of TXT resource.
     * @param args
     * @throws IOException
     * @throws URISyntaxException
     */
    public static void main(final String[] args) throws IOException, URISyntaxException {

        final BigramCountingBase testBase = new BigramCountingBase();

        testBase.prepareData();

        final char[] dataTXT = testBase.data;

        System.out.println(dataTXT);
    }
}