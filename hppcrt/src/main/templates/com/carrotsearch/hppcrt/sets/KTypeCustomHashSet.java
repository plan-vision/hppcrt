package com.carrotsearch.hppcrt.sets;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.cursors.*;
import com.carrotsearch.hppcrt.predicates.*;
import com.carrotsearch.hppcrt.procedures.*;
import com.carrotsearch.hppcrt.strategies.*;
import com.carrotsearch.hppcrt.hash.*;

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
/*! ${TemplateOptions.doNotGenerateKType( "byte", "char", "short", "float", "double" )} !*/
/*! #set( $ROBIN_HOOD_FOR_ALL = true) !*/
/*! #set( $DEBUG = false) !*/
//If RH is defined, RobinHood Hashing is in effect :
/*! #set( $RH = $ROBIN_HOOD_FOR_ALL) !*/

/**
 * A hash set of <code>KType</code>s, implemented using using open
 * addressing with linear probing for collision resolution.
 * 
 * <p>
 * The difference with {@link KTypeHashSet} is that it uses a
 * {@link KTypeHashingStrategy} to compare objects externally instead of using
 * the built-in hashCode() /  equals(). In particular, the management of <code>null</code>
 * keys is up to the {@link KTypeHashingStrategy} implementation.
 * 
 * <p>
 * The internal buffers of this implementation ({@link #keys}, etc...)
 * are always allocated to the nearest size that is a power of two. When
 * the capacity exceeds the given load factor, the buffer size is doubled.
 * </p>
 * 
 * <p><b>Important note.</b> The implementation uses power-of-two tables and linear
 * probing, which may cause poor performance (many collisions) if hash values are
 * not properly distributed. Therefore, it is up to the {@link KTypeHashingStrategy} to
 * assure good performance.</p>
 * 
 *
#if ($TemplateOptions.KTypeGeneric)
 * <p>This implementation supports <code>null</code> keys. In addition, objects passed to the {@link KTypeHashingStrategy} are guaranteed to be not-<code>null</code>. </p>
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
public class KTypeCustomHashSet<KType>
        extends AbstractKTypeCollection<KType>
        implements KTypeLookupContainer<KType>, KTypeSet<KType>, Cloneable
{
    /**
     * Hash-indexed array holding all set entries.
     * <p>
     * Direct set iteration: iterate  {keys[i]} for i in [0; keys.length[ where keys[i] != 0/null, then also
     * {0/null} is in the set if {@link #allocatedDefaultKey} = true.
     * </p>
     */
    public/*! #if ($TemplateOptions.KTypePrimitive)
          KType []
          #else !*/
    Object[]
    /*! #end !*/
    keys;

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
     * Custom hashing strategy :
     * comparisons and hash codes of keys will be computed
     * with the strategy methods instead of the native Object equals() and hashCode() methods.
     */
    protected final KTypeHashingStrategy<? super KType> hashStrategy;

    /**
     * Creates a hash set with the default capacity of {@value Containers#DEFAULT_EXPECTED_ELEMENTS},
     * load factor of {@value HashContainers#DEFAULT_LOAD_FACTOR}, using the hashStrategy as {@link KTypeHashingStrategy}
     */
    public KTypeCustomHashSet(final KTypeHashingStrategy<? super KType> hashStrategy) {
        this(Containers.DEFAULT_EXPECTED_ELEMENTS, HashContainers.DEFAULT_LOAD_FACTOR, hashStrategy);
    }

    /**
     * Creates a hash set with the given capacity,
     * load factor of {@value HashContainers#DEFAULT_LOAD_FACTOR}, using the hashStrategy as {@link KTypeHashingStrategy}
     */
    public KTypeCustomHashSet(final int initialCapacity, final KTypeHashingStrategy<? super KType> hashStrategy) {
        this(initialCapacity, HashContainers.DEFAULT_LOAD_FACTOR, hashStrategy);
    }

    /**
     * Creates a hash set with the given capacity and load factor, using the hashStrategy as {@link KTypeHashingStrategy}
     */
    public KTypeCustomHashSet(final int initialCapacity, final double loadFactor,
            final KTypeHashingStrategy<? super KType> hashStrategy) {
        //only accept not-null strategies.
        if (hashStrategy != null) {
            this.hashStrategy = hashStrategy;
        } else {

            throw new IllegalArgumentException("KTypeOpenCustomHashSet() cannot have a null hashStrategy !");
        }

        this.loadFactor = loadFactor;
        //take into account of the load factor to guarantee no reallocations before reaching  initialCapacity.
        allocateBuffers(HashContainers.minBufferSize(initialCapacity, loadFactor));
    }

    /**
     * Creates a hash set from elements of another container. Default load factor is used.
     */
    public KTypeCustomHashSet(final KTypeContainer<KType> container,
            final KTypeHashingStrategy<? super KType> hashStrategy) {
        this(container.size(), hashStrategy);
        addAll(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(KType e) {
        if (Intrinsics.<KType> isEmpty(e)) {

            if (this.allocatedDefaultKey) {

                return false;
            }

            this.allocatedDefaultKey = true;

            return true;
        }

        final int mask = this.keys.length - 1;

        final KTypeHashingStrategy<? super KType> strategy = this.hashStrategy;

        final KType[] keys = Intrinsics.<KType[]> cast(this.keys);

        int slot = REHASH(strategy, e) & mask;
        KType existing;

        /*! #if ($RH) !*/
        final int[] cached = this.hash_cache;
        KType tmpKey;
        int tmpAllocated;
        int initial_slot = slot;
        int dist = 0;
        int existing_distance = 0;
        /*! #end !*/

        while (!Intrinsics.<KType> isEmpty(existing = keys[slot])) {
            if (strategy.equals(e, existing)) {
                return false;
            }

            /*! #if ($RH) !*/
            //re-shuffle keys to minimize variance
            existing_distance = probe_distance(slot, cached);

            if (dist > existing_distance) {
                //swap current (key, value, initial_slot) with slot places
                tmpKey = keys[slot];
                keys[slot] = e;
                e = tmpKey;

                tmpAllocated = cached[slot];
                cached[slot] = initial_slot;
                initial_slot = tmpAllocated;

                /*! #if($DEBUG) !*/
                //Check invariants
                assert cached[slot] == (REHASH(strategy, keys[slot]) & mask);
                assert initial_slot == (REHASH(strategy, e) & mask);
                /*! #end !*/

                dist = existing_distance;
            }
            /*! #end !*/

            slot = (slot + 1) & mask;
            /*! #if ($RH) !*/
            dist++;
            /*! #end !*/
        }

        // Check if we need to grow. If so, reallocate new data,
        // fill in the last element and rehash.
        if (this.assigned == this.resizeAt) {

            expandAndAdd(e, slot);
        } else {
            this.assigned++;
            /*! #if ($RH) !*/
            cached[slot] = initial_slot;
            /*!  #end !*/

            keys[slot] = e;

            /*! #if ($RH) !*/
            /*! #if($DEBUG) !*/
            //Check invariants
            assert cached[slot] == (REHASH(strategy, keys[slot]) & mask);
            /*! #end !*/
            /*! #end !*/
        }
        return true;
    }

    /**
     * Adds two elements to the set.
     */
    public int add(final KType e1, final KType e2) {
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
    public int add(final KType... elements) {
        int count = 0;
        for (final KType e : elements) {
            if (add(e)) {
                count++;
            }
        }
        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int addAll(final KTypeContainer<? extends KType> container) {
        return addAll((Iterable<? extends KTypeCursor<? extends KType>>) container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int addAll(final Iterable<? extends KTypeCursor<? extends KType>> iterable) {
        int count = 0;
        for (final KTypeCursor<? extends KType> cursor : iterable) {
            if (add(cursor.value)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Expand the internal storage buffers (capacity) or rehash current
     * keys and values if there are a lot of deleted slots.
     */
    private void expandAndAdd(final KType pendingKey, final int freeSlot) {
        assert this.assigned == this.resizeAt;

        //default sentinel value is never in the keys[] array, so never trigger reallocs
        assert (!Intrinsics.<KType> isEmpty(pendingKey));

        // Try to allocate new buffers first. If we OOM, it'll be now without
        // leaving the data structure in an inconsistent state.
        final KType[] oldKeys = Intrinsics.<KType[]> cast(this.keys);

        allocateBuffers(HashContainers.nextBufferSize(this.keys.length, this.assigned, this.loadFactor));

        // We have succeeded at allocating new data so insert the pending key/value at
        // the free slot in the old arrays before rehashing.

        this.assigned++;

        oldKeys[freeSlot] = pendingKey;

        //Variables for adding
        final int mask = this.keys.length - 1;

        final KTypeHashingStrategy<? super KType> strategy = this.hashStrategy;

        KType e = Intrinsics.<KType> empty();
        //adding phase
        int slot = -1;

        final KType[] keys = Intrinsics.<KType[]> cast(this.keys);

        /*! #if ($RH) !*/
        final int[] cached = this.hash_cache;
        /*! #end !*/

        /*! #if ($RH) !*/
        KType tmpKey = Intrinsics.<KType> empty();
        int tmpAllocated = -1;
        int initial_slot = -1;
        int dist = -1;
        int existing_distance = -1;
        /*! #end !*/

        //iterate all the old arrays to add in the newly allocated buffers
        //It is important to iterate backwards to minimize the conflict chain length !
        final int perturb = this.perturbation;

        for (int i = oldKeys.length; --i >= 0;) {

            if (!Intrinsics.<KType> isEmpty(e = oldKeys[i])) {

                slot = REHASH2(strategy, e, perturb) & mask;

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
                        keys[slot] = e;
                        e = tmpKey;

                        tmpAllocated = cached[slot];
                        cached[slot] = initial_slot;
                        initial_slot = tmpAllocated;

                        /*! #if($DEBUG) !*/
                        //Check invariants
                        assert cached[slot] == (REHASH(strategy, keys[slot]) & mask);
                        assert initial_slot == (REHASH(strategy, e) & mask);
                        /*! #end !*/

                        dist = existing_distance;
                    } //endif
                    /*! #end !*/

                    slot = (slot + 1) & mask;

                    /*! #if ($RH) !*/
                    dist++;
                    /*! #end !*/
                } //end while

                //place it at that position
                /*! #if ($RH) !*/
                cached[slot] = initial_slot;
                /*! #end !*/

                keys[slot] = e;

                /*! #if ($RH) !*/
                /*! #if($DEBUG) !*/
                //Check invariants
                assert cached[slot] == (REHASH(strategy, keys[slot]) & mask);
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

            /*! #if ($RH) !*/
            final int[] allocated = new int[capacity];
            /*! #end !*/

            this.keys = keys;

            /*! #if ($RH) !*/
            this.hash_cache = allocated;
            /*! #end !*/

            //allocate so that there is at least one slot that remains allocated = false
            //this is compulsory to guarantee proper stop in searching loops
            this.resizeAt = HashContainers.expandAtCount(capacity, this.loadFactor);
        } catch (final OutOfMemoryError e) {

            throw new BufferAllocationException("Not enough memory to allocate buffers to grow from %d -> %d elements", e,
                    this.keys.length, capacity);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeAll(final KType key) {
        return remove(key) ? 1 : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(final KType key) {
        if (Intrinsics.<KType> isEmpty(key)) {

            if (this.allocatedDefaultKey) {

                this.allocatedDefaultKey = false;
                return true;
            }

            return false;
        }

        final int mask = this.keys.length - 1;

        final KTypeHashingStrategy<? super KType> strategy = this.hashStrategy;

        final KType[] keys = Intrinsics.<KType[]> cast(this.keys);

        int slot = REHASH(strategy, key) & mask;
        KType existing;

        /*! #if ($RH) !*/
        int dist = 0;
        final int[] cached = this.hash_cache;
        /*! #end !*/

        while (!Intrinsics.<KType> isEmpty(existing = keys[slot])
                /*! #if ($RH) !*/&& dist <= probe_distance(slot, cached) /*! #end !*/) {
            if (strategy.equals(key, existing)) {

                shiftConflictingKeys(slot);
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
     * Shift all the slot-conflicting keys allocated to (and including) <code>slot</code>.
     */
    private void shiftConflictingKeys(int gapSlot) {
        final int mask = this.keys.length - 1;

        final KTypeHashingStrategy<? super KType> strategy = this.hashStrategy;

        final KType[] keys = Intrinsics.<KType[]> cast(this.keys);

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

            if (Intrinsics.<KType> isEmpty(existing)) {
                break;
            }

            /*! #if ($RH) !*/
            //use the cached value, no need to recompute
            final int idealSlotModMask = cached[slot];
            /*! #if($DEBUG) !*/
            //Check invariants
            assert idealSlotModMask == (REHASH(strategy, existing) & mask);
            /*! #end !*/
            /*! #else
            final int idealSlotModMask = REHASH2(strategy, existing, perturb) & mask;
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

                /*! #if ($RH) !*/
                cached[gapSlot] = idealSlotModMask;
                /*! #end !*/

                gapSlot = slot;
                distance = 0;
            }
        } //end while

        // Mark the last found gap slot without a conflict as empty.
        keys[gapSlot] = Intrinsics.<KType> empty();

        this.assigned--;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final KType key) {
        if (Intrinsics.<KType> isEmpty(key)) {

            return this.allocatedDefaultKey;
        }

        final int mask = this.keys.length - 1;

        final KTypeHashingStrategy<? super KType> strategy = this.hashStrategy;

        final KType[] keys = Intrinsics.<KType[]> cast(this.keys);

        int slot = REHASH(strategy, key) & mask;
        KType existing;

        /*! #if ($RH) !*/
        final int[] cached = this.hash_cache;
        int dist = 0;
        /*! #end !*/

        while (!Intrinsics.<KType> isEmpty(existing = keys[slot])
                /*! #if ($RH) !*/&& dist <= probe_distance(slot, cached) /*! #end !*/) {
            if (strategy.equals(key, existing)) {
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
     * 
     * <p>Does not release internal buffers.</p>
     */
    @Override
    public void clear() {
        this.assigned = 0;

        // States are always cleared.
        this.allocatedDefaultKey = false;

        //Faster than Arrays.fill(keys, null); // Help the GC.
        KTypeArrays.blankArray(this.keys, 0, this.keys.length);
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
     */
    @Override
    public int hashCode() {
        final KTypeHashingStrategy<? super KType> strategy = this.hashStrategy;

        int h = 0;
        //allocated default key has hash = 0

        final KType[] keys = Intrinsics.<KType[]> cast(this.keys);

        for (int i = keys.length; --i >= 0;) {
            KType existing;
            if (!Intrinsics.<KType> isEmpty(existing = keys[i])) {

                h += BitMixer.mix(strategy.computeHashCode(existing));
            }
        }

        return h;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
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

            //their hash strategies MUST be "equal", i.e apply the same equivalence criteria.
            if (!this.hashStrategy.equals(((KTypeCustomHashSet<KType>) obj).hashStrategy)) {

                return false;
            }

            final KTypeCustomHashSet<KType> other = (KTypeCustomHashSet<KType>) obj;

            //must be of the same size
            if (other.size() != this.size()) {
                return false;
            }

            final EntryIterator it = this.iterator();

            while (it.hasNext()) {
                if (!other.contains(it.next().value)) {
                    //recycle
                    it.release();
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * An iterator implementation for {@link #iterator}.
     * Holds a KTypeCursor returning (value, index) = (KType value, index the position in {@link KTypeCustomHashSet#keys}, or keys.length for key = 0/null.)
     */
    public final class EntryIterator extends AbstractIterator<KTypeCursor<KType>>
    {
        public final KTypeCursor<KType> cursor;

        public EntryIterator() {
            this.cursor = new KTypeCursor<KType>();
            this.cursor.index = -2;
        }

        /**
         * Iterate backwards w.r.t the buffer, to
         * minimize collision chains when filling another hash container (ex. with putAll())
         */
        @Override
        protected KTypeCursor<KType> fetch() {
            if (this.cursor.index == KTypeCustomHashSet.this.keys.length + 1) {

                if (KTypeCustomHashSet.this.allocatedDefaultKey) {

                    this.cursor.index = KTypeCustomHashSet.this.keys.length;
                    this.cursor.value = Intrinsics.<KType> empty();

                    return this.cursor;

                }
                //no value associated with the default key, continue iteration...
                this.cursor.index = KTypeCustomHashSet.this.keys.length;
            }

            int i = this.cursor.index - 1;

            while (i >= 0 && !is_allocated(i, Intrinsics.<KType[]> cast(KTypeCustomHashSet.this.keys))) {
                i--;
            }

            if (i == -1) {
                return done();
            }

            this.cursor.index = i;
            this.cursor.value = Intrinsics.<KType> cast(KTypeCustomHashSet.this.keys[i]);
            return this.cursor;
        }
    }

    /**
     * internal pool of EntryIterator
     */
    protected final IteratorPool<KTypeCursor<KType>, EntryIterator> entryIteratorPool = new IteratorPool<KTypeCursor<KType>, EntryIterator>(
            new ObjectFactory<EntryIterator>() {

                @Override
                public EntryIterator create() {

                    return new EntryIterator();
                }

                @Override
                public void initialize(final EntryIterator obj) {
                    obj.cursor.index = KTypeCustomHashSet.this.keys.length + 1;
                }

                @Override
                public void reset(final EntryIterator obj) {
                    /*! #if ($TemplateOptions.KTypeGeneric) !*/
                    obj.cursor.value = null;
                    /*! #end !*/

                }
            });

    /**
     * {@inheritDoc}
     * 
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
    public <T extends KTypeProcedure<? super KType>> T forEach(final T procedure) {
        if (this.allocatedDefaultKey) {

            procedure.apply(Intrinsics.<KType> empty());
        }

        final KType[] keys = Intrinsics.<KType[]> cast(this.keys);

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

    /**
     * {@inheritDoc}
     */
    @Override
    public KType[] toArray(final KType[] target) {
        int count = 0;

        if (this.allocatedDefaultKey) {

            target[count++] = Intrinsics.<KType> empty();
        }

        final KType[] keys = Intrinsics.<KType[]> cast(this.keys);

        for (int i = 0; i < keys.length; i++) {
            KType existing;
            if (!Intrinsics.<KType> isEmpty(existing = keys[i])) {
                target[count++] = existing;
            }
        }
        assert count == this.size();

        return target;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KTypeCustomHashSet<KType> clone() {
        //clone to size() to prevent eventual exponential growth
        final KTypeCustomHashSet<KType> cloned = new KTypeCustomHashSet<KType>(this.size(), this.loadFactor,
                this.hashStrategy);

        //We must NOT clone because of the independent perturbation seeds
        cloned.addAll(this);

        cloned.allocatedDefaultKey = this.allocatedDefaultKey;

        return cloned;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends KTypePredicate<? super KType>> T forEach(final T predicate) {

        if (this.allocatedDefaultKey) {

            if (!predicate.apply(Intrinsics.<KType> empty())) {

                return predicate;
            }
        }

        final KType[] keys = Intrinsics.<KType[]> cast(this.keys);

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
    public int removeAll(final KTypePredicate<? super KType> predicate) {
        final int before = this.size();

        if (this.allocatedDefaultKey) {

            if (predicate.apply(Intrinsics.<KType> empty())) {
                this.allocatedDefaultKey = false;
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
     * Create a set from a variable number of arguments or an array of <code>KType</code>.
     */
    public static <KType> KTypeCustomHashSet<KType> from(final KTypeHashingStrategy<? super KType> hashStrategy,
            final KType... elements) {
        final KTypeCustomHashSet<KType> set = new KTypeCustomHashSet<KType>(elements.length, hashStrategy);
        set.add(elements);
        return set;
    }

    /**
     * Create a set from elements of another container.
     */
    public static <KType> KTypeCustomHashSet<KType> from(final KTypeContainer<KType> container,
            final KTypeHashingStrategy<? super KType> hashStrategy) {
        return new KTypeCustomHashSet<KType>(container, hashStrategy);
    }

    /**
     * Create a new hash set with default parameters (shortcut
     * instead of using a constructor).
     */
    public static <KType> KTypeCustomHashSet<KType> newInstance(final KTypeHashingStrategy<? super KType> hashStrategy) {
        return new KTypeCustomHashSet<KType>(hashStrategy);
    }

    /**
     * Returns a new object of this class with no need to declare generic type
     * (shortcut instead of using a constructor).
     */
    public static <KType> KTypeCustomHashSet<KType> newInstance(final int initialCapacity, final double loadFactor,
            final KTypeHashingStrategy<? super KType> hashStrategy) {
        return new KTypeCustomHashSet<KType>(initialCapacity, loadFactor, hashStrategy);
    }

    /**
     * Returns the current  hashing strategy in use.
     */
    public KTypeHashingStrategy<? super KType> strategy() {
        return this.hashStrategy;
    }

    //Test for existence in template
    /*! #if ($TemplateOptions.declareInline("is_allocated(slot, keys)",
    "<*>==>!Intrinsics.<KType>isEmpty(keys[slot])")) !*/
    /**
     *  template version
     * (actual method is inlined in generated code)
     */
    private boolean is_allocated(final int slot, final KType[] keys) {

        return !Intrinsics.<KType> isEmpty(keys[slot]);
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.declareInline("probe_distance(slot, cached)",
        "<*>==>slot < cached[slot] ? slot + cached.length - cached[slot] : slot - cached[slot]")) !*/
    /**
     * (actual method is inlined in generated code)
     */
    private int probe_distance(final int slot, final int[] cached) {

        final int rh = cached[slot];

        /*! #if($DEBUG) !*/
        //Check : cached hashed slot is == computed value
        final int mask = cached.length - 1;
        assert rh == (REHASH(this.hashStrategy, Intrinsics.<KType> cast(this.keys[slot])) & mask);
        /*! #end !*/

        if (slot < rh) {
            //wrap around
            return slot + cached.length - rh;
        }

        return slot - rh;
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.declareInline("REHASH(strategy, value)",
    "<*>==>MurmurHash3.mix(strategy.computeHashCode(value) , this.perturbation )")) !*/
    /**
     * (inlined in generated code)
     */
    private int REHASH(final KTypeHashingStrategy<? super KType> strategy, final KType value) {

        return MurmurHash3.mix(strategy.computeHashCode(value), this.perturbation);
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.declareInline("REHASH2(strategy, value, perturb)",
    "<*>==>MurmurHash3.mix(strategy.computeHashCode(value) , perturb)")) !*/
    /**
     * REHASH2 method for rehashing the keys with perturbation seed as parameter
     * (inlined in generated code)
     * Thanks to single array mode, no need to check for null/0 or booleans.
     */
    private int REHASH2(final KTypeHashingStrategy<? super KType> strategy, final KType value, final int perturb) {

        return MurmurHash3.mix(strategy.computeHashCode(value), perturb);
    }
    /*! #end !*/
}
