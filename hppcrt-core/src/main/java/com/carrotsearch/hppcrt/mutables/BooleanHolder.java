package com.carrotsearch.hppcrt.mutables;

/**
 * <code>boolean</code> holder.
 */
public class BooleanHolder implements Comparable<BooleanHolder>
{
    public boolean value;

    public BooleanHolder()
    {
    }

    public BooleanHolder(final boolean value)
    {
        this.value = value;
    }

    @Override
    public int hashCode()
    {
        return this.value ? 1231 : 1237;
    }

    @Override
    public boolean equals(final Object other)
    {
        return (other instanceof BooleanHolder) && this.value == ((BooleanHolder) other).value;
    }

    @Override
    public int compareTo(final BooleanHolder o) {

        return (this.value == o.value) ? 0 : (this.value ? 1 : -1);
    }
}
