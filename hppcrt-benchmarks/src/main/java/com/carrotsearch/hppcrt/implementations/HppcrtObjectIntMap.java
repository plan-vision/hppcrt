package com.carrotsearch.hppcrt.implementations;

import java.util.Random;

import com.carrotsearch.hppcrt.Util;
import com.carrotsearch.hppcrt.XorShift128P;
import com.carrotsearch.hppcrt.maps.ObjectIntHashMap;

public class HppcrtObjectIntMap extends MapImplementation<ObjectIntHashMap<MapImplementation.ComparableInt>>
{

    private ComparableInt[] insertKeys;
    private ComparableInt[] containsKeys;
    private ComparableInt[] removedKeys;
    private int[] insertValues;

    protected HppcrtObjectIntMap(final int size, final float loadFactor)
    {
        super(new ObjectIntHashMap<ComparableInt>(size, loadFactor));
    }

    /**
     * Setup
     */
    @Override
    public void setup(final int[] keysToInsert, final MapImplementation.HASH_QUALITY hashQ, final int[] keysForContainsQuery, final int[] keysForRemovalQuery) {

        final Random prng = new XorShift128P(0x122335577L);

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

        int count = 0;

        final ComparableInt[] keys = this.removedKeys;

        for (int i = 0; i < keys.length; i++) {

            count += instance.remove(keys[i]);
        }

        return count;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setCopyOfInstance(final MapImplementation<?> toCloneFrom) {

        this.instance = ((ObjectIntHashMap<MapImplementation.ComparableInt>) toCloneFrom.instance).clone();

    }

    @Override
    public void reshuffleInsertedKeys(final Random rand) {
        Util.shuffle(this.insertKeys, rand);

    }

    @Override
    public void reshuffleInsertedValues(final Random rand) {
        Util.shuffle(this.insertValues, rand);

    }
}