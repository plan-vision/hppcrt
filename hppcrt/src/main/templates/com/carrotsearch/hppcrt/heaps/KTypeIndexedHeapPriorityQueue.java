package com.carrotsearch.hppcrt.heaps;

import java.util.*;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.cursors.*;
import com.carrotsearch.hppcrt.hash.BitMixer;
import com.carrotsearch.hppcrt.predicates.*;
import com.carrotsearch.hppcrt.procedures.*;
import com.carrotsearch.hppcrt.strategies.*;

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
/**
 * A Heap-based, indexed min-priority queue of <code>KType</code>s,
 * i.e. top() is the smallest element of the queue.
 * as defined by Sedgewick: Algorithms 4th Edition (2011).
 * This class is also a {@link IntKTypeMap}, and acts like a (K,V) = (int, KType) map with >= 0 keys.
 * It assures O(log(N)) complexity for insertion, deletion, updating priorities,
 * and constant time to examine the min element by {@link #top()} and for {@link #containsKey(int)}.
 * <p><b>Important: </b>
 * Ordering of elements must be defined either
 * #if ($TemplateOptions.KTypeGeneric)
 * by {@link Comparable}
 * #else
 * by natural ordering
 * #end
 *  or by a custom comparator provided in constructors,
 * see {@link #comparator()} .
 * 
 *<p><b>Warning : This implementation uses direct indexing, meaning that a map
 * at any given time is only able to have <code>int</code> keys in
 * the [0 ; {@link #capacity()}[ range. So when a {@link #put} occurs, the map may be resized to be able hold a key exceeding the current capacity.</b>
 * </p>
 */
