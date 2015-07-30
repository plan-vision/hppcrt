package com.carrotsearch.hppcrt.maps;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.cursors.*;
import com.carrotsearch.hppcrt.predicates.*;
import com.carrotsearch.hppcrt.procedures.*;
import com.carrotsearch.hppcrt.hash.*;

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
/*! #set( $ROBIN_HOOD_FOR_GENERICS = true) !*/
/*! #set( $DEBUG = false) !*/
// If RH is defined, RobinHood Hashing is in effect :
/*! #set( $RH = ($TemplateOptions.KTypeGeneric && $ROBIN_HOOD_FOR_GENERICS) ) !*/

/**
 * A hash map of <code>KType</code> to <code>VType</code>, implemented using open
 * addressing with linear probing for collision resolution.
 * 
 * <p>
 * The internal buffers of this implementation ({@link #keys}, {@link #values}),
 * are always allocated to the nearest size that is a power of two. When
 * the capacity exceeds the given load factor, the buffer size is doubled.
 * </p>
 *
 * <p><b>Important note.</b> The implementation uses power-of-two tables and linear
 * probing, which may cause poor performance (many collisions) if hash values are
 * not properly distributed.
 * 
 * 
#if ($TemplateOptions.KTypeGeneric)
 * <p>This implementation supports <code>null</code> keys. </p>
#end
#if ($TemplateOptions.VTypeGeneric)
 * <p>This implementation supports <code>null</code> values.</p>
#end
 *
 * 
#if ($RH)
 *   <p> Robin-Hood hashing algorithm is also used to minimize variance
 *  in insertion and search-related operations, for an all-around smother operation at the cost
 *  of smaller peak performance:</p>
 *  <p> - Pedro Celis (1986) for the original Robin-Hood hashing paper, </p>
 *  <p> - <a href="cliff@leaninto.it">MoonPolySoft/Cliff Moon</a> for the initial Robin-hood on HPPC implementation,</p>
 *  <p> - <a href="vsonnier@gmail.com" >Vincent Sonnier</a> for the present implementation using cached hashes.</p>
#end
 *
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeVTypeHashMap<KType, VType>
implements KTypeVTypeMap<KType, VType>, Cloneable
{
    protected VType defaultValue = Intrinsics.<VType> empty();

    /**
     * Hash-indexed array holding all keys.
     * <p>
     * Direct map iteration: iterate  {keys[i], values[i]} for i in [0; keys.length[ where keys[i] != 0/null, then also
     * {0/null, {@link #allocatedDefaultKeyValue} } is in the map if {@link #allocatedDefaultKey} = true.
     * </p>
     */
    public/*! #if ($TemplateOptions.KTypePrimitive)
          KType []
          #else !*/
    Object[]
            /*! #end !*/
            keys;

    /**
     * Hash-indexed array holding all values associated to the keys.
     * stored in {@link #keys}.
     */
    public/*! #if ($TemplateOptions.VTypePrimitive)
          VType []
          #else !*/
    Object[]
            /*! #end !*/
            values;

    /*! #if ($RH) !*/
    /**
     * #if ($RH)
     * Caches the hash value = hash(keys[i]) & mask, if keys[i] != 0/null,
     * for every index i.
     * #end
     * @see #assigned
     */
    /*! #end !*/
    /*! #if ($RH) !*/
    protected int[] hash_cache;
    /*! #end !*/

    /**
     * True if key = 0/null is in the map.
     */
    public boolean allocatedDefaultKey = false;

    /**
     * if allocatedDefaultKey = true, contains the associated V to the key = 0/null
     */
    public VType allocatedDefaultKeyValue;

    /**
     * Cached number of assigned slots in {@link #keys}.
     */
    protected int assigned;

    /**
     * The load factor for this map (fraction of allocated slots
     * before the buffers must be rehashed or reallocated).
     */
    protected final double loadFactor;

    /**
     * Resize buffers when {@link #keys} hits this value.
     */
    private int resizeAt;

    /**
     * Per-instance, per-allocation size perturbation
     * introduced in rehashing to create a unique key distribution.
     */
    private final int perturbation = HashContainers.computeUniqueIdentifier(this);

    /**
     * Default constructor: Creates a hash map with the default capacity of {@link Containers#DEFAULT_EXPECTED_ELEMENTS},
     * load factor of {@link HashContainers#DEFAULT_LOAD_FACTOR}.
     * 
     * <p>See class notes about hash distribution importance.</p>
     */
    public KTypeVTypeHashMap() {
        this(Containers.DEFAULT_EXPECTED_ELEMENTS);
    }

    /**
     * Creates a hash map with the given initial capacity, default load factor of
     * {@link HashContainers#DEFAULT_LOAD_FACTOR}.
     * 
     * <p>See class notes about hash distribution importance.</p>
     * 
     * @param initialCapacity Initial capacity (greater than zero and automatically
     *            rounded to the next power of two).
     */
    public KTypeVTypeHashMap(final int initialCapacity) {
        this(initialCapacity, HashContainers.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a hash map with the given initial capacity,
     * load factor.
     *
     * @param loadFactor The load factor (greater than zero and smaller than 1).
     */
    public KTypeVTypeHashMap(final int initialCapacity, final double loadFactor) {
        this.loadFactor = loadFactor;
        //take into account of the load factor to guarantee no reallocations before reaching  initialCapacity.
        allocateBuffers(HashContainers.minBufferSize(initialCapacity, loadFactor));
    }

    /**
     * Create a hash map from all key-value pairs of another container.
     */
    public KTypeVTypeHashMap(final KTypeVTypeAssociativeContainer<KType, VType> container) {
        this(container.size());
        putAll(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VType put(KType key, VType value) {

        if (Intrinsics.<KType> isEmpty(key)) {

            if (this.allocatedDefaultKey) {

                final VType previousValue = this.allocatedDefaultKeyValue;
                this.allocatedDefaultKeyValue = value;

                return previousValue;
            }

            this.allocatedDefaultKeyValue = value;
            this.allocatedDefaultKey = true;

            return this.defaultValue;
        }

        final int mask = this.keys.length - 1;

        final KType[] keys = Intrinsics.<KType[]> cast(this.keys);

        int slot = REHASH(key) & mask;
        KType existing;

        /*! #if ($RH) !*/
        final VType[] values = Intrinsics.<VType[]> cast(this.values);

        final int[] cached = this.hash_cache;

        KType tmpKey;
        VType tmpValue;
        int tmpAllocated;
        int initial_slot = slot;
        int dist = 0;
        int existing_distance = 0;
        /*! #end !*/

        while (!Intrinsics.<KType> isEmpty(existing = keys[slot])) {

            if (Intrinsics.<KType> equalsNotNull(key, existing)) {
                final VType oldValue = Intrinsics.<VType> cast(this.values[slot]);
                values[slot] = value;

                return oldValue;
            }

            /*! #if ($RH) !*/
            //re-shuffle keys to minimize variance
            existing_distance = probe_distance(slot, cached);

            if (dist > existing_distance) {
                //swap current (key, value, initial_slot) with slot places
                tmpKey = keys[slot];
                keys[slot] = key;
                key = tmpKey;

                tmpAllocated = cached[slot];
                cached[slot] = initial_slot;
                initial_slot = tmpAllocated;

                tmpValue = values[slot];
                values[slot] = value;
                value = tmpValue;

                /*! #if($DEBUG) !*/
                //Check invariants
                assert cached[slot] == (REHASH(keys[slot]) & mask);
                assert initial_slot == (REHASH(key) & mask);
                /*! #end !*/

                dist = existing_distance;
            }
            /*! #end !*/

            slot = (slot + 1) & mask;

            /*! #if ($RH) !*/
            dist++;
            /*! #end !*/
        } //end while

        // Check if we need to grow. If so, reallocate new data, fill in the last element
        // and rehash.
        if (this.assigned == this.resizeAt) {
            expandAndPut(key, value, slot);
        } else {
            this.assigned++;
            /*! #if ($RH) !*/
            cached[slot] = initial_slot;
            /*! #end !*/

            keys[slot] = key;
            values[slot] = value;

            /*! #if ($RH) !*/
            /*! #if($DEBUG) !*/
            //Check invariants
            assert cached[slot] == (REHASH(keys[slot]) & mask);

            /*! #end !*/
            /*! #end !*/
        }

        return this.defaultValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int putAll(final KTypeVTypeAssociativeContainer<? extends KType, ? extends VType> container) {
        return putAll((Iterable<? extends KTypeVTypeCursor<? extends KType, ? extends VType>>) container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int putAll(final Iterable<? extends KTypeVTypeCursor<? extends KType, ? extends VType>> iterable) {
        final int count = this.size();
        for (final KTypeVTypeCursor<? extends KType, ? extends VType> c : iterable) {
            put(c.key, c.value);
        }
        return this.size() - count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean putIfAbsent(final KType key, final VType value) {
        if (!containsKey(key)) {
            put(key, value);
            return true;
        }
        return false;
    }

    /*! #if ($TemplateOptions.VTypePrimitive) !*/
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
    @SuppressWarnings("cast")
    @Override
    public VType putOrAdd(final KType key, VType putValue, final VType incrementValue) {

        if (containsKey(key)) {
            putValue = get(key);

            putValue = (VType) (Intrinsics.<VType> add(putValue, incrementValue));
        }

        put(key, putValue);
        return putValue;
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.VTypePrimitive) !*/
    /**
     * Adds <code>incrementValue</code> to any existing value for the given <code>key</code>
     * or inserts <code>incrementValue</code> if <code>key</code> did not previously exist.
     * 
     * @param key The key of the value to adjust.
     * @param incrementValue The value to put or add to the existing value if <code>key</code> exists.
     * @return Returns the current value associated with <code>key</code> (after changes).
     */
    @Override
    public VType addTo(final KType key, final VType incrementValue)
    {
        return putOrAdd(key, incrementValue, incrementValue);
    }

    /*! #end !*/

    /**
     * Expand the internal storage buffers (capacity) and rehash.
     */
    private void expandAndPut(final KType pendingKey, final VType pendingValue, final int freeSlot) {
        assert this.assigned == this.resizeAt;

        //default sentinel value is never in the keys[] array, so never trigger reallocs
        assert !Intrinsics.<KType> isEmpty(pendingKey);

        // Try to allocate new buffers first. If we OOM, it'll be now without
        // leaving the data structure in an inconsistent state.
        final KType[] oldKeys = Intrinsics.<KType[]> cast(this.keys);
        final VType[] oldValues = Intrinsics.<VType[]> cast(this.values);

        allocateBuffers(HashContainers.nextBufferSize(this.keys.length, this.assigned, this.loadFactor));

        // We have succeeded at allocating new data so insert the pending key/value at
        // the free slot in the old arrays before rehashing.
        this.assigned++;

        oldKeys[freeSlot] = pendingKey;
        oldValues[freeSlot] = pendingValue;

        //for inserts
        final int mask = this.keys.length - 1;

        KType key = Intrinsics.<KType> empty();
        VType value = Intrinsics.<VType> empty();

        int slot = -1;

        final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
        final VType[] values = Intrinsics.<VType[]> cast(this.values);

        /*! #if ($RH) !*/
        final int[] cached = this.hash_cache;
        /*! #end !*/

        /*! #if ($RH) !*/
        KType tmpKey = Intrinsics.<KType> empty();
        VType tmpValue = Intrinsics.<VType> empty();
        int tmpAllocated = -1;
        int initial_slot = -1;
        int dist = -1;
        int existing_distance = -1;
        /*! #end !*/

        //iterate all the old arrays to add in the newly allocated buffers
        //It is important to iterate backwards to minimize the conflict chain length !
        final int perturb = this.perturbation;

        for (int i = oldKeys.length; --i >= 0;) {

            if (!Intrinsics.<KType> isEmpty(key = oldKeys[i])) {

                value = oldValues[i];

                slot = REHASH2(key, perturb) & mask;

                /*! #if ($RH) !*/
                initial_slot = slot;
                dist = 0;
                /*! #end !*/

                while (is_allocated(slot, keys)) {
                    /*! #if ($RH) !*/
                    //re-shuffle keys to minimize variance
                    existing_distance = probe_distance(slot, cached);

                    if (dist > existing_distance) {
                        //swap current (key, value, initial_slot) with slot places
                        tmpKey = keys[slot];
                        keys[slot] = key;
                        key = tmpKey;

                        tmpAllocated = cached[slot];
                        cached[slot] = initial_slot;
                        initial_slot = tmpAllocated;

                        tmpValue = values[slot];
                        values[slot] = value;
                        value = tmpValue;

                        /*! #if($DEBUG) !*/
                        //Check invariants
                        assert cached[slot] == (REHASH(keys[slot]) & mask);
                        assert initial_slot == (REHASH(key) & mask);
                        /*! #end !*/

                        dist = existing_distance;
                    }
                    /*! #end !*/

                    slot = (slot + 1) & mask;

                    /*! #if ($RH) !*/
                    dist++;
                    /*! #end !*/
                } //end while

                /*! #if ($RH) !*/
                cached[slot] = initial_slot;
                /*! #end !*/

                keys[slot] = key;
                values[slot] = value;

                /*! #if ($RH) !*/
                /*! #if($DEBUG) !*/
                //Check invariants
                assert cached[slot] == (REHASH(keys[slot]) & mask);
                /*! #end !*/
                /*! #end !*/
            }
        }
    }

    /**
     * Allocate internal buffers for a given capacity.
     * 
     * @param capacity New capacity (must be a power of two).
     */
    @SuppressWarnings("boxing")
    private void allocateBuffers(final int capacity) {
        try {

            final KType[] keys = Intrinsics.<KType> newArray(capacity);
            final VType[] values = Intrinsics.<VType> newArray(capacity);

            /*! #if ($RH) !*/
            final int[] cached = new int[capacity];
            /*!  #end !*/

            this.keys = keys;
            this.values = values;

            /*! #if ($RH) !*/
            this.hash_cache = cached;
            /*! #end !*/

            //allocate so that there is at least one slot that remains allocated = false
            //this is compulsory to guarantee proper stop in searching loops
            this.resizeAt = HashContainers.expandAtCount(capacity, this.loadFactor);
        } catch (final OutOfMemoryError e) {

            throw new BufferAllocationException(
                    "Not enough memory to allocate buffers to grow from %d -> %d elements",
                    e,
                    (this.keys == null) ? 0 : this.keys.length,
                            capacity);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VType remove(final KType key) {

        if (Intrinsics.<KType> isEmpty(key)) {

            if (this.allocatedDefaultKey) {

                final VType previousValue = this.allocatedDefaultKeyValue;

                /*! #if ($TemplateOptions.VTypeGeneric) !*/
                //help the GC
                this.allocatedDefaultKeyValue = Intrinsics.<VType> empty();
                /*! #end !*/

                this.allocatedDefaultKey = false;
                return previousValue;
            }

            return this.defaultValue;
        }

        final int mask = this.keys.length - 1;

        final KType[] keys = Intrinsics.<KType[]> cast(this.keys);

        int slot = REHASH(key) & mask;
        KType existing;

        /*! #if ($RH) !*/
        int dist = 0;
        final int[] cached = this.hash_cache;
        /*!  #end !*/

        while (!Intrinsics.<KType> isEmpty(existing = keys[slot])
                /*! #if ($RH) !*/&& dist <= probe_distance(slot, cached) /*! #end !*/) {

            if (Intrinsics.<KType> equalsNotNull(key, existing)) {

                final VType value = Intrinsics.<VType> cast(this.values[slot]);

                shiftConflictingKeys(slot);

                return value;
            }
            slot = (slot + 1) & mask;

            /*! #if ($RH) !*/
            dist++;
            /*! #end !*/
        } //end while true

        return this.defaultValue;
    }

    /**
     * Shift all the slot-conflicting keys allocated to (and including) <code>slot</code>.
     */
    private void shiftConflictingKeys(int gapSlot) {
        final int mask = this.keys.length - 1;

        final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
        final VType[] values = Intrinsics.<VType[]> cast(this.values);

        /*! #if ($RH) !*/
        final int[] cached = this.hash_cache;
        /*!  #else
         final int perturb = this.perturbation;
         #end !*/

        // Perform shifts of conflicting keys to fill in the gap.
        int distance = 0;
        while (true) {

            final int slot = (gapSlot + (++distance)) & mask;

            final KType existing = keys[slot];
            final VType existingValue = values[slot];

            if (Intrinsics.<KType> isEmpty(existing)) {
                break;
            }

            /*! #if ($RH) !*/
            //use the cached value, no need to recompute
            final int idealSlotModMask = cached[slot];
            /*! #if($DEBUG) !*/
            //Check invariants
            assert idealSlotModMask == (REHASH(existing) & mask);
            /*! #end !*/
            /*! #else
            final int idealSlotModMask = REHASH2(existing, perturb) & mask;
            #end !*/

            //original HPPC code: shift = (slot - idealSlot) & mask;
            //equivalent to shift = (slot & mask - idealSlot & mask) & mask;
            //since slot and idealSlotModMask are already folded, we have :
            final int shift = (slot - idealSlotModMask) & mask;

            if (shift >= distance) {
                // Entry at this position was originally at or before the gap slot.
                // Move the conflict-shifted entry to the gap's position and repeat the procedure
                // for any entries to the right of the current position, treating it
                // as the new gap.
                keys[gapSlot] = existing;
                values[gapSlot] = existingValue;

                /*! #if ($RH) !*/
                cached[gapSlot] = idealSlotModMask;
                /*! #end !*/

                gapSlot = slot;
                distance = 0;
            }
        } //end while

        // Mark the last found gap slot without a conflict as empty.
        keys[gapSlot] = Intrinsics.<KType> empty();

        /* #if ($TemplateOptions.VTypeGeneric) */
        values[gapSlot] = Intrinsics.<VType> empty();
        /* #end */

        this.assigned--;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public int removeAll(final KTypeContainer<? super KType> other) {
        final int before = this.size();

        //1) other is a KTypeLookupContainer, so with fast lookup guarantees
        //and is bigger than this, so take advantage of both and iterate over this
        //and test other elements by their contains().
        if (other.size() >= before && other instanceof KTypeLookupContainer<?>) {

            if (this.allocatedDefaultKey) {

                if (other.contains(Intrinsics.<KType> empty())) {
                    this.allocatedDefaultKey = false;

                    /*! #if ($TemplateOptions.VTypeGeneric) !*/
                    //help the GC
                    this.allocatedDefaultKeyValue = Intrinsics.<VType> empty();
                    /*! #end !*/
                }
            }

            final KType[] keys = Intrinsics.<KType[]> cast(this.keys);

            for (int i = 0; i < keys.length;) {
                KType existing;
                if (!Intrinsics.<KType> isEmpty(existing = keys[i]) && other.contains(existing)) {

                    shiftConflictingKeys(i);
                    // Shift, do not increment slot.
                } else {
                    i++;
                }
            }
        } else {
            //2) Do not use contains() from container, which may lead to O(n**2) execution times,
            //so it iterate linearly and call remove() from map which is O(1).
            for (final KTypeCursor<? super KType> c : other) {

                remove(Intrinsics.<KType> cast(c.value));
            }
        }

        return before - this.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeAll(final KTypePredicate<? super KType> predicate) {
        final int before = this.size();

        if (this.allocatedDefaultKey) {

            if (predicate.apply(Intrinsics.<KType> empty())) {
                this.allocatedDefaultKey = false;

                /*! #if ($TemplateOptions.VTypeGeneric) !*/
                //help the GC
                this.allocatedDefaultKeyValue = Intrinsics.<VType> empty();
                /*! #end !*/
            }
        }

        final KType[] keys = Intrinsics.<KType[]> cast(this.keys);

        for (int i = 0; i < keys.length;) {
            KType existing;
            if (!Intrinsics.<KType> isEmpty(existing = keys[i]) && predicate.apply(existing)) {

                shiftConflictingKeys(i);
                // Shift, do not increment slot.
            } else {
                i++;
            }
        }

        return before - this.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeAll(final KTypeVTypePredicate<? super KType, ? super VType> predicate) {

        final int before = this.size();

        if (this.allocatedDefaultKey) {

            if (predicate.apply(Intrinsics.<KType> empty(), this.allocatedDefaultKeyValue)) {
                this.allocatedDefaultKey = false;

                /*! #if ($TemplateOptions.VTypeGeneric) !*/
                //help the GC
                this.allocatedDefaultKeyValue = Intrinsics.<VType> empty();
                /*! #end !*/
            }
        }

        final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
        final VType[] values = Intrinsics.<VType[]> cast(this.values);

        for (int i = 0; i < keys.length;) {
            KType existing;
            if (!Intrinsics.<KType> isEmpty(existing = keys[i]) && predicate.apply(existing, values[i])) {

                shiftConflictingKeys(i);
                // Shift, do not increment slot.
            } else {
                i++;
            }
        }

        return before - this.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VType get(final KType key) {
        if (Intrinsics.<KType> isEmpty(key)) {

            if (this.allocatedDefaultKey) {

                return this.allocatedDefaultKeyValue;
            }

            return this.defaultValue;
        }

        final int mask = this.keys.length - 1;

        final KType[] keys = Intrinsics.<KType[]> cast(this.keys);

        int slot = REHASH(key) & mask;
        KType existing;

        /*! #if ($RH) !*/
        int dist = 0;
        final int[] cached = this.hash_cache;
        /*! #end !*/

        while (!Intrinsics.<KType> isEmpty(existing = keys[slot])
                /*! #if ($RH) !*/&& dist <= probe_distance(slot, cached) /*! #end !*/) {

            if (Intrinsics.<KType> equalsNotNull(key, existing)) {

                return Intrinsics.<VType> cast(this.values[slot]);
            }
            slot = (slot + 1) & mask;

            /*! #if ($RH) !*/
            dist++;
            /*! #end !*/
        } //end while true

        return this.defaultValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(final KType key) {

        if (Intrinsics.<KType> isEmpty(key)) {

            return this.allocatedDefaultKey;
        }

        final int mask = this.keys.length - 1;

        final KType[] keys = Intrinsics.<KType[]> cast(this.keys);

        int slot = REHASH(key) & mask;
        KType existing;

        /*! #if ($RH) !*/
        int dist = 0;
        final int[] cached = this.hash_cache;
        /*! #end !*/

        while (!Intrinsics.<KType> isEmpty(existing = keys[slot])
                /*! #if ($RH) !*/&& dist <= probe_distance(slot, cached) /*! #end !*/) {

            if (Intrinsics.<KType> equalsNotNull(key, existing)) {
                return true;
            }
            slot = (slot + 1) & mask;

            /*! #if ($RH) !*/
            dist++;
            /*! #end !*/
        } //end while true

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        this.assigned = 0;

        // States are always cleared.
        this.allocatedDefaultKey = false;

        /*! #if ($TemplateOptions.VTypeGeneric) !*/
        //help the GC
        this.allocatedDefaultKeyValue = Intrinsics.<VType> empty();
        /*! #end !*/

        //Faster than Arrays.fill(keys, null); // Help the GC.
        KTypeArrays.blankArray(this.keys, 0, this.keys.length);

        /*! #if ($TemplateOptions.VTypeGeneric) !*/
        //Faster than Arrays.fill(values, null); // Help the GC.
        VTypeArrays.<VType> blankArray(Intrinsics.<VType[]> cast(this.values), 0, this.values.length);
        /*! #end !*/
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return this.assigned + (this.allocatedDefaultKey ? 1 : 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int capacity() {

        return this.resizeAt;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Note that an empty container may still contain many deleted keys (that occupy buffer
     * space). Adding even a single element to such a container may cause rehashing.</p>
     */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int h = 0;

        if (this.allocatedDefaultKey) {
            h += BitMixer.mix(this.allocatedDefaultKeyValue);
        }

        final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
        final VType[] values = Intrinsics.<VType[]> cast(this.values);

        for (int i = keys.length; --i >= 0;) {
            KType existing;
            if (!Intrinsics.<KType> isEmpty(existing = keys[i])) {

                h += BitMixer.mix(existing) ^ BitMixer.mix(values[i]);
            }
        }

        return h;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj != null) {
            if (obj == this) {
                return true;
            }

            //must be of the same class, subclasses are not comparable
            if (obj.getClass() != this.getClass()) {
                return false;
            }

            /* #if ($TemplateOptions.AnyGeneric) */
            @SuppressWarnings("unchecked")
            final/* #end */
            KTypeVTypeHashMap<KType, VType> other = (KTypeVTypeHashMap<KType, VType>) obj;

            //must be of the same size
            if (other.size() != this.size()) {
                return false;
            }

            final EntryIterator it = this.iterator();

            while (it.hasNext()) {
                final KTypeVTypeCursor<KType, VType> c = it.next();

                if (!other.containsKey(c.key)) {
                    //recycle
                    it.release();
                    return false;
                }

                final VType otherValue = other.get(c.key);

                if (!Intrinsics.<VType> equals(c.value, otherValue)) {
                    //recycle
                    it.release();
                    return false;
                }
            } //end while
            return true;
        }
        return false;
    }

    /**
     * An iterator implementation for {@link #iterator}.
     * Holds a KTypeVTypeCursor returning
     * (key, value, index) = (KType key, VType value, index the position in keys {@link KTypeVTypeHashMap#keys}, or keys.length for key = 0/null)
     */
    public final class EntryIterator extends AbstractIterator<KTypeVTypeCursor<KType, VType>>
    {
        public final KTypeVTypeCursor<KType, VType> cursor;

        public EntryIterator() {
            this.cursor = new KTypeVTypeCursor<KType, VType>();
            this.cursor.index = -2;
        }

        /**
         * Iterate backwards w.r.t the buffer, to
         * minimize collision chains when filling another hash container (ex. with putAll())
         */
        @Override
        protected KTypeVTypeCursor<KType, VType> fetch() {
            if (this.cursor.index == KTypeVTypeHashMap.this.keys.length + 1) {

                if (KTypeVTypeHashMap.this.allocatedDefaultKey) {

                    this.cursor.index = KTypeVTypeHashMap.this.keys.length;
                    this.cursor.key = Intrinsics.<KType> empty();
                    this.cursor.value = KTypeVTypeHashMap.this.allocatedDefaultKeyValue;

                    return this.cursor;

                }
                //no value associated with the default key, continue iteration...
                this.cursor.index = KTypeVTypeHashMap.this.keys.length;

            }

            int i = this.cursor.index - 1;

            while (i >= 0 && !is_allocated(i, Intrinsics.<KType[]> cast(KTypeVTypeHashMap.this.keys))) {
                i--;
            }

            if (i == -1) {
                return done();
            }

            this.cursor.index = i;
            this.cursor.key = Intrinsics.<KType> cast(KTypeVTypeHashMap.this.keys[i]);
            this.cursor.value = Intrinsics.<VType> cast(KTypeVTypeHashMap.this.values[i]);

            return this.cursor;
        }
    }

    /**
     * internal pool of EntryIterator
     */
    protected final IteratorPool<KTypeVTypeCursor<KType, VType>, EntryIterator> entryIteratorPool = new IteratorPool<KTypeVTypeCursor<KType, VType>, EntryIterator>(
            new ObjectFactory<EntryIterator>() {

                @Override
                public EntryIterator create() {
                    return new EntryIterator();
                }

                @Override
                public void initialize(final EntryIterator obj) {
                    obj.cursor.index = KTypeVTypeHashMap.this.keys.length + 1;
                }

                @Override
                public void reset(final EntryIterator obj) {
                    /*! #if ($TemplateOptions.KTypeGeneric) !*/
                    obj.cursor.key = null;
                    /*! #end !*/

                    /*! #if ($TemplateOptions.VTypeGeneric) !*/
                    obj.cursor.value = null;
                    /*! #end !*/
                }
            });

    /**
     * {@inheritDoc}
     */
    @Override
    public EntryIterator iterator() {
        //return new EntryIterator();
        return this.entryIteratorPool.borrow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends KTypeVTypeProcedure<? super KType, ? super VType>> T forEach(final T procedure) {

        if (this.allocatedDefaultKey) {

            procedure.apply(Intrinsics.<KType> empty(), this.allocatedDefaultKeyValue);
        }

        final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
        final VType[] values = Intrinsics.<VType[]> cast(this.values);

        //Iterate in reverse for side-stepping the longest conflict chain
        //in another hash, in case apply() is actually used to fill another hash container.
        for (int i = keys.length - 1; i >= 0; i--) {
            KType existing;
            if (!Intrinsics.<KType> isEmpty(existing = keys[i])) {
                procedure.apply(existing, values[i]);
            }
        }

        return procedure;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends KTypeVTypePredicate<? super KType, ? super VType>> T forEach(final T predicate) {
        if (this.allocatedDefaultKey) {

            if (!predicate.apply(Intrinsics.<KType> empty(), this.allocatedDefaultKeyValue)) {

                return predicate;
            }
        }

        final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
        final VType[] values = Intrinsics.<VType[]> cast(this.values);

        //Iterate in reverse for side-stepping the longest conflict chain
        //in another hash, in case apply() is actually used to fill another hash container.
        for (int i = keys.length - 1; i >= 0; i--) {
            KType existing;
            if (!Intrinsics.<KType> isEmpty(existing = keys[i])) {
                if (!predicate.apply(existing, values[i])) {
                    break;
                }
            }
        } //end for

        return predicate;
    }

    /**
     * {@inheritDoc}
     * @return a new KeysCollection view of the keys of this map.
     */
    @Override
    public KeysCollection keys() {
        return new KeysCollection();
    }

    /**
     * A view of the keys inside this map.
     */
    public final class KeysCollection extends AbstractKTypeCollection<KType> implements KTypeLookupContainer<KType>
    {
        private final KTypeVTypeHashMap<KType, VType> owner = KTypeVTypeHashMap.this;

        @Override
        public boolean contains(final KType e) {
            return containsKey(e);
        }

        @Override
        public <T extends KTypeProcedure<? super KType>> T forEach(final T procedure) {
            if (this.owner.allocatedDefaultKey) {

                procedure.apply(Intrinsics.<KType> empty());
            }

            final KType[] keys = Intrinsics.<KType[]> cast(this.owner.keys);

            //Iterate in reverse for side-stepping the longest conflict chain
            //in another hash, in case apply() is actually used to fill another hash container.
            for (int i = keys.length - 1; i >= 0; i--) {

                KType existing;
                if (!Intrinsics.<KType> isEmpty(existing = keys[i])) {
                    procedure.apply(existing);
                }
            }

            return procedure;
        }

        @Override
        public <T extends KTypePredicate<? super KType>> T forEach(final T predicate) {
            if (this.owner.allocatedDefaultKey) {

                if (!predicate.apply(Intrinsics.<KType> empty())) {

                    return predicate;
                }
            }

            final KType[] keys = Intrinsics.<KType[]> cast(this.owner.keys);

            //Iterate in reverse for side-stepping the longest conflict chain
            //in another hash, in case apply() is actually used to fill another hash container.
            for (int i = keys.length - 1; i >= 0; i--) {
                KType existing;
                if (!Intrinsics.<KType> isEmpty(existing = keys[i])) {
                    if (!predicate.apply(existing)) {
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
        public int removeAll(final KTypePredicate<? super KType> predicate) {
            return this.owner.removeAll(predicate);
        }

        @Override
        public int removeAll(final KType e) {
            final boolean hasKey = this.owner.containsKey(e);
            int result = 0;
            if (hasKey) {
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
                    public KeysIterator create() {
                        return new KeysIterator();
                    }

                    @Override
                    public void initialize(final KeysIterator obj) {
                        obj.cursor.index = KTypeVTypeHashMap.this.keys.length + 1;
                    }

                    @Override
                    public void reset(final KeysIterator obj) {
                        /*! #if ($TemplateOptions.KTypeGeneric) !*/
                        obj.cursor.value = null;
                        /*! #end !*/

                    }
                });

        @Override
        public KType[] toArray(final KType[] target) {
            int count = 0;

            if (this.owner.allocatedDefaultKey) {

                target[count++] = Intrinsics.<KType> empty();
            }

            final KType[] keys = Intrinsics.<KType[]> cast(this.owner.keys);

            for (int i = 0; i < keys.length; i++) {
                KType existing;
                if (!Intrinsics.<KType> isEmpty(existing = keys[i])) {
                    target[count++] = existing;
                }
            }

            assert count == this.owner.size();
            return target;
        }
    };

    /**
     * An iterator over the set of keys.
     * Holds a KTypeCursor returning (value, index) = (KType key, index the position in buffer {@link KTypeVTypeHashMap#keys}, or keys.length for key = 0/null.)
     */
    public final class KeysIterator extends AbstractIterator<KTypeCursor<KType>>
    {
        public final KTypeCursor<KType> cursor;

        public KeysIterator() {
            this.cursor = new KTypeCursor<KType>();
            this.cursor.index = -2;
        }

        /**
         * Iterate backwards w.r.t the buffer, to
         * minimize collision chains when filling another hash container (ex. with putAll())
         */
        @Override
        protected KTypeCursor<KType> fetch() {

            if (this.cursor.index == KTypeVTypeHashMap.this.keys.length + 1) {

                if (KTypeVTypeHashMap.this.allocatedDefaultKey) {

                    this.cursor.index = KTypeVTypeHashMap.this.keys.length;
                    this.cursor.value = Intrinsics.<KType> empty();

                    return this.cursor;

                }
                //no value associated with the default key, continue iteration...
                this.cursor.index = KTypeVTypeHashMap.this.keys.length;

            }

            int i = this.cursor.index - 1;

            while (i >= 0 && !is_allocated(i, Intrinsics.<KType[]> cast(KTypeVTypeHashMap.this.keys))) {
                i--;
            }

            if (i == -1) {
                return done();
            }

            this.cursor.index = i;
            this.cursor.value = Intrinsics.<KType> cast(KTypeVTypeHashMap.this.keys[i]);

            return this.cursor;
        }
    }

    /**
     * {@inheritDoc}
     * @return a new ValuesCollection view of the values of this map.
     */
    @Override
    public ValuesCollection values() {
        return new ValuesCollection();
    }

    /**
     * A view over the set of values of this map.
     */
    public final class ValuesCollection extends AbstractKTypeCollection<VType>
    {
        private final KTypeVTypeHashMap<KType, VType> owner = KTypeVTypeHashMap.this;

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
        public boolean contains(final VType value) {
            if (this.owner.allocatedDefaultKey && Intrinsics.<VType> equals(value, this.owner.allocatedDefaultKeyValue)) {

                return true;
            }

            // This is a linear scan over the values, but it's in the contract, so be it.

            final KType[] keys = Intrinsics.<KType[]> cast(this.owner.keys);
            final VType[] values = Intrinsics.<VType[]> cast(this.owner.values);

            for (int slot = 0; slot < keys.length; slot++) {
                if (is_allocated(slot, keys) && Intrinsics.<VType> equals(value, values[slot])) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public <T extends KTypeProcedure<? super VType>> T forEach(final T procedure) {
            if (this.owner.allocatedDefaultKey) {

                procedure.apply(this.owner.allocatedDefaultKeyValue);
            }

            final KType[] keys = Intrinsics.<KType[]> cast(this.owner.keys);
            final VType[] values = Intrinsics.<VType[]> cast(this.owner.values);

            for (int slot = 0; slot < keys.length; slot++) {
                if (is_allocated(slot, keys)) {

                    procedure.apply(values[slot]);
                }
            }

            return procedure;
        }

        @Override
        public <T extends KTypePredicate<? super VType>> T forEach(final T predicate) {
            if (this.owner.allocatedDefaultKey) {

                if (!predicate.apply(this.owner.allocatedDefaultKeyValue)) {
                    return predicate;
                }
            }

            final KType[] keys = Intrinsics.<KType[]> cast(this.owner.keys);
            final VType[] values = Intrinsics.<VType[]> cast(this.owner.values);

            for (int slot = 0; slot < keys.length; slot++) {
                if (is_allocated(slot, keys)) {
                    if (!predicate.apply(values[slot])) {
                        break;
                    }
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
         */
        @Override
        public int removeAll(final VType e) {
            final int before = this.owner.size();

            if (this.owner.allocatedDefaultKey) {

                if (Intrinsics.<VType> equals(e, this.owner.allocatedDefaultKeyValue)) {

                    this.owner.allocatedDefaultKey = false;

                    /*! #if ($TemplateOptions.VTypeGeneric) !*/
                    //help the GC
                    this.owner.allocatedDefaultKeyValue = Intrinsics.<VType> empty();
                    /*! #end !*/
                }
            }

            final KType[] keys = Intrinsics.<KType[]> cast(this.owner.keys);
            final VType[] values = Intrinsics.<VType[]> cast(this.owner.values);

            for (int slot = 0; slot < keys.length;) {
                if (is_allocated(slot, keys) && Intrinsics.<VType> equals(e, values[slot])) {

                    shiftConflictingKeys(slot);
                    // Shift, do not increment slot.
                } else {
                    slot++;
                }
            }
            return before - this.owner.size();
        }

        /**
         * {@inheritDoc}
         * Indeed removes all the (key,value) pairs matching
         * the predicate for the values, from  the map.
         */
        @Override
        public int removeAll(final KTypePredicate<? super VType> predicate) {
            final int before = this.owner.size();

            if (this.owner.allocatedDefaultKey) {

                if (predicate.apply(this.owner.allocatedDefaultKeyValue)) {

                    this.owner.allocatedDefaultKey = false;

                    /*! #if ($TemplateOptions.VTypeGeneric) !*/
                    //help the GC
                    this.owner.allocatedDefaultKeyValue = Intrinsics.<VType> empty();
                    /*! #end !*/
                }
            }

            final KType[] keys = Intrinsics.<KType[]> cast(this.owner.keys);
            final VType[] values = Intrinsics.<VType[]> cast(this.owner.values);

            for (int slot = 0; slot < keys.length;) {
                if (is_allocated(slot, keys) && predicate.apply(values[slot])) {

                    shiftConflictingKeys(slot);
                    // Shift, do not increment slot.
                } else {
                    slot++;
                }
            }
            return before - this.owner.size();
        }

        /**
         * {@inheritDoc}
         *  Alias for clear() the whole map.
         */
        @Override
        public void clear() {
            this.owner.clear();
        }

        /**
         * internal pool of ValuesIterator
         */
        protected final IteratorPool<KTypeCursor<VType>, ValuesIterator> valuesIteratorPool = new IteratorPool<KTypeCursor<VType>, ValuesIterator>(
                new ObjectFactory<ValuesIterator>() {

                    @Override
                    public ValuesIterator create() {
                        return new ValuesIterator();
                    }

                    @Override
                    public void initialize(final ValuesIterator obj) {
                        obj.cursor.index = KTypeVTypeHashMap.this.keys.length + 1;
                    }

                    @Override
                    public void reset(final ValuesIterator obj) {

                        /*! #if ($TemplateOptions.VTypeGeneric) !*/
                        obj.cursor.value = null;
                        /*! #end !*/
                    }
                });

        @Override
        public VType[] toArray(final VType[] target) {
            int count = 0;

            if (this.owner.allocatedDefaultKey) {

                target[count++] = this.owner.allocatedDefaultKeyValue;
            }

            final KType[] keys = Intrinsics.<KType[]> cast(this.owner.keys);

            final VType[] values = Intrinsics.<VType[]> cast(this.owner.values);

            for (int i = 0; i < values.length; i++) {
                if (is_allocated(i, keys)) {
                    target[count++] = values[i];
                }
            }

            assert count == this.owner.size();
            return target;
        }
    }

    /**
     * An iterator over the set of values.
     * Holds a KTypeCursor returning (value, index) = (VType value, index the position in buffer {@link KTypeVTypeHashMap#values},
     * or values.length for value = {@link KTypeVTypeHashMap#allocatedDefaultKeyValue}).
     */
    public final class ValuesIterator extends AbstractIterator<KTypeCursor<VType>>
    {
        public final KTypeCursor<VType> cursor;

        public ValuesIterator() {
            this.cursor = new KTypeCursor<VType>();
            this.cursor.index = -2;
        }

        /**
         * Iterate backwards w.r.t the buffer, to
         * minimize collision chains when filling another hash container (ex. with putAll())
         */
        @Override
        protected KTypeCursor<VType> fetch() {
            if (this.cursor.index == KTypeVTypeHashMap.this.values.length + 1) {

                if (KTypeVTypeHashMap.this.allocatedDefaultKey) {

                    this.cursor.index = KTypeVTypeHashMap.this.values.length;
                    this.cursor.value = KTypeVTypeHashMap.this.allocatedDefaultKeyValue;

                    return this.cursor;

                }
                //no value associated with the default key, continue iteration...
                this.cursor.index = KTypeVTypeHashMap.this.keys.length;

            }

            int i = this.cursor.index - 1;

            while (i >= 0 && !is_allocated(i, Intrinsics.<KType[]> cast(KTypeVTypeHashMap.this.keys))) {
                i--;
            }

            if (i == -1) {
                return done();
            }

            this.cursor.index = i;
            this.cursor.value = Intrinsics.<VType> cast(KTypeVTypeHashMap.this.values[i]);

            return this.cursor;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KTypeVTypeHashMap<KType, VType> clone() {
        //clone to size() to prevent some cases of exponential sizes,
        final KTypeVTypeHashMap<KType, VType> cloned = new KTypeVTypeHashMap<KType, VType>(this.size(), this.loadFactor);

        //We must NOT clone because of independent perturbations seeds
        cloned.putAll(this);

        cloned.allocatedDefaultKeyValue = this.allocatedDefaultKeyValue;
        cloned.allocatedDefaultKey = this.allocatedDefaultKey;
        cloned.defaultValue = this.defaultValue;

        return cloned;
    }

    /**
     * Convert the contents of this map to a human-friendly string.
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("[");

        boolean first = true;
        for (final KTypeVTypeCursor<KType, VType> cursor : this) {
            if (!first) {
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
     * Creates a hash map from two index-aligned arrays of key-value pairs. Default load factor is used.
     */
    public static <KType, VType> KTypeVTypeHashMap<KType, VType> from(final KType[] keys, final VType[] values) {
        if (keys.length != values.length) {
            throw new IllegalArgumentException("Arrays of keys and values must have an identical length.");
        }

        final KTypeVTypeHashMap<KType, VType> map = new KTypeVTypeHashMap<KType, VType>(keys.length);

        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return map;
    }

    /**
     * Create a hash map from another associative container. (constructor shortcut) Default load factor is used.
     */
    public static <KType, VType> KTypeVTypeHashMap<KType, VType> from(
            final KTypeVTypeAssociativeContainer<KType, VType> container) {
        return new KTypeVTypeHashMap<KType, VType>(container);
    }

    /**
     * Create a new hash map without providing the full generic signature
     * (constructor shortcut).
     */
    public static <KType, VType> KTypeVTypeHashMap<KType, VType> newInstance() {
        return new KTypeVTypeHashMap<KType, VType>();
    }

    /**
     * Create a new hash map with initial capacity and load factor control.
     * (constructor shortcut).
     */
    public static <KType, VType> KTypeVTypeHashMap<KType, VType> newInstance(final int initialCapacity,
            final double loadFactor) {
        return new KTypeVTypeHashMap<KType, VType>(initialCapacity, loadFactor);
    }

    /**
     * Returns the "default value" value used in containers methods returning
     * "default value"
     */
    @Override
    public VType getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Set the "default value" value to be used in containers methods returning
     * "default value"
     */
    @Override
    public void setDefaultValue(final VType defaultValue) {
        this.defaultValue = defaultValue;
    }

    //Test for existence in template
    /*! #if ($TemplateOptions.declareInline("is_allocated(slot, keys)",
        "<*,*>==>!Intrinsics.<KType>isEmpty(keys[slot])")) !*/
    /**
     *  template version
     * (actual method is inlined in generated code)
     */
    private boolean is_allocated(final int slot, final KType[] keys) {

        return !Intrinsics.<KType> isEmpty(keys[slot]);
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.declareInline("probe_distance(slot, cache)",
        "<*,*>==>slot < cache[slot] ? slot + cache.length - cache[slot] : slot - cache[slot]")) !*/
    /**
     * (actual method is inlined in generated code)
     */
    private int probe_distance(final int slot, final int[] cache) {

        final int rh = cache[slot];

        /*! #if($DEBUG) !*/
        //Check : cached hashed slot is == computed value
        final int mask = cache.length - 1;
        assert rh == (REHASH(Intrinsics.<KType> cast(this.keys[slot])) & mask);
        /*! #end !*/

        if (slot < rh) {
            //wrap around
            return slot + cache.length - rh;
        }

        return slot - rh;
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.declareInline("REHASH(value)",
    "<Object,*>==>BitMixer.mix(value.hashCode() , this.perturbation)",
    "<*,*>==>BitMixer.mix(value , this.perturbation)")) !*/
    /**
     * REHASH method for rehashing the keys.
     * (inlined in generated code)
     * Thanks to single array mode, no need to check for null/0 or booleans.
     */
    private int REHASH(final KType value) {

        return BitMixer.mix(value.hashCode(), this.perturbation);
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.declareInline("REHASH2(value, perturb)",
    "<Object,*>==>BitMixer.mix(value.hashCode() , perturb)",
    "<*,*>==>BitMixer.mix(value , perturb)")) !*/
    /**
     * REHASH2 method for rehashing the keys with perturbation seed as parameter
     * (inlined in generated code)
     * Thanks to single array mode, no need to check for null/0 or booleans.
     */
    private int REHASH2(final KType value, final int perturb) {

        return BitMixer.mix(value.hashCode(), perturb);
    }
    /*! #end !*/
}
