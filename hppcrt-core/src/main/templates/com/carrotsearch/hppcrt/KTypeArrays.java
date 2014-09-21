package com.carrotsearch.hppcrt;

import java.util.Comparator;

import com.carrotsearch.hppcrt.Internals;

/**
 * Utility class gathering array or KTypeIndexedContainer handling algorithms for <code>KType</code>s.
 * This is a kind of complement for {@link java.util.Arrays}.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public final class KTypeArrays
{
    private KTypeArrays() {

        //nothing
    }

    /**
     * Rotate utility :
     * Transforms the range [[slice_1:  from; mid - 1][slice_2: mid, to - 1]] of table, into
     * [[slice_2][slice_1]]in place, i.e swap the two slices while keeping their internal order.
     * @param table
     * @param from the start range to consider
     * @param mid start index of the second slice
     * @param to the array end range, exclusive
     */
    public static/*! #if ($TemplateOptions.KTypeGeneric) !*/<KType> /*! #end !*/void rotate(final KType[] table, final int from, final int mid, final int to) {

        KTypeArrays.reverse(table, from, mid);
        KTypeArrays.reverse(table, mid, to);
        KTypeArrays.reverse(table, from, to);
    }

    /**
     * Rotate utility :
     * Transforms the range [[slice_1:  from; mid - 1][slice_2: mid, to - 1]] of KTypeIndexedContainer, into
     * [[slice_2][slice_1]] in place, i.e swap the two slices while keeping their internal order.
     * @author Thomas Baudel for the original code
     * @param table
     * @param from the start range to consider
     * @param mid start index of the second slice
     * @param to the array end range, exclusive
     */
    public static/*! #if ($TemplateOptions.KTypeGeneric) !*/<KType> /*! #end !*/void rotate(final KTypeIndexedContainer<KType> table, final int from, final int mid, final int to) {

        KTypeArrays.reverse(table, from, mid);
        KTypeArrays.reverse(table, mid, to);
        KTypeArrays.reverse(table, from, to);
    }

    /**
     * Reverse the elements positions of the specified range of array table :
     * @param table
     * @param from the start range to consider
     * @param to the array end range, exclusive
     */
    public static/*! #if ($TemplateOptions.KTypeGeneric) !*/<KType> /*! #end !*/void reverse(final KType[] table, final int from, final int to) {

        final int halfSize = (to - from) >>> 1;
        KType tmpValue;

        for (int i = 0; i < halfSize; i++)
        {
            tmpValue = table[i + from];
            table[i + from] = table[to - i - 1];
            table[to - i - 1] = tmpValue;
        }
    }

    /**
     * Reverse the elements positions of the specified range of KTypeIndexedContainer table :
     * @param table
     * @param from the start range to consider
     * @param to the array end range, exclusive
     */
    public static/*! #if ($TemplateOptions.KTypeGeneric) !*/<KType> /*! #end !*/void reverse(final KTypeIndexedContainer<KType> table, final int from, final int to) {

        final int halfSize = (to - from) >>> 1;
        KType tmpValue;

        for (int i = 0; i < halfSize; i++)
        {
            tmpValue = table.get(i + from);
            table.set(i + from, table.get(to - i - 1));
            table.set(to - i - 1, tmpValue);
        }
    }

}