/*! ${TemplateOptions.doNotGenerateKType("BOOLEAN")} !*/
/*! #set( $DEBUG = false) !*/
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeIndexedHeapPriorityQueue<KType> implements IntKTypeMap<KType>, Cloneable
{
    /**
     * Internal array for storing the priority queue.
     * <p>
     * Direct indexed priority queue iteration: iterate pq[i] for i in [0; pq.length[
     * and buffer[pq[i]] to get value where pq[i] > 0
     * </p>
     */
    public/*! #if ($TemplateOptions.KTypePrimitive)
          KType []
          #else !*/
    Object[]
            /*! #end !*/
            buffer;

    /**
     * Internal array for storing index to buffer position matching
     * i.e for an index i, pq[i] is the position of element in priority queue buffer.
     * <p>
     * Direct iteration: iterate pq[i] for indices i in [0; pq.length[
     * where pq[i] > 0, then buffer[pq[i]] is the value associated with index i.
     * </p>
     */
    public int[] pq;

    /**
     * Internal array pq inversing :
     * i.e for a priority buffer position pos, qp[pos] is the index of the value.,
     * ie qp[pq|i]] = i
     */
    public int[] qp;

    /**
     * Number of elements in the queue.
     */
    protected int elementsCount;

    /**
     * Defines the Comparator ordering of the queue,
     * If null, natural ordering is used.
     */
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    protected Comparator<? super KType> comparator;
    /*! #else
    protected KTypeComparator<? super KType> comparator;
    #end !*/

    protected KType defaultValue = Intrinsics.<KType> empty();

    /**
     * internal pool of EntryIterator (must be created in constructor)
     */
    protected final IteratorPool<IntKTypeCursor<KType>, EntryIterator> entryIteratorPool;

    /**
     * Create with a given initial capacity, using a
     * Comparator for ordering.
     */
    public KTypeIndexedHeapPriorityQueue(
            /*! #if ($TemplateOptions.KTypeGeneric) !*/final Comparator<? super KType> comp,
            /*! #else
            KTypeComparator<? super KType> comp,
            #end !*/final int initialCapacity) {
        this.comparator = comp;

        //1-based index buffer, assure allocation
        ensureBufferSpace(Math.max(Containers.DEFAULT_EXPECTED_ELEMENTS, initialCapacity));

        this.entryIteratorPool = new IteratorPool<IntKTypeCursor<KType>, EntryIterator>(new ObjectFactory<EntryIterator>() {

            @Override
            public EntryIterator create() {
                return new EntryIterator();
            }

            @Override
            public void initialize(final EntryIterator obj) {
                obj.cursor.index = 0;
                obj.buffer = Intrinsics.<KType[]> cast(KTypeIndexedHeapPriorityQueue.this.buffer);
                obj.size = KTypeIndexedHeapPriorityQueue.this.elementsCount;
                obj.qp = KTypeIndexedHeapPriorityQueue.this.qp;
            }

            @Override
            public void reset(final EntryIterator obj) {
                // for GC sake
                obj.qp = null;
                obj.buffer = null;
            }
        });
    }

    /**
     * Create with default sizing strategy and initial capacity for storing
     * {@value Containers#DEFAULT_EXPECTED_ELEMENTS} elements.
     * 
     * @see BoundedProportionalArraySizingStrategy
     */
    public KTypeIndexedHeapPriorityQueue(/*! #if ($TemplateOptions.KTypeGeneric) !*/final Comparator<? super KType> comp
            /*! #else
    KTypeComparator<? super KType> comp
    #end !*/)
    {
        this(comp, Containers.DEFAULT_EXPECTED_ELEMENTS);
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    /**
     * Create with an initial capacity,
     * using the Comparable natural ordering
     */
    /*! #else !*/
    /**
     * Create with an initial capacity,
     * using the natural ordering of <code>KType</code>s
     */
    /*! #end !*/
    public KTypeIndexedHeapPriorityQueue(final int initialCapacity) {
        this(null, initialCapacity);
    }

    /**
     * Create a indexed heap from all key-value pairs of another container.
     */
    public KTypeIndexedHeapPriorityQueue(final IntKTypeAssociativeContainer<KType> container) {
        this(container.size());
        putAll(container);
    }

    /**
     * Create a indexed heap from all key-value pairs of another container.. (constructor shortcut)
     */
    public static <KType> KTypeIndexedHeapPriorityQueue<KType> from(final IntKTypeAssociativeContainer<KType> container) {
        return new KTypeIndexedHeapPriorityQueue<KType>(container);
    }

    /**
     * Creates a Indexed heap from two index-aligned arrays of key-value pairs.
     */
    public static <KType> KTypeIndexedHeapPriorityQueue<KType> from(final int[] keys, final KType[] values) {
        if (keys.length != values.length) {
            throw new IllegalArgumentException("Arrays of keys and values must have an identical length.");
        }

        final KTypeIndexedHeapPriorityQueue<KType> heap = new KTypeIndexedHeapPriorityQueue<KType>(keys.length);

        for (int i = 0; i < keys.length; i++) {
            heap.put(keys[i], values[i]);
        }
        return heap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        //1-based indexing
        KTypeArrays.blankArray(this.buffer, 1, this.elementsCount + 1);
        /*! #end !*/

        //we need to init to zero, not -1 !!!
        Arrays.fill(this.pq, 0);

        //This is not really needed to reset this,
        //but is useful to catch inconsistencies in assertions
        /*! #if($DEBUG) !*/
        Arrays.fill(this.qp, -1);
        /*! #end !*/

        this.elementsCount = 0;
    }

    /**
     * An iterator implementation for {@link KTypeIndexedHeapPriorityQueue#iterator} entries.
     * Holds a IntKTypeCursor cursor returning (key, value, index) = (int key, KType value, index the position in heap)
     */
    public final class EntryIterator extends AbstractIterator<IntKTypeCursor<KType>>
    {
        public final IntKTypeCursor<KType> cursor;

        private KType[] buffer;
        private int size;
        private int[] qp;

        public EntryIterator() {
            this.cursor = new IntKTypeCursor<KType>();
            //index 0 is not used in Priority queue
            this.cursor.index = 0;
            this.buffer = Intrinsics.<KType[]> cast(KTypeIndexedHeapPriorityQueue.this.buffer);
            this.size = KTypeIndexedHeapPriorityQueue.this.size();
            this.qp = KTypeIndexedHeapPriorityQueue.this.qp;
        }

        @Override
        protected IntKTypeCursor<KType> fetch() {
            //priority is 1-based index
            if (this.cursor.index == this.size) {
                return done();
            }

            //this.cursor.index represent the position in the heap buffer.
            this.cursor.key = this.qp[++this.cursor.index];

            this.cursor.value = this.buffer[this.cursor.index];

            return this.cursor;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EntryIterator iterator() {
        return this.entryIteratorPool.borrow();
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
        return this.buffer.length - 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends IntKTypeProcedure<? super KType>> T forEach(final T procedure) {
        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);
        final int[] qp = this.qp;
        final int size = this.elementsCount;

        for (int pos = 1; pos <= size; pos++) {
            procedure.apply(qp[pos], buffer[pos]);
        }

        return procedure;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends IntKTypePredicate<? super KType>> T forEach(final T predicate) {
        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);
        final int[] qp = this.qp;
        final int size = this.elementsCount;

        for (int pos = 1; pos <= size; pos++) {
            if (!predicate.apply(qp[pos], buffer[pos])) {
                break;
            }
        }

        return predicate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return this.elementsCount == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeAll(final IntContainer container) {
        final int before = this.elementsCount;

        for (final IntCursor cursor : container) {
            remove(cursor.value);
        }

        return before - this.elementsCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeAll(final IntPredicate predicate) {
        final int[] pq = this.pq;
        final int size = this.pq.length;

        final int initialSize = this.elementsCount;

        //iterate keys, for all valid keys is OK because only the current pq[key] slot
        //is affected by the current remove() but the next ones are not.
        for (int key = 0; key < size; key++) {
            if (pq[key] > 0 && predicate.apply(key)) {
                remove(key);
            }
        } //end for

        /*! #if($DEBUG) !*/
        assert isMinHeap();
        assert isConsistent();
        /*! #end !*/

        return initialSize - this.elementsCount;
    }

    @Override
    public int removeAll(final IntKTypePredicate<? super KType> predicate) {

        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);
        final int[] pq = this.pq;
        final int size = this.elementsCount;

        final int initialSize = this.elementsCount;

        //iterate keys, for all valid keys is OK because only the current pq[key] slot
        //is affected by the current remove() but the next ones are not.
        for (int key = 0; key < size; key++) {
            final int pos = pq[key];

            if (pos > 0 && predicate.apply(key, buffer[pos])) {
                remove(key);
            }
        } //end for

        /*! #if($DEBUG) !*/
        assert isMinHeap();
        assert isConsistent();
        /*! #end !*/

        return initialSize - this.elementsCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean putIfAbsent(final int key, final KType value) {
        if (!containsKey(key)) {
            put(key, value);
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int putAll(final IntKTypeAssociativeContainer<? extends KType> container) {
        return putAll((Iterable<? extends IntKTypeCursor<? extends KType>>) container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int putAll(final Iterable<? extends IntKTypeCursor<? extends KType>> iterable) {
        final int count = this.elementsCount;

        for (final IntKTypeCursor<? extends KType> c : iterable) {
            put(c.key, c.value);
        }

        return this.elementsCount - count;
    }

    /**
     * {@inheritDoc}
     * cost: O(log(N)) for a N sized queue
     * <p><b>Important: </b>
     * Whenever a new (key, value) pair is inserted, or
     * a value is updated with an already present key as specified by the  {@link IntKTypeMap#put}
     * contract, the inserted value priority is always consistent towards the comparison criteria.
     * In other words, there is no need to call {@link #updatePriority(int)} after a {@link #put}.
     * @param key the integer key, must be >= 0
     * @param element the associated value
     */
    @Override
    public KType put(final int key, final KType element) {
        assert key >= 0 : "Keys must be >= 0";

        //1) Key already present, insert new value
        if (key < this.pq.length && this.pq[key] > 0) {
            //element already exists : insert brutally at the same position in buffer and refresh the priorities to reestablish heap
            final KType previousValue = Intrinsics.<KType> cast(this.buffer[this.pq[key]]);

            this.buffer[this.pq[key]] = element;

            //re-establish heap
            sink(this.pq[key]);
            swim(this.pq[key]);

            /*! #if($DEBUG) !*/
            assert isMinHeap();
            assert isConsistent();
            /*! #end !*/

            return previousValue;
        }

        //2) not present, add at the end
        // 2-1) pq must be sufficient to receive index by direct indexing,
        //resize if needed.
        ensureBufferSpace(key);

        //2-2) Add
        this.elementsCount++;
        final int count = this.elementsCount;

        this.buffer[count] = element;

        //initial position
        this.pq[key] = count;
        this.qp[count] = key;

        //swim last element
        swim(count);

        return this.defaultValue;
    }

    /*! #if ($TemplateOptions.KTypeNumeric) !*/
    /**
     * If <code>key</code> exists, <code>putValue</code> is inserted into the map,
     * otherwise any existing value is incremented by <code>additionValue</code>.
     * 
     * @param key
     *          The key of the value to adjust.
     * @param putValue
     *          The value to put if <code>key</code> does not exist.
     * @param incrementValue
     *          The value to add to the existing value if <code>key</code> exists.
     * @return Returns the current value associated with <code>key</code> (after
     *         changes).
     */
    @Override
    public KType putOrAdd(final int key, KType putValue, final KType incrementValue) {

        if (containsKey(key)) {
            putValue = get(key);

            putValue = (KType) (Intrinsics.<KType> add(putValue, incrementValue));
        }

        put(key, putValue);
        return putValue;
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.KTypeNumeric) !*/
    /**
     * Adds <code>incrementValue</code> to any existing value for the given <code>key</code>
     * or inserts <code>incrementValue</code> if <code>key</code> did not previously exist.
     * 
     * @param key The key of the value to adjust.
     * @param incrementValue The value to put or add to the existing value if <code>key</code> exists.
     * @return Returns the current value associated with <code>key</code> (after changes).
     */
    @Override
    public KType addTo(final int key, final KType incrementValue)
    {
        return putOrAdd(key, incrementValue, incrementValue);
    }

    /*! #end !*/

    /**
     * Retrieve, but not remove, the top element of the queue,
     * i.e. the min. element with respect to the comparison criteria
     * of the queue. Returns the default value if empty.
     * cost: O(1)
     */
    public KType top() {
        KType elem = this.defaultValue;

        if (this.elementsCount > 0) {
            elem = Intrinsics.<KType> cast(this.buffer[1]);
        }

        return elem;
    }

    /**
     * Retrieve the key corresponding to the top element of the queue,
     * i.e. the min element with respect to the comparison criteria
     * of the queue. Returns -1 if empty.
     * cost: O(1)
     */
    public int topKey() {
        int key = -1;

        if (this.elementsCount > 0) {
            key = this.qp[1];
        }

        return key;
    }

    /**
     * Retrieve, and remove the top element of the queue,
     * i.e. the min/max element with respect to the comparison criteria
     * (implementation defined) Returns the default value if empty.
     * cost: O(log(N)) for a N sized queue
     */
    public KType popTop() {
        KType elem = this.defaultValue;

        if (this.elementsCount > 0) {
            elem = Intrinsics.<KType> cast(this.buffer[1]);

            remove(this.qp[1]);
        }

        return elem;
    }

    /**
     * {@inheritDoc}
     * cost: O(1)
     */
    @Override
    public KType get(final int key) {
        /*! #if($DEBUG) !*/
        assert key >= this.pq.length || this.pq[key] > 0 : "Element of index " + key + " doesn't exist ! (size="
                + this.elementsCount + ")";
        /*! #end !*/

        KType elem = this.defaultValue;

        if (key < this.pq.length && this.pq[key] > 0) {
            elem = Intrinsics.<KType> cast(this.buffer[this.pq[key]]);
        }

        return elem;
    }

    /**
     * {@inheritDoc} cost: O(log(N))
     */
    @SuppressWarnings("boxing")
    @Override
    public KType remove(final int key) {
        KType deletedElement = this.defaultValue;

        final int[] qp = this.qp;
        final int[] pq = this.pq;
        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);

        if (key < pq.length && pq[key] > 0) {
            final int deletedPos = pq[key];
            deletedElement = buffer[deletedPos];

            if (deletedPos == this.elementsCount) {
                //we remove the last element
                pq[key] = 0;

                //for GC
                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                buffer[deletedPos] = Intrinsics.<KType> empty();
                /*! #end !*/

                //Not really needed, but usefull to catch inconsistencies
                /*! #if($DEBUG) !*/
                qp[deletedPos] = -1;
                /*! #end !*/

                //diminuish size
                this.elementsCount--;
            } else {
                //We are not removing the last element

                /*! #if($DEBUG) !*/
                assert deletedPos > 0 && qp[deletedPos] == key : String.format("pq[key] = %d, qp[pq[key]] = %d (key = %d)",
                        deletedPos, qp[deletedPos], key);
                /*! #end !*/

                final int lastElementIndex = qp[this.elementsCount];

                /*! #if($DEBUG) !*/
                assert lastElementIndex >= 0 && pq[lastElementIndex] == this.elementsCount : String.format(
                        "lastElementIndex = qp[elementsCount] = %d, pq[lastElementIndex] = %d, elementsCount = %d",
                        lastElementIndex, pq[lastElementIndex], this.elementsCount);
                /*! #end !*/

                //not needed, overwritten below :
                /*! #if($DEBUG) !*/
                qp[deletedPos] = -1;
                /*! #end !*/

                buffer[deletedPos] = buffer[this.elementsCount];
                //last element is now at pos deletedPos
                pq[lastElementIndex] = deletedPos;
                qp[deletedPos] = lastElementIndex;

                //mark the index element to be removed
                //we must reset with 0 so that qp[pq[index]] is always valid !
                pq[key] = 0;

                //Not really needed, but usefull to catch inconsistencies
                /*! #if($DEBUG) !*/
                qp[this.elementsCount] = -1;
                /*! #end !*/

                //for GC
                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                buffer[this.elementsCount] = Intrinsics.<KType> empty();
                /*! #end !*/

                //diminuish size
                this.elementsCount--;

                //after swapping positions
                /*! #if($DEBUG) !*/
                assert pq[lastElementIndex] == deletedPos : String.format(
                        "pq[lastElementIndex = %d] = %d, while deletedPos = %d, (key = %d)", lastElementIndex,
                        pq[lastElementIndex], deletedPos, key);

                assert qp[deletedPos] == lastElementIndex : String.format(
                        "qp[deletedPos = %d] = %d, while lastElementIndex = %d, (key = %d)", deletedPos, qp[deletedPos],
                        lastElementIndex, key);
                /*! #end !*/

                if (this.elementsCount > 1) {
                    //re-establish heap
                    sink(pq[lastElementIndex]);
                    swim(pq[lastElementIndex]);
                }
            }
        }

        return deletedElement;
    }

    /**
     * Update the priority of the value associated with key, to re-establish the value correct priority
     * towards the comparison criteria.
     * cost: O(log(N))
     */
    public void updatePriority(final int key) {
        if (key < this.pq.length && this.pq[key] > 0) {
            swim(this.pq[key]);
            sink(this.pq[key]);
        }
    }

    /**
     * Update the priority of the {@link #top()} element, to re-establish its actual priority
     * towards the comparison criteria when it may have changed such that it is no longer the
     *  min element with respect to the comparison criteria.
     * cost: O(log(N))
     */
    public void updateTopPriority() {
        //only attempt to sink if there is at least 2 elements....
        if (this.elementsCount > 1) {

            sink(1);
        }
    }

    /**
     * {@inheritDoc}
     * cost: O(1)
     */
    @Override
    public boolean containsKey(final int key) {
        if (key < this.pq.length && this.pq[key] > 0) {
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int h = 1;
        final int size = this.pq.length;
        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);
        final int[] pq = this.pq;

        //iterate by (ordered) index to have a reproducible hash and
        //so keeping a multiplicative quality
        for (int index = 0; index < size; index++) {
            if (pq[index] > 0) {
                //rehash of the index
                h = 31 * h + BitMixer.mix(index);
                //append the rehash of the value
                h = 31 * h + BitMixer.mix(buffer[pq[index]]);
            }
        }

        return h;
    }

    /**
     * this instance and obj can only be equal if either: <pre>
     * (both don't have set comparators)
     * or
     * (both have equal comparators defined by {@link #comparator()}.equals(obj.comparator))</pre>
     * then, both heaps are compared as follows: <pre>
     * {@inheritDoc}</pre>
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

            //we can only compare both KTypeHeapPriorityQueue and not subclasses between themselves
            //that has the same comparison function reference
            if (obj.getClass() != this.getClass()) {
                return false;
            }

            final KTypeIndexedHeapPriorityQueue<KType> other = (KTypeIndexedHeapPriorityQueue<KType>) obj;

            if (other.size() != this.size()) {
                return false;
            }

            //Iterate over the smallest pq buffer of the two.
            int[] pqBuffer, otherPqBuffer;
            KType[] buffer, otherBuffer;

            if (this.pq.length < other.pq.length) {
                pqBuffer = this.pq;
                otherPqBuffer = other.pq;
                buffer = Intrinsics.<KType[]> cast(this.buffer);
                otherBuffer = Intrinsics.<KType[]> cast(other.buffer);
            } else {
                pqBuffer = other.pq;
                otherPqBuffer = this.pq;
                buffer = Intrinsics.<KType[]> cast(other.buffer);
                otherBuffer = Intrinsics.<KType[]> cast(this.buffer);
            }

            final int pqBufferSize = pqBuffer.length;
            final KType currentValue, otherValue;
            int currentIndex, otherIndex;

            //Both have null comparators
            if (this.comparator == null && other.comparator == null) {
                for (int i = 0; i < pqBufferSize; i++) {
                    currentIndex = pqBuffer[i];

                    if (currentIndex > 0) {
                        //check that currentIndex exists in otherBuffer at the same i
                        otherIndex = otherPqBuffer[i];

                        if (otherIndex <= 0) {
                            return false;
                        }

                        //compare both elements with Comparable, or natural ordering
                        if (!Intrinsics.<KType> isCompEqualUnchecked(buffer[currentIndex], otherBuffer[otherIndex])) {
                            return false;
                        }
                    }
                }

                return true;

            } else if (this.comparator != null && this.comparator.equals(other.comparator)) {

                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                final Comparator<? super KType> comp = this.comparator;
                /*! #else
                KTypeComparator<? super KType> comp = this.comparator;
                #end !*/

                for (int i = 0; i < pqBufferSize; i++) {
                    currentIndex = pqBuffer[i];

                    if (currentIndex > 0) {
                        //check that currentIndex exists in otherBuffer
                        otherIndex = otherPqBuffer[i];

                        if (otherIndex <= 0) {
                            return false;
                        }

                        //compare both elements with Comparator
                        if (comp.compare(buffer[i], otherBuffer[i]) != 0) {
                            return false;
                        }
                    }
                } //end for

                return true;
            } //end else comparator
        }

        return false;
    }

    /**
     * Clone this object. The returned clone will use the same comparator.
     */
    @Override
    public KTypeIndexedHeapPriorityQueue<KType> clone() {
        //placeholder container
        final KTypeIndexedHeapPriorityQueue<KType> cloned = new KTypeIndexedHeapPriorityQueue<KType>(this.comparator,
                Containers.DEFAULT_EXPECTED_ELEMENTS);

        //clone raw buffers
        cloned.buffer = this.buffer.clone();
        cloned.pq = this.pq.clone();
        cloned.qp = this.qp.clone();

        cloned.defaultValue = this.defaultValue;
        cloned.elementsCount = this.elementsCount;

        return cloned;
    }

    /**
     * Update priorities of all the elements of the queue, to re-establish the
     * correct priorities towards the comparison criteria. cost: O(n*log(N))
     */
    public void updatePriorities() {
        if (this.comparator == null) {
            for (int k = this.elementsCount >> 1; k >= 1; k--) {
                sinkComparable(k);
            }
        } else {
            for (int k = this.elementsCount >> 1; k >= 1; k--) {
                sinkComparator(k);
            }
        }
    }

    /**
     * @return a new KeysCollection view of the keys of this associated container.
     *         This view then reflects all changes from the heap.
     */
    @Override
    public KeysCollection keys() {
        return new KeysCollection();
    }

    /**
     * A view of the keys inside this Indexed heap.
     */
    public final class KeysCollection extends AbstractIntCollection implements IntLookupContainer
    {
        private final KTypeIndexedHeapPriorityQueue<KType> owner = KTypeIndexedHeapPriorityQueue.this;

        @Override
        public boolean contains(final int e) {
            return this.owner.containsKey(e);
        }

        @Override
        public <T extends IntProcedure> T forEach(final T procedure) {
            final int[] qp = this.owner.qp;
            final int size = this.owner.elementsCount;

            for (int pos = 1; pos <= size; pos++) {
                procedure.apply(qp[pos]);
            }

            return procedure;
        }

        @Override
        public <T extends IntPredicate> T forEach(final T predicate) {
            final int[] qp = this.owner.qp;
            final int size = this.owner.elementsCount;

            for (int pos = 1; pos <= size; pos++) {
                if (!predicate.apply(qp[pos])) {
                    break;
                }
            }

            return predicate;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public KeysIterator iterator() {
            //return new KeysIterator();
            return this.keyIteratorPool.borrow();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int size() {
            return this.owner.size();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int capacity() {

            return this.owner.capacity();
        }

        @Override
        public void clear() {
            this.owner.clear();
        }

        @Override
        public int removeAll(final IntPredicate predicate) {
            return this.owner.removeAll(predicate);
        }

        @Override
        public int removeAll(final int e) {
            final boolean hasKey = this.owner.containsKey(e);
            int result = 0;
            if (hasKey) {
                this.owner.remove(e);
                result = 1;
            }
            return result;
        }

        @Override
        public int[] toArray(final int[] target) {
            int count = 0;
            final int[] pq = this.owner.pq;
            final int size = this.owner.pq.length;

            for (int key = 0; key < size; key++) {

                if (pq[key] > 0) {

                    target[count] = key;
                    count++;
                }
            }

            return target;
        }

        /**
         * internal pool of KeysIterator
         */
        protected final IteratorPool<IntCursor, KeysIterator> keyIteratorPool = new IteratorPool<IntCursor, KeysIterator>(
                new ObjectFactory<KeysIterator>() {

                    @Override
                    public KeysIterator create() {
                        return new KeysIterator();
                    }

                    @Override
                    public void initialize(final KeysIterator obj) {
                        obj.cursor.value = -1;
                        obj.pq = KTypeIndexedHeapPriorityQueue.this.pq;
                    }

                    @Override
                    public void reset(final KeysIterator obj) {
                        //no dangling references
                        obj.pq = null;
                    }
                });

    };

    /**
     * An iterator over the set of assigned keys.
     * Holds a IntCursor cursor returning (value, index) = (int key, index the position in heap)
     */
    public final class KeysIterator extends AbstractIterator<IntCursor>
    {
        public final IntCursor cursor;

        private int[] pq;

        public KeysIterator() {
            this.cursor = new IntCursor();

            this.cursor.value = -1;
            this.pq = KTypeIndexedHeapPriorityQueue.this.pq;
        }

        /**
         * 
         */
        @Override
        protected IntCursor fetch() {
            //iterate next() : first iteration starts indeed at 0
            int i = this.cursor.value + 1;

            while (i < this.pq.length && this.pq[i] <= 0) {
                i++;
            }

            /*! #if($DEBUG) !*/
            assert i <= this.pq.length : i + "|" + this.pq.length;
            /*! #end !*/

            if (i == this.pq.length) {

                return done();
            }

            //the cursor index corresponds to the position in heap buffer
            this.cursor.value = i;
            this.cursor.index = this.pq[i];

            return this.cursor;
        }
    }

    /**
     * @return a new ValuesCollection, view of the values of this indexed heap.
     *         This view then reflects all changes from the heap.
     */
    @Override
    public ValuesCollection values() {
        return new ValuesCollection();
    }

    /**
     * A view over the set of values of this map.
     */
    public final class ValuesCollection extends AbstractKTypeCollection<KType>
    {
        private final KTypeIndexedHeapPriorityQueue<KType> owner = KTypeIndexedHeapPriorityQueue.this;

        private KType currentOccurenceToBeRemoved;

        private final KTypePredicate<? super KType> removeAllOccurencesPredicate = new KTypePredicate<KType>() {

            @Override
            public final boolean apply(final KType value) {

                if (ValuesCollection.this.owner.comparator == null) {

                    if (Intrinsics.<KType> isCompEqualUnchecked(value, ValuesCollection.this.currentOccurenceToBeRemoved)) {

                        return true;
                    }

                } else {

                    if (ValuesCollection.this.owner.comparator.compare(value, ValuesCollection.this.currentOccurenceToBeRemoved) == 0) {

                        return true;
                    }
                }

                return false;
            }
        };

        /**
         * {@inheritDoc}
         */
        @Override
        public int size() {
            return this.owner.size();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int capacity() {
            return this.owner.capacity();
        }

        /**
         * {@inheritDoc}
         * <p><b>Note : </b> The comparison criteria for
         * identity test is based on
         * #if ($TemplateOptions.KTypeGeneric)
         * {@link Comparable} compareTo() if no
         * #else
         * natural ordering if no
         * #end
         * custom comparator is given, else it uses the {@link #comparator()} criteria.
         */
        @Override
        public boolean contains(final KType value) {
            final KType[] buffer = Intrinsics.<KType[]> cast(this.owner.buffer);
            final int size = this.owner.elementsCount;

            if (this.owner.comparator == null) {

                //iterate the heap buffer, use the natural comparison criteria
                for (int pos = 1; pos <= size; pos++) {
                    if (Intrinsics.<KType> isCompEqualUnchecked(buffer[pos], value)) {
                        return true;
                    }
                }
            } else {

                //use the dedicated comparator
                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                final Comparator<? super KType> comp = this.owner.comparator;
                /*! #else
                        KTypeComparator<? super KType> comp = this.owner.comparator;
                        #end !*/
                for (int pos = 1; pos <= size; pos++) {
                    if (comp.compare(Intrinsics.<KType> cast(KTypeIndexedHeapPriorityQueue.this.buffer[pos]), value) == 0) {
                        return true;
                    }
                }
            } //end else

            return false;
        }

        @Override
        public <T extends KTypeProcedure<? super KType>> T forEach(final T procedure) {
            final KType[] buffer = Intrinsics.<KType[]> cast(this.owner.buffer);
            final int size = this.owner.elementsCount;

            //iterate the heap buffer, use the natural comparison criteria
            for (int pos = 1; pos <= size; pos++) {
                procedure.apply(buffer[pos]);
            }

            return procedure;
        }

        @Override
        public <T extends KTypePredicate<? super KType>> T forEach(final T predicate) {
            final KType[] buffer = Intrinsics.<KType[]> cast(this.owner.buffer);
            final int size = this.owner.elementsCount;

            //iterate the heap buffer, use the natural comparison criteria
            for (int pos = 1; pos <= size; pos++) {
                if (!predicate.apply(buffer[pos])) {
                    break;
                }
            }

            return predicate;
        }

        @Override
        public ValuesIterator iterator() {
            // return new ValuesIterator();
            return this.valuesIteratorPool.borrow();
        }

        /**
         * {@inheritDoc}
         * Indeed removes all the (key,value) pairs matching
         * (key ? ,  e) with the  same  e,  from  the map.
         * <p><b>Note : </b> The comparison criteria for
         * identity test is based on
         * !#if ($TemplateOptions.KTypeGeneric)
         * {@link Comparable} compareTo() if no
         *  #else
         * natural ordering if no
         *  #end
         * custom comparator is given, else it uses the {@link #comparator()} criteria.
         */
        @Override
        public int removeAll(final KType e) {
            this.currentOccurenceToBeRemoved = e;
            return this.owner.removeAllInternal(this.removeAllOccurencesPredicate);
        }

        /**
         * {@inheritDoc}
         * Indeed removes all the (key,value) pairs matching
         * the predicate for the values, from  the map.
         */
        @Override
        public int removeAll(final KTypePredicate<? super KType> predicate) {
            return this.owner.removeAllInternal(predicate);
        }

        /**
         * {@inheritDoc}
         *  Alias for clear() the whole map.
         */
        @Override
        public void clear() {
            this.owner.clear();
        }

        @Override
        public KType[] toArray(final KType[] target) {
            //buffer validity starts at 1
            System.arraycopy(this.owner.buffer, 1, target, 0, this.owner.elementsCount);

            return target;
        }

        /**
         * internal pool of ValuesIterator
         */
        protected final IteratorPool<KTypeCursor<KType>, ValuesIterator> valuesIteratorPool = new IteratorPool<KTypeCursor<KType>, ValuesIterator>(
                new ObjectFactory<ValuesIterator>() {

                    @Override
                    public ValuesIterator create() {
                        return new ValuesIterator();
                    }

                    @Override
                    public void initialize(final ValuesIterator obj) {
                        obj.cursor.index = 0;
                        obj.buffer = Intrinsics.<KType[]> cast(KTypeIndexedHeapPriorityQueue.this.buffer);
                        obj.size = KTypeIndexedHeapPriorityQueue.this.size();
                    }

                    @Override
                    public void reset(final ValuesIterator obj) {
                        obj.buffer = null;
                    }
                });

    }

    /**
     * An iterator over the set of assigned values.
     * Holds a KTypeCursor cursor returning (value, index) = (KType value, index the position in heap)
     */
    public final class ValuesIterator extends AbstractIterator<KTypeCursor<KType>>
    {
        public final KTypeCursor<KType> cursor;

        private KType[] buffer;
        private int size;

        public ValuesIterator() {
            this.cursor = new KTypeCursor<KType>();

            //index 0 is not used in Priority queue
            this.cursor.index = 0;
            this.buffer = Intrinsics.<KType[]> cast(KTypeIndexedHeapPriorityQueue.this.buffer);
            this.size = size();
        }

        @Override
        protected KTypeCursor<KType> fetch() {
            //priority is 1-based index
            if (this.cursor.index == this.size) {
                return done();
            }

            //this.cursor.index represent the position in the heap buffer.
            this.cursor.value = this.buffer[++this.cursor.index];

            return this.cursor;
        }
    }

    @Override
    public String toString() {
        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);
        final int[] pq = this.pq;

        final StringBuilder buff = new StringBuilder();
        buff.append("[");

        boolean first = true;

        //Indices are displayed in ascending order, for easier reading.
        for (int i = 0; i < pq.length; i++) {
            if (pq[i] > 0) {
                if (!first) {
                    buff.append(", ");
                }

                buff.append(i);
                buff.append("=>");
                buff.append(buffer[pq[i]]);
                first = false;
            }
        }

        buff.append("]");
        return buff.toString();
    }

    /**
     * Returns the "default value" value used
     * in methods returning "default value"
     */
    public KType getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Set the "default value" value to be used
     * in methods returning the "default value"
     */
    public void setDefaultValue(final KType defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Get the custom comparator used for comparing elements
     * @return null if no custom comparator was set, i.e natural ordering
     * of <code>KType</code>s is used instead
     * #if ($TemplateOptions.KTypeGeneric) , which means objects in this case must be {@link Comparable}. #end
     */
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    public Comparator<? super KType>
    /*! #else
                                                                    public KTypeComparator<? super KType>
                                                                    #end !*/
    comparator() {

        return this.comparator;
    }

    /**
     * Ensures the internal buffer has enough free slots to accommodate the index
     * <code>index</code>. Increases internal buffer size if needed.
     */
    @SuppressWarnings("boxing")
    protected void ensureBufferSpace(final int index) {
        final int pqLen = this.pq == null ? 0 : this.pq.length;

        if (index > pqLen - 1) {
            //resize to accomodate this index: use a 50% grow to mitigate when the user
            //has not presized properly the container.
            final int newPQSize = Math.max(index + Containers.DEFAULT_EXPECTED_ELEMENTS, (int) (index * 1.5));

            try {
                final int[] newPQIndex = new int[newPQSize];
                final KType[] newBuffer = Intrinsics.<KType> newArray(newPQSize + 1);
                final int[] newQPIndex = new int[newPQSize + 1];

                if (pqLen > 0) {
                    System.arraycopy(this.buffer, 0, newBuffer, 0, this.buffer.length);
                    System.arraycopy(this.pq, 0, newPQIndex, 0, this.pq.length);
                    System.arraycopy(this.qp, 0, newQPIndex, 0, this.qp.length);
                }
                this.buffer = newBuffer;
                this.pq = newPQIndex;
                this.qp = newQPIndex;

            } catch (final OutOfMemoryError e) {

                throw new BufferAllocationException("Not enough memory to allocate buffers to grow from %d -> %d elements", e,
                        pqLen, newPQSize);
            }
        } //end if
    }

    /**
     * Sink function for Comparable elements
     * 
     * @param k
     */
    private void sinkComparable(int k) {
        final int N = this.elementsCount;
        KType tmp;
        int child;
        int indexK, indexChild;

        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);
        final int[] pq = this.pq;
        final int[] qp = this.qp;

        while (k << 1 <= N) {
            //get the child of k
            child = k << 1;

            if (child < N && Intrinsics.<KType> isCompSupUnchecked(buffer[child], buffer[child + 1])) {
                child++;
            }

            if (!Intrinsics.<KType> isCompSupUnchecked(buffer[k], buffer[child])) {
                break;
            }

            //swap k and child
            tmp = buffer[k];
            buffer[k] = buffer[child];
            buffer[child] = tmp;

            //swap references
            indexK = qp[k];
            indexChild = qp[child];

            pq[indexK] = child;
            pq[indexChild] = k;

            qp[k] = indexChild;
            qp[child] = indexK;

            k = child;
        } //end while
    }

    /**
     * Sink function for KTypeComparator elements
     * 
     * @param k
     */
    private void sinkComparator(int k) {
        final int N = this.elementsCount;
        KType tmp;
        int child;
        int indexK, indexChild;

        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);
        final int[] pq = this.pq;
        final int[] qp = this.qp;

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        final Comparator<? super KType> comp = this.comparator;
        /*! #else
        KTypeComparator<? super KType> comp = this.comparator;
        #end !*/

        while (k << 1 <= N) {
            //get the child of k
            child = k << 1;

            if (child < N && comp.compare(buffer[child], buffer[child + 1]) > 0) {
                child++;
            }

            if (comp.compare(buffer[k], buffer[child]) <= 0) {
                break;
            }

            //swap k and child
            tmp = buffer[k];
            buffer[k] = buffer[child];
            buffer[child] = tmp;

            //swap references
            indexK = qp[k];
            indexChild = qp[child];

            pq[indexK] = child;
            pq[indexChild] = k;

            qp[k] = indexChild;
            qp[child] = indexK;

            k = child;
        } //end while
    }

    /**
     * Swim function for Comparable elements
     * 
     * @param k
     */
    private void swimComparable(int k) {
        KType tmp;
        int parent;
        int indexK, indexParent;

        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);
        final int[] pq = this.pq;
        final int[] qp = this.qp;

        while (k > 1 && Intrinsics.<KType> isCompSupUnchecked(buffer[k >> 1], buffer[k])) {
            //swap k and its parent
            parent = k >> 1;

        //swap k and parent
        tmp = buffer[k];
        buffer[k] = buffer[parent];
        buffer[parent] = tmp;

        //swap references
        indexK = qp[k];
        indexParent = qp[parent];

        pq[indexK] = parent;
        pq[indexParent] = k;

        qp[k] = indexParent;
        qp[parent] = indexK;

        k = parent;
        }
    }

    /**
     * Swim function for Comparator elements
     * 
     * @param k
     */
    private void swimComparator(int k) {
        KType tmp;
        int parent;
        int indexK, indexParent;

        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);
        final int[] pq = this.pq;
        final int[] qp = this.qp;

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        final Comparator<? super KType> comp = this.comparator;
        /*! #else
        KTypeComparator<? super KType> comp = this.comparator;
        #end !*/

        while (k > 1 && comp.compare(buffer[k >> 1], buffer[k]) > 0) {
            //swap k and its parent
            parent = k >> 1;

        //swap k and parent
        tmp = buffer[k];
        buffer[k] = buffer[parent];
        buffer[parent] = tmp;

        //swap references
        indexK = qp[k];
        indexParent = qp[parent];

        pq[indexK] = parent;
        pq[indexParent] = k;

        qp[k] = indexParent;
        qp[parent] = indexK;

        k = parent;
        }
    }

    private void swim(final int k) {
        if (this.comparator == null) {
            swimComparable(k);
        } else {
            swimComparator(k);
        }
    }

    private void sink(final int k) {
        if (this.comparator == null) {
            sinkComparable(k);
        } else {
            sinkComparator(k);
        }
    }

    private int removeAllInternal(final KTypePredicate<? super KType> predicate) {
        //remove by position
        int deleted = 0;
        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);

        final int[] qp = this.qp;
        final int[] pq = this.pq;

        int lastElementIndex = -1;

        int elementsCount = this.elementsCount;

        //1-based index
        int pos = 1;

        try {
            while (pos <= elementsCount) {
                //delete it
                if (predicate.apply(buffer[pos])) {
                    lastElementIndex = qp[elementsCount];

                    //put the last element at position pos, like in remove()

                    buffer[pos] = buffer[elementsCount];
                    //last element is now at deleted position pos
                    pq[lastElementIndex] = pos;

                    //mark the index element to be removed
                    //we must reset with 0 so that qp[pq[index]] is always valid !
                    pq[qp[pos]] = 0;

                    qp[pos] = lastElementIndex;

                    //Not really needed
                    /*! #if($DEBUG) !*/
                    qp[elementsCount] = -1;
                    /*! #end !*/

                    //for GC
                    /*! #if ($TemplateOptions.KTypeGeneric) !*/
                    buffer[elementsCount] = Intrinsics.<KType> empty();
                    /*! #end !*/

                    //Diminish size
                    elementsCount--;
                    deleted++;
                } //end if to delete
                else {
                    pos++;
                }
            } //end while

            //At that point, heap property is not OK, but we are consistent nonetheless.
            /*! #if($DEBUG) !*/
            this.elementsCount = elementsCount;
            assert isConsistent();
            /*! #end !*/
        } finally {
            this.elementsCount = elementsCount;
            //reestablish heap
            updatePriorities();
        }

        /*! #if($DEBUG) !*/
        assert isMinHeap();
        assert isConsistent();
        /*! #end !*/

        return deleted;
    }

    /*! #if($DEBUG) !*/
    /**
     * methods to test invariant in assert, not present in final generated code
     */

    /**
     * method to test pq[]/qp[]/buffer[] consistency in assert expressions
     */
    @SuppressWarnings("boxing")
    private boolean isConsistent() {
        if (this.elementsCount > 0) {
            //A) For each valid index, (in pq), there is match in position in qp
            for (int key = 0; key < this.pq.length; key++) {
                if (this.pq[key] > 0) {
                    if (key != this.qp[this.pq[key]]) {
                        assert false : String.format("Inconsistent key: key=%d, size=%d , pq[key] = %d, ==> qp[pq[key]] = %d", key,
                                size(), this.pq[key], this.qp[this.pq[key]]);
                    }
                }
            }

            //B) Reverse check : for each element of position pos in buffer, there is a match in pq
            for (int pos = 1; pos <= this.elementsCount; pos++) {
                assert this.qp.length > this.elementsCount : this.qp.length;
                assert this.qp[pos] >= 0 : this.elementsCount + "|" + pos + " | " + this.qp[pos];
                assert this.qp[pos] < this.pq.length : this.pq.length;

                if (pos != this.pq[this.qp[pos]]) {
                    assert false : String.format("Inconsistent position: pos=%d, size=%d , qp[pos] = %d, ==> pq[qp[pos]] = %d",
                            pos, size(), this.qp[pos], this.pq[this.qp[pos]]);
                }
            }
        }

        return true;
    }

    /**
     * methods to test heap invariant in assert expressions
     */
    // is buffer[1..N] a min heap?
    private boolean isMinHeap() {
        if (this.comparator == null) {
            return isMinHeapComparable(1);
        }

        return isMinHeapComparator(1);
    }

    // is subtree of pq[1..N] rooted at k a min heap?
    private boolean isMinHeapComparable(final int k) {
        final int N = this.elementsCount;
        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);

        if (k > N) {
            return true;
        }
        final int left = 2 * k, right = 2 * k + 1;

        if (left <= N && Intrinsics.<KType> isCompSupUnchecked(buffer[k], buffer[left])) {
            return false;
        }
        if (right <= N && Intrinsics.<KType> isCompSupUnchecked(buffer[k], buffer[right])) {
            return false;
        }
        //recursively test
        return isMinHeapComparable(left) && isMinHeapComparable(right);
    }

    // is subtree of pq[1..N] rooted at k a min heap?
    private boolean isMinHeapComparator(final int k) {
        final int N = this.elementsCount;
        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        final Comparator<? super KType> comp = this.comparator;
        /*! #else
        KTypeComparator<? super KType> comp = this.comparator;
        #end !*/

        if (k > N) {
            return true;
        }
        final int left = 2 * k, right = 2 * k + 1;

        if (left <= N && comp.compare(buffer[k], buffer[left]) > 0) {
            return false;
        }
        if (right <= N && comp.compare(buffer[k], buffer[right]) > 0) {
            return false;
        }
        //recursively test
        return isMinHeapComparator(left) && isMinHeapComparator(right);
    }

    //end ifdef debug
    /*! #end !*/
}
