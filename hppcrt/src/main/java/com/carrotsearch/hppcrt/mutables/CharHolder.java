package com.carrotsearch.hppcrt.mutables;

/**
 * <code>char</code> holder.
 */
public class CharHolder implements Comparable<CharHolder>
{
    public char value;

    public CharHolder()
    {
    }

    public CharHolder(final char value)
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
        return (other instanceof CharHolder) && this.value == ((CharHolder) other).value;
    }

    @Override
    public int compareTo(final CharHolder o) {

        if (this.value < o.value) {

            return -1;
        }
        else if (this.value > o.value) {

            return 1;
        }

        return 0;
    }
}
