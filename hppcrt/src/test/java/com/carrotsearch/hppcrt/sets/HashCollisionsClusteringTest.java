package com.carrotsearch.hppcrt.sets;

import static org.junit.Assert.*;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.carrotsearch.hppcrt.cursors.IntCursor;
import com.carrotsearch.hppcrt.cursors.ObjectCursor;
import com.carrotsearch.hppcrt.maps.IntIntOpenHashMap;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Timeout;

public class HashCollisionsClusteringTest extends RandomizedTest
{
    private static long BATCH_TIMEOUT_INTEGERS = TimeUnit.SECONDS.toMillis(3); //3s
    private static long BATCH_TIMEOUT_OBJECTS = TimeUnit.SECONDS.toMillis(5);  //5s

    /** @see "http://issues.carrot2.org/browse/HPPC-80" */
    @Test
    public void testHashSetClusteringOnRehash()
    {
        final IntOpenHashSet source = new IntOpenHashSet(0, 0.9d);

        for (int i = 1250000; i-- != 0;) {
            source.add(i);
        }

        final IntOpenHashSet target = new IntOpenHashSet(0, 0.9d);
        int i = 0;
        final long start = System.currentTimeMillis();
        final long deadline = start + HashCollisionsClusteringTest.BATCH_TIMEOUT_INTEGERS;
        for (final IntCursor c : source) {

            target.add(c.value);

            if ((i++ % 5000) == 0) {

                System.out.println(String.format(Locale.ROOT,
                        "Keys: %7d, %5d ms.",
                        i,
                        System.currentTimeMillis() - start));

                if (System.currentTimeMillis() >= deadline) {
                    Assert.fail("Takes too long, something is wrong. Added " + i + " keys out of " + source.size());
                }
            }
        }
    }

    @Test
    public void testHashSetClusteringOnRehashObject()
    {
        final ObjectOpenHashSet<Integer> source = new ObjectOpenHashSet<Integer>(0, 0.9d);

        for (int i = 1250000; i-- != 0;) {
            source.add(i);
        }

        final ObjectOpenHashSet<Integer> target = new ObjectOpenHashSet<Integer>(0, 0.9d);
        int i = 0;
        final long start = System.currentTimeMillis();
        final long deadline = start + HashCollisionsClusteringTest.BATCH_TIMEOUT_OBJECTS;

        for (final ObjectCursor<Integer> c : source) {

            target.add(c.value);

            if ((i++ % 5000) == 0) {

                System.out.println(String.format(Locale.ROOT,
                        "Keys: %7d, %5d ms.",
                        i,
                        System.currentTimeMillis() - start));

                if (System.currentTimeMillis() >= deadline) {
                    Assert.fail("Takes too long, something is wrong. Added " + i + " keys out of " + source.size());
                }
            }
        }
    }

    /** @see "http://issues.carrot2.org/browse/HPPC-80" */
    @Test
    @Timeout(millis = 20000)
    public void testHashMapClusteringOnRehash()
    {
        final IntIntOpenHashMap a = new IntIntOpenHashMap();

        for (int i = 10000000; i-- != 0;) {
            a.put(i, 0);
        }
        final IntIntOpenHashMap b2 = new IntIntOpenHashMap();
        b2.putAll(a);
    }

    /** */
    @Test
    public void testHashSetClusteringAtFront()
    {
        final int keys = 500000;
        final IntOpenHashSet target = new IntOpenHashSet(keys, 0.9);

        final int expandAtCount = target.capacity() - 1;
        final int fillUntil = expandAtCount - 100000;

        final IntOpenHashSet source = new IntOpenHashSet(keys, 0.9);
        int unique = 0;
        while (source.size() < expandAtCount - 1) {
            source.add(unique++);
        }
        System.out.println("Source filled up.");

        while (target.size() < fillUntil) {
            target.add(unique++);
        }
        System.out.println("Target filled up.");

        Assert.assertEquals(source.keys.length, target.keys.length);

        final long start = System.currentTimeMillis();
        final long deadline = start + HashCollisionsClusteringTest.BATCH_TIMEOUT_INTEGERS;
        int i = 0;
        for (final IntCursor c : source) {

            target.add(c.value);

            if ((i++ % 5000) == 0) {

                if (source.keys.length == target.keys.length) {

                    printDistributionResult(i, start, System.currentTimeMillis(), target);
                }
                if (System.currentTimeMillis() >= deadline) {
                    Assert.fail("Takes too long, something is wrong. Added " + i + " keys out of " + source.size());
                }
            }
        }
    }

    /** */
    @Test
    public void testHashSetClusteringAtFrontObject()
    {
        final int keys = 500000;
        final ObjectOpenHashSet<Integer> target = new ObjectOpenHashSet<Integer>(keys, 0.9);

        final int expandAtCount = target.capacity() - 1;
        final int fillUntil = expandAtCount - 100000;

        final ObjectOpenHashSet<Integer> source = new ObjectOpenHashSet<Integer>(keys, 0.9);
        int unique = 0;
        while (source.size() < expandAtCount - 1) {
            source.add(unique++);
        }
        System.out.println("Source filled up.");

        while (target.size() < fillUntil) {
            target.add(unique++);
        }
        System.out.println("Target filled up.");

        Assert.assertEquals(((Object[]) source.keys).length, ((Object[]) target.keys).length);

        final long start = System.currentTimeMillis();
        final long deadline = start + HashCollisionsClusteringTest.BATCH_TIMEOUT_OBJECTS;
        int i = 0;
        for (final ObjectCursor<Integer> c : source) {

            target.add(c.value);

            if ((i++ % 5000) == 0) {

                if (((Object[]) source.keys).length == ((Object[]) target.keys).length) {

                    printDistributionResult(i, start, System.currentTimeMillis(), target);
                }
                if (System.currentTimeMillis() >= deadline) {
                    Assert.fail("Takes too long, something is wrong. Added " + i + " keys out of " + source.size());
                }
            }
        }
    }

