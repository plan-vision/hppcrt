package com.carrotsearch.hppcrt;

import com.carrotsearch.hppcrt.KTypeCollection;
import com.carrotsearch.hppcrt.cursors.KTypeCursor;

/**
 * A set of <code>KType</code>s.
 */
// ${TemplateOptions.doNotGenerateKType("BOOLEAN")}
/*! ${TemplateOptions.generatedAnnotation} !*/
public interface KTypeSet<KType> extends KTypeCollection<KType>
{
    /**
     * Adds <code>k</code> to the set.
     * 
     * @return Returns <code>true</code> if this element was not part of the set before. Returns
     * <code>false</code> if an equal element is already part of the set, <b>and leaves the set unchanged. </b>.
     */
    boolean add(KType k);

    /**
     * Adds all elements from a given container to this set.
     * 
     * @return Returns the number of elements actually added as a result of this
     *         call (not previously present in the set).
     */
    int addAll(final KTypeContainer<? extends KType> container);

    /**
     * Adds all elements from a given iterable to this set.
     * 
     * @return Returns the number of elements actually added as a result of this
     *         call (not previously present in the set).
     */
    int addAll(final Iterable<? extends KTypeCursor<? extends KType>> iterable);

    /**
     * Remove all elements of the set matching key. Returns true
     * if key was present in the set and has been successfully removed.
     * This is indeed an alias for {@code KTypeCollection.removeAll(key) > 0}
     * @see KTypeCollection#removeAll(key)
     */
    boolean remove(KType key);
}
