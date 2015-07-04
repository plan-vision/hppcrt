package com.carrotsearch.hppcrt;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test case for bounded proportional sizing strategy.
 */
public class BoundedProportionalArraySizingStrategyTest
{
    private BoundedProportionalArraySizingStrategy resizer;

    @Test(expected = BufferAllocationException.class)
    public void testBeyondIntRange()
    {
        this.resizer = new BoundedProportionalArraySizingStrategy();
        this.resizer.grow(
                BoundedProportionalArraySizingStrategy.MAX_ARRAY_LENGTH,
                BoundedProportionalArraySizingStrategy.MAX_ARRAY_LENGTH, 1);
    }

    @Test
    public void testExactIntRange()
    {
        this.resizer = new BoundedProportionalArraySizingStrategy();
        int size = BoundedProportionalArraySizingStrategy.MAX_ARRAY_LENGTH - 2;
        size = this.resizer.grow(size, size, 1);
        Assert.assertEquals(BoundedProportionalArraySizingStrategy.MAX_ARRAY_LENGTH, size);
        try {
            size = this.resizer.grow(size, size, 1);
            throw new RuntimeException("Unexpected.");
        } catch (final BufferAllocationException e) {
            // Expected.
        }
    }
}
