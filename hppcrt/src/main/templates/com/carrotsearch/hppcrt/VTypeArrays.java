package com.carrotsearch.hppcrt;

import java.util.Arrays;

import com.carrotsearch.hppcrt.KTypeArrays;
import com.carrotsearch.hppcrt.KTypeIndexedContainer;

/**
 * Utility class gathering array or KTypeIndexedContainer handling algorithms for <code>KType</code>s.
 * This is indeed a placeholder for template compilation,
 * and will indeed be replaced by a (VType) instantiation
 * of KTypeArrays.
 */
/*! ${TemplateOptions.doNotGenerate()} !*/
/*! ${TemplateOptions.generatedAnnotation} !*/
public final class VTypeArrays
{
    private VTypeArrays() {
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
    public static <T> void rotate(final T[] table, final int from, final int mid, final int to) {

        KTypeArrays.<T> rotate(table, from, mid, to);
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
    public static <T> void rotate(final KTypeIndexedContainer<T> table, final int from, final int mid, final int to) {

        KTypeArrays.<T> rotate(table, from, mid, to);
    }

    /**
     * Reverse the elements positions of the specified range of array table :
     * @param table
     * @param from the start range to consider
     * @param to the array end range, exclusive
     */
    public static <T> void reverse(final T[] table, final int from, final int to) {

        KTypeArrays.<T> reverse(table, from, to);
    }

    /**
     * Reverse the elements positions of the specified range of KTypeIndexedContainer table :
     * @param table
     * @param from the start range to consider
     * @param to the array end range, exclusive
     */
    public static <T> void reverse(final KTypeIndexedContainer<T> table, final int from, final int to) {

        KTypeArrays.<T> reverse(table, from, to);

    }

    /**
     * Method to blank any KType[] array elements to its default value
     * from [startIndex; endIndex[, equivalent to {@link Arrays}.fill(objectArray, startIndex, endIndex, 0 or null)
     */
    public static <T> void blankArray(final T[] objectArray, final int startIndex, final int endIndex) {

        KTypeArrays.<T> blankArray(objectArray, startIndex, endIndex);

    }
}
