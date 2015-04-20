package com.carrotsearch.hppcrt;

/**
 * <code>int</code> holder.
 */
public class IntHolder implements Comparable<IntHolder>
{
    public int value;

    public IntHolder()
    {
    }

    public IntHolder(final int value)
    {
        this.value = value;
    }

    @Override
    public int hashCode()
    {
        return this.value;
    }

    @Override
    public boolean equals(final Object other)
    {
        return (other instanceof IntHolder) && this.value == ((IntHolder) other).value;
    }

    @Override
    public int compareTo(final IntHolder o) {

        if (this.value < o.value) {

            return -1;
        }
        else if (this.value > o.value) {

            return 1;
        }

        return 0;
    }
}
