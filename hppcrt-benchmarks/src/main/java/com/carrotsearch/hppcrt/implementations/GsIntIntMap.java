package com.carrotsearch.hppcrt.implementations;


import java.util.Arrays;
import java.util.Random;

import com.carrotsearch.hppcrt.XorShiftRandom;
import com.gs.collections.impl.map.mutable.primitive.IntIntHashMap;

public class GsIntIntMap extends MapImplementation<IntIntHashMap>
{
    private int[] insertKeys;
    private int[] containsKeys;
    private int[] removedKeys;
    private int[] insertValues;

    public GsIntIntMap(final int size, final float loadFactor)
    {
        //load factor is fixed to 0.5 !
        super(new IntIntHashMap(size));
    }

    /**
     * Setup
     */
    @Override
    public void setup(final int[] keysToInsert, final MapImplementation.HASH_QUALITY hashQ, final int[] keysForContainsQuery, final int[] keysForRemovalQuery) {

        final Random prng = new XorShiftRandom(0x122335577L);

        //make a full copy
        this.insertKeys = Arrays.copyOf(keysToInsert, keysToInsert.length);
        this.containsKeys = Arrays.copyOf(keysForContainsQuery, keysForContainsQuery.length);
        this.removedKeys = Arrays.copyOf(keysForRemovalQuery, keysForRemovalQuery.length);

        this.insertValues = new int[keysToInsert.length];

        for (int i = 0; i < this.insertValues.length; i++) {

            this.insertValues[i] = prng.nextInt();
        }
    }

    @Override
    public void clear() {
        this.instance.clear();
    }

    @Override
    public int size() {

        return this.instance.size();
    }

    @Override
    public int benchPutAll() {

        final IntIntHashMap instance = this.instance;

        final int[] values = this.insertValues;

        final int count = 0;

        final int[] keys = this.insertKeys;

        for (int i = 0; i < keys.length; i++) {

            //those ones do not return the previous value....
            instance.put(keys[i], values[i]);
        }

        return instance.size();
    }

    @Override
    public int benchContainKeys()
    {
        final IntIntHashMap instance = this.instance;

        int count = 0;

        final int[] keys = this.containsKeys;

        for (int i = 0; i < keys.length; i++) {

            count += instance.containsKey(keys[i]) ? 1 : 0;
        }

        return count;
    }

    @Override
    public int benchRemoveKeys() {

        final IntIntHashMap instance = this.instance;

        final int count = 0;

        final int[] keys = this.removedKeys;

        for (int i = 0; i < keys.length; i++) {

            //those ones do not return the previous value....
            instance.remove(keys[i]);
        }

        return instance.size();
    }

    @Override
    public void setCopyOfInstance(final MapImplementation<?> toCloneFrom) {

        //no clone, but a copy constructor
        this.instance = new IntIntHashMap(((IntIntHashMap) toCloneFrom.instance));
    }

}