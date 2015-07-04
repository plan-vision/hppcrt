package com.carrotsearch.hppcrt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 */
public class AbstractIteratorTest
{
    public static class RangeIterator extends AbstractIterator<Integer>
    {
        int start;
        int count;

        public RangeIterator(final int start, final int count)
        {
            this.start = start;
            this.count = count;
        }

        @Override
        protected Integer fetch()
        {
            if (this.count == 0)
            {
                return done();
            }

            this.count--;
            return this.start++;
        }
    }

    @Test
    public void testEmpty()
    {
        final RangeIterator i = new RangeIterator(1, 0);
        Assert.assertFalse(i.hasNext());
        Assert.assertFalse(i.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void testEmptyExceptionOnNext()
    {
        final RangeIterator i = new RangeIterator(1, 0);
        i.next();
    }

    @Test
    public void testNonEmpty()
    {
        final RangeIterator i = new RangeIterator(1, 1);
        Assert.assertTrue(i.hasNext());
        Assert.assertTrue(i.hasNext());
        i.next();
        Assert.assertFalse(i.hasNext());
        Assert.assertFalse(i.hasNext());
        try
        {
            i.next();
            Assert.fail();
        } catch (final NoSuchElementException e)
        {
            // expected.
        }
    }

    @Test
    public void testValuesAllRight()
    {
        Assert.assertEquals(Arrays.asList(1), AbstractIteratorTest.addAll(new RangeIterator(1, 1)));
        Assert.assertEquals(Arrays.asList(1, 2), AbstractIteratorTest.addAll(new RangeIterator(1, 2)));
        Assert.assertEquals(Arrays.asList(1, 2, 3), AbstractIteratorTest.addAll(new RangeIterator(1, 3)));
    }

    private static <T> List<T> addAll(final Iterator<T> i)
    {
        final List<T> t = new ArrayList<T>();
        while (i.hasNext()) {
            t.add(i.next());
        }
        return t;
    }
}
