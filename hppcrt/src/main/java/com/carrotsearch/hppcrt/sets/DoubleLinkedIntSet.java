package com.carrotsearch.hppcrt.sets;

import java.util.Arrays;
import java.util.Iterator;

import com.carrotsearch.hppcrt.AbstractIntCollection;
import com.carrotsearch.hppcrt.ArraySizingStrategy;
import com.carrotsearch.hppcrt.BitSet;
import com.carrotsearch.hppcrt.BoundedProportionalArraySizingStrategy;
import com.carrotsearch.hppcrt.EmptyArrays;
import com.carrotsearch.hppcrt.IntContainer;
import com.carrotsearch.hppcrt.IntLookupContainer;
import com.carrotsearch.hppcrt.IntSet;
import com.carrotsearch.hppcrt.IteratorPool;
import com.carrotsearch.hppcrt.ObjectFactory;
import com.carrotsearch.hppcrt.cursors.IntCursor;
import com.carrotsearch.hppcrt.lists.IntArrayList;
import com.carrotsearch.hppcrt.predicates.IntPredicate;
import com.carrotsearch.hppcrt.procedures.IntProcedure;

/**
 * A double-linked set of <code>int</code> values. This data structure is characterized by
 * constant-time lookup, insertions, deletions and removal of all set elements (unlike a
 * {@link BitSet} which takes time proportional to the maximum element's length).
 * 
 * <p>The implementation in based on
 * <a href="http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.30.7319">
 * Preston Briggs and Linda Torczon's paper "An Efficient Representation for Sparse Sets"</a></p>
 */
public class DoubleLinkedIntSet extends AbstractIntCollection implements IntLookupContainer, IntSet, Cloneable
{
    /**
     * Default capacity if no other capacity is given in the constructor.
     */
    public final static int DEFAULT_CAPACITY = 5;

    /**
     * Dense array of set elements.
     */
    public int[] dense = EmptyArrays.EMPTY_INT_ARRAY;

    /**
     * Sparse, element value-indexed array pointing back at {@link #dense}.
     */
    public int[] sparse = EmptyArrays.EMPTY_INT_ARRAY;

    /**
     * Current number of elements stored in the set ({@link #dense}).
     */
    public int elementsCount;

    /**
     * Buffer resizing strategy.
     */
    protected final ArraySizingStrategy resizer;

    /**
     * internal pool of ValueIterator (must be created in constructor)
     */

    protected final IntArrayList arrayListWrapper;

    protected final IteratorPool<IntCursor, IntArrayList.ValueIterator> valueIteratorPool;

    /**
     * Create with default sizing strategy and initial dense capacity of
     * {@value #DEFAULT_CAPACITY} elements and initial sparse capacity of zero (first insert
     * will cause reallocation).
     * 
     * @see BoundedProportionalArraySizingStrategy
     */
    public DoubleLinkedIntSet()
    {
        this(DoubleLinkedIntSet.DEFAULT_CAPACITY, 0);
    }

    /**
     * Create with default sizing strategy and the given initial capacity.
     * 
     * @see BoundedProportionalArraySizingStrategy
     */
    public DoubleLinkedIntSet(final int denseCapacity, final int sparseCapacity)
    {
        this(denseCapacity, sparseCapacity, new BoundedProportionalArraySizingStrategy());
    }

    /**
     * Create with a custom dense array resizing strategy.
     */
    public DoubleLinkedIntSet(final int denseCapacity, final int sparseCapacity, final ArraySizingStrategy resizer)
    {
        assert denseCapacity >= 0 : "denseCapacity must be >= 0: " + denseCapacity;
        assert sparseCapacity >= 0 : "sparseCapacity must be >= 0: " + sparseCapacity;
        assert resizer != null;

        this.resizer = resizer;

        //this is not really used, it is just there to provide a
        //IntArrayList iterator-like interface to IntDoubleLinkedSet
        this.arrayListWrapper = new IntArrayList(DoubleLinkedIntSet.DEFAULT_CAPACITY);

        ensureDenseCapacity(resizer.round(denseCapacity));
        ensureSparseCapacity(sparseCapacity);

        this.valueIteratorPool = new IteratorPool<IntCursor, IntArrayList.ValueIterator>(
                new ObjectFactory<IntArrayList.ValueIterator>() {

                    @Override
                    public IntArrayList.ValueIterator create() {

                        return DoubleLinkedIntSet.this.arrayListWrapper.new ValueIterator();
                    }

                    @Override
                    public void initialize(final IntArrayList.ValueIterator obj) {

                        //Make the buffer points on the one of the IntDoubleLinkedSet
                        obj.init(DoubleLinkedIntSet.this.dense, DoubleLinkedIntSet.this.size());
                    }

                    @Override
                    public void reset(final IntArrayList.ValueIterator obj) {
                        obj.reset();

                    }
                });
    }

