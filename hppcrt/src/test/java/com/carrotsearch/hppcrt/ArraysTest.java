package com.carrotsearch.hppcrt;

import org.junit.Test;

import com.carrotsearch.hppcrt.lists.IntArrayList;

public class ArraysTest
{
    public ArraysTest() {
        //nothing
    }

    @Test
    public void testIntRotate() {

        final IntArrayList testArray = IntArrayList.from(new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 });

        IntArrays.rotate(testArray, 0, 6, 14);

        //0 1
        System.out.println(testArray.toString());
    }

}
