package com.carrotsearch.hppcrt.sets;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.cursors.*;
import com.carrotsearch.hppcrt.predicates.*;
import com.carrotsearch.hppcrt.procedures.*;
import com.carrotsearch.hppcrt.strategies.*;
import com.carrotsearch.hppcrt.hash.*;

/*! ${TemplateOptions.doNotGenerateKType("BOOLEAN", "BYTE", "CHAR", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE")} !*/
/**
 * An identity hash set of <code>KType</code> types, implemented using open
 * addressing with linear probing for collision resolution.
 *
 * The difference with {@link KTypeOpenHashSet} is that it uses direct Object reference equality for comparison and
 * direct "address" {@link System#identityHashCode(Object)} for hashCode(), instead of using
 * the built-in hashCode() /  equals().
 * <p>
 * The internal buffers of this implementation ({@link #keys}, etc...)
 * are always allocated to the nearest size that is a power of two. When
 * the capacity exceeds the given load factor, the buffer size is doubled.
 * </p>
 * 
 * <p>This implementation supports <code>null</code> keys.</p>
 *
 * @author This code is inspired by the collaboration and implementation in the <a
 *         href="http://fastutil.dsi.unimi.it/">fastutil</a> project.
 *
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public final class KTypeOpenIdentityHashSet<KType>
        extends KTypeOpenCustomHashSet<KType>
{
    /**
     * Creates a hash set with the default capacity of {@value #DEFAULT_CAPACITY},
     * load factor of {@value #DEFAULT_LOAD_FACTOR}.
     */
    public KTypeOpenIdentityHashSet()
    {
        this(KTypeOpenCustomHashSet.DEFAULT_CAPACITY, KTypeOpenCustomHashSet.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a hash set with the given capacity,
     * load factor of {@value #DEFAULT_LOAD_FACTOR}.
     */
    public KTypeOpenIdentityHashSet(final int initialCapacity)
    {
        this(initialCapacity, KTypeOpenCustomHashSet.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a hash set with the given capacity and load factor.
     */
    public KTypeOpenIdentityHashSet(final int initialCapacity, final float loadFactor)
    {
        super(initialCapacity, loadFactor, new KTypeIdentityHash<KType>());
    }

    /**
     * Creates a hash set from elements of another container. Default load factor is used.
     */
    public KTypeOpenIdentityHashSet(final KTypeContainer<KType> container)
    {
        this(container.size());
        addAll(container);
    }

    @Override
    public KTypeOpenIdentityHashSet<KType> clone() {

        final KTypeOpenIdentityHashSet<KType> cloned = new KTypeOpenIdentityHashSet<KType>(size(), this.loadFactor);

        cloned.addAll(this);

        cloned.allocatedDefaultKey = this.allocatedDefaultKey;
        cloned.defaultValue = this.defaultValue;

        return cloned;
    }

    /**
     * Create a set from a variable number of arguments or an array of <code>KType</code>.
     */
    public static <KType> KTypeOpenIdentityHashSet<KType> from(final KType... elements)
    {
        final KTypeOpenIdentityHashSet<KType> set = new KTypeOpenIdentityHashSet<KType>(elements.length);
        set.add(elements);
        return set;
    }

    /**
     * Create a set from elements of another container.
     */
    public static <KType> KTypeOpenIdentityHashSet<KType> from(final KTypeContainer<KType> container)
    {
        return new KTypeOpenIdentityHashSet<KType>(container);
    }

    /**
     * Create a new hash set with default parameters (shortcut
     * instead of using a constructor).
     */
    public static <KType> KTypeOpenIdentityHashSet<KType> newInstance()
    {
        return new KTypeOpenIdentityHashSet<KType>();
    }

    /**
     * Returns a new object of this class with no need to declare generic type (shortcut
     * instead of using a constructor).
     */
    public static <KType> KTypeOpenIdentityHashSet<KType> newInstanceWithCapacity(final int initialCapacity, final float loadFactor)
    {
        return new KTypeOpenIdentityHashSet<KType>(initialCapacity, loadFactor);
    }

    /**
     * Inherited from KTypeOpenCustomHashSet, DO NOT USE, throws RuntimeException
     * @throws RuntimeException
     */
    public static <KType> KTypeOpenIdentityHashSet<KType> from(final KTypeHashingStrategy<? super KType> hashStrategy, final KType... elements)
    {
        throw new RuntimeException("Identity hash from(strategy, ...elements) usage logical error");
    }

    /**
     * Inherited from KTypeOpenCustomHashSet, DO NOT USE, throws RuntimeException
     * @throws RuntimeException
     */
    public static final <KType> KTypeOpenIdentityHashSet<KType> from(final KTypeContainer<KType> container, final KTypeHashingStrategy<? super KType> hashStrategy)
    {
        throw new RuntimeException("Identity hash from(KTypeContainer, strategy) usage logical error");
    }

    /**
     * Inherited from KTypeOpenCustomHashSet, DO NOT USE, throws RuntimeException
     * @throws RuntimeException
     */
    public static final <KType> KTypeOpenIdentityHashSet<KType> newInstance(final KTypeHashingStrategy<? super KType> hashStrategy)
    {
        throw new RuntimeException("Identity hash newInstance(strategy) usage logical error");
    }

    /**
     * Inherited from KTypeOpenCustomHashSet, DO NOT USE, throws RuntimeException
     * @throws RuntimeException
     */
    public static final <KType> KTypeOpenIdentityHashSet<KType> newInstanceWithCapacity(final int initialCapacity, final float loadFactor, final KTypeHashingStrategy<? super KType> hashStrategy)
    {
        throw new RuntimeException("Identity hash newInstanceWithCapacity(strategy) usage logical error");
    }
}
