package com.carrotsearch.hppcrt.implementations;


import java.util.Arrays;
import java.util.Random;

import org.apache.mahout.math.map.OpenIntIntHashMap;

import com.carrotsearch.hppcrt.XorShiftRandom;

public class MahoutIntIntMap extends MapImplementation<OpenIntIntHashMap>
{
    private int[] insertKeys;
    private int[] containsKeys;
    private int[] removedKeys;
    private int[] insertValues;

    public MahoutIntIntMap(final int size, final float loadFactor)
    {
        super(new OpenIntIntHashMap(
                size,
                0.1,
                loadFactor));
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

        final OpenIntIntHashMap instance = this.instance;
        final int[] values = this.insertValues;

        int count = 0;

        final int[] keys = this.insertKeys;

        for (int i = 0; i < keys.length; i++) {

            count += (instance.put(keys[i], values[i]) ? 1 : 0);
        }

        return count;
    }

    @Override
    public int benchContainKeys()
    {
        final OpenIntIntHashMap instance = this.instance;

        int count = 0;

        final int[] keys = this.containsKeys;

        for (int i = 0; i < keys.length; i++) {

            count += instance.containsKey(keys[i]) ? 1 : 0;
        }

        return count;
    }

    @Override
    public int benchRemoveKeys() {

        final OpenIntIntHashMap instance = this.instance;

        int count = 0;

        final int[] keys = this.removedKeys;

        for (int i = 0; i < keys.length; i++) {

            count += instance.removeKey(keys[i]) ? 1 : 0;
        }

        return count;
    }

    @Override
    public void setCopyOfInstance(final MapImplementation<?> toCloneFrom) {

        this.instance = (OpenIntIntHashMap) ((OpenIntIntHashMap) toCloneFrom.instance).clone();

    }
}