    /**
     * Creates a set from elements of another container.
     */
    public DoubleLinkedIntSet(final IntContainer container)
    {
        this(container.size(), 1 + DoubleLinkedIntSet.maxElement(container));
        for (final IntCursor cursor : container)
        {
            addNoChecks(cursor.value);
        }
    }

    /**
     * Ensures the internal dense buffer has enough free slots to store
     * <code>expectedAdditions</code>.
     */
    protected void ensureDenseCapacity(final int expectedAdditions)
    {
        final int bufferLen = (this.dense == null ? 0 : this.dense.length);
        final int elementsCount = size();
        if (elementsCount > bufferLen - expectedAdditions)
        {
            final int newSize = this.resizer.grow(bufferLen, elementsCount, expectedAdditions);
            assert newSize >= elementsCount + expectedAdditions : "Resizer failed to" +
                    " return sensible new size: " + newSize + " <= "
                    + (elementsCount + expectedAdditions);

            final int[] newBuffer = new int[newSize];
            if (bufferLen > 0)
            {
                System.arraycopy(this.dense, 0, newBuffer, 0, elementsCount);
            }
            this.dense = newBuffer;
        }
    }

    /**
     * Ensures the internal sparse buffer has enough free slots to store
     * index of <code>value</code>.
     */
    protected void ensureSparseCapacity(final int value)
    {
        assert value >= 0 : "value must be >= 0: " + value;

        if (value >= this.sparse.length)
        {
            final int[] newBuffer = new int[value + 1];
            if (this.sparse.length > 0)
            {
                System.arraycopy(this.sparse, 0, newBuffer, 0, this.sparse.length);
            }
            this.sparse = newBuffer;
        }
    }

    @Override
    public int size()
    {
        return this.elementsCount;
    }

    @Override
    public int capacity()
    {
        return this.dense.length;
    }

    @Override
    public int[] toArray(final int[] target)
    {
        System.arraycopy(this.dense, 0, target, 0, size());
        return target;
    }

    @Override
    public void clear()
    {
        this.elementsCount = 0;
    }

    @Override
    public boolean contains(final int value)
    {
        int index;
        return value >= 0
                && value < this.sparse.length
                && (index = this.sparse[value]) < this.elementsCount
                && this.dense[index] == value;
    }

    @Override
    public boolean add(final int value)
    {
        assert value >= 0 : "Double linked set supports values >= 0 only.";

        final boolean containsAlready = contains(value);
        if (!containsAlready)
        {
            // TODO: check if a fixed-size set is (much) faster without these checks?
            ensureDenseCapacity(1);
            ensureSparseCapacity(value);

            this.sparse[value] = this.elementsCount;
            this.dense[this.elementsCount++] = value;
        }
        return !containsAlready;
    }

    /**
     * A faster version of {@link #add(int)} that does check or attempt to expand the
     * internal buffers. Assertions are still present.
     */
    private void addNoChecks(final int value)
    {
        assert value >= 0 : "Double linked set supports values >= 0 only.";

        final boolean containsAlready = contains(value);
        if (!containsAlready)
        {
            assert size() + 1 < this.dense.length : "Dense array too small.";
            assert value < this.sparse.length : "Value too large for sparse.";

            this.sparse[value] = this.elementsCount;
            this.dense[this.elementsCount++] = value;
        }
    }

    /**
     * Adds two elements to the set.
     */
    public int add(final int e1, final int e2)
    {
        int count = 0;
        if (add(e1)) {
            count++;
        }
        if (add(e2)) {
            count++;
        }
        return count;
    }

