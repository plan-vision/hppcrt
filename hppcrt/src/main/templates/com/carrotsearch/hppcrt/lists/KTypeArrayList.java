package com.carrotsearch.hppcrt.lists;

import java.util.*;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.cursors.*;
import com.carrotsearch.hppcrt.hash.BitMixer;
import com.carrotsearch.hppcrt.predicates.*;
import com.carrotsearch.hppcrt.procedures.*;
import com.carrotsearch.hppcrt.sorting.*;
import com.carrotsearch.hppcrt.strategies.*;

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
/**
 * An array-backed list of KTypes. A single array is used to store and manipulate
 * all elements. Reallocations are governed by a {@link ArraySizingStrategy}
 * and may be expensive if they move around really large chunks of memory.
 * 
#if ($TemplateOptions.KTypeGeneric)
 * A brief comparison of the API against the Java Collections framework:
 * <table class="nice" summary="Java Collections ArrayList and HPPC ObjectArrayList, related methods.">
 * <caption>Java Collections ArrayList and HPPC {@link ObjectArrayList}, related methods.</caption>
 * <thead>
 * <tr class="odd">
 * <th scope="col">{@linkplain ArrayList java.util.ArrayList}</th>
 * <th scope="col">{@link ObjectArrayList}</th>
 * </tr>
 * </thead>
 * <tbody>
 * <tr            ><td>add            </td><td>add            </td></tr>
 * <tr class="odd"><td>add(index,v)   </td><td>insert(index,v)</td></tr>
 * <tr            ><td>get            </td><td>get            </td></tr>
 * <tr class="odd"><td>removeRange,
 *                     removeElementAt</td><td>removeRange, remove</td></tr>
 * <tr            ><td>remove(Object) </td><td>removeFirst, removeLast,
 *                                             removeAll</td></tr>
 * <tr class="odd"><td>clear          </td><td>clear, release </td></tr>
 * <tr            ><td>size           </td><td>size           </td></tr>
 * <tr class="odd"><td>ensureCapacity </td><td>ensureCapacity, resize</td></tr>
 * <tr            ><td>indexOf        </td><td>indexOf        </td></tr>
 * <tr class="odd"><td>lastIndexOf    </td><td>lastIndexOf    </td></tr>
 * <tr            ><td>trimToSize     </td><td>trimtoSize</td></tr>
 * <tr class="odd"><td>Object[] toArray()</td><td>KType[] toArray()</td></tr>
 * <tr            ><td>iterator       </td><td>{@linkplain #iterator() cursor over values}</td></tr>
 * </tbody>
 * </table>
#else
 * <p>See {@link ObjectArrayList} class for API similarities and differences against Java
 * Collections.
#end
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeArrayList<KType>
        extends AbstractKTypeCollection<KType> implements KTypeIndexedContainer<KType>, Cloneable
{
    /**
     * Internal array for storing the list. The array may be larger than the current size
     * ({@link #size()}).
     * 
     * <p>
     * Direct list iteration: iterate buffer[i] for i in [0; size()[
     * </p>
     */
    public/*! #if ($TemplateOptions.KTypePrimitive)
          KType []
          #else !*/
    Object[]
    /*! #end !*/
    buffer;

    /**
     * Current number of elements stored in {@link #buffer}.
     */
    protected int elementsCount;

    /**
     * Buffer resizing strategy.
     */
    protected final ArraySizingStrategy resizer;

    /**
     * internal pool of ValueIterator (must be created in constructor)
     */
    protected final IteratorPool<KTypeCursor<KType>, ValueIterator> valueIteratorPool;

    /**
     * Create with default sizing strategy and initial capacity for storing
     * {@value Containers#DEFAULT_EXPECTED_ELEMENTS} elements.
     * 
     * @see BoundedProportionalArraySizingStrategy
     */
    public KTypeArrayList() {
        this(Containers.DEFAULT_EXPECTED_ELEMENTS);
    }

    /**
     * Create with default sizing strategy and the given initial capacity.
     * 
     * @see BoundedProportionalArraySizingStrategy
     */
    public KTypeArrayList(final int initialCapacity) {
        this(initialCapacity, new BoundedProportionalArraySizingStrategy());
    }

    /**
     * Create with a custom buffer resizing strategy.
     */
    public KTypeArrayList(final int initialCapacity, final ArraySizingStrategy resizer) {
        assert resizer != null;

        this.resizer = resizer;

        //allocate internal buffer
        ensureBufferSpace(Math.max(Containers.DEFAULT_EXPECTED_ELEMENTS, initialCapacity));

        this.valueIteratorPool = new IteratorPool<KTypeCursor<KType>, ValueIterator>(new ObjectFactory<ValueIterator>() {

            @Override
            public ValueIterator create() {
                return new ValueIterator();
            }

            @Override
            public void initialize(final ValueIterator obj) {
                obj.cursor.index = -1;
                obj.size = KTypeArrayList.this.size();
                obj.buffer = Intrinsics.<KType[]> cast(KTypeArrayList.this.buffer);
            }

            @Override
            public void reset(final ValueIterator obj) {
                // for GC sake
                obj.buffer = null;

                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                obj.cursor.value = null;
                /*! #end !*/
            }
        });
    }

    /**
     * Creates a new list from elements of another container.
     */
    public KTypeArrayList(final KTypeContainer<? extends KType> container) {
        this(container.size());
        addAll(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final KType e1) {
        ensureBufferSpace(1);
        this.buffer[this.elementsCount++] = e1;
    }

    /**
     * Appends two elements at the end of the list. To add more than two elements,
     * use <code>add</code> (vararg-version) or access the buffer directly (tight
     * loop).
     */
    public void add(final KType e1, final KType e2) {
        ensureBufferSpace(2);
        this.buffer[this.elementsCount++] = e1;
        this.buffer[this.elementsCount++] = e2;
    }

    /**
     * Add all elements from a range of given array to the list.
     */
    public void add(final KType[] elements, final int start, final int length) {
        assert length >= 0 : "Length must be >= 0";

        ensureBufferSpace(length);
        System.arraycopy(elements, start, this.buffer, this.elementsCount, length);
        this.elementsCount += length;
    }

    /**
     * Vararg-signature method for adding elements at the end of the list.
     * <p><b>This method is handy, but costly if used in tight loops (anonymous
     * array passing)</b></p>
     */
    public void add(final KType... elements) {
        add(elements, 0, elements.length);
    }

    /**
     * Adds all elements from another container.
     */
    public int addAll(final KTypeContainer<? extends KType> container) {
        return addAll((Iterable<? extends KTypeCursor<? extends KType>>) container);
    }

    /**
     * Adds all elements from another iterable.
     */
    public int addAll(final Iterable<? extends KTypeCursor<? extends KType>> iterable) {
        int size = 0;
        for (final KTypeCursor<? extends KType> cursor : iterable) {
            add(cursor.value);
            size++;
        }
        return size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insert(final int index, final KType e1) {
        assert (index >= 0 && index <= size()) : "Index " + index + " out of bounds [" + 0 + ", " + size() + "].";

        ensureBufferSpace(1);
        System.arraycopy(this.buffer, index, this.buffer, index + 1, this.elementsCount - index);
        this.buffer[index] = e1;
        this.elementsCount++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KType get(final int index) {
        assert (index >= 0 && index < size()) : "Index " + index + " out of bounds [" + 0 + ", " + size() + "[.";

        return Intrinsics.<KType> cast(this.buffer[index]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KType set(final int index, final KType e1) {
        assert (index >= 0 && index < size()) : "Index " + index + " out of bounds [" + 0 + ", " + size() + "[.";

        final KType v = Intrinsics.<KType> cast(this.buffer[index]);
        this.buffer[index] = e1;
        return v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KType remove(final int index) {
        assert (index >= 0 && index < size()) : "Index " + index + " out of bounds [" + 0 + ", " + size() + "[.";

        final KType v = Intrinsics.<KType> cast(this.buffer[index]);
        if (index + 1 < this.elementsCount) {
            System.arraycopy(this.buffer, index + 1, this.buffer, index, this.elementsCount - index - 1);
        }
        this.elementsCount--;
        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        this.buffer[this.elementsCount] = Intrinsics.<KType> empty();
        /*! #end !*/
        return v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeRange(final int fromIndex, final int toIndex) {

        checkRangeBounds(fromIndex, toIndex);

        System.arraycopy(this.buffer, toIndex, this.buffer, fromIndex, this.elementsCount - toIndex);

        final int count = toIndex - fromIndex;
        this.elementsCount -= count;

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        KTypeArrays.blankArray(this.buffer, this.elementsCount, this.elementsCount + count);
        /*! #end !*/
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeFirst(final KType e1) {
        final int index = indexOf(e1);
        if (index >= 0) {
            remove(index);
        }
        return index;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeLast(final KType e1) {
        final int index = lastIndexOf(e1);
        if (index >= 0) {
            remove(index);
        }
        return index;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeAll(final KType e1) {
        int to = 0;
        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);

        for (int from = 0; from < this.elementsCount; from++) {
            if (Intrinsics.<KType> equals(e1, buffer[from])) {
                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                buffer[from] = Intrinsics.<KType> empty();
                /*! #end !*/
                continue;
            }

            if (to != from) {
                buffer[to] = buffer[from];
                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                buffer[from] = Intrinsics.<KType> empty();
                /*! #end !*/
            }
            to++;
        }

        final int deleted = this.elementsCount - to;
        this.elementsCount = to;
        return deleted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final KType e1) {
        return indexOf(e1) >= 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int indexOf(final KType e1) {
        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);

        for (int i = 0; i < this.elementsCount; i++) {
            if (Intrinsics.<KType> equals(e1, buffer[i])) {
                return i;
            }
        }

        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int lastIndexOf(final KType e1) {
        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);

        for (int i = this.elementsCount - 1; i >= 0; i--) {
            if (Intrinsics.<KType> equals(e1, buffer[i])) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Increases the capacity of this instance, if necessary, to ensure
     * that it can hold at least the number of elements specified by
     * the minimum capacity argument.
     */
    public void ensureCapacity(final int minCapacity) {
        if (minCapacity > this.buffer.length) {
            ensureBufferSpace(minCapacity - size());
        }
    }

    /**
     * Ensures the internal buffer has enough free slots to store
     * <code>expectedAdditions</code>. Increases internal buffer size if needed.
     */
    @SuppressWarnings("boxing")
    protected void ensureBufferSpace(final int expectedAdditions) {
        final int bufferLen = (this.buffer == null ? 0 : this.buffer.length);

        if (this.elementsCount > bufferLen - expectedAdditions) {
            final int newSize = this.resizer.grow(bufferLen, this.elementsCount, expectedAdditions);

            try {
                final KType[] newBuffer = Intrinsics.<KType> newArray(newSize);
                if (bufferLen > 0) {
                    System.arraycopy(this.buffer, 0, newBuffer, 0, this.buffer.length);
                }
                this.buffer = newBuffer;

            } catch (final OutOfMemoryError e) {
                throw new BufferAllocationException(
                        "Not enough memory to allocate buffers to grow from %d -> %d elements",
                        e,
                        bufferLen,
                        newSize);
            }
        }
    }

    /**
     * Truncate or expand the list to the new size. If the list is truncated, the buffer
     * will not be reallocated (use {@link #trimToSize()} if you need a truncated buffer).
     * If the list is expanded, the elements beyond the current size are initialized with JVM-defaults
     * (zero or <code>null</code> values).
     */
    public void resize(final int newSize) {
        if (newSize <= this.buffer.length) {
            if (newSize < this.elementsCount) {
                //there is no point in resetting to "null" elements
                //that becomes non-observable anyway. Still,
                //resetting is needed for GC in case of Objects because they may become "free"
                //if not referenced anywhere else.
                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                KTypeArrays.blankArray(this.buffer, newSize, this.elementsCount);
                /*! #end !*/
            } else {
                //in all cases, the contract of resize if that new elements
                //are set to default values.
                Arrays.fill(this.buffer, this.elementsCount, newSize, Intrinsics.<KType> empty());
            }
        } else {
            ensureCapacity(newSize);
        }

        this.elementsCount = newSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return this.elementsCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int capacity() {

        return this.buffer.length;
    }

    /**
     * Trim the internal buffer to the current size.
     */
    /* #if ($TemplateOptions.KTypeGeneric) */
    @SuppressWarnings({ "unchecked", "cast" })
    /* #end */
    public void trimToSize() {
        if (size() != this.buffer.length) {
            this.buffer = (KType[]) toArray();
        }
    }

    /**
     * Sets the number of stored elements to zero.
     */
    @Override
    public void clear() {
        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        KTypeArrays.blankArray(this.buffer, 0, this.elementsCount);
        /*! #end !*/
        this.elementsCount = 0;
    }

    /**
     * Sets the number of stored elements to zero and releases the internal storage array.
     */
    /* #if ($TemplateOptions.KTypeGeneric) */
    @SuppressWarnings({ "unchecked", "cast" })
    /* #end */
    public void release() {
        this.buffer = (KType[]) KTypeArrays.EMPTY;
        this.elementsCount = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KType[] toArray(final KType[] target) {
        System.arraycopy(this.buffer, 0, target, 0, this.elementsCount);
        return target;
    }

    /**
     * Clone this object.
     */
    @Override
    public KTypeArrayList<KType> clone() {
        //placeholder
        final KTypeArrayList<KType> cloned = new KTypeArrayList<KType>(Containers.DEFAULT_EXPECTED_ELEMENTS, this.resizer);

        //clone raw buffers
        cloned.buffer = this.buffer.clone();

        cloned.elementsCount = this.elementsCount;

        return cloned;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int h = 1;
        final int max = this.elementsCount;
        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);

        for (int i = 0; i < max; i++) {
            h = 31 * h + BitMixer.mix(buffer[i]);
        }
        return h;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    /* #if ($TemplateOptions.KTypeGeneric) */
    @SuppressWarnings("unchecked")
    /* #end */
    public boolean equals(final Object obj) {
        if (obj != null) {
            if (obj == this) {
                return true;
            }

            if (obj instanceof KTypeArrayList<?>) {
                final KTypeArrayList<?> other = (KTypeArrayList<?>) obj;
                return other.size() == this.size() && rangeEquals(other.buffer, this.buffer, size());
            } else if (obj instanceof KTypeIndexedContainer<?>) {
                final KTypeIndexedContainer<?> other = (KTypeIndexedContainer<?>) obj;
                return other.size() == this.size() && allIndexesEqual(this, (KTypeIndexedContainer<KType>) other, this.size());
            }
        }
        return false;
    }

    /**
     * Compare a range of values in two arrays.
     */
    /*! #if ($TemplateOptions.KTypePrimitive)
    private boolean rangeEquals(KType [] b1, KType [] b2, int length)
        #else !*/
    private boolean rangeEquals(final Object[] b1, final Object[] b2, final int length)
    /*! #end !*/
    {
        for (int i = 0; i < length; i++) {
            if (!Intrinsics.<KType> equals(b1[i], b2[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Compare index-aligned KTypeIndexedContainer objects
     */
    final protected boolean allIndexesEqual(final KTypeIndexedContainer<KType> b1, final KTypeIndexedContainer<KType> b2,
            final int length) {
        for (int i = 0; i < length; i++) {
            final KType o1 = b1.get(i);
            final KType o2 = b2.get(i);

            if (!Intrinsics.<KType> equals(o1, o2)) {
                return false;
            }
        }

        return true;
    }

    /**
     * An iterator implementation for {@link ObjectArrayList#iterator}.
     */
    public final class ValueIterator extends AbstractIterator<KTypeCursor<KType>>
    {
        public final KTypeCursor<KType> cursor;

        private KType[] buffer;
        private int size;

        public ValueIterator() {
            this.cursor = new KTypeCursor<KType>();
            this.cursor.index = -1;
            this.size = KTypeArrayList.this.size();
            this.buffer = Intrinsics.<KType[]> cast(KTypeArrayList.this.buffer);
        }

        @Override
        protected KTypeCursor<KType> fetch() {
            if (this.cursor.index + 1 == this.size) {
                return done();
            }

            this.cursor.value = this.buffer[++this.cursor.index];
            return this.cursor;
        }
    }

    /**
     * Returns an iterator over the values of this list.
     * The iterator is implemented as a cursor and it returns <b>the same cursor instance</b>
     * on every call to {@link Iterator#next()} (to avoid boxing of primitive types). To
     * read the current value, or index in the list's {@link #buffer} (which also matches index as in {@link #get(int)}), use the cursor's public
     * fields. An example is shown below.
     * 
     * <pre>
     * for (Iterator<IntCursor> i = intDeque.descendingIterator(); i.hasNext(); )
     * {
     *   final IntCursor c = i.next();
     *     System.out.println(&quot;buffer index=&quot;
     *         + c.index + &quot; value=&quot; + c.value);
     * }
     * </pre>
     */
    @Override
    public ValueIterator iterator() {
        //return new ValueIterator(buffer, size());
        return this.valueIteratorPool.borrow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends KTypeProcedure<? super KType>> T forEach(final T procedure) {
        return forEach(procedure, 0, size());
    }

    /**
     * Applies <code>procedure</code> to a slice of the list,
     * <code>fromIndex</code>, inclusive, to <code>toIndex</code>, exclusive.
     */
    @Override
    public <T extends KTypeProcedure<? super KType>> T forEach(final T procedure, final int fromIndex, final int toIndex) {

        checkRangeBounds(fromIndex, toIndex);

        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);

        for (int i = fromIndex; i < toIndex; i++) {
            procedure.apply(buffer[i]);
        }

        return procedure;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeAll(final KTypePredicate<? super KType> predicate) {
        final int elementsCount = this.elementsCount;
        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);

        int to = 0;
        int from = 0;
        try {
            for (; from < elementsCount; from++) {
                if (predicate.apply(buffer[from])) {
                    /*! #if ($TemplateOptions.KTypeGeneric) !*/
                    buffer[from] = Intrinsics.<KType> empty();
                    /*! #end !*/
                    continue;
                }

                if (to != from) {
                    buffer[to] = buffer[from];
                    /*! #if ($TemplateOptions.KTypeGeneric) !*/
                    buffer[from] = Intrinsics.<KType> empty();
                    /*! #end !*/
                }
                to++;
            }
        } finally {
            // Keep the list in a consistent state, even if the predicate throws an exception.
            for (; from < elementsCount; from++) {
                if (to != from) {
                    buffer[to] = buffer[from];
                    /*! #if ($TemplateOptions.KTypeGeneric) !*/
                    buffer[from] = Intrinsics.<KType> empty();
                    /*! #end !*/
                }
                to++;
            }

            this.elementsCount = to;
        }

        return elementsCount - to;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends KTypePredicate<? super KType>> T forEach(final T predicate) {
        return forEach(predicate, 0, size());
    }

    /**
     * Applies <code>predicate</code> to a slice of the list,
     * <code>fromIndex</code>, inclusive, to <code>toIndex</code>,
     * exclusive, or until predicate returns <code>false</code>.
     */
    @Override
    public <T extends KTypePredicate<? super KType>> T forEach(final T predicate, final int fromIndex, final int toIndex) {

        checkRangeBounds(fromIndex, toIndex);

        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);

        for (int i = fromIndex; i < toIndex; i++) {
            if (!predicate.apply(buffer[i])) {
                break;
            }
        }

        return predicate;
    }

    /**
     * Returns a new object of this class with no need to declare generic type (shortcut
     * instead of using a constructor).
     */
    public static/* #if ($TemplateOptions.KTypeGeneric) */<KType> /* #end */
            KTypeArrayList<KType> newInstance() {
        return new KTypeArrayList<KType>();
    }

    /**
     * Returns a new object of this class with no need to declare generic type (shortcut
     * instead of using a constructor).
     */
    public static/* #if ($TemplateOptions.KTypeGeneric) */<KType> /* #end */
            KTypeArrayList<KType> newInstance(final int initialCapacity) {
        return new KTypeArrayList<KType>(initialCapacity);
    }

    /**
     * Create a list from a variable number of arguments or an array of
     * <code>KType</code>.
     */
    public static/* #if ($TemplateOptions.KTypeGeneric) */<KType> /* #end */
            KTypeArrayList<KType> from(final KType... elements) {
        final KTypeArrayList<KType> list = new KTypeArrayList<KType>(elements.length);
        list.add(elements);
        return list;
    }

    /**
     * Create a list from elements of another container.
     */
    public static/* #if ($TemplateOptions.KTypeGeneric) */<KType> /* #end */
            KTypeArrayList<KType> from(final KTypeContainer<KType> container) {
        return new KTypeArrayList<KType>(container);
    }

    /**
     * In-place sort the list from [beginIndex, endIndex[
     * by natural ordering (smaller first)
     * @param beginIndex the start index to be sorted
     * @param endIndex the end index to be sorted (excluded)
    #if ($TemplateOptions.KTypeGeneric)
     * <p><b>
     * This sort is NOT stable.
     * </b></p>
     * @throws ClassCastException if the list contains elements that are not mutually Comparable.
    #end
     */
    public void sort(final int beginIndex, final int endIndex) {

        if (endIndex - beginIndex > 1) {
            KTypeSort.quicksort(this.buffer, beginIndex, endIndex);
        }
    }

    /**
     * In-place sort the whole list by natural ordering (smaller first)
     #if ($TemplateOptions.KTypeGeneric)
     * <p><b>
     * This sort is NOT stable.
     * </b></p>
     * @throws ClassCastException if the list contains elements that are not mutually Comparable.
    #end
     */
    public void sort() {
        sort(0, this.elementsCount);
    }

    ////////////////////////////
    /**
     * In-place sort the list from [beginIndex, endIndex[
     * using a #if ($TemplateOptions.KTypeGeneric) <code>Comparator</code> #else <code>KTypeComparator</code> #end
     #if ($TemplateOptions.KTypeGeneric)
     * <p><b>
     * This sort is NOT stable.
     * </b></p>
    #end
     * @param beginIndex the start index to be sorted
     * @param endIndex the end index to be sorted (excluded)
     */
    public void sort(final int beginIndex, final int endIndex,
            /*! #if ($TemplateOptions.KTypeGeneric) !*/
            final Comparator<? super KType>
            /*! #else
                            KTypeComparator<? super KType>
                            #end !*/
            comp) {

        if (endIndex - beginIndex > 1) {
            KTypeSort.quicksort(Intrinsics.<KType[]> cast(this.buffer), beginIndex, endIndex, comp);
        }
    }

    /**
     * In-place sort the whole list
     * using a #if ($TemplateOptions.KTypeGeneric) <code>Comparator</code> #else <code>KTypeComparator</code> #end
    #if ($TemplateOptions.KTypeGeneric)
     * <p><b>
     * This sort is NOT stable.
     * </b></p>
    #end
     */
    public void sort(
            /*! #if ($TemplateOptions.KTypeGeneric) !*/
            final Comparator<? super KType>
            /*! #else
            KTypeComparator<? super KType>
            #end !*/
            comp) {
        sort(0, this.elementsCount, comp);
    }

    private void checkRangeBounds(final int beginIndex, final int endIndex) {

        if (beginIndex >= endIndex) {

            throw new IllegalArgumentException("Index beginIndex " + beginIndex + " is >= endIndex " + endIndex);
        }

        if (beginIndex < 0 || beginIndex >= this.elementsCount) {

            throw new IndexOutOfBoundsException("Index beginIndex " + beginIndex + " out of bounds [" + 0 + ", " + this.elementsCount + "[.");
        }

        if (endIndex > this.elementsCount) {

            throw new IndexOutOfBoundsException("Index endIndex " + endIndex + " out of bounds [" + 0 + ", " + this.elementsCount + "].");
        }
    }
}
