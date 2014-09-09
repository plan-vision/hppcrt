package com.carrotsearch.hppcrt.mutables;

/**
 * <code>byte</code> holder.
 */
public class ByteHolder implements Comparable<ByteHolder>
{
    public byte value;

    public ByteHolder()
    {
    }

    public ByteHolder(final byte value)
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
        return (other instanceof ByteHolder) && this.value == ((ByteHolder) other).value;
    }

    @Override
    public int compareTo(final ByteHolder o) {

        if (this.value < o.value) {

            return -1;

        }
        else if (this.value > o.value) {

            return 1;
        }

        return 0;
    }
}
