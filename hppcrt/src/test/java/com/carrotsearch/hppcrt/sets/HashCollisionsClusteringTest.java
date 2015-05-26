package com.carrotsearch.hppcrt.sets;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.carrotsearch.hppcrt.IntBufferVisualizer;
import com.carrotsearch.hppcrt.ObjectBufferVisualizer;
import com.carrotsearch.hppcrt.ShortBufferVisualizer;
import com.carrotsearch.hppcrt.cursors.IntCursor;
import com.carrotsearch.hppcrt.cursors.ObjectCursor;
import com.carrotsearch.hppcrt.cursors.ShortCursor;
import com.carrotsearch.hppcrt.maps.IntIntHashMap;
import com.carrotsearch.hppcrt.maps.ShortIntHashMap;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Timeout;

public class HashCollisionsClusteringTest extends RandomizedTest
{
    private static long BATCH_TIMEOUT_INTEGERS = TimeUnit.SECONDS.toMillis(5); //5s
    private static long BATCH_TIMEOUT_OBJECTS = TimeUnit.SECONDS.toMillis(15);  //15s

    @Before
    public void purgeMemory() {
        System.gc();
        System.gc();
        System.gc();
    }

    /** @see "http://issues.carrot2.org/browse/HPPC-80" */
    @Test
    public void testHashSetClusteringOnRehash()
    {
        final IntHashSet source = new IntHashSet(0, 0.9d);

        for (int i = 1250000; i-- != 0;) {
            source.add(i);
        }

        final IntHashSet target = new IntHashSet(0, 0.9d);
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
        final ObjectHashSet<Integer> source = new ObjectHashSet<Integer>(0, 0.9d);

        for (int i = 1250000; i-- != 0;) {
            source.add(i);
        }

        final ObjectHashSet<Integer> target = new ObjectHashSet<Integer>(0, 0.9d);
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
    @Timeout(millis = 10000)
    public void testHashMapClusteringOnRehash()
    {
        final IntIntHashMap a = new IntIntHashMap();

        for (int i = 10000000; i-- != 0;) {
            a.put(i, 0);
        }
        final IntIntHashMap b2 = new IntIntHashMap();
        b2.putAll(a);
    }

    @Test
    @Timeout(millis = 2000)
    public void testHashMapClusteringOnRehashShort()
    {
        final ShortIntHashMap a = new ShortIntHashMap();

        for (short i = 32000; i-- != 0;) {
            a.put(i, 0);
        }
        final ShortIntHashMap b2 = new ShortIntHashMap();
        b2.putAll(a);
    }

    /** */
    @Test
    public void testHashSetClusteringAtFront()
    {
        final int keys = 500000;
        final IntHashSet target = new IntHashSet(keys, 0.9);

        final int expandAtCount = target.capacity() - 1;
        final int fillUntil = expandAtCount - 100000;

        final IntHashSet source = new IntHashSet(keys, 0.9);
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
        final ObjectHashSet<Integer> target = new ObjectHashSet<Integer>(keys, 0.9);

        final int expandAtCount = target.capacity() - 1;
        final int fillUntil = expandAtCount - 100000;

        final ObjectHashSet<Integer> source = new ObjectHashSet<Integer>(keys, 0.9);
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
        final long deadline = start + HashCollisionsClusteringTest.BATCH_TIMEOUT_OBJECTS;
        int i = 0;
        for (final ObjectCursor<Integer> c : source) {

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
    public void testHashSetClusteringAtFrontSmallBatches()
    {
        final int keys = 100000;
        final int expected = keys * 5;
        final IntHashSet target = new IntHashSet(expected, 0.9);

        final long deadline = System.currentTimeMillis() + HashCollisionsClusteringTest.BATCH_TIMEOUT_INTEGERS;
        final IntHashSet source = new IntHashSet(expected, 0.9);
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
                if (firstSubsetOfKeys-- == 0) {
                    break;
                }
            }

            printDistributionResult(i, start, System.currentTimeMillis(), target);

            if (System.currentTimeMillis() > deadline) {
                Assert.fail("Takes too long, something is wrong. Added " + i + " batches.");
            }
        }
    }

    /** */
    @Test
    public void testHashSetClusteringAtFrontSmallBatchesShort()
    {
        final int keys = 1000;
        final int expected = keys * 5;
        final ShortHashSet target = new ShortHashSet(expected, 0.9);

        final long deadline = System.currentTimeMillis() + HashCollisionsClusteringTest.BATCH_TIMEOUT_INTEGERS;
        final ShortHashSet source = new ShortHashSet(expected, 0.9);
        short unique = 0;

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
            int firstSubsetOfKeys = 200;

            for (final ShortCursor c : source) {
                target.add(c.value);
                if (firstSubsetOfKeys-- == 0) {
                    break;
                }
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
        final ObjectHashSet<Integer> target = new ObjectHashSet<Integer>(expected, 0.9);

        final long deadline = System.currentTimeMillis() + HashCollisionsClusteringTest.BATCH_TIMEOUT_OBJECTS;

        final ObjectHashSet<Integer> source = new ObjectHashSet<Integer>(expected, 0.9);
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

                if (firstSubsetOfKeys-- == 0) {
                    break;
                }
            }

            printDistributionResult(i, start, System.currentTimeMillis(), target);

            if (System.currentTimeMillis() > deadline) {
                Assert.fail("Takes too long, something is wrong. Added " + i + " batches.");
            }
        }
    }

    protected <U> void printDistributionResult(final int keyNb, final long startMillis, final long endMillis, final ObjectHashSet<U> target) {

        System.out.println(String.format(Locale.ROOT,
                "Keys: %7d, %5d ms.: %s",
                keyNb,
                endMillis - startMillis,
                ObjectBufferVisualizer.visualizeKeyDistribution(target.keys, 100)));
    }

    protected void printDistributionResult(final int keyNb, final long startMillis, final long endMillis, final IntHashSet target) {

        System.out.println(String.format(Locale.ROOT,
                "Keys: %7d, %5d ms.: %s",
                keyNb,
                endMillis - startMillis,
                IntBufferVisualizer.visualizeKeyDistribution(target.keys, 100)));
    }

    protected void printDistributionResult(final int keyNb, final long startMillis, final long endMillis, final ShortHashSet target) {

        System.out.println(String.format(Locale.ROOT,
                "Keys: %7d, %5d ms.: %s",
                keyNb,
                endMillis - startMillis,
                ShortBufferVisualizer.visualizeKeyDistribution(target.keys, 100)));
    }
}
