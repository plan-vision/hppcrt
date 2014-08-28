package com.carrotsearch.hppcrt.heaps;

import java.util.*;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.cursors.*;
import com.carrotsearch.hppcrt.predicates.*;
import com.carrotsearch.hppcrt.procedures.*;
import com.carrotsearch.hppcrt.sorting.*;

/**
 * A Heap-based, indexed min-priority queue of <code>KType</code>s,
 * i.e. top() is the smallest element of the queue.
 * as defined by Sedgewick: Algorithms 4th Edition (2011).
 * This class is also a {@link IntKTypeMap}, and acts like a (K,V) = (int, KType) map.
 * It assures O(log2(N)) complexity for insertion, deletion of the first element,
 * and constant time to examine the first element.
 * As it is <code>int</code> indexed, it also supports {@link #containsKey()} in constant time, {@link #remove()}
 * and {@link #changePriority(int)} in O(log2(N)) time.
 * * <p><b>Important note: This implementation uses direct indexing, meaning that a map
 * at any given time is only able to have <code>int</code> keys in
 * the [0 ; {@link #capacity()}[ range. So when a {@link #put(key, KType)} occurs, the map may be resized to be able hold a key exceeding the current capacity.</b>
 * </p>
 * @author <a href="https://github.com/vsonnier" >Vincent Sonnier</a>
 */
