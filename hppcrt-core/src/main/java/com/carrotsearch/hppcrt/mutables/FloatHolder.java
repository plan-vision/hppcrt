package com.carrotsearch.hppcrt.mutables;

/**
 * <code>float</code> holder.
 */
public class FloatHolder implements Comparable<FloatHolder>
{
    public float value;

    public FloatHolder()
    {
    }

    public FloatHolder(final float value)
    {
        this.value = value;
    }

    @Override
    public int hashCode()
    {
        return Float.floatToIntBits(this.value);
    }

    @Override
    public boolean equals(final Object other)
    {
        return (other instanceof FloatHolder) &&
                Float.floatToIntBits(this.value) ==
                Float.floatToIntBits(((FloatHolder) other).value);
    }

    @Override
    public int compareTo(final FloatHolder o) {

        return Float.compare(this.value, o.value);
    }
}
