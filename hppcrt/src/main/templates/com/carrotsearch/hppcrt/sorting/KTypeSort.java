package com.carrotsearch.hppcrt.sorting;

import java.util.Comparator;

import com.carrotsearch.hppcrt.Intrinsics;
import com.carrotsearch.hppcrt.strategies.*;
import com.carrotsearch.hppcrt.KTypeIndexedContainer;

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
/**
 * Utility class gathering sorting algorithms for <code>KType</code>s containers.
 * It is a replacement for {@link java.util.Arrays} sorting routines, with
 * memory and speed guarantees documented in Javadoc.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public final class KTypeSort
{
    /**
     * Minimum window length to apply insertion sort.
     */
    private static final int MIN_LENGTH_FOR_INSERTION_SORT = 24;

    private static final int DIST_SIZE_DUALQSORT = 13;

    private KTypeSort()
    {
        // Utility class, nothing to do
    }

    /**
     * In-place sort by dual-pivot quicksort an array of naturally comparable <code>KType</code>s from [beginIndex, endIndex[
     * <p>
     * <b>
     * This routine uses Dual-pivot Quicksort, from [Vladimir Yaroslavskiy 2009] #if ($TemplateOptions.KTypeGeneric), so is NOT stable. #end
     * </b>
     * </p>
     #if ($TemplateOptions.KTypeGeneric)
     * @throws ClassCastException if the array contains elements that are not mutually Comparable.
     #end
     */
    public static/*! #if ($TemplateOptions.KTypeGeneric) !*/<KType> /*! #end !*/void quicksort(final KType[] table, final int beginIndex, final int endIndex)
    {
        if (beginIndex < 0 || beginIndex >= table.length) {

            throw new IndexOutOfBoundsException("Index beginIndex " + beginIndex + " out of bounds [" + 0 + ", " + table.length + "[.");
        }

        if (beginIndex >= endIndex) {

            throw new IllegalArgumentException("Index beginIndex " + beginIndex + " is >= endIndex " + endIndex);
        }

        if (endIndex > table.length) {

            throw new IndexOutOfBoundsException("Index endIndex " + endIndex + " out of bounds [" + 0 + ", " + table.length + "].");
        }

        if (endIndex - beginIndex > 1)
        {
            KTypeSort.dualPivotQuicksort(table, beginIndex, endIndex - 1);
        }
    }

    /**
     * In-place sort by dual-pivot quicksort a entire array of of naturally Comparable <code>KType</code>s
     * <p>
     * <b>
     * This routine uses Dual-pivot Quicksort, from [Vladimir Yaroslavskiy 2009] #if ($TemplateOptions.KTypeGeneric), so is NOT stable. #end
     * </b>
     * </p>
     #if ($TemplateOptions.KTypeGeneric)
     * @throws ClassCastException if the array contains elements that are not mutually Comparable.
     #end
     */
    public static/*! #if ($TemplateOptions.KTypeGeneric) !*/<KType> /*! #end !*/void quicksort(final KType[] table)
    {
        KTypeSort.quicksort(table, 0, table.length);
    }

    /**
     * In-place sort by dual-pivot quicksort a {@link KTypeIndexedContainer} of naturally Comparable <code>KType</code>s from [beginIndex, endIndex[
     * <p>
     * <b>
     * This routine uses Dual-pivot Quicksort, from [Vladimir Yaroslavskiy 2009] #if ($TemplateOptions.KTypeGeneric), so is NOT stable. #end
     * </b>
     * </p>
     #if ($TemplateOptions.KTypeGeneric)
     * @throws ClassCastException if the KTypeIndexedContainer contains elements that are not mutually Comparable.
     #end
     */
    public static/*! #if ($TemplateOptions.KTypeGeneric) !*/<KType> /*! #end !*/void quicksort(final KTypeIndexedContainer<KType> table, final int beginIndex,
            final int endIndex)
    {
        final int size = table.size();

        if (beginIndex < 0 || beginIndex >= size) {

            throw new IndexOutOfBoundsException("Index beginIndex " + beginIndex + " out of bounds [" + 0 + ", " + size + "[.");
        }

        if (beginIndex >= endIndex) {

            throw new IllegalArgumentException("Index beginIndex " + beginIndex + " is >= endIndex " + endIndex);
        }

        if (endIndex > size) {

            throw new IndexOutOfBoundsException("Index endIndex " + endIndex + " out of bounds [" + 0 + ", " + size + "].");
        }

        if (endIndex - beginIndex > 1)
        {
            KTypeSort.dualPivotQuicksort(table, beginIndex, endIndex - 1);
        }
    }

    /**
     * In-place sort by dual-pivot quicksort a entire {@link KTypeIndexedContainer} of naturally Comparable <code>KType</code>s
     * <p>
     * <b>
     * This routine uses Dual-pivot Quicksort, from [Vladimir Yaroslavskiy 2009] #if ($TemplateOptions.KTypeGeneric), so is NOT stable. #end
     * </b>
     * </p>
      #if ($TemplateOptions.KTypeGeneric)
     * @throws ClassCastException if the KTypeIndexedContainer contains elements that are not mutually Comparable.
     #end
     */
    public static/*! #if ($TemplateOptions.KTypeGeneric) !*/<KType> /*! #end !*/void quicksort(final KTypeIndexedContainer<KType> table)
    {
        KTypeSort.quicksort(table, 0, table.size());
    }

    ////////////////////////////
    /**
     * In-place sort by  dual-pivot quicksort an array of <code>KType</code>s from [beginIndex, endIndex[
     * using a #if ($TemplateOptions.KTypeGeneric) <code>Comparator</code> #else <code>KTypeComparator</code> #end
     * <p><b>
     * This routine uses Dual-pivot Quicksort, from [Vladimir Yaroslavskiy 2009] #if ($TemplateOptions.KTypeGeneric), so is NOT stable. #end
     * </b></p>
     */
    public static/*! #if ($TemplateOptions.KTypeGeneric) !*/<KType> /*! #end !*/void quicksort(
            final KType[] table, final int beginIndex, final int endIndex,
            /*! #if ($TemplateOptions.KTypeGeneric) !*/
            final Comparator<? super KType>
            /*! #else
            KTypeComparator<? super KType>
             #end !*/
            comp)
    {
        if (beginIndex < 0 || beginIndex >= table.length) {

            throw new IndexOutOfBoundsException("Index beginIndex " + beginIndex + " out of bounds [" + 0 + ", " + table.length + "[.");
        }

        if (beginIndex >= endIndex) {

            throw new IllegalArgumentException("Index beginIndex " + beginIndex + " is >= endIndex " + endIndex);
        }

        if (endIndex > table.length) {

            throw new IndexOutOfBoundsException("Index endIndex " + endIndex + " out of bounds [" + 0 + ", " + table.length + "].");
        }

        if (endIndex - beginIndex > 1)
        {
            KTypeSort.dualPivotQuicksort(table, beginIndex, endIndex - 1, comp);
        }
    }

    /**
     * In-place sort by dual-pivot quicksort an entire array of <code>KType</code>s
     * using a #if ($TemplateOptions.KTypeGeneric) <code>Comparator</code> #else <code>KTypeComparator</code> #end
     * <p><b>
     * This routine uses Dual-pivot Quicksort, from [Vladimir Yaroslavskiy 2009] #if ($TemplateOptions.KTypeGeneric), so is NOT stable. #end
     * </b></p>
     */
    public static/*! #if ($TemplateOptions.KTypeGeneric) !*/<KType> /*! #end !*/void quicksort(final KType[] table,
            /*! #if ($TemplateOptions.KTypeGeneric) !*/
            final Comparator<? super KType>
            /*! #else
                                                                            KTypeComparator<? super KType>
                                                                    #end !*/
            comp)
    {
        KTypeSort.quicksort(table, 0, table.length, comp);
    }

    /**
     * In-place sort by dual-pivot quicksort a generic {@link KTypeIndexedContainer} from [beginIndex, endIndex[
     * using a #if ($TemplateOptions.KTypeGeneric) <code>Comparator</code> #else <code>KTypeComparator</code> #end
     * <p><b>
     * This routine uses Dual-pivot Quicksort, from [Vladimir Yaroslavskiy 2009] #if ($TemplateOptions.KTypeGeneric), so is NOT stable. #end
     * </b></p>
     */
    public static/*! #if ($TemplateOptions.KTypeGeneric) !*/<KType> /*! #end !*/void quicksort(
            final KTypeIndexedContainer<KType> table, final int beginIndex, final int endIndex,
            /*! #if ($TemplateOptions.KTypeGeneric) !*/
            final Comparator<? super KType>
            /*! #else
            KTypeComparator<? super KType>
             #end !*/
            comp)
    {
        final int size = table.size();

        if (beginIndex < 0 || beginIndex >= size) {

            throw new IndexOutOfBoundsException("Index beginIndex " + beginIndex + " out of bounds [" + 0 + ", " + size + "[.");
        }

        if (beginIndex >= endIndex) {

            throw new IllegalArgumentException("Index beginIndex " + beginIndex + " is >= endIndex " + endIndex);
        }

        if (endIndex > size) {

            throw new IndexOutOfBoundsException("Index endIndex " + endIndex + " out of bounds [" + 0 + ", " + size + "].");
        }

        if (endIndex - beginIndex > 1)
        {
            KTypeSort.dualPivotQuicksort(table, beginIndex, endIndex - 1, comp);
        }
    }

    /**
     * In-place sort by dual-pivot quicksort an entire generic {@link KTypeIndexedContainer}
     * using a #if ($TemplateOptions.KTypeGeneric) <code>Comparator</code> #else <code>KTypeComparator</code> #end
     * <p><b>
     * This routine uses Dual-pivot Quicksort, from [Vladimir Yaroslavskiy 2009] #if ($TemplateOptions.KTypeGeneric), so is NOT stable. #end
     * </b></p>
     */
    public static/*! #if ($TemplateOptions.KTypeGeneric) !*/<KType> /*! #end !*/void quicksort(final KTypeIndexedContainer<KType> table,
            /*! #if ($TemplateOptions.KTypeGeneric) !*/
            final Comparator<? super KType>
    /*! #else
                                                            KTypeComparator<? super KType>
                                                            #end !*/
            comp)
    {
        KTypeSort.quicksort(table, 0, table.size(), comp);
    }

    /**
     * Insertion sort for smaller arrays, for Comparable
     * @param a
     * @param left
     * @param right inclusive
     */
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    //because of Intrinsics.xxxUnchecked inlining for objects.
    /*! #end !*/
    private static/*! #if ($TemplateOptions.KTypeGeneric) !*/<KType> /*! #end !*/void insertionsort(final KType[] a, final int left, final int right)
    {
        KType x;
        // insertion sort on tiny array
        for (int i = left + 1; i <= right; i++)
        {
            for (int j = i; j > left && Intrinsics.<KType> isCompInfUnchecked(a[j], a[j - 1]); j--)
            {
                x = a[j - 1];
                a[j - 1] = a[j];
                a[j] = x;
            }
        }
    }

    /**
     * Private recursive sort method, [left, right] inclusive for Comparable objects
     * or natural ordering for primitives.
     * @param a
     * @param left
     * @param right inclusive
     */
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    //because of Intrinsics.xxxUnchecked inlining for objects.
    /*! #end !*/
    private static/*! #if ($TemplateOptions.KTypeGeneric) !*/<KType> /*! #end !*/void dualPivotQuicksort(final KType[] a, final int left, final int right)
    {
        final int len = right - left;

        KType x;

        //insertion sort
        //to prevent too-big recursion, swap to insertion sort below a certain size
        if (len < KTypeSort.MIN_LENGTH_FOR_INSERTION_SORT)
        {
            // insertion sort on tiny array
            KTypeSort.insertionsort(a, left, right);
            return;
        }

        // median indexes
        final int sixth = len / 6;
        final int m1 = left + sixth;
        final int m2 = m1 + sixth;
        final int m3 = m2 + sixth;
        final int m4 = m3 + sixth;
        final int m5 = m4 + sixth;

        // 5-element sorting network
        if (Intrinsics.<KType> isCompSupUnchecked(a[m1], a[m2]) /* a[m1] > a[m2]*/)
        {
            x = a[m1];
            a[m1] = a[m2];
            a[m2] = x;
        }
        if (Intrinsics.<KType> isCompSupUnchecked(a[m4], a[m5]) /* a[m4] > a[m5]*/)
        {
            x = a[m4];
            a[m4] = a[m5];
            a[m5] = x;
        }
        if (Intrinsics.<KType> isCompSupUnchecked(a[m1], a[m3]) /*a[m1] > a[m3]*/)
        {
            x = a[m1];
            a[m1] = a[m3];
            a[m3] = x;
        }
        if (Intrinsics.<KType> isCompSupUnchecked(a[m2], a[m3]) /* a[m2] > a[m3]*/)
        {
            x = a[m2];
            a[m2] = a[m3];
            a[m3] = x;
        }
        if (Intrinsics.<KType> isCompSupUnchecked(a[m1], a[m4]) /* a[m1] > a[m4]*/)
        {
            x = a[m1];
            a[m1] = a[m4];
            a[m4] = x;
        }
        if (Intrinsics.<KType> isCompSupUnchecked(a[m3], a[m4]) /*a[m3] > a[m4]*/)
        {
            x = a[m3];
            a[m3] = a[m4];
            a[m4] = x;
        }
        if (Intrinsics.<KType> isCompSupUnchecked(a[m2], a[m5]) /* a[m2] > a[m5]*/)
        {
            x = a[m2];
            a[m2] = a[m5];
            a[m5] = x;
        }
        if (Intrinsics.<KType> isCompSupUnchecked(a[m2], a[m3]) /*a[m2] > a[m3]*/)
        {
            x = a[m2];
            a[m2] = a[m3];
            a[m3] = x;
        }
        if (Intrinsics.<KType> isCompSupUnchecked(a[m4], a[m5]) /* a[m4] > a[m5]*/)
        {
            x = a[m4];
            a[m4] = a[m5];
            a[m5] = x;
        }

        // pivots: [ < pivot1 | pivot1 <= && <= pivot2 | > pivot2 ]
        final KType pivot1 = a[m2];
        final KType pivot2 = a[m4];

        final boolean diffPivots = !Intrinsics.<KType> isCompEqualUnchecked(pivot1, pivot2);

        a[m2] = a[left];
        a[m4] = a[right];
        // center part pointers
        int less = left + 1;
        int great = right - 1;
        // sorting
        if (diffPivots)
        {
            for (int k = less; k <= great; k++)
            {
                x = a[k];

                if (Intrinsics.<KType> isCompInfUnchecked(x, pivot1)/* x < pivot1 */)
                {
                    a[k] = a[less];
                    a[less++] = x;
                }
                else if (Intrinsics.<KType> isCompSupUnchecked(x, pivot2) /* x > pivot2 */)
                {
                    while (Intrinsics.<KType> isCompSupUnchecked(a[great], pivot2) /* a[great] > pivot2 */&& k < great)
                    {
                        great--;
                    }
                    a[k] = a[great];
                    a[great--] = x;
                    x = a[k];

                    if (Intrinsics.<KType> isCompInfUnchecked(x, pivot1) /*x < pivot1*/)
                    {
                        a[k] = a[less];
                        a[less++] = x;
                    }
                }
            }
        }
        else
        {
            for (int k = less; k <= great; k++)
            {
                x = a[k];
                if (Intrinsics.<KType> isCompEqualUnchecked(x, pivot1) /*x == pivot1*/)
                {
                    continue;
                }
                if (Intrinsics.<KType> isCompInfUnchecked(x, pivot1) /* x < pivot1 */)
                {
                    a[k] = a[less];
                    a[less++] = x;
                }
                else
                {
                    while (Intrinsics.<KType> isCompSupUnchecked(a[great], pivot2) /* a[great] > pivot2*/&& k < great)
                    {
                        great--;
                    }
                    a[k] = a[great];
                    a[great--] = x;
                    x = a[k];

                    if (Intrinsics.<KType> isCompInfUnchecked(x, pivot1) /*x < pivot1*/)
                    {
                        a[k] = a[less];
                        a[less++] = x;
                    }
                }
            }
        }
        // swap
        a[left] = a[less - 1];
        a[less - 1] = pivot1;
        a[right] = a[great + 1];
        a[great + 1] = pivot2;
        // left and right parts
        KTypeSort.dualPivotQuicksort(a, left, less - 2);
        KTypeSort.dualPivotQuicksort(a, great + 2, right);

        // equal elements
        if (great - less > len - KTypeSort.DIST_SIZE_DUALQSORT && diffPivots)
        {
            for (int k = less; k <= great; k++)
            {
                x = a[k];
                if (Intrinsics.<KType> isCompEqualUnchecked(x, pivot1) /*x == pivot1*/)
                {
                    a[k] = a[less];
                    a[less++] = x;
                }
                else if (Intrinsics.<KType> isCompEqualUnchecked(x, pivot2) /*x == pivot2*/)
                {
                    a[k] = a[great];
                    a[great--] = x;
                    x = a[k];

                    if (Intrinsics.<KType> isCompEqualUnchecked(x, pivot1) /*x == pivot1*/)
                    {
                        a[k] = a[less];
                        a[less++] = x;
                    }
                }
            }
        }
        // center part
        if (diffPivots)
        {
            KTypeSort.dualPivotQuicksort(a, less, great);
        }
    }

    /**
     * Insertion sort for smaller arrays, for KTypeIndexedContainer
     * @param a
     * @param left
     * @param right inclusive
     */
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    //because of Intrinsics.xxxUnchecked inlining for objects.
    /*! #end !*/
    private static/*! #if ($TemplateOptions.KTypeGeneric) !*/<KType> /*! #end !*/void insertionsort(final KTypeIndexedContainer<KType> a, final int left,
            final int right)
    {
        KType x;

        for (int i = left + 1; i <= right; i++)
        {
            for (int j = i; j > left && Intrinsics.<KType> isCompInfUnchecked(a.get(j), a.get(j - 1)); j--)
            {
                x = a.get(j - 1);
                a.set(j - 1, a.get(j));
                a.set(j, x);
            }
        }
    }

    /**
     * Private recursive sort method for KTypeIndexedContainer, [left, right] inclusive for Comparable objects
     * or natural ordering for primitives.
     * @param a
     * @param left
     * @param right inclusive
     */
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    //because of Intrinsics.xxxUnchecked inlining for objects.
    /*! #end !*/
    private static/*! #if ($TemplateOptions.KTypeGeneric) !*/<KType> /*! #end !*/void dualPivotQuicksort(final KTypeIndexedContainer<KType> a, final int left,
            final int right)
    {
        final int len = right - left;

        KType x;

        //insertion sort
        //to prevent too-big recursion, swap to insertion sort below a certain size
        if (len < KTypeSort.MIN_LENGTH_FOR_INSERTION_SORT)
        {
            // insertion sort on tiny array
            KTypeSort.insertionsort(a, left, right);
            return;
        }

        // median indexes
        final int sixth = len / 6;
        final int m1 = left + sixth;
        final int m2 = m1 + sixth;
        final int m3 = m2 + sixth;
        final int m4 = m3 + sixth;
        final int m5 = m4 + sixth;

        // 5-element sorting network
        if (Intrinsics.<KType> isCompSupUnchecked(a.get(m1), a.get(m2)) /* a[m1] > a[m2]*/)
        {
            x = a.get(m1);
            a.set(m1, a.get(m2));
            a.set(m2, x);
        }
        if (Intrinsics.<KType> isCompSupUnchecked(a.get(m4), a.get(m5)) /* a[m4] > a[m5]*/)
        {
            x = a.get(m4);
            a.set(m4, a.get(m5));
            a.set(m5, x);

        }
        if (Intrinsics.<KType> isCompSupUnchecked(a.get(m1), a.get(m3)) /*a[m1] > a[m3]*/)
        {
            x = a.get(m1);
            a.set(m1, a.get(m3));
            a.set(m3, x);
        }
        if (Intrinsics.<KType> isCompSupUnchecked(a.get(m2), a.get(m3)) /* a[m2] > a[m3]*/)
        {
            x = a.get(m2);
            a.set(m2, a.get(m3));
            a.set(m3, x);

        }
        if (Intrinsics.<KType> isCompSupUnchecked(a.get(m1), a.get(m4)) /* a[m1] > a[m4]*/)
        {
            x = a.get(m1);
            a.set(m1, a.get(m4));
            a.set(m4, x);

        }
        if (Intrinsics.<KType> isCompSupUnchecked(a.get(m3), a.get(m4)) /*a[m3] > a[m4]*/)
        {
            x = a.get(m3);
            a.set(m3, a.get(m4));
            a.set(m4, x);

        }
        if (Intrinsics.<KType> isCompSupUnchecked(a.get(m2), a.get(m5)) /* a[m2] > a[m5]*/)
        {
            x = a.get(m2);
            a.set(m2, a.get(m5));
            a.set(m5, x);
        }
        if (Intrinsics.<KType> isCompSupUnchecked(a.get(m2), a.get(m3)) /*a[m2] > a[m3]*/)
        {
            x = a.get(m2);
            a.set(m2, a.get(m3));
            a.set(m3, x);
        }
        if (Intrinsics.<KType> isCompSupUnchecked(a.get(m4), a.get(m5)) /* a[m4] > a[m5]*/)
        {
            x = a.get(m4);
            a.set(m4, a.get(m5));
            a.set(m5, x);
        }

        // pivots: [ < pivot1 | pivot1 <= && <= pivot2 | > pivot2 ]
        final KType pivot1 = a.get(m2);
        final KType pivot2 = a.get(m4);

        final boolean diffPivots = !Intrinsics.<KType> isCompEqualUnchecked(pivot1, pivot2);

        a.set(m2, a.get(left));
        a.set(m4, a.get(right));
        // center part pointers
        int less = left + 1;
        int great = right - 1;
        // sorting
        if (diffPivots)
        {
            for (int k = less; k <= great; k++)
            {
                x = a.get(k);

                if (Intrinsics.<KType> isCompInfUnchecked(x, pivot1)/* x < pivot1 */)
                {
                    a.set(k, a.get(less));
                    a.set(less, x);
                    less++;
                }
                else if (Intrinsics.<KType> isCompSupUnchecked(x, pivot2) /* x > pivot2 */)
                {
                    while (Intrinsics.<KType> isCompSupUnchecked(a.get(great), pivot2) /* a[great] > pivot2 */&& k < great)
                    {
                        great--;
                    }
                    a.set(k, a.get(great));
                    a.set(great, x);
                    great--;
                    x = a.get(k);

                    if (Intrinsics.<KType> isCompInfUnchecked(x, pivot1) /*x < pivot1*/)
                    {
                        a.set(k, a.get(less));
                        a.set(less, x);
                        less++;
                    }
                }
            }
        }
        else
        {
            for (int k = less; k <= great; k++)
            {
                x = a.get(k);

                if (Intrinsics.<KType> isCompEqualUnchecked(x, pivot1) /*x == pivot1*/)
                {
                    continue;
                }
                if (Intrinsics.<KType> isCompInfUnchecked(x, pivot1) /* x < pivot1 */)
                {
                    a.set(k, a.get(less));
                    a.set(less, x);
                    less++;
                }
                else
                {
                    while (Intrinsics.<KType> isCompSupUnchecked(a.get(great), pivot2) /* a[great] > pivot2*/&& k < great)
                    {
                        great--;
                    }
                    a.set(k, a.get(great));
                    a.set(great, x);
                    great--;
                    x = a.get(k);

                    if (Intrinsics.<KType> isCompInfUnchecked(x, pivot1) /*x < pivot1*/)
                    {
                        a.set(k, a.get(less));
                        a.set(less, x);
                        less++;
                    }
                }
            }
        }
        // swap
        a.set(left, a.get(less - 1));
        a.set(less - 1, pivot1);
        a.set(right, a.get(great + 1));
        a.set(great + 1, pivot2);
        // left and right parts
        KTypeSort.dualPivotQuicksort(a, left, less - 2);
        KTypeSort.dualPivotQuicksort(a, great + 2, right);

        // equal elements
        if (great - less > len - KTypeSort.DIST_SIZE_DUALQSORT && diffPivots)
        {
            for (int k = less; k <= great; k++)
            {
                x = a.get(k);
                if (Intrinsics.<KType> isCompEqualUnchecked(x, pivot1) /*x == pivot1*/)
                {
                    a.set(k, a.get(less));
                    a.set(less, x);
                    less++;
                }
                else if (Intrinsics.<KType> isCompEqualUnchecked(x, pivot2) /*x == pivot2*/)
                {
                    a.set(k, a.get(great));
                    a.set(great, x);
                    great--;
                    x = a.get(k);

                    if (Intrinsics.<KType> isCompEqualUnchecked(x, pivot1) /*x == pivot1*/)
                    {
                        a.set(k, a.get(less));
                        a.set(less, x);
                        less++;
                    }
                }
            }
        }
        // center part
        if (diffPivots)
        {
            KTypeSort.dualPivotQuicksort(a, less, great);
        }
    }

    /**
     * Insertion sort for smaller arrays, with Comparator
     * @param a
     * @param left
     * @param right inclusive
     */
    private static/*! #if ($TemplateOptions.KTypeGeneric) !*/<KType> /*! #end !*/void insertionsort(
            final KType[] a,
            final int left, final int right,
            /*! #if ($TemplateOptions.KTypeGeneric) !*/
            final Comparator<? super KType>
            /*! #else
            KTypeComparator<? super KType>
             #end !*/
            comp)
    {
        KType x;

        for (int i = left + 1; i <= right; i++)
        {
            for (int j = i; j > left && comp.compare(a[j], a[j - 1]) < 0; j--)
            {
                x = a[j - 1];
                a[j - 1] = a[j];
                a[j] = x;
            }
        }
    }

    /**
     * Private recursive sort method, [left, right] inclusive using KTypeComparator
     * for comparison.
     * @param a
     * @param left
     * @param right inclusive
     */
    private static/*! #if ($TemplateOptions.KTypeGeneric) !*/<KType> /*! #end !*/void dualPivotQuicksort(
            final KType[] a,
            final int left, final int right,
            /*! #if ($TemplateOptions.KTypeGeneric) !*/
            final Comparator<? super KType>
            /*! #else
            KTypeComparator<? super KType>
             #end !*/
            comp)
    {
        final int len = right - left;

        KType x;

        //insertion sort
        //to prevent too-big recursion, swap to insertion sort below a certain size
        if (len < KTypeSort.MIN_LENGTH_FOR_INSERTION_SORT)
        {
            // insertion sort on tiny array
            KTypeSort.insertionsort(a, left, right, comp);
            return;
        }

        // median indexes
        final int sixth = len / 6;
        final int m1 = left + sixth;
        final int m2 = m1 + sixth;
        final int m3 = m2 + sixth;
        final int m4 = m3 + sixth;
        final int m5 = m4 + sixth;

        // 5-element sorting network
        if (comp.compare(a[m1], a[m2]) > 0 /* a[m1] > a[m2]*/)
        {
            x = a[m1];
            a[m1] = a[m2];
            a[m2] = x;
        }
        if (comp.compare(a[m4], a[m5]) > 0 /* a[m4] > a[m5]*/)
        {
            x = a[m4];
            a[m4] = a[m5];
            a[m5] = x;
        }
        if (comp.compare(a[m1], a[m3]) > 0 /*a[m1] > a[m3]*/)
        {
            x = a[m1];
            a[m1] = a[m3];
            a[m3] = x;
        }
        if (comp.compare(a[m2], a[m3]) > 0 /* a[m2] > a[m3]*/)
        {
            x = a[m2];
            a[m2] = a[m3];
            a[m3] = x;
        }
        if (comp.compare(a[m1], a[m4]) > 0 /* a[m1] > a[m4]*/)
        {
            x = a[m1];
            a[m1] = a[m4];
            a[m4] = x;
        }
        if (comp.compare(a[m3], a[m4]) > 0 /*a[m3] > a[m4]*/)
        {
            x = a[m3];
            a[m3] = a[m4];
            a[m4] = x;
        }
        if (comp.compare(a[m2], a[m5]) > 0 /* a[m2] > a[m5]*/)
        {
            x = a[m2];
            a[m2] = a[m5];
            a[m5] = x;
        }
        if (comp.compare(a[m2], a[m3]) > 0 /*a[m2] > a[m3]*/)
        {
            x = a[m2];
            a[m2] = a[m3];
            a[m3] = x;
        }
        if (comp.compare(a[m4], a[m5]) > 0 /* a[m4] > a[m5]*/)
        {
            x = a[m4];
            a[m4] = a[m5];
            a[m5] = x;
        }

        // pivots: [ < pivot1 | pivot1 <= && <= pivot2 | > pivot2 ]
        final KType pivot1 = a[m2];
        final KType pivot2 = a[m4];

        final boolean diffPivots = comp.compare(pivot1, pivot2) != 0;

        a[m2] = a[left];
        a[m4] = a[right];
        // center part pointers
        int less = left + 1;
        int great = right - 1;
        // sorting
        if (diffPivots)
        {
            for (int k = less; k <= great; k++)
            {
                x = a[k];

                if (comp.compare(x, pivot1) < 0/* x < pivot1 */)
                {
                    a[k] = a[less];
                    a[less++] = x;
                }
                else if (comp.compare(x, pivot2) > 0 /* x > pivot2 */)
                {
                    while (comp.compare(a[great], pivot2) > 0 /* a[great] > pivot2 */&& k < great)
                    {
                        great--;
                    }
                    a[k] = a[great];
                    a[great--] = x;
                    x = a[k];

                    if (comp.compare(x, pivot1) < 0 /*x < pivot1*/)
                    {
                        a[k] = a[less];
                        a[less++] = x;
                    }
                }
            }
        }
        else
        {
            for (int k = less; k <= great; k++)
            {
                x = a[k];
                if (comp.compare(x, pivot1) == 0 /*x == pivot1*/)
                {
                    continue;
                }
                if (comp.compare(x, pivot1) < 0 /* x < pivot1 */)
                {
                    a[k] = a[less];
                    a[less++] = x;
                }
                else
                {
                    while (comp.compare(a[great], pivot2) > 0 /* a[great] > pivot2*/&& k < great)
                    {
                        great--;
                    }
                    a[k] = a[great];
                    a[great--] = x;
                    x = a[k];

                    if (comp.compare(x, pivot1) < 0 /*x < pivot1*/)
                    {
                        a[k] = a[less];
                        a[less++] = x;
                    }
                }
            }
        }
        // swap
        a[left] = a[less - 1];
        a[less - 1] = pivot1;
        a[right] = a[great + 1];
        a[great + 1] = pivot2;
        // left and right parts
        KTypeSort.dualPivotQuicksort(a, left, less - 2, comp);
        KTypeSort.dualPivotQuicksort(a, great + 2, right, comp);

        // equal elements
        if (great - less > len - KTypeSort.DIST_SIZE_DUALQSORT && diffPivots)
        {
            for (int k = less; k <= great; k++)
            {
                x = a[k];
                if (comp.compare(x, pivot1) == 0 /*x == pivot1*/)
                {
                    a[k] = a[less];
                    a[less++] = x;
                }
                else if (comp.compare(x, pivot2) == 0 /*x == pivot2*/)
                {
                    a[k] = a[great];
                    a[great--] = x;
                    x = a[k];

                    if (comp.compare(x, pivot1) == 0 /*x == pivot1*/)
                    {
                        a[k] = a[less];
                        a[less++] = x;
                    }
                }
            }
        }
        // center part
        if (diffPivots)
        {
            KTypeSort.dualPivotQuicksort(a, less, great, comp);
        }
    }

    /**
     * Insertion sort for smaller KTypeIndexedContainer, Comparator version
     * @param a
     * @param left
     * @param right inclusive
     */
    private static/*! #if ($TemplateOptions.KTypeGeneric) !*/<KType> /*! #end !*/void insertionsort(
            final KTypeIndexedContainer<KType> a,
            final int left, final int right,
            /*! #if ($TemplateOptions.KTypeGeneric) !*/
            final Comparator<? super KType>
            /*! #else
            KTypeComparator<? super KType>
             #end !*/
            comp)
    {
        KType x;

        for (int i = left + 1; i <= right; i++)
        {
            for (int j = i; j > left && comp.compare(a.get(j), a.get(j - 1)) < 0; j--)
            {
                x = a.get(j - 1);
                a.set(j - 1, a.get(j));
                a.set(j, x);
            }
        }
    }

    /**
     * Private recursive sort method for KTypeIndexedContainer a , [left, right] inclusive using KTypeComparator
     * for comparison.
     * @param a
     * @param left
     * @param right inclusive
     */
    private static/*! #if ($TemplateOptions.KTypeGeneric) !*/<KType> /*! #end !*/void dualPivotQuicksort(
            final KTypeIndexedContainer<KType> a,
            final int left, final int right,
            /*! #if ($TemplateOptions.KTypeGeneric) !*/
            final Comparator<? super KType>
            /*! #else
            KTypeComparator<? super KType>
             #end !*/
            comp)
    {
        final int len = right - left;

        KType x;

        //insertion sort
        //to prevent too-big recursion, swap to insertion sort below a certain size
        if (len < KTypeSort.MIN_LENGTH_FOR_INSERTION_SORT)
        {
            // insertion sort on tiny array
            KTypeSort.insertionsort(a, left, right, comp);
            return;
        }

        // median indexes
        final int sixth = len / 6;
        final int m1 = left + sixth;
        final int m2 = m1 + sixth;
        final int m3 = m2 + sixth;
        final int m4 = m3 + sixth;
        final int m5 = m4 + sixth;

        // 5-element sorting network
        if (comp.compare(a.get(m1), a.get(m2)) > 0 /* a[m1] > a[m2]*/)
        {
            x = a.get(m1);
            a.set(m1, a.get(m2));
            a.set(m2, x);
        }
        if (comp.compare(a.get(m4), a.get(m5)) > 0 /* a[m4] > a[m5]*/)
        {
            x = a.get(m4);
            a.set(m4, a.get(m5));
            a.set(m5, x);

        }
        if (comp.compare(a.get(m1), a.get(m3)) > 0 /*a[m1] > a[m3]*/)
        {
            x = a.get(m1);
            a.set(m1, a.get(m3));
            a.set(m3, x);
        }
        if (comp.compare(a.get(m2), a.get(m3)) > 0 /* a[m2] > a[m3]*/)
        {
            x = a.get(m2);
            a.set(m2, a.get(m3));
            a.set(m3, x);

        }
        if (comp.compare(a.get(m1), a.get(m4)) > 0 /* a[m1] > a[m4]*/)
        {
            x = a.get(m1);
            a.set(m1, a.get(m4));
            a.set(m4, x);

        }
        if (comp.compare(a.get(m3), a.get(m4)) > 0 /*a[m3] > a[m4]*/)
        {
            x = a.get(m3);
            a.set(m3, a.get(m4));
            a.set(m4, x);

        }
        if (comp.compare(a.get(m2), a.get(m5)) > 0 /* a[m2] > a[m5]*/)
        {
            x = a.get(m2);
            a.set(m2, a.get(m5));
            a.set(m5, x);
        }
        if (comp.compare(a.get(m2), a.get(m3)) > 0 /*a[m2] > a[m3]*/)
        {
            x = a.get(m2);
            a.set(m2, a.get(m3));
            a.set(m3, x);
        }
        if (comp.compare(a.get(m4), a.get(m5)) > 0 /* a[m4] > a[m5]*/)
        {
            x = a.get(m4);
            a.set(m4, a.get(m5));
            a.set(m5, x);
        }

        // pivots: [ < pivot1 | pivot1 <= && <= pivot2 | > pivot2 ]
        final KType pivot1 = a.get(m2);
        final KType pivot2 = a.get(m4);

        final boolean diffPivots = (comp.compare(pivot1, pivot2) != 0);

        a.set(m2, a.get(left));
        a.set(m4, a.get(right));
        // center part pointers
        int less = left + 1;
        int great = right - 1;
        // sorting
        if (diffPivots)
        {
            for (int k = less; k <= great; k++)
            {
                x = a.get(k);

                if (comp.compare(x, pivot1) < 0/* x < pivot1 */)
                {
                    a.set(k, a.get(less));
                    a.set(less, x);
                    less++;
                }
                else if (comp.compare(x, pivot2) > 0/* x > pivot2 */)
                {
                    while (comp.compare(a.get(great), pivot2) > 0/* a[great] > pivot2 */&& k < great)
                    {
                        great--;
                    }
                    a.set(k, a.get(great));
                    a.set(great, x);
                    great--;
                    x = a.get(k);

                    if (comp.compare(x, pivot1) < 0/*x < pivot1*/)
                    {
                        a.set(k, a.get(less));
                        a.set(less, x);
                        less++;
                    }
                }
            }
        }
        else
        {
            for (int k = less; k <= great; k++)
            {
                x = a.get(k);

                if (comp.compare(x, pivot1) == 0/*x == pivot1*/)
                {
                    continue;
                }
                if (comp.compare(x, pivot1) < 0/* x < pivot1 */)
                {
                    a.set(k, a.get(less));
                    a.set(less, x);
                    less++;
                }
                else
                {
                    while (comp.compare(a.get(great), pivot2) > 0 /* a[great] > pivot2*/&& k < great)
                    {
                        great--;
                    }
                    a.set(k, a.get(great));
                    a.set(great, x);
                    great--;
                    x = a.get(k);

                    if (comp.compare(x, pivot1) < 0 /*x < pivot1*/)
                    {
                        a.set(k, a.get(less));
                        a.set(less, x);
                        less++;
                    }
                }
            }
        }
        // swap
        a.set(left, a.get(less - 1));
        a.set(less - 1, pivot1);
        a.set(right, a.get(great + 1));
        a.set(great + 1, pivot2);
        // left and right parts
        KTypeSort.dualPivotQuicksort(a, left, less - 2, comp);
        KTypeSort.dualPivotQuicksort(a, great + 2, right, comp);

        // equal elements
        if (great - less > len - KTypeSort.DIST_SIZE_DUALQSORT && diffPivots)
        {
            for (int k = less; k <= great; k++)
            {
                x = a.get(k);
                if (comp.compare(x, pivot1) == 0/*x == pivot1*/)
                {
                    a.set(k, a.get(less));
                    a.set(less, x);
                    less++;
                }
                else if (comp.compare(x, pivot2) == 0 /*x == pivot2*/)
                {
                    a.set(k, a.get(great));
                    a.set(great, x);
                    great--;
                    x = a.get(k);

                    if (comp.compare(x, pivot1) == 0 /*x == pivot1*/)
                    {
                        a.set(k, a.get(less));
                        a.set(less, x);
                        less++;
                    }
                }
            }
        }
        // center part
        if (diffPivots)
        {
            KTypeSort.dualPivotQuicksort(a, less, great, comp);
        }
    }
}