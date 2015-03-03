package com.carrotsearch.hppcrt;

import com.carrotsearch.hppcrt.cursors.IntKTypeCursor;

/**
 * An associative container with unique binding from ints to a single value.
 * This is indeed a placeholder for template compilation,
 * and will indeed be replaced by a (int, VType) instantiation
 * of KTypeVTypeMap
 */
/*! ($TemplateOptions.doNotGenerate()) !*/
/*! ${TemplateOptions.generatedAnnotation} !*/
public interface IntKTypeMap<T>
extends IntKTypeAssociativeContainer<T>
{
    /**
     * Place a given key and value in the container.
     * @return Returns the value previously stored under the given key in the map if an equal key is part of the map, <b>and replaces the existing
     *  value only </b> with the argument value. If no previous key existed,
     * the default value is returned and the (key, value) pair is inserted.
     */
    T put(int key, T value);

    /**
     * <a href="http://trove4j.sourceforge.net">Trove</a>-inspired API method. An equivalent
     * of the following code:
     * <pre>
     * if (!map.containsKey(key))
     *      map.put(key, value);
     * </pre>
     * 
     * @param key The key of the value to check.
     * @param value The value to put if <code>key</code> does not exist.
     * @return <code>true</code> if <code>key</code> did not exist and <code>value</code>
     * was placed in the map.
     */
    boolean putIfAbsent(final int key, final T value);

    /**
     * @return Returns the value associated with the given key or the default value
     * for the value type, if the key is not associated with any value.
     *
     */
    T get(int key);

    /**
     * Puts all keys from another container to this map, replacing the values
     * of existing keys, if such keys are present.
     * 
     * @return Returns the number of keys added to the map as a result of this
     * call (not previously present in the map). Values of existing keys are overwritten.
     */
    int putAll(IntKTypeAssociativeContainer<? extends T> container);

    /**
     * Puts all keys from an iterable cursor to this map, replacing the values
     * of existing keys, if such keys are present.
     * 
     * @return Returns the number of keys added to the map as a result of this
     * call (not previously present in the map). Values of existing keys are overwritten.
     */
    int putAll(Iterable<? extends IntKTypeCursor<? extends T>> iterable);

    /**
     * Remove all values at the given key. The default value for the key type is returned
     * if the value does not exist in the map.
     */
    T remove(int key);
}
