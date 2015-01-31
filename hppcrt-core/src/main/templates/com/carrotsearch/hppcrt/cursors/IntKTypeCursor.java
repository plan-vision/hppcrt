package com.carrotsearch.hppcrt.cursors;

/**
 * A cursor over entries of an associative container (int keys and KType values).
 * This is indeed a placeholder for template compilation,
 * and will indeed be replaced by a (int, VType) instantiation
 * of KTypeVTypeCursor
 */
//${TemplateOptions.doNotGenerateKType("all")}
/*! ${TemplateOptions.generatedAnnotation} !*/
public final class IntKTypeCursor<T>
{
    /**
     * The current key and value's index in the container this cursor belongs to. The meaning of
     * this index is defined by the container (usually it will be an index in the underlying
     * storage buffer).
     */
    public int index;

    /**
     * The current key.
     */
    public int key;

    /**
     * The current value.
     */
    public T value;

    @Override
    public String toString()
    {
        return "[cursor, index: " + this.index + ", key: " + this.key + ", value: " + this.value + "]";
    }
}
