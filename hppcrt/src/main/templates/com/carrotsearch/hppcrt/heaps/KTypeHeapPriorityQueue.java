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
 * A Heap-based, min-priority queue of <code>KType</code>s.
 * i.e. top() is the smallest element,
 * as defined by Sedgewick: Algorithms 4th Edition (2011).
 * It assure O(log(N)) complexity for insertion,  deletion and update priority of the min element,
 * and constant time to examine the min element by {@link #top()}.
 * <p><b>Important: </b>
 * Ordering of elements must be defined either
 * #if ($TemplateOptions.KTypeGeneric)
 * by {@link Comparable}
 * #else
 * by natural ordering
 * #end
 *  or by a custom comparator provided in constructors,
 * see {@link #comparator()} .
 */
/*! #set( $DEBUG = false) !*/
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeHeapPriorityQueue<KType> extends AbstractKTypeCollection<KType>
implements KTypePriorityQueue<KType>, Cloneable
{
    /**
     * Internal array for storing the priority queue.
     * <p>
     * Direct priority queue iteration: iterate buffer[i] for i in [1; size()] (included) but is out-of-order w.r.t {@link #popTop()}
     * </p>
     */
    public/*! #if ($TemplateOptions.KTypePrimitive)
              KType []
              #else !*/
    Object[]
    /*! #end !*/
    buffer;

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

    /**
     * Buffer resizing strategy.
     */
    protected final ArraySizingStrategy resizer;

    /**
     * internal pool of ValueIterator (must be created in constructor)
     */
    protected final IteratorPool<KTypeCursor<KType>, ValueIterator> valueIteratorPool;

    /**
     * The current value set for removeAll
     */
    private KType currentOccurenceToBeRemoved;

    /**
     * Internal predicate for removeAll
     */
    private final KTypePredicate<? super KType> removeAllOccurencesPredicate = new KTypePredicate<KType>() {

        @Override
        public final boolean apply(final KType value) {

            if (KTypeHeapPriorityQueue.this.comparator == null) {

                if (Intrinsics.<KType> isCompEqualUnchecked(value, KTypeHeapPriorityQueue.this.currentOccurenceToBeRemoved)) {

                    return true;
                }
            } else {

                if (KTypeHeapPriorityQueue.this.comparator.compare(value,
                        KTypeHeapPriorityQueue.this.currentOccurenceToBeRemoved) == 0) {

                    return true;
                }
            }

            return false;
        }
    };

    /**
     * Default value returned when specified
     * in methods.
     * @see #top()
     */
    private KType defaultValue;

    /**
     * Create with a Comparator, an initial capacity, and a custom buffer resizing strategy.
     */
    public KTypeHeapPriorityQueue(/*! #if ($TemplateOptions.KTypeGeneric) !*/final Comparator<? super KType> comp,
            /*! #else
            KTypeComparator<? super KType> comp,
            #end !*/final int initialCapacity, final ArraySizingStrategy resizer) {
        this.comparator = comp;

        assert resizer != null;

        this.resizer = resizer;

        //1-based index buffer, assure allocation
        ensureBufferSpace(Math.max(Containers.DEFAULT_EXPECTED_ELEMENTS, initialCapacity));

        this.valueIteratorPool = new IteratorPool<KTypeCursor<KType>, ValueIterator>(new ObjectFactory<ValueIterator>() {

            @Override
            public ValueIterator create() {
                return new ValueIterator();
            }

            @Override
            public void initialize(final ValueIterator obj) {
                obj.cursor.index = 0;
                obj.size = KTypeHeapPriorityQueue.this.size();
                obj.buffer = Intrinsics.<KType[]> cast(KTypeHeapPriorityQueue.this.buffer);
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
     * Create with default sizing strategy and initial capacity for storing
     * {@value Containers#DEFAULT_EXPECTED_ELEMENTS} elements.
     * 
     * @see BoundedProportionalArraySizingStrategy
     */
    public KTypeHeapPriorityQueue(/*! #if ($TemplateOptions.KTypeGeneric) !*/final Comparator<? super KType> comp
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
    public KTypeHeapPriorityQueue(final int initialCapacity) {
        this(null, initialCapacity, new BoundedProportionalArraySizingStrategy());
    }

    /**
     * Create with a given initial capacity, using a
     * Comparator for ordering.
     * 
     * @see BoundedProportionalArraySizingStrategy
     */
    public KTypeHeapPriorityQueue(/*! #if ($TemplateOptions.KTypeGeneric) !*/final Comparator<? super KType> comp,
            /*! #else
            KTypeComparator<? super KType> comp,
                      #end !*/final int initialCapacity)
    {
        this(comp, initialCapacity, new BoundedProportionalArraySizingStrategy());
    }

    /**
     * Creates a new heap from elements of another container.
     */
    public KTypeHeapPriorityQueue(final KTypeContainer<? extends KType> container) {
        this(container.size());
        addAll(container);
    }

    /**
     * Create a heap from elements of another container (constructor shortcut)
     */
    public static/* #if ($TemplateOptions.KTypeGeneric) */<KType> /* #end */
            KTypeHeapPriorityQueue<KType> from(final KTypeContainer<KType> container) {
        return new KTypeHeapPriorityQueue<KType>(container);
    }

    /**
     * Create a heap from a variable number of arguments or an array of
     * <code>KType</code>.
     */
    public static/* #if ($TemplateOptions.KTypeGeneric) */<KType> /* #end */
            KTypeHeapPriorityQueue<KType> from(final KType... elements) {
        final KTypeHeapPriorityQueue<KType> heap = new KTypeHeapPriorityQueue<KType>(elements.length);

        for (final KType elem : elements) {

            heap.add(elem);
        }

        return heap;
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
    public int removeAll(final KType e1) {
        this.currentOccurenceToBeRemoved = e1;
        return removeAll(this.removeAllOccurencesPredicate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeAll(final KTypePredicate<? super KType> predicate) {
        //remove by position
        int deleted = 0;
        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);
        int elementsCount = this.elementsCount;

        //1-based index
        int pos = 1;

        try {
            while (pos <= elementsCount) {
                //delete it
                if (predicate.apply(buffer[pos])) {
                    //put the last element at position pos, like in deleteIndex()
                    buffer[pos] = buffer[elementsCount];

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
        } finally {
            this.elementsCount = elementsCount;
            //reestablish heap
            updatePriorities();
        }

        /*! #if($DEBUG) !*/
        assert isMinHeap();
        /*! #end !*/

        return deleted;
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
        this.elementsCount = 0;
    }

    /**
     * An iterator implementation for {@link KTypeHeapPriorityQueue#iterator}.
     * Holds a KTypeCursor returning (value, index) = (KType value, index the position in heap {@link KTypeHeapPriorityQueue#buffer}.)
     */
    public final class ValueIterator extends AbstractIterator<KTypeCursor<KType>>
    {
        public final KTypeCursor<KType> cursor;

        private KType[] buffer;
        private int size;

        public ValueIterator() {
            this.cursor = new KTypeCursor<KType>();
            //index 0 is not used in Priority queue
            this.cursor.index = 0;
            this.size = KTypeHeapPriorityQueue.this.size();
            this.buffer = Intrinsics.<KType[]> cast(KTypeHeapPriorityQueue.this.buffer);
        }

        @Override
        protected KTypeCursor<KType> fetch() {
            //priority is 1-based index
            if (this.cursor.index == this.size) {
                return done();
            }

            this.cursor.value = this.buffer[++this.cursor.index];
            return this.cursor;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValueIterator iterator() {
        //return new ValueIterator(buffer, size());
        return this.valueIteratorPool.borrow();
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
    public boolean contains(final KType element) {
        //1-based index
        final int size = this.elementsCount;
        final KType[] buff = Intrinsics.<KType[]> cast(this.buffer);

        if (this.comparator == null) {

            for (int i = 1; i <= size; i++) {
                if (Intrinsics.<KType> isCompEqualUnchecked(element, buff[i])) {
                    return true;
                }
            } //end for
        } else {

            /*! #if ($TemplateOptions.KTypeGeneric) !*/
            final Comparator<? super KType> comp = this.comparator;
            /*! #else
            KTypeComparator<? super KType> comp = this.comparator;
            #end !*/

            for (int i = 1; i <= size; i++) {
                if (comp.compare(element, buff[i]) == 0) {
                    return true;
                }
            } //end for
        }

        return false;
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
    public <T extends KTypeProcedure<? super KType>> T forEach(final T procedure) {
        final KType[] buff = Intrinsics.<KType[]> cast(this.buffer);
        final int size = this.elementsCount;

        for (int i = 1; i <= size; i++) {
            procedure.apply(buff[i]);
        }

        return procedure;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends KTypePredicate<? super KType>> T forEach(final T predicate) {
        final KType[] buff = Intrinsics.<KType[]> cast(this.buffer);
        final int size = this.elementsCount;

        for (int i = 1; i <= size; i++) {
            if (!predicate.apply(buff[i])) {
                break;
            }
        }

        return predicate;
    }

    /**
     * Insert a KType into the queue.
     * cost: O(log(N)) for a N sized queue
     */
    @Override
    public void add(final KType element) {
        ensureBufferSpace(1);

        //add at the end
        this.elementsCount++;
        this.buffer[this.elementsCount] = element;

        //swim last element
        swim(this.elementsCount);
    }

    /**
     * {@inheritDoc}
     * cost: O(1)
     */
    @Override
    public KType top() {
        KType elem = this.defaultValue;

        if (this.elementsCount > 0) {
            elem = Intrinsics.<KType> cast(this.buffer[1]);
        }

        return elem;
    }

    /**
     * {@inheritDoc}
     * cost: O(log(N)) for a N sized queue
     */
    @Override
    public KType popTop() {
        KType elem = this.defaultValue;

        if (this.elementsCount > 0) {
            elem = Intrinsics.<KType> cast(this.buffer[1]);

            if (this.elementsCount == 1) {
                //for GC
                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                this.buffer[1] = Intrinsics.<KType> empty();
                /*! #end !*/
                //diminish size
                this.elementsCount = 0;
            } else {
                //at least 2 elements
                //put the last element in first position

                this.buffer[1] = this.buffer[this.elementsCount];

                //for GC
                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                this.buffer[this.elementsCount] = Intrinsics.<KType> empty();
                /*! #end !*/

                //diminish size
                this.elementsCount--;

                //percolate down the first element
                sink(1);
            }
        }

        return elem;
    }

    /**
     * Adds all elements from another container.
     * cost: O(N*log(N)) for N elements
     */
    public int addAll(final KTypeContainer<? extends KType> container) {
        return addAll((Iterable<? extends KTypeCursor<? extends KType>>) container);
    }

    /**
     * Adds all elements from another iterable.
     * cost: O(N*log(N)) for N elements
     */
    public int addAll(final Iterable<? extends KTypeCursor<? extends KType>> iterable) {
        int size = 0;
        final KType[] buff = Intrinsics.<KType[]> cast(this.buffer);
        int count = this.elementsCount;

        for (final KTypeCursor<? extends KType> cursor : iterable) {
            ensureBufferSpace(1);
            count++;
            buff[count] = cursor.value;
            size++;
        }

        this.elementsCount = count;

        //restore heap
        updatePriorities();
        /*! #if($DEBUG) !*/
        assert isMinHeap();
        /*! #end !*/

        return size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int h = 1;
        final int max = this.elementsCount;
        final KType[] buff = Intrinsics.<KType[]> cast(this.buffer);

        //1-based index
        for (int i = 1; i <= max; i++) {
            h = 31 * h + BitMixer.mix(buff[i]);
        }
        return h;
    }

    /**
     * {@inheritDoc}
     * cost: O(n*log(N))
     */
    @Override
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
     * {@inheritDoc}
     * cost: O(log(N))
     */
    @Override
    public void updateTopPriority() {
        //only attempt to sink if there is at least 2 elements....
        if (this.elementsCount > 1) {

            sink(1);
        }
    }

    /**
     * Clone this object. The returned clone will use the same resizing strategy and comparator.
     */
    @Override
    public KTypeHeapPriorityQueue<KType> clone() {
        //real constructor call, of a place holder
        final KTypeHeapPriorityQueue<KType> cloned = new KTypeHeapPriorityQueue<KType>(this.comparator,
                Containers.DEFAULT_EXPECTED_ELEMENTS, this.resizer);

        //clone raw buffers
        cloned.buffer = this.buffer.clone();

        cloned.defaultValue = this.defaultValue;
        cloned.elementsCount = this.elementsCount;

        return cloned;
    }

    /**
     * this instance and obj can only be equal to this if either: <pre>
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

            final KTypeHeapPriorityQueue<KType> other = (KTypeHeapPriorityQueue<KType>) obj;

            if (other.size() != this.size()) {

                return false;
            }

            final int size = this.elementsCount;
            final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);
            final KType[] otherbuffer = Intrinsics.<KType[]> cast(other.buffer);

            //both heaps must have the same comparison criteria
            if (this.comparator == null && other.comparator == null) {

                for (int i = 1; i <= size; i++) {
                    if (!Intrinsics.<KType> isCompEqualUnchecked(buffer[i], otherbuffer[i])) {
                        return false;
                    }
                }

                return true;
            } else if (this.comparator != null && this.comparator.equals(other.comparator)) {

                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                final Comparator<? super KType> comp = this.comparator;
                /*! #else
                KTypeComparator<? super KType> comp = this.comparator;
                #end !*/

                for (int i = 1; i <= size; i++) {
                    if (comp.compare(buffer[i], otherbuffer[i]) != 0) {
                        return false;
                    }
                }

                return true;
            }

        }

        return false;
    }

    /**
     * Ensures the internal buffer has enough free slots to store
     * <code>expectedAdditions</code>. Increases internal buffer size if needed.
     */
    @SuppressWarnings("boxing")
    protected void ensureBufferSpace(final int expectedAdditions) {
        final int bufferLen = this.buffer == null ? 0 : this.buffer.length;

        //element of index 0 is not used
        if (this.elementsCount + 1 > bufferLen - expectedAdditions) {
            int newSize = this.resizer.grow(bufferLen, this.elementsCount, expectedAdditions);

            //first allocation, reserve an additional slot because index 0  is not used
            if (this.buffer == null) {
                newSize++;
            }

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
     * {@inheritDoc}
     */
    @Override
    public KType[] toArray(final KType[] target) {
        //copy from index 1
        System.arraycopy(this.buffer, 1, target, 0, this.elementsCount);
        return target;
    }

    /**
     * Get the custom comparator used for comparing elements
     * @return null if no custom comparator was set, i.e natural ordering
     * of <code>KType</code>s is used instead
     * #if($TemplateOptions.KTypeGeneric) , which means objects in this case must be {@link Comparable}.
     * #end
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
     * Returns the "default value" value used
     * in methods returning "default value"
     */
    @Override
    public KType getDefaultValue()
    {
        return this.defaultValue;
    }

    /**
     * Set the "default value" value to be used
     * in methods returning "default value"
     */
    @Override
    public void setDefaultValue(final KType defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    /**
     * Sink function for Comparable elements
     * 
     * @param k
     */
    private void sinkComparable(int k) {
        final int N = this.elementsCount;
        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);

        KType tmp;
        int child;

        while ((k << 1) <= N) {
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
        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);

        KType tmp;
        int child;
        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        final Comparator<? super KType> comp = this.comparator;
        /*! #else
            KTypeComparator<? super KType> comp = this.comparator;
            #end !*/

        while ((k << 1) <= N) {
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
        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);

        while (k > 1 && Intrinsics.<KType> isCompSupUnchecked(buffer[k >> 1], buffer[k])) {
            //swap k and its parent
            parent = k >> 1;

            tmp = buffer[k];
            buffer[k] = buffer[parent];
            buffer[parent] = tmp;

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
        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        final Comparator<? super KType> comp = this.comparator;
        /*! #else
            KTypeComparator<? super KType> comp = this.comparator;
            #end !*/

        while (k > 1 && comp.compare(buffer[k >> 1], buffer[k]) > 0) {
            //swap k and its parent
            parent = k >> 1;
            tmp = buffer[k];
            buffer[k] = buffer[parent];
            buffer[parent] = tmp;

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

    /*! #if($DEBUG) !*/
    /**
     * methods to test invariant in assert, not present in final generated code
     */
    // is pq[1..N] a min heap?
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

    //end of ifdef debug
    /*! #end !*/
}
