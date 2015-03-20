package com.carrotsearch.hppcrt.mutables;

/**
 * <code>double</code> holder.
 */
public class DoubleHolder implements Comparable<DoubleHolder>
{
    public double value;

    public DoubleHolder()
    {
    }

    public DoubleHolder(final double value)
    {
        this.value = value;
    }

    @Override
    public int hashCode()
    {
        final long bits = Double.doubleToLongBits(this.value);
        return (int) (bits ^ (bits >>> 32));
    }

    @Override
    public boolean equals(final Object other)
    {
        return (other instanceof DoubleHolder) &&
                Double.doubleToLongBits(this.value) ==
                Double.doubleToLongBits(((DoubleHolder) other).value);
    }

    @Override
    public int compareTo(final DoubleHolder o) {

        return Double.compare(this.value, o.value);
    }
}
