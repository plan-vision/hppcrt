package com.carrotsearch.hppcrt;

import java.util.Arrays;

/**
 * Utility class gathering array or KTypeIndexedContainer handling algorithms for <code>KType</code>s.
 * This is a kind of complement for {@link java.util.Arrays}.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public final class KTypeArrays
{
    final private static int BLANK_ARRAY_SIZE_IN_BIT_SHIFT = 10;

    /**
     * Batch blanking array size
     */
    final private static int BLANK_ARRAY_SIZE = 1 << KTypeArrays.BLANK_ARRAY_SIZE_IN_BIT_SHIFT;

    /**
     * Batch blanking array with KType nulls
     */
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    final private static Object[] BLANKING_OBJECT_ARRAY = new Object[KTypeArrays.BLANK_ARRAY_SIZE];

    /*! #else
    final private static KType[] BLANKING_OBJECT_ARRAY = Intrinsics.<KType[]>newKTypeArray(KTypeArrays.BLANK_ARRAY_SIZE);
    #end  !*/

    private KTypeArrays() {

        //nothing
    }

    /**
     * Rotate utility :
     * Transforms the range [[slice_1:  from; mid - 1][slice_2: mid, to - 1]] of table, into
     * [[slice_2][slice_1]]in place, i.e swap the two slices while keeping their own internal order.
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
     * [[slice_2][slice_1]] in place, i.e swap the two slices while keeping their own internal order.
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

    /**
     * Method to blank any KType[] array elements to its default value
     * from [startIndex; endIndex[, equivalent to {@link Arrays}.fill(objectArray, startIndex, endIndex, 0 or null)
     */
    public static/*! #if ($TemplateOptions.KTypeGeneric) !*/<KType> /*! #end !*/void blankArray(final KType[] objectArray, final int startIndex, final int endIndex) {

        assert startIndex <= endIndex;

        final int size = endIndex - startIndex;
        final int nbChunks = size >> KTypeArrays.BLANK_ARRAY_SIZE_IN_BIT_SHIFT;
        //compute remainder
        final int rem = size & (KTypeArrays.BLANK_ARRAY_SIZE - 1);

        for (int i = 0; i < nbChunks; i++) {

            System.arraycopy(KTypeArrays.BLANKING_OBJECT_ARRAY, 0,
                    objectArray, startIndex + (i << KTypeArrays.BLANK_ARRAY_SIZE_IN_BIT_SHIFT),
                    KTypeArrays.BLANK_ARRAY_SIZE);
        } //end for

        //fill the reminder
        if (rem > 0) {
            Arrays.fill(objectArray, startIndex + (nbChunks << KTypeArrays.BLANK_ARRAY_SIZE_IN_BIT_SHIFT),
                    startIndex + (nbChunks << KTypeArrays.BLANK_ARRAY_SIZE_IN_BIT_SHIFT) + rem, Intrinsics.defaultKTypeValue());
        }
    }

}