    /** */
    @Test
    public void testHashSetClusteringAtFrontSmallBatches()
    {
        final int keys = 100000;
        final int expected = keys * 5;
        final IntOpenHashSet target = new IntOpenHashSet(expected, 0.9);

        final long deadline = System.currentTimeMillis() + HashCollisionsClusteringTest.BATCH_TIMEOUT_INTEGERS;
        final IntOpenHashSet source = new IntOpenHashSet(expected, 0.9);
        int unique = 0;

        //Add up to 200 batches
        for (int i = 0; i < 200; i++) {
            source.clear();

            //Each batch is built of keys all different from each batch, and from each other.
            while (source.size() < keys) {
                source.add(unique++);
            }

            //push the first firstSubsetOfKeys keys of source batch to target, measure it, show the distribution evolution while
            //the batches keep piling up.
            final long start = System.currentTimeMillis();
            int firstSubsetOfKeys = 5000;

            for (final IntCursor c : source) {
                target.add(c.value);
                if (firstSubsetOfKeys-- == 0)
                    break;
            }

            printDistributionResult(i, start, System.currentTimeMillis(), target);

            if (System.currentTimeMillis() > deadline) {
                Assert.fail("Takes too long, something is wrong. Added " + i + " batches.");
            }
        }
    }

    /** */
    @Test
    public void testHashSetClusteringAtFrontSmallBatchesObject()
    {
        final int keys = 100000;
        final int expected = keys * 5;
        final ObjectOpenHashSet<Integer> target = new ObjectOpenHashSet<Integer>(expected, 0.9);

        final long deadline = System.currentTimeMillis() + HashCollisionsClusteringTest.BATCH_TIMEOUT_OBJECTS;

        final ObjectOpenHashSet<Integer> source = new ObjectOpenHashSet<Integer>(expected, 0.9);
        int unique = 0;

        //Add up to 200 batches
        for (int i = 0; i < 200; i++) {

            source.clear();

            //Each batch is built of keys all different from each batch, and from each other.
            while (source.size() < keys) {

                source.add(unique++);
            }

            final long start = System.currentTimeMillis();
            int firstSubsetOfKeys = 5000;

            //push the first firstSubsetOfKeys keys of source batch to target, measure it, show the distribution evolution while
            //the batches keep piling up.
            for (final ObjectCursor<Integer> c : source) {

                target.add(c.value);

                if (firstSubsetOfKeys-- == 0)
                    break;
            }

            printDistributionResult(i, start, System.currentTimeMillis(), target);

            if (System.currentTimeMillis() > deadline) {
                Assert.fail("Takes too long, something is wrong. Added " + i + " batches.");
            }
        }
    }

    protected String visualizeDistribution(final IntOpenHashSet target, final int lineLength) {
        final int bucketSize = Math.max(lineLength, target.keys.length) / lineLength;
        final int[] counts = new int[lineLength];

        for (int x = 0; x < target.keys.length; x++) {

            if (target.keys[x] != 0) {
                counts[Math.min(counts.length - 1, x / bucketSize)]++;
            }
        }

        int max = counts[0];
        for (int x = 0; x < counts.length; x++) {
            max = Math.max(max, counts[x]);
        }

        final StringBuilder b = new StringBuilder();
        final char[] chars = ".0123456789".toCharArray();
        for (int x = 0; x < counts.length; x++) {
            b.append(chars[(counts[x] * 10 / max)]);
        }
        return b.toString();
    }

    protected <U> String visualizeDistribution(final ObjectOpenHashSet<U> target, final int lineLength) {
        final int bucketSize = Math.max(lineLength, target.keys.length) / lineLength;

        final int[] counts = new int[lineLength];

        for (int x = 0; x < target.keys.length; x++) {
            if (target.keys[x] != null) {
                counts[Math.min(counts.length - 1, x / bucketSize)]++;
            }
        }

        int max = counts[0];
        for (int x = 0; x < counts.length; x++) {
            max = Math.max(max, counts[x]);
        }

        final StringBuilder b = new StringBuilder();
        final char[] chars = ".0123456789".toCharArray();
        for (int x = 0; x < counts.length; x++) {
            b.append(chars[(counts[x] * 10 / max)]);
        }
        return b.toString();
    }

    protected <U> void printDistributionResult(final int keyNb, final long startMillis, final long endMillis, final ObjectOpenHashSet<U> target) {

        System.out.println(String.format(Locale.ROOT,
                "Keys: %7d, %5d ms.: %s",
                keyNb,
                endMillis - startMillis,
                visualizeDistribution(target, 100)));
    }

    protected void printDistributionResult(final int keyNb, final long startMillis, final long endMillis, final IntOpenHashSet target) {

        System.out.println(String.format(Locale.ROOT,
                "Keys: %7d, %5d ms.: %s",
                keyNb,
                endMillis - startMillis,
                visualizeDistribution(target, 100)));
    }
}
