package com.carrotsearch.hppcrt;

import org.junit.Assert;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;

import static com.carrotsearch.hppcrt.HashContainers.*;

public class HashContainersTest extends RandomizedTest
{
    /* */
    @Test
    public void testCapacityCalculations() {

        Assert.assertEquals(HashContainers.MIN_HASH_ARRAY_LENGTH, HashContainers.minBufferSize(0, 0.5f));
        Assert.assertEquals(HashContainers.MIN_HASH_ARRAY_LENGTH, HashContainers.minBufferSize(1, 0.5f));

        Assert.assertEquals(0x20, HashContainers.minBufferSize(0x10, 0.5f));
        Assert.assertEquals(0x40, HashContainers.minBufferSize(0x10, 0.49f));

        final int maxCapacity = HashContainers.maxElements(HashContainers.MAX_LOAD_FACTOR);
        Assert.assertEquals(0x40000000, HashContainers.minBufferSize(maxCapacity, HashContainers.MAX_LOAD_FACTOR));

        // This would fill the array fully, validating the invariant, but should
        // be possible without reallocating the buffer.
        HashContainers.minBufferSize(maxCapacity + 1, HashContainers.MAX_LOAD_FACTOR);
        Assert.assertEquals(maxCapacity + 1, HashContainers.expandAtCount(HashContainers.MAX_HASH_ARRAY_LENGTH, HashContainers.MAX_LOAD_FACTOR));

        try {
            // This should be impossible because it'd create a negative-sized array.
            HashContainers.minBufferSize(maxCapacity + 2, HashContainers.MAX_LOAD_FACTOR);
            Assert.fail();
        }
        catch (final BufferAllocationException e) {
            // Expected.
        }
    }

    /* */
    @Test
    public void testLoadFactorOne() {
        Assert.assertEquals(0x100, HashContainers.minBufferSize(0x80, 1d));
        Assert.assertEquals(0x7f, HashContainers.expandAtCount(0x80, 1d));
        Assert.assertEquals(0xff, HashContainers.expandAtCount(0x100, 1d));
    }
}
