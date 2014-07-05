package com.carrotsearch.hppcrt;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

import com.carrotsearch.hppcrt.maps.ObjectIntOpenHashMap;

public class ObjectIntOpenHashMapRegressionTest
{
    /** @see "http://issues.carrot2.org/browse/HPPC-32" */
    @Test
    public void testEqualsOnObjectKeys()
    {
        final ObjectIntOpenHashMap<String> map = new ObjectIntOpenHashMap<String>();
        final String key1 = "key1";
        final String key2 = new String("key1");

        map.put(key1, 1);
        Assert.assertEquals(1, map.get(key2));
        Assert.assertEquals(1, map.put(key2, 2));
        Assert.assertEquals(1, map.size());
    }
}
