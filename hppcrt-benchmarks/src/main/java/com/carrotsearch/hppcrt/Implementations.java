package com.carrotsearch.hppcrt;

import com.carrotsearch.hppcrt.maps.*;
import com.carrotsearch.hppcrt.strategies.IntStandardHash;

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

        @Override
        public MapImplementation<?> getInstance(final int size)
        {
            return new HppcMap(IntIntOpenHashMap.newInstance(size, IntIntOpenHashMap.DEFAULT_LOAD_FACTOR));
        }
    },

    HPPC_STRATEGY
    {
        @Override
        public MapImplementation<?> getInstance()
        {
            return new HppcMap(IntIntOpenCustomHashMap.newInstance(new IntStandardHash()));
        }

        @Override
        public MapImplementation<?> getInstance(final int size)
        {
            return new HppcMap(IntIntOpenCustomHashMap.newInstance(size, IntIntOpenCustomHashMap.DEFAULT_LOAD_FACTOR, new IntStandardHash()));
        }
    },

    FASTUTIL
    {
        @Override
        public MapImplementation<?> getInstance()
        {
            return new FastUtilMap();
        }

        @Override
        public MapImplementation<?> getInstance(final int size)
        {
            return new FastUtilMap(size);
        }
    },

    JAVA
    {
        @Override
        public MapImplementation<?> getInstance()
        {
            return new JavaMap();
        }

        @Override
        public MapImplementation<?> getInstance(final int size)
        {
            return new JavaMap(size);
        }
    },

    TROVE
    {
        @Override
        public MapImplementation<?> getInstance()
        {
            return new TroveMap();
        }

        @Override
        public MapImplementation<?> getInstance(final int size)
        {
            return new TroveMap(size);
        }
    },

    MAHOUT
    {
        @Override
        public MapImplementation<?> getInstance()
        {
            return new MahoutMap();
        }

        @Override
        public MapImplementation<?> getInstance(final int size)
        {
            return new MahoutMap(size);
        }
    };

    public abstract MapImplementation<?> getInstance();

    public abstract MapImplementation<?> getInstance(int size);
}