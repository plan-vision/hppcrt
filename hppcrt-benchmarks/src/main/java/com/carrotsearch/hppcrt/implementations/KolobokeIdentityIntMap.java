package com.carrotsearch.hppcrt.implementations;

import java.util.Random;

import net.openhft.koloboke.collect.Equivalence;
import net.openhft.koloboke.collect.hash.HashConfig;
import net.openhft.koloboke.collect.map.hash.HashObjIntMap;
import net.openhft.koloboke.collect.map.hash.HashObjIntMaps;

import com.carrotsearch.hppcrt.Util;
import com.carrotsearch.hppcrt.XorShiftRandom;

public class KolobokeIdentityIntMap extends MapImplementation<HashObjIntMap<MapImplementation.ComparableInt>>
{
    private ComparableInt[] insertKeys;
    private ComparableInt[] containsKeys;
    private ComparableInt[] removedKeys;
    private int[] insertValues;

    protected KolobokeIdentityIntMap(final int size, final float loadFactor)
    {
        super(HashObjIntMaps.<MapImplementation.ComparableInt> getDefaultFactory().withKeyEquivalence(Equivalence.identity()).
                withHashConfig(HashConfig.fromLoads(loadFactor / 2, loadFactor, loadFactor)).newMutableMap(size));
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
        for (int ii = 0; ii < keysToInsert.length; ii++) {

            this.insertKeys[ii] = new ComparableInt(keysToInsert[ii], hashQ);

            this.insertValues[ii] = prng.nextInt();
        }

        //Auto box into Integers, they must have the same length anyway.
        for (int ii = 0; ii < keysForContainsQuery.length; ii++) {

            this.containsKeys[ii] = new ComparableInt(keysForContainsQuery[ii], hashQ);
        }

        //Auto box into Integers, they must have the same length anyway.
        for (int ii = 0; ii < keysForRemovalQuery.length; ii++) {

            this.removedKeys[ii] = new ComparableInt(keysForRemovalQuery[ii], hashQ);
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

        final HashObjIntMap<MapImplementation.ComparableInt> instance = this.instance;
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
        final HashObjIntMap<MapImplementation.ComparableInt> instance = this.instance;

        int count = 0;

        final ComparableInt[] keys = this.containsKeys;

        for (int i = 0; i < keys.length; i++) {

            count += instance.containsKey(keys[i]) ? 1 : 0;
        }

        return count;
    }

    @Override
    public int benchRemoveKeys() {

        final HashObjIntMap<MapImplementation.ComparableInt> instance = this.instance;

        int count = 0;

        final ComparableInt[] keys = this.removedKeys;

        for (int i = 0; i < keys.length; i++) {

            count += instance.removeAsInt(keys[i]);
        }

        return count;
    }

    @Override
    public boolean isIdentityMap() {

        return true;
    }
}