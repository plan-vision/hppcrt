package com.carrotsearch.hppcrt.implementations;


import java.util.Arrays;
import java.util.Random;

import net.openhft.koloboke.collect.hash.HashConfig;
import net.openhft.koloboke.collect.map.hash.HashIntIntMap;
import net.openhft.koloboke.collect.map.hash.HashIntIntMaps;
import net.openhft.koloboke.collect.map.hash.HashObjIntMap;

import com.carrotsearch.hppcrt.XorShiftRandom;

public class KolobokeIntIntMap extends MapImplementation<HashIntIntMap>
{
    private int[] insertKeys;
    private int[] containsKeys;
    private int[] removedKeys;
    private int[] insertValues;

    private final int size;
    private final float loadFactor;

    public KolobokeIntIntMap(final int size, final float loadFactor)
    {
        super(HashIntIntMaps.getDefaultFactory().
                withHashConfig(HashConfig.fromLoads(loadFactor / 2, loadFactor, loadFactor)).newMutableMap(size));
        this.size = size;
        this.loadFactor = loadFactor;
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

        final HashIntIntMap instance = this.instance;
        final int[] values = this.insertValues;

        int count = 0;

        final int[] keys = this.insertKeys;

        for (int i = 0; i < keys.length; i++) {

            count += instance.put(keys[i], values[i]);
        }

        return count;
    }

    @Override
    public int benchContainKeys()
    {
        final HashIntIntMap instance = this.instance;

        int count = 0;

        final int[] keys = this.containsKeys;

        for (int i = 0; i < keys.length; i++) {

            count += instance.containsKey(keys[i]) ? 1 : 0;
        }

        return count;
    }

    @Override
    public int benchRemoveKeys() {

        final HashIntIntMap instance = this.instance;

        int count = 0;

        final int[] keys = this.removedKeys;

        for (int i = 0; i < keys.length; i++) {

            count += instance.remove(keys[i]);
        }

        return count;
    }

    @Override
    public void setCopyOfInstance(final MapImplementation<?> toCloneFrom) {

        this.instance = HashIntIntMaps.getDefaultFactory().
                withHashConfig(HashConfig.fromLoads(this.loadFactor / 2, this.loadFactor, this.loadFactor)).newMutableMap(this.size);

        final HashIntIntMap sourceCopy = (HashIntIntMap) toCloneFrom;

        this.instance.entrySet().addAll(sourceCopy.entrySet());

    }
}