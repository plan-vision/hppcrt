package com.carrotsearch.hppcrt.mutables;

/**
 * <code>short</code> holder.
 */
public class ShortHolder implements Comparable<ShortHolder>
{
    public short value;

    public ShortHolder()
    {
    }

    public ShortHolder(final short value)
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
        return (other instanceof ShortHolder) && this.value == ((ShortHolder) other).value;
    }

    @Override
    public int compareTo(final ShortHolder o) {

        if (this.value < o.value) {

            return -1;
        }
        else if (this.value > o.value) {

            return 1;
        }

        return 0;
    }
}
