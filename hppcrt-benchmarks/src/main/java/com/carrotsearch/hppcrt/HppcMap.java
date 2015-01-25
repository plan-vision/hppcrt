package com.carrotsearch.hppcrt;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.maps.*;

public class HppcMap extends MapImplementation<IntIntMap>
{
    protected HppcMap(final IntIntMap instance)
    {
        super(instance);
    }

    @Override
    public void remove(final int k) {
        this.instance.remove(k);
    }

    @Override
    public void clear() {
        this.instance.clear();
    }

    @Override
    public void put(final int k, final int v) {
        this.instance.put(k, v);
    }

    @Override
    public int get(final int k) {
        return this.instance.get(k);
    }

    @Override
    public int containKeys(final int[] keys)
    {
        final IntIntMap prepared = this.instance;
        int count = 0;
        for (int i = 0; i < keys.length; i++) {
            count += prepared.containsKey(keys[i]) ? 1 : 0;
        }
        return count;
    }

    @Override
    public int putAll(final int[] keys, final int[] values)
    {
        final IntIntMap instance = this.instance;
        int count = 0;
        for (int i = 0; i < keys.length; i++)
        {
            count += instance.put(keys[i], values[i]);
        }
        return count;
    }
}