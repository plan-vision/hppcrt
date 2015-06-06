package com.carrotsearch.hppcrt.implementations;

import java.util.Random;

import org.openjdk.jmh.infra.Blackhole;

import net.openhft.koloboke.collect.StatelessEquivalence;
import net.openhft.koloboke.collect.hash.HashConfig;
import net.openhft.koloboke.collect.map.hash.HashObjIntMap;
import net.openhft.koloboke.collect.map.hash.HashObjIntMaps;

import com.carrotsearch.hppcrt.Util;
import com.carrotsearch.hppcrt.XorShift128P;

public class KolobokeObjectIntMap extends MapImplementation<HashObjIntMap<MapImplementation.ComparableInt>>
{
    private ComparableInt[] insertKeys;
    private ComparableInt[] containsKeys;
    private ComparableInt[] removedKeys;
    private int[] insertValues;

    private final int size;
    private final float loadFactor;

    protected KolobokeObjectIntMap(final int size, final float loadFactor)
    {
        //since Objects are not mixed in Koloboke, a strategy must be used.
        super(HashObjIntMaps.<MapImplementation.ComparableInt> getDefaultFactory().
                withHashConfig(HashConfig.fromLoads(loadFactor / 2, loadFactor, loadFactor)).
                withKeyEquivalence(new StatelessEquivalence<MapImplementation.ComparableInt>() {

                    @Override
                    public boolean equivalent(final MapImplementation.ComparableInt i1, final MapImplementation.ComparableInt i2) {

                        return i1.value == i2.value;
                    }

                    @Override
                    public int hash(final MapImplementation.ComparableInt i) {

                        return i.value * -1640531527; // magic mix
                    }
                }).
                newMutableMap(size));

        this.size = size;
        this.loadFactor = loadFactor;
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
    public void setCopyOfInstance(final MapImplementation<?> toCloneFrom) {

        @SuppressWarnings("unchecked")
        final HashObjIntMap<MapImplementation.ComparableInt> sourceCopy = (HashObjIntMap<MapImplementation.ComparableInt>) (toCloneFrom.instance);

        //copy constructor
        this.instance = HashObjIntMaps.<MapImplementation.ComparableInt> getDefaultFactory().
                withHashConfig(HashConfig.fromLoads(this.loadFactor / 2, this.loadFactor, this.loadFactor)).
                withKeyEquivalence(new StatelessEquivalence<MapImplementation.ComparableInt>() {

                    @Override
                    public boolean equivalent(final MapImplementation.ComparableInt i1, final MapImplementation.ComparableInt i2) {

                        return i1.value == i2.value;
                    }

                    @Override
                    public int hash(final MapImplementation.ComparableInt i) {

                        return i.value * -1640531527; // magic mix
                    }
                }).
                newMutableMap(sourceCopy);

    }
}