package com.carrotsearch.hppcrt;

import java.util.Iterator;

import com.carrotsearch.hppcrt.cursors.*;
import com.carrotsearch.hppcrt.predicates.*;
import com.carrotsearch.hppcrt.procedures.*;

/**
 * An associative container (alias: map, dictionary) from keys to (one or possibly more) values.
 * Object keys must fulfill the contract of {@link Object#hashCode()} and {@link Object#equals(Object)}.
 * This is indeed a placeholder for template compilation,
 * and will indeed be replaced by a (int, VType) instantiation
 * of KTypeVTypeAssociativeContainer
 * 
 * @see KTypeContainer
 */
/*! ($TemplateOptions.doNotGenerate()) !*/
public interface IntKTypeAssociativeContainer<U>
        extends Iterable<IntKTypeCursor<U>>
{

    @Override
    Iterator<IntKTypeCursor<U>> iterator();

    boolean containsKey(int key);

    int size();

    int capacity();

    boolean isEmpty();

    int removeAll(IntContainer container);

    int removeAll(IntPredicate predicate);

    public int removeAll(IntKTypePredicate<? super U> predicate);

    <T extends IntKTypeProcedure<? super U>> T forEach(T procedure);

    <T extends IntKTypePredicate<? super U>> T forEach(T predicate);

    void clear();

    IntCollection keys();

    KTypeCollection<U> values();
}