    /**
     * Vararg-signature method for adding elements to this set.
     * <p><b>This method is handy, but costly if used in tight loops (anonymous
     * array passing)</b></p>
     * 
     * @return Returns the number of elements that were added to the set
     * (were not present in the set).
     */
    public int add(final int... elements)
    {
        int count = 0;
        for (final int e : elements) {
            if (add(e)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Adds all elements from a given container to this set.
     * 
     * @return Returns the number of elements actually added as a result of this
     * call (not previously present in the set).
     */
    public int addAll(final IntContainer container)
    {
        return addAll((Iterable<IntCursor>) container);
    }

    /**
     * Adds all elements from a given iterable to this set.
     * 
     * @return Returns the number of elements actually added as a result of this
     * call (not previously present in the set).
     */
    public int addAll(final Iterable<? extends IntCursor> iterable)
    {
        int count = 0;
        for (final IntCursor cursor : iterable)
        {
            if (add(cursor.value)) {
                count++;
            }
        }

        return count;
    }

    /*
     * 
     */
    @Override
    public int removeAll(final int value)
    {
        if (value >= 0 && value < this.sparse.length)
        {
            final int slot = this.sparse[value];
            final int n = this.elementsCount - 1;
            if (slot <= n && this.dense[slot] == value)
            {
                // Swap the last value with the removed value.
                final int lastValue = this.dense[n];
                this.elementsCount--;
                this.dense[slot] = lastValue;
                this.sparse[lastValue] = slot;
                return 1;
            }
        }
        return 0;
    }

    /**
     * An alias for the (preferred) {@link #removeAll}.
     */
    public boolean remove(final int key)
    {
        return removeAll(key) == 1;
    }

    @Override
    public Iterator<IntCursor> iterator()
    {
        return this.valueIteratorPool.borrow();
        //return new IntArrayList.ValueIterator(dense, size());
    }

    @Override
    public <T extends IntProcedure> T forEach(final T procedure)
    {
        final int max = size();
        final int[] dense = this.dense;
        for (int i = 0; i < max; i++)
        {
            procedure.apply(dense[i]);
        }

        return procedure;
    }

    @Override
    public <T extends IntPredicate> T forEach(final T predicate)
    {
        final int max = size();
        final int[] dense = this.dense;
        for (int i = 0; i < max; i++)
        {
            if (predicate.apply(dense[i])) {
                break;
            }
        }

        return predicate;
    }

    @Override
    public int removeAll(final IntLookupContainer c)
    {
        int max = size(), removed = 0;
        final int[] dense = this.dense;
        for (int i = 0; i < max;)
        {
            if (c.contains(dense[i])) {
                // Swap the last value with the removed value.
                final int lastValue = dense[--max];
                dense[i] = lastValue;
                this.sparse[lastValue] = i;
                removed++;
            }
            else {
                i++;
            }
        }
        this.elementsCount = max;
        return removed;
    }

    @Override
    public int removeAll(final IntPredicate predicate)
    {
        int max = size(), removed = 0;
        final int[] dense = this.dense;
        for (int i = 0; i < max;)
        {
            if (predicate.apply(dense[i])) {
                // Swap the last value with the removed value.
                final int lastValue = dense[--max];
                dense[i] = lastValue;
                this.sparse[lastValue] = i;
                removed++;
            }
            else {
                i++;
            }
        }
        this.elementsCount = max;
        return removed;
    }

    @Override
    public int retainAll(final IntLookupContainer c)
    {
        int max = size(), removed = 0;
        final int[] dense = this.dense;
        for (int i = 0; i < max;)
        {
            if (!c.contains(dense[i])) {
                // Swap the last value with the removed value.
                final int lastValue = dense[--max];
                dense[i] = lastValue;
                this.sparse[lastValue] = i;
                removed++;
            }
            else {
                i++;
            }
        }
        this.elementsCount = max;
        return removed;
    }

    /**
     * Create a set from a variable number of arguments or an array of <code>int</code>.
     * The elements are copied from the argument to the internal buffer.
     */
    public static DoubleLinkedIntSet from(final int... elements)
    {
        final DoubleLinkedIntSet set =
                new DoubleLinkedIntSet(elements.length, 1 + DoubleLinkedIntSet.maxElement(elements));
        for (final int i : elements) {
            set.addNoChecks(i);
        }
        return set;
    }

    /**
     * Create a set from elements of another container.
     */
    public static DoubleLinkedIntSet from(final IntContainer container)
    {
        return new DoubleLinkedIntSet(container);
    }

    /**
     * Static constructor-like method similar to other (generic) collections.
     */
    public static DoubleLinkedIntSet newInstance()
    {
        return new DoubleLinkedIntSet();
    }

    /**
     * Return the value of the maximum element (or zero) in a given container.
     */
    private static int maxElement(final IntContainer container)
    {
        int max = 0;
        for (final IntCursor c : container) {
            max = Math.max(max, c.value);
        }
        return max;
    }

    /**
     * Return the value of the maximum element (or zero) in a given container.
     */
    private static int maxElement(final int... elements)
    {
        int max = 0;
        for (final int c : elements) {
            max = Math.max(max, c);
        }
        return max;
    }

    /**
     * Clone this object.
     */
    @Override
    public DoubleLinkedIntSet clone()
    {
        try
        {
            final DoubleLinkedIntSet cloned = (DoubleLinkedIntSet) super.clone();
            cloned.dense = this.dense.clone();
            cloned.sparse = this.sparse.clone();
            return cloned;
        }
        catch (final CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
    }

    /*
     * 
     */
    @Override
    public String toString()
    {
        return Arrays.toString(this.toArray());
    }
}
