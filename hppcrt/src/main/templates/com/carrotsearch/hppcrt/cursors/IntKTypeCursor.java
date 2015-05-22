package com.carrotsearch.hppcrt.cursors;

/*! ($TemplateOptions.doNotGenerate()) !*/
/**
 * Place holder
 * @author Vincent
 *
 */
public final class IntKTypeCursor<T>
{
    public int index;

    public int key;

    public T value;

    @Override
    public String toString()
    {
        return "[cursor, index: " + this.index + ", key: " + this.key + ", value: " + this.value + "]";
    }
}
