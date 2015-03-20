package com.carrotsearch.hppcrt.maps;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.cursors.*;
import com.carrotsearch.hppcrt.predicates.*;
import com.carrotsearch.hppcrt.procedures.*;
import com.carrotsearch.hppcrt.strategies.*;
import com.carrotsearch.hppcrt.hash.*;

/*! ${TemplateOptions.doNotGenerateKType("BOOLEAN", "BYTE", "CHAR", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE")} !*/
/**
 * An identity hash map of <code>KType</code> to <code>VType</code>, implemented using open
 * addressing with linear probing for collision resolution.
 *
 * The difference with {@link KTypeVTypeOpenHashMap} is that it uses direct Object reference equality for comparison and
 * direct "address" {@link System#identityHashCode(Object)} for hashCode(), instead of using
 * the built-in hashCode() /  equals().
 * The internal buffers of this implementation ({@link #keys},{@link #values}, etc...)
 * are always allocated to the nearest size that is a power of two. When
 * the capacity exceeds the given load factor, the buffer size is doubled.
 * </p>
 * 
 * <p>This implementation supports <code>null</code> keys.</p>
 * 
#if ($TemplateOptions.VTypeGeneric)
 * <p>This implementation supports <code>null</code> values.</p>
#end
 * 
 * 
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public final class KTypeVTypeOpenIdentityHashMap<KType, VType>
extends KTypeVTypeOpenCustomHashMap<KType, VType>
{
    private static final KTypeIdentityHash<Object> IDENTITY_EQUALITY = new KTypeIdentityHash<Object>();

    /**
     * Creates a hash map with the default capacity of {@value #DEFAULT_CAPACITY},
     * load factor of {@value #DEFAULT_LOAD_FACTOR}.
     * 
     * <p>See class notes about hash distribution importance.</p>
     */
    public KTypeVTypeOpenIdentityHashMap()
    {
        this(KTypeVTypeOpenCustomHashMap.DEFAULT_CAPACITY, KTypeVTypeOpenCustomHashMap.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a hash map with the given initial capacity, default load factor of
     * {@value #DEFAULT_LOAD_FACTOR}.
     * 
     * @param initialCapacity Initial capacity (greater than zero and automatically
     *            rounded to the next power of two).
     */
    public KTypeVTypeOpenIdentityHashMap(final int initialCapacity)
    {
        this(initialCapacity, KTypeVTypeOpenCustomHashMap.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a hash map with the given initial capacity,
     * load factor.
     * 
     * @param initialCapacity Initial capacity (greater than zero and automatically
     *            rounded to the next power of two).
     *
     * @param loadFactor The load factor (greater than zero and smaller than 1).
     * 
     * 
     */
    @SuppressWarnings({ "cast", "unchecked" })
    public KTypeVTypeOpenIdentityHashMap(final int initialCapacity, final float loadFactor)
    {
        super(initialCapacity, loadFactor, (KTypeIdentityHash<KType>) KTypeVTypeOpenIdentityHashMap.IDENTITY_EQUALITY);
    }

    /**
     * Create a hash map from all key-value pairs of another container.
     */
    public KTypeVTypeOpenIdentityHashMap(final KTypeVTypeAssociativeContainer<KType, VType> container)
    {
        this(container.size());
        putAll(container);
    }

    @Override
    public KTypeVTypeOpenIdentityHashMap<KType, VType> clone()
    {
        final KTypeVTypeOpenIdentityHashMap<KType, VType> cloned =
                new KTypeVTypeOpenIdentityHashMap<KType, VType>(capacity(), this.loadFactor);

        //We must NOT clone because of the independent perturbation seeds
        cloned.putAll(this);

        cloned.allocatedDefaultKeyValue = this.allocatedDefaultKeyValue;
        cloned.allocatedDefaultKey = this.allocatedDefaultKey;
        cloned.defaultValue = this.defaultValue;

        return cloned;
    }

    /**
     * Creates a hash map from two index-aligned arrays of key-value pairs.
     */
    public static <KType, VType> KTypeVTypeOpenIdentityHashMap<KType, VType> from(final KType[] keys, final VType[] values)
    {
        if (keys.length != values.length)
        {
            throw new IllegalArgumentException("Arrays of keys and values must have an identical length.");
        }

        final KTypeVTypeOpenIdentityHashMap<KType, VType> map = new KTypeVTypeOpenIdentityHashMap<KType, VType>(keys.length);

        for (int i = 0; i < keys.length; i++)
        {
            map.put(keys[i], values[i]);
        }
        return map;
    }

    /**
     * Create a hash map from another associative container.
     */
    public static <KType, VType> KTypeVTypeOpenIdentityHashMap<KType, VType> from(final KTypeVTypeAssociativeContainer<KType, VType> container)
    {
        return new KTypeVTypeOpenIdentityHashMap<KType, VType>(container);
    }

    /**
     * Create a new hash map without providing the full generic signature (constructor
     * shortcut).
     */
    public static <KType, VType> KTypeVTypeOpenIdentityHashMap<KType, VType> newInstance()
    {
        return new KTypeVTypeOpenIdentityHashMap<KType, VType>();
    }

    /**
     * Create a new hash map with initial capacity and load factor control. (constructor
     * shortcut).
     */
    public static <KType, VType> KTypeVTypeOpenIdentityHashMap<KType, VType> newInstance(final int initialCapacity, final float loadFactor)
    {
        return new KTypeVTypeOpenIdentityHashMap<KType, VType>(initialCapacity, loadFactor);
    }

    /**
     * Inherited from KTypeVTypeOpenCustomHashMap, DO NOT USE, throws RuntimeException
     * @throws RuntimeException
     */
    public static final <KType, VType> KTypeVTypeOpenIdentityHashMap<KType, VType> newInstance(final KTypeHashingStrategy<? super KType> hashStrategy)
    {
        throw new RuntimeException("Identity hash newInstance(strategy) usage logical error");
    }

    /**
     * Inherited from KTypeVTypeOpenCustomHashMap, DO NOT USE, throws RuntimeException
     * @throws RuntimeException
     */
    public static final <KType, VType> KTypeVTypeOpenIdentityHashMap<KType, VType> newInstance(final int initialCapacity, final float loadFactor, final KTypeHashingStrategy<? super KType> hashStrategy)
    {
        throw new RuntimeException("Identity hash newInstance(capacity, loadfactor, strategy) usage logical error");
    }

    /**
     * Inherited from KTypeVTypeOpenCustomHashMap, DO NOT USE, throws RuntimeException
     * @throws RuntimeException
     */
    public static final <KType, VType> KTypeVTypeOpenIdentityHashMap<KType, VType> from(final KType[] keys, final VType[] values, final KTypeHashingStrategy<? super KType> hashStrategy)
    {
        throw new RuntimeException("Identity hash from(keys,values,strategy) usage logical error");
    }

    /**
     * Inherited from KTypeVTypeOpenCustomHashMap, DO NOT USE, throws RuntimeException
     * @throws RuntimeException
     */
    public static final <KType, VType> KTypeVTypeOpenIdentityHashMap<KType, VType> from(final KTypeVTypeAssociativeContainer<KType, VType> container,
            final KTypeHashingStrategy<? super KType> hashStrategy)
    {
        throw new RuntimeException("Identity hash from(KTypeVTypeAssociativeContainer, strategy) usage logical error");
    }
}
