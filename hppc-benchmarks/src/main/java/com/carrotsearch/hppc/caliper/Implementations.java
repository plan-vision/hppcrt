package com.carrotsearch.hppc.caliper;

import com.carrotsearch.hppc.IntIntOpenCustomHashMap;
import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.carrotsearch.hppc.strategies.IntStandardHash;

/**
 * 
 */
public enum Implementations
{
    HPPC
    {
        @Override
        public MapImplementation<?> getInstance()
        {
            return new HppcMap(IntIntOpenHashMap.newInstance());
        }
    },

    HPPC_NOPERTURBS
    {
        @Override
        public MapImplementation<?> getInstance()
        {
            return new HppcMap(IntIntOpenHashMap.newInstanceWithoutPerturbations());
        }
    },

    HPPC_STRATEGY
    {
        @Override
        public MapImplementation<?> getInstance()
        {
            return new HppcMap(IntIntOpenCustomHashMap.newInstance(new IntStandardHash()));
        }
    },

    FASTUTIL
    {
        @Override
        public MapImplementation<?> getInstance()
        {
            return new FastUtilMap();
        }
    },

    JAVA
    {
        @Override
        public MapImplementation<?> getInstance()
        {
            return new JavaMap();
        }
    },

    TROVE
    {
        @Override
        public MapImplementation<?> getInstance()
        {
            return new TroveMap();
        }
    },

    MAHOUT
    {
        @Override
        public MapImplementation<?> getInstance()
        {
            return new MahoutMap();
        }
    };

    public abstract MapImplementation<?> getInstance();
}