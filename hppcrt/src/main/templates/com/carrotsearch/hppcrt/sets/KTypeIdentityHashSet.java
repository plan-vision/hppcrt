package com.carrotsearch.hppcrt.sets;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.strategies.*;

/*! ${TemplateOptions.doNotGenerateKType( "BYTE", "CHAR", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE")} !*/
/**
 * An identity hash set of <code>KType</code> types, implemented using open
 * addressing with linear probing for collision resolution.
 *
 * The difference with {@link KTypeHashSet} is that it uses direct Object reference equality for comparison and
 * direct "address" {@link System#identityHashCode(Object)} for hashCode(), instead of using
 * the built-in hashCode() /  equals().
 * 
 * <p>This implementation supports <code>null</code> keys.</p>
 *
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public final class KTypeIdentityHashSet<KType>
        extends KTypeCustomHashSet<KType>
{
    private static final KTypeIdentityHash<Object> IDENTITY_EQUALITY = new KTypeIdentityHash<Object>();

    /**
     * Creates a hash set with the default capacity of {@value Containers#DEFAULT_EXPECTED_ELEMENTS},
     * load factor of {@value HashContainers#DEFAULT_LOAD_FACTOR}.
     */
    public KTypeIdentityHashSet()
    {
        this(Containers.DEFAULT_EXPECTED_ELEMENTS, HashContainers.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a hash set with the given capacity,
     * load factor of {@value HashContainers#DEFAULT_LOAD_FACTOR}.
     */
    public KTypeIdentityHashSet(final int initialCapacity)
    {
        this(initialCapacity, HashContainers.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a hash set with the given capacity and load factor.
     */
    @SuppressWarnings({ "cast", "unchecked" })
    public KTypeIdentityHashSet(final int initialCapacity, final double loadFactor)
    {
        super(initialCapacity, loadFactor, (KTypeIdentityHash<KType>) KTypeIdentityHashSet.IDENTITY_EQUALITY);
    }

    /**
     * Creates a hash set from elements of another container. Default load factor is used.
     */
    public KTypeIdentityHashSet(final KTypeContainer<KType> container)
    {
        this(container.size());
        addAll(container);
    }

    @Override
    public KTypeIdentityHashSet<KType> clone() {

        //clone to size() to prevent eventual exponential growth
        final KTypeIdentityHashSet<KType> cloned = new KTypeIdentityHashSet<KType>(size(), this.loadFactor);

        //We must NOT clone because of the independent perturbation values
        cloned.addAll(this);

        cloned.allocatedDefaultKey = this.allocatedDefaultKey;

        return cloned;
    }

    /**
     * Create a set from a variable number of arguments or an array of <code>KType</code>.
     */
    public static <KType> KTypeIdentityHashSet<KType> from(final KType... elements)
    {
        final KTypeIdentityHashSet<KType> set = new KTypeIdentityHashSet<KType>(elements.length);
        set.add(elements);
        return set;
    }

    /**
     * Create a set from elements of another container.
     */
    public static <KType> KTypeIdentityHashSet<KType> from(final KTypeContainer<KType> container)
    {
        return new KTypeIdentityHashSet<KType>(container);
    }

    /**
     * Create a new hash set with default parameters (shortcut
     * instead of using a constructor).
     */
    public static <KType> KTypeIdentityHashSet<KType> newInstance()
    {
        return new KTypeIdentityHashSet<KType>();
    }

    /**
     * Returns a new object of this class with no need to declare generic type (shortcut
     * instead of using a constructor).
     */
    public static <KType> KTypeIdentityHashSet<KType> newInstance(final int initialCapacity, final double loadFactor)
    {
        return new KTypeIdentityHashSet<KType>(initialCapacity, loadFactor);
    }

    /**
     * Inherited from KTypeOpenCustomHashSet, DO NOT USE, throws RuntimeException
     * @throws RuntimeException
     */
    public static <KType> KTypeIdentityHashSet<KType> from(final KTypeHashingStrategy<? super KType> hashStrategy, final KType... elements)
    {
        throw new RuntimeException("Identity hash from(strategy, ...elements) usage logical error");
    }

    /**
     * Inherited from KTypeOpenCustomHashSet, DO NOT USE, throws RuntimeException
     * @throws RuntimeException
     */
    public static final <KType> KTypeIdentityHashSet<KType> from(final KTypeContainer<KType> container, final KTypeHashingStrategy<? super KType> hashStrategy)
    {
        throw new RuntimeException("Identity hash from(KTypeContainer, strategy) usage logical error");
    }

    /**
     * Inherited from KTypeOpenCustomHashSet, DO NOT USE, throws RuntimeException
     * @throws RuntimeException
     */
    public static final <KType> KTypeIdentityHashSet<KType> newInstance(final KTypeHashingStrategy<? super KType> hashStrategy)
    {
        throw new RuntimeException("Identity hash newInstance(strategy) usage logical error");
    }

    /**
     * Inherited from KTypeOpenCustomHashSet, DO NOT USE, throws RuntimeException
     * @throws RuntimeException
     */
    public static final <KType> KTypeIdentityHashSet<KType> newInstance(final int initialCapacity, final double loadFactor, final KTypeHashingStrategy<? super KType> hashStrategy)
    {
        throw new RuntimeException("Identity hash newInstance(strategy) usage logical error");
    }
}
