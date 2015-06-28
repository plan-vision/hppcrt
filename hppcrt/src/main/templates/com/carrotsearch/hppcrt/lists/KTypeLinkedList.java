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
 * An double-linked list of KTypes. This is an hybrid of {@link KTypeDeque} and {@link KTypeIndexedContainer},
 * with the ability to push or remove values from either head or tail, in constant time.
 * The drawback is the double-linked list ones, i.e insert(index), remove(index) are O(N)
 * since they must traverse the collection to reach the index.
 * In addition, elements could by pushed or removed from the middle of the list without moving
 * big amounts of memory, contrary to {@link KTypeArrayList}s. Like {@link KTypeDeque}s, the double-linked list
 * can also be iterated in reverse.
 * Plus, the Iterator or reversed-iterator supports powerful methods to modify/delete/set the neighbors,
 * in replacement of the error-prone java.util.Iterator ones.
 * A compact representation is used to store and manipulate
 * all elements, without creating Objects for links. Reallocations are governed by a {@link ArraySizingStrategy}
 * and may be expensive if they move around really large chunks of memory.
 * <b>
 * Important note: DO NOT USE java.util.Iterator methods ! They are here only for enhanced-loop syntax. Use
 * the specialized methods of {@link ValueIterator} or {@link DescendingValueIterator} instead !
 * </b>
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeLinkedList<KType>
        extends AbstractKTypeCollection<KType> implements KTypeIndexedContainer<KType>, KTypeDeque<KType>, Cloneable
{
    /**
     * Internal array for storing the list. The array may be larger than the current size
     * ({@link #size()}).
     * 
     * <p>
     * Direct list iteration: iterate buffer[i] for i in [2; size()+2[, but beware, it is out of order w.r.t the real list order !
     * </p>
     */
    public/*! #if ($TemplateOptions.KTypePrimitive)
          KType []
          #else !*/
    Object[]
    /*! #end !*/
    buffer;

    /**
     * Represent the before / after nodes for each element of the buffer,
     * such as buffer[i] nodes are beforeAfterPointers[i] where
     * the 32 highest bits represent the unsigned before index in buffer : (beforeAfterPointers[i] & 0xFFFFFFFF00000000) >> 32
     * the 32 lowest bits represent the unsigned after index in buffer : (beforeAfterPointers[i] & 0x00000000FFFFFFFF)
     */
    protected long[] beforeAfterPointers;

    /**
     * Respective positions of head and tail elements of the list,
     * as a position in buffer, such as
     * after head is the actual first element of the list,
     * before tail is the actual last element of the list.
     * Technically, head and tail have hardcoded positions of 0 and 1,
     * and occupy buffer[0] and buffer[1] virtually.
     * So real buffer elements starts at index 2.
     */
    protected static final int HEAD_POSITION = 0;
    protected static final int TAIL_POSITION = KTypeLinkedList.HEAD_POSITION + 1;

    /**
     * Current number of elements stored in {@link #buffer}.
     * Beware, the real number of elements is elementsCount - 2
     */
    protected int elementsCount = 2;

    /**
     * Buffer resizing strategy.
     */
    protected final ArraySizingStrategy resizer;

    /**
     * internal pool of ValueIterator (must be created in constructor)
     */
    protected final IteratorPool<KTypeCursor<KType>, ValueIterator> valueIteratorPool;

    /**
     * internal pool of DescendingValueIterator (must be created in constructor)
     */
    protected final IteratorPool<KTypeCursor<KType>, DescendingValueIterator> descendingValueIteratorPool;

    /**
     * Create with default sizing strategy and initial capacity for storing
     * {@link Containers#DEFAULT_EXPECTED_ELEMENTS} elements.
     * 
     * @see BoundedProportionalArraySizingStrategy
     */
    public KTypeLinkedList() {
        this(Containers.DEFAULT_EXPECTED_ELEMENTS);
    }

    /**
     * Create with default sizing strategy and the given initial capacity.
     * 
     * @see BoundedProportionalArraySizingStrategy
     */
    public KTypeLinkedList(final int initialCapacity) {
        this(initialCapacity, new BoundedProportionalArraySizingStrategy());
    }

    /**
     * Create with a custom buffer resizing strategy.
     */
    public KTypeLinkedList(final int initialCapacity, final ArraySizingStrategy resizer) {
        assert resizer != null;

        this.resizer = resizer;

        //allocate internal buffer
        ensureBufferSpace(Math.max(Containers.DEFAULT_EXPECTED_ELEMENTS, initialCapacity));

        //initialize
        this.elementsCount = 2;

        //initialize head and tail: initially, they are linked to each other.
        this.beforeAfterPointers[KTypeLinkedList.HEAD_POSITION] = getLinkNodeValue(KTypeLinkedList.HEAD_POSITION, KTypeLinkedList.TAIL_POSITION);
        this.beforeAfterPointers[KTypeLinkedList.TAIL_POSITION] = getLinkNodeValue(KTypeLinkedList.HEAD_POSITION, KTypeLinkedList.TAIL_POSITION);

        this.valueIteratorPool = new IteratorPool<KTypeCursor<KType>, ValueIterator>(new ObjectFactory<ValueIterator>() {

            @Override
            public ValueIterator create() {
                return new ValueIterator();
            }

            @Override
            public void initialize(final ValueIterator obj) {
                obj.internalIndex = -1;
                obj.cursor.index = -1;

                obj.buffer = Intrinsics.<KType[]> cast(KTypeLinkedList.this.buffer);
                obj.pointers = KTypeLinkedList.this.beforeAfterPointers;
                obj.internalPos = KTypeLinkedList.HEAD_POSITION;
            }

            @Override
            public void reset(final ValueIterator obj) {
                // for GC sake
                obj.buffer = null;
                obj.pointers = null;

            }
        });

        this.descendingValueIteratorPool = new IteratorPool<KTypeCursor<KType>, DescendingValueIterator>(
                new ObjectFactory<DescendingValueIterator>() {

                    @Override
                    public DescendingValueIterator create() {
                        return new DescendingValueIterator();
                    }

                    @Override
                    public void initialize(final DescendingValueIterator obj) {
                        obj.internalIndex = KTypeLinkedList.this.size();
                        obj.cursor.index = KTypeLinkedList.this.size();

                        obj.buffer = Intrinsics.<KType[]> cast(KTypeLinkedList.this.buffer);
                        obj.pointers = KTypeLinkedList.this.beforeAfterPointers;
                        obj.internalPos = KTypeLinkedList.TAIL_POSITION;
                    }

                    @Override
                    public void reset(final DescendingValueIterator obj) {
                        // for GC sake
                        obj.buffer = null;
                        obj.pointers = null;
                    }
                });
    }

    /**
     * Creates a new list from elements of another container.
     */
    public KTypeLinkedList(final KTypeContainer<? extends KType> container) {
        this(container.size());
        addAll(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final KType e1) {
        addLast(e1);
    }

    /**
     * Appends two elements at the end of the list. To add more than two elements,
     * use <code>add</code> (vararg-version) or access the buffer directly (tight
     * loop).
     */
    public void add(final KType e1, final KType e2) {
        ensureBufferSpace(2);
        insertAfterPosNoCheck(e1, getLinkBefore(this.beforeAfterPointers[KTypeLinkedList.TAIL_POSITION]));

        insertAfterPosNoCheck(e2, getLinkBefore(this.beforeAfterPointers[KTypeLinkedList.TAIL_POSITION]));

    }

    /**
     * Add all elements (var-args signature) to the list, equivalent of add(final KType... elements)
     * @see #add(KType[])
     * @param elements
     */
    public void addLast(final KType... elements) {
        addLast(elements, 0, elements.length);
    }

    /**
     * Add all elements from a range of given array to the list.
     */
    public void addLast(final KType[] elements, final int start, final int length) {
        assert length + start <= elements.length : "Length is smaller than required";

        ensureBufferSpace(length);

        final long[] beforeAfterPointers = this.beforeAfterPointers;

        for (int i = 0; i < length; i++)
        {
            insertAfterPosNoCheck(elements[start + i], getLinkBefore(beforeAfterPointers[KTypeLinkedList.TAIL_POSITION]));
        }
    }

    /**
     * Add all elements from a range of given array to the list, equivalent to addLast()
     */
    public void add(final KType[] elements, final int start, final int length) {
        addLast(elements, start, length);
    }

    /**
     * Vararg-signature method for adding elements at the end of the list.
     * <p><b>This method is handy, but costly if used in tight loops (anonymous
     * array passing)</b></p>
     */
    public void add(final KType... elements) {
        addLast(elements, 0, elements.length);
    }

    /**
     * Adds all elements from another container, equivalent to addLast()
     */
    public int addAll(final KTypeContainer<? extends KType> container) {
        return addLast(container);
    }

    /**
     * Adds all elements from another iterable, equivalent to addLast()
     */
    public int addAll(final Iterable<? extends KTypeCursor<? extends KType>> iterable) {
        return addLast(iterable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insert(final int index, final KType e1) {

        ensureBufferSpace(1);

        if (index == 0) {
            insertAfterPosNoCheck(e1, KTypeLinkedList.HEAD_POSITION);
        } else {
            insertAfterPosNoCheck(e1, gotoIndex(index - 1));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KType get(final int index) {

        //get object at pos currentPos in buffer
        return Intrinsics.<KType> cast(this.buffer[gotoIndex(index)]);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KType set(final int index, final KType e1) {

        //get object at pos currentPos in buffer
        final int pos = gotoIndex(index);

        //keep the previous value
        final KType elem = Intrinsics.<KType> cast(this.buffer[pos]);

        //new value
        this.buffer[pos] = e1;

        return elem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KType remove(final int index) {

        //get object at pos currentPos in buffer
        final int currentPos = gotoIndex(index);

        final KType elem = Intrinsics.<KType> cast(this.buffer[currentPos]);

        //remove
        removeAtPosNoCheck(currentPos);

        return elem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeRange(final int fromIndex, final int toIndex) {

        //goto pos
        int currentPos = gotoIndex(fromIndex);

        if (toIndex < fromIndex || toIndex > size()) {

            throw new IndexOutOfBoundsException("Index toIndex " + toIndex + " out of bounds [" + fromIndex + ", " + size() + "].");
        }

        //start removing size elements...
        final int size = toIndex - fromIndex;
        int count = 0;

        while (count < size) {
            currentPos = removeAtPosNoCheck(currentPos);
            count++;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeFirst(final KType e1) {
        final long[] pointers = this.beforeAfterPointers;
        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);

        int currentPos = getLinkAfter(pointers[KTypeLinkedList.HEAD_POSITION]);
        int count = 0;

        while (currentPos != KTypeLinkedList.TAIL_POSITION) {
            if (Intrinsics.<KType> equals(e1, buffer[currentPos])) {
                removeAtPosNoCheck(currentPos);
                return count;
            }

            //increment
            currentPos = getLinkAfter(pointers[currentPos]);
            count++;
        }

        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeLast(final KType e1) {
        final long[] pointers = this.beforeAfterPointers;
        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);
        final int size = size();

        int currentPos = getLinkBefore(pointers[KTypeLinkedList.TAIL_POSITION]);
        int count = 0;

        while (currentPos != KTypeLinkedList.HEAD_POSITION) {
            if (Intrinsics.<KType> equals(e1, buffer[currentPos])) {
                removeAtPosNoCheck(currentPos);
                return size - count - 1;
            }

            currentPos = getLinkBefore(pointers[currentPos]);
            count++;
        }

        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeAll(final KType e1) {
        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);

        int deleted = 0;

        //real elements starts in postion 2.
        int pos = 2;

        //directly iterate the buffer, so out of order.
        while (pos < this.elementsCount) {
            if (Intrinsics.<KType> equals(e1, buffer[pos])) {
                //each time a pos is removed, pos is patched with the last element,
                //so continue to test the same position
                removeAtPosNoCheck(pos);
                deleted++;
            } else {
                pos++;
            }
        } //end while

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
        final long[] pointers = this.beforeAfterPointers;
        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);

        int currentPos = getLinkAfter(pointers[KTypeLinkedList.HEAD_POSITION]);
        int count = 0;

        while (currentPos != KTypeLinkedList.TAIL_POSITION) {
            if (Intrinsics.<KType> equals(e1, buffer[currentPos])) {
                return count;
            }

            currentPos = getLinkAfter(pointers[currentPos]);
            count++;
        }

        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int lastIndexOf(final KType e1) {
        final long[] pointers = this.beforeAfterPointers;
        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);

        int currentPos = getLinkBefore(pointers[KTypeLinkedList.TAIL_POSITION]);
        int count = 0;

        while (currentPos != KTypeLinkedList.HEAD_POSITION) {
            if (Intrinsics.<KType> equals(e1, buffer[currentPos])) {
                return size() - count - 1;
            }

            currentPos = getLinkBefore(pointers[currentPos]);
            count++;
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
     * 
     * @return true if a reallocation occurs
     */
    @SuppressWarnings("boxing")
    protected boolean ensureBufferSpace(final int expectedAdditions) {
        final int bufferLen = (this.buffer == null ? 0 : this.buffer.length);

        //this.elementsCount is size() + 2
        if (this.elementsCount > bufferLen - expectedAdditions) {
            int newSize = this.resizer.grow(bufferLen, this.elementsCount, expectedAdditions);

            //first allocation, reserve 2 more slots
            if (this.buffer == null) {
                newSize += 2;
            }

            try {

                //the first 2 slots are head/tail placeholders
                final KType[] newBuffer = Intrinsics.<KType> newArray(newSize);
                final long[] newPointers = new long[newSize];

                if (bufferLen > 0) {
                    System.arraycopy(this.buffer, 0, newBuffer, 0, this.buffer.length);
                    System.arraycopy(this.beforeAfterPointers, 0, newPointers, 0, this.beforeAfterPointers.length);
                }
                this.buffer = newBuffer;
                this.beforeAfterPointers = newPointers;

            } catch (final OutOfMemoryError e) {
                throw new BufferAllocationException(
                        "Not enough memory to allocate buffers to grow from %d -> %d elements",
                        e,
                        bufferLen,
                        newSize);
            }

            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return this.elementsCount - 2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int capacity() {

        return this.buffer.length - 2;
    }

    /**
     * Sets the number of stored elements to zero.
     */
    @Override
    public void clear() {
        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        KTypeArrays.blankArray(this.buffer, 0, this.elementsCount);
        /*! #end !*/

        //the first two are placeholders
        this.elementsCount = 2;

        //rebuild head/tail
        this.beforeAfterPointers[KTypeLinkedList.HEAD_POSITION] = getLinkNodeValue(KTypeLinkedList.HEAD_POSITION, KTypeLinkedList.TAIL_POSITION);
        this.beforeAfterPointers[KTypeLinkedList.TAIL_POSITION] = getLinkNodeValue(KTypeLinkedList.HEAD_POSITION, KTypeLinkedList.TAIL_POSITION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KType[] toArray(final KType[] target) {
        //Return in-order
        final long[] pointers = this.beforeAfterPointers;
        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);

        int index = 0;

        int currentPos = getLinkAfter(pointers[KTypeLinkedList.HEAD_POSITION]);

        while (currentPos != KTypeLinkedList.TAIL_POSITION) {
            target[index] = buffer[currentPos];
            //increment both
            currentPos = getLinkAfter(pointers[currentPos]);
            index++;
        }

        return target;
    }

    /**
     * Clone this object. The returned clone will use the same resizing strategy.
     */
    @Override
    public KTypeLinkedList<KType> clone() {
        //placeholder container
        final KTypeLinkedList<KType> cloned = new KTypeLinkedList<KType>(Containers.DEFAULT_EXPECTED_ELEMENTS, this.resizer);

        //clone raw buffers is ok
        cloned.buffer = this.buffer.clone();
        cloned.beforeAfterPointers = this.beforeAfterPointers.clone();

        cloned.elementsCount = this.elementsCount;

        return cloned;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final long[] pointers = this.beforeAfterPointers;
        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);
        int h = 1;

        int currentPos = getLinkAfter(pointers[KTypeLinkedList.HEAD_POSITION]);

        while (currentPos != KTypeLinkedList.TAIL_POSITION) {
            h = 31 * h + BitMixer.mix(buffer[currentPos]);

            //increment
            currentPos = getLinkAfter(pointers[currentPos]);
        }

        return h;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    public boolean equals(final Object obj) {
        if (obj != null) {
            if (obj == this) {
                return true;
            }

            final long[] pointers = this.beforeAfterPointers;
            final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);

            if (obj instanceof KTypeLinkedList<?>) {
                final KTypeLinkedList<?> other = (KTypeLinkedList<?>) obj;

                if (other.size() != this.size()) {
                    return false;
                }

                final long[] pointersOther = other.beforeAfterPointers;
                final KType[] bufferOther = (KType[]) other.buffer;

                //compare index/index
                int currentPos = getLinkAfter(pointers[KTypeLinkedList.HEAD_POSITION]);
                int currentPosOther = getLinkAfter(pointersOther[KTypeLinkedList.HEAD_POSITION]);

                while (currentPos != KTypeLinkedList.TAIL_POSITION && currentPosOther != KTypeLinkedList.TAIL_POSITION) {
                    if (!Intrinsics.<KType> equals(buffer[currentPos], bufferOther[currentPosOther])) {
                        return false;
                    }

                    //increment both
                    currentPos = getLinkAfter(pointers[currentPos]);
                    currentPosOther = getLinkAfter(pointersOther[currentPosOther]);
                }

                return true;
            } else if (obj instanceof KTypeDeque<?>) {

                final KTypeDeque<KType> other = (KTypeDeque<KType>) obj;

                if (other.size() != this.size()) {
                    return false;
                }

                //compare index/index
                int currentPos = getLinkAfter(pointers[KTypeLinkedList.HEAD_POSITION]);

                //request a pooled iterator
                final Iterator<KTypeCursor<KType>> it = other.iterator();

                KTypeCursor<KType> c;

                //iterate over the linkedList
                while (currentPos != KTypeLinkedList.TAIL_POSITION) {
                    c = it.next();

                    if (!Intrinsics.<KType> equals(buffer[currentPos], c.value)) {
                        //if iterator was pooled, recycled it
                        if (it instanceof AbstractIterator<?>) {
                            ((AbstractIterator<?>) it).release();
                        }

                        return false;
                    }

                    //increment both
                    currentPos = getLinkAfter(pointers[currentPos]);
                } //end while

                //if iterator was pooled, recycled it
                if (it instanceof AbstractIterator<?>) {
                    ((AbstractIterator<?>) it).release();
                }

                return true;
            } else if (obj instanceof KTypeIndexedContainer<?>) {

                final KTypeIndexedContainer<KType> other = (KTypeIndexedContainer<KType>) obj;

                if (other.size() != this.size()) {
                    return false;
                }

                //compare index/index
                int currentPos = getLinkAfter(pointers[KTypeLinkedList.HEAD_POSITION]);
                int index = 0;

                while (currentPos != KTypeLinkedList.TAIL_POSITION) {
                    if (!Intrinsics.<KType> equals(buffer[currentPos], other.get(index))) {
                        return false;
                    }

                    //increment both
                    currentPos = getLinkAfter(pointers[currentPos]);
                    index++;
                }

                return true;
            }
        }
        return false;
    }

    /**
     * Traverse the list to index, return the position in buffer
     * when reached. This is a O(n) operation !.
     * 
     * @param index
     * @return
     */
    protected int gotoIndex(final int index) {

        if (index < 0 || index >= size()) {

            throw new IndexOutOfBoundsException("Index " + index + " out of bounds [" + 0 + ", size=" + size() + "[.");
        }

        int currentPos = KTypeLinkedList.TAIL_POSITION;
        int currentIndex = -1;
        final long[] pointers = this.beforeAfterPointers;

        //A) Search by head
        if (index <= (this.elementsCount / 2.0)) {
            //reach index - 1 position, from head, insert after
            currentIndex = 0;
            currentPos = getLinkAfter(pointers[KTypeLinkedList.HEAD_POSITION]);

            while (currentIndex < index && currentPos != KTypeLinkedList.TAIL_POSITION) {
                currentPos = getLinkAfter(pointers[currentPos]);
                currentIndex++;
            }
        } else {
            //B) short path to go from tail,
            //reach index - 1 position, from head, insert after
            currentIndex = size() - 1;
            currentPos = getLinkBefore(pointers[KTypeLinkedList.TAIL_POSITION]);

            while (currentIndex > index && currentPos != KTypeLinkedList.HEAD_POSITION) {
                currentPos = getLinkBefore(pointers[currentPos]);
                currentIndex--;
            }
        }

        assert currentIndex == index;

        return currentPos;
    }

    /**
     * Insert after position insertionPos.
     * Insert an element just after insertionPos element.
     * 
     * @param e1
     * @param insertionPos
     */
    private void insertAfterPosNoCheck(final KType e1, final int insertionPos) {
        final long[] pointers = this.beforeAfterPointers;

        //we insert between insertionPos and its successor, keep reference of the successor
        final int nextAfterInsertionPos = getLinkAfter(pointers[insertionPos]);

        //the new element is taken at the end of the buffer, at elementsCount.
        //link it as: [insertionPos | nextAfterInsertionPos]
        pointers[this.elementsCount] = getLinkNodeValue(insertionPos, nextAfterInsertionPos);

        //re-link insertionPos element (left) :
        //[.. | nextAfterInsertionPos] ==> [.. | elementsCount]
        pointers[insertionPos] = setLinkAfterNodeValue(pointers[insertionPos], this.elementsCount);

        //re-link nextAfterInsertionPos element (right)
        //[insertionPos |..] ==> [elementsCount | ..]
        pointers[nextAfterInsertionPos] = setLinkBeforeNodeValue(pointers[nextAfterInsertionPos], this.elementsCount);

        //really add element to buffer at position elementsCount
        this.buffer[this.elementsCount] = e1;
        this.elementsCount++;
    }

    /**
     * Remove element at position removalPos in buffer
     * 
     * @param removalPos
     * @return the next valid position after the removal, supposing head to tail iteration.
     */
    private int removeAtPosNoCheck(final int removalPos) {
        final long[] pointers = this.beforeAfterPointers;

        //A) Unlink removalPos properly
        final int beforeRemovalPos = getLinkBefore(pointers[removalPos]);
        final int afterRemovalPos = getLinkAfter(pointers[removalPos]);

        //the element before element removalPos is now linked to afterRemovalPos :
        //[... | removalPos] ==> [... | afterRemovalPos]
        pointers[beforeRemovalPos] = setLinkAfterNodeValue(pointers[beforeRemovalPos], afterRemovalPos);

        //the element after element removalPos is now linked to beforeRemovalPos :
        //[removalPos | ...] ==> [beforeRemovalPos | ...]
        pointers[afterRemovalPos] = setLinkBeforeNodeValue(pointers[afterRemovalPos], beforeRemovalPos);

        //if the removed element is not the last of the buffer, move it to the "hole" created at removalPos
        if (removalPos != this.elementsCount - 1) {
            //B) To keep the buffer compact, take now the last buffer element and put it to  removalPos
            //keep the positions of the last buffer element, because we'll need to re-link it after
            //moving the element elementsCount - 1
            final int beforeLastElementPos = getLinkBefore(pointers[this.elementsCount - 1]);
            final int afterLastElementPos = getLinkAfter(pointers[this.elementsCount - 1]);

            //To keep the buffer compact, take now the last buffer element and put it to  removalPos
            this.buffer[removalPos] = this.buffer[this.elementsCount - 1];
            pointers[removalPos] = pointers[this.elementsCount - 1];

            //B-2) Re-link the elements that where neighbours of "elementsCount - 1" to point to removalPos element now
            //update the pointer of the before the last element = [... | elementsCount - 1] ==> [... | removalPos]
            pointers[beforeLastElementPos] = setLinkAfterNodeValue(pointers[beforeLastElementPos], removalPos);

            //update the pointer of the after the last element = [... | elementsCount - 1] ==> [... | removalPos]
            pointers[afterLastElementPos] = setLinkBeforeNodeValue(pointers[afterLastElementPos], removalPos);
        }

        //for GC
        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        this.buffer[this.elementsCount - 1] = Intrinsics.<KType> empty();
        /*! #end !*/

        this.elementsCount--;

        return getLinkAfter(pointers[beforeRemovalPos]);
    }

    /**
     * An iterator implementation for {@link ObjectLinkedList#iterator}.
     * <pre>
     * <b>
     * Important note: DO NOT USE java.util.Iterator methods ! They are here only for enhanced-loop syntax and compatibility. Use
     * the specialized methods instead !
     * Iteration example:
     * </b></pre>
     * <pre>
     *   KTypeLinkedList.ValueIterator it = null;
     *   try
     *   {
     *       for (it = list.iterator().gotoNext(); !it.isTail(); it.gotoNext())
     *       {
     *
     *          // do something
     *       }
     *   }
     *   finally
     *   {
     *       //do not forget to release the iterator !
     *       it.release();
     *   }
     * </pre>
     */
    public class ValueIterator extends AbstractIterator<KTypeCursor<KType>>
    {
        public final KTypeCursor<KType> cursor;

        protected final KTypeCursor<KType> tmpCursor;

        KType[] buffer;
        int internalPos;
        long[] pointers;
        int internalIndex = -1;

        public ValueIterator() {
            this.cursor = new KTypeCursor<KType>();
            this.tmpCursor = new KTypeCursor<KType>();

            this.internalIndex = -1;
            this.cursor.index = -1;

            this.buffer = Intrinsics.<KType[]> cast(KTypeLinkedList.this.buffer);
            this.pointers = KTypeLinkedList.this.beforeAfterPointers;
            this.internalPos = KTypeLinkedList.HEAD_POSITION;
        }

        /**
         * <b>
         * DO NOT USE, unsupported operation.
         * </b>
         */
        @Override
        protected KTypeCursor<KType> fetch() {
            throw new UnsupportedOperationException();
        }

        /**
         * <b>
         * DO NOT USE directly, It is here only for the enhanced for loop syntax support.
         * </b>
         */
        @Override
        public boolean hasNext() {
            final int nextPos = getLinkAfter(this.pointers[this.internalPos]);

            //auto-release when hasNext() returns false;
            if (nextPos == KTypeLinkedList.TAIL_POSITION && this.iteratorPool != null && !this.isFree) {
                this.iteratorPool.release(this);
                this.isFree = true;
            }

            return nextPos != KTypeLinkedList.TAIL_POSITION;
        }

        /**
         * <b>
         * DO NOT USE directly, It is here only for the enhanced for loop syntax support.
         * </b>
         */
        @Override
        public KTypeCursor<KType> next() {
            //search for the next position
            final int nextPos = getLinkAfter(this.pointers[this.internalPos]);

            //we are at tail already.
            if (nextPos == KTypeLinkedList.TAIL_POSITION) {
                throw new NoSuchElementException();
            }

            //point to next
            this.internalPos = nextPos;
            this.internalIndex++;

            this.cursor.index = this.internalIndex;
            this.cursor.value = this.buffer[nextPos];

            return this.cursor;
        }

        ///////////////////////// Forward iteration methods //////////////////////////////////////
        /**
         * True is iterator points to the "head", i.e such as gotoNext() point to the first
         * element, with respect to the forward iteration.
         * 
         */
        public boolean isHead() {
            return this.internalPos == KTypeLinkedList.HEAD_POSITION;
        }

        /**
         * True is iterator points to the "tail", i.e such as gotoPrevious() point to the last
         * element, with respect to the forward iteration.
         * 
         */
        public boolean isTail() {
            return this.internalPos == KTypeLinkedList.TAIL_POSITION;
        }

        /**
         * True if the iterator points to the first element
         * with respect to the forward iteration. Always true if the list is empty.
         */
        public boolean isFirst() {
            final int nextPos = getLinkAfter(this.pointers[KTypeLinkedList.HEAD_POSITION]);

            return (nextPos == KTypeLinkedList.TAIL_POSITION) ? true : (nextPos == this.internalPos);
        }

        /**
         * True if the iterator points to the last element
         * with respect to the forward iteration. Always true if the list is empty.
         */
        public boolean isLast() {
            final int nextPos = getLinkBefore(this.pointers[KTypeLinkedList.TAIL_POSITION]);

            return (nextPos == KTypeLinkedList.HEAD_POSITION) ? true : (nextPos == this.internalPos);
        }

        /**
         * Move the iterator to the "head", returning itself for chaining.
         */
        public ValueIterator gotoHead() {
            this.internalIndex = -1;
            this.internalPos = KTypeLinkedList.HEAD_POSITION;

            //update cursor
            this.cursor.index = -1;

            return this;
        }

        /**
         * Move the iterator to the "tail", returning itself for chaining.
         */
        public ValueIterator gotoTail() {
            this.internalIndex = KTypeLinkedList.this.size();
            this.internalPos = KTypeLinkedList.TAIL_POSITION;

            //update cursor
            this.cursor.index = this.internalIndex;

            return this;
        }

        /**
         * Move the iterator to the next element, with respect to the forward iteration,
         * returning itself for chaining. When "tail" is reached, gotoNext() stays in place, at tail.
         */
        public ValueIterator gotoNext() {
            this.internalPos = getLinkAfter(this.pointers[this.internalPos]);

            if (this.internalPos == KTypeLinkedList.TAIL_POSITION) {
                this.internalIndex = KTypeLinkedList.this.size();
                //update cursor
                this.cursor.index = this.internalIndex;

            } else {
                this.internalIndex++;

                //update cursor
                this.cursor.index = this.internalIndex;
                this.cursor.value = this.buffer[this.internalPos];
            }

            return this;
        }

        /**
         * Move the iterator to the previous element, with respect to the forward iteration,
         * returning itself for chaining. When "head" is reached, gotoPrevious() stays in place, at head.
         */
        public ValueIterator gotoPrevious() {
            this.internalPos = getLinkBefore(this.pointers[this.internalPos]);

            if (this.internalPos == KTypeLinkedList.HEAD_POSITION) {
                this.internalIndex = -1;
                this.cursor.index = this.internalIndex;

            } else {
                this.internalIndex--;
                this.cursor.index = this.internalIndex;
                this.cursor.value = this.buffer[this.internalPos];
            }

            return this;
        }

        /**
         * Get the next element value with respect to the forward iteration,
         * without moving the iterator.
         * Returns null if no such element exists.
         */
        public KTypeCursor<KType> getNext() {
            final int nextPos = getLinkAfter(this.pointers[this.internalPos]);

            if (nextPos == KTypeLinkedList.TAIL_POSITION) {
                return null;
            }
            //use the temporary Cursor in order to protect the iterator cursor
            this.tmpCursor.index = this.internalIndex + 1;
            this.tmpCursor.value = this.buffer[nextPos];

            return this.tmpCursor;
        }

        /**
         * Get the previous element value with respect to the forward iteration,
         * Returns null if no such element exists.
         */
        public KTypeCursor<KType> getPrevious() {
            final int beforePos = getLinkBefore(this.pointers[this.internalPos]);

            if (beforePos == KTypeLinkedList.HEAD_POSITION) {
                return null;
            }

            //use the temporary Cursor in order to protect the iterator cursor
            this.tmpCursor.index = this.internalIndex - 1;
            this.tmpCursor.value = this.buffer[beforePos];

            return this.tmpCursor;
        }

        /**
         * Removes the next element, without moving the iterator.
         * Returns the removed element cursor, or null if the removal failed.
         * (because there is no element left after)
         */
        public KTypeCursor<KType> removeNext() {
            final int nextPos = getLinkAfter(this.pointers[this.internalPos]);

            if (nextPos == KTypeLinkedList.TAIL_POSITION) {
                return null;
            }

            //store the next positions in returned cursor
            this.tmpCursor.index = this.internalIndex + 1;
            this.tmpCursor.value = this.buffer[nextPos];

            // the current position is unaffected.
            removeAtPosNoCheck(nextPos);

            return this.tmpCursor;
        }

        /**
         * Removes the previous element, without moving the iterator.
         * Returns the removed element cursor, or null if the removal failed.
         * (because there is no element left before)
         */
        public KTypeCursor<KType> removePrevious() {
            final int previousPos = getLinkBefore(this.pointers[this.internalPos]);

            if (previousPos == KTypeLinkedList.HEAD_POSITION) {
                return null;
            }

            //store the next positions in returned cursor
            this.tmpCursor.index = this.internalIndex - 1;
            this.tmpCursor.value = this.buffer[previousPos];

            //update the new current position
            this.internalPos = removeAtPosNoCheck(previousPos);

            //the internal index changed, update the cursor index, the buffer stays unchanged.
            this.internalIndex--;
            this.cursor.index = this.internalIndex;

            return this.tmpCursor;
        }

        /**
         * Insert e1 before the iterator position, without moving the iterator.
         */
        public void insertBefore(final KType e1) {
            //protect the head
            if (this.internalPos != KTypeLinkedList.HEAD_POSITION) {
                //assure growing, and grab the new arrays references if needed
                if (ensureBufferSpace(1)) {

                    this.pointers = KTypeLinkedList.this.beforeAfterPointers;
                    this.buffer = Intrinsics.<KType[]> cast(KTypeLinkedList.this.buffer);
                }

                final int beforePos = getLinkBefore(this.pointers[this.internalPos]);

                //we insert after the previous
                insertAfterPosNoCheck(e1, beforePos);

                //the internal index changed, update the cursor index, the buffer stays unchanged.
                this.internalIndex++;
                this.cursor.index = this.internalIndex;
            }
        }

        /**
         * Insert e1 after the iterator position, without moving the iterator.
         */
        public void insertAfter(final KType e1) {
            //protect the tail
            if (this.internalPos != KTypeLinkedList.TAIL_POSITION) {
                //assure growing, and grab the new arrays references if needed
                if (ensureBufferSpace(1)) {

                    this.pointers = KTypeLinkedList.this.beforeAfterPointers;
                    this.buffer = Intrinsics.<KType[]> cast(KTypeLinkedList.this.buffer);
                }

                //we insert after us
                insertAfterPosNoCheck(e1, this.internalPos);
            }

            //the internal index doesn't change...
        }

        /**
         * Set e1 to the current iterator (position), without moving the iterator.
         */
        public void set(final KType e1) {

            //protect the heads/tails
            if (this.internalPos != KTypeLinkedList.HEAD_POSITION && this.internalPos != KTypeLinkedList.TAIL_POSITION) {

                this.buffer[this.internalPos] = e1;
                //update cursor value
                this.cursor.value = e1;
            }

        }

        /**
         * Delete the current iterator position, and moves to the next valid
         * element after this removal, with respect to the forward iteration.
         * Returns the iterator itself for chaining.
         */
        public ValueIterator delete() {
            //protect the heads/tails
            if (this.internalPos != KTypeLinkedList.HEAD_POSITION && this.internalPos != KTypeLinkedList.TAIL_POSITION) {
                this.internalPos = removeAtPosNoCheck(this.internalPos);

                //update cursor returned by iterator
                this.cursor.value = this.buffer[this.internalPos];
                //internal index doesn't change
            }

            return this;
        }

    }

    /**
     * An iterator implementation for {@link ObjectLinkedList#descendingIterator()}.
     * <pre><b>
     * Important note: DO NOT USE java.util.Iterator methods ! They are here only for compatibility. Use
     * the specialized methods instead !
     * </b></pre>
     * Iteration example:
     * </b>
     * <pre>
     * KTypeLinkedList.ValueIterator it = null;
     * try
     * {
     * for (it = list.iterator().gotoNext(); !it.isTail(); it.gotoNext())
     * {
     * // do something
     * }
     * }
     * finally
     * {
     * //do not forget to release the iterator !
     * it.release();
     * }
     * </pre>
     */
    public final class DescendingValueIterator extends ValueIterator
    {
        public DescendingValueIterator() {
            super();

            this.internalIndex = KTypeLinkedList.this.size();

            this.cursor.index = KTypeLinkedList.this.size();

            this.buffer = Intrinsics.<KType[]> cast(KTypeLinkedList.this.buffer);
            this.pointers = KTypeLinkedList.this.beforeAfterPointers;
            this.internalPos = KTypeLinkedList.TAIL_POSITION;
        }

        /**
         * <b>
         * DO NOT USE.
         * </b>
         */
        @Override
        public boolean hasNext() {
            final int nextPos = getLinkBefore(this.pointers[this.internalPos]);

            //auto-release when hasNext() returns false;
            if (nextPos == KTypeLinkedList.HEAD_POSITION && this.iteratorPool != null && !this.isFree) {
                this.iteratorPool.release(this);
                this.isFree = true;
            }

            return nextPos != KTypeLinkedList.HEAD_POSITION;
        }

        /**
         * <b>
         * DO NOT USE directly, It is here only for the enhanced for loop syntax support.
         * </b>
         */
        @Override
        public KTypeCursor<KType> next() {
            //search for the next position
            final int nextPos = getLinkBefore(this.pointers[this.internalPos]);

            //we are at tail already.
            if (nextPos == KTypeLinkedList.HEAD_POSITION) {
                throw new NoSuchElementException();
            }

            //point to next
            this.internalPos = nextPos;
            this.internalIndex--;

            this.cursor.index = this.internalIndex;
            this.cursor.value = this.buffer[nextPos];

            return this.cursor;
        }

        ///////////////////////// Descending iteration methods //////////////////////////////////////
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isHead() {
            return super.isTail();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isTail() {
            return super.isHead();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isFirst() {
            return super.isLast();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isLast() {
            return super.isFirst();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ValueIterator gotoHead() {
            return super.gotoTail();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("unchecked")
        public DescendingValueIterator gotoTail() {
            return (DescendingValueIterator) super.gotoHead();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("unchecked")
        public DescendingValueIterator gotoNext() {
            return (DescendingValueIterator) super.gotoPrevious();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("unchecked")
        public DescendingValueIterator gotoPrevious() {
            return (DescendingValueIterator) super.gotoNext();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public KTypeCursor<KType> getNext() {
            return super.getPrevious();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public KTypeCursor<KType> getPrevious() {
            return super.getNext();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public KTypeCursor<KType> removeNext() {
            return super.removePrevious();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public KTypeCursor<KType> removePrevious() {
            return super.removeNext();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void insertBefore(final KType e1) {
            super.insertAfter(e1);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void insertAfter(final KType e1) {
            super.insertBefore(e1);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DescendingValueIterator delete() {
            if (this.internalPos != KTypeLinkedList.HEAD_POSITION && this.internalPos != KTypeLinkedList.TAIL_POSITION) {
                //this is the next position in the normal iteration direction, so we must go back one step
                final int nextPos = removeAtPosNoCheck(this.internalPos);

                this.internalPos = getLinkBefore(this.pointers[nextPos]);

                this.internalIndex--;
                //update cursor
                this.cursor.value = this.buffer[this.internalPos];
                //index is decremented
                this.cursor.index = this.internalIndex;
            }

            return this;
        }
    }

    /**
     * 
     * {@inheritDoc}
     * The iterator is implemented as a cursor and it returns <b>the same cursor instance</b>
     * on every call to {@link Iterator#next()} (to avoid boxing of primitive types). To
     * read the current value, or index as in {@link #get(index)}, use the cursor's public
     * fields.
     * The iterator points to "head", such as ValueIterator.gotoNext()
     * is the first element of the list.
     * <b>
     * Important note: java.util.Iterator methods are error-prone, and only there for compatibility and enhanced for loop usage:
     * <pre>
     * for (KTypeCursor c : container) {
     * System.out.println("index=" + c.index + " value=" + c.value);
     * }
     * </pre>
     * 
     * Prefer the specialized methods instead :
     * 
     * <pre>
     * ValueIterator it = list.iterator();
     * while (!it.isTail())
     * {
     * final KTypeCursor c = it.gotoNext();
     * System.out.println(&quot;buffer index=&quot;
     * + c.index + &quot; value=&quot; + c.value);
     * }
     * 
     * // release the iterator at the end !
     * it.release()
     * </pre>
     * 
     * </b>
     * 
     * @see ValueIterator#isHead()
     */
    @Override
    public ValueIterator iterator() {
        //return new ValueIterator();
        return this.valueIteratorPool.borrow();
    }

    /**
     * Returns an iterator over the values of this list (in tail to head order).
     * The iterator points to the "head", such as DescendingValueIterator.gotoNext()
     * is the last element of the list, since it is a "reversed" iteration.
     * The iterator is implemented as a cursor and it returns <b>the same cursor instance</b>
     * on every call to {@link Iterator#next()} (to avoid boxing of primitive types). To
     * read the current value, or index as in {@link #get(index)}, use the cursor's public
     * fields. An example is shown below.
     * <b>
     * Important note: java.util.Iterator methods are error-prone, and only there for compatibility. Use
     * the specialized methods instead :
     * * <pre>
     * DescendingValueIterator it = list.descendingIterator();
     * while (!it.isTail())
     * {
     * final KTypeCursor c = it.gotoNext();
     * System.out.println(&quot;buffer index=&quot;
     * + c.index + &quot; value=&quot; + c.value);
     * }
     * 
     * // release the iterator at the end !
     * it.release()
     * </pre>
     * 
     * </b>
     * 
     * @see ValueIterator#isHead()
     */
    @Override
    public DescendingValueIterator descendingIterator() {
        //return new DescendingValueIterator();
        return this.descendingValueIteratorPool.borrow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends KTypeProcedure<? super KType>> T forEach(final T procedure) {
        final long[] pointers = this.beforeAfterPointers;

        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);

        int currentPos = getLinkAfter(pointers[KTypeLinkedList.HEAD_POSITION]);

        while (currentPos != KTypeLinkedList.TAIL_POSITION) {
            procedure.apply(buffer[currentPos]);

            currentPos = getLinkAfter(pointers[currentPos]);

        } //end while

        return procedure;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends KTypePredicate<? super KType>> T forEach(final T predicate) {
        final long[] pointers = this.beforeAfterPointers;

        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);

        int currentPos = getLinkAfter(pointers[KTypeLinkedList.HEAD_POSITION]);

        while (currentPos != KTypeLinkedList.TAIL_POSITION) {
            if (!predicate.apply(buffer[currentPos])) {
                break;
            }

            currentPos = getLinkAfter(pointers[currentPos]);

        } //end while

        return predicate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends KTypeProcedure<? super KType>> T descendingForEach(final T procedure) {
        final long[] pointers = this.beforeAfterPointers;

        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);

        int currentPos = getLinkBefore(pointers[KTypeLinkedList.TAIL_POSITION]);

        while (currentPos != KTypeLinkedList.HEAD_POSITION) {
            procedure.apply(buffer[currentPos]);

            currentPos = getLinkBefore(pointers[currentPos]);

        } //end while

        return procedure;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends KTypePredicate<? super KType>> T descendingForEach(final T predicate) {
        final long[] pointers = this.beforeAfterPointers;

        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);

        int currentPos = getLinkBefore(pointers[KTypeLinkedList.TAIL_POSITION]);

        while (currentPos != KTypeLinkedList.HEAD_POSITION) {
            if (!predicate.apply(buffer[currentPos])) {
                break;
            }

            currentPos = getLinkBefore(pointers[currentPos]);

        } //end while

        return predicate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeAll(final KTypePredicate<? super KType> predicate) {
        final KType[] buffer = Intrinsics.<KType[]> cast(this.buffer);

        int deleted = 0;

        //real elements starts in position 2.
        int pos = 2;

        //directly iterate the buffer, so out of order.
        while (pos < this.elementsCount) {
            if (predicate.apply(buffer[pos])) {
                //each time a pos is removed, pos itself is patched with the last element,
                //so continue to test the same position.
                removeAtPosNoCheck(pos);
                deleted++;
            } else {
                pos++;
            }
        } //end while

        return deleted;
    }

    /**
     * Returns a new object of this class with no need to declare generic type (shortcut
     * instead of using a constructor).
     */
    public static/* #if ($TemplateOptions.KTypeGeneric) */<KType> /* #end */
            KTypeLinkedList<KType> newInstance() {
        return new KTypeLinkedList<KType>();
    }

    /**
     * Returns a new object of this class with no need to declare generic type (shortcut
     * instead of using a constructor).
     */
    public static/* #if ($TemplateOptions.KTypeGeneric) */<KType> /* #end */
            KTypeLinkedList<KType> newInstance(final int initialCapacity) {
        return new KTypeLinkedList<KType>(initialCapacity);
    }

    /**
     * Create a list from a variable number of arguments or an array of <code>KType</code>.
     * The elements are copied from the argument to the internal buffer.
     */
    public static/* #if ($TemplateOptions.KTypeGeneric) */<KType> /* #end */
            KTypeLinkedList<KType> from(final KType... elements) {
        final KTypeLinkedList<KType> list = new KTypeLinkedList<KType>(elements.length);
        list.add(elements);
        return list;
    }

    /**
     * Create a list from elements of another container.
     */
    public static/* #if ($TemplateOptions.KTypeGeneric) */<KType> /* #end */
            KTypeLinkedList<KType> from(final KTypeContainer<KType> container) {
        return new KTypeLinkedList<KType>(container);
    }

    /**
     * In-place sort the list from [beginIndex, endIndex[
     * by natural ordering (smaller first)
     * <p><b>
     * WARNING: This method runs in O(n*n*log(n)). Consider yourself warned.
     * </b></p>
     * 
    #if ($TemplateOptions.KTypeGeneric)
     * <p><b>
     * This sort is NOT stable.
     * </b></p>
     * @throws ClassCastException if the list contains elements that are not mutually Comparable.
    #end
     * 
     * @param beginIndex the start index to be sorted
     * @param endIndex the end index to be sorted (excluded)
     */
    public void sort(final int beginIndex, final int endIndex) {

        if (endIndex - beginIndex > 1) {
            KTypeSort.quicksort(this, beginIndex, endIndex);
        }
    }

    /**
     * In-place sort the list from [beginIndex, endIndex[
     * using a #if ($TemplateOptions.KTypeGeneric) <code>Comparator</code> #else <code>KTypeComparator</code> #end
     * <p><b>
     * WARNING: This method runs in O(n*n*log(n)). Consider yourself warned.
     * </b></p>
     * 
    #if ($TemplateOptions.KTypeGeneric)
     * <p><b>
     * This sort is NOT stable.
     * </b></p>
    #end
     * 
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
            KTypeSort.quicksort(this, beginIndex, endIndex, comp);
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

        if (this.elementsCount > 3) {
            final int elementsCount = this.elementsCount;
            final long[] pointers = this.beforeAfterPointers;

            KTypeSort.quicksort(this.buffer, 2, elementsCount);

            //rebuild nodes, in order
            //a) rebuild head/tail

            //ties HEAD to the first element, and first element to HEAD
            pointers[KTypeLinkedList.HEAD_POSITION] = getLinkNodeValue(KTypeLinkedList.HEAD_POSITION, 2);
            pointers[2] = getLinkNodeValue(KTypeLinkedList.HEAD_POSITION, 3);

            for (int pos = 3; pos < elementsCount - 1; pos++) {
                pointers[pos] = getLinkNodeValue(pos - 1, pos + 1);
            }

            //ties the last element to tail, and tail to last element
            pointers[elementsCount - 1] = getLinkNodeValue(elementsCount - 2, KTypeLinkedList.TAIL_POSITION);
            pointers[KTypeLinkedList.TAIL_POSITION] = getLinkNodeValue(elementsCount - 1, KTypeLinkedList.TAIL_POSITION);
        }
    }

    ////////////////////////////

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
        if (this.elementsCount > 3) {
            final int elementsCount = this.elementsCount;
            final long[] pointers = this.beforeAfterPointers;

            KTypeSort.quicksort(Intrinsics.<KType[]> cast(this.buffer), 2, elementsCount, comp);

            //rebuild nodes, in order
            //a) rebuild head/tail

            //ties HEAD to the first element, and first element to HEAD
            pointers[KTypeLinkedList.HEAD_POSITION] = getLinkNodeValue(KTypeLinkedList.HEAD_POSITION, 2);
            pointers[2] = getLinkNodeValue(KTypeLinkedList.HEAD_POSITION, 3);

            for (int pos = 3; pos < elementsCount - 1; pos++) {
                pointers[pos] = getLinkNodeValue(pos - 1, pos + 1);
            }

            //ties the last element to tail, and tail to last element
            pointers[elementsCount - 1] = getLinkNodeValue(elementsCount - 2, KTypeLinkedList.TAIL_POSITION);
            pointers[KTypeLinkedList.TAIL_POSITION] = getLinkNodeValue(elementsCount - 1,
                    KTypeLinkedList.TAIL_POSITION);
        }
    }

    @Override
    public void addFirst(final KType e1) {
        ensureBufferSpace(1);
        insertAfterPosNoCheck(e1, KTypeLinkedList.HEAD_POSITION);
    }

    /**
     * Inserts all elements from the given container to the front of this list.
     * 
     * @return Returns the number of elements actually added as a result of this
     * call.
     */
    public int addFirst(final KTypeContainer<? extends KType> container) {
        return addFirst((Iterable<? extends KTypeCursor<? extends KType>>) container);
    }

    /**
     * Vararg-signature method for adding elements at the front of this deque.
     * 
     * <p><b>This method is handy, but costly if used in tight loops (anonymous
     * array passing)</b></p>
     */
    public void addFirst(final KType... elements) {
        ensureBufferSpace(elements.length);
        // For now, naive loop.
        for (int i = 0; i < elements.length; i++) {
            insertAfterPosNoCheck(elements[i], KTypeLinkedList.HEAD_POSITION);
        }
    }

    /**
     * Inserts all elements from the given iterable to the front of this list.
     * 
     * @return Returns the number of elements actually added as a result of this call.
     */
    public int addFirst(final Iterable<? extends KTypeCursor<? extends KType>> iterable) {
        int size = 0;

        for (final KTypeCursor<? extends KType> cursor : iterable) {
            ensureBufferSpace(1);
            insertAfterPosNoCheck(cursor.value, KTypeLinkedList.HEAD_POSITION);
            size++;
        }
        return size;
    }

    @Override
    public void addLast(final KType e1) {
        ensureBufferSpace(1);
        insertAfterPosNoCheck(e1, getLinkBefore(this.beforeAfterPointers[KTypeLinkedList.TAIL_POSITION]));
    }

    /**
     * Inserts all elements from the given container to the end of this list.
     * 
     * @return Returns the number of elements actually added as a result of this
     * call.
     */
    public int addLast(final KTypeContainer<? extends KType> container) {
        return addLast((Iterable<? extends KTypeCursor<? extends KType>>) container);
    }

    /**
     * Inserts all elements from the given iterable to the end of this list.
     * 
     * @return Returns the number of elements actually added as a result of this call.
     */
    public int addLast(final Iterable<? extends KTypeCursor<? extends KType>> iterable) {
        int size = 0;
        for (final KTypeCursor<? extends KType> cursor : iterable) {
            ensureBufferSpace(1);
            insertAfterPosNoCheck(cursor.value,
                    getLinkBefore(this.beforeAfterPointers[KTypeLinkedList.TAIL_POSITION]));
            size++;
        }
        return size;
    }

    @Override
    public KType removeFirst() {
        assert size() > 0;

        final int removedPos = getLinkAfter(this.beforeAfterPointers[KTypeLinkedList.HEAD_POSITION]);

        final KType elem = Intrinsics.<KType> cast(this.buffer[removedPos]);

        removeAtPosNoCheck(removedPos);

        return elem;
    }

    @Override
    public KType removeLast() {
        assert size() > 0;

        final int removedPos = getLinkBefore(this.beforeAfterPointers[KTypeLinkedList.TAIL_POSITION]);

        final KType elem = Intrinsics.<KType> cast(this.buffer[removedPos]);

        removeAtPosNoCheck(removedPos);

        return elem;
    }

    @Override
    public KType getFirst() {
        assert size() > 0;

        return Intrinsics.<KType> cast(this.buffer[getLinkAfter(this.beforeAfterPointers[KTypeLinkedList.HEAD_POSITION])]);
    }

    @Override
    public KType getLast() {
        assert size() > 0;

        return Intrinsics.<KType> cast(this.buffer[getLinkBefore(this.beforeAfterPointers[KTypeLinkedList.TAIL_POSITION])]);
    }

    /*! #if ($TemplateOptions.declareInline("getLinkNodeValue(beforeIndex, afterIndex)",
     "<*>==>((long) beforeIndex << 32) | afterIndex")) !*/
    /**
     * Builds a LinkList node value from its before an after links.
     * (actual method is inlined in generated code)
     * 
     * @param beforeIndex
     * @param afterIndex
     * @return long
     */
    private long getLinkNodeValue(final int beforeIndex, final int afterIndex) {
        return ((long) beforeIndex << 32) | afterIndex;
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.declareInline("getLinkBefore(nodeValue)", "<*>==>(int) (nodeValue >> 32)")) !*/
    private int getLinkBefore(final long nodeValue) {
        return (int) (nodeValue >> 32);
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.declareInline("getLinkAfter(nodeValue)",
       "<*>==>(int) (nodeValue & 0x00000000FFFFFFFFL)")) !*/
    private int getLinkAfter(final long nodeValue) {
        return (int) (nodeValue & 0x00000000FFFFFFFFL);
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.declareInline("setLinkBeforeNodeValue(nodeValue, newBefore)",
     "<*>==>((long) newBefore << 32) | (nodeValue & 0x00000000FFFFFFFFL)")) !*/
    private long setLinkBeforeNodeValue(final long nodeValue, final int newBefore) {
        return ((long) newBefore << 32) | (nodeValue & 0x00000000FFFFFFFFL);
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.declareInline("setLinkAfterNodeValue(nodeValue, newAfter)",
      "<*>==> newAfter | (nodeValue & 0xFFFFFFFF00000000L)")) !*/
    private long setLinkAfterNodeValue(final long nodeValue, final int newAfter) {
        return newAfter | (nodeValue & 0xFFFFFFFF00000000L);
    }
    /*! #end !*/
}
