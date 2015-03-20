package com.carrotsearch.hppcrt.mutables;

/**
 * <code>long</code> holder.
 */
public class LongHolder implements Comparable<LongHolder>
{
    public long value;

    public LongHolder()
    {
    }

    public LongHolder(final long value)
    {
        this.value = value;
    }

    @Override
    public int hashCode()
    {
        return (int) (this.value ^ (this.value >>> 32));
    }

    @Override
    public boolean equals(final Object other)
    {
        return (other instanceof LongHolder) && this.value == ((LongHolder) other).value;
    }

    @Override
    public int compareTo(final LongHolder o) {

        if (this.value < o.value) {

            return -1;
        }
        else if (this.value > o.value) {

            return 1;
        }

        return 0;
    }
}
