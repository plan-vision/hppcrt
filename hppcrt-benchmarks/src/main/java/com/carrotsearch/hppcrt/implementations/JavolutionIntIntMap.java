package com.carrotsearch.hppcrt.implementations;

import java.util.HashMap;
import java.util.Random;

import javolution.util.FastMap;
import javolution.util.function.Equality;

import com.carrotsearch.hppcrt.Util;
import com.carrotsearch.hppcrt.XorShiftRandom;

public class JavolutionIntIntMap extends MapImplementation<FastMap<Integer, Integer>>
{
    private Integer[] insertKeys;
    private Integer[] insertValues;
    private Integer[] containsKeys;
    private Integer[] removedKeys;

    public JavolutionIntIntMap(final int size, final float loadFactor)
    {
        //Javolution 6.0.0 "do not need" of preallocation, so let's see...
        //the no-Equaltity hangs with HIFGBITS,, so we must provide a custom Equality with
        //scrambling: use the same as Koloboke has for objects
        super(new FastMap<Integer, Integer>(new Equality<Integer>() {

            @Override
            public int hashCodeOf(final Integer object) {

                return object.intValue() * -1640531527; // magic mix;
            }

            @Override
            public boolean areEqual(final Integer left, final Integer right) {

                return left.intValue() == right.intValue();
            }

            @Override
            public int compare(final Integer left, final Integer right) {

                return left.compareTo(right);
            }
        }));
    }

    /**
     * Setup
     */
    @Override
    public void setup(final int[] keysToInsert, final MapImplementation.HASH_QUALITY hashQ, final int[] keysForContainsQuery, final int[] keysForRemovalQuery) {

        final Random prng = new XorShiftRandom(0x122335577L);

        this.insertKeys = new Integer[keysToInsert.length];
        this.insertValues = new Integer[keysToInsert.length];

        this.containsKeys = new Integer[keysForContainsQuery.length];
        this.removedKeys = new Integer[keysForRemovalQuery.length];

        //Auto box into Integers
        for (int ii = 0; ii < keysToInsert.length; ii++) {

            this.insertKeys[ii] = new Integer(keysToInsert[ii]);
            this.insertValues[ii] = new Integer(prng.nextInt());
        }

        //Auto box into Integers
        for (int ii = 0; ii < keysForContainsQuery.length; ii++) {

            this.containsKeys[ii] = new Integer(keysForContainsQuery[ii]);
        }

        //Auto box into Integers
        for (int ii = 0; ii < keysForRemovalQuery.length; ii++) {

            this.removedKeys[ii] = new Integer(keysForRemovalQuery[ii]);
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

        final FastMap<Integer, Integer> instance = this.instance;

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
        final FastMap<Integer, Integer> instance = this.instance;

        int count = 0;

        final Integer[] keys = this.containsKeys;

        for (int i = 0; i < keys.length; i++) {

            count += instance.containsKey(keys[i]) ? 1 : 0;
        }

        return count;
    }

    @Override
    public int benchRemoveKeys() {

        final FastMap<Integer, Integer> instance = this.instance;

        int count = 0;

        final Integer[] keys = this.removedKeys;

        for (int i = 0; i < keys.length; i++) {

            count += (instance.remove(keys[i]) != null) ? 1 : 0;
        }

        return count;
    }
}
