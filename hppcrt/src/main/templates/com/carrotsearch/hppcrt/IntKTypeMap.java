package com.carrotsearch.hppcrt;

import com.carrotsearch.hppcrt.cursors.*;

/**
 * An associative container with unique binding from ints to a single value.
 * This is indeed a placeholder for template compilation,
 * and will indeed be replaced by a (int, VType) instantiation
 * of KTypeVTypeMap
 */
/*! ($TemplateOptions.doNotGenerate()) !*/
public interface IntKTypeMap<T>
extends IntKTypeAssociativeContainer<T>
{

    T put(int key, T value);

    boolean putIfAbsent(final int key, final T value);

    T get(int key);

    int putAll(IntKTypeAssociativeContainer<? extends T> container);

    int putAll(Iterable<? extends IntKTypeCursor<? extends T>> iterable);

    T remove(int key);
}
