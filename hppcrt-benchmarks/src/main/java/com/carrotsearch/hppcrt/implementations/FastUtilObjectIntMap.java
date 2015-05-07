package com.carrotsearch.hppcrt.implementations;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.Random;

import com.carrotsearch.hppcrt.Util;
import com.carrotsearch.hppcrt.XorShiftRandom;

public class FastUtilObjectIntMap extends MapImplementation<Object2IntOpenHashMap<MapImplementation.ComparableInt>>
{

    private ComparableInt[] insertKeys;
    private ComparableInt[] containsKeys;
    private ComparableInt[] removedKeys;
    private int[] insertValues;

    protected FastUtilObjectIntMap(final int size, final float loadFactor)
    {
        super(new Object2IntOpenHashMap<ComparableInt>(size, loadFactor));
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

        final Object2IntOpenHashMap<ComparableInt> instance = this.instance;
        final int[] values = this.insertValues;

        int count = 0;

        final ComparableInt[] keys = this.insertKeys;

        for (int i = 0; i < keys.length; i++) {

            count += instance.put(keys[i], values[i]);
        }

        return count;
    }

    @Override
    public int benchContainKeys()
    {
        final Object2IntOpenHashMap<ComparableInt> instance = this.instance;

        int count = 0;

        final ComparableInt[] keys = this.containsKeys;

        for (int i = 0; i < keys.length; i++) {

            count += instance.containsKey(keys[i]) ? 1 : 0;
        }

        return count;
    }

    @Override
    public int benchRemoveKeys() {

        final Object2IntOpenHashMap<ComparableInt> instance = this.instance;

        int count = 0;

        final ComparableInt[] keys = this.removedKeys;

        for (int i = 0; i < keys.length; i++) {

            count += instance.removeInt(keys[i]);
        }

        return count;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setCopyOfInstance(final MapImplementation<?> toCloneFrom) {

        this.instance = ((Object2IntOpenHashMap<MapImplementation.ComparableInt>) toCloneFrom.instance).clone();

    }
}