/*! ${TemplateOptions.doNotGenerateKType("BOOLEAN")} !*/
/*! #set( $DEBUG = false) !*/
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeIndexedHeapPriorityQueue<KType> implements IntKTypeMap<KType>, Cloneable
{
    /**
     * Default capacity if no other capacity is given in the constructor.
     */
    public final static int DEFAULT_CAPACITY = 16;

    /**
     * Internal array for storing the priority queue
      #if ($TemplateOptions.KTypeGeneric)
     * <p><strong>Important!</strong>
     * The actual value in this field is always an instance of <code>Object[]</code>.
     * Be warned that <code>javac</code> emits additional casts when <code>buffer</code>
     * is directly accessed; <strong>these casts
     * may result in exceptions at runtime</strong>. A workaround is to cast directly to
     * <code>Object[]</code> before accessing the buffer's elements (although it is highly
     * recommended to use a {@link #iterator()} instead.
     * </pre>
    #end
     * <p>
     * Direct indexed priority queue iteration: iterate pq[i] for i in [0; pq.length[
     * and buffer[pq[i]] to get value where pq[i] > 0
     * </p>
     */
    public KType[] buffer;

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
    protected int[] qp;

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

    protected KType defaultValue = Intrinsics.<KType> defaultKTypeValue();

    /**
     * internal pool of EntryIterator (must be created in constructor)
     */
    protected final IteratorPool<IntKTypeCursor<KType>, EntryIterator> entryIteratorPool;

    /**
     * Create with a given initial capacity, using a
     * Comparator for ordering.
     */
    public KTypeIndexedHeapPriorityQueue(/*! #if ($TemplateOptions.KTypeGeneric) !*/final Comparator<? super KType> comp,
            /*! #else
            KTypeComparator<? super KType> comp,
            #end !*/final int initialCapacity)
    {
        this.comparator = comp;

        assert initialCapacity >= 0 : "initialCapacity must be >= 0: " + initialCapacity;

        //1-based index buffer, assure allocation
        ensureBufferSpace(initialCapacity + 1);

        this.entryIteratorPool = new IteratorPool<IntKTypeCursor<KType>, EntryIterator>(
                new ObjectFactory<EntryIterator>() {

                    @Override
                    public EntryIterator create()
                    {
                        return new EntryIterator();
                    }

                    @Override
                    public void initialize(final EntryIterator obj)
                    {
                        obj.cursor.index = 0;
                        obj.buffer = KTypeIndexedHeapPriorityQueue.this.buffer;
                        obj.size = KTypeIndexedHeapPriorityQueue.this.elementsCount;
                        obj.qp = KTypeIndexedHeapPriorityQueue.this.qp;
                    }

                    @Override
                    public void reset(final EntryIterator obj)
                    {
                        // for GC sake
                        obj.qp = null;
                        obj.buffer = null;
                    }
                });
    }

    /**
     * Create with default sizing strategy and initial capacity for storing
     * {@value #DEFAULT_CAPACITY} elements.
     * 
     * @see BoundedProportionalArraySizingStrategy
     */
    public KTypeIndexedHeapPriorityQueue(/*! #if ($TemplateOptions.KTypeGeneric) !*/final Comparator<? super KType> comp
            /*! #else
    KTypeComparator<? super KType> comp
    #end !*/)
    {
        this(comp, KTypeIndexedHeapPriorityQueue.DEFAULT_CAPACITY);
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
    public KTypeIndexedHeapPriorityQueue(final int initialCapacity)
    {
        this(null, initialCapacity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear()
    {
        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        //1-based indexing
        Internals.blankObjectArray(this.buffer, 1, this.elementsCount + 1);
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
     * Holds a IntKTypeCursor<KType> cursor returning (key, value, index) = (int key, KType value, index the position in heap)
     */
    public final class EntryIterator extends AbstractIterator<IntKTypeCursor<KType>>
    {
        public final IntKTypeCursor<KType> cursor;

        private KType[] buffer;
        private int size;
        private int[] qp;

        public EntryIterator()
        {
            this.cursor = new IntKTypeCursor<KType>();
            //index 0 is not used in Priority queue
            this.cursor.index = 0;
            this.buffer = KTypeIndexedHeapPriorityQueue.this.buffer;
            this.size = KTypeIndexedHeapPriorityQueue.this.size();
            this.qp = KTypeIndexedHeapPriorityQueue.this.qp;
        }

        @Override
        protected IntKTypeCursor<KType> fetch()
        {
            //priority is 1-based index
            if (this.cursor.index == this.size)
            {
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
    public EntryIterator iterator()
    {
        return this.entryIteratorPool.borrow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size()
    {
        return this.elementsCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int capacity()
    {
        return this.buffer.length - 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends IntKTypeProcedure<? super KType>> T forEach(final T procedure)
    {
        final KType[] buffer = this.buffer;
        final int[] qp = this.qp;
        final int size = this.elementsCount;

        for (int pos = 1; pos <= size; pos++)
        {
            procedure.apply(qp[pos], buffer[pos]);
        }

        return procedure;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends IntKTypePredicate<? super KType>> T forEach(final T predicate)
    {
        final KType[] buffer = this.buffer;
        final int[] qp = this.qp;
        final int size = this.elementsCount;

        for (int pos = 1; pos <= size; pos++)
        {
            if (!predicate.apply(qp[pos], buffer[pos]))
            {
                break;
            }
        }

        return predicate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty()
    {
        return this.elementsCount == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeAll(final IntContainer container)
    {
        final int before = this.elementsCount;

        for (final IntCursor cursor : container)
        {
            remove(cursor.value);
        }

        return before - this.elementsCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeAll(final IntPredicate predicate)
    {
        final int[] pq = this.pq;
        final int size = this.pq.length;

        final int initialSize = this.elementsCount;

        //iterate keys, for all valid keys is OK because only the current pq[key] slot
        //is affected by the current remove() but the next ones are not.
        for (int key = 0; key < size; key++)
        {
            if (pq[key] > 0 && predicate.apply(key))
            {
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
    public boolean putIfAbsent(final int key, final KType value)
    {
        if (!containsKey(key))
        {
            put(key, value);
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int putAll(final IntKTypeAssociativeContainer<? extends KType> container)
    {
        return putAll((Iterable<? extends IntKTypeCursor<? extends KType>>) container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int putAll(final Iterable<? extends IntKTypeCursor<? extends KType>> iterable)
    {
        final int count = this.elementsCount;

        for (final IntKTypeCursor<? extends KType> c : iterable)
        {
            put(c.key, c.value);
        }

        return this.elementsCount - count;
    }

    /**
     * {@inheritDoc}
     * cost: O(log2(N)) for a N sized queue
     */
    @Override
    public KType put(final int key, final KType element)
    {
        assert key >= 0;

        //1) Key already present, insert new value
        if (key < this.pq.length && this.pq[key] > 0)
        {
            //element already exists : insert brutally at the same position in buffer and refresh the priorities to reestablish heap
            final KType previousValue = this.buffer[this.pq[key]];

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
     * <a href="http://trove4j.sourceforge.net">Trove</a>-inspired API method. An equivalent
     * of the following code:
     * <pre>
     *  if (containsKey(key))
     *  {
     *      KType v = get(key) + additionValue;
     *      put(key, v);
     *      return v;
     *  }
     *  else
     *  {
     *     put(key, putValue);
     *     return putValue;
     *  }
     * </pre>
     * 
     * @param key The key of the value to adjust.
     * @param putValue The value to put if <code>key</code> does not exist.
     * @param additionValue The value to add to the existing value if <code>key</code> exists.
     * @return Returns the current value associated with <code>key</code> (after changes).
     */
    /*! #end !*/
    /*! #if ($TemplateOptions.KTypeNumeric)
    @Override
    public KType putOrAdd(int key, KType putValue, KType additionValue)
    {
       assert key >= 0;

        //1) Key already present, add additionValue to the existing one
        if (key < this.pq.length && this.pq[key] > 0)
        {
            //element already exists : insert brutally at the same position in buffer and refresh the priorities to reestablish heap
            this.buffer[this.pq[key]] += additionValue;

            //re-establish heap
            sink(this.pq[key]);
            swim(this.pq[key]);

            #if($DEBUG)
            assert isMinHeap();
            assert isConsistent();
            #end

            return this.buffer[this.pq[key]];
        }

        //2) not present, add at the end
        // 2-1) pq must be sufficient to receive index by direct indexing,
        //resize if needed.
        ensureBufferSpace(key);

        //2-2) Add
        this.elementsCount++;
        final int count = this.elementsCount;

        this.buffer[count] = putValue;

        //initial position
        this.pq[key] = count;
        this.qp[count] = key;

        //swim last element
        swim(count);

        return putValue;
    }
    #end !*/

    /*! #if ($TemplateOptions.KTypeNumeric) !*/
    /**
     * An equivalent of calling
     * <pre>
     *  if (containsKey(key))
     *  {
     *      KType v = get(key) + additionValue;
     *      put(key, v);
     *      return v;
     *  }
     *  else
     *  {
     *     put(key, additionValue);
     *     return additionValue;
     *  }
     * </pre>
     * 
     * @param key The key of the value to adjust.
     * @param additionValue The value to put or add to the existing value if <code>key</code> exists.
     * @return Returns the current value associated with <code>key</code> (after changes).
     */
    /*! #end !*/
    /*! #if ($TemplateOptions.KTypeNumeric)
    @Override
    public KType addTo(int key, KType additionValue)
    {
        return putOrAdd(key, additionValue, additionValue);
    }
    #end !*/

    /**
     * Retrieve, but not remove, the top element of the queue,
     * i.e. the min/max element with respect to the comparison criteria
     * (implementation defined)
     * of the queue. Returns the default value if empty.
     * cost: O(1)
     */
    public KType top()
    {
        KType elem = this.defaultValue;

        if (this.elementsCount > 0)
        {
            elem = this.buffer[1];
        }

        return elem;
    }

    /**
     * Retrieve, and remove the top element of the queue,
     * i.e. the min/max element with respect to the comparison criteria
     * (implementation defined) Returns the default value if empty.
     * cost: O(log2(N)) for a N sized queue
     */
    public KType popTop()
    {
        KType elem = this.defaultValue;

        if (this.elementsCount > 0)
        {
            elem = this.buffer[1];

            remove(this.qp[1]);
        }

        return elem;
    }

    /**
     * {@inheritDoc}
     * cost: O(1)
     */
    @Override
    public KType get(final int key)
    {
        /*! #if($DEBUG) !*/
        assert key >= this.pq.length || this.pq[key] > 0 : "Element of index " + " doesn't exist !";
        /*! #end !*/

        KType elem = this.defaultValue;

        if (key < this.pq.length && this.pq[key] > 0)
        {
            elem = this.buffer[this.pq[key]];
        }

        return elem;
    }

    /**
     * {@inheritDoc}
     * cost: O(log2(N))
     */
    @Override
    public KType remove(final int key)
    {
        KType deletedElement = this.defaultValue;

        final int[] qp = this.qp;
        final int[] pq = this.pq;
        final KType[] buffer = this.buffer;

        if (key < pq.length && pq[key] > 0)
        {
            final int deletedPos = pq[key];
            deletedElement = buffer[deletedPos];

            if (deletedPos == this.elementsCount)
            {
                //we remove the last element
                pq[key] = 0;

                //for GC
                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                buffer[deletedPos] = Intrinsics.<KType> defaultKTypeValue();
                /*! #end !*/

                //Not really needed, but usefull to catch inconsistencies
                /*! #if($DEBUG) !*/
                qp[deletedPos] = -1;
                /*! #end !*/

                //diminuish size
                this.elementsCount--;
            }
            else
            {
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
                buffer[this.elementsCount] = Intrinsics.<KType> defaultKTypeValue();
                /*! #end !*/

                //diminuish size
                this.elementsCount--;

                //after swapping positions
                /*! #if($DEBUG) !*/
                assert pq[lastElementIndex] == deletedPos : String.format("pq[lastElementIndex = %d] = %d, while deletedPos = %d, (key = %d)",
                        lastElementIndex, pq[lastElementIndex], deletedPos, key);

                assert qp[deletedPos] == lastElementIndex : String.format("qp[deletedPos = %d] = %d, while lastElementIndex = %d, (key = %d)",
                        deletedPos, qp[deletedPos], lastElementIndex, key);
                /*! #end !*/

                if (this.elementsCount > 1)
                {
                    //re-establish heap
                    sink(pq[lastElementIndex]);
                    swim(pq[lastElementIndex]);
                }
            }
        }

        return deletedElement;
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    /**
     * Update the priority of  the value of the queue with key, to re-establish the correct priority
     * towards the comparison criteria.
     * cost: O(log2(N))
     */
    public void changePriority(final int key)
    {
        if (key < this.pq.length && this.pq[key] > 0)
        {
            swim(this.pq[key]);
            sink(this.pq[key]);
        }
    }

    /*! #end !*/

    /**
     * {@inheritDoc}
     * cost: O(1)
     */
    @Override
    public boolean containsKey(final int key)
    {
        if (key < this.pq.length && this.pq[key] > 0)
        {
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        int h = 1;
        final int size = this.pq.length;
        final KType[] buffer = this.buffer;
        final int[] pq = this.pq;

        //iterate by (ordered) index to have a reproducible hash and
        //so keeping a multiplicative quality
        for (int index = 0; index < size; index++)
        {
            if (pq[index] > 0)
            {
                //rehash of the index
                h = 31 * h + Internals.rehash(index);
                //append the rehash of the value
                h = 31 * h + Internals.rehash(buffer[pq[index]]);
            }
        }

        return h;
    }

    /**
     * this instance and obj can only be equal if either: <pre>
     * (both don't have set comparators)
     * or
     * (both have equal comparators defined by {@link #comparator}.equals(obj.comparator))</pre>
     * then, both heaps are compared as follows: <pre>
     * {@inheritDoc}</pre>
     */
    @Override
    /* #if ($TemplateOptions.KTypeGeneric) */
    @SuppressWarnings("unchecked")
    /* #end */
    public boolean equals(final Object obj)
    {
        if (obj != null)
        {
            if (obj == this)
            {
                return true;
            }

            //we can only compare both KTypeHeapPriorityQueue,
            //that has the same comparison function reference
            if (!(obj instanceof KTypeIndexedHeapPriorityQueue<?>))
            {
                return false;
            }

            final KTypeIndexedHeapPriorityQueue<KType> other = (KTypeIndexedHeapPriorityQueue<KType>) obj;

            if (other.size() != this.size())
            {
                return false;
            }

            //Iterate over the smallest pq buffer of the two.
            int[] pqBuffer, otherPqBuffer;
            KType[] buffer, otherBuffer;

            if (this.pq.length < other.pq.length)
            {

                pqBuffer = this.pq;
                otherPqBuffer = other.pq;
                buffer = this.buffer;
                otherBuffer = other.buffer;
            }
            else
            {

                pqBuffer = other.pq;
                otherPqBuffer = this.pq;
                buffer = other.buffer;
                otherBuffer = this.buffer;
            }

            final int pqBufferSize = pqBuffer.length;
            final KType currentValue, otherValue;
            int currentIndex, otherIndex;

            //Both have null comparators
            if (this.comparator == null && other.comparator == null)
            {

                for (int i = 0; i < pqBufferSize; i++)
                {
                    currentIndex = pqBuffer[i];

                    if (currentIndex > 0)
                    {
                        //check that currentIndex exists in otherBuffer at the same i
                        otherIndex = otherPqBuffer[i];

                        if (otherIndex <= 0)
                        {
                            return false;
                        }

                        //compare both elements with Comparable, or natural ordering
                        if (!Intrinsics.isCompEqualKTypeUnchecked(buffer[currentIndex], otherBuffer[otherIndex]))
                        {
                            return false;
                        }
                    }
                }

                return true;

            }
            else if (this.comparator != null && this.comparator.equals(other.comparator))
            {

                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                final Comparator<? super KType> comp = this.comparator;
                /*! #else
                KTypeComparator<? super KType> comp = this.comparator;
                #end !*/

                for (int i = 0; i < pqBufferSize; i++)
                {
                    currentIndex = pqBuffer[i];

                    if (currentIndex > 0)
                    {
                        //check that currentIndex exists in otherBuffer
                        otherIndex = otherPqBuffer[i];

                        if (otherIndex <= 0)
                        {
                            return false;
                        }

                        //compare both elements with Comparator
                        if (comp.compare(buffer[i], otherBuffer[i]) != 0)
                        {
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
     * It also realizes a trim-to- this.size() in the process.
     */
    @Override
    public KTypeIndexedHeapPriorityQueue<KType> clone()
    {
        final KTypeIndexedHeapPriorityQueue<KType> cloned = new KTypeIndexedHeapPriorityQueue<KType>(this.comparator, this.size());

        for (final IntKTypeCursor<KType> cursor : this)
        {
            cloned.put(cursor.index, cursor.value);
        }

        cloned.defaultValue = this.defaultValue;

        return cloned;
    }

    /**
     * Update priorities of all the elements of the queue, to re-establish the correct priorities
     * towards the comparison criteria.
     */
    public void refreshPriorities()
    {
        if (this.comparator == null)
        {
            for (int k = this.elementsCount >> 1; k >= 1; k--)
            {
                sinkComparable(k);
            }
        }
        else
        {
            for (int k = this.elementsCount >> 1; k >= 1; k--)
            {
                sinkComparator(k);
            }
        }
    }

    /**
     *  @return a new KeysContainer view of the keys of this associated container.
     * This view then reflects all changes from the map.
     */
    @Override
    public KeysContainer keys()
    {
        return new KeysContainer();
    }

    /**
     * A view of the keys inside this hash map.
     */
    public final class KeysContainer extends AbstractIntCollection implements IntLookupContainer
    {
        private final KTypeIndexedHeapPriorityQueue<KType> owner =
                KTypeIndexedHeapPriorityQueue.this;

        @Override
        public boolean contains(final int e)
        {
            return this.owner.containsKey(e);
        }

        @Override
        public <T extends IntProcedure> T forEach(final T procedure)
        {
            final KType[] buffer = this.owner.buffer;
            final int[] qp = this.owner.qp;
            final int size = this.owner.elementsCount;

            for (int pos = 1; pos <= size; pos++)
            {
                procedure.apply(qp[pos]);
            }

            return procedure;
        }

        @Override
        public <T extends IntPredicate> T forEach(final T predicate)
        {
            final KType[] buffer = this.owner.buffer;
            final int[] qp = this.owner.qp;
            final int size = this.owner.elementsCount;

            for (int pos = 1; pos <= size; pos++)
            {
                if (!predicate.apply(qp[pos]))
                {
                    break;
                }
            }

            return predicate;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public KeysIterator iterator()
        {
            //return new KeysIterator();
            return this.keyIteratorPool.borrow();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int size()
        {
            return this.owner.size();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int capacity()
        {

            return this.owner.capacity();
        }

        @Override
        public void clear()
        {
            this.owner.clear();
        }

        @Override
        public int removeAll(final IntPredicate predicate)
        {
            return this.owner.removeAll(predicate);
        }

        @Override
        public int removeAllOccurrences(final int e)
        {
            final boolean hasKey = this.owner.containsKey(e);
            int result = 0;
            if (hasKey)
            {
                this.owner.remove(e);
                result = 1;
            }
            return result;
        }

        @Override
        public int[] toArray(final int[] target)
        {
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
                    public KeysIterator create()
                    {
                        return new KeysIterator();
                    }

                    @Override
                    public void initialize(final KeysIterator obj)
                    {
                        //the value here represent the key value
                        obj.cursor.value = 0;
                        obj.pq = KTypeIndexedHeapPriorityQueue.this.pq;
                    }

                    @Override
                    public void reset(final KeysIterator obj)
                    {
                        //no dangling references
                        obj.pq = null;
                    }
                });

    };

    /**
     * An iterator over the set of assigned keys.
     */
    public final class KeysIterator extends AbstractIterator<IntCursor>
    {
        public final IntCursor cursor;

        private int[] pq;

        public KeysIterator()
        {
            this.cursor = new IntCursor();
            //the value here represent the key value
            this.cursor.value = 0;
            this.pq = KTypeIndexedHeapPriorityQueue.this.pq;
        }

        /**
         * 
         */
        @Override
        protected IntCursor fetch()
        {
            //skip all non-affected keys and stop at the next affected key
            while (this.cursor.value < this.pq.length && this.pq[this.cursor.value] <= 0)
            {
                this.cursor.value++;
            }

            if (this.cursor.value == this.pq.length)
            {
                return done();
            }

            //the cursor index corresponds to the position in heap buffer
            this.cursor.index = this.pq[this.cursor.value];

            return this.cursor;
        }
    }

    /**
     *  @return a new ValuesContainer, view of the values of this map.
     * This view then reflects all changes from the map.
     */
    @Override
    public ValuesContainer values()
    {
        return new ValuesContainer();
    }

    /**
     * A view over the set of values of this map.
     */
    public final class ValuesContainer extends AbstractKTypeCollection<KType>
    {
        private final KTypeIndexedHeapPriorityQueue<KType> owner =
                KTypeIndexedHeapPriorityQueue.this;

        private KType currentOccurenceToBeRemoved;

        private final  KTypePredicate<? super KType> removeAllOccurencesPredicate = new KTypePredicate<KType> () {

            @Override
            public final  boolean apply(final KType value) {

                if (ValuesContainer.this.owner.comparator == null) {

                    if (Intrinsics.isCompEqualKTypeUnchecked(value, ValuesContainer.this.currentOccurenceToBeRemoved)) {

                        return true;
                    }

                } else {

                    if (ValuesContainer.this.owner.comparator.compare(value, ValuesContainer.this.currentOccurenceToBeRemoved) == 0) {

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
        public int size()
        {
            return this.owner.size();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int capacity()
        {
            return this.owner.capacity();
        }

        @Override
        public boolean contains(final KType value)
        {
            final KType[] buffer = this.owner.buffer;
            final int size = this.owner.elementsCount;

            if (this.owner.comparator == null) {

                //iterate the heap buffer, use the natural comparison criteria
                for (int pos = 1; pos <= size; pos++)
                {
                    if (Intrinsics.isCompEqualKTypeUnchecked(buffer[pos], value))
                    {
                        return true;
                    }
                }
            }
            else {

                //use the dedicated comparator
                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                final Comparator<? super KType> comp = this.owner.comparator;
                /*! #else
                KTypeComparator<? super KType> comp = this.owner.comparator;
                #end !*/
                for (int pos = 1; pos <= size; pos++)
                {
                    if (comp.compare(buffer[pos], value) == 0)
                    {
                        return true;
                    }
                }
            } //end else

            return false;
        }

        @Override
        public <T extends KTypeProcedure<? super KType>> T forEach(final T procedure)
        {
            final KType[] buffer = this.owner.buffer;
            final int size = this.owner.elementsCount;

            //iterate the heap buffer, use the natural comparison criteria
            for (int pos = 1; pos <= size; pos++)
            {
                procedure.apply(buffer[pos]);
            }

            return procedure;
        }

        @Override
        public <T extends KTypePredicate<? super KType>> T forEach(final T predicate)
        {
            final KType[] buffer = this.owner.buffer;
            final int size = this.owner.elementsCount;

            //iterate the heap buffer, use the natural comparison criteria
            for (int pos = 1; pos <= size; pos++)
            {
                if (!predicate.apply(buffer[pos])) {
                    break;
                }
            }

            return predicate;
        }

        @Override
        public ValuesIterator iterator()
        {
            // return new ValuesIterator();
            return this.valuesIteratorPool.borrow();
        }

        /**
         * {@inheritDoc}
         * Indeed removes all the (key,value) pairs matching
         * (key ? ,  e) with the  same  e,  from  the map.
         */
        @Override
        public int removeAllOccurrences(final KType e)
        {
            this.currentOccurenceToBeRemoved = e;
            return this.owner.removeAllInternal(this.removeAllOccurencesPredicate);
        }

        /**
         * {@inheritDoc}
         * Indeed removes all the (key,value) pairs matching
         * the predicate for the values, from  the map.
         */
        @Override
        public int removeAll(final KTypePredicate<? super KType> predicate)
        {
            return this.owner.removeAllInternal(predicate);
        }

        /**
         * {@inheritDoc}
         *  Alias for clear() the whole map.
         */
        @Override
        public void clear()
        {
            this.owner.clear();
        }

        @Override
        public KType[] toArray(final KType[] target)
        {
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
                    public ValuesIterator create()
                    {
                        return new ValuesIterator();
                    }

                    @Override
                    public void initialize(final ValuesIterator obj)
                    {
                        obj.cursor.index = 0;
                        obj.buffer = KTypeIndexedHeapPriorityQueue.this.buffer;
                        obj.size = KTypeIndexedHeapPriorityQueue.this.size();
                    }

                    @Override
                    public void reset(final ValuesIterator obj)
                    {
                        obj.buffer = null;
                    }
                });


    }

    /**
     * An iterator over the set of assigned values.
     */
    public final class ValuesIterator extends AbstractIterator<KTypeCursor<KType>>
    {
        public final KTypeCursor<KType> cursor;

        private KType[] buffer;
        private int size;

        public ValuesIterator()
        {
            this.cursor = new KTypeCursor<KType>();

            //index 0 is not used in Priority queue
            this.cursor.index = 0;
            this.buffer = KTypeIndexedHeapPriorityQueue.this.buffer;
            this.size = KTypeIndexedHeapPriorityQueue.this.size();
        }


        @Override
        protected KTypeCursor<KType> fetch()
        {
            //priority is 1-based index
            if (this.cursor.index == this.size)
            {
                return done();
            }

            //this.cursor.index represent the position in the heap buffer.
            this.cursor.value = this.buffer[++this.cursor.index];

            return this.cursor;
        }
    }

    @Override
    public String toString()
    {
        final KType[] buffer = this.buffer;
        final int[] pq = this.pq;

        final StringBuilder buff = new StringBuilder();
        buff.append("[");

        boolean first = true;

        //Indices are displayed in ascending order, for easier reading.
        for (int i = 0; i < pq.length; i++)
        {
            if (pq[i] > 0)
            {
                if (!first)
                {
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
     * in containers methods returning "default value"
     * @return
     */
    public KType getDefaultValue()
    {
        return this.defaultValue;
    }

    /**
     * Ensures the internal buffer has enough free slots to accomodate the index
     * <code>index</code>. Increases internal buffer size if needed.
     */
    protected void ensureBufferSpace(final int index)
    {
        final int pqLen = this.pq == null ? 0 : this.pq.length;
        if (index > pqLen - 1)
        {
            //resize to accomodate this index.
            final int newPQSize = index + KTypeIndexedHeapPriorityQueue.DEFAULT_CAPACITY;

            final int[] newPQIndex = new int[newPQSize];
            final KType[] newBuffer = Intrinsics.newKTypeArray(newPQSize + 1);
            final int[] newQPIndex = new int[newPQSize + 1];

            if (pqLen > 0)
            {
                System.arraycopy(this.buffer, 0, newBuffer, 0, this.buffer.length);
                System.arraycopy(this.pq, 0, newPQIndex, 0, this.pq.length);
                System.arraycopy(this.qp, 0, newQPIndex, 0, this.qp.length);
            }
            this.buffer = newBuffer;
            this.pq = newPQIndex;
            this.qp = newQPIndex;
        }
    }

    /**
     * Sink function for Comparable elements
     * @param k
     */
    private void sinkComparable(int k)
    {
        final int N = this.elementsCount;
        KType tmp;
        int child;
        int indexK, indexChild;

        final KType[] buffer = this.buffer;
        final int[] pq = this.pq;
        final int[] qp = this.qp;

        while (k << 1 <= N)
        {
            //get the child of k
            child = k << 1;

            if (child < N && Intrinsics.isCompSupKTypeUnchecked(buffer[child], buffer[child + 1]))
            {
                child++;
            }

            if (!Intrinsics.isCompSupKTypeUnchecked(buffer[k], buffer[child]))
            {
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
     * @param k
     */
    private void sinkComparator(int k)
    {
        final int N = this.elementsCount;
        KType tmp;
        int child;
        int indexK, indexChild;

        final KType[] buffer = this.buffer;
        final int[] pq = this.pq;
        final int[] qp = this.qp;

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        final Comparator<? super KType> comp = this.comparator;
        /*! #else
        KTypeComparator<? super KType> comp = this.comparator;
        #end !*/

        while (k << 1 <= N)
        {
            //get the child of k
            child = k << 1;

            if (child < N && comp.compare(buffer[child], buffer[child + 1]) > 0)
            {
                child++;
            }

            if (comp.compare(buffer[k], buffer[child]) <= 0)
            {
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
     * @param k
     */
    private void swimComparable(int k)
    {
        KType tmp;
        int parent;
        int indexK, indexParent;

        final KType[] buffer = this.buffer;
        final int[] pq = this.pq;
        final int[] qp = this.qp;

        while (k > 1 && Intrinsics.isCompSupKTypeUnchecked(buffer[k >> 1], buffer[k]))
        {
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
     * @param k
     */
    private void swimComparator(int k)
    {
        KType tmp;
        int parent;
        int indexK, indexParent;

        final KType[] buffer = this.buffer;
        final int[] pq = this.pq;
        final int[] qp = this.qp;

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        final Comparator<? super KType> comp = this.comparator;
        /*! #else
        KTypeComparator<? super KType> comp = this.comparator;
        #end !*/

        while (k > 1 && comp.compare(buffer[k >> 1], buffer[k]) > 0)
        {
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

    private void swim(final int k)
    {
        if (this.comparator == null)
        {
            swimComparable(k);
        }
        else
        {
            swimComparator(k);
        }
    }

    private void sink(final int k)
    {
        if (this.comparator == null)
        {
            sinkComparable(k);
        }
        else
        {
            sinkComparator(k);
        }
    }

    private int removeAllInternal(final KTypePredicate<? super KType> predicate)
    {
        //remove by position
        int deleted = 0;
        final KType[] buffer = this.buffer;

        final int[] qp = this.qp;
        final int[] pq = this.pq;

        int lastElementIndex = -1;

        int elementsCount = this.elementsCount;

        //1-based index
        int pos = 1;

        try
        {
            while (pos <= elementsCount)
            {
                //delete it
                if (predicate.apply(buffer[pos]))
                {
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
                    buffer[elementsCount] = Intrinsics.<KType> defaultKTypeValue();
                    /*! #end !*/

                    //Diminish size
                    elementsCount--;
                    deleted++;
                } //end if to delete
                else
                {
                    pos++;
                }
            } //end while

            //At that point, heap property is not OK, but we are consistent nonetheless.
            /*! #if($DEBUG) !*/
            assert isConsistent();
            /*! #end !*/
        }
        finally
        {
            this.elementsCount = elementsCount;
            //reestablish heap
            refreshPriorities();
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
    private boolean isConsistent()
    {
        if (this.elementsCount > 0)
        {
            //A) For each valid index, (in pq), there is match in position in qp
            for (int key = 0; key < this.pq.length; key++)
            {
                if (this.pq[key] > 0)
                {
                    if (key != this.qp[this.pq[key]])
                    {
                        assert false : String.format("Inconsistent key: key=%d, size=%d , pq[key] = %d, ==> qp[pq[key]] = %d",
                                key, size(), this.pq[key], this.qp[this.pq[key]]);
                    }
                }
            }

            //B) Reverse check : for each element of position pos in buffer, there is a match in pq
            for (int pos = 1; pos <= this.elementsCount; pos++)
            {
                if (pos != this.pq[this.qp[pos]])
                {
                    assert false : String.format("Inconsistent position: pos=%d, size=%d , qp[pos] = %d, ==> pq[qp[pos]] = %d",
                            pos, size(), this.qp[pos], this.pq[this.qp[pos]]);
                }
            }
        }

        return true;
    }

    /**
     * method to test heap invariant in assert expressions
     */
    // is buffer[1..N] a min heap?
    private boolean isMinHeap()
    {
        if (this.comparator == null)
        {
            return isMinHeapComparable(1);
        }

        return isMinHeapComparator(1);
    }

    // is subtree of pq[1..N] rooted at k a min heap?
    private boolean isMinHeapComparable(final int k)
    {
        final int N = this.elementsCount;
        final KType[] buffer = this.buffer;

        if (k > N)
        {
            return true;
        }
        final int left = 2 * k, right = 2 * k + 1;

        if (left <= N && Intrinsics.isCompSupKTypeUnchecked(buffer[k], buffer[left]))
        {
            return false;
        }
        if (right <= N && Intrinsics.isCompSupKTypeUnchecked(buffer[k], buffer[right]))
        {
            return false;
        }
        //recursively test
        return isMinHeapComparable(left) && isMinHeapComparable(right);
    }

    // is subtree of pq[1..N] rooted at k a min heap?
    private boolean isMinHeapComparator(final int k)
    {
        final int N = this.elementsCount;
        final KType[] buffer = this.buffer;

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        final Comparator<? super KType> comp = this.comparator;
        /*! #else
        KTypeComparator<? super KType> comp = this.comparator;
        #end !*/

        if (k > N)
        {
            return true;
        }
        final int left = 2 * k, right = 2 * k + 1;

        if (left <= N && comp.compare(buffer[k], buffer[left]) > 0)
        {
            return false;
        }
        if (right <= N && comp.compare(buffer[k], buffer[right]) > 0)
        {
            return false;
        }
        //recursively test
        return isMinHeapComparator(left) && isMinHeapComparator(right);
    }

    //end ifdef debug
/*! #end !*/
}
