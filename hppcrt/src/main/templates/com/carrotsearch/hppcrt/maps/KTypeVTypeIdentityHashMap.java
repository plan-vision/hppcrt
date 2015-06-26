package com.carrotsearch.hppcrt.maps;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.cursors.*;
import com.carrotsearch.hppcrt.predicates.*;
import com.carrotsearch.hppcrt.procedures.*;
import com.carrotsearch.hppcrt.strategies.*;
import com.carrotsearch.hppcrt.hash.*;

/*! ${TemplateOptions.doNotGenerateKType("BYTE", "CHAR", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE")} !*/
/**
 * An identity hash map of <code>KType</code> to <code>VType</code>, implemented using open
 * addressing with linear probing for collision resolution.
 *
 * The difference with {@link KTypeVTypeHashMap} is that it uses direct Object reference equality for comparison and
 * direct "address" {@link System#identityHashCode(Object)} for hashCode(), instead of using
 * the built-in hashCode() /  equals().
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
public final class KTypeVTypeIdentityHashMap<KType, VType>
        extends KTypeVTypeCustomHashMap<KType, VType>
{
    private static final KTypeIdentityHash<Object> IDENTITY_EQUALITY = new KTypeIdentityHash<Object>();

    /**
     * Creates a hash map with the default capacity of {@value Containers#DEFAULT_EXPECTED_ELEMENTS},
     * load factor of {@value HashContainers#DEFAULT_LOAD_FACTOR}.
     * 
     * <p>See class notes about hash distribution importance.</p>
     */
    public KTypeVTypeIdentityHashMap()
    {
        this(Containers.DEFAULT_EXPECTED_ELEMENTS, HashContainers.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a hash map with the given initial capacity, default load factor of
     * {@value HashContainers#DEFAULT_LOAD_FACTOR}.
     * 
     * @param initialCapacity Initial capacity (greater than zero and automatically
     *            rounded to the next power of two).
     */
    public KTypeVTypeIdentityHashMap(final int initialCapacity)
    {
        this(initialCapacity, HashContainers.DEFAULT_LOAD_FACTOR);
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
    public KTypeVTypeIdentityHashMap(final int initialCapacity, final double loadFactor)
    {
        super(initialCapacity, loadFactor, (KTypeIdentityHash<KType>) KTypeVTypeIdentityHashMap.IDENTITY_EQUALITY);
    }

    /**
     * Create a hash map from all key-value pairs of another container.
     */
    public KTypeVTypeIdentityHashMap(final KTypeVTypeAssociativeContainer<KType, VType> container)
    {
        this(container.size());
        putAll(container);
    }

    @Override
    public KTypeVTypeIdentityHashMap<KType, VType> clone()
    {
        //clone to size() to prevent eventual exponential growth
        final KTypeVTypeIdentityHashMap<KType, VType> cloned =
                new KTypeVTypeIdentityHashMap<KType, VType>(size(), this.loadFactor);

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
    public static <KType, VType> KTypeVTypeIdentityHashMap<KType, VType> from(final KType[] keys, final VType[] values)
    {
        if (keys.length != values.length)
        {
            throw new IllegalArgumentException("Arrays of keys and values must have an identical length.");
        }

        final KTypeVTypeIdentityHashMap<KType, VType> map = new KTypeVTypeIdentityHashMap<KType, VType>(keys.length);

        for (int i = 0; i < keys.length; i++)
        {
            map.put(keys[i], values[i]);
        }
        return map;
    }

    /**
     * Create a hash map from another associative container.
     */
    public static <KType, VType> KTypeVTypeIdentityHashMap<KType, VType> from(final KTypeVTypeAssociativeContainer<KType, VType> container)
    {
        return new KTypeVTypeIdentityHashMap<KType, VType>(container);
    }

    /**
     * Create a new hash map without providing the full generic signature (constructor
     * shortcut).
     */
    public static <KType, VType> KTypeVTypeIdentityHashMap<KType, VType> newInstance()
    {
        return new KTypeVTypeIdentityHashMap<KType, VType>();
    }

    /**
     * Create a new hash map with initial capacity and load factor control. (constructor
     * shortcut).
     */
    public static <KType, VType> KTypeVTypeIdentityHashMap<KType, VType> newInstance(final int initialCapacity, final double loadFactor)
    {
        return new KTypeVTypeIdentityHashMap<KType, VType>(initialCapacity, loadFactor);
    }

    /**
     * Inherited from KTypeVTypeOpenCustomHashMap, DO NOT USE, throws RuntimeException
     * @throws RuntimeException
     */
    public static final <KType, VType> KTypeVTypeIdentityHashMap<KType, VType> newInstance(final KTypeHashingStrategy<? super KType> hashStrategy)
    {
        throw new RuntimeException("Identity hash newInstance(strategy) usage logical error");
    }

    /**
     * Inherited from KTypeVTypeOpenCustomHashMap, DO NOT USE, throws RuntimeException
     * @throws RuntimeException
     */
    public static final <KType, VType> KTypeVTypeIdentityHashMap<KType, VType> newInstance(final int initialCapacity, final double loadFactor, final KTypeHashingStrategy<? super KType> hashStrategy)
    {
        throw new RuntimeException("Identity hash newInstance(capacity, loadfactor, strategy) usage logical error");
    }

    /**
     * Inherited from KTypeVTypeOpenCustomHashMap, DO NOT USE, throws RuntimeException
     * @throws RuntimeException
     */
    public static final <KType, VType> KTypeVTypeIdentityHashMap<KType, VType> from(final KType[] keys, final VType[] values, final KTypeHashingStrategy<? super KType> hashStrategy)
    {
        throw new RuntimeException("Identity hash from(keys,values,strategy) usage logical error");
    }

    /**
     * Inherited from KTypeVTypeOpenCustomHashMap, DO NOT USE, throws RuntimeException
     * @throws RuntimeException
     */
    public static final <KType, VType> KTypeVTypeIdentityHashMap<KType, VType> from(final KTypeVTypeAssociativeContainer<KType, VType> container,
            final KTypeHashingStrategy<? super KType> hashStrategy)
            {
        throw new RuntimeException("Identity hash from(KTypeVTypeAssociativeContainer, strategy) usage logical error");
            }
}
