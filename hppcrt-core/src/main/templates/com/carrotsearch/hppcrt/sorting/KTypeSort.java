package com.carrotsearch.hppcrt.sorting;

import java.util.Comparator;

import com.carrotsearch.hppcrt.*;

/**
 * Utility class gathering sorting algorithms for <code>KType</code>s structures.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/

public final class KTypeSort
{
    /**
     * Minimum window length to apply insertion sort in quick sort.
     */
    private static final int MIN_LENGTH_FOR_INSERTION_SORT = 17;

    private static final int DIST_SIZE_DUALQSORT = 13;

    private KTypeSort()
    {
        // Utility class, nothing to do
    }

    /**
     * Sort by  dual-pivot quicksort an array of naturally comparable <code>KType</code>s from [beginIndex, endIndex[
     * <p>
     * <b>
     * This routine uses Dual-pivot Quicksort, from [Yaroslavskiy 2009] #if ($TemplateOptions.KTypeGeneric), so is NOT stable. #end
     * </b>
     * </p>
     * @param table
     * @param beginIndex
     * @param endIndex
     */
    public static/*! #if ($TemplateOptions.KTypeGeneric) !*/<KType extends Comparable<? super KType>> /*! #end !*/void quicksort(final KType[] table, final int beginIndex, final int endIndex)
    {
        if (endIndex - beginIndex > 1)
        {
            KTypeSort.dualPivotQuicksort(table, beginIndex, endIndex - 1);
        }
    }

    /**
     * Sort by  dual-pivot quicksort a entire array of of naturally Comparable <code>KType</code>s
     * <p>
     * <b>
     * This routine uses Dual-pivot Quicksort, from [Yaroslavskiy 2009] #if ($TemplateOptions.KTypeGeneric), so is NOT stable. #end
     * </b>
     * </p>
     * @param table
     */
    public static/*! #if ($TemplateOptions.KTypeGeneric) !*/<KType extends Comparable<? super KType>> /*! #end !*/void quicksort(final KType[] table)
    {
        KTypeSort.quicksort(table, 0, table.length);
    }

    /**
     * Sort by  dual-pivot quicksort a {@link KTypeIndexedContainer} of naturally Comparable <code>KType</code>s from [beginIndex, endIndex[
     * <p>
     * <b>
     * This routine uses Dual-pivot Quicksort, from [Yaroslavskiy 2009] #if ($TemplateOptions.KTypeGeneric), so is NOT stable. #end
     * </b>
     * </p>
     * @param table
     * @param beginIndex
     * @param endIndex
     */
    public static/*! #if ($TemplateOptions.KTypeGeneric) !*/<KType extends Comparable<? super KType>> /*! #end !*/void quicksort(final KTypeIndexedContainer<KType> table, final int beginIndex,
            final int endIndex)
    {
        if (endIndex - beginIndex > 1)
        {
            KTypeSort.dualPivotQuicksort(table, beginIndex, endIndex - 1);
        }
    }

    /**
     * Sort by  dual-pivot quicksort a entire {@link KTypeIndexedContainer} of naturally Comparable <code>KType</code>s
     * <p>
     * <b>
     * This routine uses Dual-pivot Quicksort, from [Yaroslavskiy 2009] #if ($TemplateOptions.KTypeGeneric), so is NOT stable. #end
     * </b>
     * </p>
     * @param table
     */
    public static/*! #if ($TemplateOptions.KTypeGeneric) !*/<KType extends Comparable<? super KType>> /*! #end !*/void quicksort(final KTypeIndexedContainer<KType> table)
    {
        KTypeSort.quicksort(table, 0, table.size());
    }

    ////////////////////////////
    /**
     * Sort by  dual-pivot quicksort an array of <code>KType</code>s from [beginIndex, endIndex[
     * using a #if ($TemplateOptions.KTypeGeneric) <code>Comparator</code> #else <code>KTypeComparator<? super KType></code> #end
     * <p><b>
     * This routine uses Dual-pivot Quicksort, from [Yaroslavskiy 2009] #if ($TemplateOptions.KTypeGeneric), so is NOT stable. #end
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
        if (endIndex - beginIndex > 1)
        {
            KTypeSort.dualPivotQuicksort(table, beginIndex, endIndex - 1, comp);
        }
    }

    /**
     * Sort by  dual-pivot quicksort an entire array of <code>KType</code>s
     * using a #if ($TemplateOptions.KTypeGeneric) <code>Comparator</code> #else <code>KTypeComparator<? super KType></code> #end
     * <p><b>
     * This routine uses Dual-pivot Quicksort, from [Yaroslavskiy 2009] #if ($TemplateOptions.KTypeGeneric), so is NOT stable. #end
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
     * Sort by  dual-pivot quicksort a generic {@link KTypeIndexedContainer} from [beginIndex, endIndex[
     * using a #if ($TemplateOptions.KTypeGeneric) <code>Comparator</code> #else <code>KTypeComparator<? super KType></code> #end
     * <p><b>
     * This routine uses Dual-pivot Quicksort, from [Yaroslavskiy 2009] #if ($TemplateOptions.KTypeGeneric), so is NOT stable. #end
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
        if (endIndex - beginIndex > 1)
        {
            KTypeSort.dualPivotQuicksort(table, beginIndex, endIndex - 1, comp);
        }
    }

    /**
     * Sort by  dual-pivot quicksort an entire generic {@link KTypeIndexedContainer}
     * using a #if ($TemplateOptions.KTypeGeneric) <code>Comparator</code> #else <code>KTypeComparator<? super KType></code> #end
     * <p><b>
     * This routine uses Dual-pivot Quicksort, from [Yaroslavskiy 2009] #if ($TemplateOptions.KTypeGeneric), so is NOT stable. #end
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
     * @param right
     */
    private static/*! #if ($TemplateOptions.KTypeGeneric) !*/<KType extends Comparable<? super KType>> /*! #end !*/void insertionsort(final KType[] a, final int left, final int right)
    {
        KType x;
        // insertion sort on tiny array
        for (int i = left + 1; i <= right; i++)
        {
            for (int j = i; j > left && Intrinsics.isCompInfKType(a[j], a[j - 1]); j--)
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
     * @param right
     */
    private static/*! #if ($TemplateOptions.KTypeGeneric) !*/<KType extends Comparable<? super KType>> /*! #end !*/void dualPivotQuicksort(final KType[] a, final int left, final int right)
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
        if (Intrinsics.isCompSupKType(a[m1], a[m2]) /* a[m1] > a[m2]*/)
        {
            x = a[m1];
            a[m1] = a[m2];
            a[m2] = x;
        }
        if (Intrinsics.isCompSupKType(a[m4], a[m5]) /* a[m4] > a[m5]*/)
        {
            x = a[m4];
            a[m4] = a[m5];
            a[m5] = x;
        }
        if (Intrinsics.isCompSupKType(a[m1], a[m3]) /*a[m1] > a[m3]*/)
        {
            x = a[m1];
            a[m1] = a[m3];
            a[m3] = x;
        }
        if (Intrinsics.isCompSupKType(a[m2], a[m3]) /* a[m2] > a[m3]*/)
        {
            x = a[m2];
            a[m2] = a[m3];
            a[m3] = x;
        }
        if (Intrinsics.isCompSupKType(a[m1], a[m4]) /* a[m1] > a[m4]*/)
        {
            x = a[m1];
            a[m1] = a[m4];
            a[m4] = x;
        }
        if (Intrinsics.isCompSupKType(a[m3], a[m4]) /*a[m3] > a[m4]*/)
        {
            x = a[m3];
            a[m3] = a[m4];
            a[m4] = x;
        }
        if (Intrinsics.isCompSupKType(a[m2], a[m5]) /* a[m2] > a[m5]*/)
        {
            x = a[m2];
            a[m2] = a[m5];
            a[m5] = x;
        }
        if (Intrinsics.isCompSupKType(a[m2], a[m3]) /*a[m2] > a[m3]*/)
        {
            x = a[m2];
            a[m2] = a[m3];
            a[m3] = x;
        }
        if (Intrinsics.isCompSupKType(a[m4], a[m5]) /* a[m4] > a[m5]*/)
        {
            x = a[m4];
            a[m4] = a[m5];
            a[m5] = x;
        }

        // pivots: [ < pivot1 | pivot1 <= && <= pivot2 | > pivot2 ]
        final KType pivot1 = a[m2];
        final KType pivot2 = a[m4];

        final boolean diffPivots = !Intrinsics.isCompEqualKType(pivot1, pivot2);

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

                if (Intrinsics.isCompInfKType(x, pivot1)/* x < pivot1 */)
                {
                    a[k] = a[less];
                    a[less++] = x;
                }
                else if (Intrinsics.isCompSupKType(x, pivot2) /* x > pivot2 */)
                {
                    while (Intrinsics.isCompSupKType(a[great], pivot2) /* a[great] > pivot2 */&& k < great)
                    {
                        great--;
                    }
                    a[k] = a[great];
                    a[great--] = x;
                    x = a[k];

                    if (Intrinsics.isCompInfKType(x, pivot1) /*x < pivot1*/)
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
                if (Intrinsics.isCompEqualKType(x, pivot1) /*x == pivot1*/)
                {
                    continue;
                }
                if (Intrinsics.isCompInfKType(x, pivot1) /* x < pivot1 */)
                {
                    a[k] = a[less];
                    a[less++] = x;
                }
                else
                {
                    while (Intrinsics.isCompSupKType(a[great], pivot2) /* a[great] > pivot2*/&& k < great)
                    {
                        great--;
                    }
                    a[k] = a[great];
                    a[great--] = x;
                    x = a[k];

                    if (Intrinsics.isCompInfKType(x, pivot1) /*x < pivot1*/)
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
                if (Intrinsics.isCompEqualKType(x, pivot1) /*x == pivot1*/)
                {
                    a[k] = a[less];
                    a[less++] = x;
                }
                else if (Intrinsics.isCompEqualKType(x, pivot2) /*x == pivot2*/)
                {
                    a[k] = a[great];
                    a[great--] = x;
                    x = a[k];

                    if (Intrinsics.isCompEqualKType(x, pivot1) /*x == pivot1*/)
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
     * @param right
     */
    private static/*! #if ($TemplateOptions.KTypeGeneric) !*/<KType extends Comparable<? super KType>> /*! #end !*/void insertionsort(final KTypeIndexedContainer<KType> a, final int left,
            final int right)
    {
        KType x;

        for (int i = left + 1; i <= right; i++)
        {
            for (int j = i; j > left && Intrinsics.isCompInfKType(a.get(j), a.get(j - 1)); j--)
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
     * @param right
     */
    private static/*! #if ($TemplateOptions.KTypeGeneric) !*/<KType extends Comparable<? super KType>> /*! #end !*/void dualPivotQuicksort(final KTypeIndexedContainer<KType> a, final int left,
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
        if (Intrinsics.isCompSupKType(a.get(m1), a.get(m2)) /* a[m1] > a[m2]*/)
        {
            x = a.get(m1);
            a.set(m1, a.get(m2));
            a.set(m2, x);
        }
        if (Intrinsics.isCompSupKType(a.get(m4), a.get(m5)) /* a[m4] > a[m5]*/)
        {
            x = a.get(m4);
            a.set(m4, a.get(m5));
            a.set(m5, x);

        }
        if (Intrinsics.isCompSupKType(a.get(m1), a.get(m3)) /*a[m1] > a[m3]*/)
        {
            x = a.get(m1);
            a.set(m1, a.get(m3));
            a.set(m3, x);
        }
        if (Intrinsics.isCompSupKType(a.get(m2), a.get(m3)) /* a[m2] > a[m3]*/)
        {
            x = a.get(m2);
            a.set(m2, a.get(m3));
            a.set(m3, x);

        }
        if (Intrinsics.isCompSupKType(a.get(m1), a.get(m4)) /* a[m1] > a[m4]*/)
        {
            x = a.get(m1);
            a.set(m1, a.get(m4));
            a.set(m4, x);

        }
        if (Intrinsics.isCompSupKType(a.get(m3), a.get(m4)) /*a[m3] > a[m4]*/)
        {
            x = a.get(m3);
            a.set(m3, a.get(m4));
            a.set(m4, x);

        }
        if (Intrinsics.isCompSupKType(a.get(m2), a.get(m5)) /* a[m2] > a[m5]*/)
        {
            x = a.get(m2);
            a.set(m2, a.get(m5));
            a.set(m5, x);
        }
        if (Intrinsics.isCompSupKType(a.get(m2), a.get(m3)) /*a[m2] > a[m3]*/)
        {
            x = a.get(m2);
            a.set(m2, a.get(m3));
            a.set(m3, x);
        }
        if (Intrinsics.isCompSupKType(a.get(m4), a.get(m5)) /* a[m4] > a[m5]*/)
        {
            x = a.get(m4);
            a.set(m4, a.get(m5));
            a.set(m5, x);
        }

        // pivots: [ < pivot1 | pivot1 <= && <= pivot2 | > pivot2 ]
        final KType pivot1 = a.get(m2);
        final KType pivot2 = a.get(m4);

        final boolean diffPivots = !Intrinsics.isCompEqualKType(pivot1, pivot2);

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

                if (Intrinsics.isCompInfKType(x, pivot1)/* x < pivot1 */)
                {
                    a.set(k, a.get(less));
                    a.set(less, x);
                    less++;
                }
                else if (Intrinsics.isCompSupKType(x, pivot2) /* x > pivot2 */)
                {
                    while (Intrinsics.isCompSupKType(a.get(great), pivot2) /* a[great] > pivot2 */&& k < great)
                    {
                        great--;
                    }
                    a.set(k, a.get(great));
                    a.set(great, x);
                    great--;
                    x = a.get(k);

                    if (Intrinsics.isCompInfKType(x, pivot1) /*x < pivot1*/)
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

                if (Intrinsics.isCompEqualKType(x, pivot1) /*x == pivot1*/)
                {
                    continue;
                }
                if (Intrinsics.isCompInfKType(x, pivot1) /* x < pivot1 */)
                {
                    a.set(k, a.get(less));
                    a.set(less, x);
                    less++;
                }
                else
                {
                    while (Intrinsics.isCompSupKType(a.get(great), pivot2) /* a[great] > pivot2*/&& k < great)
                    {
                        great--;
                    }
                    a.set(k, a.get(great));
                    a.set(great, x);
                    great--;
                    x = a.get(k);

                    if (Intrinsics.isCompInfKType(x, pivot1) /*x < pivot1*/)
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
                if (Intrinsics.isCompEqualKType(x, pivot1) /*x == pivot1*/)
                {
                    a.set(k, a.get(less));
                    a.set(less, x);
                    less++;
                }
                else if (Intrinsics.isCompEqualKType(x, pivot2) /*x == pivot2*/)
                {
                    a.set(k, a.get(great));
                    a.set(great, x);
                    great--;
                    x = a.get(k);

                    if (Intrinsics.isCompEqualKType(x, pivot1) /*x == pivot1*/)
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
     * @param right
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
     * @param right
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
     * @param right
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
     * Private recursive sort method for KTypeIndexedContainer<KType> a , [left, right] inclusive using KTypeComparator
     * for comparison.
     * @param a
     * @param left
     * @param right
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
