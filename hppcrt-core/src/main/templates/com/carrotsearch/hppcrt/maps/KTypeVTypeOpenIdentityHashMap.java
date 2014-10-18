package com.carrotsearch.hppcrt.maps;

import java.util.*;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.cursors.*;
import com.carrotsearch.hppcrt.predicates.*;
import com.carrotsearch.hppcrt.procedures.*;

/*! ${TemplateOptions.doNotGenerateKType("BOOLEAN", "BYTE", "CHAR", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE")} !*/
/*! #set( $SINGLE_ARRAY = true) !*/
/*! #set( $DEBUG = false) !*/
//If SA is defined, no allocated array is used but instead default sentinel values
/*! #set( $SA =  $SINGLE_ARRAY ) !*/
/**
 * An identity hash map of <code>KType</code> to <code>VType</code>, implemented using open
 * addressing with linear probing for collision resolution.
 *
 * The difference with {@link KTypeVTypeOpenHashMap} is that it uses direct Object reference equality for comparison and
 * direct "address" {@link System#identityHashCode(Object)} for hashCode(), instead of using
 * the built-in hashCode() /  equals().
 * The internal buffers of this implementation ({@link #keys},{@link #values}, etc...)
 * are always allocated to the nearest size that is a power of two. When
 * the capacity exceeds the given load factor, the buffer size is doubled.
 * </p>
 * 
#if ($TemplateOptions.KTypeGeneric)
 * <p>This implementation supports <code>null</code> keys.</p>
#end
#if ($TemplateOptions.VTypeGeneric)
 * <p>This implementation supports <code>null</code> values.</p>
#end
 * 
 * @author This code is inspired by the collaboration and implementation in the <a
 *         href="http://fastutil.dsi.unimi.it/">fastutil</a> project.
 * 
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeVTypeOpenIdentityHashMap<KType, VType>
        implements KTypeVTypeMap<KType, VType>, Cloneable
{
    /**
     * Minimum capacity for the map.
     */
    public final static int MIN_CAPACITY = HashContainerUtils.MIN_CAPACITY;

    /**
     * Default capacity.
     */
    public final static int DEFAULT_CAPACITY = HashContainerUtils.DEFAULT_CAPACITY;

    /**
     * Default load factor.
     */
    public final static float DEFAULT_LOAD_FACTOR = HashContainerUtils.DEFAULT_LOAD_FACTOR;

    protected VType defaultValue = Intrinsics.<VType> defaultVTypeValue();

    /**
     * Hash-indexed array holding all keys.
    #if ($TemplateOptions.KTypeGeneric)
     * <p><strong>Important!</strong>
     * The actual value in this field is always an instance of <code>Object[]</code>.
     * Be warned that <code>javac</code> emits additional casts when <code>keys</code>
     * are directly accessed; <strong>these casts
     * may result in exceptions at runtime</strong>. A workaround is to cast directly to
     * <code>Object[]</code> before accessing the buffer's elements (although it is highly
     * recommended to use a {@link #iterator()} instead.
     * </pre>
    #end
    #if ($SA)
     * <p>
     * Direct map iteration: iterate  {keys[i], values[i]} for i in [0; keys.length[ where keys[i] != null, then also
     * {null, {@link #defaultKeyValue} } is in the map if {@link #allocatedDefaultKey} = true.
     * </p>
    #else
     * <p>
     * Direct map iteration: iterate  {keys[i], values[i]} for i in [0; keys.length[ where this.allocated[i] is true.
     * </p>
    #end
     * 
     * <p><b>Direct iteration warning: </b>
     * If the iteration goal is to fill another hash container, please iterate {@link #keys} in reverse to prevent performance losses.
     * @see #values
     */
    public KType[] keys;

    /**
     * Hash-indexed array holding all values associated to the keys
     * stored in {@link #keys}.
    #if ($TemplateOptions.KTypeGeneric)
     * <p><strong>Important!</strong>
     * The actual value in this field is always an instance of <code>Object[]</code>.
     * Be warned that <code>javac</code> emits additional casts when <code>values</code>
     * are directly accessed; <strong>these casts
     * may result in exceptions at runtime</strong>. A workaround is to cast directly to
     * <code>Object[]</code> before accessing the buffer's elements (although it is highly
     * recommended to use a {@link #iterator()} instead.
     * </pre>
    #end
     * 
     * @see #keys
     */
    public VType[] values;

    /*! #if (!$SA) !*/
    /**
     * Information if an entry (slot) in the {@link #keys} table is allocated
     * or empty.
     * @see #assigned
     */
    public boolean[] allocated;
    /*! #else
    //True if key = null is in the map.
    public boolean allocatedDefaultKey = false;
    //if allocatedDefaultKey = true, contains the associated V to the key = 0
    public VType defaultKeyValue;
    #end

    /**
     * Cached number of assigned slots in {@link #keys}.
     */
    protected int assigned;

    /**
     * The load factor for this map (fraction of allocated slots
     * before the buffers must be rehashed or reallocated).
     */
    protected final float loadFactor;

    /**
     * Resize buffers when {@link #keys} hits this value.
     */
    protected int resizeAt;

    /**
     * The most recent slot accessed in {@link #containsKey} (required for
     * {@link #lget}).
     * 
     * @see #containsKey
     * @see #lget
     */
    protected int lastSlot;

    /**
     * Creates a hash map with the default capacity of {@value #DEFAULT_CAPACITY},
     * load factor of {@value #DEFAULT_LOAD_FACTOR}.
     * 
     * <p>See class notes about hash distribution importance.</p>
     */
    public KTypeVTypeOpenIdentityHashMap()
    {
        this(KTypeVTypeOpenIdentityHashMap.DEFAULT_CAPACITY);
    }

    /**
     * Creates a hash map with the given initial capacity, default load factor of
     * {@value #DEFAULT_LOAD_FACTOR}.
     * 
     * @param initialCapacity Initial capacity (greater than zero and automatically
     *            rounded to the next power of two).
     */
    public KTypeVTypeOpenIdentityHashMap(final int initialCapacity)
    {
        this(initialCapacity, KTypeVTypeOpenIdentityHashMap.DEFAULT_LOAD_FACTOR);
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
    public KTypeVTypeOpenIdentityHashMap(final int initialCapacity, final float loadFactor)
    {
        assert loadFactor > 0 && loadFactor <= 1 : "Load factor must be between (0, 1].";

        this.loadFactor = loadFactor;

        //take into account of the load factor to garantee no reallocations before reaching  initialCapacity.
        int internalCapacity = (int) (initialCapacity / loadFactor) + KTypeVTypeOpenIdentityHashMap.MIN_CAPACITY;

        //align on next power of two
        internalCapacity = HashContainerUtils.roundCapacity(internalCapacity);

        this.keys = Intrinsics.newKTypeArray(internalCapacity);
        this.values = Intrinsics.newVTypeArray(internalCapacity);

        //fill with "not allocated" value
        /*! #if (!$SA) !*/
        this.allocated = new boolean[internalCapacity];
        /*! #end !*/

        //Take advantage of the rounding so that the resize occur a bit later than expected.
        //allocate so that there is at least one slot that remains allocated = false
        //this is compulsory to guarantee proper stop in searching loops
        this.resizeAt = Math.max(3, (int) (internalCapacity * loadFactor)) - 2;
    }

    /**
     * Create a hash map from all key-value pairs of another container.
     */
    public KTypeVTypeOpenIdentityHashMap(final KTypeVTypeAssociativeContainer<KType, VType> container)
    {
        this(container.size());
        putAll(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VType put(final KType key, final VType value)
    {
/*! #if ($SA)
        if (Intrinsics.equalsKTypeDefault(key)) {

            if (this.allocatedDefaultKey) {

                VType previousValue = this.defaultKeyValue;
                this.defaultKeyValue = value;

                return previousValue;
            }

            this.defaultKeyValue = value;

            this.allocatedDefaultKey = true;

            return this.defaultValue;
        }
#end !*/
        final int mask = this.keys.length - 1;

        int slot = Internals.rehash(System.identityHashCode(key)) & mask;

        final KType[] keys = this.keys;
        final VType[] values = this.values;

        /*! #if (!$SA) !*/
        final boolean[] allocated = this.allocated;
        /*! #end !*/

        while (is_allocated(allocated, slot, keys))
        {
            if (key == keys[slot])
            {
                final VType oldValue = values[slot];
                values[slot] = value;

                return oldValue;
            }

            slot = (slot + 1) & mask;
        }

        // Check if we need to grow. If so, reallocate new data, fill in the last element
        // and rehash.
        if (this.assigned == this.resizeAt)
        {
            expandAndPut(key, value, slot);
        }
        else
        {
            this.assigned++;
            /*! #if (!$SA) !*/
            allocated[slot] = true;
            /*! #end !*/

            keys[slot] = key;
            values[slot] = value;
        }
        return this.defaultValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int putAll(final KTypeVTypeAssociativeContainer<? extends KType, ? extends VType> container)
    {
        return putAll((Iterable<? extends KTypeVTypeCursor<? extends KType, ? extends VType>>) container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int putAll(final Iterable<? extends KTypeVTypeCursor<? extends KType, ? extends VType>> iterable)
    {
        final int count = this.size();

        for (final KTypeVTypeCursor<? extends KType, ? extends VType> c : iterable)
        {
            put(c.key, c.value);
        }
        return this.size() - count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean putIfAbsent(final KType key, final VType value)
    {
        if (!containsKey(key))
        {
            put(key, value);
            return true;
        }
        return false;
    }

    /*! #if ($TemplateOptions.VTypeNumeric) !*/
    /**
     * <a href="http://trove4j.sourceforge.net">Trove</a>-inspired API method. An equivalent
     * of the following code:
     * <pre>
     *  if (containsKey(key))
     *  {
     *      VType v = (VType) (lget() + additionValue);
     *      lset(v);
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
    /*! #if ($TemplateOptions.VTypeNumeric)
    @Override
    public VType putOrAdd(KType key, VType putValue, VType additionValue)
    {
     #if ($SA)

        if (Intrinsics.equalsKTypeDefault(key)) {

            if (this.allocatedDefaultKey) {

                this.defaultKeyValue += additionValue;

                return this.defaultKeyValue;
            }

            this.defaultKeyValue = putValue;

            this.allocatedDefaultKey = true;

            return putValue;
        }
    #end

        final int mask = this.keys.length - 1;

        int slot = Internals.rehash(System.identityHashCode(key)) & mask;

        #if (!$SA)
        final boolean[] allocated = this.allocated;
        #end

        final KType[] keys = this.keys;
        final VType[] values = this.values;

        while (is_allocated(allocated, slot, keys))
        {
            if (key == keys[slot])
            {
                values[slot] += additionValue;
                return values[slot];
            }

            slot = (slot + 1) & mask;
        }

        if (assigned == resizeAt) {
            expandAndPut(key, putValue, slot);
        } else {

            assigned++;

            #if (!$SA)
            allocated[slot] = true;
            #end

            keys[slot] = key;
            values[slot] = putValue;
        }
        return putValue;
    }
    #end !*/

    /*! #if ($TemplateOptions.VTypeNumeric) !*/
    /**
     * An equivalent of calling
     * <pre>
     *  if (containsKey(key))
     *  {
     *      VType v = (VType) (lget() + additionValue);
     *      lset(v);
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
    /*! #if ($TemplateOptions.VTypeNumeric)
    @Override
    public VType addTo(KType key, VType additionValue)
    {
        return putOrAdd(key, additionValue, additionValue);
    }
    #end !*/

    /**
     * Expand the internal storage buffers (capacity) and rehash.
     */
    private void expandAndPut(final KType pendingKey, final VType pendingValue, final int freeSlot)
    {
        assert this.assigned == this.resizeAt;

        /*! #if (!$SA) !*/
        assert !this.allocated[freeSlot];
        /*! #else
        //default sentinel value is never in the keys[] array, so never trigger reallocs
        assert !Intrinsics.equalsKTypeDefault(pendingKey);
        #end !*/

        // Try to allocate new buffers first. If we OOM, it'll be now without
        // leaving the data structure in an inconsistent state.
        final KType[] oldKeys = this.keys;
        final VType[] oldValues = this.values;

        /*! #if (!$SA) !*/
        final boolean[] oldAllocated = this.allocated;
        /*! #end !*/

        allocateBuffers(HashContainerUtils.nextCapacity(this.keys.length));

        // We have succeeded at allocating new data so insert the pending key/value at
        // the free slot in the old arrays before rehashing.
        this.lastSlot = -1;
        this.assigned++;

        /*! #if (!$SA) !*/
        oldAllocated[freeSlot] = true;
        /*! #end !*/

        oldKeys[freeSlot] = pendingKey;
        oldValues[freeSlot] = pendingValue;

        //for inserts
        final int mask = this.keys.length - 1;

        KType key = Intrinsics.<KType> defaultKTypeValue();
        VType value = Intrinsics.<VType> defaultVTypeValue();

        int slot = -1;

        final KType[] keys = this.keys;
        final VType[] values = this.values;

        /*! #if (!$SA) !*/
        final boolean[] allocated = this.allocated;
        /*! #end !*/

        //iterate all the old arrays to add in the newly allocated buffers
        //It is important to iterate backwards to minimize the conflict chain length !
        for (int i = oldKeys.length; --i >= 0;)
        {
            if (is_allocated(oldAllocated, i, oldKeys))
            {
                key = oldKeys[i];
                value = oldValues[i];

                slot = Internals.rehash(System.identityHashCode(key)) & mask;

                while (is_allocated(allocated, slot, keys))
                {
                    slot = (slot + 1) & mask;
                } //end while

                /*! #if (!$SA) !*/
                allocated[slot] = true;
                /*! #end !*/

                keys[slot] = key;
                values[slot] = value;
            }
        }
    }

    /**
     * Allocate internal buffers for a given capacity.
     * 
     * @param capacity New capacity (must be a power of two).
     */
    private void allocateBuffers(final int capacity)
    {
        final KType[] keys = Intrinsics.newKTypeArray(capacity);
        final VType[] values = Intrinsics.newVTypeArray(capacity);

        /*! #if (!$SA) !*/
        final boolean[] allocated = new boolean[capacity];
        /*! #end !*/

        this.keys = keys;
        this.values = values;

        /*! #if (!$SA) !*/
        this.allocated = allocated;
        /*! #end !*/

        //allocate so that there is at least one slot that remains allocated = false
        //this is compulsory to guarantee proper stop in searching loops
        this.resizeAt = Math.max(3, (int) (capacity * this.loadFactor)) - 2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VType remove(final KType key)
    {
/*! #if ($SA)
        if (Intrinsics.equalsKTypeDefault(key)) {

            if (this.allocatedDefaultKey) {

                VType previousValue = this.defaultKeyValue;

                this.allocatedDefaultKey = false;
                return previousValue;
            }

            return this.defaultValue;
        }
#end !*/

        final int mask = this.keys.length - 1;

        int slot = Internals.rehash(System.identityHashCode(key)) & mask;

        final KType[] keys = this.keys;

        /*! #if (!$SA) !*/
        final boolean[] states = this.allocated;
        /*! #end !*/

        while (is_allocated(states, slot, keys))
        {
            if (key == keys[slot])
            {
                final VType value = this.values[slot];

                this.assigned--;
                shiftConflictingKeys(slot);

                return value;
            }
            slot = (slot + 1) & mask;
        } //end while true

        return this.defaultValue;
    }

    /**
     * Shift all the slot-conflicting keys allocated to (and including) <code>slot</code>.
     */
    protected void shiftConflictingKeys(int slotCurr)
    {
        // Copied nearly verbatim from fastutil's impl.
        final int mask = this.keys.length - 1;

        int slotPrev, slotOther;

        final KType[] keys = this.keys;
        final VType[] values = this.values;

        /*! #if (!$SA) !*/
        final boolean[] allocated = this.allocated;
        /*! #end !*/

        while (true)
        {
            slotCurr = ((slotPrev = slotCurr) + 1) & mask;

            while (is_allocated(allocated, slotCurr, keys))
            {
                slotOther = (Internals.rehash(System.identityHashCode(keys[slotCurr])) & mask);

                if (slotPrev <= slotCurr)
                {
                    // we're on the right of the original slot.
                    if (slotPrev >= slotOther || slotOther > slotCurr)
                    {
                        break;
                    }
                }
                else
                {
                    // we've wrapped around.
                    if (slotPrev >= slotOther && slotOther > slotCurr)
                    {
                        break;
                    }
                }
                slotCurr = (slotCurr + 1) & mask;
            }

            if (!is_allocated(allocated, slotCurr, keys))
            {
                break;
            }

            // Shift key/value/allocated triplet.
            keys[slotPrev] = keys[slotCurr];
            values[slotPrev] = values[slotCurr];
        }

        //means not allocated
        /*! #if (!$SA) !*/
        allocated[slotPrev] = false;
        /*! #end !*/

        /* #if (($TemplateOptions.KTypeGeneric) || $SA ) */
        keys[slotPrev] = Intrinsics.<KType> defaultKTypeValue();
        /* #end */
        /* #if ($TemplateOptions.VTypeGeneric) */
        values[slotPrev] = Intrinsics.<VType> defaultVTypeValue();
        /* #end */
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeAll(final KTypeContainer<? extends KType> container)
    {
        final int before = this.size();

        for (final KTypeCursor<? extends KType> cursor : container)
        {
            remove(cursor.value);
        }

        return before - this.size();
    }

    /**
     * {@inheritDoc}
     * <p><strong>Important!</strong>
     * If the predicate actually injects the removed keys in another hash container, you may experience performance losses.
     */
    @Override
    public int removeAll(final KTypePredicate<? super KType> predicate)
    {
        final int before = this.size();

/*! #if ($SA)
        if (this.allocatedDefaultKey) {

            if (predicate.apply(Intrinsics.defaultKTypeValue()))
            {
                 this.allocatedDefaultKey = false;
            }
        }
#end !*/

        final KType[] keys = this.keys;

        /*! #if (!$SA) !*/
        final boolean[] states = this.allocated;
        /*! #end !*/

        for (int i = 0; i < keys.length;)
        {
            if (is_allocated(states, i, keys))
            {
                if (predicate.apply(keys[i]))
                {
                    this.assigned--;
                    shiftConflictingKeys(i);
                    // Repeat the check for the same i.
                    continue;
                }
            }
            i++;
        }
        return before - this.size();
    }

    /**
     * {@inheritDoc}
     * 
     * <p> Use the following snippet of code to check for key existence
     * first and then retrieve the value if it exists.</p>
     * <pre>
     * if (map.containsKey(key))
     *   value = map.lget();
     * </pre>
     */
    @Override
    public VType get(final KType key)
    {
/*! #if ($SA)
        if (Intrinsics.equalsKTypeDefault(key)) {

            if (this.allocatedDefaultKey) {

                return this.defaultKeyValue;
            }

            return this.defaultValue;
        }
#end !*/
        final int mask = this.keys.length - 1;

        int slot = Internals.rehash(System.identityHashCode(key)) & mask;

        final KType[] keys = this.keys;

        /*! #if (!$SA) !*/
        final boolean[] states = this.allocated;
        /*! #end !*/

        while (is_allocated(states, slot, keys))
        {
            if (key == keys[slot])
            {
                return this.values[slot];
            }
            slot = (slot + 1) & mask;
        } //end while true

        return this.defaultValue;
    }

    /**
     * Returns the last key stored in this has map for the corresponding
     * most recent call to {@link #containsKey}.
     * Precondition : {@link #containsKey} must have been called previously !
     * <p>Use the following snippet of code to check for key existence
     * first and then retrieve the key value if it exists.</p>
     * <pre>
     * if (map.containsKey(key))
     *   value = map.lkey();
     * </pre>
     * 
     * <p>This is equivalent to calling:</p>
     * <pre>
     * if (map.containsKey(key))
     *   key = map.keys[map.lslot()];
     * </pre>
     */
    public KType lkey()
    {
/*! #if ($SA)

        if (this.lastSlot == -2) {

            return Intrinsics.defaultKTypeValue();
        }

#end !*/
        assert this.lastSlot >= 0 : "Call containsKey() first.";

        /*! #if (!$SA) !*/
        assert this.allocated[this.lastSlot] : "Last call to exists did not have any associated value.";
        /*! #else
         assert ! Intrinsics.equalsKTypeDefault(this.keys[lastSlot]) : "Last call to exists did not have any associated value.";
         #end !*/

        return this.keys[this.lastSlot];
    }

    /**
     * Returns the last value saved in a call to {@link #containsKey}.
     * Precondition : {@link #containsKey} must have been called previously !
     * @see #containsKey
     */
    public VType lget()
    {
/*! #if ($SA)
        if (this.lastSlot == -2) {

            return this.defaultKeyValue;
        }
#end !*/
        assert this.lastSlot >= 0 : "Call containsKey() first.";

        /*! #if (!$SA) !*/
        assert this.allocated[this.lastSlot] : "Last call to exists did not have any associated value.";
        /*! #else
         assert ! Intrinsics.equalsKTypeDefault(this.keys[lastSlot]) : "Last call to exists did not have any associated value.";
         #end !*/

        return this.values[this.lastSlot];
    }

    /**
     * Sets the value corresponding to the key saved in the last
     * call to {@link #containsKey}, if and only if the key exists
     * in the map already.
     * Precondition : {@link #containsKey} must have been called previously !
     * @see #containsKey
     * @return Returns the previous value stored under the given key.
     */
    public VType lset(final VType value)
    {
/*! #if ($SA)
        if (this.lastSlot == -2) {

            VType previous = this.defaultKeyValue;
            this.defaultKeyValue = value;
            return previous;
        }
#end !*/
        assert this.lastSlot >= 0 : "Call containsKey() first.";

        /*! #if (!$SA) !*/
        assert this.allocated[this.lastSlot] : "Last call to exists did not have any associated value.";
        /*! #else
         assert ! Intrinsics.equalsKTypeDefault(this.keys[lastSlot]) : "Last call to exists did not have any associated value.";
         #end !*/

        final VType previous = this.values[this.lastSlot];
        this.values[this.lastSlot] = value;
        return previous;
    }

    /**
     * @return Returns the slot of the last key looked up in a call to {@link #containsKey} if
     * it returned <code>true</code>.
     * 
     * @see #containsKey
     */
    public int lslot()
    {
        assert this.lastSlot >= 0 || this.lastSlot == -2 : "Call containsKey() first.";
        return this.lastSlot;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Saves the associated value for fast access using {@link #lget}
     * or {@link #lset}.</p>
     * <pre>
     * if (map.containsKey(key))
     *   value = map.lget();
     * </pre>
     * or, to modify the value at the given key without looking up
     * its slot twice:
     * <pre>
     * if (map.containsKey(key))
     *   map.lset(map.lget() + 1);
     * </pre>
     * #if ($TemplateOptions.KTypeGeneric) or, to retrieve the key-equivalent object from the map:
     * <pre>
     * if (map.containsKey(key))
     *   map.lkey();
     * </pre>#end
     */
    @Override
    public boolean containsKey(final KType key)
    {
/*! #if ($SA)
        if (Intrinsics.equalsKTypeDefault(key)) {

            if (this.allocatedDefaultKey) {
                this.lastSlot = -2;
            } else {
                this.lastSlot = -1;
            }

            return this.allocatedDefaultKey;
        }
#end !*/
        final int mask = this.keys.length - 1;

        int slot = Internals.rehash(System.identityHashCode(key)) & mask;

        final KType[] keys = this.keys;

        /*! #if (!$SA) !*/
        final boolean[] states = this.allocated;
        /*! #end !*/

        while (is_allocated(states, slot, keys))
        {
            if (key == keys[slot])
            {
                this.lastSlot = slot;
                return true;
            }
            slot = (slot + 1) & mask;

        } //end while true

        //unsuccessful search
        this.lastSlot = -1;

        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Does not release internal buffers.</p>
     */
    @Override
    public void clear()
    {
        this.assigned = 0;
        this.lastSlot = -1;

        // States are always cleared.
        /*! #if (!$SA) !*/
        Internals.blankBooleanArray(this.allocated, 0, this.allocated.length);
        /*! #end !*/

        /*! #if (($TemplateOptions.KTypeGeneric) || $SA) !*/
        //Faster than Arrays.fill(keys, null); // Help the GC.
        KTypeArrays.blankArray(this.keys, 0, this.keys.length);
        /*! #end !*/

        /*! #if ($SA)
        this.allocatedDefaultKey = false;
        #end !*/

        /*! #if ($TemplateOptions.VTypeGeneric) !*/
        //Faster than Arrays.fill(values, null); // Help the GC.
        Internals.blankObjectArray(this.values, 0, this.values.length);
        /*! #end !*/
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size()
    {
        return this.assigned /*! #if ($SA) + (this.allocatedDefaultKey?1:0) #end !*/;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int capacity() {

        return this.resizeAt - 1;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Note that an empty container may still contain many deleted keys (that occupy buffer
     * space). Adding even a single element to such a container may cause rehashing.</p>
     */
    @Override
    public boolean isEmpty()
    {
        return size() == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        int h = 0;

        /*! #if ($SA)
        if (this.allocatedDefaultKey) {
            h +=  Internals.rehash(Intrinsics.defaultKTypeValue()) + Internals.rehash(this.defaultKeyValue);
        }
        #end !*/

        final KType[] keys = this.keys;
        final VType[] values = this.values;

        /*! #if (!$SA) !*/
        final boolean[] states = this.allocated;
        /*! #end !*/

        for (int i = keys.length; --i >= 0;)
        {
            if (is_allocated(states, i, keys))
            {
                //This hash is an intrinsic property of the container contents
                h += Internals.rehash(System.identityHashCode(keys[i])) + Internals.rehash(values[i]);
            }
        }

        return h;
    }

    /**
     * this instance and obj can only be equal if : <pre>
     * (both are KTypeVTypeOpenCustomHashMap)
     * and
     * (both have equal hash strategies defined by {@link #KTypeHashingStrategy}.equals(obj.hashStrategy))</pre>
     * then, both maps are compared using their {@link #KTypeHashingStrategy}.
     */
    @Override
    public boolean equals(final Object obj)
    {
        if (obj != null)
        {
            if (obj == this)
            {
                return true;
            }

            if (!(obj instanceof KTypeVTypeOpenIdentityHashMap))
            {
                return false;
            }

            @SuppressWarnings("unchecked")
            final KTypeVTypeOpenIdentityHashMap<KType, VType> other = (KTypeVTypeOpenIdentityHashMap<KType, VType>) obj;

            if (other.size() == this.size())
            {
                final EntryIterator it = this.iterator();

                while (it.hasNext())
                {
                    final KTypeVTypeCursor<KType, VType> c = it.next();

                    if (other.containsKey(c.key))
                    {
                        final VType v = other.get(c.key);
                        if (Intrinsics.equalsVType(c.value, v))
                        {
                            continue;
                        }
                    }
                    //recycle
                    it.release();
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * An iterator implementation for {@link #iterator}.
     */
    public final class EntryIterator extends AbstractIterator<KTypeVTypeCursor<KType, VType>>
    {
        public final KTypeVTypeCursor<KType, VType> cursor;

        public EntryIterator()
        {
            this.cursor = new KTypeVTypeCursor<KType, VType>();
            this.cursor.index = -2;
        }

        /**
         * Iterate backwards w.r.t the buffer, to
         * minimize collision chains when filling another hash container (ex. with putAll())
         */
        @Override
        protected KTypeVTypeCursor<KType, VType> fetch()
        {
            /*! #if ($SA)
            if (this.cursor.index == KTypeVTypeOpenIdentityHashMap.this.keys.length + 1) {

                if (KTypeVTypeOpenIdentityHashMap.this.allocatedDefaultKey) {

                    this.cursor.index = KTypeVTypeOpenIdentityHashMap.this.keys.length;
                    this.cursor.key = Intrinsics.defaultKTypeValue();
                    this.cursor.value = KTypeVTypeOpenIdentityHashMap.this.defaultKeyValue;

                    return this.cursor;

                } else {
                    //no value associated with the default key, continue iteration...
                     this.cursor.index = KTypeVTypeOpenIdentityHashMap.this.keys.length;
                }
            }

            #end !*/

            int i = this.cursor.index - 1;

            while (i >= 0 && !is_allocated(KTypeVTypeOpenIdentityHashMap.this.allocated, i, KTypeVTypeOpenIdentityHashMap.this.keys))
            {
                i--;
            }

            if (i == -1)
            {
                return done();
            }

            this.cursor.index = i;
            this.cursor.key = KTypeVTypeOpenIdentityHashMap.this.keys[i];
            this.cursor.value = KTypeVTypeOpenIdentityHashMap.this.values[i];

            return this.cursor;
        }
    }

    /**
     * internal pool of EntryIterator
     */
    protected final IteratorPool<KTypeVTypeCursor<KType, VType>, EntryIterator> entryIteratorPool = new IteratorPool<KTypeVTypeCursor<KType, VType>, EntryIterator>(
            new ObjectFactory<EntryIterator>() {

                @Override
                public EntryIterator create()
                {
                    return new EntryIterator();
                }

                @Override
                public void initialize(final EntryIterator obj)
                {
                    obj.cursor.index = KTypeVTypeOpenIdentityHashMap.this.keys.length /*! #if ($SA) +1 #end !*/;
                }

                @Override
                public void reset(final EntryIterator obj) {
                    // nothing
                }
            });

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public EntryIterator iterator()
    {
        //return new EntryIterator();
        return this.entryIteratorPool.borrow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends KTypeVTypeProcedure<? super KType, ? super VType>> T forEach(final T procedure)
    {
        /*! #if ($SA)
        if (this.allocatedDefaultKey) {

            procedure.apply(Intrinsics.defaultKTypeValue(), this.defaultKeyValue);
        }
        #end !*/

        final KType[] keys = this.keys;
        final VType[] values = this.values;

        /*! #if (!$SA) !*/
        final boolean[] states = this.allocated;
        /*! #end !*/

        //Iterate in reverse for side-stepping the longest conflict chain
        //in another hash, in case apply() is actually used to fill another hash container.
        for (int i = keys.length - 1; i >= 0; i--)
        {
            if (is_allocated(states, i, keys))
            {
                procedure.apply(keys[i], values[i]);
            }
        }

        return procedure;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends KTypeVTypePredicate<? super KType, ? super VType>> T forEach(final T predicate)
    {
        /*! #if ($SA)
        if (this.allocatedDefaultKey) {

            if(! predicate.apply(Intrinsics.defaultKTypeValue(), this.defaultKeyValue)) {

                return predicate;
            }
        }
        #end !*/

        final KType[] keys = this.keys;
        final VType[] values = this.values;

        /*! #if (!$SA) !*/
        final boolean[] states = this.allocated;
        /*! #end !*/

        //Iterate in reverse for side-stepping the longest conflict chain
        //in another hash, in case apply() is actually used to fill another hash container.
        for (int i = keys.length - 1; i >= 0; i--)
        {
            if (is_allocated(states, i, keys))
            {
                if (!predicate.apply(keys[i], values[i]))
                {
                    break;
                }
            }
        } //end for

        return predicate;
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
    public final class KeysContainer
            extends AbstractKTypeCollection<KType> implements KTypeLookupContainer<KType>
    {
        private final KTypeVTypeOpenIdentityHashMap<KType, VType> owner =
                KTypeVTypeOpenIdentityHashMap.this;

        @Override
        public boolean contains(final KType e)
        {
            return containsKey(e);
        }

        @Override
        public <T extends KTypeProcedure<? super KType>> T forEach(final T procedure)
        {
            /*! #if ($SA)
            if (this.owner.allocatedDefaultKey) {

                procedure.apply(Intrinsics.defaultKTypeValue());
            }
            #end !*/

            final KType[] keys = this.owner.keys;

            /*! #if (!$SA) !*/
            final boolean[] states = this.owner.allocated;
            /*! #end !*/

            //Iterate in reverse for side-stepping the longest conflict chain
            //in another hash, in case apply() is actually used to fill another hash container.
            for (int i = keys.length - 1; i >= 0; i--)
            {
                if (is_allocated(states, i, keys))
                {
                    procedure.apply(keys[i]);
                }
            }

            return procedure;
        }

        @Override
        public <T extends KTypePredicate<? super KType>> T forEach(final T predicate)
        {
            /*! #if ($SA)
            if (this.owner.allocatedDefaultKey) {

                if(! predicate.apply(Intrinsics.defaultKTypeValue())) {

                    return predicate;
                }
            }
            #end !*/

            final KType[] keys = this.owner.keys;

            /*! #if (!$SA) !*/
            final boolean[] states = this.owner.allocated;
            /*! #end !*/

            //Iterate in reverse for side-stepping the longest conflict chain
            //in another hash, in case apply() is actually used to fill another hash container.
            for (int i = keys.length - 1; i >= 0; i--)
            {
                if (is_allocated(states, i, keys))
                {
                    if (!predicate.apply(keys[i]))
                    {
                        break;
                    }
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
        public int capacity() {

            return this.owner.capacity();
        }

        @Override
        public void clear()
        {
            this.owner.clear();
        }

        @Override
        public int removeAll(final KTypePredicate<? super KType> predicate)
        {
            return this.owner.removeAll(predicate);
        }

        @Override
        public int removeAllOccurrences(final KType e)
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

        /**
         * internal pool of KeysIterator
         */
        protected final IteratorPool<KTypeCursor<KType>, KeysIterator> keyIteratorPool = new IteratorPool<KTypeCursor<KType>, KeysIterator>(
                new ObjectFactory<KeysIterator>() {

                    @Override
                    public KeysIterator create()
                    {
                        return new KeysIterator();
                    }

                    @Override
                    public void initialize(final KeysIterator obj)
                    {
                        obj.cursor.index = KTypeVTypeOpenIdentityHashMap.this.keys.length /*! #if ($SA) +1 #end !*/;
                    }

                    @Override
                    public void reset(final KeysIterator obj) {
                        // nothing
                    }
                });

        @Override
        public KType[] toArray(final KType[] target)
        {
            int count = 0;

            /*! #if ($SA)
                    if (this.owner.allocatedDefaultKey) {

                        target[count++] = Intrinsics.defaultKTypeValue();
                    }
            #end !*/

            final KType[] keys = this.owner.keys;

            /*! #if (!$SA) !*/
            final boolean[] states = this.owner.allocated;
            /*! #end !*/

            for (int i = 0; i < keys.length; i++)
            {
                if (is_allocated(states, i, keys))
                {
                    target[count++] = keys[i];
                }
            }

            assert count == this.owner.size();
            return target;
        }
    };

    /**
     * An iterator over the set of assigned keys.
     */
    public final class KeysIterator extends AbstractIterator<KTypeCursor<KType>>
    {
        public final KTypeCursor<KType> cursor;

        public KeysIterator()
        {
            this.cursor = new KTypeCursor<KType>();
            this.cursor.index = -2;
        }

        /**
         * Iterate backwards w.r.t the buffer, to
         * minimize collision chains when filling another hash container (ex. with putAll())
         */
        @Override
        protected KTypeCursor<KType> fetch()
        {
            /*! #if ($SA)
            if (this.cursor.index == KTypeVTypeOpenIdentityHashMap.this.keys.length + 1) {

                if (KTypeVTypeOpenIdentityHashMap.this.allocatedDefaultKey) {

                    this.cursor.index = KTypeVTypeOpenIdentityHashMap.this.keys.length;
                    this.cursor.value = Intrinsics.defaultKTypeValue();

                    return this.cursor;

                } else {
                    //no value associated with the default key, continue iteration...
                    this.cursor.index = KTypeVTypeOpenIdentityHashMap.this.keys.length;
                }

            }

            #end !*/

            int i = this.cursor.index - 1;

            while (i >= 0 && !is_allocated(KTypeVTypeOpenIdentityHashMap.this.allocated, i, KTypeVTypeOpenIdentityHashMap.this.keys))
            {
                i--;
            }

            if (i == -1)
            {
                return done();
            }

            this.cursor.index = i;
            this.cursor.value = KTypeVTypeOpenIdentityHashMap.this.keys[i];

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
    public final class ValuesContainer extends AbstractKTypeCollection<VType>
    {
        private final KTypeVTypeOpenIdentityHashMap<KType, VType> owner =
                KTypeVTypeOpenIdentityHashMap.this;

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
        public int capacity() {

            return this.owner.capacity();
        }

        @Override
        public boolean contains(final VType value)
        {
            /*! #if ($SA)
            if (this.owner.allocatedDefaultKey && Intrinsics.equalsVType(value, this.owner.defaultKeyValue)) {

                return true;
            }
            #end !*/

            // This is a linear scan over the values, but it's in the contract, so be it.

            /*! #if (!$SA) !*/
            final boolean[] states = this.owner.allocated;
            /*! #end !*/

            final KType[] keys = this.owner.keys;
            final VType[] values = this.owner.values;

            for (int slot = 0; slot < keys.length; slot++)
            {
                if (is_allocated(states, slot, keys)
                        && Intrinsics.equalsVType(value, values[slot]))
                {
                    return true;
                }
            }
            return false;
        }

        @Override
        public <T extends KTypeProcedure<? super VType>> T forEach(final T procedure)
        {

            /*! #if ($SA)
            if (this.owner.allocatedDefaultKey) {

                procedure.apply(this.owner.defaultKeyValue);
            }
            #end !*/

            /*! #if (!$SA) !*/
            final boolean[] states = this.owner.allocated;
            /*! #end !*/

            final KType[] keys = this.owner.keys;
            final VType[] values = this.owner.values;

            for (int slot = 0; slot < keys.length; slot++)
            {
                if (is_allocated(states, slot, keys)) {
                    procedure.apply(values[slot]);
                }
            }

            return procedure;
        }

        @Override
        public <T extends KTypePredicate<? super VType>> T forEach(final T predicate)
        {
            /*! #if ($SA)
            if (this.owner.allocatedDefaultKey) {

               if (!predicate.apply(this.owner.defaultKeyValue))
               {
                   return predicate;
               }
            }
            #end !*/

            /*! #if (!$SA) !*/
            final boolean[] states = this.owner.allocated;
            /*! #end !*/

            final KType[] keys = this.owner.keys;
            final VType[] values = this.owner.values;

            for (int slot = 0; slot < keys.length; slot++)
            {
                if (is_allocated(states, slot, keys))
                {
                    if (!predicate.apply(values[slot]))
                    {
                        break;
                    }
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
        public int removeAllOccurrences(final VType e)
        {
            final int before = this.owner.size();

            /*! #if ($SA)
            if (this.owner.allocatedDefaultKey) {

                if(Intrinsics.equalsVType(e, this.owner.defaultKeyValue)) {

                    this.owner.allocatedDefaultKey = false;
                }
            }
            #end !*/

            final KType[] keys = this.owner.keys;
            final VType[] values = this.owner.values;

            /*! #if (!$SA) !*/
            final boolean[] states = this.owner.allocated;
            /*! #end !*/

            for (int slot = 0; slot < keys.length;)
            {
                if (is_allocated(states, slot, keys))
                {
                    if (Intrinsics.equalsVType(e, values[slot]))
                    {
                        this.owner.assigned--;
                        shiftConflictingKeys(slot);
                        // Repeat the check for the same i.
                        continue;
                    }
                }
                slot++;
            }
            return before - this.owner.size();
        }

        /**
         * {@inheritDoc}
         * Indeed removes all the (key,value) pairs matching
         * the predicate for the values, from  the map.
         */
        @Override
        public int removeAll(final KTypePredicate<? super VType> predicate)
        {
            final int before = this.owner.size();

            /*! #if ($SA)
            if (this.owner.allocatedDefaultKey) {

                if(predicate.apply(this.owner.defaultKeyValue)) {

                    this.owner.allocatedDefaultKey = false;
                }
            }
            #end !*/

            final KType[] keys = this.owner.keys;
            final VType[] values = this.owner.values;

            /*! #if (!$SA) !*/
            final boolean[] states = this.owner.allocated;
            /*! #end !*/

            for (int slot = 0; slot < keys.length;)
            {
                if (is_allocated(states, slot, keys))
                {
                    if (predicate.apply(values[slot]))
                    {
                        this.owner.assigned--;
                        shiftConflictingKeys(slot);
                        // Repeat the check for the same i.
                        continue;
                    }
                }
                slot++;
            }
            return before - this.owner.size();
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

        /**
         * internal pool of ValuesIterator
         */
        protected final IteratorPool<KTypeCursor<VType>, ValuesIterator> valuesIteratorPool = new IteratorPool<KTypeCursor<VType>, ValuesIterator>(
                new ObjectFactory<ValuesIterator>() {

                    @Override
                    public ValuesIterator create()
                    {

                        return new ValuesIterator();
                    }

                    @Override
                    public void initialize(final ValuesIterator obj)
                    {
                        obj.cursor.index = KTypeVTypeOpenIdentityHashMap.this.keys.length /*! #if ($SA) +1 #end !*/;
                    }

                    @Override
                    public void reset(final ValuesIterator obj) {
                        // nothing

                    }
                });

        @Override
        public VType[] toArray(final VType[] target)
        {
            int count = 0;

            /*! #if ($SA)
            if (this.owner.allocatedDefaultKey) {

                target[count++] = this.owner.defaultKeyValue;
            }
            #end !*/

            /*! #if (!$SA) !*/
            final boolean[] states = this.owner.allocated;
            /*! #end !*/

            final KType[] keys = this.owner.keys;
            final VType[] values = this.owner.values;

            for (int i = 0; i < values.length; i++)
            {
                if (is_allocated(states, i, keys))
                {
                    target[count++] = values[i];
                }
            }

            assert count == this.owner.size();
            return target;
        }
    }

    /**
     * An iterator over the set of values.
     */
    public final class ValuesIterator extends AbstractIterator<KTypeCursor<VType>>
    {
        public final KTypeCursor<VType> cursor;

        public ValuesIterator()
        {
            this.cursor = new KTypeCursor<VType>();
            this.cursor.index = -2;
        }

        /**
         * Iterate backwards w.r.t the buffer, to
         * minimize collision chains when filling another hash container (ex. with putAll())
         */
        @Override
        protected KTypeCursor<VType> fetch()
        {
            /*! #if ($SA)
            if (this.cursor.index == KTypeVTypeOpenIdentityHashMap.this.values.length + 1) {

                if (KTypeVTypeOpenIdentityHashMap.this.allocatedDefaultKey) {

                    this.cursor.index = KTypeVTypeOpenIdentityHashMap.this.values.length;
                    this.cursor.value = KTypeVTypeOpenIdentityHashMap.this.defaultKeyValue;

                    return this.cursor;

                } else {
                    //no value associated with the default key, continue iteration...
                    this.cursor.index = KTypeVTypeOpenIdentityHashMap.this.keys.length;
                }
            }

            #end !*/

            int i = this.cursor.index - 1;

            while (i >= 0 && !is_allocated(KTypeVTypeOpenIdentityHashMap.this.allocated, i, KTypeVTypeOpenIdentityHashMap.this.keys))
            {
                i--;
            }

            if (i == -1)
            {
                return done();
            }

            this.cursor.index = i;
            this.cursor.value = KTypeVTypeOpenIdentityHashMap.this.values[i];

            return this.cursor;
        }
    }

    /**
     * Clone this object.
     * #if ($TemplateOptions.AnyGeneric)
     * It also realizes a trim-to- this.size() in the process.
     * #end
     */
    @Override
    public KTypeVTypeOpenIdentityHashMap<KType, VType> clone()
    {
        /* #if ($TemplateOptions.AnyGeneric) */
        @SuppressWarnings("unchecked")
        final/* #end */
        KTypeVTypeOpenIdentityHashMap<KType, VType> cloned =
                new KTypeVTypeOpenIdentityHashMap<KType, VType>(this.size(), this.loadFactor);

        cloned.putAll(this);

        cloned.defaultValue = this.defaultValue;

        return cloned;

    }

    /**
     * Convert the contents of this map to a human-friendly string.
     */
    @Override
    public String toString()
    {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("[");

        boolean first = true;
        for (final KTypeVTypeCursor<KType, VType> cursor : this)
        {
            if (!first)
            {
                buffer.append(", ");
            }
            buffer.append(cursor.key);
            buffer.append("=>");
            buffer.append(cursor.value);
            first = false;
        }
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * Creates a hash map from two index-aligned arrays of key-value pairs.
     */
    public static <KType, VType> KTypeVTypeOpenIdentityHashMap<KType, VType> from(final KType[] keys, final VType[] values)
    {
        if (keys.length != values.length)
        {
            throw new IllegalArgumentException("Arrays of keys and values must have an identical length.");
        }

        final KTypeVTypeOpenIdentityHashMap<KType, VType> map = new KTypeVTypeOpenIdentityHashMap<KType, VType>(keys.length);

        for (int i = 0; i < keys.length; i++)
        {
            map.put(keys[i], values[i]);
        }
        return map;
    }

    /**
     * Create a hash map from another associative container.
     */
    public static <KType, VType> KTypeVTypeOpenIdentityHashMap<KType, VType> from(final KTypeVTypeAssociativeContainer<KType, VType> container)
    {
        return new KTypeVTypeOpenIdentityHashMap<KType, VType>(container);
    }

    /**
     * Create a new hash map without providing the full generic signature (constructor
     * shortcut).
     */
    public static <KType, VType> KTypeVTypeOpenIdentityHashMap<KType, VType> newInstance()
    {
        return new KTypeVTypeOpenIdentityHashMap<KType, VType>();
    }

    /**
     * Create a new hash map with initial capacity and load factor control. (constructor
     * shortcut).
     */
    public static <KType, VType> KTypeVTypeOpenIdentityHashMap<KType, VType> newInstance(final int initialCapacity, final float loadFactor)
    {
        return new KTypeVTypeOpenIdentityHashMap<KType, VType>(initialCapacity, loadFactor);
    }

    /**
     * Returns the "default value" value used
     * in containers methods returning "default value"
     * @return
     */
    public VType getDefaultValue()
    {
        return this.defaultValue;
    }

    /**
     * Set the "default value" value to be used
     * in containers methods returning "default value"
     * @return
     */
    public void setDefaultValue(final VType defaultValue)
    {
        this.defaultValue = defaultValue;
    }

/*! #if (!$SA) !*/
    //Test for existence in template

    /*! #if ($TemplateOptions.inline("is_allocated",
    "(alloc, slot, keys)",
    "alloc[slot]")) !*/
    /**
     *  template version
     * (actual method is inlined in generated code)
     */
    private boolean is_allocated(final boolean[] alloc, final int slot, final KType[] keys) {

        return alloc[slot];
    }
    /*! #end !*/

/*! #else
  //Test for existence with default value sentinels

     #if ($TemplateOptions.inline("is_allocated",
    "(alloc, slot, keys)",
    "! Intrinsics.equalsKTypeDefault(keys[slot])"))
    //nothing !
    #end
#end !*/
}
