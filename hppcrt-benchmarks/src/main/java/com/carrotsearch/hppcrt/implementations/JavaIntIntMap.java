package com.carrotsearch.hppcrt.implementations;

import java.util.HashMap;
import java.util.Random;

import com.carrotsearch.hppcrt.Util;
import com.carrotsearch.hppcrt.XorShift128P;

public class JavaIntIntMap extends MapImplementation<HashMap<Integer, Integer>>
{
    private Integer[] insertKeys;
    private Integer[] insertValues;
    private Integer[] containsKeys;
    private Integer[] removedKeys;

    public JavaIntIntMap(final int size, final float loadFactor)
    {
        super(new HashMap<Integer, Integer>(size, loadFactor));
    }

    /**
     * Setup
     */
    @SuppressWarnings("boxing")
    @Override
    public void setup(final int[] keysToInsert, final MapImplementation.HASH_QUALITY hashQ, final int[] keysForContainsQuery, final int[] keysForRemovalQuery) {

        final Random prng = new XorShift128P(0x122335577L);

        this.insertKeys = new Integer[keysToInsert.length];
        this.insertValues = new Integer[keysToInsert.length];

        this.containsKeys = new Integer[keysForContainsQuery.length];
        this.removedKeys = new Integer[keysForRemovalQuery.length];

        //Auto box into Integers
        for (int ii = 0; ii < keysToInsert.length; ii++) {

            //autoboxing occurs here
            this.insertKeys[ii] = keysToInsert[ii];
            this.insertValues[ii] = prng.nextInt();
        }

        //Auto box into Integers
        for (int ii = 0; ii < keysForContainsQuery.length; ii++) {
            //autoboxing occurs here
            this.containsKeys[ii] = keysForContainsQuery[ii];
        }

        //Auto box into Integers
        for (int ii = 0; ii < keysForRemovalQuery.length; ii++) {
            //autoboxing occurs here
            this.removedKeys[ii] = keysForRemovalQuery[ii];
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

        final HashMap<Integer, Integer> instance = this.instance;

        int count = 0;

        final Integer[] keys = this.insertKeys;
        final Integer[] values = this.insertValues;

        for (int i = 0; i < keys.length; i++) {

            count += (instance.put(keys[i], values[i]) != null) ? 1 : 0;
        }

        return count;
    }

    @Override
    public int benchContainKeys()
    {
        final HashMap<Integer, Integer> instance = this.instance;

        int count = 0;

        final Integer[] keys = this.containsKeys;

        for (int i = 0; i < keys.length; i++) {

            count += instance.containsKey(keys[i]) ? 1 : 0;
        }

        return count;
    }

    @Override
    public int benchRemoveKeys() {

        final HashMap<Integer, Integer> instance = this.instance;

        int count = 0;

        final Integer[] keys = this.removedKeys;

        for (int i = 0; i < keys.length; i++) {

            count += (instance.remove(keys[i]) != null) ? 1 : 0;
        }

        return count;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setCopyOfInstance(final MapImplementation<?> toCloneFrom) {

        //copy constructor
        this.instance = new HashMap<Integer, Integer>((HashMap<Integer, Integer>) (toCloneFrom.instance));

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
