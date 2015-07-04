package com.carrotsearch.hppcrt;

import java.util.List;
import java.util.RandomAccess;

import com.carrotsearch.hppcrt.predicates.KTypePredicate;
import com.carrotsearch.hppcrt.procedures.KTypeProcedure;

/**
 * An indexed container provides random access to elements based on an
 * <code>index</code>. Indexes are zero-based.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public interface KTypeIndexedContainer<KType> extends KTypeCollection<KType>, RandomAccess
{
    /**
     * Removes the first element that equals <code>e1</code>, returning its
     * deleted position or <code>-1</code> if the element was not found.
     */
    int removeFirst(KType e1);

    /**
     * Removes the last element that equals <code>e1</code>, returning its
     * deleted position or <code>-1</code> if the element was not found.
     */
    int removeLast(KType e1);

    /**
     * Returns the index of the first occurrence of the specified element in this list,
     * or -1 if this list does not contain the element.
     */
    int indexOf(KType e1);

    /**
     * Returns the index of the last occurrence of the specified element in this list,
     * or -1 if this list does not contain the element.
     */
    int lastIndexOf(KType e1);

    /**
     * Adds an element to the end of this container (the last index is incremented by one).
     */
    void add(KType e1);

    /**
     * Inserts the specified element at the specified position in this list.
     * 
     * @param index The index at which the element should be inserted, shifting
     * any existing and subsequent elements to the right.
     * Precondition : index must be valid !
     */
    void insert(int index, KType e1);

    /**
     * Replaces the element at the specified position in this list
     * with the specified element.
     * Precondition : index must be valid !
     * @return Returns the previous value in the list.
     */
    KType set(int index, KType e1);

    /**
     * @return Returns the element at index <code>index</code> from the list.
     * Precondition : index must be valid !
     */
    public KType get(int index);

    /**
     * Removes the element at the specified position in this list and returns it.
     * Precondition : index must be valid !
     * <p><b>Careful.</b> Do not confuse this method with the overridden signature in
     * Java Collections ({@link List#remove(Object)}). Use: {@link #removeAll},
     * {@link #removeFirst} or {@link #removeLast} depending
     * on the actual need.</p>
     */
    KType remove(int index);

    /**
     * Removes from this list all of the elements whose index is between
     * <code>fromIndex</code>, inclusive, and <code>toIndex</code>, exclusive.
     */
    void removeRange(int fromIndex, int toIndex);

    /**
     * Applies <code>procedure</code> to a slice of the container,
     * <code>fromIndex</code>, inclusive, to <code>toIndex</code>, exclusive.
     */
    <T extends KTypeProcedure<? super KType>> T forEach(final T procedure, final int fromIndex, final int toIndex);

    /**
     * Applies <code>predicate</code> to a slice of the container,
     * <code>fromIndex</code>, inclusive, to <code>toIndex</code>,
     * exclusive, or until predicate returns <code>false</code>.
     */
    <T extends KTypePredicate<? super KType>> T forEach(final T predicate, final int fromIndex, final int toIndex);

}
