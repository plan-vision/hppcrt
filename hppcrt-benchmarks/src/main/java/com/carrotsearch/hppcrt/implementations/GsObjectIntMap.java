package com.carrotsearch.hppcrt.implementations;

import java.util.Random;

import com.carrotsearch.hppcrt.Util;
import com.carrotsearch.hppcrt.XorShiftRandom;
import com.gs.collections.impl.map.mutable.primitive.ObjectIntHashMap;

public class GsObjectIntMap extends MapImplementation<ObjectIntHashMap<MapImplementation.ComparableInt>>
{

    private ComparableInt[] insertKeys;
    private ComparableInt[] containsKeys;
    private ComparableInt[] removedKeys;
    private int[] insertValues;

    protected GsObjectIntMap(final int size, final float loadFactor)
    {
        //load factor is fixed to 0.5 !
        super(new ObjectIntHashMap<ComparableInt>(size));
    }

    /**
     * Setup
     */
    @Override
    public void setup(final int[] keysToInsert, final MapImplementation.HASH_QUALITY hashQ, final int[] keysForContainsQuery, final int[] keysForRemovalQuery) {

        final Random prng = new XorShiftRandom(0x122335577L);

        this.insertKeys = new ComparableInt[keysToInsert.length];

        this.containsKeys = new ComparableInt[keysForContainsQuery.length];
        this.removedKeys = new ComparableInt[keysForRemovalQuery.length];

        this.insertValues = new int[keysToInsert.length];

        //Auto box into Integers, they must have the same length anyway.
        for (int i = 0; i < keysToInsert.length; i++) {

            this.insertKeys[i] = new ComparableInt(keysToInsert[i], hashQ);

            this.insertValues[i] = prng.nextInt();
        }

        //Auto box into Integers
        for (int i = 0; i < keysForContainsQuery.length; i++) {

            this.containsKeys[i] = new ComparableInt(keysForContainsQuery[i], hashQ);
        }

        //Auto box into Integers
        for (int i = 0; i < keysForRemovalQuery.length; i++) {

            this.removedKeys[i] = new ComparableInt(keysForRemovalQuery[i], hashQ);
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

        final ObjectIntHashMap<ComparableInt> instance = this.instance;
        final int[] values = this.insertValues;

        final int count = 0;

        final ComparableInt[] keys = this.insertKeys;

        for (int i = 0; i < keys.length; i++) {

            instance.put(keys[i], values[i]);
        }

        return instance.size();
    }

    @Override
    public int benchContainKeys()
    {
        final ObjectIntHashMap<ComparableInt> instance = this.instance;

        int count = 0;

        final ComparableInt[] keys = this.containsKeys;

        for (int i = 0; i < keys.length; i++) {

            count += instance.containsKey(keys[i]) ? 1 : 0;
        }

        return count;
    }

    @Override
    public int benchRemoveKeys() {

        final ObjectIntHashMap<ComparableInt> instance = this.instance;

        final int count = 0;

        final ComparableInt[] keys = this.removedKeys;

        for (int i = 0; i < keys.length; i++) {

            instance.remove(keys[i]);
        }

        return instance.size();
    }
}