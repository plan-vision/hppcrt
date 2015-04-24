package com.carrotsearch.hppcrt;

import org.junit.Assert;
import org.junit.Test;

import com.carrotsearch.hppcrt.lists.ByteArrayList;
import com.carrotsearch.hppcrt.lists.CharArrayList;
import com.carrotsearch.hppcrt.lists.ObjectArrayList;
import com.carrotsearch.hppcrt.maps.ObjectObjectHashMap;

public class StringConversionsTest
{
    @Test
    public void testByteList()
    {
        final ByteArrayList list = new ByteArrayList();
        list.add(new byte[] { 1, 2, 3 });
        Assert.assertEquals("[1, 2, 3]", list.toString());
    }

    @Test
    public void testCharList()
    {
        final CharArrayList list = new CharArrayList();
        list.add(new char[] { 'a', 'b', 'c' });
        Assert.assertEquals("[a, b, c]", list.toString());
    }

    @Test
    public void testObjectList()
    {
        final ObjectArrayList<String> list = new ObjectArrayList<String>();
        list.add("ab", "ac", "ad");
        Assert.assertEquals("[ab, ac, ad]", list.toString());
    }

    @Test
    public void testObjectObjectMap()
    {
        final ObjectObjectHashMap<String, String> map =
                ObjectObjectHashMap.from(
                        new String[] { "a" },
                        new String[] { "b" });

        Assert.assertEquals("[a=>b]", map.toString());
    }
}
