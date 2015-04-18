package com.carrotsearch.hppcrt;

import java.util.Arrays;

import com.carrotsearch.hppcrt.cursors.KTypeCursor;
import com.carrotsearch.hppcrt.predicates.KTypePredicate;

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
/**
 * Common superclass for collections.
 */
/*! #if ($TemplateOptions.KTypeGeneric) !*/
@SuppressWarnings("unchecked")
/*! #end !*/
/*! ${TemplateOptions.generatedAnnotation} !*/
public abstract class AbstractKTypeCollection<KType> implements KTypeCollection<KType>
{
    protected KTypeLookupContainer<? super KType> testContainer;
    protected KTypePredicate<? super KType> testPredicate;

    protected KType defaultValue = Intrinsics.<KType> defaultKTypeValue();

    protected KTypePredicate<KType> containsTestPredicate = new KTypePredicate<KType>() {

        @Override
        public final boolean apply(final KType k)
        {
            return AbstractKTypeCollection.this.testContainer.contains(k);
        }
    };

    protected KTypePredicate<KType> containsNegateTestPredicate = new KTypePredicate<KType>() {

        @Override
        public final boolean apply(final KType k)
        {
            return !AbstractKTypeCollection.this.testContainer.contains(k);
        }
    };

    protected KTypePredicate<KType> negatePredicate = new KTypePredicate<KType>() {

        @Override
        public final boolean apply(final KType k)
        {
            return !AbstractKTypeCollection.this.testPredicate.apply(k);
        }
    };

    /**
     * Default implementation uses a predicate for removal.
     */
    @Override
    public int removeAll(final KTypeLookupContainer<? super KType> c)
    {
        // We know c holds sub-types of KType and we're not modifying c, so go unchecked.
        this.testContainer = c;
        return this.removeAll(this.containsTestPredicate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int retainAll(final KTypeLookupContainer<? super KType> c)
    {
        // We know c holds sub-types of KType and we're not modifying c, so go unchecked.
        this.testContainer = c;
        return this.removeAll(this.containsNegateTestPredicate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int retainAll(final KTypePredicate<? super KType> predicate)
    {
        this.testPredicate = predicate;
        return this.removeAll(this.negatePredicate);
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    /**
     * Default implementation for:
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(final Class<T> componentClass)
    {
        final int size = size();

        final T[] array = (T[]) java.lang.reflect.Array.newInstance(componentClass, size);

        int i = 0;
        for (final KTypeCursor<KType> c : this)
        {
            array[i++] = (T) c.value;
        }

        return array;
    }

    /*! #end !*/

    /**
     * Default implementation for:
     * {@inheritDoc}
     */
    @Override
    public KType[] toArray(final KType[] target)
    {
        assert target.length >= size() : "Target array must be >= " + size();

        int i = 0;
        //use default iterator capability
        for (final KTypeCursor<KType> c : this)
        {
            target[i++] = c.value;
        }

        return target;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    /*! #if ($TemplateOptions.KTypePrimitive)
    public KType [] toArray()
    #else !*/
    public Object[] toArray()
    /*! #end !*/
    {
        try {

            return toArray(Intrinsics.<KType[]> newKTypeArray(size()));
        }
        catch (final OutOfMemoryError e) {

            throw new BufferAllocationException(
                    "Not enough memory to allocate toArray() buffer for  %d elements",
                    e,
                    size());
        }
    }

    /**
     * Convert the contents of this container to a human-friendly string.
     */
    @Override
    public String toString()
    {
        return Arrays.toString(this.toArray());
    }

    @Override
    public boolean isEmpty()
    {
        return size() == 0;
    }

    /**
     * Returns the "default value" value used
     * in containers methods returning "default value"
     * @return
     */
    public KType getDefaultValue()
    {
        return this.defaultValue;
    }

    /**
     * Set the "default value" value to be used
     * in containers methods returning "default value"
     * @return
     */
    public void setDefaultValue(final KType defaultValue)
    {
        this.defaultValue = defaultValue;
    }
}
