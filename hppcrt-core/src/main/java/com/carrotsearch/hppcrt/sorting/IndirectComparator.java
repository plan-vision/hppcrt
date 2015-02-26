package com.carrotsearch.hppcrt.sorting;

import java.util.Comparator;
import com.carrotsearch.hppcrt.strategies.*;

/**
 * Compare objects at two given indices and return the result of their comparison
 * consistent with {@link Comparator}'s contract.
 * <p>
 * <b>Beware of the <code>return (int - int) idiom</code>, it is usually broken if
 * arbitrary numbers can appear on input. Use regular comparison operations - they are
 * very fast anyway.
 */
public interface IndirectComparator extends IntComparator
{
    /**
     * See class documentation.
     */
    @Override
    public int compare(int indexA, int indexB);

    /**
     * A natural-order comparator for integers.
     */
    public static class AscendingIntComparator implements IndirectComparator
    {
        private final int[] array;

        public AscendingIntComparator(final int[] array)
        {
            this.array = array;
        }

        @Override
        public int compare(final int indexA, final int indexB)
        {
            final int a = this.array[indexA];
            final int b = this.array[indexB];

            if (a < b)
                return -1;
            if (a > b)
                return 1;
            return 0;
        }
    }

    /**
     * A reverse-order comparator for integers.
     */
    public static class DescendingIntComparator extends AscendingIntComparator
    {
        public DescendingIntComparator(final int[] array)
        {
            super(array);
        }

        @Override
        public final int compare(final int indexA, final int indexB)
        {
            return -super.compare(indexA, indexB);
        }
    }

    /**
     * A natural-order comparator for integers.
     */
    public static class AscendingShortComparator implements IndirectComparator
    {
        private final short[] array;

        public AscendingShortComparator(final short[] array)
        {
            this.array = array;
        }

        @Override
        public int compare(final int indexA, final int indexB)
        {
            final short a = this.array[indexA];
            final short b = this.array[indexB];

            if (a < b)
                return -1;
            if (a > b)
                return 1;
            return 0;
        }
    }

    /**
     * A reverse-order comparator for shorts.
     */
    public static class DescendingShortComparator extends AscendingShortComparator
    {
        public DescendingShortComparator(final short[] array)
        {
            super(array);
        }

        @Override
        public final int compare(final int indexA, final int indexB)
        {
            return -super.compare(indexA, indexB);
        }
    }

    /**
     * A natural-order comparator for doubles.
     */
    public static class AscendingDoubleComparator implements IndirectComparator
    {
        private final double[] array;

        public AscendingDoubleComparator(final double[] array)
        {
            this.array = array;
        }

        @Override
        public int compare(final int indexA, final int indexB)
        {
            final double a = this.array[indexA];
            final double b = this.array[indexB];

            if (a < b)
                return -1;
            if (a > b)
                return 1;
            return 0;
        }
    }

    /**
     * A reverse-order comparator for doubles.
     */
    public static class DescendingDoubleComparator extends AscendingDoubleComparator
    {
        public DescendingDoubleComparator(final double[] array)
        {
            super(array);
        }

        @Override
        public final int compare(final int indexA, final int indexB)
        {
            return -super.compare(indexA, indexB);
        }
    }

    /**
     * A natural-order comparator for floats.
     */
    public static class AscendingFloatComparator implements IndirectComparator
    {
        private final float[] array;

        public AscendingFloatComparator(final float[] array)
        {
            this.array = array;
        }

        @Override
        public int compare(final int indexA, final int indexB)
        {
            final float a = this.array[indexA];
            final float b = this.array[indexB];

            if (a < b)
                return -1;
            if (a > b)
                return 1;
            return 0;
        }
    }

    /**
     * A reverse-order comparator for floats.
     */
    public static class DescendingFloatComparator extends AscendingFloatComparator
    {
        public DescendingFloatComparator(final float[] array)
        {
            super(array);
        }

        @Override
        public final int compare(final int indexA, final int indexB)
        {
            return -super.compare(indexA, indexB);
        }
    }

    /**
     * A delegating comparator for object types.
     */
    public final static class DelegatingComparator<T> implements IndirectComparator
    {
        private final T[] array;
        private final Comparator<? super T> delegate;

        public DelegatingComparator(final T[] array, final Comparator<? super T> delegate)
        {
            this.array = array;
            this.delegate = delegate;
        }

        @Override
        public final int compare(final int indexA, final int indexB)
        {
            return this.delegate.compare(this.array[indexA], this.array[indexB]);
        }

        @Override
        public String toString()
        {
            return this.getClass().getSimpleName() + " -> " + this.delegate;
        }
    }
}
